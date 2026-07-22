<template>
  <div class="flex justify-center py-16 px-5">
    <div class="w-[400px] max-w-full bg-elevated rounded-lg shadow-sm p-6">
      <h2 class="text-xl font-bold text-center text-default mb-6">注册</h2>
      <form @submit.prevent="handleRegister" class="flex flex-col gap-4">
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">用户名</label>
          <UInput v-model="form.username" placeholder="3-20位字符" required minlength="3" maxlength="20" />
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">密码</label>
          <UInput v-model="form.password" type="password" placeholder="至少6位" required minlength="6" />
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">手机号</label>
          <UInput v-model="form.phone" placeholder="选填" />
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">邮箱</label>
          <UInput v-model="form.email" placeholder="选填" />
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">验证码</label>
          <div class="flex gap-2">
            <UInput v-model="form.captchaAnswer" placeholder="计算结果" required class="flex-1" />
            <button
              type="button"
              class="px-3 py-2 bg-gray-100 rounded text-sm font-mono hover:bg-gray-200 transition-colors whitespace-nowrap"
              @click="refreshCaptcha"
            >
              {{ captcha.expression || '加载中...' }}
            </button>
          </div>
        </div>
        <label class="flex items-start gap-2 text-sm text-muted">
          <input v-model="agreed" type="checkbox" class="mt-1" />
          <span>我已阅读并同意《用户服务协议》和《隐私政策》</span>
        </label>
        <UButton type="submit" block size="lg" color="primary" :loading="loading" :disabled="!agreed">
          {{ loading ? '注册中...' : '注册' }}
        </UButton>
        <p class="text-center text-sm text-muted mt-2">
          已有账号？<NuxtLink to="/user/login" class="text-primary font-medium">去登录</NuxtLink>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
const api = useApi()
const authStore = useAuthStore()
const toast = useAppToast()

const form = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  captchaId: '',
  captchaAnswer: '',
})
const loading = ref(false)
const agreed = ref(false)
const captcha = reactive({ captchaId: '', expression: '' })

async function refreshCaptcha() {
  try {
    const res = await api.get<any>('/api/user/auth/captcha')
    if (res.code === 200) {
      captcha.captchaId = res.data.captchaId
      captcha.expression = res.data.expression
      form.captchaId = res.data.captchaId
    }
  } catch {}
}

onMounted(refreshCaptcha)

async function handleRegister() {
  if (!form.username || !form.password) {
    toast.warning('请输入用户名和密码')
    return
  }
  if (!form.captchaAnswer) {
    toast.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    const res = await api.post<any>('/api/user/auth/register', {
      username: form.username,
      password: form.password,
      phone: form.phone || undefined,
      email: form.email || undefined,
    })
    if (res.code === 200) {
      // 注册成功，自动登录
      const loginRes = await api.post<any>('/api/user/auth/login', {
        username: form.username,
        password: form.password,
      })
      if (loginRes.code === 200 && loginRes.data) {
        authStore.setAuth(loginRes.data.token, loginRes.data.userId, loginRes.data.username, loginRes.data.refreshToken, loginRes.data.deviceId)
        toast.success('注册成功')
        navigateTo('/')
      } else {
        toast.success('注册成功，请登录')
        navigateTo('/user/login')
      }
    }
  } catch (e: any) {
    // useApi 已处理
  } finally {
    loading.value = false
    refreshCaptcha()
  }
}
</script>
