import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const userId = ref<number | null>(null)
  const username = ref<string>('')

  const isLoggedIn = computed(() => !!token.value)

  function initFromStorage() {
    if (import.meta.client) {
      const saved = localStorage.getItem('cc_token')
      const uid = localStorage.getItem('cc_uid')
      const name = localStorage.getItem('cc_username')
      if (saved) {
        token.value = saved
        userId.value = uid ? Number(uid) : null
        username.value = name || ''
      }
    }
  }

  function setAuth(t: string, uid: number, name: string) {
    token.value = t
    userId.value = uid
    username.value = name
    if (import.meta.client) {
      localStorage.setItem('cc_token', t)
      localStorage.setItem('cc_uid', String(uid))
      localStorage.setItem('cc_username', name)
    }
  }

  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    if (import.meta.client) {
      localStorage.removeItem('cc_token')
      localStorage.removeItem('cc_uid')
      localStorage.removeItem('cc_username')
    }
  }

  return { token, userId, username, isLoggedIn, initFromStorage, setAuth, logout }
})
