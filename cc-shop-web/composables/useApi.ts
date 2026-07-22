/**
 * 统一 API 请求封装
 * 自动携带 JWT、401 自动刷新 Token、处理错误 toast
 */
export function useApi() {
  const config = useRuntimeConfig()
  const toast = useAppToast()
  const authStore = useAuthStore()

  // 确保从 localStorage 恢复状态
  if (import.meta.client && !authStore.isLoggedIn) {
    authStore.initFromStorage()
  }

  let isRefreshing = false
  let refreshQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = []

  async function doRefreshToken(): Promise<string> {
    if (!authStore.refreshToken || !authStore.deviceId) {
      throw new Error('无 RefreshToken')
    }
    const data = await $fetch<any>('/api/user/auth/refresh', {
      method: 'POST',
      baseURL: config.public.apiBase,
      body: { refreshToken: authStore.refreshToken, deviceId: authStore.deviceId },
    })
    if (data.code === 200 && data.data) {
      const { token: newToken, refreshToken: newRefresh } = data.data
      authStore.setToken(newToken)
      if (newRefresh) {
        localStorage.setItem('cc_refresh_token', newRefresh)
        authStore.refreshToken = newRefresh
      }
      return newToken
    }
    throw new Error('刷新失败')
  }

  async function request<T = any>(url: string, options: any = {}): Promise<T> {
    const headers: Record<string, string> = options.headers || {}

    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }

    try {
      const data = await $fetch<T>(url, {
        baseURL: config.public.apiBase,
        ...options,
        headers,
      })
      return data
    } catch (err: any) {
      const status = err?.response?.status || err?.statusCode
      const body = err?.data || err?.response?._data

      // 401 尝试刷新 Token
      if (status === 401 && authStore.refreshToken && !url.includes('/auth/refresh')) {
        if (!isRefreshing) {
          isRefreshing = true
          try {
            const newToken = await doRefreshToken()
            // 重试原请求
            headers['Authorization'] = `Bearer ${newToken}`
            const retryData = await $fetch<T>(url, {
              baseURL: config.public.apiBase,
              ...options,
              headers,
            })
            // 处理队列中的等待请求
            refreshQueue.forEach(({ resolve }) => resolve(newToken))
            refreshQueue = []
            return retryData
          } catch (refreshErr) {
            // 刷新失败，登出
            refreshQueue.forEach(({ reject }) => reject(refreshErr))
            refreshQueue = []
            authStore.logout()
            if (import.meta.client) {
              navigateTo('/user/login')
            }
            throw new Error('登录已过期，请重新登录')
          } finally {
            isRefreshing = false
          }
        } else {
          // 正在刷新中，加入队列等待
          return new Promise<T>((resolve, reject) => {
            refreshQueue.push({
              resolve: (newToken: string) => {
                headers['Authorization'] = `Bearer ${newToken}`
                $fetch<T>(url, { baseURL: config.public.apiBase, ...options, headers })
                  .then(resolve)
                  .catch(reject)
              },
              reject,
            })
          })
        }
      }

      // 非 401 或刷新失败
      if (status === 401) {
        authStore.logout()
        if (import.meta.client) {
          navigateTo('/user/login')
        }
        throw new Error('未登录')
      }

      const msg = body?.message || err?.message || '请求失败'
      // 400 错误由调用方处理（如登录/注册的业务错误）
      if (status !== 400) {
        toast.error(msg)
      }
      throw new Error(msg)
    }
  }

  function get<T>(url: string, query?: Record<string, any>) {
    return request<T>(url, { method: 'GET', query })
  }

  function post<T>(url: string, body?: any, query?: Record<string, any>) {
    return request<T>(url, { method: 'POST', body, query })
  }

  function put<T>(url: string, body?: any) {
    return request<T>(url, { method: 'PUT', body })
  }

  function del<T>(url: string) {
    return request<T>(url, { method: 'DELETE' })
  }

  return { get, post, put, del, request }
}
