<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <div class="filter-bar">
        <select v-model="filters.categoryId" class="form-input filter-select" @change="loadProducts">
          <option :value="undefined">全部分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <select v-model="filters.sort" class="form-input filter-select" @change="loadProducts">
          <option value="">默认排序</option>
          <option value="price_asc">价格从低到高</option>
          <option value="price_desc">价格从高到低</option>
        </select>
      </div>
    </div>

    <!-- 搜索结果提示 -->
    <div v-if="keyword" class="search-hint mb-4">
      搜索 "<strong>{{ keyword }}</strong>" 的结果（{{ total }} 件商品）
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="products.length === 0" class="empty-state">
      <span style="font-size:48px">🔍</span>
      <p class="mt-4">暂无商品</p>
    </div>

    <div v-else class="grid grid-4">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination mt-8">
      <button class="btn btn-sm btn-outline" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
      <span class="page-info text-sm text-secondary">第 {{ page }} / {{ totalPages }} 页</span>
      <button class="btn btn-sm btn-outline" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const router = useRouter()
const api = useApi()

const keyword = computed(() => route.query.keyword as string || '')
const pageTitle = computed(() => keyword.value ? '搜索结果' : '全部商品')

const categories = ref<any[]>([])
const products = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)
const loading = ref(false)

const filters = reactive<{ categoryId?: number; sort: string }>({
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  sort: '',
})

onMounted(async () => {
  try {
    const catRes = await api.get<any>('/api/product/category/tree')
    if (catRes.code === 200) {
      // Flatten categories for filter dropdown
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

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
}

.filter-bar {
  display: flex;
  gap: 12px;
}

.filter-select {
  width: auto;
  min-width: 140px;
}

.search-hint {
  font-size: 14px;
  color: var(--text-secondary);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}
</style>
