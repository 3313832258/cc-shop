import { describe, it, expect } from 'vitest'
import { createWrapper } from '../test/utils'
import ProductCard from './ProductCard.vue'

describe('ProductCard', () => {
  const defaultProduct = {
    id: 1,
    name: '测试商品',
    image: 'https://example.com/image.jpg',
    price: 99.99,
    stock: 100,
  }

  it('renders product name correctly', () => {
    const wrapper = createWrapper(ProductCard, {
      props: { product: defaultProduct },
    })

    expect(wrapper.text()).toContain('测试商品')
  })

  it('renders product price correctly', () => {
    const wrapper = createWrapper(ProductCard, {
      props: { product: defaultProduct },
    })

    expect(wrapper.text()).toContain('¥99.99')
  })

  it('renders product stock correctly', () => {
    const wrapper = createWrapper(ProductCard, {
      props: { product: defaultProduct },
    })

    expect(wrapper.text()).toContain('库存 100')
  })

  it('renders product image with correct src', () => {
    const wrapper = createWrapper(ProductCard, {
      props: { product: defaultProduct },
    })

    const img = wrapper.find('img')
    expect(img.attributes('src')).toBe('https://example.com/image.jpg')
    expect(img.attributes('alt')).toBe('测试商品')
  })

  it('renders placeholder image when no image provided', () => {
    const productWithoutImage = { ...defaultProduct, image: undefined }
    const wrapper = createWrapper(ProductCard, {
      props: { product: productWithoutImage },
    })

    const img = wrapper.find('img')
    expect(img.attributes('src')).toContain('picsum.photos')
  })

  it('links to product detail page', () => {
    const wrapper = createWrapper(ProductCard, {
      props: { product: defaultProduct },
    })

    const link = wrapper.find('a')
    expect(link.attributes('href')).toBe('/product/1')
  })

  it('formats price with two decimal places', () => {
    const productWithIntegerPrice = { ...defaultProduct, price: 100 }
    const wrapper = createWrapper(ProductCard, {
      props: { product: productWithIntegerPrice },
    })

    expect(wrapper.text()).toContain('¥100.00')
  })

  it('handles zero price', () => {
    const freeProduct = { ...defaultProduct, price: 0 }
    const wrapper = createWrapper(ProductCard, {
      props: { product: freeProduct },
    })

    expect(wrapper.text()).toContain('¥0.00')
  })

  it('handles undefined price', () => {
    const productWithoutPrice = { ...defaultProduct, price: undefined }
    const wrapper = createWrapper(ProductCard, {
      props: { product: productWithoutPrice },
    })

    expect(wrapper.text()).toContain('¥0.00')
  })

  it('handles undefined stock', () => {
    const productWithoutStock = { ...defaultProduct, stock: undefined }
    const wrapper = createWrapper(ProductCard, {
      props: { product: productWithoutStock },
    })

    // When stock is undefined, the template shows "库存" followed by undefined
    // which renders as just "库存" in the text
    expect(wrapper.text()).toContain('库存')
  })
})
