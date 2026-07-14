<template>
  <router-link :to="`/product/${product.id}`" class="product-card card">
    <div class="product-image">
      <img :src="product.image || 'https://picsum.photos/seed/placeholder/400/400'" :alt="product.name" loading="lazy" />
    </div>
    <div class="product-info">
      <h3 class="product-name">{{ product.name }}</h3>
      <div class="product-meta">
        <span class="product-price">¥{{ formatPrice(product.price) }}</span>
        <span class="product-stock text-xs text-secondary">库存 {{ product.stock }}</span>
      </div>
    </div>
  </router-link>
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

<style scoped>
.product-card {
  display: block;
  padding: 0;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  text-decoration: none;
  color: var(--text);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.product-image {
  aspect-ratio: 1;
  background: var(--bg);
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
  color: var(--text);
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
}
</style>
