<template>
  <div
    class="flex rounded-xl overflow-hidden min-h-[120px] shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200"
    :class="mode === 'my' && coupon.status !== 0 ? 'opacity-60' : ''"
  >
    <!-- 左侧金额 -->
    <div
      class="w-[100px] min-w-[100px] flex flex-col items-center justify-center p-4 text-inverted"
      :class="coupon.type === 0 ? 'bg-gradient-to-br from-primary to-primary-700' : 'bg-gradient-to-br from-warning to-warning-700'"
    >
      <template v-if="coupon.type === 0">
        <span class="text-2xl font-extrabold leading-none">¥{{ coupon.value }}</span>
        <span class="text-xs mt-1 opacity-90">满减券</span>
      </template>
      <template v-else>
        <span class="text-2xl font-extrabold leading-none">{{ formatDiscount(coupon.value) }}</span>
        <span class="text-xs mt-1 opacity-90">折扣券</span>
      </template>
    </div>

    <!-- 右侧信息 -->
    <div class="flex-1 bg-elevated border border-l-0 border-default rounded-r-xl p-4 flex flex-col gap-1.5">
      <h4 class="text-[15px] font-semibold text-default">{{ coupon.name }}</h4>
      <p class="text-xs text-muted">满 ¥{{ coupon.minOrderAmount }} 可用</p>
      <p v-if="coupon.endTime" class="text-xs text-dimmed">
        有效期至 {{ formatDate(coupon.endTime) }}
      </p>
      <div class="mt-auto">
        <template v-if="mode === 'available'">
          <UButton v-if="coupon.received" size="sm" color="neutral" variant="outline" disabled>
            已领取
          </UButton>
          <UButton v-else size="sm" color="primary" @click="$emit('receive', coupon.id)">
            立即领取
          </UButton>
        </template>
        <template v-else>
          <UBadge
            :color="coupon.status === 1 ? 'success' : coupon.status === 2 ? 'neutral' : 'primary'"
            variant="subtle"
            size="sm"
          >
            {{ coupon.status === 1 ? '已使用' : coupon.status === 2 ? '已过期' : '可使用' }}
          </UBadge>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  coupon: {
    id: number
    couponId?: number
    name: string
    type: number   // 0=满减, 1=折扣
    value: number
    minOrderAmount: number
    endTime?: string
    status?: number  // 0=可用, 1=已用, 2=过期 (仅 my 模式)
  }
  mode: 'available' | 'my'
}>()

defineEmits<{
  receive: [couponId: number]
}>()

function formatDiscount(value: number): string {
  return (value * 10).toFixed(1) + '折'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.split('T')[0] || dateStr.split(' ')[0] || dateStr
}
</script>
