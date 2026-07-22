import { mount, VueWrapper } from '@vue/test-utils'
import { vi } from 'vitest'
import { defineComponent, h } from 'vue'

/**
 * Create a wrapper for a component with common mocks
 */
export function createWrapper(component: any, options: any = {}) {
  return mount(component, {
    global: {
      stubs: {
        NuxtLink: defineComponent({
          name: 'NuxtLink',
          props: ['to'],
          setup(props, { slots }) {
            return () => h('a', { href: props.to }, slots.default?.())
          },
        }),
        UButton: defineComponent({
          name: 'UButton',
          props: ['color', 'variant', 'size', 'disabled', 'loading', 'to', 'icon', 'block'],
          setup(props, { slots }) {
            return () => h('button', {
              disabled: props.disabled,
              class: ['u-button', `color-${props.color}`, `variant-${props.variant}`],
            }, slots.default?.())
          },
        }),
        UInput: defineComponent({
          name: 'UInput',
          props: ['modelValue', 'placeholder', 'type', 'icon'],
          emits: ['update:modelValue'],
          setup(props, { emit, slots }) {
            return () => h('div', { class: 'u-input' }, [
              h('input', {
                value: props.modelValue,
                placeholder: props.placeholder,
                type: props.type || 'text',
                onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLInputElement).value),
              }),
            ])
          },
        }),
        UBadge: defineComponent({
          name: 'UBadge',
          props: ['color', 'variant', 'size'],
          setup(props, { slots }) {
            return () => h('span', { class: ['u-badge', `color-${props.color}`] }, slots.default?.())
          },
        }),
        UCard: defineComponent({
          name: 'UCard',
          setup(props, { slots }) {
            return () => h('div', { class: 'u-card' }, [
              slots.header && h('div', { class: 'u-card-header' }, slots.header()),
              h('div', { class: 'u-card-body' }, slots.default?.()),
            ])
          },
        }),
        UIcon: defineComponent({
          name: 'UIcon',
          props: ['name', 'size'],
          setup(props) {
            return () => h('span', { class: ['u-icon', props.name] })
          },
        }),
      },
      plugins: [],
    },
    ...options,
  })
}

/**
 * Mock API response
 */
export function mockApiResponse(data: any, code = 200) {
  return {
    code,
    message: code === 200 ? 'success' : 'error',
    data,
    timestamp: Date.now(),
  }
}

/**
 * Mock API error
 */
export function mockApiError(message = 'Network Error') {
  return new Error(message)
}

/**
 * Create a mock toast
 */
export function createMockToast() {
  return {
    show: vi.fn(),
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  }
}

/**
 * Create a mock API composable
 */
export function createMockApi() {
  return {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
  }
}

/**
 * Create a mock auth store
 */
export function createMockAuthStore(overrides = {}) {
  return {
    isLoggedIn: false,
    userId: null,
    username: null,
    token: null,
    setAuth: vi.fn(),
    logout: vi.fn(),
    initFromStorage: vi.fn(),
    ...overrides,
  }
}

/**
 * Wait for next tick
 */
export async function nextTick() {
  return new Promise((resolve) => setTimeout(resolve, 0))
}

/**
 * Flush promises
 */
export async function flushPromises() {
  return new Promise((resolve) => setTimeout(resolve, 0))
}
