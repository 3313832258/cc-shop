import { describe, it, expect } from 'vitest'
import { createWrapper } from '../test/utils'
import AppFooter from './AppFooter.vue'

describe('AppFooter', () => {
  it('renders copyright text', () => {
    const wrapper = createWrapper(AppFooter)

    expect(wrapper.text()).toContain('© 2026 CC-Shop')
    expect(wrapper.text()).toContain('简历/学习项目')
  })

  it('renders tech stack', () => {
    const wrapper = createWrapper(AppFooter)

    expect(wrapper.text()).toContain('Nuxt3')
    expect(wrapper.text()).toContain('Spring Cloud')
    expect(wrapper.text()).toContain('Nacos')
    expect(wrapper.text()).toContain('Seata')
    expect(wrapper.text()).toContain('Sentinel')
  })

  it('renders navigation links', () => {
    const wrapper = createWrapper(AppFooter)

    const links = wrapper.findAll('a')
    const hrefs = links.map(link => link.attributes('href'))

    expect(hrefs).toContain('/')
    expect(hrefs).toContain('/product/list')
  })

  it('has correct link text', () => {
    const wrapper = createWrapper(AppFooter)

    expect(wrapper.text()).toContain('首页')
    expect(wrapper.text()).toContain('商品')
  })
})
