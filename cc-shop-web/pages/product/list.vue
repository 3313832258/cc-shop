<template>
  <div class="max-w-7xl mx-auto px-5">
    <div class="flex justify-between items-center mb-6 flex-wrap gap-4">
      <h1 class="text-xl font-bold text-default">{{ pageTitle }}</h1>
      <div class="flex gap-3 items-center flex-wrap">
        <USelect
          v-model="filters.categoryId"
          :items="categoryOptions"
          placeholder="全部分类"
          class="w-36"
          @update:model-value="loadProducts"
        />
        <div class="flex items-center gap-1">
          <UInput v-model="filters.priceMin" type="number" placeholder="最低价" class="w-24" size="sm" />
          <span class="text-dimmed">-</span>
          <UInput v-model="filters.priceMax" type="number" placeholder="最高价" class="w-24" size="sm" />
          <UButton size="sm" variant="outline" @click="loadProducts">筛选</UButton>
        </div>
        <USelect
          v-model="filters.sort"
          :items="sortOptions"
          placeholder="默认排序"
          class="w-36"
          @update:model-value="loadProducts"
        />
      </div>
    </div>

    <!-- 搜索结果提示 -->
    <div v-if="keyword" class="text-sm text-muted mb-4">
      搜索 "<strong class="text-default">{{ keyword }}</strong>" 的结果（{{ total }} 件商品）
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="grid grid-cols-4 gap-5">
      <SkeletonCard v-for="i in 8" :key="i" />
    </div>

    <div v-else-if="products.length === 0" class="flex flex-col items-center justify-center py-16">
      <span class="text-5xl mb-4">🔍</span>
      <p class="text-dimmed">暂无商品</p>
    </div>

    <div v-else class="grid grid-cols-4 gap-5">
      <ProductCard v-for="p in filteredProducts" :key="p.id" :product="p" />
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-4 mt-8">
      <UButton size="sm" variant="outline" :disabled="page <= 1" @click="goPage(page - 1)">上一页</UButton>
      <span class="text-sm text-muted">第 {{ page }} / {{ totalPages }} 页</span>
      <UButton size="sm" variant="outline" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const api = useApi()

const keyword = computed(() => route.query.keyword as string || '')
const pageTitle = computed(() => keyword.value ? '搜索结果' : '全部商品')

const categories = ref<any[]>([])
const products = ref<any[]>([])
const filteredProducts = computed(() => {
  return products.value.filter(p => {
    if (filters.priceMin != null && p.price != null && p.price < filters.priceMin) return false
    if (filters.priceMax != null && p.price != null && p.price > filters.priceMax) return false
    return true
  })
})
const total = ref(0)
const page = ref(1)
const pageSize = 20
const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)
const loading = ref(false)

const filters = reactive<{ categoryId?: number; sort?: string; priceMin?: number; priceMax?: number }>({
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  sort: undefined,
  priceMin: undefined,
  priceMax: undefined,
})

const categoryOptions = computed(() => [
  { label: '全部分类', value: undefined },
  ...categories.value.map((c: any) => ({ label: c.name, value: c.id })),
])

const sortOptions = [
  { label: '默认排序', value: undefined },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' },
]

onMounted(async () => {
  try {
    const catRes = await api.get<any>('/api/product/category/tree')
    if (catRes.code === 200) {
      const flat: any[] = []
      for (const c of catRes.data || []) {
        flat.push(c)
        if (c.children) flat.push(...c.children)
      }
      categories.value = flat
    }
  } catch {}

  if (keyword.value) {
    await searchProducts()
  } else {
    await loadProducts()
  }
})

watch(() => route.query.keyword, async (newVal) => {
  if (newVal) await searchProducts()
})

async function loadProducts() {
  loading.value = true
  try {
    const res = await api.get<any>('/api/product/list', {
      page: page.value,
      size: pageSize,
      categoryId: filters.categoryId,
      sort: filters.sort || undefined,
    })
    if (res.code === 200 && res.data) {
      products.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {} finally {
    loading.value = false
  }
}

async function searchProducts() {
  loading.value = true
  try {
    const res = await api.get<any>('/api/product/search', { keyword: keyword.value })
    if (res.code === 200) {
      products.value = res.data || []
      total.value = products.value.length
    }
  } catch {} finally {
    loading.value = false
  }
}

function goPage(p: number) {
  page.value = p
  if (keyword.value) searchProducts()
  else loadProducts()
}
</script>
