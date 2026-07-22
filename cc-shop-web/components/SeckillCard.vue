<template>
  <div class="bg-elevated rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-200">
    <!-- 商品图片 -->
    <div class="aspect-square bg-muted overflow-hidden relative">
      <img
        :src="item.productImage || 'https://picsum.photos/seed/flash-' + item.id + '/400/400'"
        :alt="item.productName || '秒杀商品'"
        class="w-full h-full object-cover"
      />
      <div class="absolute top-2 left-2">
        <UBadge color="error" size="sm">秒杀</UBadge>
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="p-4">
      <h3 class="text-sm font-medium leading-snug line-clamp-2 text-default mb-2">
        {{ item.productName || '秒杀商品' }}
      </h3>

      <div class="flex items-baseline gap-2 mb-2">
        <span class="text-xl font-bold text-error">¥{{ item.flashPrice }}</span>
        <span class="text-sm text-dimmed line-through">¥{{ item.originalPrice }}</span>
      </div>

      <!-- 库存进度 -->
      <div class="mb-3">
        <div class="flex justify-between text-xs text-muted mb-1">
          <span>已抢 {{ soldPercent }}%</span>
          <span>剩余 {{ item.availableStock }}</span>
        </div>
        <div class="h-2 bg-default/10 rounded-full overflow-hidden">
          <div
            class="h-full bg-error rounded-full transition-all duration-300"
            :style="{ width: soldPercent + '%' }"
          ></div>
        </div>
      </div>

      <!-- 倒计时/按钮 -->
      <template v-if="item.activityStatus === 0">
        <div class="text-center">
          <p class="text-xs text-muted mb-2">距离开始还有</p>
          <div class="flex justify-center gap-1">
            <span class="bg-default/10 px-2 py-1 rounded text-sm font-mono">{{ countdown.hours }}</span>
            <span>:</span>
            <span class="bg-default/10 px-2 py-1 rounded text-sm font-mono">{{ countdown.minutes }}</span>
            <span>:</span>
            <span class="bg-default/10 px-2 py-1 rounded text-sm font-mono">{{ countdown.seconds }}</span>
          </div>
        </div>
      </template>

      <template v-else-if="item.activityStatus === 1">
        <UButton
          block
          color="error"
          :disabled="item.availableStock <= 0"
          :loading="buying"
          @click="$emit('buy', item.id)"
        >
          {{ item.availableStock <= 0 ? '已抢光' : '立即抢购' }}
        </UButton>
      </template>

      <template v-else>
        <UButton block color="neutral" disabled>
          活动已结束
        </UButton>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  item: {
    id: number
    productName?: string
    productImage?: string
    originalPrice?: number
    flashPrice: number
    totalStock: number
    availableStock: number
    activityStatus: number
    startTime?: string
    endTime?: string
  }
  buying?: boolean
}>()

defineEmits<{
  buy: [itemId: number]
}>()

const countdown = ref({ hours: '00', minutes: '00', seconds: '00' })
let timer: ReturnType<typeof setInterval> | null = null

const soldPercent = computed(() => {
  if (!props.item.totalStock) return 0
  return Math.round(((props.item.totalStock - props.item.availableStock) / props.item.totalStock) * 100)
})

onMounted(() => {
  if (props.item.activityStatus === 0 && props.item.startTime) {
    startCountdown()
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function startCountdown() {
  updateCountdown()
  timer = setInterval(updateCountdown, 1000)
}

function updateCountdown() {
  if (!props.item.startTime) return

  const now = Date.now()
  const start = new Date(props.item.startTime).getTime()
  const diff = start - now

  if (diff <= 0) {
    if (timer) clearInterval(timer)
    countdown.value = { hours: '00', minutes: '00', seconds: '00' }
    return
  }

  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)

  countdown.value = {
    hours: String(hours).padStart(2, '0'),
    minutes: String(minutes).padStart(2, '0'),
    seconds: String(seconds).padStart(2, '0'),
  }
}
</script>
