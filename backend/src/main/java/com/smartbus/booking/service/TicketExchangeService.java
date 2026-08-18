package com.smartbus.booking.service;

import com.smartbus.booking.dto.TicketExchangeRequest;
import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.entity.BookingExchange;
import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.entity.SeatReservation;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.BookingExchangeRepository;
import com.smartbus.booking.repository.BookingRepository;
import com.smartbus.booking.repository.TicketExchangeSeatRepository;
import com.smartbus.booking.repository.TicketExchangeReservationRepository;
import com.smartbus.booking.repository.TicketExchangeTripRepository;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketExchangeService {

    private static final int MINIMUM_HOURS_BEFORE_DEPARTURE = 12;
    private static final int MAX_EXCHANGES_PER_BOOKING = 1;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final BookingRepository bookingRepository;
    private final BookingExchangeRepository bookingExchangeRepository;
    private final TicketExchangeSeatRepository seatRepository;
    private final TicketExchangeReservationRepository reservationRepository;
    private final TicketExchangeTripRepository tripRepository;
    private final UserRepository userRepository;
    private final FundService fundService;
    private final TicketExchangeEmailService ticketExchangeEmailService;
    private final TicketExchangePaymentVerifier paymentVerifier;

    @Transactional(readOnly = true)
    public Map<String, Object> getOptions(Long bookingId, Long userId) {
        Booking booking = getOwnedBooking(bookingId, userId);
        validateEligibility(booking);

        Trip currentTrip = booking.getTrip();
        Set<String> currentSeatNumbers = new HashSet<>(booking.getSeatNumbers());
        Set<String> heldSeatNumbers = getHeldSeatNumbers(currentTrip.getId());
        List<Map<String, Object>> currentTripSeats = seatRepository
                .findByTripIdOrderBySeatNumberAsc(currentTrip.getId())
                .stream()
                .map(seat -> toSeatOption(seat, currentSeatNumbers, heldSeatNumbers))
                .toList();

        List<Map<String, Object>> alternativeTrips = tripRepository.findExchangeCandidates(
                        currentTrip.getDeparturePoint(),
                        currentTrip.getArrivalPoint(),
                        LocalDate.now(BUSINESS_ZONE).toString(),
                        currentTrip.getId())
                .stream()
                .filter(trip -> trip.getAvailableSeats() != null && trip.getAvailableSeats() >= booking.getSeatNumbers().size())
                .map(this::toTripOption)
                .toList();

        User user = booking.getUser();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("booking", toBookingOption(booking));
        response.put("currentTripSeats", currentTripSeats);
        response.put("alternativeTrips", alternativeTrips);
        response.put("walletBalance", user.getWalletBalance() == null ? 0.0 : user.getWalletBalance());
        response.put("minimumHoursBeforeDeparture", MINIMUM_HOURS_BEFORE_DEPARTURE);
        response.put("maxExchanges", MAX_EXCHANGES_PER_BOOKING);
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTripSeats(Long bookingId, Long tripId, Long userId) {
        Booking booking = getOwnedBooking(bookingId, userId);
        validateEligibility(booking);
        Trip targetTrip = getValidTargetTrip(booking, tripId);
        Set<String> currentSeats = targetTrip.getId().equals(booking.getTrip().getId())
                ? new HashSet<>(booking.getSeatNumbers())
                : Collections.emptySet();
        Set<String> heldSeats = getHeldSeatNumbers(targetTrip.getId());
        return seatRepository.findByTripIdOrderBySeatNumberAsc(targetTrip.getId())
                .stream()
                .map(seat -> toSeatOption(seat, currentSeats, heldSeats))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAdminExchangeHistory() {
        return bookingExchangeRepository.findAll().stream()
                .filter(exchange -> !"EXPIRED".equals(exchange.getStatus()))
                .sorted(Comparator.comparing(BookingExchange::getExchangedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toAdminExchangeItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyCompletedExchanges(Long userId) {
        return bookingExchangeRepository
                .findByBookingUserIdAndStatusOrderByExchangedAtDesc(userId, "COMPLETED")
                .stream()
                .map(this::toCustomerExchangeItem)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> exchange(Long bookingId, Long userId, TicketExchangeRequest request) {
        Booking booking = getOwnedBooking(bookingId, userId);
        validateEligibility(booking);
        validateRequest(request, booking);

        Trip oldTrip = booking.getTrip();
        Trip newTrip = getValidTargetTrip(booking, request.getNewTripId());
        List<String> oldSeats = new ArrayList<>(booking.getSeatNumbers());
        List<String> newSeats = request.getNewSeatNumbers().stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .toList();

        if (newSeats.size() != oldSeats.size()) {
            throw new IllegalArgumentException("Số lượng ghế mới phải bằng số lượng ghế trên vé hiện tại.");
        }
        if (oldTrip.getId().equals(newTrip.getId()) && new HashSet<>(oldSeats).equals(new HashSet<>(newSeats))) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế khác ghế hiện tại.");
        }

        double oldPrice = safeAmount(booking.getTotalPrice());
        double discount = safeAmount(booking.getDiscountAmount());
        boolean sameTrip = oldTrip.getId().equals(newTrip.getId());
        double newPrice = sameTrip
                ? oldPrice
                : Math.max(0.0, safeAmount(newTrip.getPrice()) * newSeats.size() - discount);
        double difference = newPrice - oldPrice;
        User user = booking.getUser();

        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod(), difference);
        if (difference > 0 && "QR".equals(paymentMethod)) {
            return createQrPaymentSession(booking, oldTrip, newTrip, oldSeats, newSeats,
                    oldPrice, newPrice, difference, request.getReason());
        }

        BookingExchange history = buildExchangeHistory(
                booking, oldTrip, newTrip, oldSeats, newSeats,
                oldPrice, newPrice, difference, request.getReason(), paymentMethod, "COMPLETED");
        return completeExchange(booking, oldTrip, newTrip, oldSeats, newSeats,
                oldPrice, newPrice, difference, paymentMethod, history, Collections.emptySet());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkQrPayment(Long exchangeId, Long userId) {
        BookingExchange pending = bookingExchangeRepository.findByIdAndBookingUserId(exchangeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên thanh toán đổi vé."));

        if ("COMPLETED".equals(pending.getStatus())) {
            Map<String, Object> completed = new LinkedHashMap<>();
            completed.put("paid", true);
            completed.put("message", "Giao dịch đổi vé đã hoàn tất.");
            return completed;
        }
        if (!"PENDING_PAYMENT".equals(pending.getStatus())) {
            throw new IllegalArgumentException("Phiên thanh toán không còn hiệu lực.");
        }
        if (pending.getExpiresAt() == null || LocalDateTime.now(BUSINESS_ZONE).isAfter(pending.getExpiresAt())) {
            expirePendingExchange(pending);
            Map<String, Object> expired = new LinkedHashMap<>();
            expired.put("paid", false);
            expired.put("status", "EXPIRED");
            expired.put("message", "Phiên thanh toán đã hết hạn.");
            return expired;
        }

        boolean paid = paymentVerifier.hasMatchingTransfer(
                pending.getPaymentCode(), pending.getPriceDifference(), pending.getExchangedAt());
        if (!paid) {
            Map<String, Object> waiting = new LinkedHashMap<>();
            waiting.put("paid", false);
            waiting.put("status", "PENDING_PAYMENT");
            return waiting;
        }

        Booking booking = getOwnedBooking(pending.getBooking().getId(), userId);
        validatePendingBookingUnchanged(booking, pending);
        Trip oldTrip = booking.getTrip();
        Trip newTrip = getValidTargetTrip(booking, pending.getNewTripId());
        List<String> oldSeats = splitSeatNumbers(pending.getOldSeatNumbers());
        List<String> newSeats = splitSeatNumbers(pending.getNewSeatNumbers());

        Map<String, Object> result = completeExchange(
                booking, oldTrip, newTrip, oldSeats, newSeats,
                pending.getOldPrice(), pending.getNewPrice(), pending.getPriceDifference(),
                "QR", pending, new HashSet<>(newSeats));
        result.put("paid", true);
        return result;
    }

    private Map<String, Object> createQrPaymentSession(
            Booking booking, Trip oldTrip, Trip newTrip,
            List<String> oldSeats, List<String> newSeats,
            double oldPrice, double newPrice, double difference, String reason) {
        expirePreviousPendingExchanges(booking.getId());
        validateNewSeatsAvailable(oldTrip, newTrip, oldSeats, newSeats);

        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        LocalDateTime expiresAt = now.plusMinutes(10);
        String paymentCode = "DX" + booking.getId()
                + now.format(java.time.format.DateTimeFormatter.ofPattern("MMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BookingExchange pending = buildExchangeHistory(
                booking, oldTrip, newTrip, oldSeats, newSeats,
                oldPrice, newPrice, difference, reason, "QR", "PENDING_PAYMENT");
        pending.setPaymentCode(paymentCode);
        pending.setExpiresAt(expiresAt);
        pending = bookingExchangeRepository.save(pending);

        List<Long> reservationIds = new ArrayList<>();
        for (String seatNumber : newSeats) {
            SeatReservation reservation = reservationRepository.save(SeatReservation.builder()
                    .tripId(newTrip.getId())
                    .seatNumber(seatNumber)
                    .createdAt(now)
                    .expiredAt(expiresAt)
                    .build());
            reservationIds.add(reservation.getId());
        }
        pending.setReservationIds(reservationIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        bookingExchangeRepository.save(pending);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentRequired", true);
        response.put("exchangeId", pending.getId());
        response.put("paymentCode", paymentCode);
        response.put("amount", difference);
        response.put("expiresAt", expiresAt.toString());
        response.put("message", "Đã giữ ghế mới trong 10 phút. Vui lòng hoàn tất chuyển khoản.");
        return response;
    }

    private Map<String, Object> completeExchange(
            Booking booking, Trip oldTrip, Trip newTrip,
            List<String> oldSeats, List<String> newSeats,
            double oldPrice, double newPrice, double difference,
            String paymentMethod, BookingExchange history, Set<String> allowedHeldSeats) {
        lockNewSeatsAndReleaseOldSeats(oldTrip, newTrip, oldSeats, newSeats, allowedHeldSeats);
        User user = booking.getUser();
        double currentBalance = safeAmount(user.getWalletBalance());

        if (difference > 0 && "WALLET".equals(paymentMethod)) {
            if (currentBalance < difference) {
                throw new IllegalArgumentException("Số dư ví không đủ. Vui lòng nạp thêm " + formatAmount(difference - currentBalance) + "đ.");
            }
            user.setWalletBalance(currentBalance - difference);
            fundService.recordTransaction("WALLET", "INCOME", difference,
                    "Thu chênh lệch đổi vé #" + booking.getId(), String.valueOf(booking.getId()), user.getFullName());
        } else if (difference > 0 && "QR".equals(paymentMethod)) {
            fundService.recordTransaction("BANK_TRANSFER", "INCOME", difference,
                    "Thu chênh lệch đổi vé #" + booking.getId(), "EXCHANGE-" + history.getId(), user.getFullName());
        } else if (difference < 0) {
            user.setWalletBalance(currentBalance + Math.abs(difference));
            fundService.recordTransaction("WALLET", "EXPENSE", Math.abs(difference),
                    "Hoàn chênh lệch đổi vé #" + booking.getId(), String.valueOf(booking.getId()), user.getFullName());
        }

        int oldPoints = LoyaltyPointPolicy.pointsFor(oldPrice);
        int newPoints = LoyaltyPointPolicy.pointsFor(newPrice);
        int loyaltyPoints = user.getLoyaltyPoints() == null ? 0 : user.getLoyaltyPoints();
        user.setLoyaltyPoints(Math.max(0, loyaltyPoints + newPoints - oldPoints));
        userRepository.save(user);

        booking.setTrip(newTrip);
        booking.setSeatNumbers(new ArrayList<>(newSeats));
        booking.setTotalPrice(newPrice);
        bookingRepository.save(booking);

        history.setStatus("COMPLETED");
        history.setPaymentMethod(paymentMethod);
        history.setExchangedAt(LocalDateTime.now(BUSINESS_ZONE));
        releaseReservations(history);
        bookingExchangeRepository.save(history);

        ticketExchangeEmailService.sendExchangeConfirmation(
                booking,
                oldTrip.getId(),
                String.join(", ", oldSeats),
                oldPrice,
                newPrice,
                difference);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentRequired", false);
        response.put("message", oldTrip.getId().equals(newTrip.getId())
                ? "Đổi ghế thành công."
                : "Đổi ngày/chuyến thành công.");
        response.put("bookingId", booking.getId());
        response.put("newTripId", newTrip.getId());
        response.put("newSeatNumbers", newSeats);
        response.put("newTotalPrice", newPrice);
        response.put("priceDifference", difference);
        response.put("paymentMethod", paymentMethod);
        response.put("walletBalance", user.getWalletBalance());
        return response;
    }

    private BookingExchange buildExchangeHistory(
            Booking booking, Trip oldTrip, Trip newTrip,
            List<String> oldSeats, List<String> newSeats,
            double oldPrice, double newPrice, double difference,
            String reason, String paymentMethod, String status) {
        return BookingExchange.builder()
                .booking(booking)
                .exchangeType(oldTrip.getId().equals(newTrip.getId()) ? "SEAT" : "TRIP")
                .oldTripId(oldTrip.getId())
                .newTripId(newTrip.getId())
                .oldSeatNumbers(String.join(", ", oldSeats))
                .newSeatNumbers(String.join(", ", newSeats))
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .priceDifference(difference)
                .reason(reason == null ? "" : reason.trim())
                .status(status)
                .paymentMethod(paymentMethod)
                .exchangedAt(LocalDateTime.now(BUSINESS_ZONE))
                .build();
    }

    private Booking getOwnedBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé."));
        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền đổi vé này.");
        }
        return booking;
    }

    private void validateEligibility(Booking booking) {
        if (!"PAID".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Chỉ vé đã thanh toán mới được phép đổi.");
        }
        if (bookingExchangeRepository.existsByBookingIdAndStatus(booking.getId(), "COMPLETED")) {
            throw new IllegalArgumentException("Vé này đã sử dụng một lần đổi vé.");
        }

        LocalDateTime departure = parseDeparture(booking.getTrip());
        long minutes = Duration.between(LocalDateTime.now(BUSINESS_ZONE), departure).toMinutes();
        if (minutes < MINIMUM_HOURS_BEFORE_DEPARTURE * 60L) {
            throw new IllegalArgumentException("Chỉ được đổi vé trước giờ khởi hành ít nhất 12 giờ.");
        }
    }

    private void validateRequest(TicketExchangeRequest request, Booking booking) {
        if (request == null || request.getNewTripId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn chuyến muốn đổi.");
        }
        if (request.getNewSeatNumbers() == null || request.getNewSeatNumbers().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ghế mới.");
        }
        String type = request.getExchangeType() == null ? "" : request.getExchangeType().trim().toUpperCase();
        if (!Set.of("SEAT", "TRIP").contains(type)) {
            throw new IllegalArgumentException("Loại đổi vé không hợp lệ.");
        }
        if ("SEAT".equals(type) && !request.getNewTripId().equals(booking.getTrip().getId())) {
            throw new IllegalArgumentException("Đổi ghế chỉ áp dụng trên chuyến hiện tại.");
        }
        if ("TRIP".equals(type) && request.getNewTripId().equals(booking.getTrip().getId())) {
            throw new IllegalArgumentException("Vui lòng chọn một ngày hoặc chuyến khác.");
        }
    }

    private String normalizePaymentMethod(String rawMethod, double difference) {
        if (difference <= 0) return "WALLET";
        String method = rawMethod == null ? "WALLET" : rawMethod.trim().toUpperCase();
        if (!Set.of("WALLET", "QR").contains(method)) {
            throw new IllegalArgumentException("Vui lòng chọn thanh toán bằng Ví hoặc chuyển khoản QR.");
        }
        return method;
    }

    private void validateNewSeatsAvailable(
            Trip oldTrip, Trip newTrip, List<String> oldSeats, List<String> newSeats) {
        List<Seat> targetSeats = seatRepository.findTripSeatsForUpdate(newTrip.getId());
        Map<String, Seat> seatMap = targetSeats.stream()
                .collect(Collectors.toMap(seat -> seat.getSeatNumber().toUpperCase(), seat -> seat));
        Set<String> oldSeatSet = oldSeats.stream().map(String::toUpperCase).collect(Collectors.toSet());
        Set<String> heldSeatSet = getHeldSeatNumbers(newTrip.getId());

        for (String seatNumber : newSeats) {
            Seat seat = seatMap.get(seatNumber.toUpperCase());
            if (seat == null) {
                throw new IllegalArgumentException("Ghế " + seatNumber + " không tồn tại trên chuyến mới.");
            }
            boolean ownCurrentSeat = oldTrip.getId().equals(newTrip.getId()) && oldSeatSet.contains(seatNumber.toUpperCase());
            if (!ownCurrentSeat && (Boolean.TRUE.equals(seat.getIsBooked()) || heldSeatSet.contains(seatNumber.toUpperCase()))) {
                throw new IllegalArgumentException("Ghế " + seatNumber + " không còn khả dụng.");
            }
        }
    }

    private void validatePendingBookingUnchanged(Booking booking, BookingExchange pending) {
        validateEligibility(booking);
        Set<String> currentSeats = booking.getSeatNumbers().stream().map(String::toUpperCase).collect(Collectors.toSet());
        Set<String> expectedSeats = splitSeatNumbers(pending.getOldSeatNumbers()).stream()
                .map(String::toUpperCase).collect(Collectors.toSet());
        if (!booking.getTrip().getId().equals(pending.getOldTripId()) || !currentSeats.equals(expectedSeats)) {
            expirePendingExchange(pending);
            throw new IllegalArgumentException("Vé đã thay đổi trong lúc chờ thanh toán. Vui lòng thao tác lại.");
        }
    }

    private void expirePreviousPendingExchanges(Long bookingId) {
        for (BookingExchange pending : bookingExchangeRepository.findByBookingIdAndStatus(bookingId, "PENDING_PAYMENT")) {
            expirePendingExchange(pending);
        }
    }

    private void expirePendingExchange(BookingExchange pending) {
        releaseReservations(pending);
        pending.setStatus("EXPIRED");
        bookingExchangeRepository.save(pending);
    }

    private void releaseReservations(BookingExchange exchange) {
        if (exchange.getReservationIds() == null || exchange.getReservationIds().isBlank()) return;
        List<Long> ids = Arrays.stream(exchange.getReservationIds().split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Long::valueOf)
                .toList();
        reservationRepository.deleteAllById(ids);
        exchange.setReservationIds(null);
    }

    private List<String> splitSeatNumbers(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(seat -> !seat.isEmpty())
                .map(String::toUpperCase)
                .toList();
    }

    private Trip getValidTargetTrip(Booking booking, Long tripId) {
        Trip target = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến muốn đổi."));
        Trip current = booking.getTrip();
        if (!current.getDeparturePoint().equalsIgnoreCase(target.getDeparturePoint())
                || !current.getArrivalPoint().equalsIgnoreCase(target.getArrivalPoint())) {
            throw new IllegalArgumentException("Chỉ được đổi sang chuyến có cùng tuyến đường.");
        }
        if (Boolean.FALSE.equals(target.getIsVisible())
                || Set.of("CANCELLED", "COMPLETED").contains(target.getStatus())) {
            throw new IllegalArgumentException("Chuyến được chọn hiện không còn khả dụng.");
        }
        if (parseDeparture(target).isBefore(LocalDateTime.now(BUSINESS_ZONE).plusHours(MINIMUM_HOURS_BEFORE_DEPARTURE))) {
            throw new IllegalArgumentException("Chuyến mới phải khởi hành sau ít nhất 12 giờ.");
        }
        return target;
    }

    private void lockNewSeatsAndReleaseOldSeats(
            Trip oldTrip, Trip newTrip, List<String> oldSeats, List<String> newSeats,
            Set<String> allowedHeldSeats) {
        List<Seat> newTripSeats = seatRepository.findTripSeatsForUpdate(newTrip.getId());
        Map<String, Seat> newSeatMap = newTripSeats.stream()
                .collect(Collectors.toMap(seat -> seat.getSeatNumber().toUpperCase(), seat -> seat));
        Set<String> oldSeatSet = oldSeats.stream().map(String::toUpperCase).collect(Collectors.toSet());
        Set<String> heldSeatSet = getHeldSeatNumbers(newTrip.getId());

        for (String seatNumber : newSeats) {
            Seat seat = newSeatMap.get(seatNumber);
            if (seat == null) {
                throw new IllegalArgumentException("Ghế " + seatNumber + " không tồn tại trên chuyến mới.");
            }
            boolean isOwnCurrentSeat = oldTrip.getId().equals(newTrip.getId()) && oldSeatSet.contains(seatNumber);
            boolean isAllowedHold = allowedHeldSeats.contains(seatNumber);
            if (heldSeatSet.contains(seatNumber) && !isOwnCurrentSeat && !isAllowedHold) {
                throw new IllegalArgumentException("Ghế " + seatNumber + " đang được khách khác giữ để thanh toán.");
            }
            if (Boolean.TRUE.equals(seat.getIsBooked()) && !isOwnCurrentSeat) {
                throw new IllegalArgumentException("Ghế " + seatNumber + " vừa được người khác đặt.");
            }
            seat.setIsBooked(true);
        }

        Set<String> newSeatSet = new HashSet<>(newSeats);
        if (oldTrip.getId().equals(newTrip.getId())) {
            for (String oldSeatNumber : oldSeatSet) {
                if (!newSeatSet.contains(oldSeatNumber)) {
                    Seat oldSeat = newSeatMap.get(oldSeatNumber);
                    if (oldSeat != null) oldSeat.setIsBooked(false);
                }
            }
            seatRepository.saveAll(newTripSeats);
            syncAvailableSeats(newTrip);
            return;
        }

        seatRepository.saveAll(newTripSeats);
        List<Seat> oldTripSeats = seatRepository.findTripSeatsForUpdate(oldTrip.getId());
        for (Seat seat : oldTripSeats) {
            if (oldSeatSet.contains(seat.getSeatNumber().toUpperCase())) {
                seat.setIsBooked(false);
            }
        }
        seatRepository.saveAll(oldTripSeats);
        syncAvailableSeats(oldTrip);
        syncAvailableSeats(newTrip);
    }

    private void syncAvailableSeats(Trip trip) {
        trip.setAvailableSeats(Math.toIntExact(seatRepository.countByTripIdAndIsBookedFalse(trip.getId())));
        tripRepository.save(trip);
    }

    private LocalDateTime parseDeparture(Trip trip) {
        try {
            String[] parts = trip.getDepartureTime().trim().split(":");
            LocalTime time = LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            return LocalDate.parse(trip.getDepartureDate()).atTime(time);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Ngày giờ khởi hành của chuyến không hợp lệ.");
        }
    }

    private Set<String> getHeldSeatNumbers(Long tripId) {
        return reservationRepository
                .findByTripIdAndExpiredAtAfter(tripId, LocalDateTime.now(BUSINESS_ZONE))
                .stream()
                .map(reservation -> reservation.getSeatNumber().toUpperCase())
                .collect(Collectors.toSet());
    }

    private Map<String, Object> toSeatOption(Seat seat, Set<String> currentSeats, Set<String> heldSeats) {
        boolean isCurrent = currentSeats.contains(seat.getSeatNumber());
        boolean isHeld = heldSeats.contains(seat.getSeatNumber().toUpperCase());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", seat.getId());
        item.put("seatNumber", seat.getSeatNumber());
        item.put("seatFloor", seat.getSeatFloor());
        item.put("isCurrent", isCurrent);
        item.put("isAvailable", isCurrent || (!Boolean.TRUE.equals(seat.getIsBooked()) && !isHeld));
        return item;
    }

    private Map<String, Object> toTripOption(Trip trip) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", trip.getId());
        item.put("departureDate", trip.getDepartureDate());
        item.put("departureTime", trip.getDepartureTime());
        item.put("arrivalTime", trip.getArrivalTime() == null ? "" : trip.getArrivalTime());
        item.put("price", safeAmount(trip.getPrice()));
        item.put("availableSeats", trip.getAvailableSeats() == null ? 0 : trip.getAvailableSeats());
        item.put("busType", trip.getBusType() == null ? "" : trip.getBusType());
        item.put("companyName", trip.getCompanyName() == null ? "" : trip.getCompanyName());
        return item;
    }

    private Map<String, Object> toBookingOption(Booking booking) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", booking.getId());
        item.put("tripId", booking.getTrip().getId());
        item.put("departurePoint", booking.getTrip().getDeparturePoint());
        item.put("arrivalPoint", booking.getTrip().getArrivalPoint());
        item.put("departureDate", booking.getTrip().getDepartureDate());
        item.put("departureTime", booking.getTrip().getDepartureTime());
        item.put("seatNumbers", booking.getSeatNumbers());
        item.put("seatCount", booking.getSeatNumbers().size());
        item.put("totalPrice", safeAmount(booking.getTotalPrice()));
        item.put("discountAmount", safeAmount(booking.getDiscountAmount()));
        return item;
    }

    private Map<String, Object> toAdminExchangeItem(BookingExchange exchange) {
        Booking booking = exchange.getBooking();
        User user = booking == null ? null : booking.getUser();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", exchange.getId());
        item.put("bookingId", booking == null ? null : booking.getId());
        item.put("customerName", booking == null ? "" : booking.getCustomerName());
        item.put("customerPhone", booking == null ? "" : booking.getCustomerPhone());
        item.put("userId", user == null ? null : user.getId());
        item.put("exchangeType", exchange.getExchangeType());
        item.put("oldTripId", exchange.getOldTripId());
        item.put("newTripId", exchange.getNewTripId());
        item.put("oldSeatNumbers", exchange.getOldSeatNumbers());
        item.put("newSeatNumbers", exchange.getNewSeatNumbers());
        item.put("oldPrice", safeAmount(exchange.getOldPrice()));
        item.put("newPrice", safeAmount(exchange.getNewPrice()));
        item.put("priceDifference", safeAmount(exchange.getPriceDifference()));
        item.put("paymentMethod", exchange.getPaymentMethod() == null ? "" : exchange.getPaymentMethod());
        item.put("paymentCode", exchange.getPaymentCode() == null ? "" : exchange.getPaymentCode());
        item.put("reason", exchange.getReason() == null ? "" : exchange.getReason());
        item.put("status", exchange.getStatus());
        item.put("exchangedAt", exchange.getExchangedAt());
        item.put("expiresAt", exchange.getExpiresAt());
        return item;
    }

    private Map<String, Object> toCustomerExchangeItem(BookingExchange exchange) {
        Trip oldTrip = tripRepository.findById(exchange.getOldTripId()).orElse(null);
        Trip newTrip = tripRepository.findById(exchange.getNewTripId()).orElse(null);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", exchange.getId());
        item.put("bookingId", exchange.getBooking().getId());
        item.put("exchangeType", exchange.getExchangeType());
        item.put("exchangedAt", exchange.getExchangedAt());
        item.put("reason", exchange.getReason() == null ? "" : exchange.getReason());
        item.put("priceDifference", safeAmount(exchange.getPriceDifference()));
        item.put("oldTicket", toExchangeTicketSnapshot(
                exchange.getOldTripId(), oldTrip, exchange.getOldSeatNumbers(), exchange.getOldPrice()));
        item.put("newTicket", toExchangeTicketSnapshot(
                exchange.getNewTripId(), newTrip, exchange.getNewSeatNumbers(), exchange.getNewPrice()));
        return item;
    }

    private Map<String, Object> toExchangeTicketSnapshot(Long tripId, Trip trip, String seats, Double price) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tripId", tripId);
        item.put("departurePoint", trip == null ? "" : trip.getDeparturePoint());
        item.put("arrivalPoint", trip == null ? "" : trip.getArrivalPoint());
        item.put("departureDate", trip == null ? "" : trip.getDepartureDate());
        item.put("departureTime", trip == null ? "" : trip.getDepartureTime());
        item.put("seatNumbers", splitSeatNumbers(seats));
        item.put("price", safeAmount(price));
        return item;
    }

    private double safeAmount(Double amount) {
        return amount == null ? 0.0 : amount;
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "%,.0f", amount).replace(',', '.');
    }
}
