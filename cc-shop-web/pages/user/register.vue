<template>
  <div class="auth-page">
    <div class="card auth-card">
      <h2 class="auth-title">注册</h2>
      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" class="form-input" placeholder="3-20位字符" required minlength="3" maxlength="20" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" class="form-input" placeholder="至少6位" required minlength="6" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" type="tel" class="form-input" placeholder="选填" />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="form.email" type="email" class="form-input" placeholder="选填" />
        </div>
        <button type="submit" class="btn btn-primary btn-lg" style="width:100%" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
        <p class="auth-link text-center text-sm mt-4">
          已有账号？<router-link to="/user/login">去登录</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
const api = useApi()
const toast = useToast()
const router = useRouter()

const form = reactive({ username: '', password: '', phone: '', email: '' })
const loading = ref(false)

async function handleRegister() {
  loading.value = true
  try {
    const res = await api.post<any>('/api/user/auth/register', form)
    if (res.code === 200) {
      toast.success('注册成功，请登录')
      router.push('/user/login')
    }
  } catch (e: any) {
    // useApi 已处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 60px 20px; }
.auth-card { width: 400px; max-width: 100%; }
.auth-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; text-align: center; }
.auth-form { display: flex; flex-direction: column; gap: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 500; margin-bottom: 6px; }
.auth-link a { color: var(--primary); font-weight: 500; }
</style>
