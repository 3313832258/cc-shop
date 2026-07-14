<template>
  <div class="container">
    <h1 class="page-title">购物车</h1>
    <div class="empty-state mt-8">
      <span style="font-size:48px">🛒</span>
      <p class="mt-4">购物车功能将在 Day 2 实现</p>
      <router-link to="/product/list" class="btn btn-primary mt-4">去逛逛</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
</script>

<style scoped>
.page-title { font-size: 24px; font-weight: 700; }
</style>
