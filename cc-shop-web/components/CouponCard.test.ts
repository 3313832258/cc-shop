import { describe, it, expect } from 'vitest'
import { createWrapper } from '../test/utils'
import CouponCard from './CouponCard.vue'

describe('CouponCard', () => {
  const fixedCoupon = {
    id: 1,
    name: '满100减20',
    type: 0, // 满减
    value: 20,
    minOrderAmount: 100,
    endTime: '2026-12-31',
    status: 0,
  }

  const percentCoupon = {
    id: 2,
    name: '满200打8折',
    type: 1, // 折扣
    value: 0.8,
    minOrderAmount: 200,
    endTime: '2026-12-31',
    status: 0,
  }

  it('renders fixed coupon value correctly', () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: fixedCoupon, mode: 'available' },
    })

    expect(wrapper.text()).toContain('¥20')
    expect(wrapper.text()).toContain('满减券')
  })

  it('renders percent coupon value correctly', () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: percentCoupon, mode: 'available' },
    })

    expect(wrapper.text()).toContain('8.0折')
    expect(wrapper.text()).toContain('折扣券')
  })

  it('renders coupon name', () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: fixedCoupon, mode: 'available' },
    })

    expect(wrapper.text()).toContain('满100减20')
  })

  it('renders minimum order amount', () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: fixedCoupon, mode: 'available' },
    })

    expect(wrapper.text()).toContain('满 ¥100 可用')
  })

  it('renders end time', () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: fixedCoupon, mode: 'available' },
    })

    expect(wrapper.text()).toContain('有效期至 2026-12-31')
  })

  it('shows receive button in available mode', () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: fixedCoupon, mode: 'available' },
    })

    expect(wrapper.find('button').text()).toContain('立即领取')
  })

  it('emits receive event when button clicked', async () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: fixedCoupon, mode: 'available' },
    })

    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('receive')).toBeTruthy()
    expect(wrapper.emitted('receive')![0]).toEqual([1])
  })

  it('shows status badge in my mode', () => {
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: fixedCoupon, mode: 'my' },
    })

    expect(wrapper.text()).toContain('可使用')
  })

  it('shows used status correctly', () => {
    const usedCoupon = { ...fixedCoupon, status: 1 }
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: usedCoupon, mode: 'my' },
    })

    expect(wrapper.text()).toContain('已使用')
  })

  it('shows expired status correctly', () => {
    const expiredCoupon = { ...fixedCoupon, status: 2 }
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: expiredCoupon, mode: 'my' },
    })

    expect(wrapper.text()).toContain('已过期')
  })

  it('applies opacity for used coupons', () => {
    const usedCoupon = { ...fixedCoupon, status: 1 }
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: usedCoupon, mode: 'my' },
    })

    expect(wrapper.classes()).toContain('opacity-60')
  })

  it('handles missing end time', () => {
    const couponWithoutEndTime = { ...fixedCoupon, endTime: undefined }
    const wrapper = createWrapper(CouponCard, {
      props: { coupon: couponWithoutEndTime, mode: 'available' },
    })

    expect(wrapper.text()).not.toContain('有效期至')
  })
})
