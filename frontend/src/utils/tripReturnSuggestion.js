const normalizePoint = value => String(value || '')
  .trim()
  .toLocaleLowerCase('vi-VN')
  .replace(/\s+/g, ' ')

const toMinutes = (date, time) => {
  if (!date || !time) return 0
  const [year, month, day] = date.split('-').map(Number)
  const [hour, minute] = time.split(':').map(Number)
  return new Date(year, month - 1, day, hour, minute).getTime() / 60000
}

const arrivalMinutes = trip => {
  const departure = toMinutes(trip?.departureDate, trip?.departureTime)
  let arrival = toMinutes(trip?.departureDate, trip?.arrivalTime)
  if (!arrival) return departure
  if (arrival <= departure) arrival += 1440
  return arrival
}

const isReverseTrip = (outbound, candidate) =>
  normalizePoint(outbound?.departurePoint) === normalizePoint(candidate?.arrivalPoint)
  && normalizePoint(outbound?.arrivalPoint) === normalizePoint(candidate?.departurePoint)
  && outbound?.busType === candidate?.busType

export const findSuggestedReturnTrip = (trip, allTrips = []) => {
  if (!trip?.id) return null

  const outboundArrival = arrivalMinutes(trip)
  const latestReturn = outboundArrival + (14 * 1440)

  // Không dựa vào ID liền kề: khi nhiều chuyến được tạo gần nhau, chuyến khứ hồi
  // có thể không mang ID ngay sau chuyến đi. Chọn chuyến ngược chiều gần nhất
  // sau giờ đến, cùng dòng xe và chưa được phân công.
  return allTrips
    .filter(candidate => Number(candidate?.id) !== Number(trip.id))
    .filter(candidate => isReverseTrip(trip, candidate))
    .filter(candidate => ['PENDING', 'ASSIGNED'].includes(candidate.status))
    .filter(candidate => !candidate.assignedDriverUsername && !candidate.assignedLicensePlate)
    .map(candidate => ({
      candidate,
      departure: toMinutes(candidate.departureDate, candidate.departureTime)
    }))
    .filter(({ departure }) => departure >= outboundArrival && departure <= latestReturn)
    .sort((left, right) => left.departure - right.departure || Number(left.candidate.id) - Number(right.candidate.id))[0]
    ?.candidate || null
}
