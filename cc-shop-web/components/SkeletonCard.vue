<template>
  <div class="skeleton-card" :style="{ width: cardWidth }">
    <div class="skeleton-image skeleton-pulse" :style="{ height: imageHeight }"></div>
    <div class="skeleton-content">
      <div class="skeleton-title skeleton-pulse"></div>
      <div class="skeleton-desc skeleton-pulse"></div>
      <div class="skeleton-price skeleton-pulse"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  cardWidth?: string
  imageHeight?: string
}

withDefaults(defineProps<Props>(), {
  cardWidth: '100%',
  imageHeight: '200px'
})
</script>

<style scoped>
.skeleton-card {
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.skeleton-pulse {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.skeleton-image {
  width: 100%;
}

.skeleton-content {
  padding: 12px;
}

.skeleton-title {
  height: 16px;
  width: 80%;
  margin-bottom: 8px;
  border-radius: 4px;
}

.skeleton-desc {
  height: 12px;
  width: 60%;
  margin-bottom: 12px;
  border-radius: 4px;
}

.skeleton-price {
  height: 20px;
  width: 40%;
  border-radius: 4px;
}
</style>