<template>
  <div class="container">
    <div v-if="loading" class="loading">加载商品详情...</div>

    <div v-else-if="product" class="product-detail">
      <!-- 主图 + SKU 选择 -->
      <div class="detail-main">
        <div class="gallery">
          <div class="main-image">
            <img :src="currentImage" :alt="product.name" />
          </div>
          <div v-if="product.images && product.images.length > 1" class="thumb-list">
            <img
              v-for="(img, i) in product.images"
              :key="i"
              :src="img"
              :class="['thumb', currentImage === img ? 'active' : '']"
              @click="currentImage = img"
            />
          </div>
        </div>

        <div class="detail-info">
          <h1 class="detail-title">{{ product.name }}</h1>
          <p class="detail-desc text-secondary">{{ product.description }}</p>

          <!-- SKU 选择器 -->
          <ProductSkuSelector
            v-if="product.skus && product.skus.length > 0"
            :skus="product.skus"
            :spec-options="product.specOptions || {}"
            @change="onSkuChange"
          />

          <!-- 操作按钮 -->
          <div class="detail-actions mt-6">
            <button
              class="btn btn-accent btn-lg"
              :disabled="!selectedSku || selectedSku.stock <= 0"
              @click="addToCart"
            >
              加入购物车
            </button>
            <button
              class="btn btn-primary btn-lg"
              :disabled="!selectedSku || selectedSku.stock <= 0"
              @click="buyNow"
            >
              立即购买
            </button>
            <button
              :class="['btn btn-lg', isFav ? 'btn-accent' : 'btn-outline']"
              @click="toggleFavorite"
            >
              {{ isFav ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 商品参数 -->
      <section class="detail-section card mt-6">
        <h2 class="section-title">商品参数</h2>
        <table v-if="product.specs && product.specs.length" class="spec-table">
          <tr v-for="(s, i) in product.specs" :key="i">
            <td class="spec-name">{{ s.name }}</td>
            <td class="spec-value">{{ s.value }}</td>
          </tr>
        </table>
        <p v-else class="text-secondary">暂无参数信息</p>
      </section>

      <!-- 评价 -->
      <section class="detail-section card mt-6">
        <h2 class="section-title">用户评价</h2>
        <div v-if="product.reviews && product.reviews.length" class="review-list">
          <div v-for="r in product.reviews" :key="r.id" class="review-item">
            <div class="review-header">
              <span class="review-rating">
                {{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}
              </span>
              <span class="text-sm text-secondary">{{ formatDate(r.createdAt) }}</span>
            </div>
            <p class="review-content">{{ r.content }}</p>
          </div>
        </div>
        <p v-else class="text-secondary">暂无评价</p>
      </section>
    </div>

    <div v-else class="empty-state">
      <span style="font-size:48px">😕</span>
      <p class="mt-4">商品不存在</p>
      <router-link to="/product/list" class="btn btn-primary mt-4">返回商品列表</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const api = useApi()
const toast = useToast()
const authStore = useAuthStore()

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
      // 默认选第一个 SKU
      if (res.data.skus && res.data.skus.length > 0) {
        selectedSku.value = res.data.skus[0]
      }
    }
  } catch {}

  // 检查收藏状态
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
  // 如果 SKU 有专属图片，切换主图
  if (sku?.image) {
    currentImage.value = sku.image
  }
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
    }
  } catch {}
}

function buyNow() {
  if (!authStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  toast.info('下单功能将在阶段 2 实现')
}

function formatDate(d: string): string {
  if (!d) return ''
  return d.substring(0, 10)
}
</script>

<style scoped>
.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.gallery {
  position: sticky;
  top: 88px;
  align-self: start;
}

.main-image {
  aspect-ratio: 1;
  background: var(--surface);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.thumb.active, .thumb:hover {
  border-color: var(--primary);
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 12px;
}

.detail-desc {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.detail-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.spec-table {
  width: 100%;
  border-collapse: collapse;
}

.spec-table tr:nth-child(even) {
  background: var(--bg);
}

.spec-table td {
  padding: 10px 16px;
  font-size: 14px;
  border-bottom: 1px solid var(--border);
}

.spec-name {
  width: 120px;
  color: var(--text-secondary);
  font-weight: 500;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.review-rating {
  color: var(--warning);
  font-size: 14px;
}

.review-content {
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .detail-main {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  .gallery {
    position: static;
  }
}
</style>
