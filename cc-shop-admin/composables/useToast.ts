export function useAppToast() {
  const uiToast = useToast()

  type ToastColor = 'success' | 'error' | 'warning' | 'primary' | 'secondary' | 'neutral'

  function show(message: string, type: string = 'info') {
    const colorMap: Record<string, ToastColor> = {
      success: 'success',
      error: 'error',
      warning: 'warning',
      info: 'primary',
    }

    uiToast.add({
      title: message,
      color: colorMap[type] || 'primary',
    })
  }

  return {
    show,
    success: (msg: string) => show(msg, 'success'),
    error: (msg: string) => show(msg, 'error'),
    warning: (msg: string) => show(msg, 'warning'),
    info: (msg: string) => show(msg, 'info'),
  }
}
