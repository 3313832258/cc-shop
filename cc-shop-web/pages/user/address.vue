<template>
  <div class="max-w-3xl mx-auto px-5">
    <div class="flex items-center gap-4 mb-6">
      <UButton variant="ghost" icon="i-lucide-arrow-left" @click="navigateTo('/user/profile')" />
      <h1 class="text-xl font-bold text-default">收货地址管理</h1>
    </div>

    <!-- 添加地址按钮 -->
    <UButton class="mb-4" @click="showForm = true">
      + 添加新地址
    </UButton>

    <!-- 地址列表 -->
    <div v-if="loading" class="flex justify-center py-10">
      <UIcon name="i-lucide-loader-2" class="animate-spin text-primary" size="24" />
    </div>

    <div v-else-if="!addresses.length" class="text-center py-10">
      <p class="text-dimmed">暂无收货地址</p>
    </div>

    <div v-else class="flex flex-col gap-3">
      <div
        v-for="addr in addresses"
        :key="addr.id"
        class="bg-elevated rounded-lg shadow-sm p-4"
      >
        <div class="flex justify-between items-start">
          <div>
            <div class="flex items-center gap-2 mb-1">
              <span class="font-medium text-default">{{ addr.receiverName }}</span>
              <span class="text-muted">{{ addr.phone }}</span>
              <UBadge v-if="addr.isDefault === 1" color="primary" variant="subtle" size="xs">默认</UBadge>
            </div>
            <p class="text-sm text-muted">
              {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
            </p>
          </div>
          <div class="flex gap-2">
            <UButton size="xs" variant="outline" @click="editAddress(addr)">编辑</UButton>
            <UButton size="xs" variant="outline" color="error" @click="deleteAddress(addr.id)">删除</UButton>
            <UButton
              v-if="addr.isDefault !== 1"
              size="xs"
              variant="outline"
              @click="setDefault(addr.id)"
            >
              设为默认
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址表单弹窗 -->
    <div v-if="showForm" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div class="bg-elevated rounded-lg p-6 shadow-xl max-w-md w-[90%] max-h-[90vh] overflow-y-auto">
        <h3 class="text-lg font-semibold text-default mb-4">{{ editingId ? '编辑地址' : '添加地址' }}</h3>
        <form @submit.prevent="submitForm" class="flex flex-col gap-3">
          <div>
            <label class="block text-sm font-medium text-muted mb-1">收货人姓名 *</label>
            <UInput v-model="form.receiverName" placeholder="请输入收货人姓名" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-muted mb-1">联系电话 *</label>
            <UInput v-model="form.phone" placeholder="请输入手机号" required />
          </div>
          <div class="grid grid-cols-3 gap-2">
            <div>
              <label class="block text-sm font-medium text-muted mb-1">省份 *</label>
              <UInput v-model="form.province" placeholder="省" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-muted mb-1">城市 *</label>
              <UInput v-model="form.city" placeholder="市" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-muted mb-1">区县 *</label>
              <UInput v-model="form.district" placeholder="区" required />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-muted mb-1">详细地址 *</label>
            <UInput v-model="form.detail" placeholder="街道、门牌号等" required />
          </div>
          <div class="flex items-center gap-2">
            <input type="checkbox" v-model="form.isDefault" class="accent-primary" />
            <label class="text-sm text-muted">设为默认地址</label>
          </div>
          <div class="flex gap-3 mt-2">
            <UButton type="submit" color="primary" :loading="submitting">
              {{ submitting ? '保存中...' : '保存' }}
            </UButton>
            <UButton variant="outline" @click="closeForm">取消</UButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const api = useApi()
const toast = useAppToast()

const addresses = ref<any[]>([])
const loading = ref(true)
const showForm = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false,
})

async function loadAddresses() {
  loading.value = true
  try {
    const res = await api.get<any>('/api/user/address')
    if (res.code === 200) {
      addresses.value = res.data || []
    }
  } catch {}
  loading.value = false
}

function editAddress(addr: any) {
  editingId.value = addr.id
  form.receiverName = addr.receiverName
  form.phone = addr.phone
  form.province = addr.province
  form.city = addr.city
  form.district = addr.district
  form.detail = addr.detail
  form.isDefault = addr.isDefault === 1
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
  form.receiverName = ''
  form.phone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detail = ''
  form.isDefault = false
}

async function submitForm() {
  submitting.value = true
  try {
    const body = {
      ...form,
      isDefault: form.isDefault ? 1 : 0,
    }

    let res
    if (editingId.value) {
      res = await api.put<any>(`/api/user/address/${editingId.value}`, body)
    } else {
      res = await api.post<any>('/api/user/address', body)
    }

    if (res.code === 200) {
      toast.success(editingId.value ? '地址已更新' : '地址已添加')
      closeForm()
      await loadAddresses()
    }
  } catch {}
  submitting.value = false
}

async function deleteAddress(id: number) {
  if (!confirm('确定要删除这个地址吗？')) return
  try {
    const res = await api.del<any>(`/api/user/address/${id}`)
    if (res.code === 200) {
      toast.success('地址已删除')
      await loadAddresses()
    }
  } catch {}
}

async function setDefault(id: number) {
  try {
    const res = await api.put<any>(`/api/user/address/${id}/default`)
    if (res.code === 200) {
      toast.success('已设为默认地址')
      await loadAddresses()
    }
  } catch {}
}

onMounted(() => {
  loadAddresses()
})
</script>
