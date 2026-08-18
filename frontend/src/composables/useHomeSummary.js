import { useApi } from '@/composables/useApi'

const CACHE_TTL = 5 * 60 * 1000
let cachedSummary = null
let cachedAt = 0
let pendingRequest = null

export function useHomeSummary() {
  const api = useApi()

  const getHomeSummary = async (force = false) => {
    const cacheIsFresh = cachedSummary && Date.now() - cachedAt < CACHE_TTL
    if (!force && cacheIsFresh) return cachedSummary
    if (!force && pendingRequest) return pendingRequest

    pendingRequest = api.get('/trips/home-summary')
      .then(response => {
        cachedSummary = response.data || {}
        cachedAt = Date.now()
        return cachedSummary
      })
      .finally(() => {
        pendingRequest = null
      })

    return pendingRequest
  }

  return { getHomeSummary }
}
