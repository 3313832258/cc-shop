<template>
  <div class="max-w-7xl mx-auto px-5">
    <h1 class="text-xl font-bold text-default mb-6">个人信息</h1>

    <div v-if="user" class="max-w-lg bg-elevated rounded-lg shadow-sm p-5">
      <div class="flex justify-between py-3 border-b border-muted text-sm">
        <span class="text-muted font-medium">用户名</span>
        <span class="text-default">{{ user.username }}</span>
      </div>
      <div class="flex justify-between py-3 border-b border-muted text-sm">
        <span class="text-muted font-medium">手机</span>
        <span class="text-default">{{ user.phone || '未绑定' }}</span>
      </div>
      <div class="flex justify-between py-3 border-b border-muted text-sm">
        <span class="text-muted font-medium">邮箱</span>
        <span class="text-default">{{ user.email || '未绑定' }}</span>
      </div>
      <div class="flex justify-between py-3 border-b border-muted text-sm">
        <span class="text-muted font-medium">注册时间</span>
        <span class="text-default">{{ user.createdAt?.substring(0, 10) }}</span>
      </div>
      <div class="flex justify-between py-3 text-sm">
        <span class="text-muted font-medium">收货地址</span>
        <UButton size="sm" variant="outline" @click="navigateTo('/user/address')">管理地址</UButton>
      </div>
    </div>

    <div v-else class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>
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
