<template>
  <div class="flex justify-center py-16 px-5">
    <div class="w-[400px] max-w-full bg-elevated rounded-lg shadow-sm p-6">
      <h2 class="text-xl font-bold text-center text-default mb-6">登录</h2>

      <!-- Tab 切换 -->
      <div class="flex border-b border-default mb-5">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="flex-1 pb-2.5 text-sm font-medium transition-colors border-b-2"
          :class="activeTab === tab.key ? 'text-primary border-primary' : 'text-muted border-transparent hover:text-default'"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 密码登录 -->
      <form v-if="activeTab === 'password'" @submit.prevent="handlePasswordLogin" class="flex flex-col gap-4">
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">用户名 / 手机号</label>
          <UInput v-model="pwdForm.account" placeholder="请输入用户名或手机号" required />
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">密码</label>
          <UInput v-model="pwdForm.password" type="password" placeholder="请输入密码" required />
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">验证码</label>
          <div class="flex gap-2">
            <UInput v-model="pwdForm.captchaAnswer" placeholder="计算结果" required class="flex-1" />
            <button
              type="button"
              class="px-3 py-2 bg-gray-100 rounded text-sm font-mono hover:bg-gray-200 transition-colors whitespace-nowrap"
              @click="refreshCaptcha"
            >
              {{ captcha.expression || '加载中...' }}
            </button>
          </div>
        </div>
        <UButton type="submit" block size="lg" color="primary" :loading="loading">
          {{ loading ? '登录中...' : '登录' }}
        </UButton>
        <p class="text-right">
          <NuxtLink to="/user/forgot-password" class="text-sm text-primary hover:underline">忘记密码？</NuxtLink>
        </p>
      </form>

      <!-- 验证码登录 -->
      <form v-else @submit.prevent="handleSmsLogin" class="flex flex-col gap-4">
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">手机号</label>
          <UInput v-model="smsForm.phone" placeholder="请输入手机号" required />
          <p v-if="smsForm.phone && !phoneValid" class="text-xs text-red-500 mt-1">请输入正确的11位手机号</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">图形验证码</label>
          <div class="flex gap-2">
            <UInput v-model="smsForm.captchaAnswer" placeholder="计算结果" required class="flex-1" />
            <button
              type="button"
              class="px-3 py-2 bg-gray-100 rounded text-sm font-mono hover:bg-gray-200 transition-colors whitespace-nowrap"
              @click="refreshCaptcha"
            >
              {{ captcha.expression || '加载中...' }}
            </button>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">短信验证码</label>
          <div class="flex gap-2">
            <UInput v-model="smsForm.code" placeholder="请输入验证码" required class="flex-1" />
            <UButton
              variant="outline"
              :disabled="countdown > 0"
              @click="sendCode"
              :loading="sendingCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </UButton>
          </div>
        </div>
        <UButton type="submit" block size="lg" color="primary" :loading="loading">
          {{ loading ? '登录中...' : '登录' }}
        </UButton>
        <p class="text-xs text-muted text-center">未注册的手机号将自动创建账号</p>
      </form>

      <p class="text-center text-sm text-muted mt-5">
        还没有账号？<NuxtLink to="/user/register" class="text-primary font-medium">立即注册</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
const api = useApi()
const authStore = useAuthStore()
const toast = useAppToast()

onMounted(() => {
  if (import.meta.client) {
    authStore.initFromStorage()
    if (authStore.isLoggedIn) {
      navigateTo(redirectTarget.value)
    }
    refreshCaptcha()
  }
})

const route = useRoute()
const redirectTarget = computed(() => (route.query.redirect as string) || '/')
const tabs = [
  { key: 'password', label: '密码登录' },
  { key: 'sms', label: '验证码登录' },
]
const activeTab = ref('password')
const loading = ref(false)

// 验证码状态
const captcha = reactive({ captchaId: '', expression: '' })

async function refreshCaptcha() {
  try {
    const res = await api.get<any>('/api/user/auth/captcha')
    if (res.code === 200) {
      captcha.captchaId = res.data.captchaId
      captcha.expression = res.data.expression
    }
  } catch {}
}

function switchTab(key: string) {
  activeTab.value = key
  refreshCaptcha()
}

// 密码登录
const pwdForm = reactive({
  account: (route.query.username as string) || '',
  password: '',
  captchaAnswer: '',
})

async function handlePasswordLogin() {
  if (!pwdForm.account || !pwdForm.password) {
    toast.warning('请输入用户名/手机号和密码')
    return
  }
  if (!pwdForm.captchaAnswer) {
    toast.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    const isPhone = /^1\d{10}$/.test(pwdForm.account)
    const body: any = {
      password: pwdForm.password,
      captchaId: captcha.captchaId,
      captchaAnswer: pwdForm.captchaAnswer,
    }
    if (isPhone) {
      body.phone = pwdForm.account
    } else {
      body.username = pwdForm.account
    }
    const res = await api.post<any>('/api/user/auth/login', body)
    if (res.code === 200 && res.data) {
      authStore.setAuth(res.data.token, res.data.userId, res.data.username, res.data.refreshToken, res.data.deviceId)
      toast.success('登录成功')
      navigateTo(redirectTarget.value)
    } else {
      toast.error(res.message || '登录失败')
      refreshCaptcha()
    }
  } catch (e: any) {
    const msg = e?.message || '登录失败'
    if (msg.includes('请先注册')) {
      toast.error('用户不存在，请先注册')
      setTimeout(() => navigateTo('/user/register'), 1500)
    } else {
      toast.error(msg)
    }
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// 验证码倒计时
const countdown = ref(0)
const sendingCode = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const smsForm = reactive({
  phone: '',
  code: '',
  captchaAnswer: '',
})
const phoneValid = computed(() => /^1\d{10}$/.test(smsForm.phone))

async function sendCode() {
  if (!smsForm.phone) {
    toast.warning('请输入手机号')
    return
  }
  if (!/^1\d{10}$/.test(smsForm.phone)) {
    toast.warning('请输入正确的手机号')
    return
  }
  if (!smsForm.captchaAnswer) {
    toast.warning('请输入图形验证码')
    return
  }
  sendingCode.value = true
  try {
    const res = await api.post<any>('/api/user/auth/sms/send', {
      phone: smsForm.phone,
      captchaId: captcha.captchaId,
      captchaAnswer: smsForm.captchaAnswer,
    })
    if (res.code === 200) {
      toast.success('验证码已发送，请查看控制台日志')
      countdown.value = 60
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0 && countdownTimer) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
      }, 1000)
    } else {
      toast.error(res.message || '发送失败')
      refreshCaptcha()
    }
  } catch (e: any) {
    toast.error(e?.message || '发送失败')
    refreshCaptcha()
  } finally {
    sendingCode.value = false
  }
}

async function handleSmsLogin() {
  if (!smsForm.phone || !smsForm.code) {
    toast.warning('请输入手机号和验证码')
    return
  }
  loading.value = true
  try {
    const res = await api.post<any>('/api/user/auth/sms/login', { phone: smsForm.phone, code: smsForm.code })
    if (res.code === 200 && res.data) {
      authStore.setAuth(res.data.token, res.data.userId, res.data.username, res.data.refreshToken, res.data.deviceId)
      toast.success('登录成功')
      navigateTo(redirectTarget.value)
    } else {
      toast.error(res.message || '登录失败')
    }
  } catch (e: any) {
    toast.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
