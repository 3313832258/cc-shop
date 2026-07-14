<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">消息中心</h1>
      <button v-if="messages.length" class="btn btn-sm btn-ghost" @click="markAllRead">全部已读</button>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="messages.length === 0" class="empty-state">
      <span style="font-size:48px">📭</span>
      <p class="mt-4">暂无消息</p>
    </div>
    <div v-else class="message-list">
      <div v-for="m in messages" :key="m.id" :class="['card message-item', m.isRead ? '' : 'unread']">
        <div class="message-header">
          <span :class="['tag', typeClass(m.type)]">{{ typeLabel(m.type) }}</span>
          <span class="text-xs text-secondary">{{ m.createdAt?.substring(0, 16) }}</span>
        </div>
        <h3 class="message-title">{{ m.title }}</h3>
        <p class="text-sm text-secondary">{{ m.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
const api = useApi()
const messages = ref<any[]>([])
const loading = ref(true)

onMounted(loadMessages)

async function loadMessages() {
  try {
    const res = await api.get<any>('/api/user/message', { page: 1, size: 50 })
    if (res.code === 200 && res.data) messages.value = res.data.records || []
  } catch {}
  loading.value = false
}

async function markAllRead() {
  await api.put('/api/user/message/read-all')
  messages.value.forEach(m => m.isRead = 1)
}

function typeLabel(t: string) {
  return { order: '订单', promotion: '促销', system: '系统' }[t] || t
}
function typeClass(t: string) {
  return { order: 'tag-primary', promotion: 'tag-accent', system: 'tag-success' }[t] || 'tag-primary'
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; }
.message-list { display: flex; flex-direction: column; gap: 12px; }
.message-item { padding: 16px; }
.message-item.unread { border-left: 3px solid var(--primary); }
.message-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.message-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
</style>
