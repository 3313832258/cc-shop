<template>
  <div class="sku-selector">
    <!-- 每个规格维度 -->
    <div v-for="(options, specKey) in specOptions" :key="specKey" class="spec-group">
      <span class="spec-label">{{ specKey }}：</span>
      <div class="spec-options">
        <button
          v-for="opt in options"
          :key="opt"
          :class="[
            'spec-btn',
            selectedSpecs[specKey] === opt ? 'active' : '',
            isDisabled(specKey, opt) ? 'disabled' : ''
          ]"
          :disabled="isDisabled(specKey, opt)"
          @click="selectSpec(specKey, opt)"
        >
          {{ opt }}
        </button>
      </div>
    </div>

    <!-- 选中结果 -->
    <div v-if="selectedSku" class="sku-result mt-4">
      <span class="sku-price">¥{{ Number(selectedSku.price).toFixed(2) }}</span>
      <span v-if="selectedSku.originalPrice" class="sku-original-price">
        ¥{{ Number(selectedSku.originalPrice).toFixed(2) }}
      </span>
      <span :class="['sku-stock', selectedSku.stock > 0 ? 'in-stock' : 'out-stock']">
        {{ selectedSku.stock > 0 ? `库存 ${selectedSku.stock} 件` : '暂时缺货' }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Sku {
  id: number
  price: number
  originalPrice?: number
  stock: number
  skuCode: string
  image?: string
  specs: Record<string, string>
}

const props = defineProps<{
  skus: Sku[]
  specOptions: Record<string, string[]>
}>()

const emit = defineEmits<{
  (e: 'change', sku: Sku | null): void
}>()

// 当前选中的规格
const selectedSpecs = reactive<Record<string, string>>({})

// 初始化：默认选中每个规格的第一个
onMounted(() => {
  for (const [key, options] of Object.entries(props.specOptions)) {
    if (options.length > 0) {
      selectedSpecs[key] = options[0]
    }
  }
  emitChange()
})

// 选择规格
function selectSpec(key: string, value: string) {
  if (selectedSpecs[key] === value) return
  selectedSpecs[key] = value
  emitChange()
}

// 判断某规格值是否可选（无对应 SKU 或库存为0）
function isDisabled(key: string, value: string): boolean {
  const testSpecs = { ...selectedSpecs, [key]: value }
  const match = findMatchingSku(testSpecs)
  return !match || match.stock <= 0
}

// 根据选中的规格匹配 SKU
function findMatchingSku(specs: Record<string, string>): Sku | undefined {
  return props.skus.find(sku => {
    if (!sku.specs) return false
    for (const [k, v] of Object.entries(specs)) {
      if (sku.specs[k] !== v) return false
    }
    // 确保规格维度数量一致
    return Object.keys(sku.specs).length === Object.keys(specs).length
  })
}

const selectedSku = computed(() => findMatchingSku(selectedSpecs))

function emitChange() {
  emit('change', selectedSku.value || null)
}

// 监听 selectedSku 变化
watch(selectedSku, () => emitChange())
</script>

<style scoped>
.spec-group {
  margin-bottom: 16px;
}

.spec-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--text);
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-btn {
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text);
}

.spec-btn:hover:not(.disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.spec-btn.active {
  border-color: var(--primary);
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 500;
}

.spec-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  text-decoration: line-through;
}

.sku-result {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.sku-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
}

.sku-original-price {
  font-size: 16px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.sku-stock {
  font-size: 13px;
}

.in-stock { color: var(--success); }
.out-stock { color: var(--danger); }
</style>
