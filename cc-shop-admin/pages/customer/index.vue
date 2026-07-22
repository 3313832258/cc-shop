<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">客户管理</h1>

    <!-- 搜索 -->
    <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
      <div class="flex gap-4">
        <UInput
          v-model="keyword"
          placeholder="搜索用户名/手机号/邮箱..."
          icon="i-lucide-search"
          class="w-72"
          @keyup.enter="loadCustomers"
        />
        <UButton @click="loadCustomers">搜索</UButton>
        <UButton variant="ghost" color="gray" @click="keyword = ''; loadCustomers()">重置</UButton>
      </div>
    </div>

    <!-- 客户列表 -->
    <div class="bg-white rounded-lg shadow-sm">
      <div v-if="loading" class="flex justify-center py-10">
        <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
      </div>

      <table v-else class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">ID</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">用户名</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">手机号</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">邮箱</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">订单数</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">累计消费</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">注册时间</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in customers" :key="item.id" class="border-t border-gray-100">
            <td class="px-4 py-3 text-sm">{{ item.id }}</td>
            <td class="px-4 py-3 text-sm font-medium">{{ item.username }}</td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ item.phone || '-' }}</td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ item.email || '-' }}</td>
            <td class="px-4 py-3 text-sm">{{ item.orderCount }}</td>
            <td class="px-4 py-3 text-sm font-medium">¥{{ formatAmount(item.totalSpent) }}</td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ formatDate(item.created_at) }}</td>
            <td class="px-4 py-3">
              <UButton size="xs" variant="outline" @click="viewDetail(item.id)">详情</UButton>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && !customers.length" class="text-center py-10 text-gray-500">
        暂无客户
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center py-4 border-t border-gray-100">
        <UPagination
          v-model:page="currentPage"
          :total="total"
          :page-size="pageSize"
          @update:page="loadCustomers"
        />
      </div>
    </div>

    <!-- 客户详情弹窗 -->
    <UModal v-model="showDetailModal">
      <div v-if="current" class="p-6 max-h-[80vh] overflow-y-auto min-w-[600px]">
        <h2 class="text-lg font-semibold mb-4">客户详情</h2>

        <!-- 基本信息 -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <p class="text-sm text-gray-500">用户名</p>
            <p class="font-medium">{{ current.username }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">手机号</p>
            <p>{{ current.phone || '未绑定' }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">邮箱</p>
            <p>{{ current.email || '未绑定' }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">注册时间</p>
            <p>{{ formatDate(current.created_at) }}</p>
          </div>
        </div>

        <!-- 消费统计 -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div class="bg-blue-50 rounded-lg p-4 text-center">
            <p class="text-2xl font-bold text-blue-600">{{ current.orderCount }}</p>
            <p class="text-xs text-gray-500">总订单数</p>
          </div>
          <div class="bg-green-50 rounded-lg p-4 text-center">
            <p class="text-2xl font-bold text-green-600">¥{{ formatAmount(current.totalSpent) }}</p>
            <p class="text-xs text-gray-500">累计消费</p>
          </div>
        </div>

        <!-- 收货地址 -->
        <div v-if="current.addresses?.length" class="border-t pt-4 mb-4">
          <p class="text-sm text-gray-500 mb-2">收货地址</p>
          <div v-for="(addr, i) in current.addresses" :key="i" class="text-sm bg-gray-50 rounded p-2 mb-1">
            <span v-if="addr.is_default" class="text-xs bg-primary text-white px-1.5 py-0.5 rounded mr-2">默认</span>
            {{ addr.receiver_name }} {{ addr.phone }} — {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}
          </div>
        </div>

        <!-- 最近订单 -->
        <div class="border-t pt-4">
          <p class="text-sm text-gray-500 mb-2">最近订单</p>
          <table class="w-full text-sm" v-if="current.recentOrders?.length">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-3 py-2 text-left text-gray-600">订单号</th>
                <th class="px-3 py-2 text-left text-gray-600">金额</th>
                <th class="px-3 py-2 text-left text-gray-600">状态</th>
                <th class="px-3 py-2 text-left text-gray-600">时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="o in current.recentOrders" :key="o.id" class="border-t border-gray-100">
                <td class="px-3 py-2 font-mono text-xs">{{ o.order_no }}</td>
                <td class="px-3 py-2">¥{{ o.final_amount }}</td>
                <td class="px-3 py-2">
                  <UBadge :color="orderStatusColor(o.status)" size="sm">{{ orderStatusText(o.status) }}</UBadge>
                </td>
                <td class="px-3 py-2 text-gray-500">{{ formatDate(o.created_at) }}</td>
              </tr>
            </tbody>
          </table>
          <p v-else class="text-sm text-gray-400">暂无订单</p>
        </div>

        <div class="flex justify-end pt-4 border-t mt-4">
          <UButton variant="outline" @click="showDetailModal = false">关闭</UButton>
        </div>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()

const loading = ref(true)
const customers = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20
const keyword = ref('')

const showDetailModal = ref(false)
const current = ref<any>(null)

function formatAmount(amount: any): string {
  return Number(amount || 0).toFixed(2)
}

function formatDate(date: string) {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 10)
}

const orderStatusMap: Record<number, string> = {
  0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消', 5: '已退款'
}
const orderColorMap: Record<number, string> = {
  0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'gray', 5: 'error'
}

function orderStatusText(s: number) { return orderStatusMap[s] || '未知' }
function orderStatusColor(s: number) { return orderColorMap[s] || 'gray' }

async function loadCustomers() {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(currentPage.value),
      size: String(pageSize),
    })
    if (keyword.value) params.append('keyword', keyword.value)
    const res = await api.get<any>(`/api/admin/customer/list?${params}`)
    if (res.code === 200) {
      customers.value = res.data.list
      total.value = res.data.total
    }
  } catch {}
  loading.value = false
}

async function viewDetail(id: number) {
  try {
    const res = await api.get<any>(`/api/admin/customer/${id}`)
    if (res.code === 200) {
      current.value = res.data
      showDetailModal.value = true
    }
  } catch {}
}

onMounted(() => {
  loadCustomers()
})
</script>
