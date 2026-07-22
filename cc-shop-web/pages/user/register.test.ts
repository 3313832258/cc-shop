import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createWrapper, mockApiResponse, createMockApi, createMockToast } from '../../test/utils'
import RegisterPage from './register.vue'

// Mock composables
const mockApi = createMockApi()
const mockToast = createMockToast()

vi.mock('../../composables/useApi', () => ({
  useApi: () => mockApi,
}))

vi.mock('../../composables/useToast', () => ({
  useAppToast: () => mockToast,
}))

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders register form', () => {
    const wrapper = createWrapper(RegisterPage)

    expect(wrapper.text()).toContain('注册')
    expect(wrapper.text()).toContain('用户名')
    expect(wrapper.text()).toContain('密码')
    expect(wrapper.text()).toContain('手机号')
    expect(wrapper.text()).toContain('邮箱')
  })

  it('renders username input', () => {
    const wrapper = createWrapper(RegisterPage)

    const inputs = wrapper.findAll('input')
    expect(inputs[0].attributes('placeholder')).toBe('3-20位字符')
  })

  it('renders password input', () => {
    const wrapper = createWrapper(RegisterPage)

    const inputs = wrapper.findAll('input')
    expect(inputs[1].attributes('type')).toBe('password')
    expect(inputs[1].attributes('placeholder')).toBe('至少6位')
  })

  it('renders phone input', () => {
    const wrapper = createWrapper(RegisterPage)

    const inputs = wrapper.findAll('input')
    expect(inputs[2].attributes('placeholder')).toBe('选填')
  })

  it('renders email input', () => {
    const wrapper = createWrapper(RegisterPage)

    const inputs = wrapper.findAll('input')
    expect(inputs[3].attributes('placeholder')).toBe('选填')
  })

  it('renders submit button', () => {
    const wrapper = createWrapper(RegisterPage)

    const button = wrapper.find('button')
    expect(button.text()).toContain('注册')
  })

  it('renders login link', () => {
    const wrapper = createWrapper(RegisterPage)

    expect(wrapper.text()).toContain('已有账号？')
    expect(wrapper.text()).toContain('去登录')
  })

  it('has link to login page', () => {
    const wrapper = createWrapper(RegisterPage)

    const links = wrapper.findAll('a')
    const loginLink = links.find(link => link.attributes('href') === '/user/login')

    expect(loginLink).toBeTruthy()
  })

  it('submits register form with data', async () => {
    mockApi.post.mockResolvedValue(mockApiResponse(null))

    const wrapper = createWrapper(RegisterPage)

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('newuser')
    await inputs[1].setValue('password123')
    await inputs[2].setValue('13800138000')
    await inputs[3].setValue('test@example.com')

    const form = wrapper.find('form')
    await form.trigger('submit')

    // Wait for async operation
    await new Promise((resolve) => setTimeout(resolve, 500))

    expect(mockApi.post).toHaveBeenCalledWith('/api/user/auth/register', {
      username: 'newuser',
      password: 'password123',
      phone: '13800138000',
      email: 'test@example.com',
    })
  })

  it('handles register error gracefully', async () => {
    mockApi.post.mockRejectedValue(new Error('用户名已存在'))

    const wrapper = createWrapper(RegisterPage)

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('existinguser')
    await inputs[1].setValue('password123')

    const form = wrapper.find('form')
    await form.trigger('submit')

    // Wait for async operation
    await new Promise((resolve) => setTimeout(resolve, 500))

    // Should not show success toast
    expect(mockToast.success).not.toHaveBeenCalled()
  })
})
