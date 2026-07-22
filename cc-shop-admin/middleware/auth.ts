export default defineNuxtRouteMiddleware((to) => {
  const authStore = useAuthStore()

  if (import.meta.client) {
    authStore.initFromStorage()
  }

  if (!authStore.isLoggedIn) {
    return navigateTo('/login')
  }
})
