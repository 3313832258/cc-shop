<template>
  <footer class="bg-elevated border-t border-default py-6 mt-10">
    <div class="max-w-7xl mx-auto px-5 flex justify-between items-center">
      <div class="flex gap-4 items-center">
        <NuxtLink to="/" class="text-muted text-xs hover:text-primary">首页</NuxtLink>
        <NuxtLink to="/product/list" class="text-muted text-xs hover:text-primary">商品</NuxtLink>
        <span class="text-dimmed text-xs">© 2026 CC-Shop. 简历/学习项目</span>
      </div>
      <div class="text-dimmed text-xs">
        Nuxt3 · Spring Cloud · Nacos · Seata · Sentinel
      </div>
    </div>
  </footer>
</template>
