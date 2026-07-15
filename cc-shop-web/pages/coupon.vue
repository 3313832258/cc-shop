<template>
  <div class="coupon-page container">
    <h2 class="page-title">优惠券中心</h2>

    <div class="tabs">
      <button
        :class="['tab', { active: activeTab === 'available' }]"
        @click="activeTab = 'available'"
      >
        可领取
      </button>
      <button
        :class="['tab', { active: activeTab === 'my' }]"
        @click="activeTab = 'my'"
      >
        我的优惠券
      </button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <template v-else-if="activeTab === 'available'">
      <div v-if="!availableCoupons.length" class="empty-state">
        <span style="font-size:48px">🎫</span>
        <p>暂无可领取的优惠券</p>
      </div>
      <div v-else class="coupon-grid grid-2">
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
      <div v-if="!myCoupons.length" class="empty-state">
        <span style="font-size:48px">🎟️</span>
        <p>还没有优惠券，去领取吧</p>
      </div>
      <div v-else class="coupon-grid grid-2">
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
const toast = useToast()

const activeTab = ref<'available' | 'my'>('available')
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

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
}

.tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid var(--border);
  margin-bottom: 24px;
}

.tab {
  padding: 10px 24px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  font-family: var(--font-sans);
  transition: color 0.2s, border-color 0.2s;
}

.tab:hover {
  color: var(--text);
}

.tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.coupon-grid {
  gap: 16px;
}
</style>
