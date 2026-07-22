export default defineNuxtRouteMiddleware((to) => {
  const authStore = useAuthStore()

  // 确保从 localStorage 恢复状态
  if (import.meta.client && !authStore.isLoggedIn) {
    authStore.initFromStorage()
  }

  if (!authStore.isLoggedIn) {
    return navigateTo({ path: '/user/login', query: { redirect: to.fullPath } })
  }
})
