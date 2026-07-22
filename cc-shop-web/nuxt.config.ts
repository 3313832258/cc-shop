export default defineNuxtConfig({
  devtools: { enabled: false },
  css: ['~/assets/css/main.css'],
  modules: ['@pinia/nuxt', '@nuxt/ui'],
  runtimeConfig: {
    public: {
      apiBase: 'http://127.0.0.1:8080',
    },
  },
  // Nuxt UI v3 自带 Tailwind CSS v4，无需额外配置
  // 品牌色通过 app.config.ts 或 CSS 变量覆盖
  // 禁用 SSR 以避免 UDropdownMenu 等组件的 DOM 引用问题
  ssr: false,
  app: {
    head: {
      htmlAttrs: { lang: 'zh-CN' },
      meta: [
        { charset: 'utf-8' },
      ],
    },
  },
})
