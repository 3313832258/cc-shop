<template>
  <div class="max-w-3xl mx-auto px-5">
    <div class="flex items-center gap-4 mb-6">
      <UButton variant="ghost" icon="i-lucide-arrow-left" @click="navigateTo('/order/list')" />
      <h2 class="text-xl font-bold text-default">评价订单</h2>
    </div>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else-if="order">
      <div v-for="item in order.items" :key="item.id" class="bg-elevated rounded-lg shadow-sm p-5 mb-4">
        <div class="flex items-center gap-4 mb-4">
          <img :src="item.productImage || 'https://picsum.photos/seed/placeholder/80/80'" class="w-16 h-16 rounded bg-muted object-cover" />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-default truncate">{{ item.productName }}</p>
            <p class="text-xs text-dimmed">x{{ item.quantity }} · ¥{{ item.price }}</p>
          </div>
        </div>

        <!-- 评分 -->
        <div class="flex items-center gap-2 mb-3">
          <span class="text-sm text-muted">评分：</span>
          <button
            v-for="star in 5"
            :key="star"
            @click="setRating(item.productId, star)"
            class="text-xl transition-colors"
            :class="star <= (reviews[item.productId]?.rating || 0) ? 'text-yellow-400' : 'text-gray-300'"
          >
            ★
          </button>
        </div>

        <!-- 评价内容 -->
        <UTextarea
          v-model="reviews[item.productId].content"
          placeholder="分享你的使用体验..."
          :rows="3"
        />
      </div>

      <div class="flex justify-end gap-3 mt-6">
        <UButton variant="outline" @click="navigateTo('/order/list')">取消</UButton>
        <UButton color="primary" :loading="submitting" @click="submitReviews">提交评价</UButton>
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
const authStore = useAuthStore()

const orderId = Number(route.query.orderId)
const loading = ref(true)
const submitting = ref(false)
const order = ref<any>(null)

// { productId: { rating, content, skuId, orderId } }
const reviews = reactive<Record<number, { rating: number; content: string; skuId: number; orderId: number }>>({})

onMounted(async () => {
  try {
    const res = await api.get<any>(`/api/trade/order/${orderId}`)
    if (res.code === 200) {
      order.value = res.data
      // 初始化每个商品的评价数据
      for (const item of res.data.items || []) {
        reviews[item.productId] = {
          rating: 5,
          content: '',
          skuId: item.skuId || item.id,
          orderId: orderId,
        }
      }
    }
  } catch {}
  loading.value = false
})

function setRating(productId: number, rating: number) {
  if (reviews[productId]) {
    reviews[productId].rating = rating
  }
}

async function submitReviews() {
  submitting.value = true
  try {
    const entries = Object.entries(reviews)
    let successCount = 0
    for (const [productId, review] of entries) {
      if (!review.content.trim()) continue
      await api.post('/api/product/review', {
        userId: Number(authStore.userId),
        productId: Number(productId),
        skuId: review.skuId,
        orderId: review.orderId,
        rating: review.rating,
        content: review.content,
      })
      successCount++
    }
    if (successCount > 0) {
      toast.success(`成功评价 ${successCount} 件商品`)
      navigateTo('/order/list')
    } else {
      toast.warning('请至少填写一条评价')
    }
  } catch {
    toast.error('提交失败，请重试')
  }
  submitting.value = false
}
</script>
