export default defineNuxtConfig({
  devtools: { enabled: false },
  ssr: false,

  modules: ['@nuxt/ui', '@pinia/nuxt'],

  app: {
    head: {
      htmlAttrs: { lang: 'zh-CN' },
      meta: [{ charset: 'utf-8' }],
      title: 'CC-Shop 管理后台',
    },
  },

  css: ['~/assets/css/main.css'],

  // 禁用字体预加载以避免启动超时
  fonts: false,

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://127.0.0.1:8080',
    },
  },
})
