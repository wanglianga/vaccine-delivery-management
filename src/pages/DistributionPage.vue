<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Truck, Clock, CheckCircle, PackagePlus, Sparkles, AlertTriangle, X } from 'lucide-vue-next'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import Modal from '@/components/Modal.vue'
import { useApi } from '@/composables/useApi'
import type { DistributionOrder, VaccineBatch, DistributionRecommendation } from '@/types'
import { ORDER_STATUS_MAP, BATCH_STATUS_MAP } from '@/types'

const {
  getDistributionOrders,
  createDistributionOrder,
  getBatches,
  generateDistributionRecommendations,
  skipNearExpiryBatch,
} = useApi()

const orders = ref<DistributionOrder[]>([])
const batches = ref<VaccineBatch[]>([])
const recommendations = ref<DistributionRecommendation[]>([])
const detailVisible = ref(false)
const createVisible = ref(false)
const skipConfirmVisible = ref(false)
const selectedOrder = ref<DistributionOrder | null>(null)

const form = ref({
  batchId: '',
  batchNo: '',
  vaccineName: '',
  quantity: 0,
  coldChainBoxNo: '',
  tempProbeNo: '',
  sealNo: '',
  vehicleNo: '',
  driverName: '',
  driverPhone: '',
  estimatedArrivalTime: '',
  targetClinic: '',
  distanceKm: 0,
  coldChainBoxCapacity: 100,
  coldChainBoxUsed: 0,
})

const skipForm = ref({
  skippedBatchId: '',
  selectedBatchId: '',
  skipReason: '',
  targetStatus: 'pending_arrangement',
  skippedBy: '调度员',
  affectedClinics: '',
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
  total: orders.value.length,
  pending: orders.value.filter(o => o.status === 'pending').length,
  inTransit: orders.value.filter(o => o.status === 'in_transit').length,
  completed: orders.value.filter(o => o.status === 'accepted').length,
}))

const columns = [
  { key: 'orderNo', label: '配送单号', width: '160px' },
  { key: 'batchNo', label: '批号' },
  { key: 'vaccineName', label: '疫苗名称' },
  { key: 'quantity', label: '数量', align: 'center' as const },
  { key: 'targetClinic', label: '目标接种点' },
  { key: 'vehicleNo', label: '车辆编号' },
  { key: 'driverName', label: '司机' },
  { key: 'status', label: '状态', slot: true },
]

const availableBatches = computed(() => {
  return batches.value.filter(b =>
    b.status === 'available' || b.status === 'partial'
  )
})

function handleRowClick(row: DistributionOrder) {
  selectedOrder.value = row
  detailVisible.value = true
}

function openCreate() {
  form.value = {
    batchId: '',
    batchNo: '',
    vaccineName: '',
    quantity: 0,
    coldChainBoxNo: '',
    tempProbeNo: '',
    sealNo: '',
    vehicleNo: '',
    driverName: '',
    driverPhone: '',
    estimatedArrivalTime: '',
    targetClinic: '',
    distanceKm: 15,
    coldChainBoxCapacity: 100,
    coldChainBoxUsed: 0,
  }
  recommendations.value = []
  createVisible.value = true
}

async function generateRecommendations() {
  if (!form.value.targetClinic) {
    alert('请先填写目标接种点')
    return
  }
  recommendations.value = await generateDistributionRecommendations({
    targetClinic: form.value.targetClinic,
    distanceKm: form.value.distanceKm,
    coldChainBoxCapacity: form.value.coldChainBoxCapacity,
    coldChainBoxUsed: form.value.coldChainBoxUsed,
    operator: '调度员',
  })
  batches.value = await getBatches()
}

function selectRecommendedBatch(rec: DistributionRecommendation) {
  form.value.batchId = rec.batchId
  form.value.batchNo = rec.batchNo
  form.value.vaccineName = rec.vaccineName
  form.value.quantity = Math.min(50, batches.value.find(b => b.id === rec.batchId)?.distributableQty || 0)
}

function onBatchSelect(batchId: string) {
  const selectedBatch = batches.value.find(b => b.id === batchId)
  if (!selectedBatch) return

  const nearExpiryBatches = availableBatches.value.filter(b => isNearExpiry(b))
  const hasEarlierExpiry = nearExpiryBatches.some(b =>
    b.id !== batchId && new Date(b.expiryDate) < new Date(selectedBatch.expiryDate)
  )

  if (hasEarlierExpiry && !isNearExpiry(selectedBatch)) {
    const earliestBatch = nearExpiryBatches.reduce((a, b) =>
      new Date(a.expiryDate) < new Date(b.expiryDate) ? a : b
    )
    skipForm.value.skippedBatchId = earliestBatch.id
    skipForm.value.selectedBatchId = batchId
    skipForm.value.skipReason = ''
    skipForm.value.targetStatus = 'pending_arrangement'
    skipForm.value.affectedClinics = form.value.targetClinic
    skipConfirmVisible.value = true
    return
  }

  form.value.batchId = selectedBatch.id
  form.value.batchNo = selectedBatch.batchNo
  form.value.vaccineName = selectedBatch.vaccineName
  form.value.quantity = 0
}

async function confirmSkipBatch() {
  if (!skipForm.value.skipReason.trim()) {
    alert('请填写跳过临期批次的原因')
    return
  }
  try {
    await skipNearExpiryBatch(skipForm.value)
    const selectedBatch = batches.value.find(b => b.id === skipForm.value.selectedBatchId)
    if (selectedBatch) {
      form.value.batchId = selectedBatch.id
      form.value.batchNo = selectedBatch.batchNo
      form.value.vaccineName = selectedBatch.vaccineName
      form.value.quantity = 0
    }
    skipConfirmVisible.value = false
    batches.value = await getBatches()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

function cancelSkipBatch() {
  skipConfirmVisible.value = false
  form.value.batchId = ''
  form.value.batchNo = ''
  form.value.vaccineName = ''
}

async function handleSubmit() {
  if (!form.value.batchId) {
    alert('请选择批次')
    return
  }
  await createDistributionOrder({
    batchId: form.value.batchId,
    batchNo: form.value.batchNo,
    vaccineName: form.value.vaccineName,
    quantity: form.value.quantity,
    coldChainBoxNo: form.value.coldChainBoxNo,
    tempProbeNo: form.value.tempProbeNo,
    sealNo: form.value.sealNo,
    vehicleNo: form.value.vehicleNo,
    driverName: form.value.driverName,
    driverPhone: form.value.driverPhone,
    estimatedArrivalTime: form.value.estimatedArrivalTime,
    targetClinic: form.value.targetClinic,
  })
  createVisible.value = false
  orders.value = await getDistributionOrders()
}

onMounted(async () => {
  const [o, b] = await Promise.all([getDistributionOrders(), getBatches()])
  orders.value = o
  batches.value = b
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold text-gray-800">配送管理</h1>
      <button class="btn-primary" @click="openCreate">
        <PackagePlus class="w-4 h-4 mr-1.5" />
        新建配送单
      </button>
    </div>

    <div class="grid grid-cols-4 gap-4">
      <div class="card flex items-center gap-3">
        <div class="p-2.5 bg-blue-50 rounded-lg">
          <Truck class="w-5 h-5 text-blue-600" />
        </div>
        <div>
          <div class="text-xs text-gray-500">总配送单</div>
          <div class="text-lg font-bold text-gray-800">{{ stats.total }}</div>
        </div>
      </div>
      <div class="card flex items-center gap-3">
        <div class="p-2.5 bg-gray-50 rounded-lg">
          <Clock class="w-5 h-5 text-gray-600" />
        </div>
        <div>
          <div class="text-xs text-gray-500">待出库</div>
          <div class="text-lg font-bold text-gray-800">{{ stats.pending }}</div>
        </div>
      </div>
      <div class="card flex items-center gap-3">
        <div class="p-2.5 bg-yellow-50 rounded-lg">
          <Truck class="w-5 h-5 text-yellow-600" />
        </div>
        <div>
          <div class="text-xs text-gray-500">在途</div>
          <div class="text-lg font-bold text-gray-800">{{ stats.inTransit }}</div>
        </div>
      </div>
      <div class="card flex items-center gap-3">
        <div class="p-2.5 bg-green-50 rounded-lg">
          <CheckCircle class="w-5 h-5 text-green-600" />
        </div>
        <div>
          <div class="text-xs text-gray-500">已完成</div>
          <div class="text-lg font-bold text-gray-800">{{ stats.completed }}</div>
        </div>
      </div>
    </div>

    <DataTable :columns="columns" :data="orders" @row-click="handleRowClick">
      <template #cell-status="{ row }">
        <StatusBadge :status="row.status" :status-map="ORDER_STATUS_MAP" />
      </template>
    </DataTable>

    <Modal title="配送单详情" :visible="detailVisible" width="640px" @close="detailVisible = false">
      <div v-if="selectedOrder" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <div class="text-xs text-gray-500 mb-1">配送单号</div>
            <div class="text-sm font-medium text-gray-800">{{ selectedOrder.orderNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">状态</div>
            <StatusBadge :status="selectedOrder.status" :status-map="ORDER_STATUS_MAP" />
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">批号</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.batchNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">疫苗名称</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.vaccineName }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">数量</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.quantity }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">目标接种点</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.targetClinic }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">冷链箱号</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.coldChainBoxNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">温度探头号</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.tempProbeNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">封签号</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.sealNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">车辆编号</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.vehicleNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">司机</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.driverName }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">司机电话</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.driverPhone }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500 mb-1">预计到达时间</div>
            <div class="text-sm text-gray-800">{{ selectedOrder.estimatedArrivalTime }}</div>
          </div>
        </div>
      </div>
    </Modal>

    <Modal title="新建配送单" :visible="createVisible" width="800px" @close="createVisible = false">
      <form class="space-y-5" @submit.prevent="handleSubmit">
        <div class="p-4 bg-blue-50 rounded-lg border border-blue-100">
          <div class="grid grid-cols-3 gap-4">
            <div>
              <label class="form-label">目标接种点 *</label>
              <input type="text" class="form-input" v-model="form.targetClinic" placeholder="请输入接种点名称" />
            </div>
            <div>
              <label class="form-label">配送距离(公里)</label>
              <input type="number" class="form-input" v-model.number="form.distanceKm" min="0" />
            </div>
            <div class="flex items-end">
              <button type="button" class="btn-primary w-full" @click="generateRecommendations">
                <Sparkles class="w-4 h-4 mr-1.5" />
                生成推荐顺序
              </button>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4 mt-3">
            <div>
              <label class="form-label">冷链箱总容量</label>
              <input type="number" class="form-input" v-model.number="form.coldChainBoxCapacity" min="0" />
            </div>
            <div>
              <label class="form-label">冷链箱已用容量</label>
              <input type="number" class="form-input" v-model.number="form.coldChainBoxUsed" min="0" />
            </div>
          </div>
        </div>

        <div v-if="recommendations.length > 0" class="border rounded-lg overflow-hidden">
          <div class="px-4 py-3 bg-gray-50 border-b">
            <h3 class="text-sm font-semibold text-gray-700">推荐配送顺序（临期优先）</h3>
          </div>
          <div class="divide-y max-h-48 overflow-y-auto">
            <div
              v-for="rec in recommendations"
              :key="rec.id"
              class="flex items-center gap-4 px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors"
              :class="{ 'bg-blue-50': form.batchId === rec.batchId }"
              @click="selectRecommendedBatch(rec)"
            >
              <div class="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-sm">
                {{ rec.recommendedOrder }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-medium text-gray-800">{{ rec.batchNo }}</span>
                  <span class="text-gray-500">{{ rec.vaccineName }}</span>
                  <span v-if="rec.daysToExpiry <= 90 && rec.daysToExpiry > 0" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-orange-100 text-orange-700">
                    临期 {{ rec.daysToExpiry }}天
                  </span>
                </div>
                <div class="flex items-center gap-4 mt-1 text-xs text-gray-500">
                  <span>综合评分: {{ (rec.totalScore * 100).toFixed(1) }}</span>
                  <span>有效期评分: {{ (rec.expiryScore * 100).toFixed(0) }}</span>
                  <span>预约量: {{ rec.appointmentCount }}人</span>
                </div>
              </div>
              <div class="text-right text-sm text-gray-600">
                可配: {{ batches.find(b => b.id === rec.batchId)?.distributableQty || 0 }}
              </div>
            </div>
          </div>
        </div>

        <div>
          <label class="form-label">选择批次 *</label>
          <select class="form-input" v-model="form.batchId" @change="onBatchSelect(form.batchId)">
            <option value="" disabled>请选择批次</option>
            <option v-for="b in availableBatches" :key="b.id" :value="b.id">
              {{ b.batchNo }} - {{ b.vaccineName }} (可配: {{ b.distributableQty }})
              <span v-if="isNearExpiry(b)"> [临期 {{ getDaysToExpiry(b.expiryDate) }}天]</span>
            </option>
          </select>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="form-label">配送数量 *</label>
            <input type="number" class="form-input" v-model.number="form.quantity" min="1" />
          </div>
          <div>
            <label class="form-label">冷链箱号 *</label>
            <input type="text" class="form-input" v-model="form.coldChainBoxNo" />
          </div>
          <div>
            <label class="form-label">温度探头号 *</label>
            <input type="text" class="form-input" v-model="form.tempProbeNo" />
          </div>
          <div>
            <label class="form-label">封签号 *</label>
            <input type="text" class="form-input" v-model="form.sealNo" />
          </div>
          <div>
            <label class="form-label">车辆编号 *</label>
            <input type="text" class="form-input" v-model="form.vehicleNo" />
          </div>
          <div>
            <label class="form-label">司机姓名 *</label>
            <input type="text" class="form-input" v-model="form.driverName" />
          </div>
          <div>
            <label class="form-label">司机电话 *</label>
            <input type="text" class="form-input" v-model="form.driverPhone" />
          </div>
          <div>
            <label class="form-label">预计到达时间</label>
            <input type="datetime-local" class="form-input" v-model="form.estimatedArrivalTime" />
          </div>
        </div>

        <div class="flex justify-end gap-3 pt-2">
          <button type="button" class="btn-secondary" @click="createVisible = false">取消</button>
          <button type="submit" class="btn-primary">提交</button>
        </div>
      </form>
    </Modal>

    <Modal title="确认跳过临期批次" :visible="skipConfirmVisible" width="560px" @close="cancelSkipBatch">
      <div class="space-y-4">
        <div class="flex items-start gap-3 p-4 bg-orange-50 rounded-lg border border-orange-200">
          <AlertTriangle class="w-5 h-5 text-orange-600 flex-shrink-0 mt-0.5" />
          <div>
            <h4 class="font-medium text-orange-800">检测到临期批次未优先分配</h4>
            <p class="text-sm text-orange-700 mt-1">
              您选择了远期批次，但存在更早到期的临期批次。跳过临期批次需要填写原因并触发疾控仓库复核。
            </p>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4 text-sm">
          <div class="p-3 bg-gray-50 rounded-lg">
            <div class="text-xs text-gray-500 mb-1">被跳过的临期批次</div>
            <div class="font-medium text-gray-800">
              {{ batches.find(b => b.id === skipForm.skippedBatchId)?.batchNo }}
            </div>
            <div class="text-orange-600 text-xs mt-1">
              临期 {{ getDaysToExpiry(batches.find(b => b.id === skipForm.skippedBatchId)?.expiryDate || '') }}天
            </div>
          </div>
          <div class="p-3 bg-gray-50 rounded-lg">
            <div class="text-xs text-gray-500 mb-1">您选择的批次</div>
            <div class="font-medium text-gray-800">
              {{ batches.find(b => b.id === skipForm.selectedBatchId)?.batchNo }}
            </div>
          </div>
        </div>

        <div>
          <label class="form-label">跳过原因 *</label>
          <textarea
            class="form-input min-h-[80px]"
            v-model="skipForm.skipReason"
            placeholder="请详细说明跳过临期批次的原因..."
          ></textarea>
        </div>

        <div>
          <label class="form-label">被跳过批次处理方式 *</label>
          <select class="form-input" v-model="skipForm.targetStatus">
            <option value="pending_arrangement">待安排</option>
            <option value="pending_report_loss">待报损</option>
            <option value="pending_transfer">待转配</option>
          </select>
        </div>

        <div>
          <label class="form-label">影响的门诊预约</label>
          <input
            type="text"
            class="form-input"
            v-model="skipForm.affectedClinics"
            placeholder="例如：朝阳区社区卫生服务中心、海淀医院"
          />
        </div>

        <div class="flex justify-end gap-3 pt-2">
          <button type="button" class="btn-secondary" @click="cancelSkipBatch">取消</button>
          <button type="button" class="btn-primary" @click="confirmSkipBatch">确认跳过并提交复核</button>
        </div>
      </div>
    </Modal>
  </div>
</template>
