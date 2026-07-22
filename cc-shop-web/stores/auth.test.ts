import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
vi.stubGlobal('localStorage', localStorageMock)

// Mock import.meta.client
vi.stubGlobal('import', { meta: { client: true } })

// Mock ref and computed from Vue
vi.stubGlobal('ref', vi.fn((val) => ({ value: val })))
vi.stubGlobal('computed', vi.fn((fn) => ({ value: fn() })))

// Mock defineStore from Pinia
const mockStore = vi.fn()
vi.stubGlobal('defineStore', vi.fn((name, setup) => () => {
  const result = setup()
  mockStore(result)
  return result
}))

// Import after mocks
const { useAuthStore } = await import('./auth')

describe('useAuthStore', () => {
  let store: ReturnType<typeof useAuthStore>

  beforeEach(() => {
    vi.clearAllMocks()
    localStorageMock.getItem.mockReturnValue(null)
    store = useAuthStore()
  })

  describe('initial state', () => {
    it('has empty token by default', () => {
      expect(store.token.value).toBe('')
    })

    it('has null userId by default', () => {
      expect(store.userId.value).toBeNull()
    })

    it('has empty username by default', () => {
      expect(store.username.value).toBe('')
    })

    it('is not logged in by default', () => {
      expect(store.isLoggedIn.value).toBe(false)
    })
  })

  describe('setAuth', () => {
    it('sets token', () => {
      store.setAuth('test-token', 1, 'testuser')

      expect(store.token.value).toBe('test-token')
    })

    it('sets userId', () => {
      store.setAuth('test-token', 1, 'testuser')

      expect(store.userId.value).toBe(1)
    })

    it('sets username', () => {
      store.setAuth('test-token', 1, 'testuser')

      expect(store.username.value).toBe('testuser')
    })

    it('saves to localStorage', () => {
      store.setAuth('test-token', 1, 'testuser')

      expect(localStorageMock.setItem).toHaveBeenCalledWith('cc_token', 'test-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('cc_uid', '1')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('cc_username', 'testuser')
    })

    it('updates isLoggedIn', () => {
      store.setAuth('test-token', 1, 'testuser')

      expect(store.isLoggedIn.value).toBe(true)
    })
  })

  describe('logout', () => {
    beforeEach(() => {
      store.setAuth('test-token', 1, 'testuser')
    })

    it('clears token', () => {
      store.logout()

      expect(store.token.value).toBe('')
    })

    it('clears userId', () => {
      store.logout()

      expect(store.userId.value).toBeNull()
    })

    it('clears username', () => {
      store.logout()

      expect(store.username.value).toBe('')
    })

    it('removes from localStorage', () => {
      store.logout()

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('cc_token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('cc_uid')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('cc_username')
    })

    it('updates isLoggedIn', () => {
      store.logout()

      expect(store.isLoggedIn.value).toBe(false)
    })
  })

  describe('initFromStorage', () => {
    it('loads from localStorage when data exists', () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'cc_token') return 'saved-token'
        if (key === 'cc_uid') return '42'
        if (key === 'cc_username') return 'saveduser'
        return null
      })

      store.initFromStorage()

      expect(store.token.value).toBe('saved-token')
      expect(store.userId.value).toBe(42)
      expect(store.username.value).toBe('saveduser')
    })

    it('does not change state when no saved data', () => {
      localStorageMock.getItem.mockReturnValue(null)

      store.initFromStorage()

      expect(store.token.value).toBe('')
      expect(store.userId.value).toBeNull()
      expect(store.username.value).toBe('')
    })

    it('handles missing userId in storage', () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'cc_token') return 'saved-token'
        if (key === 'cc_username') return 'saveduser'
        return null
      })

      store.initFromStorage()

      expect(store.token.value).toBe('saved-token')
      expect(store.userId.value).toBeNull()
      expect(store.username.value).toBe('saveduser')
    })

    it('handles missing username in storage', () => {
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'cc_token') return 'saved-token'
        if (key === 'cc_uid') return '42'
        return null
      })

      store.initFromStorage()

      expect(store.token.value).toBe('saved-token')
      expect(store.userId.value).toBe(42)
      expect(store.username.value).toBe('')
    })
  })
})
