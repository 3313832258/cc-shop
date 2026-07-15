<template>
  <header class="app-header">
    <div class="container header-inner">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <span class="logo-text">CC<span class="logo-accent">Shop</span></span>
      </router-link>

      <!-- 搜索 -->
      <div class="search-bar">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索商品..."
          class="form-input search-input"
          @keyup.enter="doSearch"
        />
        <button class="btn btn-primary search-btn" @click="doSearch">搜索</button>
      </div>

      <!-- 导航 -->
      <nav class="header-nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/product/list" class="nav-link">商品</router-link>

        <template v-if="authStore.isLoggedIn">
          <router-link to="/favorites" class="nav-link">收藏</router-link>
          <router-link to="/message" class="nav-link">消息</router-link>
          <router-link to="/cart" class="nav-link">购物车</router-link>
          <router-link to="/coupon" class="nav-link">优惠券</router-link>
          <div class="user-dropdown">
            <span class="nav-link user-name">{{ authStore.username }}</span>
            <div class="dropdown-menu">
              <router-link to="/user/profile">个人信息</router-link>
              <router-link to="/order/list">我的订单</router-link>
              <button class="dropdown-item" @click="logout">退出登录</button>
            </div>
          </div>
        </template>
        <template v-else>
          <router-link to="/user/login" class="btn btn-sm btn-outline">登录</router-link>
          <router-link to="/user/register" class="btn btn-sm btn-primary">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
const authStore = useAuthStore()
const router = useRouter()

const searchQuery = ref('')

function doSearch() {
  if (searchQuery.value.trim()) {
    router.push({ path: '/product/list', query: { keyword: searchQuery.value } })
  }
}

function logout() {
  authStore.logout()
  router.push('/user/login')
}
</script>

<style scoped>
.app-header {
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.header-inner {
  display: flex;
  align-items: center;
  height: 64px;
  gap: 24px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.5px;
}

.logo-accent {
  color: var(--primary);
}

.search-bar {
  flex: 1;
  display: flex;
  gap: 8px;
  max-width: 480px;
}

.search-input {
  flex: 1;
}

.search-btn {
  white-space: nowrap;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link {
  color: var(--text);
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
  cursor: pointer;
}

.nav-link:hover, .nav-link.router-link-active {
  color: var(--primary);
}

.user-dropdown {
  position: relative;
}

.user-name {
  display: inline-block;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  min-width: 140px;
  display: none;
  overflow: hidden;
  z-index: 200;
}

.user-dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-menu a,
.dropdown-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  font-size: 14px;
  color: var(--text);
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-sans);
}

.dropdown-menu a:hover,
.dropdown-item:hover {
  background: var(--bg);
  color: var(--primary);
}
</style>
