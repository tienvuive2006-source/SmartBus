import { useApi } from '@/composables/useApi'

export function useRouteStopApi() {
  const api = useApi()
  return {
    getRouteStops: routeId => api.get(`/route-stops/routes/${routeId}`),
    saveRouteStops: (routeId, stops) => api.put(`/route-stops/routes/${routeId}`, stops),
    getTripStops: tripId => api.get(`/route-stops/trips/${tripId}`),
    getDriverStopPassengers: tripId => api.get(`/route-stops/driver/trips/${tripId}/passengers`),
    getMyBookingSelections: () => api.get('/route-stops/me/bookings'),
    getAdminBookingSelections: bookingIds => api.get('/route-stops/admin/bookings', { params: { ids: bookingIds.join(',') } })
  }
}
