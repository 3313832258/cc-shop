<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="w-full max-w-sm">
      <div class="bg-white rounded-lg shadow-md p-8">
        <h1 class="text-2xl font-bold text-center mb-6">
          CC-Shop <span class="text-primary">Admin</span>
        </h1>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
            <UInput
              v-model="form.username"
              placeholder="请输入用户名"
              icon="i-lucide-user"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
            <UInput
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              icon="i-lucide-lock"
            />
          </div>

          <UButton
            type="submit"
            block
            :loading="loading"
          >
            登录
          </UButton>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })

const api = useApi()
const authStore = useAuthStore()
const toast = useAppToast()

const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)

async function handleLogin() {
  if (!form.username || !form.password) {
    toast.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await api.post<any>('/api/admin/auth/login', {
      username: form.username,
      password: form.password,
    })
    if (res.code === 200) {
      authStore.setAuth({
        token: res.data.token,
        refreshToken: res.data.refreshToken,
        deviceId: res.data.deviceId,
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
      })
      toast.success('登录成功')
      navigateTo('/')
    }
  } catch (e: any) {
    toast.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (import.meta.client) {
    authStore.initFromStorage()
    if (authStore.isLoggedIn) {
      navigateTo('/')
    }
  }
})
</script>
