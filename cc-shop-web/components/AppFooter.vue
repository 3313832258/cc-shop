<template>
  <footer class="app-footer">
    <div class="container footer-inner">
      <div class="footer-links">
        <router-link to="/">首页</router-link>
        <router-link to="/product/list">商品</router-link>
        <span class="text-secondary text-sm">© 2026 CC-Shop. 简历/学习项目</span>
      </div>
      <div class="footer-tech text-sm text-secondary">
        Nuxt3 · Spring Cloud · Nacos · Seata · Sentinel
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--surface);
  border-top: 1px solid var(--border);
  padding: 24px 0;
  margin-top: 40px;
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-links {
  display: flex;
  gap: 16px;
  align-items: center;
}

.footer-links a {
  color: var(--text-secondary);
  font-size: 13px;
}

.footer-links a:hover {
  color: var(--primary);
}
</style>
