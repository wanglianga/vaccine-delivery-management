<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Truck, Clock, CheckCircle, PackagePlus } from 'lucide-vue-next'
import DataTable from '@/components/DataTable.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import Modal from '@/components/Modal.vue'
import { useApi } from '@/composables/useApi'
import type { DistributionOrder, VaccineBatch } from '@/types'
import { ORDER_STATUS_MAP } from '@/types'

const { getDistributionOrders, createDistributionOrder, getBatches } = useApi()

const orders = ref<DistributionOrder[]>([])
const batches = ref<VaccineBatch[]>([])
const detailVisible = ref(false)
const createVisible = ref(false)
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
})

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
  }
  createVisible.value = true
}

function onBatchSelect(batchId: string) {
  const batch = batches.value.find(b => b.id === batchId)
  if (batch) {
    form.value.batchId = batch.id
    form.value.batchNo = batch.batchNo
    form.value.vaccineName = batch.vaccineName
    form.value.quantity = 0
  }
}

async function handleSubmit() {
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

    <Modal title="新建配送单" :visible="createVisible" width="640px" @close="createVisible = false">
      <form class="space-y-4" @submit.prevent="handleSubmit">
        <div>
          <label class="form-label">选择批次</label>
          <select class="form-input" v-model="form.batchId" @change="onBatchSelect(form.batchId)">
            <option value="" disabled>请选择批次</option>
            <option v-for="b in batches" :key="b.id" :value="b.id">
              {{ b.batchNo }} - {{ b.vaccineName }} (可配: {{ b.distributableQty }})
            </option>
          </select>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="form-label">配送数量</label>
            <input type="number" class="form-input" v-model.number="form.quantity" min="1" />
          </div>
          <div>
            <label class="form-label">目标接种点</label>
            <input type="text" class="form-input" v-model="form.targetClinic" />
          </div>
          <div>
            <label class="form-label">冷链箱号</label>
            <input type="text" class="form-input" v-model="form.coldChainBoxNo" />
          </div>
          <div>
            <label class="form-label">温度探头号</label>
            <input type="text" class="form-input" v-model="form.tempProbeNo" />
          </div>
          <div>
            <label class="form-label">封签号</label>
            <input type="text" class="form-input" v-model="form.sealNo" />
          </div>
          <div>
            <label class="form-label">车辆编号</label>
            <input type="text" class="form-input" v-model="form.vehicleNo" />
          </div>
          <div>
            <label class="form-label">司机姓名</label>
            <input type="text" class="form-input" v-model="form.driverName" />
          </div>
          <div>
            <label class="form-label">司机电话</label>
            <input type="text" class="form-input" v-model="form.driverPhone" />
          </div>
          <div class="col-span-2">
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
  </div>
</template>
