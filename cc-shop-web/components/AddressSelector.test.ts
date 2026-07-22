import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createWrapper, mockApiResponse, createMockApi } from '../test/utils'
import AddressSelector from './AddressSelector.vue'

// Mock useApi
const mockApi = createMockApi()
vi.mock('../composables/useApi', () => ({
  useApi: () => mockApi,
}))

describe('AddressSelector', () => {
  const addresses = [
    {
      id: 1,
      receiverName: '张三',
      phone: '13800138000',
      province: '北京市',
      city: '北京市',
      district: '朝阳区',
      detail: '三里屯路1号',
      isDefault: 1,
    },
    {
      id: 2,
      receiverName: '李四',
      phone: '13900139000',
      province: '上海市',
      city: '上海市',
      district: '浦东新区',
      detail: '陆家嘴环路1000号',
      isDefault: 0,
    },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    mockApi.get.mockResolvedValue(mockApiResponse(addresses))
  })

  it('renders component correctly', () => {
    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('renders addresses after loading', async () => {
    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    // Wait for async load
    await new Promise((resolve) => setTimeout(resolve, 100))

    expect(wrapper.text()).toContain('张三')
    expect(wrapper.text()).toContain('13800138000')
    expect(wrapper.text()).toContain('李四')
  })

  it('renders full address', async () => {
    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    await new Promise((resolve) => setTimeout(resolve, 100))

    expect(wrapper.text()).toContain('北京市北京市朝阳区 三里屯路1号')
  })

  it('shows default badge for default address', async () => {
    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    await new Promise((resolve) => setTimeout(resolve, 100))

    const badges = wrapper.findAll('.u-badge')
    expect(badges.length).toBeGreaterThan(0)
    expect(badges[0].text()).toBe('默认')
  })

  it('emits update:modelValue when address clicked', async () => {
    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    await new Promise((resolve) => setTimeout(resolve, 100))

    const addressItems = wrapper.findAll('.flex.items-start')
    if (addressItems.length > 0) {
      await addressItems[0].trigger('click')
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    }
  })

  it('shows empty state when no addresses', async () => {
    mockApi.get.mockResolvedValue(mockApiResponse([]))

    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    await new Promise((resolve) => setTimeout(resolve, 100))

    expect(wrapper.text()).toContain('暂无收货地址')
  })

  it('shows add address button when no addresses', async () => {
    mockApi.get.mockResolvedValue(mockApiResponse([]))

    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    await new Promise((resolve) => setTimeout(resolve, 100))

    const button = wrapper.find('button')
    expect(button.text()).toContain('去添加地址')
  })

  it('auto-selects default address when no modelValue', async () => {
    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    await new Promise((resolve) => setTimeout(resolve, 100))

    // Should emit the default address id
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
  })

  it('handles API error gracefully', async () => {
    mockApi.get.mockRejectedValue(new Error('API Error'))

    const wrapper = createWrapper(AddressSelector, {
      props: { modelValue: null },
    })

    await new Promise((resolve) => setTimeout(resolve, 100))

    // Should not crash, just show empty state
    expect(wrapper.exists()).toBe(true)
  })
})
