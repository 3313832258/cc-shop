import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const deviceId = ref<string>('')
  const userId = ref<number | null>(null)
  const username = ref<string>('')

  const isLoggedIn = computed(() => !!token.value)

  function initFromStorage() {
    if (import.meta.client) {
      const saved = localStorage.getItem('cc_token')
      const savedRefresh = localStorage.getItem('cc_refresh_token')
      const savedDevice = localStorage.getItem('cc_device_id')
      const uid = localStorage.getItem('cc_uid')
      const name = localStorage.getItem('cc_username')
      if (saved) {
        token.value = saved
        refreshToken.value = savedRefresh || ''
        deviceId.value = savedDevice || ''
        userId.value = uid ? Number(uid) : null
        username.value = name || ''
      }
    }
  }

  function setAuth(t: string, uid: number, name: string, rt?: string, did?: string) {
    token.value = t
    userId.value = uid
    username.value = name
    if (rt) refreshToken.value = rt
    if (did) deviceId.value = did
    if (import.meta.client) {
      localStorage.setItem('cc_token', t)
      localStorage.setItem('cc_uid', String(uid))
      localStorage.setItem('cc_username', name)
      if (rt) localStorage.setItem('cc_refresh_token', rt)
      if (did) localStorage.setItem('cc_device_id', did)
    }
  }

  function setToken(t: string) {
    token.value = t
    if (import.meta.client) {
      localStorage.setItem('cc_token', t)
    }
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    deviceId.value = ''
    userId.value = null
    username.value = ''
    if (import.meta.client) {
      localStorage.removeItem('cc_token')
      localStorage.removeItem('cc_refresh_token')
      localStorage.removeItem('cc_device_id')
      localStorage.removeItem('cc_uid')
      localStorage.removeItem('cc_username')
    }
  }

  return { token, refreshToken, deviceId, userId, username, isLoggedIn, initFromStorage, setAuth, setToken, logout }
})
