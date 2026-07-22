<template>
  <div class="max-w-7xl mx-auto px-5">
    <h2 class="text-xl font-bold text-default mb-6">购物车</h2>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <div v-else-if="!cartItems.length" class="flex flex-col items-center justify-center py-16">
      <span class="text-5xl mb-4">🛒</span>
      <p class="text-dimmed mb-4">购物车是空的</p>
      <UButton to="/product/list">去逛逛</UButton>
    </div>

    <div v-else>
      <!-- 购物车列表 -->
      <div class="flex flex-col gap-3">
        <div v-for="item in cartItems" :key="item.skuId" class="flex items-center gap-4 p-4 bg-elevated rounded-lg shadow-sm">
          <input
            type="checkbox"
            class="w-[18px] h-[18px] accent-primary shrink-0 cursor-pointer"
            :checked="item.selected"
            @change="toggleSelect(item)"
          >
          <img
            :src="item.productImage || 'https://picsum.photos/seed/placeholder/120/120'"
            :alt="item.productName"
            class="w-20 h-20 object-cover rounded-lg bg-muted shrink-0"
          >
          <div class="flex-1 min-w-0">
            <NuxtLink :to="'/product/' + item.productId" class="text-sm font-semibold text-default line-clamp-2 hover:text-primary no-underline">
              {{ item.productName }}
            </NuxtLink>
            <p v-if="item.specs" class="text-xs text-dimmed mt-1">{{ formatSpecs(item.specs) }}</p>
            <span class="text-sm font-semibold text-error mt-1 inline-block">¥{{ item.price }}</span>
          </div>
          <!-- 数量步进器 -->
          <div class="flex items-center border border-default rounded-md overflow-hidden shrink-0">
            <button
              class="w-8 h-8 bg-muted text-default flex items-center justify-center hover:bg-accented disabled:opacity-30 disabled:cursor-not-allowed"
              :disabled="item.quantity <= 1"
              @click="changeQty(item, -1)"
            >−</button>
            <span class="w-10 h-8 flex items-center justify-center text-sm font-semibold border-x border-default">{{ item.quantity }}</span>
            <button
              class="w-8 h-8 bg-muted text-default flex items-center justify-center hover:bg-accented"
              @click="changeQty(item, 1)"
            >+</button>
          </div>
          <span class="text-base font-bold text-default min-w-[80px] text-right shrink-0">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </span>
          <UButton variant="ghost" size="sm" color="neutral" @click="removeItem(item)">删除</UButton>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="sticky bottom-4 mt-6 p-4 bg-elevated rounded-lg shadow-lg flex items-center justify-between gap-6">
        <label class="flex items-center gap-2 text-sm cursor-pointer shrink-0">
          <input type="checkbox" :checked="allSelected" class="w-[18px] h-[18px] accent-primary" @change="toggleAll">
          全选
        </label>
        <div class="flex items-center gap-6">
          <span class="text-sm text-muted">
            已选 <strong class="text-default">{{ selectedCount }}</strong> 件
          </span>
          <span class="text-sm">
            合计：<strong class="text-xl text-error">¥{{ selectedTotal.toFixed(2) }}</strong>
          </span>
        </div>
        <UButton
          size="lg"
          color="primary"
          :disabled="selectedCount === 0"
          @click="checkout"
        >
          去结算({{ selectedCount }})
        </UButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()

interface CartItem {
  skuId: number
  productId: number
  productName: string
  productImage: string
  specs: Record<string, string> | null
  price: number
  originalPrice: number | null
  quantity: number
  selected: boolean
}

const cartItems = ref<CartItem[]>([])
const loading = ref(false)

const selectedCount = computed(() => cartItems.value.filter(i => i.selected).length)
const selectedTotal = computed(() =>
  cartItems.value
    .filter(i => i.selected)
    .reduce((sum, i) => sum + i.price * i.quantity, 0)
)
const allSelected = computed(() =>
  cartItems.value.length > 0 && cartItems.value.every(i => i.selected)
)

function formatSpecs(specs: Record<string, string> | null): string {
  if (!specs) return ''
  return Object.entries(specs).map(([k, v]) => `${k}: ${v}`).join('，')
}

async function loadCart() {
  loading.value = true
  try {
    const res = await api.get<any>('/api/trade/cart/list')
    if (res.code === 200) {
      cartItems.value = res.data || []
    }
  } catch {}
  loading.value = false
}

async function changeQty(item: CartItem, delta: number) {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    const res = await api.put<any>('/api/trade/cart/update', { skuId: item.skuId, quantity: newQty })
    if (res.code === 200) {
      item.quantity = newQty
    }
  } catch {}
}

async function removeItem(item: CartItem) {
  try {
    const res = await api.del<any>('/api/trade/cart/remove/' + item.skuId)
    if (res.code === 200) {
      cartItems.value = cartItems.value.filter(i => i.skuId !== item.skuId)
      toast.success('已删除')
    }
  } catch {}
}

async function toggleSelect(item: CartItem) {
  const newVal = !item.selected
  try {
    const res = await api.put<any>('/api/trade/cart/select', { skuId: item.skuId, selected: newVal })
    if (res.code === 200) {
      item.selected = newVal
    }
  } catch {}
}

async function toggleAll() {
  const newVal = !allSelected.value
  for (const item of cartItems.value) {
    if (item.selected !== newVal) {
      try {
        await api.put<any>('/api/trade/cart/select', { skuId: item.skuId, selected: newVal })
        item.selected = newVal
      } catch {}
    }
  }
}

function checkout() {
  if (selectedCount.value === 0) {
    toast.error('请先选择商品')
    return
  }
  navigateTo('/order/checkout')
}

onMounted(() => {
  loadCart()
})
</script>
