<template>
  <div>
    <div v-if="loading" class="text-center py-6 text-dimmed text-sm">加载地址...</div>

    <div v-else-if="!addresses.length" class="text-center py-6">
      <p class="text-dimmed text-sm mb-3">暂无收货地址，请先添加</p>
      <UButton size="sm" variant="outline" @click="navigateTo('/user/address')">去添加地址</UButton>
    </div>

    <div v-else class="flex flex-col gap-2">
      <div
        v-for="addr in addresses"
        :key="addr.id"
        class="relative flex items-start gap-3 p-3 rounded-lg border-2 cursor-pointer transition-all"
        :class="modelValue === addr.id ? 'border-primary bg-primary/10' : 'border-default hover:border-primary/50'"
        @click="emit('update:modelValue', addr.id)"
      >
        <input
          type="radio"
          :checked="modelValue === addr.id"
          :value="addr.id"
          class="mt-1 accent-primary shrink-0"
        >
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <span class="font-medium text-sm text-default">{{ addr.receiverName }}</span>
            <span class="text-sm text-muted">{{ addr.phone }}</span>
            <UBadge v-if="addr.isDefault === 1" color="primary" variant="subtle" size="xs">默认</UBadge>
          </div>
          <p class="text-sm text-muted">
            {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
          </p>
        </div>
      </div>
      <UButton size="sm" variant="ghost" @click="navigateTo('/user/address')" class="mt-2">
        + 添加新地址
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  modelValue: number | null
}>()
const emit = defineEmits<{
  'update:modelValue': [value: number]
}>()

const api = useApi()
const addresses = ref<any[]>([])
const loading = ref(false)

async function loadAddresses() {
  loading.value = true
  try {
    const res = await api.get<any>('/api/user/address')
    if (res.code === 200) {
      addresses.value = res.data || []
      if (!props.modelValue && addresses.value.length) {
        const def = addresses.value.find((a: any) => a.isDefault === 1) || addresses.value[0]
        emit('update:modelValue', def.id)
      }
    }
  } catch {}
  loading.value = false
}

onMounted(() => {
  loadAddresses()
})

defineExpose({ refresh: loadAddresses })
</script>
