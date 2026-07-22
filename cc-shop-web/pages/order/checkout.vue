<template>
  <div class="max-w-7xl mx-auto px-5">
    <h2 class="text-xl font-bold text-default mb-6">确认订单</h2>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else>
      <!-- 收货地址 -->
      <section class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <h3 class="text-base font-semibold text-default mb-4 pb-3 border-b border-muted">收货地址</h3>
        <AddressSelector v-model="selectedAddressId" />
      </section>

      <!-- 商品清单 -->
      <section class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <h3 class="text-base font-semibold text-default mb-4 pb-3 border-b border-muted">商品清单</h3>
        <div v-if="!items.length" class="text-center py-8 text-dimmed">没有选中的商品</div>
        <div v-for="item in items" :key="item.skuId" class="flex items-center gap-3 py-3 border-b border-muted last:border-b-0">
          <img
            :src="item.productImage || 'https://picsum.photos/seed/placeholder/80/80'"
            :alt="item.productName"
            class="w-15 h-15 object-cover rounded-md bg-muted shrink-0"
          >
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-default truncate">{{ item.productName }}</p>
            <p v-if="item.specs" class="text-xs text-dimmed mt-0.5">{{ formatSpecs(item.specs) }}</p>
          </div>
          <span class="text-sm text-error font-medium shrink-0">¥{{ item.price }}</span>
          <span class="text-xs text-dimmed shrink-0">×{{ item.quantity }}</span>
          <span class="text-sm font-semibold text-default shrink-0 min-w-[60px] text-right">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </section>

      <!-- 优惠券 -->
      <section class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <h3 class="text-base font-semibold text-default mb-4 pb-3 border-b border-muted">优惠券</h3>
        <div v-if="!coupons.length" class="text-sm text-dimmed">暂无可用优惠券</div>
        <div v-else class="flex flex-col gap-2">
          <label
            v-for="c in coupons"
            :key="c.id"
            class="flex items-center gap-2.5 p-2.5 rounded-lg border cursor-pointer transition-colors"
            :class="selectedCouponId === c.id ? 'border-primary bg-primary/10' : 'border-default hover:border-primary/50'"
          >
            <input
              type="radio"
              name="coupon"
              :value="c.id"
              :checked="selectedCouponId === c.id"
              class="accent-primary"
              @change="selectedCouponId = c.id"
            >
            <span class="flex-1 text-sm">
              <span v-if="c.type === 0">满{{ c.minOrderAmount }}减{{ c.value }}</span>
              <span v-else-if="c.type === 1">{{ formatPercent(c.value) }}折 (满{{ c.minOrderAmount }})</span>
            </span>
            <UBadge :color="c.type === 0 ? 'primary' : 'warning'" variant="subtle" size="xs">
              {{ c.type === 0 ? '满减' : '折扣' }}
            </UBadge>
          </label>
          <UButton
            v-if="selectedCouponId"
            variant="ghost"
            size="sm"
            @click="selectedCouponId = null"
          >
            不使用优惠券
          </UButton>
        </div>
      </section>

      <!-- 金额汇总 -->
      <section class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <div class="flex flex-col gap-2.5">
          <div class="flex justify-between text-sm">
            <span class="text-muted">商品合计</span>
            <span class="text-default">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div v-if="discountAmount > 0" class="flex justify-between text-sm text-error">
            <span>优惠减免</span>
            <span>−¥{{ discountAmount.toFixed(2) }}</span>
          </div>
          <div class="flex justify-between text-base font-bold pt-2.5 border-t border-muted">
            <span>实付金额</span>
            <span class="text-error">¥{{ finalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </section>

      <!-- 提交 -->
      <div class="sticky bottom-4 p-4 bg-elevated rounded-lg shadow-lg flex items-center justify-end gap-6">
        <span class="text-xl font-bold text-error">¥{{ finalAmount.toFixed(2) }}</span>
        <UButton
          size="lg"
          color="primary"
          :disabled="!selectedAddressId || submitting"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交订单' }}
        </UButton>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()
const tracker = useTracker()

interface CartItem {
  skuId: number
  productId: number
  productName: string
  productImage: string
  specs: Record<string, string> | null
  price: number
  originalPrice: number | null
  quantity: number
  selected: boolean
}

const items = ref<CartItem[]>([])
const coupons = ref<any[]>([])
const selectedAddressId = ref<number | null>(null)
const selectedCouponId = ref<number | null>(null)
const loading = ref(false)
const submitting = ref(false)

const totalAmount = computed(() =>
  items.value.reduce((s, i) => s + i.price * i.quantity, 0)
)

const discountAmount = computed(() => {
  const coupon = coupons.value.find(c => c.id === selectedCouponId.value)
  if (!coupon) return 0
  if (totalAmount.value < (coupon.minOrderAmount || 0)) return 0
  if (coupon.type === 0) return Number(coupon.value) || 0
  if (coupon.type === 1) return totalAmount.value * (1 - Number(coupon.value))
  return 0
})

const finalAmount = computed(() => Math.max(0, totalAmount.value - discountAmount.value))

function formatSpecs(specs: Record<string, string> | null): string {
  if (!specs) return ''
  return Object.entries(specs).map(([k, v]) => `${k}: ${v}`).join('，')
}

function formatPercent(v: number): string {
  return (Number(v) * 10).toFixed(1)
}

async function loadCheckoutData() {
  loading.value = true
  try {
    const [cartRes, couponRes] = await Promise.all([
      api.get<any>('/api/trade/cart/list'),
      api.get<any>('/api/promotion/coupon/my'),
    ])
    if (cartRes.code === 200) {
      items.value = (cartRes.data || []).filter((i: CartItem) => i.selected)
    }
    if (couponRes.code === 200) {
      const allCoupons = couponRes.data || []
      coupons.value = allCoupons.filter((c: any) => c.status === 0)
    }
  } catch {}
  loading.value = false

  if (!items.value.length) {
    toast.error('没有选中的商品，请返回购物车勾选')
    navigateTo('/cart')
  }
}

async function handleSubmit() {
  if (!selectedAddressId.value) {
    toast.error('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const body: any = { addressId: selectedAddressId.value }
    if (selectedCouponId.value) {
      body.userCouponId = selectedCouponId.value
    }
    const res = await api.post<any>('/api/trade/order/place', body)
    if (res.code === 200) {
      toast.success('下单成功')
      // 埋点：下单
      tracker.placeOrder(res.data.id, finalAmount.value)
      navigateTo('/order/pay?orderId=' + res.data.id)
    }
  } catch {
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCheckoutData()
})
</script>
