<script setup lang="ts">
import { X } from 'lucide-vue-next'

defineProps<{
  title: string
  visible: boolean
  width?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <div class="absolute inset-0 bg-black/40" @click="emit('close')" />
      <div
        class="relative bg-white rounded-xl shadow-2xl max-h-[85vh] overflow-auto"
        :style="{ width: width || '560px' }"
      >
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <h3 class="text-base font-semibold text-gray-800">{{ title }}</h3>
          <button
            class="p-1 rounded hover:bg-gray-100 transition-colors"
            @click="emit('close')"
          >
            <X class="w-4 h-4 text-gray-500" />
          </button>
        </div>
        <div class="px-6 py-4">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>
