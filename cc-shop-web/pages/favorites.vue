<template>
  <div class="container">
    <h1 class="page-title">我的收藏</h1>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="productIds.length === 0" class="empty-state">
      <span style="font-size:48px">💝</span>
      <p class="mt-4">还没有收藏的商品</p>
      <router-link to="/product/list" class="btn btn-primary mt-4">去逛逛</router-link>
    </div>
    <div v-else class="card mt-6">
      <p>已收藏 {{ productIds.length }} 件商品</p>
      <p class="text-sm text-secondary mt-4">商品卡片将在 Day 2 完善</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const api = useApi()
const productIds = ref<number[]>([])
const loading = ref(true)
onMounted(async () => {
  try {
    const res = await api.get<any>('/api/user/favorite')
    if (res.code === 200) productIds.value = res.data || []
  } catch {}
  loading.value = false
})
</script>

<style scoped>
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; }
</style>
