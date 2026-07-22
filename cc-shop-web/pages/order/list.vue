<template>
  <div class="max-w-5xl mx-auto px-5">
    <h2 class="text-xl font-bold text-default mb-6">我的订单</h2>

    <!-- 搜索 -->
    <div class="flex gap-2 mb-4">
      <UInput
        v-model="searchKeyword"
        placeholder="搜索订单号..."
        icon="i-lucide-search"
        class="flex-1 max-w-xs"
        @keyup.enter="doSearch"
      />
      <UButton variant="outline" size="sm" @click="doSearch">搜索</UButton>
      <UButton v-if="searchKeyword" variant="ghost" size="sm" color="gray" @click="clearSearch">清空</UButton>
    </div>

    <!-- 状态 Tab -->
    <div class="flex gap-1 mb-6 bg-elevated rounded-lg p-1 shadow-sm">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="[
          'flex-1 py-2 px-3 rounded-md text-sm font-medium transition-colors relative',
          activeTab === tab.value
            ? 'bg-primary text-inverted shadow-sm'
            : 'text-muted hover:text-default hover:bg-muted'
        ]"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
        <span
          v-if="tab.count > 0"
          :class="[
            'ml-1 text-xs px-1.5 py-0.5 rounded-full',
            activeTab === tab.value ? 'bg-white/20' : 'bg-error/10 text-error'
          ]"
        >
          {{ tab.count }}
        </span>
      </button>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="flex flex-col gap-4">
      <SkeletonOrder v-for="i in 3" :key="i" />
    </div>

    <div v-else-if="!filteredOrders.length" class="flex flex-col items-center justify-center py-16">
      <span class="text-5xl mb-4">📦</span>
      <p class="text-dimmed mb-4">{{ activeTab === -1 ? '暂无订单' : '该状态下暂无订单' }}</p>
      <UButton to="/product/list">去逛逛</UButton>
    </div>

    <div v-else class="flex flex-col gap-4">
      <div v-for="order in filteredOrders" :key="order.id" class="bg-elevated rounded-lg shadow-sm overflow-hidden">
        <!-- 订单头 -->
        <div class="flex justify-between items-center p-4 border-b border-muted">
          <div class="flex gap-4">
            <span class="text-sm text-muted">订单号：{{ order.orderNo }}</span>
            <span class="text-sm text-dimmed">{{ formatDate(order.createdAt) }}</span>
          </div>
          <span :class="statusClass(order.status)">{{ statusText(order.status) }}</span>
        </div>

        <!-- 商品列表 -->
        <div class="divide-y divide-muted">
          <div v-for="item in order.items" :key="item.id" class="flex items-center gap-4 p-4">
            <img :src="item.productImage || 'https://picsum.photos/seed/placeholder/80/80'" class="w-16 h-16 rounded bg-muted object-cover shrink-0" />
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-default truncate">{{ item.productName }}</p>
            </div>
            <span class="text-sm text-muted">x{{ item.quantity }}</span>
            <span class="text-sm font-medium text-default">¥{{ item.price }}</span>
          </div>
        </div>

        <!-- 订单尾 -->
        <div class="flex justify-end items-center gap-3 p-4 border-t border-muted">
          <span class="text-sm text-muted">
            合计：<strong class="text-error">¥{{ order.totalAmount }}</strong>
          </span>
          <UButton
            v-if="order.status === 0"
            size="sm"
            color="warning"
            @click="goPay(order.id)"
          >
            立即支付
          </UButton>
          <UButton
            v-if="order.status === 3"
            size="sm"
            color="warning"
            variant="outline"
            @click="navigateTo(`/order/review?orderId=${order.id}`)"
          >
            去评价
          </UButton>
          <UButton size="sm" variant="outline" @click="viewDetail(order.id)">查看详情</UButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const orders = ref<any[]>([])
const loading = ref(true)
const activeTab = ref(-1) // -1 = 全部
const searchKeyword = ref('')

const statusMap: Record<number, string> = {
  0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '已退款'
}

const tabs = computed(() => [
  { label: '全部', value: -1, count: orders.value.length },
  { label: '待支付', value: 0, count: orders.value.filter(o => o.status === 0).length },
  { label: '待发货', value: 1, count: orders.value.filter(o => o.status === 1).length },
  { label: '待收货', value: 2, count: orders.value.filter(o => o.status === 2).length },
  { label: '已完成', value: 3, count: orders.value.filter(o => o.status === 3).length },
])

const filteredOrders = computed(() => {
  if (activeTab.value === -1) return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

onMounted(async () => {
  await loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const params: any = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await api.get<any>('/api/trade/order/list', params)
    if (res.code === 200) {
      orders.value = res.data || []
    }
  } catch {}
  loading.value = false
}

function doSearch() {
  loadOrders()
}

function clearSearch() {
  searchKeyword.value = ''
  loadOrders()
}

function viewDetail(id: number) {
  navigateTo(`/order/${id}`)
}

function goPay(id: number) {
  navigateTo(`/order/pay?orderId=${id}`)
}

function statusText(s: number) {
  return statusMap[s] || '未知'
}

function statusClass(s: number) {
  const base = 'text-xs font-medium px-2.5 py-0.5 rounded-full'
  const colors: Record<number, string> = {
    0: 'bg-warning/10 text-warning',
    1: 'bg-info/10 text-info',
    2: 'bg-primary/10 text-primary',
    3: 'bg-success/10 text-success',
    4: 'bg-muted text-muted',
    5: 'bg-error/10 text-error',
  }
  return `${base} ${colors[s] || 'bg-muted text-muted'}`
}

function formatDate(d: string) {
  return d?.substring(0, 10) || ''
}
</script>
