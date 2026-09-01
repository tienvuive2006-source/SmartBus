const NOTICE_KEY = 'route_access_notice'
export const ROUTE_ACCESS_NOTICE_EVENT = 'route-access-notice'

export const queueRouteAccessNotice = message => {
  const notice = { message, createdAt: Date.now() }
  sessionStorage.setItem(NOTICE_KEY, JSON.stringify(notice))
  window.dispatchEvent(new CustomEvent(ROUTE_ACCESS_NOTICE_EVENT, { detail: notice }))
}

export const consumeRouteAccessNotice = () => {
  const stored = sessionStorage.getItem(NOTICE_KEY)
  sessionStorage.removeItem(NOTICE_KEY)
  if (!stored) return null

  try {
    const notice = JSON.parse(stored)
    return Date.now() - Number(notice.createdAt || 0) < 10_000 ? notice : null
  } catch {
    return null
  }
}
