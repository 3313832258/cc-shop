export function useApi() {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()

  const baseUrl = config.public.apiBase

  let isRefreshing = false
  let refreshQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = []

  async function doRefreshToken(): Promise<string> {
    if (!authStore.refreshToken || !authStore.deviceId) throw new Error('无 RefreshToken')
    const response = await fetch(`${baseUrl}/api/user/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: authStore.refreshToken, deviceId: authStore.deviceId }),
    })
    const data = await response.json()
    if (data.code === 200 && data.data) {
      authStore.setToken(data.data.token)
      if (data.data.refreshToken) {
        authStore.refreshToken = data.data.refreshToken
        localStorage.setItem('admin_refresh_token', data.data.refreshToken)
      }
      return data.data.token
    }
    throw new Error('刷新失败')
  }

  async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...((options.headers as Record<string, string>) || {}),
    }

    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }

    let response = await fetch(`${baseUrl}${url}`, { ...options, headers })

    // 401 尝试刷新 Token
    if (response.status === 401 && authStore.refreshToken && !url.includes('/auth/refresh')) {
      if (!isRefreshing) {
        isRefreshing = true
        try {
          const newToken = await doRefreshToken()
          headers['Authorization'] = `Bearer ${newToken}`
          response = await fetch(`${baseUrl}${url}`, { ...options, headers })
          refreshQueue.forEach(({ resolve }) => resolve(newToken))
          refreshQueue = []
        } catch (refreshErr) {
          refreshQueue.forEach(({ reject }) => reject(refreshErr))
          refreshQueue = []
          authStore.logout()
          navigateTo('/login')
          throw new Error('登录已过期')
        } finally {
          isRefreshing = false
        }
      } else {
        return new Promise<T>((resolve, reject) => {
          refreshQueue.push({
            resolve: (newToken: string) => {
              headers['Authorization'] = `Bearer ${newToken}`
              fetch(`${baseUrl}${url}`, { ...options, headers })
                .then(r => r.json())
                .then(data => (data.code !== 200 ? reject(new Error(data.message)) : resolve(data as T)))
                .catch(reject)
            },
            reject,
          })
        })
      }
    }

    if (response.status === 401) {
      authStore.logout()
      navigateTo('/login')
      throw new Error('登录已过期')
    }

    const data = await response.json()

    if (!response.ok || data.code !== 200) {
      throw new Error(data.message || '请求失败')
    }

    return data as T
  }

  return {
    get: <T>(url: string) => request<T>(url, { method: 'GET' }),
    post: <T>(url: string, body?: any) =>
      request<T>(url, {
        method: 'POST',
        body: body ? JSON.stringify(body) : undefined,
      }),
    put: <T>(url: string, body?: any) =>
      request<T>(url, {
        method: 'PUT',
        body: body ? JSON.stringify(body) : undefined,
      }),
    del: <T>(url: string) => request<T>(url, { method: 'DELETE' }),
  }
}
