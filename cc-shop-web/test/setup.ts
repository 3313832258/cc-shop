import { vi, beforeEach } from 'vitest'
import { config } from '@vue/test-utils'

// Mock #app module
vi.mock('#app', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    useRuntimeConfig: () => ({
      public: {
        apiBase: 'http://127.0.0.1:8080',
      },
    }),
    navigateTo: vi.fn(),
    useRoute: () => ({
      query: {},
      params: {},
      path: '/',
    }),
    useRouter: () => ({
      push: vi.fn(),
      replace: vi.fn(),
    }),
    definePageMeta: vi.fn(),
    defineNuxtPlugin: vi.fn((plugin) => plugin),
    defineNuxtRouteMiddleware: vi.fn((middleware) => middleware),
    useNuxtApp: () => ({
      $pinia: {},
      $router: {
        push: vi.fn(),
        replace: vi.fn(),
      },
    }),
  }
})

// Mock Pinia
vi.mock('pinia', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    defineStore: vi.fn((name, setup) => {
      return () => {
        if (typeof setup === 'function') {
          return setup()
        }
        return setup
      }
    }),
  }
})

// Global test utilities
config.global.mocks = {
  $t: (key: string) => key,
}

// Reset mocks before each test
beforeEach(() => {
  vi.clearAllMocks()
})
