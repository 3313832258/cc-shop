<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 侧边栏 -->
    <aside class="fixed left-0 top-0 h-full w-64 bg-white border-r border-gray-200 z-40">
      <div class="p-4 border-b border-gray-200">
        <NuxtLink to="/" class="text-xl font-bold text-gray-900">
          CC-Shop <span class="text-primary">Admin</span>
        </NuxtLink>
      </div>

      <nav class="p-4 space-y-1">
        <NuxtLink
          to="/"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path === '/' ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100'"
        >
          <UIcon name="i-lucide-layout-dashboard" size="18" />
          Dashboard
        </NuxtLink>

        <NuxtLink
          to="/product"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path.startsWith('/product') ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100'"
        >
          <UIcon name="i-lucide-package" size="18" />
          商品管理
        </NuxtLink>

        <NuxtLink
          to="/order"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path.startsWith('/order') ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100'"
        >
          <UIcon name="i-lucide-shopping-cart" size="18" />
          订单管理
        </NuxtLink>

        <NuxtLink
          to="/aftersale"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path.startsWith('/aftersale') ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100'"
        >
          <UIcon name="i-lucide-rotate-ccw" size="18" />
          售后管理
        </NuxtLink>

        <NuxtLink
          to="/coupon"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path.startsWith('/coupon') ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100'"
        >
          <UIcon name="i-lucide-ticket" size="18" />
          优惠券管理
        </NuxtLink>

        <NuxtLink
          to="/customer"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path.startsWith('/customer') ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100'"
        >
          <UIcon name="i-lucide-users" size="18" />
          客户管理
        </NuxtLink>

        <NuxtLink
          v-if="authStore.isAdmin"
          to="/merchant"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
          :class="$route.path.startsWith('/merchant') ? 'bg-primary text-white' : 'text-gray-700 hover:bg-gray-100'"
        >
          <UIcon name="i-lucide-store" size="18" />
          商家管理
        </NuxtLink>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="ml-64">
      <!-- 顶部栏 -->
      <header class="sticky top-0 z-30 bg-white border-b border-gray-200 h-16 flex items-center justify-between px-6">
        <div class="text-sm text-gray-500">
          管理后台
        </div>

        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-700">{{ authStore.username }}</span>
          <UButton
            variant="ghost"
            size="sm"
            @click="handleLogout"
          >
            退出登录
          </UButton>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="p-6">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
const authStore = useAuthStore()

if (import.meta.client && !authStore.isLoggedIn) {
  authStore.initFromStorage()
}

function handleLogout() {
  authStore.logout()
  navigateTo('/login')
}
</script>
