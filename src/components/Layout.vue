<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Package,
  Truck,
  MapPin,
  ClipboardCheck,
  ThermometerSun,
  ChevronLeft,
  Menu,
  GitBranch,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const navItems = [
  { path: '/batches', label: '批次管理', icon: Package, role: 'CDC' },
  { path: '/split', label: '拆分配送', icon: GitBranch, role: '仓库' },
  { path: '/distribution', label: '配送管理', icon: Truck, role: '仓库' },
  { path: '/transit', label: '在途监控', icon: MapPin, role: '车队' },
  { path: '/acceptance', label: '验收管理', icon: ClipboardCheck, role: '门诊' },
]

const currentRole = computed(() => {
  const item = navItems.find(n => n.path === route.path)
  return item?.role || '系统'
})
</script>

<template>
  <div class="flex h-screen bg-gray-50">
    <aside
      class="flex flex-col bg-slate-900 text-white transition-all duration-300"
      :class="collapsed ? 'w-16' : 'w-60'"
    >
      <div class="flex items-center justify-between px-4 h-16 border-b border-slate-700">
        <div v-if="!collapsed" class="flex items-center gap-2">
          <ThermometerSun class="w-6 h-6 text-blue-400" />
          <span class="font-bold text-sm whitespace-nowrap">疫苗配送管理</span>
        </div>
        <button
          class="p-1.5 rounded hover:bg-slate-700 transition-colors"
          @click="collapsed = !collapsed"
        >
          <ChevronLeft v-if="!collapsed" class="w-4 h-4" />
          <Menu v-else class="w-4 h-4" />
        </button>
      </div>

      <nav class="flex-1 py-4">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-4 py-3 mx-2 rounded-lg transition-colors"
          :class="
            route.path === item.path
              ? 'bg-blue-600 text-white'
              : 'text-slate-300 hover:bg-slate-800 hover:text-white'
          "
        >
          <component :is="item.icon" class="w-5 h-5 shrink-0" />
          <span v-if="!collapsed" class="text-sm">{{ item.label }}</span>
        </router-link>
      </nav>

      <div v-if="!collapsed" class="px-4 py-3 border-t border-slate-700">
        <div class="text-xs text-slate-400">当前角色</div>
        <div class="text-sm font-medium text-blue-400">{{ currentRole }}</div>
      </div>
    </aside>

    <main class="flex-1 flex flex-col overflow-hidden">
      <header class="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-6 shrink-0">
        <h1 class="text-lg font-semibold text-gray-800">
          {{ navItems.find(n => n.path === route.path)?.label || '疫苗配送管理系统' }}
        </h1>
        <div class="flex items-center gap-3">
          <span class="text-xs px-2 py-1 rounded-full bg-blue-50 text-blue-700 font-medium">
            {{ currentRole }}
          </span>
        </div>
      </header>
      <div class="flex-1 overflow-auto p-6">
        <router-view />
      </div>
    </main>
  </div>
</template>
