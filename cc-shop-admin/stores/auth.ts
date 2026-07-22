import { defineStore } from 'pinia'

interface AuthState {
  token: string
  refreshToken: string
  deviceId: string
  userId: number | null
  username: string
  role: number
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: '',
    refreshToken: '',
    deviceId: '',
    userId: null,
    username: '',
    role: 0,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role >= 2,
    isMerchant: (state) => state.role >= 1,
  },

  actions: {
    setAuth(data: { token: string; refreshToken?: string; deviceId?: string; userId: number; username: string; role: number }) {
      this.token = data.token
      this.refreshToken = data.refreshToken || ''
      this.deviceId = data.deviceId || ''
      this.userId = data.userId
      this.username = data.username
      this.role = data.role

      if (import.meta.client) {
        localStorage.setItem('admin_token', data.token)
        localStorage.setItem('admin_uid', String(data.userId))
        localStorage.setItem('admin_username', data.username)
        localStorage.setItem('admin_role', String(data.role))
        if (data.refreshToken) localStorage.setItem('admin_refresh_token', data.refreshToken)
        if (data.deviceId) localStorage.setItem('admin_device_id', data.deviceId)
      }
    },

    setToken(t: string) {
      this.token = t
      if (import.meta.client) {
        localStorage.setItem('admin_token', t)
      }
    },

    logout() {
      this.token = ''
      this.refreshToken = ''
      this.deviceId = ''
      this.userId = null
      this.username = ''
      this.role = 0

      if (import.meta.client) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_refresh_token')
        localStorage.removeItem('admin_device_id')
        localStorage.removeItem('admin_uid')
        localStorage.removeItem('admin_username')
        localStorage.removeItem('admin_role')
      }
    },

    initFromStorage() {
      if (import.meta.client) {
        this.token = localStorage.getItem('admin_token') || ''
        this.refreshToken = localStorage.getItem('admin_refresh_token') || ''
        this.deviceId = localStorage.getItem('admin_device_id') || ''
        const uid = localStorage.getItem('admin_uid')
        this.userId = uid ? Number(uid) : null
        this.username = localStorage.getItem('admin_username') || ''
        const role = localStorage.getItem('admin_role')
        this.role = role ? Number(role) : 0
      }
    },
  },
})
