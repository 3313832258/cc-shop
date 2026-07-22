<template>
  <div class="max-w-3xl mx-auto px-5">
    <div class="flex items-center gap-4 mb-6">
      <UButton variant="ghost" icon="i-lucide-arrow-left" @click="navigateTo('/order/' + orderId)" />
      <h1 class="text-xl font-bold text-default">物流信息</h1>
    </div>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <template v-else-if="logistics">
      <!-- 物流概况 -->
      <div class="bg-elevated rounded-lg shadow-sm p-5 mb-6">
        <div class="flex justify-between items-start mb-4">
          <div>
            <p class="text-sm text-muted">承运商</p>
            <p class="font-medium text-default">{{ logistics.carrier }}</p>
          </div>
          <div>
            <p class="text-sm text-muted">运单号</p>
            <p class="font-medium text-default">{{ logistics.trackingNo }}</p>
          </div>
        </div>
        <div class="flex justify-between items-center">
          <UBadge :color="getStatusColor(logistics.status)" size="lg">
            {{ logistics.statusText }}
          </UBadge>
          <p v-if="logistics.estimatedDelivery" class="text-sm text-dimmed">
            预计 {{ formatDate(logistics.estimatedDelivery) }} 送达
          </p>
        </div>
      </div>

      <!-- 物流时间线 -->
      <div class="bg-elevated rounded-lg shadow-sm p-5">
        <h2 class="font-semibold text-default mb-4">物流动态</h2>
        <div v-if="!logistics.steps?.length" class="text-center py-6">
          <p class="text-dimmed">暂无物流信息</p>
        </div>
        <div v-else class="relative">
          <div class="absolute left-4 top-0 bottom-0 w-0.5 bg-default/10"></div>
          <div v-for="(step, index) in logistics.steps" :key="index" class="relative pl-10 pb-6 last:pb-0">
            <div
              class="absolute left-2.5 top-1 w-3 h-3 rounded-full border-2"
              :class="index === 0 ? 'bg-primary border-primary' : 'bg-default/20 border-default/20'"
            ></div>
            <div>
              <p class="font-medium text-default">{{ step.description }}</p>
              <p class="text-sm text-muted">{{ step.location }}</p>
              <p class="text-xs text-dimmed mt-1">{{ formatDateTime(step.timestamp) }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="text-center py-16">
      <span class="text-5xl mb-4">📦</span>
      <p class="text-dimmed">暂无物流信息</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const api = useApi()

const orderId = computed(() => Number(route.query.orderId))
const logistics = ref<any>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get<any>(`/api/trade/logistics/${orderId.value}`)
    if (res.code === 200) {
      logistics.value = res.data
    }
  } catch {}
  loading.value = false
})

function getStatusColor(status: number) {
  const colors: Record<number, string> = {
    0: 'neutral', 1: 'primary', 2: 'warning', 3: 'warning', 4: 'success'
  }
  return colors[status] || 'neutral'
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.split('T')[0] || dateStr.split(' ')[0] || dateStr
}

function formatDateTime(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}
</script>
