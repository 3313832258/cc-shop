<template>
  <div class="max-w-7xl mx-auto px-5">
    <!-- 头部 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <span class="text-3xl">⚡</span>
        <h1 class="text-2xl font-bold text-default">限时秒杀</h1>
      </div>
      <div class="flex items-center gap-2 text-sm text-muted">
        <UIcon name="i-lucide-clock" />
        <span>每天精选好物，限时抢购</span>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-16">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="32" />
    </div>

    <!-- 空状态 -->
    <div v-else-if="!items.length" class="flex flex-col items-center justify-center py-16">
      <span class="text-6xl mb-4">🎯</span>
      <p class="text-lg text-muted mb-2">暂无秒杀活动</p>
      <p class="text-sm text-dimmed">敬请期待</p>
    </div>

    <!-- 秒杀商品列表 -->
    <div v-else class="grid grid-cols-4 gap-5">
      <SeckillCard
        v-for="item in items"
        :key="item.id"
        :item="item"
        :buying="buyingId === item.id"
        @buy="handleBuy"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const api = useApi()
const toast = useAppToast()

const items = ref<any[]>([])
const loading = ref(true)
const buyingId = ref<number | null>(null)

onMounted(async () => {
  await loadItems()
  loading.value = false
})

async function loadItems() {
  try {
    const res = await api.get<any>('/api/promotion/flash/list')
    if (res.code === 200) {
      items.value = res.data || []
    }
  } catch {}
}

async function handleBuy(itemId: number) {
  buyingId.value = itemId
  try {
    const res = await api.post<any>(`/api/promotion/flash/buy/${itemId}`)
    if (res.code === 200) {
      if (res.data) {
        toast.success('抢购成功！')
        await loadItems()
      } else {
        toast.error('抢购失败，请重试')
      }
    }
  } catch {}
  buyingId.value = null
}
</script>
