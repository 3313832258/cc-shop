/**
 * 用户行为埋点 SDK
 * 批量采集用户行为事件，定时发送到后端
 */

interface UserActionEvent {
  action: string
  userId: number | null
  targetType: string
  targetId?: number
  extra?: string
  timestamp: number
}

export function useTracker() {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()

  // 事件队列（批量发送）
  const eventQueue: UserActionEvent[] = []
  const BATCH_SIZE = 10
  const FLUSH_INTERVAL = 5000 // 5秒

  /**
   * 追踪用户行为
   * @param action 行为类型：view/click/cart/order/favorite/search
   * @param targetType 目标类型：product/order/coupon/category/keyword
   * @param targetId 目标ID（可选）
   * @param extra 扩展信息（可选）
   */
  function track(action: string, targetType: string, targetId?: number, extra?: any) {
    const event: UserActionEvent = {
      action,
      userId: authStore.userId,
      targetType,
      targetId,
      extra: extra ? JSON.stringify(extra) : undefined,
      timestamp: Date.now()
    }

    eventQueue.push(event)

    // 批量发送
    if (eventQueue.length >= BATCH_SIZE) {
      flush()
    }
  }

  /**
   * 批量发送事件到后端
   */
  async function flush() {
    if (eventQueue.length === 0) return

    const events = [...eventQueue]
    eventQueue.length = 0

    try {
      await $fetch('/api/user/action/batch', {
        method: 'POST',
        baseURL: config.public.apiBase,
        headers: authStore.token ? { Authorization: `Bearer ${authStore.token}` } : {},
        body: events
      })
    } catch (err) {
      // 埋点失败不影响用户体验，静默处理
      console.warn('[Tracker] 埋点上报失败:', err)
    }
  }

  // 定时刷新（仅在客户端）
  if (import.meta.client) {
    setInterval(flush, FLUSH_INTERVAL)
    // 页面关闭前刷新
    window.addEventListener('beforeunload', flush)
  }

  // ==================== 便捷方法 ====================

  /**
   * 浏览商品
   */
  function viewProduct(productId: number) {
    track('view', 'product', productId)
  }

  /**
   * 点击商品（SKU选择等）
   */
  function clickProduct(productId: number, skuId?: number) {
    track('click', 'product', productId, skuId ? { skuId } : undefined)
  }

  /**
   * 加入购物车
   */
  function addToCart(productId: number, skuId: number, quantity?: number) {
    track('cart', 'product', productId, { skuId, quantity })
  }

  /**
   * 下单
   */
  function placeOrder(orderId: number, totalAmount?: number) {
    track('order', 'order', orderId, totalAmount ? { totalAmount } : undefined)
  }

  /**
   * 收藏商品
   */
  function favoriteProduct(productId: number) {
    track('favorite', 'product', productId)
  }

  /**
   * 搜索
   */
  function search(keyword: string, resultCount?: number) {
    track('search', 'keyword', undefined, { keyword, resultCount })
  }

  /**
   * 领取优惠券
   */
  function receiveCoupon(couponId: number) {
    track('receive', 'coupon', couponId)
  }

  /**
   * 查看分类
   */
  function viewCategory(categoryId: number) {
    track('view', 'category', categoryId)
  }

  return {
    track,
    flush,
    viewProduct,
    clickProduct,
    addToCart,
    placeOrder,
    favoriteProduct,
    search,
    receiveCoupon,
    viewCategory
  }
}
