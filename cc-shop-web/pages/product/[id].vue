<template>
  <div class="max-w-7xl mx-auto px-5">
    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <div v-else-if="product" class="product-detail">
      <!-- 主图 + SKU 选择 -->
      <div class="grid grid-cols-2 gap-10">
        <!-- 图片区 -->
        <div class="sticky top-[88px] self-start">
          <div class="aspect-square bg-elevated rounded-lg overflow-hidden shadow-sm">
            <img :src="currentImage" :alt="product.name" class="w-full h-full object-cover" />
          </div>
          <div v-if="product.images && product.images.length > 1" class="flex gap-2 mt-3">
            <img
              v-for="(img, i) in product.images"
              :key="i"
              :src="img"
              :class="['w-16 h-16 object-cover rounded-md cursor-pointer border-2 transition-colors', currentImage === img ? 'border-primary' : 'border-transparent hover:border-primary-300']"
              @click="currentImage = img"
            />
          </div>
        </div>

        <!-- 信息区 -->
        <div>
          <h1 class="text-xl font-bold leading-tight mb-3 text-default">{{ product.name }}</h1>
          <p class="text-sm text-muted leading-relaxed mb-6">{{ product.description }}</p>

          <!-- SKU 选择器 -->
          <ProductSkuSelector
            v-if="product.skus && product.skus.length > 0"
            :skus="product.skus"
            :spec-options="product.specOptions || {}"
            @change="onSkuChange"
          />

          <!-- 操作按钮 -->
          <div class="flex gap-3 mt-6 flex-wrap">
            <UButton
              size="lg"
              color="warning"
              :disabled="!selectedSku || selectedSku.stock <= 0"
              @click="addToCart"
            >
              加入购物车
            </UButton>
            <UButton
              size="lg"
              color="primary"
              :disabled="!selectedSku || selectedSku.stock <= 0"
              @click="buyNow"
            >
              立即购买
            </UButton>
            <UButton
              size="lg"
              :variant="isFav ? 'solid' : 'outline'"
              :color="isFav ? 'error' : 'neutral'"
              @click="toggleFavorite"
            >
              {{ isFav ? '❤️ 已收藏' : '🤍 收藏' }}
            </UButton>
          </div>
        </div>
      </div>

      <!-- 商品参数 -->
      <section class="bg-elevated rounded-lg shadow-sm p-5 mt-6">
        <h2 class="text-lg font-semibold text-default mb-4">商品参数</h2>
        <table v-if="product.specs && product.specs.length" class="w-full">
          <tr v-for="(s, i) in product.specs" :key="i" :class="Number(i) % 2 === 1 ? 'bg-muted' : ''">
            <td class="py-2.5 px-4 text-sm text-muted font-medium w-[120px]">{{ s.name }}</td>
            <td class="py-2.5 px-4 text-sm text-default">{{ s.value }}</td>
          </tr>
        </table>
        <p v-else class="text-sm text-dimmed">暂无参数信息</p>
      </section>

      <!-- 评价 -->
      <section class="bg-elevated rounded-lg shadow-sm p-5 mt-6">
        <h2 class="text-lg font-semibold text-default mb-4">用户评价</h2>
        <div v-if="product.reviews && product.reviews.length">
          <div v-for="r in product.reviews" :key="r.id" class="py-4 border-b border-muted last:border-b-0">
            <div class="flex justify-between mb-2">
              <span class="text-sm text-warning">
                {{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}
              </span>
              <span class="text-xs text-dimmed">{{ formatDate(r.createdAt) }}</span>
            </div>
            <p class="text-sm text-muted leading-relaxed">{{ r.content }}</p>
          </div>
        </div>
        <p v-else class="text-sm text-dimmed">暂无评价</p>
      </section>
    </div>

    <div v-else class="flex flex-col items-center justify-center py-16">
      <span class="text-5xl mb-4">😕</span>
      <p class="text-dimmed mb-4">商品不存在</p>
      <UButton to="/product/list" variant="outline">返回商品列表</UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const api = useApi()
const toast = useAppToast()
const authStore = useAuthStore()
const tracker = useTracker()

const productId = Number(route.params.id)
const loading = ref(true)
const product = ref<any>(null)
const selectedSku = ref<any>(null)
const currentImage = ref('')
const isFav = ref(false)

onMounted(async () => {
  try {
    const res = await api.get<any>(`/api/product/detail/${productId}`)
    if (res.code === 200 && res.data) {
      product.value = res.data
      if (res.data.images && res.data.images.length > 0) {
        currentImage.value = res.data.images[0]
      }
      if (res.data.skus && res.data.skus.length > 0) {
        selectedSku.value = res.data.skus[0]
      }
      // 埋点：浏览商品
      tracker.viewProduct(productId)
    }
  } catch {}

  if (authStore.isLoggedIn) {
    try {
      const favRes = await api.get<any>(`/api/user/favorite/${productId}/check`)
      if (favRes.code === 200) isFav.value = favRes.data
    } catch {}
  }

  loading.value = false
})

function onSkuChange(sku: any) {
  selectedSku.value = sku
  if (sku?.image) {
    currentImage.value = sku.image
  }
  // 埋点：点击SKU
  tracker.clickProduct(productId, sku?.id)
}

async function toggleFavorite() {
  if (!authStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  try {
    if (isFav.value) {
      await api.del(`/api/user/favorite/${productId}`)
      isFav.value = false
      toast.info('已取消收藏')
    } else {
      await api.post(`/api/user/favorite/${productId}`)
      isFav.value = true
      toast.success('已收藏')
      // 埋点：收藏商品
      tracker.favoriteProduct(productId)
    }
  } catch {}
}

async function addToCart() {
  if (!authStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  if (!selectedSku.value) {
    toast.warning('请选择规格')
    return
  }
  try {
    const res = await api.post<any>('/api/trade/cart/add', {
      skuId: selectedSku.value.id,
      quantity: 1,
    })
    if (res.code === 200) {
      toast.success('已加入购物车')
      // 埋点：加入购物车
      tracker.addToCart(productId, selectedSku.value.id, 1)
    }
  } catch {}
}

async function buyNow() {
  if (!authStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  if (!selectedSku.value) {
    toast.warning('请选择规格')
    return
  }
  try {
    // 先加入购物车
    const addRes = await api.post<any>('/api/trade/cart/add', {
      skuId: selectedSku.value.id,
      quantity: 1,
    })
    if (addRes.code !== 200) {
      toast.error('加入购物车失败')
      return
    }
    // 设置为选中状态
    await api.put<any>('/api/trade/cart/select', {
      skuId: selectedSku.value.id,
      selected: true,
    })
    // 跳转结算页
    navigateTo('/order/checkout')
  } catch {
    toast.error('操作失败，请重试')
  }
}

function formatDate(d: string): string {
  if (!d) return ''
  return d.substring(0, 10)
}
</script>
