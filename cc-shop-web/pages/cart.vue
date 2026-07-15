<template>
  <div class="cart-page container">
    <h2 class="page-title">购物车</h2>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="!cartItems.length" class="empty-state">
      <span style="font-size:48px">🛒</span>
      <p>购物车是空的</p>
      <router-link to="/product/list" class="btn btn-primary mt-4">去逛逛</router-link>
    </div>

    <div v-else>
      <!-- 购物车列表 -->
      <div class="cart-list">
        <div v-for="item in cartItems" :key="item.skuId" class="cart-item card">
          <input
            type="checkbox"
            class="cart-check"
            :checked="item.selected"
            @change="toggleSelect(item)"
          >
          <img
            :src="item.productImage || 'https://picsum.photos/seed/placeholder/120/120'"
            :alt="item.productName"
            class="cart-item-img"
          >
          <div class="cart-item-info">
            <router-link :to="'/product/' + item.productId" class="cart-item-name">
              {{ item.productName }}
            </router-link>
            <p v-if="item.specs" class="cart-item-specs">
              {{ formatSpecs(item.specs) }}
            </p>
            <span class="cart-item-price">¥{{ item.price }}</span>
          </div>
          <div class="qty-stepper">
            <button class="qty-btn" :disabled="item.quantity <= 1" @click="changeQty(item, -1)">−</button>
            <span class="qty-value">{{ item.quantity }}</span>
            <button class="qty-btn" @click="changeQty(item, 1)">+</button>
          </div>
          <span class="line-total">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          <button class="btn btn-ghost btn-sm remove-btn" @click="removeItem(item)">删除</button>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="cart-footer card">
        <label class="select-all">
          <input type="checkbox" :checked="allSelected" @change="toggleAll">
          全选
        </label>
        <div class="cart-summary">
          <span class="text-sm text-secondary">
            已选 <strong>{{ selectedCount }}</strong> 件
          </span>
          <span class="cart-total">
            合计：<strong class="total-price">¥{{ selectedTotal.toFixed(2) }}</strong>
          </span>
        </div>
        <button
          class="btn btn-primary btn-lg"
          :disabled="selectedCount === 0"
          @click="checkout"
        >
          去结算({{ selectedCount }})
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useToast()

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
  toast.info('下单功能将在阶段 2 实现')
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.cart-check {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: var(--primary);
  flex-shrink: 0;
}

.cart-item-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  background: var(--bg);
}

.cart-item-info {
  flex: 1;
  min-width: 0;
}

.cart-item-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cart-item-name:hover {
  color: var(--primary);
}

.cart-item-specs {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.cart-item-price {
  font-size: 15px;
  font-weight: 600;
  color: var(--danger);
  margin-top: 4px;
  display: inline-block;
}

.qty-stepper {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  flex-shrink: 0;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--bg);
  font-size: 16px;
  cursor: pointer;
  color: var(--text);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-sans);
}

.qty-btn:hover:not(:disabled) {
  background: #e0e0e0;
}

.qty-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.qty-value {
  width: 40px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
  height: 32px;
  line-height: 32px;
  display: inline-block;
}

.line-total {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  min-width: 80px;
  text-align: right;
  flex-shrink: 0;
}

.remove-btn {
  flex-shrink: 0;
}

.cart-footer {
  position: sticky;
  bottom: 16px;
  margin-top: 24px;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
  flex-shrink: 0;
}

.select-all input {
  width: 18px;
  height: 18px;
  accent-color: var(--primary);
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-price {
  font-size: 22px;
  color: var(--danger);
}

@media (max-width: 768px) {
  .cart-item {
    flex-wrap: wrap;
  }
  .line-total {
    min-width: auto;
  }
  .cart-footer {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
