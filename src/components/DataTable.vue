<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

export interface Column {
  key: string
  label: string
  width?: string
  align?: 'left' | 'center' | 'right'
  slot?: boolean
}

const props = withDefaults(defineProps<{
  columns: Column[]
  data: any[]
  pageSize?: number
  selectable?: boolean
}>(), {
  pageSize: 10,
  selectable: false,
})

const emit = defineEmits<{
  (e: 'select', row: any): void
  (e: 'row-click', row: any): void
}>()

const currentPage = ref(1)
const selectedId = ref<string | null>(null)

const totalPages = computed(() => Math.ceil(props.data.length / props.pageSize) || 1)

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * props.pageSize
  return props.data.slice(start, start + props.pageSize)
})

watch(() => props.data, () => {
  currentPage.value = 1
})

function handleRowClick(row: any) {
  if (props.selectable) {
    selectedId.value = row.id
    emit('select', row)
  }
  emit('row-click', row)
}
</script>

<template>
  <div class="bg-white rounded-lg border border-gray-200 overflow-hidden">
    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="bg-gray-50 border-b border-gray-200">
            <th
              v-for="col in columns"
              :key="col.key"
              class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              :class="{
                'text-center': col.align === 'center',
                'text-right': col.align === 'right',
              }"
              :style="col.width ? { width: col.width } : {}"
            >
              {{ col.label }}
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr
            v-for="row in paginatedData"
            :key="row.id"
            class="hover:bg-blue-50/50 cursor-pointer transition-colors"
            :class="{ 'bg-blue-50': selectedId === row.id }"
            @click="handleRowClick(row)"
          >
            <td
              v-for="col in columns"
              :key="col.key"
              class="px-4 py-3 text-gray-700"
              :class="{
                'text-center': col.align === 'center',
                'text-right': col.align === 'right',
              }"
            >
              <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
                {{ row[col.key] }}
              </slot>
            </td>
          </tr>
          <tr v-if="paginatedData.length === 0">
            <td :colspan="columns.length" class="px-4 py-12 text-center text-gray-400">
              暂无数据
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      v-if="data.length > pageSize"
      class="flex items-center justify-between px-4 py-3 border-t border-gray-200 bg-gray-50"
    >
      <span class="text-xs text-gray-500">
        共 {{ data.length }} 条，第 {{ currentPage }}/{{ totalPages }} 页
      </span>
      <div class="flex items-center gap-1">
        <button
          class="p-1 rounded hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed"
          :disabled="currentPage <= 1"
          @click="currentPage--"
        >
          <ChevronLeft class="w-4 h-4" />
        </button>
        <button
          class="p-1 rounded hover:bg-gray-200 disabled:opacity-30 disabled:cursor-not-allowed"
          :disabled="currentPage >= totalPages"
          @click="currentPage++"
        >
          <ChevronRight class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>
</template>
