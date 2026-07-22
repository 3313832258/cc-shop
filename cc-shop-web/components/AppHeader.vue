<template>
  <header class="bg-elevated border-b border-default sticky top-0 z-50 shadow-sm">
    <div class="max-w-7xl mx-auto px-5 h-16 flex items-center gap-6">
      <!-- Logo -->
      <NuxtLink to="/" class="flex items-center gap-1 shrink-0">
        <span class="text-xl font-bold tracking-tight text-default">CC<span class="text-primary">Shop</span></span>
      </NuxtLink>

      <!-- 搜索 -->
      <div class="flex-1 flex gap-2 max-w-lg">
        <UInput
          v-model="searchQuery"
          placeholder="搜索商品..."
          icon="i-lucide-search"
          class="flex-1"
          @keyup.enter="doSearch"
        />
        <UButton color="primary" @click="doSearch">搜索</UButton>
      </div>

      <!-- 导航 -->
      <nav class="flex items-center gap-4">
        <NuxtLink to="/" class="text-sm font-medium text-muted hover:text-primary transition-colors">首页</NuxtLink>
        <NuxtLink to="/product/list" class="text-sm font-medium text-muted hover:text-primary transition-colors">商品</NuxtLink>
        <NuxtLink to="/promotion/flash" class="text-sm font-medium text-error hover:text-error/80 transition-colors">⚡秒杀</NuxtLink>

        <template v-if="isLoggedIn">
          <NuxtLink to="/favorites" class="text-sm font-medium text-muted hover:text-primary transition-colors">收藏</NuxtLink>
          <NuxtLink to="/message" class="text-sm font-medium text-muted hover:text-primary transition-colors">消息</NuxtLink>
          <NuxtLink to="/cart" class="text-sm font-medium text-muted hover:text-primary transition-colors">购物车</NuxtLink>
          <NuxtLink to="/coupon" class="text-sm font-medium text-muted hover:text-primary transition-colors">优惠券</NuxtLink>

          <UDropdownMenu :items="userMenuItems">
            <UButton variant="ghost" trailing-icon="i-lucide-chevron-down" class="text-sm font-medium">
              {{ authStore.username }}
            </UButton>
          </UDropdownMenu>
        </template>

        <template v-else>
          <UButton to="/user/login" variant="outline" size="sm">登录</UButton>
          <UButton to="/user/register" size="sm">注册</UButton>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
const authStore = useAuthStore()

// 确保从 localStorage 恢复状态
if (import.meta.client && !authStore.isLoggedIn) {
  authStore.initFromStorage()
}

const isLoggedIn = computed(() => authStore.isLoggedIn)

const searchQuery = ref('')

function doSearch() {
  if (searchQuery.value.trim()) {
    navigateTo({ path: '/product/list', query: { keyword: searchQuery.value } })
  }
}

function logout() {
  authStore.logout()
  navigateTo('/')
}

const userMenuItems = [
  { label: '个人信息', to: '/user/profile' },
  { label: '我的订单', to: '/order/list' },
  { label: '退出登录', onClick: logout },
]
</script>
