// 客户端初始化：从 localStorage 恢复 auth token
export default defineNuxtPlugin(() => {
  const authStore = useAuthStore()
  if (import.meta.client) {
    authStore.initFromStorage()
  }
})
