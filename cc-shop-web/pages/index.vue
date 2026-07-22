<template>
  <div class="max-w-7xl mx-auto px-5">
    <!-- 轮播 Banner -->
    <section class="mb-10">
      <div class="relative rounded-2xl overflow-hidden h-64">
        <div
          v-for="(banner, i) in banners"
          :key="i"
          :class="[
            'absolute inset-0 transition-opacity duration-700',
            i === activeBanner ? 'opacity-100 z-10' : 'opacity-0 z-0'
          ]"
        >
          <div :class="['h-full flex items-center justify-between p-12 rounded-2xl', banner.bg]">
            <div>
              <h2 class="text-3xl font-extrabold text-white mb-3">{{ banner.title }}</h2>
              <p class="text-white/80 text-lg mb-5">{{ banner.desc }}</p>
              <UButton :to="banner.link" size="lg" color="neutral" variant="solid">立即查看</UButton>
            </div>
            <span class="text-8xl opacity-30">{{ banner.icon }}</span>
          </div>
        </div>
        <!-- 指示器 -->
        <div class="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-2 z-20">
          <button
            v-for="(_, i) in banners"
            :key="i"
            :class="['w-2.5 h-2.5 rounded-full transition-colors', i === activeBanner ? 'bg-white' : 'bg-white/40']"
            @click="activeBanner = i"
          />
        </div>
      </div>
    </section>

    <!-- 分类入口 -->
    <section class="mb-10">
      <h2 class="text-xl font-bold text-default mb-5">热门分类</h2>
      <div class="grid grid-cols-4 gap-5">
        <NuxtLink
          v-for="cat in categories"
          :key="cat.id"
          :to="`/product/list?categoryId=${cat.id}`"
          class="flex flex-col items-center gap-3 p-6 bg-elevated rounded-lg shadow-sm hover:shadow-md hover:-translate-y-1 transition-all duration-200 no-underline"
        >
          <span class="text-3xl">{{ cat.icon || '📦' }}</span>
          <span class="font-medium text-muted">{{ cat.name }}</span>
        </NuxtLink>
      </div>
    </section>

    <!-- 热卖排行 -->
    <section class="mb-10" v-if="hotProducts.length">
      <h2 class="text-xl font-bold text-default mb-5">🔥 热卖排行</h2>
      <div class="grid grid-cols-5 gap-4">
        <NuxtLink
          v-for="(p, i) in hotProducts"
          :key="p.id"
          :to="`/product/${p.id}`"
          class="relative bg-elevated rounded-lg shadow-sm overflow-hidden hover:shadow-lg transition-all duration-200 hover:-translate-y-1 no-underline"
        >
          <span
            :class="[
              'absolute top-2 left-2 w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white z-10',
              i === 0 ? 'bg-red-500' : i === 1 ? 'bg-orange-500' : i === 2 ? 'bg-yellow-500' : 'bg-gray-400'
            ]"
          >
            {{ i + 1 }}
          </span>
          <div class="aspect-square bg-muted overflow-hidden">
            <img
              :src="p.image || 'https://picsum.photos/seed/placeholder/400/400'"
              :alt="p.name"
              loading="lazy"
              class="w-full h-full object-cover"
            />
          </div>
          <div class="p-3">
            <h3 class="text-sm font-medium leading-snug line-clamp-2 text-default mb-1">{{ p.name }}</h3>
            <span class="text-base font-bold text-warning">¥{{ formatPrice(p.price) }}</span>
          </div>
        </NuxtLink>
      </div>
    </section>

    <!-- 推荐商品 -->
    <section class="mb-12">
      <h2 class="text-xl font-bold text-default mb-5">推荐商品</h2>
      <div class="grid grid-cols-4 gap-5">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const api = useApi()

const categories = ref<any[]>([])
const products = ref<any[]>([])
const hotProducts = ref<any[]>([])

// Banner 轮播
const banners = [
  { title: '新品首发', desc: '科技改变生活，新品限时优惠', icon: '📱', bg: 'bg-gradient-to-br from-blue-600 to-blue-800', link: '/product/list' },
  { title: '限时秒杀', desc: '每日精选好物，低至 1 折起', icon: '⚡', bg: 'bg-gradient-to-br from-red-500 to-red-700', link: '/promotion/flash' },
  { title: '领券中心', desc: '满减优惠券，下单更划算', icon: '🎫', bg: 'bg-gradient-to-br from-emerald-500 to-emerald-700', link: '/coupon' },
]
const activeBanner = ref(0)
let bannerTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  // 自动轮播
  bannerTimer = setInterval(() => {
    activeBanner.value = (activeBanner.value + 1) % banners.length
  }, 4000)

  // 分类
  try {
    const catRes = await api.get<any>('/api/product/category/tree')
    if (catRes.code === 200) {
      categories.value = (catRes.data || []).slice(0, 4).map((c: any) => ({
        id: c.id,
        name: c.name,
        icon: c.icon || getCategoryIcon(c.id),
      }))
    }
  } catch {}

  // 推荐商品
  try {
    const prodRes = await api.get<any>('/api/product/list', { page: 1, size: 8 })
    if (prodRes.code === 200 && prodRes.data) {
      products.value = prodRes.data.records || []
    }
  } catch {}

  // 热卖排行（取前 5 个商品作为模拟）
  try {
    const hotRes = await api.get<any>('/api/product/list', { page: 1, size: 5 })
    if (hotRes.code === 200 && hotRes.data) {
      hotProducts.value = hotRes.data.records || []
    }
  } catch {}
})

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
})

function getCategoryIcon(id: number): string {
  const icons: Record<number, string> = { 1: '📱', 2: '💻', 3: '🏠', 4: '👗', 5: '🍔', 6: '📚' }
  return icons[id] || '📦'
}

function formatPrice(price?: number): string {
  if (!price) return '0.00'
  return Number(price).toFixed(2)
}
</script>
