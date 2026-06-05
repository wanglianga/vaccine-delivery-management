<script setup lang="ts">
import { computed } from 'vue'
import type { TemperatureRecord } from '@/types'

const props = defineProps<{
  data: TemperatureRecord[]
  minTemp?: number
  maxTemp?: number
  height?: number
}>()

const chartWidth = 800
const chartHeight = props.height || 200
const padding = { top: 20, right: 20, bottom: 30, left: 50 }

const plotWidth = chartWidth - padding.left - padding.right
const plotHeight = chartHeight - padding.top - padding.bottom

const tempRange = computed(() => {
  if (props.data.length === 0) return { min: 0, max: 10 }
  const temps = props.data.map(d => d.temperature)
  const dataMin = Math.min(...temps)
  const dataMax = Math.max(...temps)
  const min = Math.min(dataMin - 2, props.minTemp ?? dataMin - 2)
  const max = Math.max(dataMax + 2, props.maxTemp ?? dataMax + 2)
  return { min, max }
})

function xPos(i: number): number {
  if (props.data.length <= 1) return padding.left
  return padding.left + (i / (props.data.length - 1)) * plotWidth
}

function yPos(temp: number): number {
  const range = tempRange.value.max - tempRange.value.min
  if (range === 0) return padding.top + plotHeight / 2
  return padding.top + plotHeight - ((temp - tempRange.value.min) / range) * plotHeight
}

const linePath = computed(() => {
  if (props.data.length === 0) return ''
  return props.data
    .map((d, i) => `${i === 0 ? 'M' : 'L'} ${xPos(i)} ${yPos(d.temperature)}`)
    .join(' ')
})

const areaPath = computed(() => {
  if (props.data.length === 0) return ''
  const bottom = padding.top + plotHeight
  const line = props.data
    .map((d, i) => `${i === 0 ? 'M' : 'L'} ${xPos(i)} ${yPos(d.temperature)}`)
    .join(' ')
  const lastX = xPos(props.data.length - 1)
  const firstX = xPos(0)
  return `${line} L ${lastX} ${bottom} L ${firstX} ${bottom} Z`
})

const yTicks = computed(() => {
  const { min, max } = tempRange.value
  const step = Math.ceil((max - min) / 5)
  const ticks = []
  for (let v = Math.floor(min); v <= Math.ceil(max); v += step) {
    ticks.push(v)
  }
  return ticks
})

const xTicks = computed(() => {
  if (props.data.length === 0) return []
  const step = Math.max(1, Math.floor(props.data.length / 6))
  const ticks = []
  for (let i = 0; i < props.data.length; i += step) {
    ticks.push({ index: i, label: props.data[i].time.slice(11, 16) })
  }
  return ticks
})

const minLineY = computed(() => {
  if (props.minTemp == null) return null
  return yPos(props.minTemp)
})

const maxLineY = computed(() => {
  if (props.maxTemp == null) return null
  return yPos(props.maxTemp)
})
</script>

<template>
  <div class="bg-white rounded-lg border border-gray-200 p-4">
    <svg :width="chartWidth" :height="chartHeight" class="w-full" :viewBox="`0 0 ${chartWidth} ${chartHeight}`">
      <g :transform="`translate(0,0)`">
        <line
          v-for="tick in yTicks"
          :key="'y' + tick"
          :x1="padding.left"
          :x2="chartWidth - padding.right"
          :y1="yPos(tick)"
          :y2="yPos(tick)"
          stroke="#f0f0f0"
          stroke-width="1"
        />
        <text
          v-for="tick in yTicks"
          :key="'yt' + tick"
          :x="padding.left - 8"
          :y="yPos(tick) + 4"
          text-anchor="end"
          class="text-xs fill-gray-400"
        >
          {{ tick }}°C
        </text>

        <text
          v-for="tick in xTicks"
          :key="'xt' + tick.index"
          :x="xPos(tick.index)"
          :y="chartHeight - 5"
          text-anchor="middle"
          class="text-xs fill-gray-400"
        >
          {{ tick.label }}
        </text>

        <line
          v-if="minLineY != null"
          :x1="padding.left"
          :x2="chartWidth - padding.right"
          :y1="minLineY"
          :y2="minLineY"
          stroke="#ef4444"
          stroke-width="1"
          stroke-dasharray="4,4"
        />
        <line
          v-if="maxLineY != null"
          :x1="padding.left"
          :x2="chartWidth - padding.right"
          :y1="maxLineY"
          :y2="maxLineY"
          stroke="#ef4444"
          stroke-width="1"
          stroke-dasharray="4,4"
        />

        <path v-if="areaPath" :d="areaPath" fill="rgba(59,130,246,0.08)" />
        <path v-if="linePath" :d="linePath" fill="none" stroke="#3b82f6" stroke-width="2" />

        <circle
          v-for="(d, i) in data"
          :key="'p' + i"
          :cx="xPos(i)"
          :cy="yPos(d.temperature)"
          r="2.5"
          fill="#3b82f6"
        />

        <rect
          :x="padding.left"
          :y="padding.top"
          :width="plotWidth"
          :height="plotHeight"
          fill="none"
          stroke="#e5e7eb"
          stroke-width="1"
        />
      </g>
    </svg>
    <div v-if="minTemp != null || maxTemp != null" class="flex items-center gap-4 mt-2 text-xs text-gray-500">
      <span v-if="minTemp != null" class="flex items-center gap-1">
        <span class="inline-block w-4 h-0 border-t-2 border-dashed border-red-500"></span>
        下限 {{ minTemp }}°C
      </span>
      <span v-if="maxTemp != null" class="flex items-center gap-1">
        <span class="inline-block w-4 h-0 border-t-2 border-dashed border-red-500"></span>
        上限 {{ maxTemp }}°C
      </span>
    </div>
  </div>
</template>
