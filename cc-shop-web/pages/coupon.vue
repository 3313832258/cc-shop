<template>
  <div class="max-w-7xl mx-auto px-5">
    <h2 class="text-xl font-bold text-default mb-6">优惠券中心</h2>

    <UTabs v-model="activeTab" :items="tabs" class="mb-6" />

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else-if="activeTab === 0">
      <div v-if="!availableCoupons.length" class="flex flex-col items-center justify-center py-16">
        <span class="text-5xl mb-4">🎫</span>
        <p class="text-dimmed">暂无可领取的优惠券</p>
      </div>
      <div v-else class="grid grid-cols-2 gap-4">
        <CouponCard
          v-for="c in availableCoupons"
          :key="c.id"
          :coupon="c"
          mode="available"
          @receive="handleReceive"
        />
      </div>
    </template>

    <template v-else>
      <div v-if="!myCoupons.length" class="flex flex-col items-center justify-center py-16">
        <span class="text-5xl mb-4">🎟️</span>
        <p class="text-dimmed">还没有优惠券，去领取吧</p>
      </div>
      <div v-else class="grid grid-cols-2 gap-4">
        <CouponCard
          v-for="c in myCoupons"
          :key="c.id"
          :coupon="c"
          mode="my"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()

const tabs = [
  { label: '可领取', value: 0 },
  { label: '我的优惠券', value: 1 },
]

const activeTab = ref(0)
const availableCoupons = ref<any[]>([])
const myCoupons = ref<any[]>([])
const loading = ref(false)

async function loadAvailable() {
  try {
    const res = await api.get<any>('/api/promotion/coupon/available')
    if (res.code === 200) {
      availableCoupons.value = res.data || []
    }
  } catch {}
}

async function loadMyCoupons() {
  try {
    const res = await api.get<any>('/api/promotion/coupon/my')
    if (res.code === 200) {
      myCoupons.value = res.data || []
    }
  } catch {}
}

async function handleReceive(couponId: number) {
  try {
    const res = await api.post<any>('/api/promotion/coupon/receive/' + couponId)
    if (res.code === 200) {
      toast.success('领取成功')
      await Promise.all([loadAvailable(), loadMyCoupons()])
    }
  } catch {}
}

onMounted(async () => {
  loading.value = true
  await Promise.all([loadAvailable(), loadMyCoupons()])
  loading.value = false
})
</script>
