<template>
  <div class="max-w-7xl mx-auto px-5">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-xl font-bold text-default">消息中心</h1>
      <UButton v-if="messages.length" variant="ghost" size="sm" @click="markAllRead">全部已读</UButton>
    </div>

    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <div v-else-if="messages.length === 0" class="flex flex-col items-center justify-center py-16">
      <span class="text-5xl mb-4">📭</span>
      <p class="text-dimmed">暂无消息</p>
    </div>

    <div v-else class="flex flex-col gap-3">
      <div
        v-for="m in messages"
        :key="m.id"
        class="bg-elevated rounded-lg shadow-sm p-4"
        :class="m.isRead ? '' : 'border-l-3 border-primary'"
      >
        <div class="flex justify-between mb-2">
          <UBadge
            :color="m.type === 'order' ? 'primary' : m.type === 'promotion' ? 'warning' : 'success'"
            variant="subtle"
            size="xs"
          >
            {{ { order: '订单', promotion: '促销', system: '系统' }[m.type] || m.type }}
          </UBadge>
          <span class="text-xs text-dimmed">{{ m.createdAt?.substring(0, 16) }}</span>
        </div>
        <h3 class="text-sm font-semibold text-default mb-1">{{ m.title }}</h3>
        <p class="text-sm text-muted">{{ m.content }}</p>
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
</script>
