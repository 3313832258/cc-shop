<template>
  <div class="chat-widget">
    <!-- 悬浮按钮 -->
    <button class="chat-toggle" @click="toggle" :class="{ active: isOpen }">
      <span v-if="!isOpen">💬</span>
      <span v-else>✕</span>
    </button>

    <!-- 聊天窗口 -->
    <div v-if="isOpen" class="chat-window">
      <div class="chat-header">
        <span>🤖 AI 智能客服</span>
        <button class="clear-btn" @click="clearMessages" title="清空对话">🗑️</button>
      </div>

      <div class="chat-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="empty-hint">
          <p>你好！我是小C，CC-Shop 的 AI 客服 🛍️</p>
          <p>有什么可以帮你的？</p>
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message"
          :class="msg.role"
        >
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content">{{ msg.content }}</div>
        </div>

        <div v-if="isLoading" class="message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-content loading">
            <span class="dot">.</span><span class="dot">.</span><span class="dot">.</span>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <input
          v-model="inputText"
          @keyup.enter="handleSend"
          placeholder="输入你的问题..."
          :disabled="isLoading"
        />
        <button @click="handleSend" :disabled="isLoading || !inputText.trim()">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { messages, isLoading, isOpen, toggle, clearMessages, sendMessage } = useChat()
const inputText = ref('')
const messagesRef = ref<HTMLElement>()

async function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  await sendMessage(text)
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 监听消息变化自动滚动
watch(messages, () => scrollToBottom(), { deep: true })
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

.chat-toggle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--primary-color, #4f46e5);
  color: white;
  border: none;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s;
}

.chat-toggle:hover {
  transform: scale(1.1);
}

.chat-toggle.active {
  background: #ef4444;
}

.chat-window {
  position: absolute;
  bottom: 68px;
  right: 0;
  width: 380px;
  height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 16px;
  background: var(--primary-color, #4f46e5);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.clear-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-hint {
  text-align: center;
  color: #999;
  margin-top: 80px;
}

.empty-hint p {
  margin: 4px 0;
}

.message {
  display: flex;
  gap: 8px;
  max-width: 90%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.message.user .message-content {
  background: var(--primary-color, #4f46e5);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: #f3f4f6;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-content.loading .dot {
  animation: blink 1.4s infinite;
  font-size: 20px;
}

.message-content.loading .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.message-content.loading .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 20% { opacity: 0; }
  50% { opacity: 1; }
  100% { opacity: 0; }
}

.chat-input {
  display: flex;
  padding: 12px;
  border-top: 1px solid #e5e7eb;
  gap: 8px;
}

.chat-input input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
}

.chat-input input:focus {
  border-color: var(--primary-color, #4f46e5);
}

.chat-input button {
  padding: 10px 16px;
  background: var(--primary-color, #4f46e5);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

.chat-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
