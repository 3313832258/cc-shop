<template>
  <div class="max-w-4xl mx-auto px-5">
    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else-if="order">
      <div class="flex items-center gap-4 mb-6">
        <UButton variant="ghost" icon="i-lucide-arrow-left" @click="navigateTo('/order/list')" />
        <h2 class="text-xl font-bold text-default">订单详情</h2>
      </div>

      <!-- 订单状态 -->
      <div class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <span :class="statusClass(order.status)">{{ statusText(order.status) }}</span>
      </div>

      <!-- 收货信息 -->
      <div class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <h3 class="text-sm font-semibold text-default mb-2">收货信息</h3>
        <p class="text-sm text-muted">
          {{ order.receiverName }} {{ order.receiverPhone }}<br />
          {{ order.receiverProvince }}{{ order.receiverCity }}{{ order.receiverDistrict }}{{ order.receiverAddress }}
        </p>
      </div>

      <!-- 商品 -->
      <div class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <h3 class="text-sm font-semibold text-default mb-2">商品信息</h3>
        <div class="divide-y divide-muted">
          <div v-for="item in order.items" :key="item.id" class="flex items-center gap-4 py-2">
            <img :src="item.productImage" class="w-14 h-14 rounded bg-muted object-cover" />
            <div class="flex-1 min-w-0">
              <p class="text-sm text-default truncate">{{ item.productName }}</p>
            </div>
            <span class="text-sm text-muted">x{{ item.quantity }}</span>
            <span class="text-sm font-medium text-default">¥{{ item.price }}</span>
          </div>
        </div>
      </div>

      <!-- 金额 -->
      <div class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <div class="flex justify-between text-sm mb-2">
          <span class="text-muted">商品金额</span>
          <span class="text-default">¥{{ order.productAmount }}</span>
        </div>
        <div class="flex justify-between text-sm mb-2">
          <span class="text-muted">优惠</span>
          <span class="text-success">-¥{{ order.couponAmount || '0.00' }}</span>
        </div>
        <div class="flex justify-between text-base font-semibold border-t border-muted pt-2">
          <span class="text-default">实付</span>
          <span class="text-error">¥{{ order.payAmount }}</span>
        </div>
      </div>

      <!-- 操作 -->
      <div class="flex gap-3">
        <UButton v-if="order.status === 0" color="primary" size="lg" @click="navigateTo(`/order/pay?orderNo=${order.orderNo}`)">去支付</UButton>
        <UButton v-if="order.status === 0" variant="outline" size="lg" @click="cancelOrder">取消订单</UButton>
        <UButton v-if="order.status === 3" color="warning" size="lg" @click="navigateTo(`/order/review?orderId=${order.id}`)">去评价</UButton>
      </div>
    </template>

    <div v-else class="flex flex-col items-center justify-center py-16">
      <span class="text-5xl mb-4">😕</span>
      <p class="text-dimmed mb-4">订单不存在</p>
      <UButton to="/order/list" variant="outline">返回订单列表</UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const api = useApi()
const toast = useAppToast()

const orderId = Number(route.params.id)
const loading = ref(true)
const order = ref<any>(null)

onMounted(async () => {
  try {
    const res = await api.get<any>(`/api/trade/order/${orderId}`)
    if (res.code === 200) order.value = res.data
  } catch {}
  loading.value = false
})

function statusText(s: number) {
  const map: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消', 5: '已退款' }
  return map[s] || '未知'
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

async function cancelOrder() {
  try {
    const res = await api.post<any>(`/api/trade/order/cancel/${orderId}`)
    if (res.code === 200) {
      toast.success('订单已取消')
      order.value.status = 4
    }
  } catch {}
}
</script>
