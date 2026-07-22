<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">商家管理</h1>
      <UButton @click="showCreateModal = true">
        <UIcon name="i-lucide-plus" size="16" class="mr-1" />
        创建商家账号
      </UButton>
    </div>

    <!-- 商家列表 -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200">
      <div v-if="loading" class="p-8 text-center text-gray-500">加载中...</div>
      <div v-else-if="merchants.length === 0" class="p-8 text-center text-gray-500">暂无商家账号</div>
      <table v-else class="w-full">
        <thead>
          <tr class="border-b border-gray-200 bg-gray-50">
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">ID</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">用户名</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in merchants" :key="m.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-4 py-3 text-sm text-gray-600">{{ m.id }}</td>
            <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ m.username }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ m.createTime }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 创建商家弹窗 -->
    <Teleport to="body">
      <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/40" @click="showCreateModal = false" />
        <div class="relative bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <h2 class="text-lg font-bold mb-4">创建商家账号</h2>
          <form @submit.prevent="handleCreate" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
              <UInput v-model="createForm.username" placeholder="请输入用户名" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
              <UInput v-model="createForm.password" type="password" placeholder="请输入密码" />
            </div>
            <div class="flex justify-end gap-2">
              <UButton variant="ghost" @click="showCreateModal = false">取消</UButton>
              <UButton type="submit" :loading="creating">创建</UButton>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
const api = useApi()
const toast = useAppToast()

const merchants = ref<any[]>([])
const loading = ref(true)
const showCreateModal = ref(false)
const creating = ref(false)
const createForm = reactive({ username: '', password: '' })

async function fetchMerchants() {
  loading.value = true
  try {
    const res = await api.get<any>('/api/user/admin/merchants')
    merchants.value = res.data || []
  } catch (e: any) {
    toast.error(e?.message || '获取商家列表失败')
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!createForm.username || !createForm.password) {
    toast.warning('请输入用户名和密码')
    return
  }
  creating.value = true
  try {
    await api.post('/api/user/admin/merchant', {
      username: createForm.username,
      password: createForm.password,
    })
    toast.success('商家账号创建成功')
    showCreateModal.value = false
    createForm.username = ''
    createForm.password = ''
    fetchMerchants()
  } catch (e: any) {
    toast.error(e?.message || '创建失败')
  } finally {
    creating.value = false
  }
}

onMounted(fetchMerchants)
</script>
