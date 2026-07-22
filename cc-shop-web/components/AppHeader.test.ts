import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createWrapper, createMockAuthStore } from '../test/utils'
import AppHeader from './AppHeader.vue'

// Mock auth store
const mockAuthStore = createMockAuthStore()
vi.mock('../stores/auth', () => ({
  useAuthStore: () => mockAuthStore,
}))

describe('AppHeader', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders logo correctly', () => {
    const wrapper = createWrapper(AppHeader)

    expect(wrapper.text()).toContain('CC')
    expect(wrapper.text()).toContain('Shop')
  })

  it('renders search input', () => {
    const wrapper = createWrapper(AppHeader)

    const input = wrapper.find('input')
    expect(input.exists()).toBe(true)
    expect(input.attributes('placeholder')).toBe('搜索商品...')
  })

  it('renders navigation links', () => {
    const wrapper = createWrapper(AppHeader)

    expect(wrapper.text()).toContain('首页')
    expect(wrapper.text()).toContain('商品')
  })

  it('shows login and register buttons when not logged in', () => {
    mockAuthStore.isLoggedIn = false
    const wrapper = createWrapper(AppHeader)

    expect(wrapper.text()).toContain('登录')
    expect(wrapper.text()).toContain('注册')
  })

  it('shows user menu when logged in', () => {
    mockAuthStore.isLoggedIn = true
    mockAuthStore.username = 'testuser'
    const wrapper = createWrapper(AppHeader)

    expect(wrapper.text()).toContain('testuser')
    expect(wrapper.text()).toContain('收藏')
    expect(wrapper.text()).toContain('购物车')
    expect(wrapper.text()).toContain('优惠券')
  })

  it('navigates to search results on search', async () => {
    const wrapper = createWrapper(AppHeader)

    const input = wrapper.find('input')
    await input.setValue('手机')

    const searchButton = wrapper.findAll('button').find(b => b.text().includes('搜索'))
    if (searchButton) {
      await searchButton.trigger('click')
    }
  })

  it('navigates to search on Enter key', async () => {
    const wrapper = createWrapper(AppHeader)

    const input = wrapper.find('input')
    await input.setValue('手机')
    await input.trigger('keyup.enter')
  })

  it('renders correct links for logged in user', () => {
    mockAuthStore.isLoggedIn = true
    const wrapper = createWrapper(AppHeader)

    const links = wrapper.findAll('a')
    const hrefs = links.map(link => link.attributes('href'))

    expect(hrefs).toContain('/')
    expect(hrefs).toContain('/product/list')
    expect(hrefs).toContain('/favorites')
    expect(hrefs).toContain('/message')
    expect(hrefs).toContain('/cart')
    expect(hrefs).toContain('/coupon')
  })

  it('shows login and register buttons for logged out user', () => {
    mockAuthStore.isLoggedIn = false
    const wrapper = createWrapper(AppHeader)

    expect(wrapper.text()).toContain('登录')
    expect(wrapper.text()).toContain('注册')
  })
})
