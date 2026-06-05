<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { PackagePlus, Plus, Trash2, Truck, Save, AlertCircle } from 'lucide-vue-next'
import Modal from '@/components/Modal.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { useApi } from '@/composables/useApi'
import type { VaccineBatch, ColdChainBox } from '@/types'
import { BATCH_STATUS_MAP, BOX_STATUS_MAP } from '@/types'
import { useRouter } from 'vue-router'

const router = useRouter()
const { getBatches, splitBatch, getColdChainBoxes, loading } = useApi()

const batches = ref<VaccineBatch[]>([])
const selectedBatchId = ref('')
const showSuccessModal = ref(false)
const createdBoxes = ref<ColdChainBox[]>([])

const boxes = ref<Array<{
  quantity: number
  coldChainBoxNo: string
  tempProbeNo: string
  sealNo: string
  vehicleNo: string
  driverName: string
  driverPhone: string
  targetClinic: string
  estimatedArrivalTime: string
  transferPoint: string
}>>([
  {
    quantity: 50,
    coldChainBoxNo: '',
    tempProbeNo: '',
    sealNo: '',
    vehicleNo: '',
    driverName: '',
    driverPhone: '',
    targetClinic: '',
    estimatedArrivalTime: '',
    transferPoint: '',
  },
])

const availableBatches = computed(() => {
  return batches.value.filter(b =>
    b.status === 'available' || b.status === 'partial'
  )
})

const selectedBatch = computed(() => {
  return batches.value.find(b => b.id === selectedBatchId.value)
})

const totalQuantity = computed(() => {
  return boxes.value.reduce((sum, box) => sum + (box.quantity || 0), 0)
})

const canSubmit = computed(() => {
  if (!selectedBatchId.value) return false
  if (boxes.value.length === 0) return false
  if (totalQuantity.value <= 0) return false
  if (selectedBatch.value && totalQuantity.value > selectedBatch.value.distributableQty) return false
  return boxes.value.every(box =>
    box.quantity > 0 &&
    box.coldChainBoxNo.trim() &&
    box.tempProbeNo.trim() &&
    box.sealNo.trim() &&
    box.vehicleNo.trim() &&
    box.driverName.trim() &&
    box.driverPhone.trim() &&
    box.targetClinic.trim()
  )
})

function addBox() {
  boxes.value.push({
    quantity: 50,
    coldChainBoxNo: '',
    tempProbeNo: '',
    sealNo: '',
    vehicleNo: '',
    driverName: '',
    driverPhone: '',
    targetClinic: '',
    estimatedArrivalTime: '',
    transferPoint: '',
  })
}

function removeBox(index: number) {
  if (boxes.value.length > 1) {
    boxes.value.splice(index, 1)
  }
}

async function handleSubmit() {
  if (!canSubmit.value) return

  try {
    const result = await splitBatch({
      batchId: selectedBatchId.value,
      boxes: boxes.value.map(box => ({
        quantity: box.quantity,
        coldChainBoxNo: box.coldChainBoxNo,
        tempProbeNo: box.tempProbeNo,
        sealNo: box.sealNo,
        vehicleNo: box.vehicleNo,
        driverName: box.driverName,
        driverPhone: box.driverPhone,
        targetClinic: box.targetClinic,
        estimatedArrivalTime: box.estimatedArrivalTime || undefined,
        transferPoint: box.transferPoint || undefined,
      })),
    })

    createdBoxes.value = result
    showSuccessModal.value = true

    boxes.value = [
      {
        quantity: 50,
        coldChainBoxNo: '',
        tempProbeNo: '',
        sealNo: '',
        vehicleNo: '',
        driverName: '',
        driverPhone: '',
        targetClinic: '',
        estimatedArrivalTime: '',
        transferPoint: '',
      },
    ]
    selectedBatchId.value = ''
  } catch (e: any) {
    alert('拆分失败: ' + (e.message || '未知错误'))
  }
}

function goToBatches() {
  router.push('/batches')
}

onMounted(async () => {
  batches.value = await getBatches()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold text-gray-800 flex items-center gap-2">
        <PackagePlus class="w-6 h-6" />
        批次拆分配送
      </h1>
    </div>

    <div class="card">
      <div class="mb-6">
        <label class="form-label">选择疫苗批次 *</label>
        <select
          v-model="selectedBatchId"
          class="form-input"
        >
          <option value="" disabled>请选择要拆分的批次</option>
          <option
            v-for="batch in availableBatches"
            :key="batch.id"
            :value="batch.id"
          >
            {{ batch.batchNo }} - {{ batch.vaccineName }} (可配送: {{ batch.distributableQty }})
          </option>
        </select>
      </div>

      <div v-if="selectedBatch" class="mb-6 p-4 bg-blue-50 rounded-lg border border-blue-100">
        <div class="grid grid-cols-4 gap-4 text-sm">
          <div>
            <div class="text-xs text-gray-500">批次号</div>
            <div class="font-medium text-gray-800">{{ selectedBatch.batchNo }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500">疫苗名称</div>
            <div class="font-medium text-gray-800">{{ selectedBatch.vaccineName }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500">可配送数量</div>
            <div class="font-medium text-blue-600">{{ selectedBatch.distributableQty }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500">状态</div>
            <StatusBadge :status="selectedBatch.status" :status-map="BATCH_STATUS_MAP" size="sm" />
          </div>
        </div>
      </div>

      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-gray-700">冷链箱配置</h3>
        <div class="flex items-center gap-4">
          <div class="text-sm">
            <span class="text-gray-500">已分配总数: </span>
            <span
              class="font-medium"
              :class="selectedBatch && totalQuantity > selectedBatch.distributableQty ? 'text-red-600' : 'text-green-600'"
            >
              {{ totalQuantity }}
            </span>
            <span v-if="selectedBatch" class="text-gray-500"> / {{ selectedBatch.distributableQty }}</span>
          </div>
          <button
            type="button"
            class="btn-secondary text-sm py-1.5 px-3"
            @click="addBox"
          >
            <Plus class="w-4 h-4 mr-1" />
            添加箱子
          </button>
        </div>
      </div>

      <div v-if="selectedBatch && totalQuantity > selectedBatch.distributableQty" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2">
        <AlertCircle class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
        <div class="text-sm text-red-700">
          分配数量超过可配送数量，请减少每个箱子的分配数量。
        </div>
      </div>

      <div class="space-y-4">
        <div
          v-for="(box, index) in boxes"
          :key="index"
          class="border rounded-lg p-4 bg-gray-50 relative"
        >
          <div class="flex items-center justify-between mb-3">
            <h4 class="text-sm font-medium text-gray-700 flex items-center gap-2">
              <Truck class="w-4 h-4" />
              冷链箱 #{{ index + 1 }}
            </h4>
            <button
              v-if="boxes.length > 1"
              type="button"
              class="text-gray-400 hover:text-red-500 transition-colors"
              @click="removeBox(index)"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>

          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="form-label text-xs">分配数量 *</label>
              <input
                v-model.number="box.quantity"
                type="number"
                min="1"
                class="form-input text-sm py-2"
              />
            </div>
            <div>
              <label class="form-label text-xs">冷链箱号 *</label>
              <input
                v-model="box.coldChainBoxNo"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: CC-001"
              />
            </div>
            <div>
              <label class="form-label text-xs">目标门诊 *</label>
              <input
                v-model="box.targetClinic"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: 朝阳社区卫生服务中心"
              />
            </div>
            <div>
              <label class="form-label text-xs">温度探头号 *</label>
              <input
                v-model="box.tempProbeNo"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: TP-001"
              />
            </div>
            <div>
              <label class="form-label text-xs">封签号 *</label>
              <input
                v-model="box.sealNo"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: SEAL-001"
              />
            </div>
            <div>
              <label class="form-label text-xs">转运交接点</label>
              <input
                v-model="box.transferPoint"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: 昌平中转站"
              />
            </div>
            <div>
              <label class="form-label text-xs">车辆编号 *</label>
              <input
                v-model="box.vehicleNo"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: 京A12345"
              />
            </div>
            <div>
              <label class="form-label text-xs">司机姓名 *</label>
              <input
                v-model="box.driverName"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: 张三"
              />
            </div>
            <div>
              <label class="form-label text-xs">司机电话 *</label>
              <input
                v-model="box.driverPhone"
                type="text"
                class="form-input text-sm py-2"
                placeholder="例如: 13800138000"
              />
            </div>
            <div class="col-span-3">
              <label class="form-label text-xs">预计到达时间</label>
              <input
                v-model="box.estimatedArrivalTime"
                type="datetime-local"
                class="form-input text-sm py-2"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="mt-6 flex justify-end gap-3">
        <button
          type="button"
          class="btn-secondary"
          @click="goToBatches"
        >
          返回批次列表
        </button>
        <button
          type="button"
          class="btn-primary flex items-center gap-2"
          :disabled="!canSubmit || loading"
          @click="handleSubmit"
        >
          <Save class="w-4 h-4" />
          确认拆分并出库
        </button>
      </div>
    </div>

    <Modal
      title="拆分成功"
      :visible="showSuccessModal"
      width="600px"
      @close="showSuccessModal = false"
    >
      <div class="space-y-4">
        <div class="p-4 bg-green-50 border border-green-200 rounded-lg">
          <div class="text-green-700 font-medium">批次拆分配送成功！</div>
          <div class="text-sm text-green-600 mt-1">
            共拆分 {{ createdBoxes.length }} 个冷链箱，已自动出库。
          </div>
        </div>

        <div class="border rounded-lg overflow-hidden">
          <div class="px-4 py-2 bg-gray-50 border-b text-sm font-medium text-gray-700">
            已创建的冷链箱
          </div>
          <div class="divide-y max-h-64 overflow-y-auto">
            <div
              v-for="box in createdBoxes"
              :key="box.id"
              class="px-4 py-3 flex items-center justify-between"
            >
              <div>
                <div class="text-sm font-medium text-gray-800">{{ box.boxNo }}</div>
                <div class="text-xs text-gray-500 mt-0.5">
                  {{ box.targetClinic }} · {{ box.quantity }}支 · 车辆: {{ box.currentVehicleNo }}
                </div>
              </div>
              <StatusBadge :status="box.status" :status-map="BOX_STATUS_MAP" size="sm" />
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 pt-2">
          <button
            class="btn-secondary"
            @click="showSuccessModal = false"
          >
            继续拆分
          </button>
          <button
            class="btn-primary"
            @click="goToBatches"
          >
            查看批次详情
          </button>
        </div>
      </div>
    </Modal>
  </div>
</template>
