import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createWrapper, mockApiResponse, createMockApi, createMockToast, createMockAuthStore } from '../../test/utils'
import LoginPage from './login.vue'

// Mock composables
const mockApi = createMockApi()
const mockToast = createMockToast()
const mockAuthStore = createMockAuthStore()

vi.mock('../../composables/useApi', () => ({
  useApi: () => mockApi,
}))

vi.mock('../../composables/useToast', () => ({
  useAppToast: () => mockToast,
}))

vi.mock('../../stores/auth', () => ({
  useAuthStore: () => mockAuthStore,
}))

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders login form', () => {
    const wrapper = createWrapper(LoginPage)

    expect(wrapper.text()).toContain('登录')
    expect(wrapper.text()).toContain('用户名')
    expect(wrapper.text()).toContain('密码')
  })

  it('renders username input', () => {
    const wrapper = createWrapper(LoginPage)

    const inputs = wrapper.findAll('input')
    expect(inputs.length).toBeGreaterThanOrEqual(2)
    expect(inputs[0].attributes('placeholder')).toBe('请输入用户名')
  })

  it('renders password input', () => {
    const wrapper = createWrapper(LoginPage)

    const inputs = wrapper.findAll('input')
    expect(inputs[1].attributes('type')).toBe('password')
    expect(inputs[1].attributes('placeholder')).toBe('请输入密码')
  })

  it('renders submit button', () => {
    const wrapper = createWrapper(LoginPage)

    const button = wrapper.find('button')
    expect(button.text()).toContain('登录')
  })

  it('renders register link', () => {
    const wrapper = createWrapper(LoginPage)

    expect(wrapper.text()).toContain('还没有账号？')
    expect(wrapper.text()).toContain('立即注册')
  })

  it('has link to register page', () => {
    const wrapper = createWrapper(LoginPage)

    const links = wrapper.findAll('a')
    const registerLink = links.find(link => link.attributes('href') === '/user/register')

    expect(registerLink).toBeTruthy()
  })

  it('submits login form with credentials', async () => {
    mockApi.post.mockResolvedValue(
      mockApiResponse({ token: 'test-token', userId: 1, username: 'testuser' })
    )

    const wrapper = createWrapper(LoginPage)

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('testuser')
    await inputs[1].setValue('password123')

    const form = wrapper.find('form')
    await form.trigger('submit')

    // Wait for async operation
    await new Promise((resolve) => setTimeout(resolve, 500))

    expect(mockApi.post).toHaveBeenCalledWith('/api/user/auth/login', {
      username: 'testuser',
      password: 'password123',
    })
  })

  it('handles login error gracefully', async () => {
    mockApi.post.mockRejectedValue(new Error('用户名或密码错误'))

    const wrapper = createWrapper(LoginPage)

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('testuser')
    await inputs[1].setValue('wrongpassword')

    const form = wrapper.find('form')
    await form.trigger('submit')

    // Wait for async operation
    await new Promise((resolve) => setTimeout(resolve, 500))

    // Should not call setAuth
    expect(mockAuthStore.setAuth).not.toHaveBeenCalled()
  })
})
