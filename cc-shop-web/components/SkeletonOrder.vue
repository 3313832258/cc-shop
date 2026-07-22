<template>
  <div class="skeleton-order">
    <div class="skeleton-order-header">
      <div class="skeleton-text-sm skeleton-pulse"></div>
      <div class="skeleton-text-xs skeleton-pulse"></div>
    </div>
    <div class="skeleton-order-body">
      <div class="skeleton-order-item" v-for="i in items" :key="i">
        <div class="skeleton-image skeleton-pulse"></div>
        <div class="skeleton-order-info">
          <div class="skeleton-text-md skeleton-pulse"></div>
          <div class="skeleton-text-sm skeleton-pulse"></div>
        </div>
        <div class="skeleton-order-price skeleton-pulse"></div>
      </div>
    </div>
    <div class="skeleton-order-footer">
      <div class="skeleton-text-sm skeleton-pulse"></div>
      <div class="skeleton-button skeleton-pulse"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  items?: number
}

withDefaults(defineProps<Props>(), {
  items: 2
})
</script>

<style scoped>
.skeleton-order {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
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

.skeleton-order-header {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.skeleton-order-body {
  padding: 16px;
}

.skeleton-order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.skeleton-order-item + .skeleton-order-item {
  border-top: 1px solid #f3f4f6;
  margin-top: 8px;
  padding-top: 16px;
}

.skeleton-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  flex-shrink: 0;
}

.skeleton-order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.skeleton-text-sm {
  height: 14px;
  width: 120px;
  border-radius: 4px;
}

.skeleton-text-xs {
  height: 12px;
  width: 80px;
  border-radius: 4px;
}

.skeleton-text-md {
  height: 16px;
  width: 200px;
  border-radius: 4px;
}

.skeleton-order-price {
  height: 18px;
  width: 80px;
  border-radius: 4px;
  flex-shrink: 0;
}

.skeleton-button {
  height: 32px;
  width: 80px;
  border-radius: 4px;
}
</style>