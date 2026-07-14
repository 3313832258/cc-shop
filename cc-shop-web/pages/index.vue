<template>
  <div class="container">
    <!-- Hero Banner -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">CC<span class="accent">Shop</span></h1>
        <p class="hero-desc">简洁·品质·技术驱动的微服务电商体验</p>
        <router-link to="/product/list" class="btn btn-primary btn-lg">浏览商品</router-link>
      </div>
      <div class="hero-visual">
        <div class="hero-grid">
          <div class="hero-card hc-1">📱 手机数码</div>
          <div class="hero-card hc-2">💻 电脑办公</div>
          <div class="hero-card hc-3">👟 运动时尚</div>
          <div class="hero-card hc-4">🏠 家用电器</div>
        </div>
      </div>
    </section>

    <!-- 分类入口 -->
    <section class="section">
      <h2 class="section-title">热门分类</h2>
      <div class="grid grid-4 category-grid">
        <router-link
          v-for="cat in categories"
          :key="cat.id"
          :to="`/product/list?categoryId=${cat.id}`"
          class="card category-card"
        >
          <span class="cat-icon">{{ cat.icon || '📦' }}</span>
          <span class="cat-name">{{ cat.name }}</span>
        </router-link>
      </div>
    </section>

    <!-- 推荐商品 -->
    <section class="section">
      <h2 class="section-title">推荐商品</h2>
      <div class="grid grid-4">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const api = useApi()

const categories = ref<any[]>([])
const products = ref<any[]>([])

onMounted(async () => {
  try {
    const catRes = await api.get<any>('/api/product/category/tree')
    if (catRes.code === 200) {
      // 取一级分类，最多4个
      categories.value = (catRes.data || []).slice(0, 4).map((c: any) => ({
        id: c.id,
        name: c.name,
        icon: c.icon || getCategoryIcon(c.id),
      }))
    }
  } catch {}

  try {
    const prodRes = await api.get<any>('/api/product/list', { page: 1, size: 8 })
    if (prodRes.code === 200 && prodRes.data) {
      products.value = prodRes.data.records || []
    }
  } catch {}
})

function getCategoryIcon(id: number): string {
  const icons: Record<number, string> = {
    1: '📱', 2: '💻', 3: '🏠', 4: '👗', 5: '🍔', 6: '📚',
  }
  return icons[id] || '📦'
}
</script>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, var(--primary) 0%, #4285f4 100%);
  color: white;
  border-radius: 16px;
  padding: 48px;
  margin-bottom: 48px;
  overflow: hidden;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  letter-spacing: -1px;
}

.hero-title .accent { color: var(--warning); }

.hero-desc {
  font-size: 18px;
  opacity: 0.9;
  margin: 16px 0 24px;
}

.hero-visual { flex-shrink: 0; }

.hero-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.hero-card {
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px 24px;
  font-size: 15px;
  font-weight: 500;
  color: white;
}

.section { margin-bottom: 48px; }

.section-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
  color: var(--text);
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  transition: transform 0.2s, box-shadow 0.2s;
  text-decoration: none;
  color: var(--text);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.cat-icon { font-size: 32px; }
.cat-name { font-weight: 500; }

@media (max-width: 768px) {
  .hero { flex-direction: column; padding: 32px 24px; text-align: center; }
  .hero-visual { margin-top: 24px; }
}
</style>
