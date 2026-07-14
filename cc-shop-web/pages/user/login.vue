<template>
  <div class="auth-page">
    <div class="card auth-card">
      <h2 class="auth-title">登录</h2>
      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" class="form-input" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" class="form-input" placeholder="请输入密码" required />
        </div>
        <button type="submit" class="btn btn-primary btn-lg" style="width:100%" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <p class="auth-link text-center text-sm mt-4">
          还没有账号？<router-link to="/user/register">立即注册</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
const api = useApi()
const authStore = useAuthStore()
const toast = useToast()
const router = useRouter()

const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    const res = await api.post<any>('/api/user/auth/login', form)
    if (res.code === 200 && res.data) {
      authStore.setAuth(res.data.token, res.data.userId, res.data.username)
      toast.success('登录成功')
      router.push('/')
    }
  } catch (e: any) {
    // useApi 已处理错误 toast
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding: 60px 20px;
}
.auth-card {
  width: 400px;
  max-width: 100%;
}
.auth-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
  text-align: center;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  color: var(--text);
}
.auth-link a {
  color: var(--primary);
  font-weight: 500;
}
</style>
