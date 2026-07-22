<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">订单管理</h1>

    <!-- 筛选 -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div class="flex gap-4 flex-wrap items-center">
        <USelect
          v-model="filters.status"
          :options="statusOptions"
          class="w-40"
        />
        <UInput
          v-model="filters.keyword"
          placeholder="搜索订单号..."
          icon="i-lucide-search"
          class="w-52"
          @keyup.enter="loadOrders"
        />
        <UInput v-model="filters.startDate" type="date" class="w-40" placeholder="开始日期" />
        <span class="text-gray-400">至</span>
        <UInput v-model="filters.endDate" type="date" class="w-40" placeholder="结束日期" />
        <UButton @click="loadOrders">搜索</UButton>
        <UButton variant="ghost" color="gray" @click="resetFilters">重置</UButton>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="bg-white rounded-lg shadow-sm">
      <div v-if="loading" class="flex justify-center py-10">
        <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
      </div>

      <table v-else class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">订单号</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">金额</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">状态</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">创建时间</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in orders" :key="item.id" class="border-t border-gray-100">
            <td class="px-4 py-3 text-sm font-mono">{{ item.order_no }}</td>
            <td class="px-4 py-3 text-sm font-medium">¥{{ item.final_amount }}</td>
            <td class="px-4 py-3">
              <UBadge :color="getStatusColor(item.status)" size="sm">
                {{ item.statusDesc }}
              </UBadge>
            </td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ formatDate(item.created_at) }}</td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <UButton size="xs" variant="outline" @click="viewDetail(item.id)">详情</UButton>
                <UButton
                  v-if="item.status === 1"
                  size="xs"
                  color="success"
                  @click="shipOrder(item.id)"
                >
                  发货
                </UButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && !orders.length" class="text-center py-10 text-gray-500">
        暂无订单
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center py-4 border-t border-gray-100">
        <UPagination
          v-model:page="currentPage"
          :total="total"
          :page-size="pageSize"
          @update:page="loadOrders"
        />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <UModal v-model="showDetailModal">
      <div v-if="currentOrder" class="p-6 max-h-[80vh] overflow-y-auto">
        <h2 class="text-lg font-semibold mb-4">订单详情</h2>

        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-500">订单号</p>
              <p class="font-mono">{{ currentOrder.order_no }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">状态</p>
              <UBadge :color="getStatusColor(currentOrder.status)">
                {{ currentOrder.statusDesc }}
              </UBadge>
            </div>
          </div>

          <div class="border-t pt-4">
            <p class="text-sm text-gray-500 mb-2">商品清单</p>
            <div v-for="item in currentOrder.items" :key="item.id" class="flex gap-3 py-2">
              <img
                :src="item.product_image || 'https://picsum.photos/seed/placeholder/60/60'"
                class="w-12 h-12 object-cover rounded"
              />
              <div class="flex-1">
                <p class="text-sm font-medium">{{ item.product_name }}</p>
                <p class="text-sm text-gray-500">¥{{ item.price }} × {{ item.quantity }}</p>
              </div>
            </div>
          </div>

          <div class="border-t pt-4">
            <p class="text-sm text-gray-500 mb-2">收货地址</p>
            <p class="text-sm">{{ formatAddress(currentOrder.address_snapshot) }}</p>
          </div>

          <div class="border-t pt-4 text-right">
            <p class="text-sm text-gray-500">实付金额</p>
            <p class="text-2xl font-bold text-error">¥{{ currentOrder.final_amount }}</p>
          </div>
        </div>

        <div class="flex justify-end pt-4">
          <UButton variant="outline" @click="showDetailModal = false">关闭</UButton>
        </div>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()
const route = useRoute()

const loading = ref(true)
const orders = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

const filters = reactive({
  status: undefined as number | undefined,
  keyword: '',
  startDate: '',
  endDate: '',
})

const statusOptions = [
  { label: '全部状态', value: undefined },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '已退款', value: 5 },
]

const showDetailModal = ref(false)
const currentOrder = ref<any>(null)

function getStatusColor(status: number) {
  const colorMap: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'info',
    3: 'success',
    4: 'gray',
    5: 'error',
  }
  return colorMap[status] || 'gray'
}

function formatDate(date: string) {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 19)
}

function formatAddress(snapshot: string) {
  try {
    const addr = JSON.parse(snapshot)
    return `${addr.province} ${addr.city} ${addr.district} ${addr.detail} ${addr.receiverName} ${addr.phone}`
  } catch {
    return snapshot
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(currentPage.value),
      size: String(pageSize),
    })
    if (filters.status !== undefined) params.append('status', String(filters.status))
    if (filters.keyword) params.append('keyword', filters.keyword)
    if (filters.startDate) params.append('startDate', filters.startDate)
    if (filters.endDate) params.append('endDate', filters.endDate)

    const res = await api.get<any>(`/api/admin/order/list?${params}`)
    if (res.code === 200) {
      orders.value = res.data.list
      total.value = res.data.total
    }
  } catch {}

  loading.value = false
}

async function viewDetail(id: number) {
  try {
    const res = await api.get<any>(`/api/admin/order/${id}`)
    if (res.code === 200) {
      currentOrder.value = res.data
      showDetailModal.value = true
    }
  } catch {}
}

async function shipOrder(id: number) {
  try {
    await api.put(`/api/admin/order/${id}/ship`)
    toast.success('发货成功')
    await loadOrders()
  } catch {}
}

function resetFilters() {
  filters.status = undefined
  filters.keyword = ''
  filters.startDate = ''
  filters.endDate = ''
  currentPage.value = 1
  loadOrders()
}

onMounted(() => {
  // 从 URL 参数读取状态筛选
  if (route.query.status) {
    filters.status = Number(route.query.status)
  }
  loadOrders()
})
</script>
