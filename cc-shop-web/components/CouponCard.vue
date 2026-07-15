<template>
  <div
    class="coupon-card"
    :class="[mode === 'available' ? 'coupon-available' : 'coupon-owned', typeClass]"
  >
    <div class="coupon-left">
      <template v-if="coupon.type === 0">
        <span class="coupon-value">¥{{ coupon.value }}</span>
        <span class="coupon-label">满减券</span>
      </template>
      <template v-else>
        <span class="coupon-value">{{ formatDiscount(coupon.value) }}</span>
        <span class="coupon-label">折扣券</span>
      </template>
    </div>
    <div class="coupon-right">
      <h4 class="coupon-name">{{ coupon.name }}</h4>
      <p class="coupon-condition">
        满 ¥{{ coupon.minOrderAmount }} 可用
      </p>
      <p v-if="coupon.endTime" class="coupon-date">
        有效期至 {{ formatDate(coupon.endTime) }}
      </p>
      <div class="coupon-action">
        <template v-if="mode === 'available'">
          <button class="btn btn-sm btn-primary" @click="$emit('receive', coupon.id)">
            立即领取
          </button>
        </template>
        <template v-else>
          <span class="tag" :class="statusClass">{{ statusText }}</span>
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
    status?: number  // 0=可用, 1=已用, 2=过期 (仅 mine 模式)
  }
  mode: 'available' | 'my'
}>()

defineEmits<{
  receive: [couponId: number]
}>()

const typeClass = computed(() => props.coupon.type === 0 ? 'type-fixed' : 'type-percent')

const statusText = computed(() => {
  if (props.coupon.status === 1) return '已使用'
  if (props.coupon.status === 2) return '已过期'
  return '可使用'
})

const statusClass = computed(() => {
  if (props.coupon.status === 1) return 'tag-success'
  if (props.coupon.status === 2) return ''
  return 'tag-primary'
})

function formatDiscount(value: number): string {
  return (value * 10).toFixed(1) + '折'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.split('T')[0] || dateStr.split(' ')[0] || dateStr
}
</script>

<style scoped>
.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  min-height: 120px;
  box-shadow: var(--shadow);
  transition: transform 0.2s;
}

.coupon-card:hover {
  transform: translateY(-2px);
}

.coupon-left {
  width: 100px;
  min-width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  color: #fff;
}

.type-fixed .coupon-left {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
}

.type-percent .coupon-left {
  background: linear-gradient(135deg, var(--accent), #e56000);
}

.coupon-value {
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}

.coupon-label {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.9;
}

.coupon-right {
  flex: 1;
  background: var(--surface);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: 1px solid var(--border);
  border-left: none;
  border-radius: 0 12px 12px 0;
}

.coupon-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.coupon-condition {
  font-size: 13px;
  color: var(--text-secondary);
}

.coupon-date {
  font-size: 12px;
  color: var(--text-secondary);
}

.coupon-action {
  margin-top: auto;
  display: flex;
}

.coupon-owned .coupon-left {
  opacity: 0.6;
}
</style>
