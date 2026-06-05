<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Truck, Wifi, WifiOff, AlertTriangle, RefreshCw, Eye, Send, ChevronDown, ChevronUp } from 'lucide-vue-next'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import TempChart from '@/components/TempChart.vue'
import Modal from '@/components/Modal.vue'
import { useApi } from '@/composables/useApi'
import { generateTempCurve } from '@/data/mock'
import type {
  DistributionOrder,
  TransitReport,
  TemperatureEvidenceGap,
  TemperatureRecord,
} from '@/types'
import {
  ORDER_STATUS_MAP,
  PROBE_STATUS_MAP,
  EVIDENCE_GAP_STATUS_MAP,
} from '@/types'

const { getDistributionOrders, getTransitReports, submitTransitReport, getEvidenceGaps } = useApi()

const orders = ref<DistributionOrder[]>([])
const reports = ref<TransitReport[]>([])
const evidenceGaps = ref<TemperatureEvidenceGap[]>([])
const selectedOrder = ref<DistributionOrder | null>(null)
const detailModalVisible = ref(false)
const detailReport = ref<TransitReport | null>(null)

const showReportForm = ref(false)
const submitting = ref(false)
const reportForm = ref({
  temperature: 5.0,
  latitude: 31.2304,
  longitude: 121.4737,
  locationDesc: '',
  stopPoint: '',
  boxOpenRecord: '',
  probeStatus: 'online',
  probeBatteryLevel: 95,
})

const transitOrders = computed(() =>
  orders.value.filter(o => o.status === 'in_transit' || o.status === 'outbound')
)

const tempData = ref<TemperatureRecord[]>(generateTempCurve(4, 8, 3))

const onlineProbes = computed(() =>
  reports.value.filter(r => r.probeStatus === 'online').length
)

const offlineProbes = computed(() =>
  reports.value.filter(r => r.probeStatus === 'offline' || r.probeStatus === 'low_battery').length
)

const orderEvidenceGaps = computed(() => {
  if (!selectedOrder.value) return []
  return evidenceGaps.value.filter(g => g.distributionOrderId === selectedOrder.value!.id)
})

const orderReports = computed(() => {
  if (!selectedOrder.value) return []
  return reports.value.filter(r => r.distributionOrderId === selectedOrder.value!.id)
})

const stats = computed(() => [
  { label: '在途车辆', value: transitOrders.value.length, icon: Truck, color: 'text-blue-600', bg: 'bg-blue-50' },
  { label: '在线探头', value: onlineProbes.value, icon: Wifi, color: 'text-green-600', bg: 'bg-green-50' },
  { label: '离线探头', value: offlineProbes.value, icon: WifiOff, color: 'text-red-600', bg: 'bg-red-50' },
  { label: '温控缺口', value: evidenceGaps.value.filter(g => g.status === 'open').length, icon: AlertTriangle, color: 'text-yellow-600', bg: 'bg-yellow-50' },
])

const orderColumns = [
  { key: 'orderNo', label: '订单号', width: '180px' },
  { key: 'vaccineName', label: '疫苗名称' },
  { key: 'vehicleNo', label: '车牌号', width: '110px' },
  { key: 'targetClinic', label: '目标门诊' },
  { key: 'status', label: '状态', width: '90px', slot: true },
]

const reportColumns = [
  { key: 'reportTime', label: '上报时间', width: '160px' },
  { key: 'locationDesc', label: '位置' },
  { key: 'temperature', label: '温度(°C)', width: '90px', align: 'center' as const },
  { key: 'stopPoint', label: '停留点', width: '140px' },
  { key: 'boxOpenRecord', label: '开箱记录', width: '140px' },
  { key: 'probeStatus', label: '探头状态', width: '100px', slot: true },
]

async function loadData() {
  const [o, r, g] = await Promise.all([
    getDistributionOrders(),
    getTransitReports(),
    getEvidenceGaps(),
  ])
  orders.value = o
  reports.value = r
  evidenceGaps.value = g
}

function handleOrderSelect(order: DistributionOrder) {
  selectedOrder.value = order
  tempData.value = generateTempCurve(4, 8, 3)
  showReportForm.value = false
}

function handleReportRowClick(report: TransitReport) {
  detailReport.value = report
  detailModalVisible.value = true
}

async function handleSubmitReport() {
  if (!selectedOrder.value) return
  submitting.value = true
  try {
    await submitTransitReport({
      distributionOrderId: selectedOrder.value.id,
      temperature: reportForm.value.temperature,
      latitude: reportForm.value.latitude,
      longitude: reportForm.value.longitude,
      locationDesc: reportForm.value.locationDesc,
      stopPoint: reportForm.value.stopPoint || null,
      boxOpenRecord: reportForm.value.boxOpenRecord || null,
      probeStatus: reportForm.value.probeStatus,
      probeBatteryLevel: reportForm.value.probeBatteryLevel,
    })
    await loadData()
    reportForm.value = {
      temperature: 5.0,
      latitude: 31.2304,
      longitude: 121.4737,
      locationDesc: '',
      stopPoint: '',
      boxOpenRecord: '',
      probeStatus: 'online',
      probeBatteryLevel: 95,
    }
    showReportForm.value = false
  } finally {
    submitting.value = false
  }
}

function formatTime(iso: string) {
  if (!iso) return '-'
  return iso.replace('T', ' ').slice(0, 16)
}

onMounted(loadData)
</script>

<template>
  <div class="flex flex-col gap-6">
    <div class="grid grid-cols-4 gap-4">
      <div
        v-for="s in stats"
        :key="s.label"
        class="card flex items-center gap-4"
      >
        <div :class="[s.bg, s.color]" class="p-3 rounded-lg">
          <component :is="s.icon" class="w-5 h-5" />
        </div>
        <div>
          <div class="text-2xl font-bold text-gray-800">{{ s.value }}</div>
          <div class="text-xs text-gray-500">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <div class="flex gap-6" style="min-height: 600px">
      <div class="w-[420px] shrink-0 flex flex-col gap-4">
        <div class="flex items-center justify-between">
          <h2 class="section-title">在途订单</h2>
          <button class="btn-secondary btn-sm" @click="loadData">
            <RefreshCw class="w-3.5 h-3.5 mr-1" />
            刷新
          </button>
        </div>
        <DataTable
          :columns="orderColumns"
          :data="transitOrders"
          :selectable="true"
          :page-size="8"
          @select="handleOrderSelect"
        >
          <template #cell-status="{ row }">
            <StatusBadge :status="row.status" :status-map="ORDER_STATUS_MAP" />
          </template>
        </DataTable>
      </div>

      <div class="flex-1 flex flex-col gap-6" style="min-width: 0">
        <template v-if="selectedOrder">
          <div class="card">
            <div class="flex items-center justify-between mb-4">
              <h2 class="section-title">温度曲线</h2>
              <span class="text-xs text-gray-400">{{ selectedOrder.orderNo }}</span>
            </div>
            <TempChart :data="tempData" :min-temp="2" :max-temp="8" :height="220" />
          </div>

          <div class="card">
            <div class="flex items-center justify-between mb-4">
              <h2 class="section-title">在途报告</h2>
              <button
                class="btn-primary btn-sm"
                @click="showReportForm = !showReportForm"
              >
                <Send class="w-3.5 h-3.5 mr-1" />
                上报运输数据
                <component :is="showReportForm ? ChevronUp : ChevronDown" class="w-3.5 h-3.5 ml-1" />
              </button>
            </div>

            <div v-if="showReportForm" class="mb-4 p-4 bg-blue-50/50 border border-blue-100 rounded-lg">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">温度 (°C)</label>
                  <input v-model.number="reportForm.temperature" type="number" step="0.1" class="form-input" />
                </div>
                <div>
                  <label class="form-label">探头状态</label>
                  <select v-model="reportForm.probeStatus" class="form-input">
                    <option value="online">在线</option>
                    <option value="offline">离线</option>
                    <option value="low_battery">低电量</option>
                  </select>
                </div>
                <div>
                  <label class="form-label">纬度</label>
                  <input v-model.number="reportForm.latitude" type="number" step="0.0001" class="form-input" />
                </div>
                <div>
                  <label class="form-label">经度</label>
                  <input v-model.number="reportForm.longitude" type="number" step="0.0001" class="form-input" />
                </div>
                <div class="col-span-2">
                  <label class="form-label">位置描述</label>
                  <input v-model="reportForm.locationDesc" type="text" class="form-input" placeholder="如：G15沈海高速苏州段" />
                </div>
                <div>
                  <label class="form-label">停留点</label>
                  <input v-model="reportForm.stopPoint" type="text" class="form-input" placeholder="如：苏州服务区（选填）" />
                </div>
                <div>
                  <label class="form-label">开箱记录</label>
                  <input v-model="reportForm.boxOpenRecord" type="text" class="form-input" placeholder="如：苏州站开箱检查（选填）" />
                </div>
                <div>
                  <label class="form-label">探头电量 (%)</label>
                  <input v-model.number="reportForm.probeBatteryLevel" type="number" min="0" max="100" class="form-input" />
                </div>
              </div>
              <div class="flex items-center gap-2 mt-3">
                <button
                  class="btn-primary"
                  :disabled="submitting || !reportForm.locationDesc.trim()"
                  @click="handleSubmitReport"
                >
                  <Send class="w-4 h-4 mr-1" />
                  {{ submitting ? '提交中...' : '提交上报' }}
                </button>
                <button class="btn-secondary" @click="showReportForm = false">取消</button>
              </div>
            </div>

            <DataTable
              :columns="reportColumns"
              :data="orderReports"
              :page-size="5"
              @row-click="handleReportRowClick"
            >
              <template #cell-probeStatus="{ row }">
                <StatusBadge :status="row.probeStatus" :status-map="PROBE_STATUS_MAP" />
              </template>
            </DataTable>
          </div>

          <div v-if="orderEvidenceGaps.length > 0" class="card">
            <h2 class="section-title mb-4">温控证据缺口</h2>
            <div class="flex flex-col gap-3">
              <div
                v-for="gap in orderEvidenceGaps"
                :key="gap.id"
                class="flex items-start gap-3 p-3 rounded-lg border border-gray-100 bg-gray-50/50"
              >
                <AlertTriangle class="w-4 h-4 text-yellow-500 mt-0.5 shrink-0" />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="text-sm font-medium text-gray-800">{{ gap.probeNo }}</span>
                    <StatusBadge :status="gap.status" :status-map="EVIDENCE_GAP_STATUS_MAP" />
                  </div>
                  <p class="text-xs text-gray-600 mb-1">{{ gap.description }}</p>
                  <div class="flex items-center gap-4 text-xs text-gray-400">
                    <span>离线时间：{{ formatTime(gap.offlineAt) }}</span>
                    <span v-if="gap.backOnlineAt">恢复时间：{{ formatTime(gap.backOnlineAt) }}</span>
                    <span v-if="gap.duration != null">持续：{{ gap.duration }}分钟</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <div v-else class="card flex flex-col items-center justify-center text-gray-400" style="min-height: 400px">
          <Eye class="w-10 h-10 mb-3 text-gray-300" />
          <p class="text-sm">请从左侧选择一个在途订单查看详情</p>
        </div>
      </div>
    </div>

    <Modal
      title="报告详情"
      :visible="detailModalVisible"
      width="480px"
      @close="detailModalVisible = false"
    >
      <template v-if="detailReport">
        <div class="flex flex-col gap-3 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-500">上报时间</span>
            <span class="text-gray-800">{{ formatTime(detailReport.reportTime) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">位置</span>
            <span class="text-gray-800">{{ detailReport.locationDesc }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">温度</span>
            <span class="text-gray-800 font-medium">{{ detailReport.temperature }}°C</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">停留点</span>
            <span class="text-gray-800">{{ detailReport.stopPoint || '无' }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">开箱记录</span>
            <span class="text-gray-800">{{ detailReport.boxOpenRecord || '无' }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">探头状态</span>
            <StatusBadge :status="detailReport.probeStatus" :status-map="PROBE_STATUS_MAP" />
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">探头电量</span>
            <span class="text-gray-800">{{ detailReport.probeBatteryLevel }}%</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">坐标</span>
            <span class="text-gray-800">{{ detailReport.latitude }}, {{ detailReport.longitude }}</span>
          </div>
        </div>
      </template>
    </Modal>
  </div>
</template>
