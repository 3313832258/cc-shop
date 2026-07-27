/**
 * AI 聊天状态管理
 * 支持 SSE 流式消息
 */
export function useChat() {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()

  const messages = ref<Array<{ role: 'user' | 'assistant'; content: string }>>([])
  const isLoading = ref(false)
  const isOpen = ref(false)

  function toggle() {
    isOpen.value = !isOpen.value
  }

  function clearMessages() {
    messages.value = []
  }

  /**
   * 发送消息（SSE 流式）
   */
  async function sendMessage(content: string) {
    if (!content.trim() || isLoading.value) return

    // 添加用户消息
    messages.value.push({ role: 'user', content: content.trim() })

    // 添加 AI 占位消息
    const aiMessage = { role: 'assistant' as const, content: '' }
    messages.value.push(aiMessage)
    isLoading.value = true

    try {
      const token = authStore.token
      const headers: Record<string, string> = {}
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }

      const response = await fetch(
        `${config.public.apiBase}/api/ai/chat/stream?message=${encodeURIComponent(content.trim())}`,
        { headers }
      )

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) throw new Error('无法读取响应流')

      const decoder = new TextDecoder()

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })

        // 解析 SSE 格式
        const lines = chunk.split('\n')
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5).trim()
            if (data === '[DONE]') continue
            if (data) {
              aiMessage.content += data
            }
          }
        }
      }
    } catch (error: any) {
      console.error('AI 对话失败:', error)
      aiMessage.content = '抱歉，AI 服务暂时不可用，请稍后再试。'
    } finally {
      isLoading.value = false
    }
  }

  return {
    messages,
    isLoading,
    isOpen,
    toggle,
    clearMessages,
    sendMessage
  }
}
