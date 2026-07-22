<template>
  <div>
    <!-- 每个规格维度 -->
    <div v-for="(options, specKey) in specOptions" :key="specKey" class="mb-4">
      <span class="block text-sm font-medium text-muted mb-2">{{ specKey }}：</span>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="opt in options"
          :key="opt"
          class="px-4 py-2 border rounded-md text-sm transition-all"
          :class="[
            selectedSpecs[specKey] === opt
              ? 'border-primary bg-primary/10 text-primary font-medium'
              : 'border-default bg-elevated text-default hover:border-primary/50 hover:text-primary',
            isDisabled(specKey, opt) ? 'opacity-40 cursor-not-allowed line-through' : 'cursor-pointer',
          ]"
          :disabled="isDisabled(specKey, opt)"
          @click="selectSpec(specKey, opt)"
        >
          {{ opt }}
        </button>
      </div>
    </div>

    <!-- 选中结果 -->
    <div v-if="selectedSku" class="flex items-baseline gap-3 mt-4">
      <span class="text-[28px] font-bold text-warning">¥{{ Number(selectedSku.price).toFixed(2) }}</span>
      <span v-if="selectedSku.originalPrice" class="text-base text-dimmed line-through">
        ¥{{ Number(selectedSku.originalPrice).toFixed(2) }}
      </span>
      <span :class="['text-xs', selectedSku.stock > 0 ? 'text-success' : 'text-error']">
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

const selectedSpecs = reactive<Record<string, string>>({})

onMounted(() => {
  for (const [key, options] of Object.entries(props.specOptions)) {
    if (options.length > 0) {
      selectedSpecs[key] = options[0]
    }
  }
  emitChange()
})

function selectSpec(key: string, value: string) {
  if (selectedSpecs[key] === value) return
  selectedSpecs[key] = value
  emitChange()
}

function isDisabled(key: string, value: string): boolean {
  const testSpecs = { ...selectedSpecs, [key]: value }
  const match = findMatchingSku(testSpecs)
  return !match || match.stock <= 0
}

function findMatchingSku(specs: Record<string, string>): Sku | undefined {
  return props.skus.find(sku => {
    if (!sku.specs) return false
    for (const [k, v] of Object.entries(specs)) {
      if (sku.specs[k] !== v) return false
    }
    return Object.keys(sku.specs).length === Object.keys(specs).length
  })
}

const selectedSku = computed(() => findMatchingSku(selectedSpecs))

function emitChange() {
  emit('change', selectedSku.value || null)
}

watch(selectedSku, () => emitChange())
</script>
