<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, FileText, Search, AlertTriangle, Clock } from 'lucide-vue-next'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import Modal from '@/components/Modal.vue'
import { useApi } from '@/composables/useApi'
import type { VaccineBatch, BatchAdjustmentRecord } from '@/types'
import { BATCH_STATUS_MAP, TEMP_ZONE_OPTIONS } from '@/types'

const { getBatches, createBatch, loading, getBatchAdjustmentRecords } = useApi()

const batches = ref<VaccineBatch[]>([])
const searchQuery = ref('')
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const selectedBatch = ref<VaccineBatch | null>(null)
const adjustmentRecords = ref<BatchAdjustmentRecord[]>([])

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
  showDetailModal.value = true
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

    <Modal title="批次详情" :visible="showDetailModal" width="720px" @close="showDetailModal = false">
      <div v-if="selectedBatch" class="space-y-5">
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
