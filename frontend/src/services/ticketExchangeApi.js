import { useApi } from '@/composables/useApi'

export function useTicketExchangeApi() {
  const api = useApi()

  return {
    getOptions(bookingId) {
      return api.get(`/ticket-exchanges/bookings/${bookingId}/options`)
    },
    getTripSeats(bookingId, tripId) {
      return api.get(`/ticket-exchanges/bookings/${bookingId}/trips/${tripId}/seats`)
    },
    exchange(bookingId, payload) {
      return api.post(`/ticket-exchanges/bookings/${bookingId}`, payload)
    },
    checkPayment(exchangeId) {
      return api.get(`/ticket-exchanges/payments/${exchangeId}/status`)
    }
  }
}
