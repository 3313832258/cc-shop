<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Dashboard</h1>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else>
      <!-- 统计卡片 -->
      <div class="grid grid-cols-4 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">今日订单</p>
              <p class="text-3xl font-bold text-gray-900">{{ stats.todayOrders }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <UIcon name="i-lucide-shopping-cart" class="text-primary" size="24" />
            </div>
          </div>
          <p class="text-sm text-gray-500 mt-2">总计 {{ stats.totalOrders }} 单</p>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">今日销售额</p>
              <p class="text-3xl font-bold text-gray-900">¥{{ formatAmount(stats.todayRevenue) }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <UIcon name="i-lucide-dollar-sign" class="text-green-600" size="24" />
            </div>
          </div>
          <p class="text-sm text-gray-500 mt-2">总计 ¥{{ formatAmount(stats.totalRevenue) }}</p>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">总用户数</p>
              <p class="text-3xl font-bold text-gray-900">{{ stats.totalUsers }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <UIcon name="i-lucide-users" class="text-purple-600" size="24" />
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">总商品数</p>
              <p class="text-3xl font-bold text-gray-900">{{ stats.totalProducts }}</p>
            </div>
            <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
              <UIcon name="i-lucide-package" class="text-orange-600" size="24" />
            </div>
          </div>
        </div>
      </div>

      <!-- 待处理事项 -->
      <div class="grid grid-cols-3 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-lg font-semibold mb-4">待付款</h2>
          <div class="flex items-center gap-4">
            <span class="text-4xl font-bold text-warning">{{ stats.pendingOrders }}</span>
            <span class="text-gray-500">笔</span>
          </div>
          <NuxtLink to="/order?status=0" class="text-primary text-sm mt-4 inline-block">查看详情 →</NuxtLink>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-lg font-semibold mb-4">待发货</h2>
          <div class="flex items-center gap-4">
            <span class="text-4xl font-bold text-blue-500">{{ stats.pendingShipments }}</span>
            <span class="text-gray-500">笔</span>
          </div>
          <NuxtLink to="/order?status=1" class="text-primary text-sm mt-4 inline-block">查看详情 →</NuxtLink>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-lg font-semibold mb-4">待处理售后</h2>
          <div class="flex items-center gap-4">
            <span class="text-4xl font-bold text-error">{{ stats.pendingAftersales }}</span>
            <span class="text-gray-500">笔</span>
          </div>
          <NuxtLink to="/aftersale?status=0" class="text-primary text-sm mt-4 inline-block">查看详情 →</NuxtLink>
        </div>
      </div>

      <!-- 7 天趋势 + 低库存预警 -->
      <div class="grid grid-cols-2 gap-6">
        <!-- 7 天订单趋势 -->
        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-lg font-semibold mb-4">近 7 天订单趋势</h2>
          <div class="flex items-end gap-2 h-40">
            <div
              v-for="(point, i) in stats.trend"
              :key="i"
              class="flex-1 flex flex-col items-center gap-1"
            >
              <span class="text-xs text-gray-500">{{ point.orderCount }}</span>
              <div
                class="w-full bg-primary/80 rounded-t transition-all duration-300"
                :style="{ height: barHeight(point.orderCount) + '%' }"
              />
              <span class="text-xs text-gray-400">{{ point.date.substring(5) }}</span>
            </div>
          </div>
          <p class="text-xs text-gray-400 mt-2 text-center">近 7 天销售额：¥{{ weekRevenue }}</p>
        </div>

        <!-- 低库存预警 -->
        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-lg font-semibold mb-4">
            低库存预警
            <span v-if="stats.lowStockItems?.length" class="text-sm font-normal text-error ml-2">
              ({{ stats.lowStockItems.length }} 项)
            </span>
          </h2>
          <div v-if="stats.lowStockItems?.length" class="overflow-y-auto max-h-44">
            <table class="w-full text-sm">
              <thead class="text-gray-500 border-b">
                <tr>
                  <th class="text-left py-2">商品</th>
                  <th class="text-left py-2">SKU</th>
                  <th class="text-right py-2">库存</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in stats.lowStockItems" :key="item.skuId" class="border-b border-gray-50">
                  <td class="py-2 truncate max-w-[160px]">{{ item.productName }}</td>
                  <td class="py-2 text-gray-500">{{ item.skuCode }}</td>
                  <td class="py-2 text-right">
                    <span :class="item.stock <= 3 ? 'text-error font-bold' : 'text-warning'">
                      {{ item.stock }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="text-sm text-gray-400">暂无低库存商品</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()

const loading = ref(true)
const stats = ref({
  totalOrders: 0,
  todayOrders: 0,
  totalUsers: 0,
  totalProducts: 0,
  totalRevenue: 0,
  todayRevenue: 0,
  pendingOrders: 0,
  pendingShipments: 0,
  pendingAftersales: 0,
  trend: [] as { date: string; orderCount: number; revenue: number }[],
  lowStockItems: [] as { productId: number; skuId: number; productName: string; skuCode: string; stock: number }[],
})

const weekRevenue = computed(() => {
  const sum = (stats.value.trend || []).reduce((s, p) => s + Number(p.revenue || 0), 0)
  return sum.toFixed(2)
})

function formatAmount(amount: number): string {
  return (amount || 0).toFixed(2)
}

function barHeight(count: number): number {
  const max = Math.max(...(stats.value.trend || []).map(p => p.orderCount), 1)
  return Math.max((count / max) * 100, 4)
}

onMounted(async () => {
  try {
    const res = await api.get<any>('/api/admin/dashboard')
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch {}
  loading.value = false
})
</script>
