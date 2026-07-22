<template>
  <div class="max-w-3xl mx-auto px-5">
    <div class="flex items-center gap-4 mb-6">
      <UButton variant="ghost" icon="i-lucide-arrow-left" @click="navigateTo('/order/' + orderId)" />
      <h1 class="text-xl font-bold text-default">申请售后</h1>
    </div>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else>
      <!-- 订单商品 -->
      <div v-if="orderItem" class="bg-elevated rounded-lg shadow-sm p-5 mb-6">
        <h2 class="font-semibold text-default mb-3">申请商品</h2>
        <div class="flex gap-4">
          <img
            :src="orderItem.productImage || 'https://picsum.photos/seed/placeholder/100/100'"
            class="w-20 h-20 object-cover rounded-lg"
          />
          <div>
            <p class="font-medium text-default">{{ orderItem.productName }}</p>
            <p class="text-sm text-muted">¥{{ orderItem.price }} × {{ orderItem.quantity }}</p>
          </div>
        </div>
      </div>

      <!-- 售后表单 -->
      <div class="bg-elevated rounded-lg shadow-sm p-5">
        <h2 class="font-semibold text-default mb-4">售后信息</h2>
        <form @submit.prevent="handleSubmit" class="flex flex-col gap-4">
          <div>
            <label class="block text-sm font-medium text-muted mb-1.5">售后类型</label>
            <USelect v-model="form.type" :items="typeOptions" />
          </div>

          <div>
            <label class="block text-sm font-medium text-muted mb-1.5">申请原因</label>
            <UTextarea v-model="form.reason" placeholder="请描述售后原因" :rows="4" required />
          </div>

          <UButton type="submit" block size="lg" color="primary" :loading="submitting">
            提交申请
          </UButton>
        </form>
      </div>

      <!-- 历史售后记录 -->
      <div v-if="aftersales.length" class="mt-6">
        <h2 class="font-semibold text-default mb-4">售后记录</h2>
        <div class="flex flex-col gap-3">
          <div
            v-for="a in aftersales"
            :key="a.id"
            class="bg-elevated rounded-lg shadow-sm p-4"
          >
            <div class="flex justify-between items-start mb-2">
              <div>
                <UBadge :color="getStatusColor(a.status)" size="sm">{{ a.statusText }}</UBadge>
                <span class="ml-2 text-sm text-muted">{{ a.typeText }}</span>
              </div>
              <span class="text-xs text-dimmed">{{ formatDateTime(a.createdAt) }}</span>
            </div>
            <p class="text-sm text-default">{{ a.reason }}</p>
            <p class="text-sm text-muted mt-1">退款金额：¥{{ a.amount }}</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const api = useApi()
const toast = useAppToast()

const orderId = computed(() => Number(route.query.orderId))
const orderItemId = computed(() => Number(route.query.orderItemId))

const orderItem = ref<any>(null)
const aftersales = ref<any[]>([])
const loading = ref(true)
const submitting = ref(false)

const form = reactive({
  type: 'refund',
  reason: '',
})

const typeOptions = [
  { label: '仅退款', value: 'refund' },
  { label: '退货退款', value: 'return_refund' },
]

onMounted(async () => {
  try {
    // 获取订单项信息
    const orderRes = await api.get<any>(`/api/trade/order/${orderId.value}`)
    if (orderRes.code === 200 && orderRes.data?.items) {
      orderItem.value = orderRes.data.items.find((i: any) => i.id === orderItemId.value)
    }

    // 获取售后记录
    const aftersaleRes = await api.get<any>('/api/trade/aftersale/list', { orderId: orderId.value })
    if (aftersaleRes.code === 200) {
      aftersales.value = aftersaleRes.data || []
    }
  } catch {}
  loading.value = false
})

async function handleSubmit() {
  if (!form.reason.trim()) {
    toast.error('请填写售后原因')
    return
  }

  submitting.value = true
  try {
    const res = await api.post<any>('/api/trade/aftersale/apply', {
      orderId: orderId.value,
      orderItemId: orderItemId.value,
      type: form.type,
      reason: form.reason,
    })
    if (res.code === 200) {
      toast.success('售后申请提交成功')
      navigateTo('/order/' + orderId.value)
    }
  } catch {}
  submitting.value = false
}

function getStatusColor(status: number) {
  const colors: Record<number, string> = {
    0: 'warning', 1: 'success', 2: 'error', 3: 'primary', 4: 'success', 5: 'neutral'
  }
  return colors[status] || 'neutral'
}

function formatDateTime(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}
</script>
