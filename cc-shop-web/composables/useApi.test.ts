import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock $fetch
const mockFetch = vi.fn()
vi.stubGlobal('$fetch', mockFetch)

// Mock navigateTo
const mockNavigateTo = vi.fn()
vi.stubGlobal('navigateTo', mockNavigateTo)

// Mock useRuntimeConfig
vi.stubGlobal('useRuntimeConfig', () => ({
  public: {
    apiBase: 'http://127.0.0.1:8080',
  },
}))

// Mock useToast
const mockToast = {
  show: vi.fn(),
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
  info: vi.fn(),
}
vi.stubGlobal('useToast', () => mockToast)

// Mock useAuthStore
const mockLogout = vi.fn()
vi.stubGlobal('useAuthStore', () => ({
  isLoggedIn: true,
  userId: 1,
  username: 'testuser',
  token: 'test-token',
  setAuth: vi.fn(),
  logout: mockLogout,
}))

// 导入被测试的模块
import { useApi } from './useApi'

describe('useApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('sends GET request with correct URL', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: 'test' })

    const api = useApi()
    await api.get('/api/test')

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][0]).toBe('/api/test')
    expect(mockFetch.mock.calls[0][1].method).toBe('GET')
  })

  it('sends GET request with query parameters', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: 'test' })

    const api = useApi()
    await api.get('/api/test', { page: 1, size: 10 })

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][0]).toBe('/api/test')
    expect(mockFetch.mock.calls[0][1].query).toEqual({ page: 1, size: 10 })
  })

  it('returns data on success', async () => {
    const responseData = { code: 200, data: { id: 1 } }
    mockFetch.mockResolvedValueOnce(responseData)

    const api = useApi()
    const result = await api.get('/api/test')

    expect(result).toEqual(responseData)
  })

  it('sends POST request with body', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: 'created' })

    const api = useApi()
    const body = { name: 'test' }
    await api.post('/api/test', body)

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][0]).toBe('/api/test')
    expect(mockFetch.mock.calls[0][1].method).toBe('POST')
    expect(mockFetch.mock.calls[0][1].body).toEqual(body)
  })

  it('sends POST request with body and query', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: 'created' })

    const api = useApi()
    const body = { name: 'test' }
    const query = { action: 'create' }
    await api.post('/api/test', body, query)

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][0]).toBe('/api/test')
    expect(mockFetch.mock.calls[0][1].method).toBe('POST')
    expect(mockFetch.mock.calls[0][1].body).toEqual(body)
    expect(mockFetch.mock.calls[0][1].query).toEqual(query)
  })

  it('sends PUT request with body', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: 'updated' })

    const api = useApi()
    const body = { name: 'updated' }
    await api.put('/api/test/1', body)

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][0]).toBe('/api/test/1')
    expect(mockFetch.mock.calls[0][1].method).toBe('PUT')
    expect(mockFetch.mock.calls[0][1].body).toEqual(body)
  })

  it('sends DELETE request', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: null })

    const api = useApi()
    await api.del('/api/test/1')

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][0]).toBe('/api/test/1')
    expect(mockFetch.mock.calls[0][1].method).toBe('DELETE')
  })

  it('includes authorization header', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: 'test' })

    const api = useApi()
    await api.get('/api/test')

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][1].headers).toHaveProperty('Authorization')
  })

  it('uses API base URL from config', async () => {
    mockFetch.mockResolvedValueOnce({ code: 200, data: 'test' })

    const api = useApi()
    await api.get('/api/test')

    expect(mockFetch).toHaveBeenCalledTimes(1)
    expect(mockFetch.mock.calls[0][1].baseURL).toBe('http://127.0.0.1:8080')
  })
})
