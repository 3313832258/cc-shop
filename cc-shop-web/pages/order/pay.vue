<template>
  <div class="max-w-7xl mx-auto px-5">
    <h2 class="text-xl font-bold text-default mb-6">支付订单</h2>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else-if="order">
      <div class="max-w-md mx-auto bg-elevated rounded-lg shadow-sm p-8 text-center">
        <div class="mb-6">
          <p class="text-xs text-dimmed mb-2">订单号：{{ order.orderNo }}</p>
          <UBadge
            :color="order.status === 0 ? 'warning' : order.status === 1 ? 'success' : order.status === 4 ? 'error' : 'primary'"
            variant="subtle"
          >
            {{ order.statusDesc }}
          </UBadge>
        </div>

        <div class="py-6 border-y border-muted mb-6">
          <p class="text-sm text-muted mb-2">应付金额</p>
          <p class="text-4xl font-bold text-error">¥{{ order.finalAmount?.toFixed(2) }}</p>
          <p v-if="order.discountAmount > 0" class="text-xs text-dimmed mt-2">
            已优惠 ¥{{ order.discountAmount?.toFixed(2) }}
          </p>
        </div>

        <!-- 待付款时显示支付按钮 -->
        <template v-if="order.status === 0">
          <p class="text-xs text-dimmed mb-4">支付方式：模拟支付</p>
          <UButton
            block
            size="lg"
            color="primary"
            :loading="paying"
            :disabled="paying"
            @click="handlePay"
          >
            {{ paying ? '支付中，请稍候...' : `确认支付 ¥${order.finalAmount?.toFixed(2)}` }}
          </UButton>
        </template>

        <!-- 支付成功 -->
        <div v-else-if="order.status === 1" class="py-6">
          <span class="text-5xl block mb-3">✅</span>
          <p class="text-lg font-semibold text-default mb-1">支付成功</p>
          <p class="text-sm text-muted">订单已提交，等待发货</p>
          <UButton to="/order/list" class="mt-6">查看订单</UButton>
        </div>

        <!-- 已取消 -->
        <div v-else class="py-6">
          <span class="text-5xl block mb-3">⏰</span>
          <p class="text-lg font-semibold text-default mb-1">订单已取消</p>
          <UButton to="/order/list" variant="outline" class="mt-6">返回订单列表</UButton>
        </div>
      </div>

      <!-- 支付结果弹窗 -->
      <div v-if="payResult" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div class="bg-elevated rounded-lg p-8 text-center shadow-xl max-w-sm w-[90%]">
          <span class="text-5xl block mb-3">{{ payResult.success ? '✅' : '❌' }}</span>
          <p class="text-lg font-semibold text-default">{{ payResult.success ? '支付成功' : '支付失败' }}</p>
          <p class="text-sm text-muted mt-1">{{ payResult.message }}</p>
          <div class="flex gap-3 justify-center mt-5">
            <template v-if="payResult.success">
              <UButton to="/order/list" size="sm">查看订单</UButton>
              <UButton to="/" variant="ghost" size="sm">返回首页</UButton>
            </template>
            <template v-else>
              <UButton size="sm" @click="retryPay">重试</UButton>
              <UButton to="/order/list" variant="ghost" size="sm">查看订单</UButton>
            </template>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()
const route = useRoute()

const orderId = Number(route.query.orderId)
const order = ref<any>(null)
const paying = ref(false)
const loading = ref(false)
const payResult = ref<{ success: boolean; message: string } | null>(null)

async function loadOrder() {
  if (!orderId) {
    toast.error('缺少订单ID')
    return
  }
  loading.value = true
  try {
    const res = await api.get<any>('/api/trade/order/' + orderId)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch {}
  loading.value = false
}

async function handlePay() {
  paying.value = true
  payResult.value = null
  try {
    const res = await api.post<any>('/api/trade/payment/pay', null, { orderId, method: 'alipay' })
    if (res.code === 200) {
      const payment = res.data
      if (payment.status === 1) {
        payResult.value = { success: true, message: '支付成功，订单已提交' }
        await loadOrder()
      } else {
        payResult.value = { success: false, message: '支付失败，请重试' }
      }
    }
  } catch {
    payResult.value = { success: false, message: '支付请求异常，请重试' }
  } finally {
    paying.value = false
  }
}

function retryPay() {
  payResult.value = null
  loadOrder().then(() => {
    if (order.value?.status === 0) {
      handlePay()
    }
  })
}

onMounted(() => {
  loadOrder()
})
</script>
