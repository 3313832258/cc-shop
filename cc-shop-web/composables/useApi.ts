/**
 * 统一 API 请求封装
 * 自动携带 JWT、处理 401 跳转、统一错误 toast
 */
export function useApi() {
  const config = useRuntimeConfig()
  const toast = useToast()
  const authStore = useAuthStore()

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

      if (status === 401) {
        authStore.logout()
        if (import.meta.client) {
          navigateTo('/user/login')
        }
        throw new Error('未登录')
      }

      const msg = body?.message || err?.message || '请求失败'
      toast.error(msg)
      throw new Error(msg)
    }
  }

  function get<T>(url: string, query?: Record<string, any>) {
    return request<T>(url, { method: 'GET', query })
  }

  function post<T>(url: string, body?: any) {
    return request<T>(url, { method: 'POST', body })
  }

  function put<T>(url: string, body?: any) {
    return request<T>(url, { method: 'PUT', body })
  }

  function del<T>(url: string) {
    return request<T>(url, { method: 'DELETE' })
  }

  return { get, post, put, del, request }
}
