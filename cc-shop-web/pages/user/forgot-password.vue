<template>
  <div class="flex justify-center py-16 px-5">
    <div class="w-[400px] max-w-full bg-elevated rounded-lg shadow-sm p-6">
      <h2 class="text-xl font-bold text-center text-default mb-6">重置密码</h2>

      <!-- 步骤 1: 输入手机号 + 图形验证码 + 短信验证码 -->
      <form v-if="step === 1" @submit.prevent="handleVerify" class="flex flex-col gap-4">
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">手机号</label>
          <UInput v-model="form.phone" placeholder="请输入注册时的手机号" required />
          <p v-if="form.phone && !phoneValid" class="text-xs text-red-500 mt-1">请输入正确的11位手机号</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">图形验证码</label>
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
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">短信验证码</label>
          <div class="flex gap-2">
            <UInput v-model="form.code" placeholder="请输入验证码" required class="flex-1" />
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
          下一步
        </UButton>
      </form>

      <!-- 步骤 2: 设置新密码 -->
      <form v-else @submit.prevent="handleReset" class="flex flex-col gap-4">
        <p class="text-sm text-muted">为手机号 {{ form.phone }} 设置新密码</p>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">新密码</label>
          <UInput v-model="form.newPassword" type="password" placeholder="至少6位" required minlength="6" />
        </div>
        <div>
          <label class="block text-sm font-medium text-muted mb-1.5">确认密码</label>
          <UInput v-model="form.confirmPassword" type="password" placeholder="再次输入密码" required />
        </div>
        <UButton type="submit" block size="lg" color="primary" :loading="loading">
          重置密码
        </UButton>
      </form>

      <p class="text-center text-sm text-muted mt-5">
        <NuxtLink to="/user/login" class="text-primary font-medium">返回登录</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })

const api = useApi()
const toast = useAppToast()

const step = ref(1)
const loading = ref(false)
const captcha = reactive({ captchaId: '', expression: '' })
const countdown = ref(0)
const sendingCode = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  phone: '',
  code: '',
  captchaAnswer: '',
  newPassword: '',
  confirmPassword: '',
})
const phoneValid = computed(() => /^1\d{10}$/.test(form.phone))

async function refreshCaptcha() {
  try {
    const res = await api.get<any>('/api/user/auth/captcha')
    if (res.code === 200) {
      captcha.captchaId = res.data.captchaId
      captcha.expression = res.data.expression
    }
  } catch {}
}

onMounted(refreshCaptcha)

async function sendCode() {
  if (!form.phone) {
    toast.warning('请输入手机号')
    return
  }
  if (!/^1\d{10}$/.test(form.phone)) {
    toast.warning('请输入正确的手机号')
    return
  }
  if (!form.captchaAnswer) {
    toast.warning('请输入图形验证码')
    return
  }
  sendingCode.value = true
  try {
    const res = await api.post<any>('/api/user/auth/sms/send', {
      phone: form.phone,
      captchaId: captcha.captchaId,
      captchaAnswer: form.captchaAnswer,
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

function handleVerify() {
  if (!form.phone || !form.code) {
    toast.warning('请输入手机号和验证码')
    return
  }
  step.value = 2
}

async function handleReset() {
  if (!form.newPassword || !form.confirmPassword) {
    toast.warning('请输入新密码')
    return
  }
  if (form.newPassword.length < 6) {
    toast.warning('密码长度不能少于6位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    toast.warning('两次密码输入不一致')
    return
  }
  loading.value = true
  try {
    const res = await api.post<any>('/api/user/auth/reset-password', {
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword,
    })
    if (res.code === 200) {
      toast.success('密码重置成功，请重新登录')
      navigateTo('/user/login')
    } else {
      toast.error(res.message || '重置失败')
    }
  } catch (e: any) {
    toast.error(e?.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>
