<template>
  <div class="toast-container">
    <transition-group name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        :class="['toast', `toast-${t.type}`]"
      >
        {{ t.message }}
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
const { toasts } = useToast()
</script>

<style scoped>
.toast-enter-active { animation: slideIn 0.3s ease; }
.toast-leave-active { animation: slideIn 0.3s ease reverse; }
</style>
