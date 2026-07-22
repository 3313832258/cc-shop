<template>
  <div class="max-w-7xl mx-auto px-5">
    <h1 class="text-xl font-bold text-default mb-6">我的收藏</h1>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <div v-else-if="products.length === 0" class="flex flex-col items-center justify-center py-16">
      <span class="text-5xl mb-4">💝</span>
      <p class="text-dimmed mb-4">还没有收藏的商品</p>
      <UButton to="/product/list">去逛逛</UButton>
    </div>

    <div v-else>
      <p class="text-sm text-dimmed mb-4">共 {{ products.length }} 件商品</p>
      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        <div v-for="product in products" :key="product.id" class="relative group">
          <ProductCard :product="product" />
          <button
            @click="removeFavorite(product.id)"
            class="absolute top-2 right-2 w-8 h-8 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
            title="取消收藏"
          >
            <UIcon name="i-lucide-x" class="text-white" size="16" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()
const loading = ref(true)
const products = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await api.get<any>('/api/user/favorite')
    if (res.code === 200 && res.data?.length > 0) {
      const ids = res.data
      const batchRes = await api.post<any>('/api/product/batch', ids)
      if (batchRes.code === 200) {
        products.value = batchRes.data || []
      }
    }
  } catch {}
  loading.value = false
})

async function removeFavorite(productId: number) {
  try {
    await api.del(`/api/user/favorite/${productId}`)
    products.value = products.value.filter(p => p.id !== productId)
    toast.info('已取消收藏')
  } catch {}
}
</script>
