<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">售后管理</h1>

    <!-- 筛选 -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div class="flex gap-4">
        <USelect
          v-model="filters.status"
          :options="statusOptions"
          class="w-40"
        />
        <UButton @click="loadAftersales">筛选</UButton>
      </div>
    </div>

    <!-- 售后列表 -->
    <div class="bg-white rounded-lg shadow-sm">
      <div v-if="loading" class="flex justify-center py-10">
        <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
      </div>

      <table v-else class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">ID</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">订单号</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">类型</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">原因</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">金额</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">状态</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">申请时间</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in aftersales" :key="item.id" class="border-t border-gray-100">
            <td class="px-4 py-3 text-sm">{{ item.id }}</td>
            <td class="px-4 py-3 text-sm font-mono">{{ item.order_no }}</td>
            <td class="px-4 py-3 text-sm">{{ item.typeDesc }}</td>
            <td class="px-4 py-3 text-sm text-gray-500 max-w-[200px] truncate">{{ item.reason }}</td>
            <td class="px-4 py-3 text-sm font-medium">¥{{ item.amount }}</td>
            <td class="px-4 py-3">
              <UBadge :color="getStatusColor(item.status)" size="sm">
                {{ item.statusDesc }}
              </UBadge>
            </td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ formatDate(item.created_at) }}</td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <UButton size="xs" variant="outline" @click="viewDetail(item)">详情</UButton>
                <template v-if="item.status === 0">
                  <UButton size="xs" color="success" @click="approve(item.id)">通过</UButton>
                  <UButton size="xs" color="error" variant="outline" @click="reject(item.id)">拒绝</UButton>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && !aftersales.length" class="text-center py-10 text-gray-500">
        暂无售后记录
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center py-4 border-t border-gray-100">
        <UPagination
          v-model:page="currentPage"
          :total="total"
          :page-size="pageSize"
          @update:page="loadAftersales"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <UModal v-model="showDetailModal">
      <div v-if="current" class="p-6 max-h-[80vh] overflow-y-auto">
        <h2 class="text-lg font-semibold mb-4">售后详情 #{{ current.id }}</h2>

        <div class="space-y-4">
          <!-- 基本信息 -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-500">订单号</p>
              <p class="font-mono">{{ current.order_no }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">售后类型</p>
              <p>{{ current.typeDesc }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">退款金额</p>
              <p class="text-lg font-bold text-error">¥{{ current.amount }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">当前状态</p>
              <UBadge :color="getStatusColor(current.status)">{{ current.statusDesc }}</UBadge>
            </div>
          </div>

          <!-- 退款原因 -->
          <div class="border-t pt-4">
            <p class="text-sm text-gray-500 mb-1">退款原因</p>
            <p class="text-sm bg-gray-50 rounded p-3">{{ current.reason || '未填写' }}</p>
          </div>

          <!-- 状态流程 -->
          <div class="border-t pt-4">
            <p class="text-sm text-gray-500 mb-3">处理流程</p>
            <div class="flex items-center gap-2">
              <div v-for="(step, i) in statusSteps" :key="i" class="flex items-center gap-2">
                <div :class="[
                  'w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold',
                  step.done ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-500'
                ]">
                  {{ step.done ? '✓' : i + 1 }}
                </div>
                <span :class="['text-xs', step.done ? 'text-green-600 font-medium' : 'text-gray-400']">
                  {{ step.label }}
                </span>
                <div v-if="i < statusSteps.length - 1" class="w-8 h-px bg-gray-300" />
              </div>
            </div>
          </div>

          <!-- 时间信息 -->
          <div class="border-t pt-4">
            <p class="text-sm text-gray-500 mb-1">申请时间</p>
            <p class="text-sm">{{ formatDate(current.created_at) }}</p>
          </div>
        </div>

        <div class="flex justify-end gap-2 pt-4 border-t mt-4">
          <template v-if="current.status === 0">
            <UButton color="success" @click="approve(current.id); showDetailModal = false">通过</UButton>
            <UButton color="error" variant="outline" @click="reject(current.id); showDetailModal = false">拒绝</UButton>
          </template>
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
const aftersales = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

const filters = reactive({
  status: undefined as number | undefined,
})

const statusOptions = [
  { label: '全部状态', value: undefined },
  { label: '待审核', value: 0 },
  { label: '审核通过', value: 1 },
  { label: '审核拒绝', value: 2 },
  { label: '退款中', value: 3 },
  { label: '退款成功', value: 4 },
  { label: '已取消', value: 5 },
]

const showDetailModal = ref(false)
const current = ref<any>(null)

const statusSteps = computed(() => {
  if (!current.value) return []
  const s = current.value.status
  return [
    { label: '提交申请', done: true },
    { label: '审核通过', done: s >= 1 },
    { label: '退款处理', done: s >= 3 },
    { label: '退款成功', done: s === 4 },
  ]
})

function getStatusColor(status: number) {
  const colorMap: Record<number, string> = {
    0: 'warning', 1: 'success', 2: 'error', 3: 'info', 4: 'success', 5: 'gray',
  }
  return colorMap[status] || 'gray'
}

function formatDate(date: string) {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 19)
}

async function loadAftersales() {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(currentPage.value),
      size: String(pageSize),
    })
    if (filters.status !== undefined) params.append('status', String(filters.status))
    const res = await api.get<any>(`/api/admin/aftersale/list?${params}`)
    if (res.code === 200) {
      aftersales.value = res.data.list
      total.value = res.data.total
    }
  } catch {}
  loading.value = false
}

async function viewDetail(item: any) {
  try {
    const res = await api.get<any>(`/api/admin/aftersale/${item.id}`)
    if (res.code === 200) {
      current.value = res.data
      showDetailModal.value = true
    }
  } catch {}
}

async function approve(id: number) {
  try {
    await api.put(`/api/admin/aftersale/${id}/approve`)
    toast.success('审批通过')
    await loadAftersales()
  } catch {}
}

async function reject(id: number) {
  try {
    await api.put(`/api/admin/aftersale/${id}/reject`)
    toast.success('已拒绝')
    await loadAftersales()
  } catch {}
}

onMounted(() => {
  if (route.query.status) filters.status = Number(route.query.status)
  loadAftersales()
})
</script>
