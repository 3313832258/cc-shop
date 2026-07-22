<template>
  <NuxtLink :to="`/product/${product.id}`" class="group block bg-elevated rounded-lg shadow-sm overflow-hidden hover:shadow-lg transition-all duration-200 hover:-translate-y-1">
    <div class="aspect-square bg-muted overflow-hidden">
      <img
        :src="product.image || 'https://picsum.photos/seed/placeholder/400/400'"
        :alt="product.name"
        loading="lazy"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
    </div>
    <div class="p-4">
      <h3 class="text-sm font-medium leading-snug line-clamp-2 text-default mb-2">
        {{ product.name }}
      </h3>
      <div class="flex justify-between items-baseline">
        <span class="text-lg font-bold text-warning">¥{{ formatPrice(product.price) }}</span>
        <span class="text-xs text-dimmed">库存 {{ product.stock }}</span>
      </div>
    </div>
  </NuxtLink>
</template>

<script setup lang="ts">
defineProps<{
  product: {
    id: number
    name: string
    image?: string
    price?: number
    stock?: number
  }
}>()

function formatPrice(price?: number): string {
  if (!price) return '0.00'
  return Number(price).toFixed(2)
}
</script>
