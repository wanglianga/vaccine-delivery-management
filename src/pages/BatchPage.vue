<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, FileText, Search, AlertTriangle, Clock, Package, ChevronDown, ChevronRight, Truck, Thermometer, ShieldAlert } from 'lucide-vue-next'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import Modal from '@/components/Modal.vue'
import { useApi } from '@/composables/useApi'
import type { VaccineBatch, BatchAdjustmentRecord, ColdChainBox, TransitSegment, BoxAcceptanceRecord } from '@/types'
import { BATCH_STATUS_MAP, TEMP_ZONE_OPTIONS, BOX_STATUS_MAP, SEGMENT_STATUS_MAP, ACCEPTANCE_STATUS_MAP } from '@/types'

const { getBatches, createBatch, loading, getBatchAdjustmentRecords, getColdChainBoxes, getBoxSegments, getBoxAcceptance } = useApi()

const batches = ref<VaccineBatch[]>([])
const searchQuery = ref('')
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const selectedBatch = ref<VaccineBatch | null>(null)
const adjustmentRecords = ref<BatchAdjustmentRecord[]>([])
const coldChainBoxes = ref<ColdChainBox[]>([])
const expandedBoxIds = ref<Set<string>>(new Set())
const boxSegmentsMap = ref<Map<string, TransitSegment[]>>(new Map())
const boxAcceptanceMap = ref<Map<string, BoxAcceptanceRecord>>(new Map())

const newBatch = ref({
  batchNo: '',
  vaccineName: '',
  manufacturer: '',
  specification: '',
  expiryDate: '',
  storageTempZone: '2-8°C' as string,
  batchReleaseDoc: '',
  distributableQty: 0,
  totalQty: 0,
  status: 'available' as VaccineBatch['status'],
})

const NEAR_EXPIRY_DAYS = 90

function getDaysToExpiry(expiryDate: string): number {
  const expiry = new Date(expiryDate)
  const now = new Date()
  return Math.ceil((expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
}

function isNearExpiry(batch: VaccineBatch): boolean {
  return getDaysToExpiry(batch.expiryDate) <= NEAR_EXPIRY_DAYS && getDaysToExpiry(batch.expiryDate) > 0
}

const stats = computed(() => ({
  total: batches.value.length,
  available: batches.value.filter(b => b.status === 'available').length,
  partial: batches.value.filter(b => b.status === 'partial').length,
  expired: batches.value.filter(b => b.status === 'expired').length,
  nearExpiry: batches.value.filter(b => isNearExpiry(b)).length,
}))

const filteredBatches = computed(() => {
  if (!searchQuery.value) return batches.value
  const q = searchQuery.value.toLowerCase()
  return batches.value.filter(
    b => b.batchNo.toLowerCase().includes(q) || b.vaccineName.toLowerCase().includes(q)
  )
})

const columns = [
  { key: 'recommendedOrder', label: '推荐顺序', width: '90px', align: 'center' as const, slot: true },
  { key: 'batchNo', label: '批次号', width: '120px' },
  { key: 'vaccineName', label: '疫苗名称', width: '140px' },
  { key: 'manufacturer', label: '生产企业', width: '120px' },
  { key: 'specification', label: '规格', width: '80px' },
  { key: 'expiryDate', label: '有效期至', width: '130px', slot: true },
  { key: 'storageTempZone', label: '储运温度', width: '90px', align: 'center' as const },
  { key: 'distributableQty', label: '可配送', width: '80px', align: 'right' as const },
  { key: 'status', label: '状态', width: '100px', align: 'center' as const, slot: true },
]

async function loadBatches() {
  batches.value = await getBatches()
}

async function handleRowClick(row: VaccineBatch) {
  selectedBatch.value = row
  adjustmentRecords.value = await getBatchAdjustmentRecords(row.id)
  coldChainBoxes.value = await getColdChainBoxes(row.id)
  expandedBoxIds.value = new Set()
  boxSegmentsMap.value = new Map()
  boxAcceptanceMap.value = new Map()
  showDetailModal.value = true
}

async function toggleBoxExpand(box: ColdChainBox) {
  if (expandedBoxIds.value.has(box.id)) {
    expandedBoxIds.value.delete(box.id)
  } else {
    expandedBoxIds.value.add(box.id)
    if (!boxSegmentsMap.value.has(box.id)) {
      const [segments, acceptance] = await Promise.all([
        getBoxSegments(box.id),
        getBoxAcceptance(box.id),
      ])
      boxSegmentsMap.value.set(box.id, segments)
      boxAcceptanceMap.value.set(box.id, acceptance)
    }
  }
  expandedBoxIds.value = new Set(expandedBoxIds.value)
}

function getBoxStats(boxes: ColdChainBox[]) {
  const total = boxes.length
  const inTransit = boxes.filter(b => b.status === 'in_transit' || b.status === 'transferring' || b.status === 'outbound').length
  const accepted = boxes.filter(b => b.status === 'accepted').length
  const delayed = boxes.filter(b => b.status === 'delayed').length
  const rejected = boxes.filter(b => b.status === 'rejected').length
  return { total, inTransit, accepted, delayed, rejected }
}

function openCreateModal() {
  newBatch.value = {
    batchNo: '',
    vaccineName: '',
    manufacturer: '',
    specification: '',
    expiryDate: '',
    storageTempZone: '2-8°C',
    batchReleaseDoc: '',
    distributableQty: 0,
    totalQty: 0,
    status: 'available',
  }
  showCreateModal.value = true
}

async function handleCreate() {
  await createBatch(newBatch.value)
  showCreateModal.value = false
  await loadBatches()
}

onMounted(loadBatches)
</script>

<template>
  <div class="space-y-6">
    <div class="grid grid-cols-5 gap-4">
      <div class="card flex items-center gap-4">
        <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
          <FileText class="w-5 h-5 text-blue-600" />
        </div>
        <div>
          <div class="text-sm text-gray-500">总批次数</div>
          <div class="text-2xl font-semibold text-gray-800">{{ stats.total }}</div>
        </div>
      </div>
      <div class="card flex items-center gap-4">
        <div class="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center">
          <FileText class="w-5 h-5 text-green-600" />
        </div>
        <div>
          <div class="text-sm text-gray-500">可配送</div>
          <div class="text-2xl font-semibold text-green-700">{{ stats.available }}</div>
        </div>
      </div>
      <div class="card flex items-center gap-4">
        <div class="w-10 h-10 rounded-lg bg-yellow-50 flex items-center justify-center">
          <FileText class="w-5 h-5 text-yellow-600" />
        </div>
        <div>
          <div class="text-sm text-gray-500">配送中</div>
          <div class="text-2xl font-semibold text-yellow-700">{{ stats.partial }}</div>
        </div>
      </div>
      <div class="card flex items-center gap-4">
        <div class="w-10 h-10 rounded-lg bg-orange-50 flex items-center justify-center">
          <AlertTriangle class="w-5 h-5 text-orange-600" />
        </div>
        <div>
          <div class="text-sm text-gray-500">临期</div>
          <div class="text-2xl font-semibold text-orange-700">{{ stats.nearExpiry }}</div>
        </div>
      </div>
      <div class="card flex items-center gap-4">
        <div class="w-10 h-10 rounded-lg bg-red-50 flex items-center justify-center">
          <FileText class="w-5 h-5 text-red-600" />
        </div>
        <div>
          <div class="text-sm text-gray-500">已过期</div>
          <div class="text-2xl font-semibold text-red-700">{{ stats.expired }}</div>
        </div>
      </div>
    </div>

    <div class="flex items-center justify-between">
      <div class="relative w-72">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索批次号或疫苗名称"
          class="form-input pl-9"
        />
      </div>
      <button class="btn-primary" @click="openCreateModal">
        <Plus class="w-4 h-4 mr-1" />
        新建批次
      </button>
    </div>

    <DataTable
      :columns="columns"
      :data="filteredBatches"
      @row-click="handleRowClick"
    >
      <template #cell-recommendedOrder="{ row }">
        <div v-if="row.recommendedOrder" class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-blue-100 text-blue-700 text-sm font-semibold">
          {{ row.recommendedOrder }}
        </div>
        <span v-else class="text-gray-400 text-xs">-</span>
      </template>
      <template #cell-expiryDate="{ row }">
        <div class="flex items-center gap-1.5">
          <span>{{ row.expiryDate }}</span>
          <span v-if="isNearExpiry(row)" class="inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded text-xs font-medium bg-orange-100 text-orange-700">
            <Clock class="w-3 h-3" />
            {{ getDaysToExpiry(row.expiryDate) }}天
          </span>
        </div>
      </template>
      <template #cell-status="{ row }">
        <StatusBadge :status="row.status" :status-map="BATCH_STATUS_MAP" />
      </template>
    </DataTable>

    <Modal title="新建批次" :visible="showCreateModal" width="640px" @close="showCreateModal = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="form-label">批次号</label>
            <input v-model="newBatch.batchNo" type="text" class="form-input" required />
          </div>
          <div>
            <label class="form-label">疫苗名称</label>
            <input v-model="newBatch.vaccineName" type="text" class="form-input" required />
          </div>
          <div>
            <label class="form-label">生产企业</label>
            <input v-model="newBatch.manufacturer" type="text" class="form-input" required />
          </div>
          <div>
            <label class="form-label">规格</label>
            <input v-model="newBatch.specification" type="text" class="form-input" required />
          </div>
          <div>
            <label class="form-label">有效期至</label>
            <input v-model="newBatch.expiryDate" type="date" class="form-input" required />
          </div>
          <div>
            <label class="form-label">储运温度</label>
            <select v-model="newBatch.storageTempZone" class="form-input">
              <option v-for="zone in TEMP_ZONE_OPTIONS" :key="zone" :value="zone">{{ zone }}</option>
            </select>
          </div>
          <div>
            <label class="form-label">批签发证明编号</label>
            <input v-model="newBatch.batchReleaseDoc" type="text" class="form-input" required />
          </div>
          <div>
            <label class="form-label">状态</label>
            <select v-model="newBatch.status" class="form-input">
              <option value="available">可配送</option>
              <option value="partial">部分配送</option>
              <option value="exhausted">已配完</option>
              <option value="expired">已过期</option>
            </select>
          </div>
          <div>
            <label class="form-label">总数量</label>
            <input v-model.number="newBatch.totalQty" type="number" min="0" class="form-input" required />
          </div>
          <div>
            <label class="form-label">可配送数量</label>
            <input v-model.number="newBatch.distributableQty" type="number" min="0" class="form-input" required />
          </div>
        </div>
        <div class="flex justify-end gap-3 pt-2">
          <button type="button" class="btn-secondary" @click="showCreateModal = false">取消</button>
          <button type="submit" class="btn-primary" :disabled="loading">确认创建</button>
        </div>
      </form>
    </Modal>

    <Modal title="批次详情" :visible="showDetailModal" width="900px" @close="showDetailModal = false">
      <div v-if="selectedBatch" class="space-y-5 max-h-[70vh] overflow-y-auto">
        <div class="grid grid-cols-2 gap-x-6 gap-y-3">
          <div>
            <div class="text-xs text-gray-500 mb-0.5">批次号</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.batchNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">疫苗名称</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.vaccineName }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">生产企业</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.manufacturer }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">规格</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.specification }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">有效期至</div>
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-gray-800">{{ selectedBatch.expiryDate }}</span>
              <span v-if="isNearExpiry(selectedBatch)" class="inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded text-xs font-medium bg-orange-100 text-orange-700">
                <Clock class="w-3 h-3" />
                临期 {{ getDaysToExpiry(selectedBatch.expiryDate) }}天
              </span>
            </div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">储运温度</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.storageTempZone }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">批签发证明编号</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.batchReleaseDoc }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">状态</div>
            <StatusBadge :status="selectedBatch.status" :status-map="BATCH_STATUS_MAP" />
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">总数量</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.totalQty }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">可配送数量</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.distributableQty }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">推荐配送顺序</div>
            <div class="text-sm font-medium text-gray-800">
              <span v-if="selectedBatch.recommendedOrder" class="inline-flex items-center justify-center w-7 h-7 rounded-full bg-blue-100 text-blue-700 font-semibold">
                {{ selectedBatch.recommendedOrder }}
              </span>
              <span v-else class="text-gray-400">未生成</span>
            </div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-0.5">最后调整人</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.lastAdjustedBy || '-' }}</div>
          </div>
          <div v-if="selectedBatch.affectedClinics" class="col-span-2">
            <div class="text-xs text-gray-500 mb-0.5">影响的门诊预约</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedBatch.affectedClinics }}</div>
          </div>
        </div>

        <div v-if="coldChainBoxes.length > 0" class="border-t pt-4">
          <div class="flex items-center justify-between mb-3">
            <h4 class="text-sm font-semibold text-gray-700 flex items-center gap-2">
              <Package class="w-4 h-4" />
              拆分配送冷链箱
            </h4>
            <div class="flex items-center gap-4 text-xs">
              <span class="text-gray-500">共 <span class="font-medium text-gray-700">{{ getBoxStats(coldChainBoxes).total }}</span> 箱</span>
              <span class="text-yellow-600">在途 <span class="font-medium">{{ getBoxStats(coldChainBoxes).inTransit }}</span></span>
              <span class="text-green-600">已验收 <span class="font-medium">{{ getBoxStats(coldChainBoxes).accepted }}</span></span>
              <span class="text-orange-600">延误 <span class="font-medium">{{ getBoxStats(coldChainBoxes).delayed }}</span></span>
              <span class="text-red-600">拒收 <span class="font-medium">{{ getBoxStats(coldChainBoxes).rejected }}</span></span>
            </div>
          </div>

          <div class="space-y-2">
            <div
              v-for="box in coldChainBoxes"
              :key="box.id"
              class="border rounded-lg overflow-hidden"
            >
              <div
                class="flex items-center gap-3 px-4 py-3 bg-gray-50 cursor-pointer hover:bg-gray-100 transition-colors"
                @click="toggleBoxExpand(box)"
              >
                <component
                  :is="expandedBoxIds.has(box.id) ? ChevronDown : ChevronRight"
                  class="w-4 h-4 text-gray-400 flex-shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-3">
                    <span class="text-sm font-medium text-gray-800">{{ box.boxNo }}</span>
                    <StatusBadge :status="box.status" :status-map="BOX_STATUS_MAP" size="sm" />
                  </div>
                  <div class="flex items-center gap-4 mt-1 text-xs text-gray-500">
                    <span>目标门诊: {{ box.targetClinic }}</span>
                    <span>数量: {{ box.quantity }}</span>
                    <span v-if="box.currentVehicleNo">车辆: {{ box.currentVehicleNo }}</span>
                  </div>
                </div>
                <div class="flex items-center gap-3 text-xs">
                  <div class="flex items-center gap-1 text-gray-500">
                    <Thermometer class="w-3.5 h-3.5" />
                    {{ box.tempProbeNo }}
                  </div>
                  <div class="flex items-center gap-1 text-gray-500">
                    <ShieldAlert class="w-3.5 h-3.5" />
                    {{ box.sealNo }}
                  </div>
                </div>
              </div>

              <div v-if="expandedBoxIds.has(box.id)" class="px-4 py-3 border-t bg-white">
                <div class="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
                  <div>
                    <span class="text-gray-500">配送单号:</span>
                    <span class="ml-2 text-gray-800">{{ box.orderNo }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">当前司机:</span>
                    <span class="ml-2 text-gray-800">{{ box.currentDriverName || '-' }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">司机电话:</span>
                    <span class="ml-2 text-gray-800">{{ box.currentDriverPhone || '-' }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">转运交接点:</span>
                    <span class="ml-2 text-gray-800">{{ box.transferPoint || '-' }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">预计到达:</span>
                    <span class="ml-2 text-gray-800">{{ box.estimatedArrivalTime || '-' }}</span>
                  </div>
                  <div>
                    <span class="text-gray-500">实际到达:</span>
                    <span class="ml-2 text-gray-800">{{ box.actualArrivalTime || '-' }}</span>
                  </div>
                </div>

                <div v-if="box.exceptionRemark" class="mt-3 p-3 bg-orange-50 rounded-lg">
                  <div class="text-xs font-medium text-orange-700 mb-1">异常备注</div>
                  <div class="text-sm text-orange-800">{{ box.exceptionRemark }}</div>
                  <div v-if="box.responsibleParty" class="text-xs text-orange-600 mt-1">
                    责任方: {{ box.responsibleParty }}
                  </div>
                </div>

                <div v-if="boxSegmentsMap.get(box.id)?.length" class="mt-3">
                  <div class="text-xs font-semibold text-gray-600 mb-2 flex items-center gap-1">
                    <Truck class="w-3.5 h-3.5" />
                    转运记录
                  </div>
                  <div class="space-y-2">
                    <div
                      v-for="(segment, idx) in boxSegmentsMap.get(box.id)"
                      :key="segment.id"
                      class="flex items-start gap-3"
                    >
                      <div class="flex flex-col items-center">
                        <div
                          class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium"
                          :class="{
                            'bg-green-100 text-green-700': segment.status === 'completed',
                            'bg-blue-100 text-blue-700': segment.status === 'in_progress',
                            'bg-orange-100 text-orange-700': segment.status === 'delayed',
                            'bg-gray-100 text-gray-600': segment.status === 'pending',
                          }"
                        >
                          {{ idx + 1 }}
                        </div>
                        <div v-if="idx < (boxSegmentsMap.get(box.id)?.length || 0) - 1" class="w-0.5 h-8 bg-gray-200"></div>
                      </div>
                      <div class="flex-1 pb-3">
                        <div class="flex items-center gap-2">
                          <span class="text-sm font-medium text-gray-800">{{ segment.fromPoint }} → {{ segment.toPoint }}</span>
                          <StatusBadge :status="segment.status" :status-map="SEGMENT_STATUS_MAP" size="sm" />
                        </div>
                        <div class="flex items-center gap-3 mt-1 text-xs text-gray-500">
                          <span>车辆: {{ segment.vehicleNo }}</span>
                          <span>司机: {{ segment.driverName }}</span>
                        </div>
                        <div class="flex items-center gap-3 mt-1 text-xs text-gray-500">
                          <span v-if="segment.departTime">出发: {{ segment.departTime }}</span>
                          <span v-if="segment.actualArrivalTime">到达: {{ segment.actualArrivalTime }}</span>
                        </div>
                        <div v-if="segment.delayReason" class="mt-1 text-xs text-orange-600">
                          延误原因: {{ segment.delayReason }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="boxAcceptanceMap.get(box.id)" class="mt-3 p-3 bg-gray-50 rounded-lg">
                  <div class="text-xs font-semibold text-gray-600 mb-2">验收结果</div>
                  <div class="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                    <div>
                      <span class="text-gray-500">发货数量:</span>
                      <span class="ml-2 text-gray-800">{{ boxAcceptanceMap.get(box.id)?.sentQty }}</span>
                    </div>
                    <div>
                      <span class="text-gray-500">实收数量:</span>
                      <span class="ml-2 text-gray-800">{{ boxAcceptanceMap.get(box.id)?.receivedQty }}</span>
                    </div>
                    <div>
                      <span class="text-gray-500">封签完整:</span>
                      <span class="ml-2" :class="boxAcceptanceMap.get(box.id)?.sealIntact ? 'text-green-600' : 'text-red-600'">
                        {{ boxAcceptanceMap.get(box.id)?.sealIntact ? '是' : '否' }}
                      </span>
                    </div>
                    <div>
                      <span class="text-gray-500">温度正常:</span>
                      <span class="ml-2" :class="boxAcceptanceMap.get(box.id)?.tempCurveOk ? 'text-green-600' : 'text-red-600'">
                        {{ boxAcceptanceMap.get(box.id)?.tempCurveOk ? '是' : '否' }}
                      </span>
                    </div>
                    <div class="col-span-2">
                      <span class="text-gray-500">验收状态:</span>
                      <StatusBadge
                        :status="boxAcceptanceMap.get(box.id)?.status || 'pending'"
                        :status-map="ACCEPTANCE_STATUS_MAP"
                        size="sm"
                        class="ml-2"
                      />
                    </div>
                    <div v-if="boxAcceptanceMap.get(box.id)?.rejectionReason" class="col-span-2">
                      <span class="text-gray-500">拒收原因:</span>
                      <span class="ml-2 text-red-600">{{ boxAcceptanceMap.get(box.id)?.rejectionReason }}</span>
                    </div>
                    <div v-if="boxAcceptanceMap.get(box.id)?.exceptionResponsibility" class="col-span-2">
                      <span class="text-gray-500">异常责任:</span>
                      <span class="ml-2 text-orange-600">{{ boxAcceptanceMap.get(box.id)?.exceptionResponsibility }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="adjustmentRecords.length > 0" class="border-t pt-4">
          <h4 class="text-sm font-semibold text-gray-700 mb-3">人工调整历史</h4>
          <div class="space-y-2 max-h-48 overflow-y-auto">
            <div v-for="record in adjustmentRecords" :key="record.id" class="p-3 bg-gray-50 rounded-lg text-sm">
              <div class="flex items-center justify-between mb-1">
                <span class="text-gray-600">
                  推荐顺序 #{{ record.recommendedOrder }} → 实际 #{{ record.actualOrder }}
                </span>
                <span class="text-xs text-gray-400">{{ record.createdAt }}</span>
              </div>
              <div v-if="record.skipReason" class="text-gray-700">
                <span class="text-gray-500">跳过原因：</span>{{ record.skipReason }}
              </div>
              <div v-if="record.adjustedBy" class="text-gray-500 text-xs mt-1">
                调整人：{{ record.adjustedBy }}
              </div>
              <div v-if="record.affectedClinics" class="text-gray-500 text-xs">
                影响门诊：{{ record.affectedClinics }}
              </div>
            </div>
          </div>
        </div>

        <div class="flex justify-end pt-2">
          <button class="btn-secondary" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </Modal>
  </div>
</template>
