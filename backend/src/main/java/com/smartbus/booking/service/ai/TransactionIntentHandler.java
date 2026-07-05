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

@Component
@Order(2)
@RequiredArgsConstructor
public class TransactionIntentHandler implements AiIntentHandler {
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
            if ("PENDING".equals(b.getStatus()) || "PAID".equals(b.getStatus())) {
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
        
        matchingActiveBookings.sort(java.util.Comparator.comparing(b -> b.getTrip().getDepartureDate() + " " + b.getTrip().getDepartureTime()));

        com.smartbus.booking.entity.Booking targetBooking = null;
        if (!matchingActiveBookings.isEmpty()) {
            targetBooking = matchingActiveBookings.get(0);
        } else {
            if (ctx.to != null || ctx.from != null) {
                for (com.smartbus.booking.entity.Booking b : userBookings) {
                    boolean matchCity = (ctx.to != null && entityService.removeAccents(b.getTrip().getArrivalPoint().toLowerCase()).contains(entityService.removeAccents(ctx.to.toLowerCase()))) ||
                                      (ctx.from != null && entityService.removeAccents(b.getTrip().getDeparturePoint().toLowerCase()).contains(entityService.removeAccents(ctx.from.toLowerCase())));
                    if (matchCity) {
                        targetBooking = b;
                        break;
                    }
                }
            }
            if (targetBooking == null) {
                targetBooking = userBookings.get(0);
            }
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
                      .append(b.getTrip().getDepartureTime()).append(" ngày ").append(b.getTrip().getDepartureDate()).append("**\n");
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
                    sb.append("- Đi **").append(b.getTrip().getArrivalPoint()).append("**: **").append(b.getTrip().getDepartureTime()).append(" ngày ").append(b.getTrip().getDepartureDate()).append("** (Ghế ").append(String.join(", ", b.getSeatNumbers())).append(")\n");
                }
                if (matchingActiveBookings.size() > 3) sb.append("- ... và ").append(matchingActiveBookings.size() - 3).append(" vé khác.\n");
                sb.append("\nBạn có thể vào Lịch sử giao dịch để xem chi tiết tất cả các vé nhé.");
                response.put("text", sb.toString());
                response.put("action", "navigate_history");
            } else {
                if ("CANCELLED".equals(targetBooking.getStatus())) {
                    response.put("text", "Vé đi " + toCity + " của bạn đã bị hủy.");
                } else {
                    response.put("text", "Chuyến xe đi **" + toCity + "** của bạn khởi hành lúc:\n\n⏰ **" + timeStr + " ngày " + dateStr + "**\n💺 **Ghế:** " + String.join(", ", targetBooking.getSeatNumbers()) + "\n\nBạn nhớ ra điểm đón trước 30 phút nhé!");
                }
                response.put("action", "none");
            }
            return response;
        }
        
        if (nonAccentMsg.contains("ma qr") || nonAccentMsg.contains("gui lai ma") || nonAccentMsg.contains("gui ma") || nonAccentMsg.contains("lay ma")) {
            if ("CANCELLED".equals(targetBooking.getStatus())) {
                response.put("text", "Vé đi " + toCity + " của bạn đã bị hủy nên mã QR không còn hiệu lực nữa.");
                response.put("action", "none");
            } else {
                String qrData = "Mã đặt vé: #" + targetBooking.getId() + "\nKhách: " + currentUser.getFullName() + "\nGhế: " + String.join(", ", targetBooking.getSeatNumbers()) + "\nTrạng thái: " + (("CASH".equals(targetBooking.getPaymentMethod()) && "PENDING".equals(targetBooking.getStatus())) ? "CHƯA THANH TOÁN (THU TIỀN MẶT)" : "ĐÃ THANH TOÁN");
                String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + java.net.URLEncoder.encode(qrData, java.nio.charset.StandardCharsets.UTF_8) + "&color=075955&bgcolor=f8fafc";
                
                String prefix = "";
                if (matchingActiveBookings.size() > 1) {
                    prefix = "Tôi thấy bạn đang có tới **" + matchingActiveBookings.size() + " vé sắp tới**.\n\nĐây là mã QR cho chuyến xe **gần nhất** của bạn (Chuyến đi **" + toCity + "** ngày **" + dateStr + "**). Nếu bạn cần lấy mã của các vé khác, vui lòng vào **Lịch sử giao dịch** nhé!\n\n";
                } else {
                    prefix = "Đây là mã QR lên xe của bạn (Chuyến đi **" + toCity + "** ngày **" + dateStr + "**).\n\nBạn hãy lưu mã này lại hoặc đưa thẳng cho tài xế quét nhé!\n\n";
                }
                
                response.put("text", prefix + "![Mã QR Lên Xe](" + qrUrl + ")");
                response.put("action", "none");
            }
            return response;
        }
        return response;
    }
}
