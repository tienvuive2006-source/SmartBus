package com.smartbus.booking.service.ai;

import com.smartbus.booking.config.JwtService;
import com.smartbus.booking.repository.BookingRepository;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Order(2)
@RequiredArgsConstructor
public class TransactionIntentHandler implements AiIntentHandler {
    private static final DateTimeFormatter TRIP_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EntityExtractionService entityService;

    @Override
    public boolean canHandle(String nonAccentMsg) {
        return nonAccentMsg.contains("huy ve") || 
               nonAccentMsg.contains("ve cua toi") || 
               nonAccentMsg.contains("bao gio chay") || 
               nonAccentMsg.contains("kiem tra ve") ||
               nonAccentMsg.contains("ma qr") ||
               nonAccentMsg.contains("gui ma") ||
               nonAccentMsg.contains("lay ma") ||
               nonAccentMsg.contains("gui lai ma");
    }

    @Override
    public boolean usesBookingContext() {
        return true;
    }

    @Override
    public Map<String, Object> handle(String nonAccentMsg, String authHeader, ChatContext ctx, String sessionId) {
        Map<String, Object> response = new HashMap<>();
        com.smartbus.booking.entity.User currentUser = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Long userId = jwtService.extractUserId(token);
                currentUser = userRepository.findById(userId).orElse(null);
            } catch (Exception e) {}
        }
        
        if (currentUser == null) {
            response.put("text", "Bạn cần **Đăng nhập** thì tôi mới có thể kiểm tra vé cho bạn được nhé! Hãy nhấn vào nút Đăng nhập ở góc trên cùng bên phải.");
            response.put("action", "none");
            return response;
        }
        
        List<com.smartbus.booking.entity.Booking> userBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        if (userBookings.isEmpty()) {
            response.put("text", "Tôi vừa kiểm tra hệ thống nhưng không thấy bạn có đặt chuyến xe nào cả. Bạn có muốn tìm vé xe mới không?");
            response.put("action", "none");
            return response;
        }
        
        List<com.smartbus.booking.entity.Booking> matchingActiveBookings = new java.util.ArrayList<>();
        
        for (com.smartbus.booking.entity.Booking b : userBookings) {
            if (isActiveUpcoming(b)) {
                boolean matchCity = true;
                if (ctx.to != null && ctx.from != null) {
                    matchCity = entityService.removeAccents(b.getTrip().getArrivalPoint().toLowerCase()).contains(entityService.removeAccents(ctx.to.toLowerCase())) &&
                                entityService.removeAccents(b.getTrip().getDeparturePoint().toLowerCase()).contains(entityService.removeAccents(ctx.from.toLowerCase()));
                } else if (ctx.to != null || ctx.from != null) {
                    matchCity = (ctx.to != null && entityService.removeAccents(b.getTrip().getArrivalPoint().toLowerCase()).contains(entityService.removeAccents(ctx.to.toLowerCase()))) ||
                                (ctx.from != null && entityService.removeAccents(b.getTrip().getDeparturePoint().toLowerCase()).contains(entityService.removeAccents(ctx.from.toLowerCase())));
                }
                if (matchCity) {
                    matchingActiveBookings.add(b);
                }
            }
        }
        
        matchingActiveBookings.sort(java.util.Comparator.comparing(this::departureDateTime));

        com.smartbus.booking.entity.Booking targetBooking = null;
        if (!matchingActiveBookings.isEmpty()) {
            targetBooking = matchingActiveBookings.get(0);
        }

        if (targetBooking == null) {
            response.put("text", ctx.to != null || ctx.from != null
                    ? "Tôi không tìm thấy vé sắp tới còn hiệu lực phù hợp với tuyến bạn hỏi. Bạn có thể mở **Lịch sử đặt vé** để kiểm tra các vé cũ."
                    : "Hiện bạn không có vé sắp tới còn hiệu lực. Bạn có thể mở **Lịch sử đặt vé** để xem các chuyến đã qua hoặc đã hủy.");
            response.put("action", "navigate_history");
            return response;
        }
        
        String fromCity = targetBooking.getTrip().getDeparturePoint();
        String toCity = targetBooking.getTrip().getArrivalPoint();
        String dateStr = targetBooking.getTrip().getDepartureDate();
        String timeStr = targetBooking.getTrip().getDepartureTime();
        
        if (nonAccentMsg.contains("huy ve")) {
            if (matchingActiveBookings.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("Tôi tìm thấy bạn đang có **").append(matchingActiveBookings.size()).append(" vé đang hoạt động** ");
                if (ctx.to != null) sb.append("đi **").append(ctx.to).append("**:\n\n");
                else sb.append(":\n\n");
                
                for (int i = 0; i < Math.min(3, matchingActiveBookings.size()); i++) {
                    com.smartbus.booking.entity.Booking b = matchingActiveBookings.get(i);
                    sb.append("- Chuyến đi **").append(b.getTrip().getArrivalPoint()).append("** lúc **")
                      .append(b.getTrip().getDepartureTime()).append(" ngày ").append(b.getTrip().getDepartureDate()).append("** (Ghế ").append(seatText(b)).append(")\n");
                }
                if (matchingActiveBookings.size() > 3) sb.append("- ... và ").append(matchingActiveBookings.size() - 3).append(" vé khác.\n");
                
                sb.append("\nBạn muốn hủy vé nào? Vui lòng vào phần **Lịch sử giao dịch** để chọn chính xác vé cần hủy nhé.");
                response.put("text", sb.toString());
                response.put("action", "navigate_history");
            } else {
                if ("CANCELLED".equals(targetBooking.getStatus())) {
                    response.put("text", "Chuyến xe đi " + toCity + " của bạn đã được hủy trước đó rồi nhé!");
                } else if ("COMPLETED".equals(targetBooking.getStatus()) || "CHECKED_IN".equals(targetBooking.getStatus())) {
                    response.put("text", "Chuyến xe đi " + toCity + " của bạn đã hoàn thành hoặc bạn đã lên xe nên không thể hủy được nữa.");
                } else {
                    response.put("text", "Tôi tìm thấy bạn đang có một vé đi **" + toCity + "** vào lúc **" + timeStr + " ngày " + dateStr + "**.\n\nBạn có chắc chắn muốn hủy vé này không? (Để hủy, vui lòng vào phần **Lịch sử giao dịch** và chọn nút Hủy nhé).");
                    response.put("action", "navigate_history");
                }
            }
            return response;
        }
        
        if (nonAccentMsg.contains("bao gio chay") || nonAccentMsg.contains("kiem tra ve") || nonAccentMsg.contains("ve cua toi")) {
            if (matchingActiveBookings.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("Bạn đang có **").append(matchingActiveBookings.size()).append(" chuyến xe sắp tới**:\n\n");
                for (int i = 0; i < Math.min(3, matchingActiveBookings.size()); i++) {
                    com.smartbus.booking.entity.Booking b = matchingActiveBookings.get(i);
                    sb.append("- Đi **").append(b.getTrip().getArrivalPoint()).append("**: **").append(b.getTrip().getDepartureTime()).append(" ngày ").append(b.getTrip().getDepartureDate()).append("** (Ghế ").append(seatText(b)).append(")\n");
                }
                if (matchingActiveBookings.size() > 3) sb.append("- ... và ").append(matchingActiveBookings.size() - 3).append(" vé khác.\n");
                sb.append("\nBạn có thể vào Lịch sử giao dịch để xem chi tiết tất cả các vé nhé.");
                response.put("text", sb.toString());
                response.put("action", "navigate_history");
            } else {
                if ("CANCELLED".equals(targetBooking.getStatus())) {
                    response.put("text", "Vé đi " + toCity + " của bạn đã bị hủy.");
                } else {
                    response.put("text", "Chuyến xe đi **" + toCity + "** của bạn khởi hành lúc:\n\n⏰ **" + timeStr + " ngày " + dateStr + "**\n💺 **Ghế:** " + seatText(targetBooking) + "\n\nBạn nhớ ra điểm đón trước 30 phút nhé!");
                }
                response.put("action", "none");
            }
            return response;
        }
        
        if (nonAccentMsg.contains("ma qr") || nonAccentMsg.contains("gui lai ma") || nonAccentMsg.contains("gui ma") || nonAccentMsg.contains("lay ma")) {
            response.put("text", "Vé gần nhất của bạn là chuyến đi **" + toCity + "** lúc **" + timeStr + " ngày " + dateStr + "**, ghế **" + seatText(targetBooking) + "**.\n\nNhấn nút bên dưới để mở vé và xem mã QR chính thức trong **Lịch sử đặt vé**.");
            response.put("action", "navigate_history");
            return response;
        }
        return response;
    }

    private boolean isActiveUpcoming(com.smartbus.booking.entity.Booking booking) {
        LocalDateTime departure = departureDateTime(booking);
        return booking != null && booking.getTrip() != null
                && ("PENDING".equals(booking.getStatus()) || "PAID".equals(booking.getStatus()))
                && !LocalDateTime.MAX.equals(departure)
                && !departure.isBefore(LocalDateTime.now());
    }

    private LocalDateTime departureDateTime(com.smartbus.booking.entity.Booking booking) {
        try {
            return LocalDateTime.parse(booking.getTrip().getDepartureDate() + " " + booking.getTrip().getDepartureTime(), TRIP_DATE_TIME);
        } catch (Exception ignored) {
            return LocalDateTime.MAX;
        }
    }

    private String seatText(com.smartbus.booking.entity.Booking booking) {
        return booking.getSeatNumbers() == null || booking.getSeatNumbers().isEmpty()
                ? "Chưa cập nhật"
                : String.join(", ", booking.getSeatNumbers());
    }
}
