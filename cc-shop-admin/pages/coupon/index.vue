<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">优惠券管理</h1>
      <UButton @click="showAddModal = true">
        <UIcon name="i-lucide-plus" class="mr-1" />
        新增优惠券
      </UButton>
    </div>

    <!-- 优惠券列表 -->
    <div class="bg-white rounded-lg shadow-sm">
      <div v-if="loading" class="flex justify-center py-10">
        <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
      </div>

      <table v-else class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">ID</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">名称</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">类型</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">面值/折扣</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">门槛</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">已领/总量</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">有效期</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in coupons" :key="item.id" class="border-t border-gray-100">
            <td class="px-4 py-3 text-sm">{{ item.id }}</td>
            <td class="px-4 py-3 text-sm font-medium">{{ item.name }}</td>
            <td class="px-4 py-3">
              <UBadge :color="item.type === 0 ? 'primary' : 'warning'" size="sm">
                {{ item.typeDesc }}
              </UBadge>
            </td>
            <td class="px-4 py-3 text-sm">
              {{ item.type === 0 ? `¥${item.value}` : `${(item.value * 10).toFixed(1)}折` }}
            </td>
            <td class="px-4 py-3 text-sm text-gray-500">满¥{{ item.min_order_amount }}</td>
            <td class="px-4 py-3 text-sm">
              {{ item.total_count - item.remaining_count }}/{{ item.total_count }}
            </td>
            <td class="px-4 py-3 text-sm text-gray-500">
              {{ formatDate(item.start_time) }} ~ {{ formatDate(item.end_time) }}
            </td>
            <td class="px-4 py-3">
              <UButton size="xs" variant="outline" @click="viewClaims(item)">领取明细</UButton>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && !coupons.length" class="text-center py-10 text-gray-500">
        暂无优惠券
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center py-4 border-t border-gray-100">
        <UPagination
          v-model:page="currentPage"
          :total="total"
          :page-size="pageSize"
          @update:page="loadCoupons"
        />
      </div>
    </div>

    <!-- 新增弹窗 -->
    <UModal v-model="showAddModal">
      <div class="p-6">
        <h2 class="text-lg font-semibold mb-4">新增优惠券</h2>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">优惠券名称</label>
            <UInput v-model="form.name" placeholder="如：满100减10" required />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">类型</label>
              <USelect
                v-model="form.type"
                :options="[
                  { label: '满减券', value: 0 },
                  { label: '折扣券', value: 1 },
                ]"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                {{ form.type === 0 ? '减免金额' : '折扣率(如0.85=8.5折)' }}
              </label>
              <UInput v-model="form.value" type="number" step="0.01" required />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">最低消费门槛</label>
              <UInput v-model="form.minOrderAmount" type="number" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">发行总量</label>
              <UInput v-model="form.total" type="number" required />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">开始时间</label>
              <UInput v-model="form.startTime" type="datetime-local" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">结束时间</label>
              <UInput v-model="form.endTime" type="datetime-local" required />
            </div>
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <UButton variant="outline" @click="showAddModal = false">取消</UButton>
            <UButton type="submit" :loading="submitting">保存</UButton>
          </div>
        </form>
      </div>
    </UModal>

    <!-- 领取明细弹窗 -->
    <UModal v-model="showClaimsModal">
      <div class="p-6 max-h-[80vh] overflow-y-auto min-w-[600px]">
        <h2 class="text-lg font-semibold mb-4">
          "{{ currentCoupon?.name }}" 领取明细
        </h2>

        <!-- 统计 -->
        <div class="grid grid-cols-3 gap-4 mb-4">
          <div class="bg-blue-50 rounded-lg p-3 text-center">
            <p class="text-2xl font-bold text-blue-600">{{ claimStats.total }}</p>
            <p class="text-xs text-gray-500">总领取</p>
          </div>
          <div class="bg-green-50 rounded-lg p-3 text-center">
            <p class="text-2xl font-bold text-green-600">{{ claimStats.used }}</p>
            <p class="text-xs text-gray-500">已使用</p>
          </div>
          <div class="bg-orange-50 rounded-lg p-3 text-center">
            <p class="text-2xl font-bold text-orange-600">{{ claimStats.unused }}</p>
            <p class="text-xs text-gray-500">未使用</p>
          </div>
        </div>

        <!-- 筛选 -->
        <div class="flex gap-2 mb-3">
          <UButton :color="claimFilter === undefined ? 'primary' : 'gray'" size="xs" variant="outline" @click="claimFilter = undefined; loadClaims()">全部</UButton>
          <UButton :color="claimFilter === 0 ? 'primary' : 'gray'" size="xs" variant="outline" @click="claimFilter = 0; loadClaims()">未使用</UButton>
          <UButton :color="claimFilter === 1 ? 'primary' : 'gray'" size="xs" variant="outline" @click="claimFilter = 1; loadClaims()">已使用</UButton>
        </div>

        <!-- 领取列表 -->
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-3 py-2 text-left text-gray-600">用户</th>
              <th class="px-3 py-2 text-left text-gray-600">状态</th>
              <th class="px-3 py-2 text-left text-gray-600">使用订单</th>
              <th class="px-3 py-2 text-left text-gray-600">领取时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in claims" :key="c.id" class="border-t border-gray-100">
              <td class="px-3 py-2">{{ c.username || `用户#${c.user_id}` }}</td>
              <td class="px-3 py-2">
                <UBadge :color="c.status === 0 ? 'success' : c.status === 1 ? 'gray' : 'warning'" size="sm">
                  {{ c.statusDesc }}
                </UBadge>
              </td>
              <td class="px-3 py-2 font-mono text-xs">{{ c.used_order_id || '-' }}</td>
              <td class="px-3 py-2 text-gray-500">{{ formatDate(c.created_at) }}</td>
            </tr>
          </tbody>
        </table>

        <div v-if="!claims.length" class="text-center py-6 text-gray-400">暂无领取记录</div>

        <!-- 分页 -->
        <div v-if="claimTotal > 20" class="flex justify-center mt-3">
          <UPagination v-model:page="claimPage" :total="claimTotal" :page-size="20" @update:page="loadClaims" />
        </div>

        <div class="flex justify-end pt-4 border-t mt-4">
          <UButton variant="outline" @click="showClaimsModal = false">关闭</UButton>
        </div>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()

const loading = ref(true)
const coupons = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

const showAddModal = ref(false)
const submitting = ref(false)

const form = reactive({
  name: '',
  type: 0,
  value: 0,
  minOrderAmount: 0,
  total: 100,
  startTime: '',
  endTime: '',
})

// 领取明细
const showClaimsModal = ref(false)
const currentCoupon = ref<any>(null)
const claims = ref<any[]>([])
const claimTotal = ref(0)
const claimPage = ref(1)
const claimFilter = ref<number | undefined>(undefined)

const claimStats = ref({ total: 0, used: 0, unused: 0 })

function formatDate(date: string) {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

async function loadCoupons() {
  loading.value = true
  try {
    const res = await api.get<any>(`/api/admin/coupon/list?page=${currentPage.value}&size=${pageSize}`)
    if (res.code === 200) {
      coupons.value = res.data.list
      total.value = res.data.total
    }
  } catch {}
  loading.value = false
}

async function viewClaims(item: any) {
  currentCoupon.value = item
  claimPage.value = 1
  claimFilter.value = undefined
  await loadClaims()
  showClaimsModal.value = true
}

async function loadClaims() {
  if (!currentCoupon.value) return
  try {
    const params = new URLSearchParams({
      page: String(claimPage.value),
      size: '20',
    })
    if (claimFilter.value !== undefined) params.append('status', String(claimFilter.value))
    const res = await api.get<any>(`/api/admin/coupon/${currentCoupon.value.id}/claims?${params}`)
    if (res.code === 200) {
      claims.value = res.data.list
      claimTotal.value = res.data.total
      if (res.data.stats) {
        claimStats.value = res.data.stats
      }
    }
  } catch {}
}

async function handleSubmit() {
  if (!form.name) {
    toast.warning('请输入优惠券名称')
    return
  }

  submitting.value = true
  try {
    await api.post('/api/admin/coupon', {
      name: form.name,
      type: form.type,
      value: form.value,
      minOrderAmount: form.minOrderAmount,
      total: form.total,
      startTime: form.startTime.replace('T', ' ') + ':00',
      endTime: form.endTime.replace('T', ' ') + ':00',
    })

    toast.success('新增成功')
    showAddModal.value = false
    form.name = ''
    form.value = 0
    form.minOrderAmount = 0
    form.total = 100
    form.startTime = ''
    form.endTime = ''
    await loadCoupons()
  } catch {}
  submitting.value = false
}

onMounted(() => {
  loadCoupons()
})
</script>
