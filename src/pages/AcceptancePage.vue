<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import TempChart from '@/components/TempChart.vue'
import Modal from '@/components/Modal.vue'
import { useApi } from '@/composables/useApi'
import type {
  AcceptanceRecord,
  TemperatureEvidenceGap,
  ReturnTask,
  ClinicInventory,
  QuantityConfirmation,
} from '@/types'
import {
  ACCEPTANCE_STATUS_MAP,
  EVIDENCE_GAP_STATUS_MAP,
  RETURN_TASK_STATUS_MAP,
} from '@/types'
import { generateTempCurve } from '@/data/mock'
import {
  ClipboardCheck,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Thermometer,
  WifiOff,
  ArrowLeftRight,
  Eye,
  Play,
  RotateCcw,
  ShieldCheck,
  Building2,
  Stethoscope,
} from 'lucide-vue-next'

const {
  getAcceptanceRecords,
  acceptDelivery,
  rejectDelivery,
  confirmQuantity,
  acknowledgeGap,
  resumeInbound,
  getEvidenceGaps,
  getReturnTasks,
  getClinicInventory,
} = useApi()

const records = ref<AcceptanceRecord[]>([])
const evidenceGaps = ref<TemperatureEvidenceGap[]>([])
const returnTasks = ref<ReturnTask[]>([])
const clinicInventory = ref<ClinicInventory[]>([])

const showModal = ref(false)
const selectedRecord = ref<AcceptanceRecord | null>(null)
const showRejectForm = ref(false)
const rejectReasonInput = ref('')
const warehouseQtyInput = ref<number | null>(null)
const clinicQtyInput = ref<number | null>(null)

const stats = computed(() => {
  const r = records.value
  return [
    { label: '待验收', count: r.filter(x => x.status === 'pending').length, icon: ClipboardCheck, color: 'text-gray-600 bg-gray-100' },
    { label: '已通过', count: r.filter(x => x.status === 'accepted').length, icon: CheckCircle, color: 'text-green-600 bg-green-100' },
    { label: '已拒收', count: r.filter(x => x.status === 'rejected').length, icon: XCircle, color: 'text-red-600 bg-red-100' },
    { label: '数量异常', count: r.filter(x => x.status === 'qty_mismatch').length, icon: ArrowLeftRight, color: 'text-orange-600 bg-orange-100' },
    { label: '温控异常', count: r.filter(x => x.status === 'temp_gap').length, icon: Thermometer, color: 'text-yellow-600 bg-yellow-100' },
    { label: '证据缺口', count: r.filter(x => x.status === 'evidence_gap').length, icon: WifiOff, color: 'text-purple-600 bg-purple-100' },
  ]
})

const relatedGaps = computed(() => {
  if (!selectedRecord.value) return []
  return evidenceGaps.value.filter(g => g.distributionOrderId === selectedRecord.value!.distributionOrderId)
})

const relatedReturnTask = computed(() => {
  if (!selectedRecord.value?.returnTaskId) return null
  return returnTasks.value.find(t => t.id === selectedRecord.value!.returnTaskId) || null
})

const tempCurveData = computed(() => {
  if (!selectedRecord.value) return []
  return generateTempCurve(4, 6, 3)
})

const columns = [
  { key: 'orderNo', label: '配送单号' },
  { key: 'batchNo', label: '批号' },
  { key: 'vaccineName', label: '疫苗名称' },
  { key: 'sentQty', label: '实发数量', align: 'center' as const },
  { key: 'receivedQty', label: '实收数量', align: 'center' as const },
  { key: 'status', label: '状态', align: 'center' as const, slot: true },
  { key: 'arrivalTime', label: '到货时间', slot: true },
]

function openDetail(record: AcceptanceRecord) {
  selectedRecord.value = { ...record }
  showModal.value = true
  showRejectForm.value = false
  rejectReasonInput.value = ''
  warehouseQtyInput.value = record.warehouseConfirmedQty
  clinicQtyInput.value = record.clinicConfirmedQty
}

function closeModal() {
  showModal.value = false
  selectedRecord.value = null
}

function canAccept(record: AcceptanceRecord): boolean {
  return record.status === 'pending' && record.sealIntact && record.tempCurveOk
}

async function handleAccept() {
  if (!selectedRecord.value) return
  await acceptDelivery(selectedRecord.value.id, selectedRecord.value.receivedQty)
  await loadData()
  closeModal()
}

async function handleReject() {
  if (!selectedRecord.value || !rejectReasonInput.value.trim()) return
  await rejectDelivery(selectedRecord.value.id, rejectReasonInput.value.trim())
  await loadData()
  closeModal()
}

async function handleConfirmWarehouseQty() {
  if (!selectedRecord.value || warehouseQtyInput.value == null) return
  await confirmQuantity(selectedRecord.value.id, 'warehouse', warehouseQtyInput.value)
  await loadData()
  const updated = records.value.find(r => r.id === selectedRecord.value!.id)
  if (updated) selectedRecord.value = { ...updated }
}

async function handleConfirmClinicQty() {
  if (!selectedRecord.value || clinicQtyInput.value == null) return
  await confirmQuantity(selectedRecord.value.id, 'clinic', clinicQtyInput.value)
  await loadData()
  const updated = records.value.find(r => r.id === selectedRecord.value!.id)
  if (updated) selectedRecord.value = { ...updated }
}

async function handleAcknowledgeGap() {
  if (!selectedRecord.value) return
  await acknowledgeGap(selectedRecord.value.id)
  await loadData()
  const updated = records.value.find(r => r.id === selectedRecord.value!.id)
  if (updated) selectedRecord.value = { ...updated }
}

async function handleResumeInbound() {
  if (!selectedRecord.value) return
  await resumeInbound(selectedRecord.value.id)
  await loadData()
  const updated = records.value.find(r => r.id === selectedRecord.value!.id)
  if (updated) selectedRecord.value = { ...updated }
}

async function loadData() {
  records.value = await getAcceptanceRecords()
  evidenceGaps.value = await getEvidenceGaps()
  returnTasks.value = await getReturnTasks()
  clinicInventory.value = await getClinicInventory()
}

onMounted(loadData)
</script>

<template>
  <div class="space-y-6">
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
      <div
        v-for="stat in stats"
        :key="stat.label"
        class="card flex items-center gap-3"
      >
        <div class="p-2 rounded-lg" :class="stat.color">
          <component :is="stat.icon" class="w-5 h-5" />
        </div>
        <div>
          <div class="text-2xl font-bold text-gray-800">{{ stat.count }}</div>
          <div class="text-xs text-gray-500">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <DataTable
      :columns="columns"
      :data="records"
      @row-click="openDetail"
    >
      <template #cell-status="{ row }">
        <StatusBadge :status="row.status" :status-map="ACCEPTANCE_STATUS_MAP" />
      </template>
      <template #cell-arrivalTime="{ row }">
        {{ row.arrivalTime ? new Date(row.arrivalTime).toLocaleString('zh-CN') : '—' }}
      </template>
    </DataTable>

    <Modal
      :visible="showModal"
      title="验收详情"
      width="720px"
      @close="closeModal"
    >
      <template v-if="selectedRecord">
        <div class="space-y-5">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label">配送单号</label>
              <div class="text-sm text-gray-800">{{ selectedRecord.orderNo }}</div>
            </div>
            <div>
              <label class="form-label">批号</label>
              <div class="text-sm text-gray-800">{{ selectedRecord.batchNo }}</div>
            </div>
            <div>
              <label class="form-label">疫苗名称</label>
              <div class="text-sm text-gray-800">{{ selectedRecord.vaccineName }}</div>
            </div>
            <div>
              <label class="form-label">当前状态</label>
              <StatusBadge :status="selectedRecord.status" :status-map="ACCEPTANCE_STATUS_MAP" />
            </div>
            <div>
              <label class="form-label">实发数量</label>
              <div class="text-sm text-gray-800">{{ selectedRecord.sentQty }}</div>
            </div>
            <div>
              <label class="form-label">实收数量</label>
              <div class="text-sm text-gray-800">{{ selectedRecord.receivedQty }}</div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="p-3 rounded-lg border" :class="selectedRecord.sealIntact ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'">
              <div class="flex items-center gap-2">
                <ShieldCheck class="w-4 h-4" :class="selectedRecord.sealIntact ? 'text-green-600' : 'text-red-600'" />
                <span class="text-sm font-medium" :class="selectedRecord.sealIntact ? 'text-green-700' : 'text-red-700'">
                  封签检查：{{ selectedRecord.sealIntact ? '完好' : '异常' }}
                </span>
              </div>
            </div>
            <div class="p-3 rounded-lg border" :class="selectedRecord.tempCurveOk ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'">
              <div class="flex items-center gap-2">
                <Thermometer class="w-4 h-4" :class="selectedRecord.tempCurveOk ? 'text-green-600' : 'text-red-600'" />
                <span class="text-sm font-medium" :class="selectedRecord.tempCurveOk ? 'text-green-700' : 'text-red-700'">
                  温度曲线：{{ selectedRecord.tempCurveOk ? '正常' : '异常' }}
                </span>
              </div>
            </div>
          </div>

          <div v-if="selectedRecord.status === 'evidence_gap'" class="space-y-4">
            <div class="p-4 rounded-lg bg-purple-50 border border-purple-200">
              <div class="flex items-center gap-2 mb-3">
                <WifiOff class="w-5 h-5 text-purple-600" />
                <span class="text-sm font-semibold text-purple-800">探头离线 — 温控证据缺口</span>
              </div>
              <p class="text-sm text-purple-700 mb-3">
                运输过程中温度探头发生离线，导致部分时段温控数据缺失，无法确认该时段温度是否合规。
              </p>
              <div v-for="gap in relatedGaps" :key="gap.id" class="space-y-2 bg-white rounded-lg p-3 border border-purple-100">
                <div class="grid grid-cols-2 gap-2 text-sm">
                  <div>
                    <span class="text-gray-500">探头编号：</span>
                    <span class="text-gray-800 font-medium">{{ gap.probeNo }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">状态：</span>
                    <StatusBadge :status="gap.status" :status-map="EVIDENCE_GAP_STATUS_MAP" />
                  </div>
                  <div>
                    <span class="text-gray-500">离线时间：</span>
                    <span class="text-gray-800">{{ new Date(gap.offlineAt).toLocaleString('zh-CN') }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">恢复时间：</span>
                    <span class="text-gray-800">{{ gap.backOnlineAt ? new Date(gap.backOnlineAt).toLocaleString('zh-CN') : '尚未恢复' }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">缺失时长：</span>
                    <span class="text-red-600 font-medium">{{ gap.duration != null ? gap.duration + ' 分钟' : '未知' }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">说明：</span>
                    <span class="text-gray-800">{{ gap.description }}</span>
                  </div>
                </div>
              </div>
              <div v-if="relatedGaps.length === 0" class="text-sm text-purple-500">
                暂无关联证据缺口记录
              </div>
            </div>
            <div class="p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div class="flex items-center gap-2">
                <AlertTriangle class="w-4 h-4 text-yellow-600" />
                <span class="text-sm text-yellow-700">门诊入库已暂停，需确认证据缺口后方可恢复</span>
              </div>
            </div>
          </div>

          <div v-if="selectedRecord.status === 'temp_gap'" class="space-y-4">
            <div class="p-4 rounded-lg bg-yellow-50 border border-yellow-200">
              <div class="flex items-center gap-2 mb-3">
                <Thermometer class="w-5 h-5 text-yellow-600" />
                <span class="text-sm font-semibold text-yellow-800">温度曲线缺失 — 暂停门诊入库</span>
              </div>
              <p class="text-sm text-yellow-700 mb-3">
                运输温度曲线存在异常区间，门诊入库流程已暂停。请核实温度曲线后方可恢复入库。
              </p>
            </div>

            <div>
              <label class="form-label">温度曲线</label>
              <TempChart
                :data="tempCurveData"
                :min-temp="2"
                :max-temp="8"
              />
            </div>

            <div class="p-3 bg-red-50 border border-red-200 rounded-lg">
              <div class="flex items-center gap-2">
                <AlertTriangle class="w-4 h-4 text-red-600" />
                <span class="text-sm font-medium text-red-700">
                  当前状态：门诊入库已暂停，需验证温度曲线后方可恢复
                </span>
              </div>
            </div>
          </div>

          <div v-if="selectedRecord.status === 'rejected'" class="space-y-4">
            <div class="p-4 rounded-lg bg-red-50 border border-red-200">
              <div class="flex items-center gap-2 mb-3">
                <XCircle class="w-5 h-5 text-red-600" />
                <span class="text-sm font-semibold text-red-800">门诊拒收 — 生成回库任务</span>
              </div>
              <div class="space-y-2">
                <div>
                  <span class="text-sm text-gray-500">拒收原因：</span>
                  <span class="text-sm text-red-700 font-medium">{{ selectedRecord.rejectionReason || '未填写' }}</span>
                </div>
              </div>
            </div>

            <div v-if="relatedReturnTask" class="p-4 rounded-lg bg-orange-50 border border-orange-200">
              <div class="flex items-center gap-2 mb-3">
                <RotateCcw class="w-5 h-5 text-orange-600" />
                <span class="text-sm font-semibold text-orange-800">回库任务</span>
              </div>
              <div class="grid grid-cols-2 gap-2 text-sm">
                <div>
                  <span class="text-gray-500">任务编号：</span>
                  <span class="text-gray-800 font-medium">{{ relatedReturnTask.id }}</span>
                </div>
                <div>
                  <span class="text-gray-500">关联单号：</span>
                  <span class="text-gray-800">{{ relatedReturnTask.orderNo }}</span>
                </div>
                <div>
                  <span class="text-gray-500">回库原因：</span>
                  <span class="text-gray-800">{{ relatedReturnTask.reason }}</span>
                </div>
                <div>
                  <span class="text-gray-500">任务状态：</span>
                  <StatusBadge :status="relatedReturnTask.status" :status-map="RETURN_TASK_STATUS_MAP" />
                </div>
                <div>
                  <span class="text-gray-500">创建时间：</span>
                  <span class="text-gray-800">{{ new Date(relatedReturnTask.createdAt).toLocaleString('zh-CN') }}</span>
                </div>
                <div>
                  <span class="text-gray-500">更新时间：</span>
                  <span class="text-gray-800">{{ new Date(relatedReturnTask.updatedAt).toLocaleString('zh-CN') }}</span>
                </div>
              </div>
            </div>
            <div v-else class="p-3 bg-gray-50 border border-gray-200 rounded-lg text-sm text-gray-500">
              暂无回库任务记录
            </div>
          </div>

          <div v-if="selectedRecord.status === 'qty_mismatch'" class="space-y-4">
            <div class="p-4 rounded-lg bg-orange-50 border border-orange-200">
              <div class="flex items-center gap-2 mb-3">
                <ArrowLeftRight class="w-5 h-5 text-orange-600" />
                <span class="text-sm font-semibold text-orange-800">数量不一致 — 仓库和门诊分别确认</span>
              </div>
              <p class="text-sm text-orange-700">
                实发数量与实收数量不一致，需仓库和门诊分别确认各自数量后，再进行后续处理。
              </p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="p-4 rounded-lg bg-blue-50 border border-blue-200">
                <div class="flex items-center gap-2 mb-3">
                  <Building2 class="w-4 h-4 text-blue-600" />
                  <span class="text-sm font-semibold text-blue-800">仓库确认（实发数量）</span>
                </div>
                <div class="space-y-3">
                  <div>
                    <span class="text-xs text-gray-500">系统记录实发：</span>
                    <span class="text-sm font-medium text-gray-800">{{ selectedRecord.sentQty }}</span>
                  </div>
                  <div>
                    <label class="form-label">确认实发数量</label>
                    <input
                      v-model.number="warehouseQtyInput"
                      type="number"
                      class="form-input"
                      placeholder="请输入实发数量"
                    />
                  </div>
                  <button
                    class="btn-primary btn-sm w-full"
                    :disabled="warehouseQtyInput == null || warehouseQtyInput < 0"
                    @click="handleConfirmWarehouseQty"
                  >
                    确认实发
                  </button>
                </div>
              </div>

              <div class="p-4 rounded-lg bg-teal-50 border border-teal-200">
                <div class="flex items-center gap-2 mb-3">
                  <Stethoscope class="w-4 h-4 text-teal-600" />
                  <span class="text-sm font-semibold text-teal-800">门诊确认（实收数量）</span>
                </div>
                <div class="space-y-3">
                  <div>
                    <span class="text-xs text-gray-500">系统记录实收：</span>
                    <span class="text-sm font-medium text-gray-800">{{ selectedRecord.receivedQty }}</span>
                  </div>
                  <div>
                    <label class="form-label">确认实收数量</label>
                    <input
                      v-model.number="clinicQtyInput"
                      type="number"
                      class="form-input"
                      placeholder="请输入实收数量"
                    />
                  </div>
                  <button
                    class="btn-success btn-sm w-full"
                    :disabled="clinicQtyInput == null || clinicQtyInput < 0"
                    @click="handleConfirmClinicQty"
                  >
                    确认实收
                  </button>
                </div>
              </div>
            </div>

            <div v-if="selectedRecord.warehouseConfirmedQty != null || selectedRecord.clinicConfirmedQty != null" class="p-3 bg-gray-50 border border-gray-200 rounded-lg">
              <div class="text-sm space-y-1">
                <div v-if="selectedRecord.warehouseConfirmedQty != null">
                  <span class="text-gray-500">仓库已确认实发：</span>
                  <span class="text-green-700 font-medium">{{ selectedRecord.warehouseConfirmedQty }}</span>
                </div>
                <div v-if="selectedRecord.clinicConfirmedQty != null">
                  <span class="text-gray-500">门诊已确认实收：</span>
                  <span class="text-green-700 font-medium">{{ selectedRecord.clinicConfirmedQty }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-3 pt-4 border-t border-gray-200">
            <button
              v-if="canAccept(selectedRecord)"
              class="btn-success"
              @click="handleAccept"
            >
              <CheckCircle class="w-4 h-4 mr-1" />
              验收通过
            </button>

            <template v-if="selectedRecord.status === 'pending' || selectedRecord.status === 'evidence_gap' || selectedRecord.status === 'temp_gap'">
              <button
                v-if="!showRejectForm"
                class="btn-danger"
                @click="showRejectForm = true"
              >
                <XCircle class="w-4 h-4 mr-1" />
                拒收
              </button>
            </template>

            <button
              v-if="selectedRecord.status === 'evidence_gap'"
              class="btn-primary"
              @click="handleAcknowledgeGap"
            >
              <Eye class="w-4 h-4 mr-1" />
              确认缺口
            </button>

            <button
              v-if="selectedRecord.status === 'temp_gap'"
              class="btn-primary"
              @click="handleResumeInbound"
            >
              <Play class="w-4 h-4 mr-1" />
              恢复入库
            </button>

            <button class="btn-secondary" @click="closeModal">关闭</button>
          </div>

          <div v-if="showRejectForm" class="p-4 bg-red-50 border border-red-200 rounded-lg space-y-3">
            <label class="form-label">拒收原因</label>
            <textarea
              v-model="rejectReasonInput"
              class="form-input"
              rows="3"
              placeholder="请输入拒收原因"
            ></textarea>
            <div class="flex items-center gap-2">
              <button
                class="btn-danger"
                :disabled="!rejectReasonInput.trim()"
                @click="handleReject"
              >
                确认拒收
              </button>
              <button class="btn-secondary" @click="showRejectForm = false">取消</button>
            </div>
          </div>
        </div>
      </template>
    </Modal>
  </div>
</template>
