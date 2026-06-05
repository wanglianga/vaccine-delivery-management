<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
  statusMap: Record<string, { label: string; color: string }>
}>()

const info = computed(() => props.statusMap[props.status] || { label: props.status, color: 'gray' })

const colorClasses: Record<string, string> = {
  green: 'bg-green-50 text-green-700 border-green-200',
  yellow: 'bg-yellow-50 text-yellow-700 border-yellow-200',
  red: 'bg-red-50 text-red-700 border-red-200',
  blue: 'bg-blue-50 text-blue-700 border-blue-200',
  gray: 'bg-gray-50 text-gray-700 border-gray-200',
  orange: 'bg-orange-50 text-orange-700 border-orange-200',
  purple: 'bg-purple-50 text-purple-700 border-purple-200',
}
</script>

<template>
  <span
    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border"
    :class="colorClasses[info.color] || colorClasses.gray"
  >
    {{ info.label }}
  </span>
</template>
