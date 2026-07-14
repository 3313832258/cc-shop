<template>
  <div class="container">
    <h1 class="page-title">个人信息</h1>
    <div v-if="user" class="card profile-card mt-6">
      <div class="profile-row"><span class="label">用户名</span><span>{{ user.username }}</span></div>
      <div class="profile-row"><span class="label">手机</span><span>{{ user.phone || '未绑定' }}</span></div>
      <div class="profile-row"><span class="label">邮箱</span><span>{{ user.email || '未绑定' }}</span></div>
      <div class="profile-row"><span class="label">注册时间</span><span>{{ user.createdAt?.substring(0, 10) }}</span></div>
    </div>
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const api = useApi()
const user = ref<any>(null)
onMounted(async () => {
  try {
    const res = await api.get<any>('/api/user/profile')
    if (res.code === 200) user.value = res.data
  } catch {}
})
</script>

<style scoped>
.page-title { font-size: 24px; font-weight: 700; }
.profile-card { max-width: 500px; }
.profile-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border); font-size: 14px; }
.profile-row:last-child { border-bottom: none; }
.label { color: var(--text-secondary); font-weight: 500; }
</style>
