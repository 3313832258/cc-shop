<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 py-6">
      <slot />
    </main>
    <AppFooter />
    <ChatWidget />
  </div>
</template>
