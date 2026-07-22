<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">商品管理</h1>
      <UButton @click="showAddModal = true">
        <UIcon name="i-lucide-plus" class="mr-1" />
        新增商品
      </UButton>
    </div>

    <!-- 筛选 -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div class="flex gap-4 flex-wrap items-center">
        <UInput
          v-model="filters.keyword"
          placeholder="搜索商品名称..."
          icon="i-lucide-search"
          class="w-52"
          @keyup.enter="loadProducts"
        />
        <USelect
          v-model="filters.categoryId"
          :options="categoryOptions"
          class="w-40"
        />
        <USelect
          v-model="filters.status"
          :options="[
            { label: '全部状态', value: undefined },
            { label: '上架', value: 1 },
            { label: '下架', value: 0 },
          ]"
          class="w-32"
        />
        <UButton @click="loadProducts">搜索</UButton>
        <UButton variant="ghost" color="gray" @click="resetFilters">重置</UButton>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="bg-white rounded-lg shadow-sm">
      <div v-if="loading" class="flex justify-center py-10">
        <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
      </div>

      <table v-else class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">ID</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">商品名称</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">分类</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">品牌</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">状态</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in products" :key="item.id" class="border-t border-gray-100">
            <td class="px-4 py-3 text-sm">{{ item.id }}</td>
            <td class="px-4 py-3 text-sm font-medium">{{ item.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ item.category_name || '-' }}</td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ item.brand_name || '-' }}</td>
            <td class="px-4 py-3">
              <UBadge :color="item.status === 1 ? 'success' : 'gray'" size="sm">
                {{ item.status === 1 ? '上架' : '下架' }}
              </UBadge>
            </td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <UButton size="xs" variant="outline" @click="editProduct(item)">编辑</UButton>
                <UButton
                  size="xs"
                  :color="item.status === 1 ? 'warning' : 'success'"
                  variant="outline"
                  @click="toggleStatus(item)"
                >
                  {{ item.status === 1 ? '下架' : '上架' }}
                </UButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && !products.length" class="text-center py-10 text-gray-500">
        暂无商品
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center py-4 border-t border-gray-100">
        <UPagination
          v-model:page="currentPage"
          :total="total"
          :page-size="pageSize"
          @update:page="loadProducts"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <UModal v-model="showAddModal">
      <div class="p-6">
        <h2 class="text-lg font-semibold mb-4">{{ editingId ? '编辑商品' : '新增商品' }}</h2>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">商品名称</label>
            <UInput v-model="form.name" placeholder="请输入商品名称" required />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">商品描述</label>
            <UTextarea v-model="form.description" placeholder="请输入商品描述" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">分类</label>
              <USelect
                v-model="form.categoryId"
                :options="categories.map(c => ({ label: c.name, value: c.id }))"
                placeholder="选择分类"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">品牌</label>
              <USelect
                v-model="form.brandId"
                :options="brands.map(b => ({ label: b.name, value: b.id }))"
                placeholder="选择品牌"
              />
            </div>
          </div>

          <!-- SKU 列表 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">SKU</label>
            <div v-for="(sku, index) in form.skus" :key="index" class="flex gap-2 mb-2">
              <UInput v-model="sku.skuCode" placeholder="SKU编码" class="w-32" />
              <UInput v-model="sku.price" type="number" placeholder="价格" class="w-24" />
              <UInput v-model="sku.stock" type="number" placeholder="库存" class="w-24" />
              <UInput v-model="sku.specs" placeholder='规格JSON: {"颜色":"黑"}' class="flex-1" />
              <UButton
                color="error"
                variant="outline"
                size="xs"
                @click="form.skus.splice(index, 1)"
              >
                删除
              </UButton>
            </div>
            <UButton variant="outline" size="sm" @click="addSku">
              <UIcon name="i-lucide-plus" class="mr-1" />
              添加 SKU
            </UButton>
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <UButton variant="outline" @click="showAddModal = false">取消</UButton>
            <UButton type="submit" :loading="submitting">保存</UButton>
          </div>
        </form>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()

const loading = ref(true)
const products = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

const filters = reactive({
  keyword: '',
  status: undefined as number | undefined,
  categoryId: undefined as number | undefined,
})

const categoryOptions = computed(() => [
  { label: '全部分类', value: undefined },
  ...categories.value.map((c: any) => ({ label: c.name, value: c.id })),
])

const showAddModal = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)

const categories = ref<any[]>([])
const brands = ref<any[]>([])

const form = reactive({
  name: '',
  description: '',
  categoryId: null as number | null,
  brandId: null as number | null,
  images: '',
  skus: [] as Array<{ skuCode: string; price: number; stock: number; specs: string; image: string }>,
})

async function loadProducts() {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(currentPage.value),
      size: String(pageSize),
    })
    if (filters.keyword) params.append('keyword', filters.keyword)
    if (filters.status !== undefined) params.append('status', String(filters.status))
    if (filters.categoryId !== undefined) params.append('categoryId', String(filters.categoryId))

    const res = await api.get<any>(`/api/admin/product/list?${params}`)
    if (res.code === 200) {
      products.value = res.data.list
      total.value = res.data.total
    }
  } catch {}

  loading.value = false
}

async function loadOptions() {
  try {
    const [catRes, brandRes] = await Promise.all([
      api.get<any>('/api/admin/product/categories'),
      api.get<any>('/api/admin/product/brands'),
    ])
    if (catRes.code === 200) categories.value = catRes.data
    if (brandRes.code === 200) brands.value = brandRes.data
  } catch {}
}

function addSku() {
  form.skus.push({ skuCode: '', price: 0, stock: 0, specs: '', image: '' })
}

function editProduct(item: any) {
  editingId.value = item.id
  form.name = item.name
  form.description = item.description || ''
  form.categoryId = item.category_id
  form.brandId = item.brand_id
  form.images = item.images || ''
  form.skus = []
  showAddModal.value = true

  // 加载商品详情获取 SKU
  api.get<any>(`/api/admin/product/${item.id}`).then(res => {
    if (res.code === 200 && res.data.skus) {
      form.skus = res.data.skus.map((s: any) => ({
        skuCode: s.sku_code || '',
        price: s.price || 0,
        stock: s.stock || 0,
        specs: s.specs || '',
        image: s.image || '',
      }))
    }
  }).catch(() => {
    showAddModal.value = false
    loadProducts()
  })
}

async function toggleStatus(item: any) {
  try {
    const newStatus = item.status === 1 ? 0 : 1
    await api.put(`/api/admin/product/${item.id}/status?status=${newStatus}`)
    toast.success(newStatus === 1 ? '已上架' : '已下架')
    await loadProducts()
  } catch {
    loadProducts()
  }
}

async function handleSubmit() {
  if (!form.name) {
    toast.warning('请输入商品名称')
    return
  }

  submitting.value = true
  try {
    const body = {
      name: form.name,
      description: form.description,
      categoryId: form.categoryId,
      brandId: form.brandId,
      images: form.images,
      skus: form.skus,
    }

    if (editingId.value) {
      await api.put(`/api/admin/product/${editingId.value}`, body)
      toast.success('编辑成功')
    } else {
      await api.post('/api/admin/product', body)
      toast.success('新增成功')
    }

    showAddModal.value = false
    resetForm()
    await loadProducts()
  } catch {}

  submitting.value = false
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.description = ''
  form.categoryId = null
  form.brandId = null
  form.images = ''
  form.skus = []
}

function resetFilters() {
  filters.keyword = ''
  filters.status = undefined
  filters.categoryId = undefined
  currentPage.value = 1
  loadProducts()
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadOptions()])
})
</script>
