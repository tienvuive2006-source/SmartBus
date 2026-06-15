package com.smartbus.booking.service;

import com.smartbus.booking.config.JwtService;
import com.smartbus.booking.entity.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiService {

    private final TripService tripService;
    private final com.smartbus.booking.repository.UserRepository userRepository;
    private final com.smartbus.booking.repository.BookingRepository bookingRepository;
    private final JwtService jwtService;

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace("đ", "d").replace("Đ", "D");
    }

    private static class ChatContext {
        String from;
        String to;
        String date;
        String sort = "default";
        String company = "all";
        String busType = "all";
        long lastUpdated = System.currentTimeMillis();
    }

    private final Map<String, ChatContext> activeSessions = new java.util.concurrent.ConcurrentHashMap<>();

    public Map<String, Object> processMessage(String message, String sessionId, String authHeader) {
        String lowerMsg = message.toLowerCase();
        String nonAccentMsg = removeAccents(lowerMsg); // Loại bỏ hoàn toàn dấu tiếng Việt để so khớp dễ hơn
        Map<String, Object> response = new HashMap<>();
        
        ChatContext ctx = activeSessions.computeIfAbsent(sessionId, k -> new ChatContext());
        ctx.lastUpdated = System.currentTimeMillis();
        
        // ==============================================================
        // TÍNH NĂNG MỚI: TRỢ LÝ GIAO DỊCH (KIỂM TRA VÉ, HỦY VÉ, LẤY MÃ QR)
        // ==============================================================
        boolean isRequestingTransaction = nonAccentMsg.contains("huy ve") || 
                                          nonAccentMsg.contains("ve cua toi") || 
                                          nonAccentMsg.contains("bao gio chay") || 
                                          nonAccentMsg.contains("kiem tra ve") ||
                                          nonAccentMsg.contains("ma qr") ||
                                          nonAccentMsg.contains("gui ma") ||
                                          nonAccentMsg.contains("lay ma") ||
                                          nonAccentMsg.contains("gui lai ma");
                                          
        // ==============================================================
        // TÍNH NĂNG MỚI: HỎI ĐÁP FAQ TỪ DATABASE & CHÍNH SÁCH
        // ==============================================================
        if (nonAccentMsg.contains("cac loai xe") || nonAccentMsg.contains("nhung loai xe") || nonAccentMsg.contains("co loai xe nao") || nonAccentMsg.contains("nhung dong xe")) {
            List<com.smartbus.booking.entity.Trip> allTrips = tripService.getAllTrips();
            java.util.Set<String> busTypes = new java.util.HashSet<>();
            for (com.smartbus.booking.entity.Trip t : allTrips) {
                if (t.getBusType() != null && !t.getBusType().trim().isEmpty()) {
                    busTypes.add(t.getBusType());
                }
            }
            if (!busTypes.isEmpty()) {
                response.put("text", "Hiện tại hệ thống nhà xe đang phục vụ các dòng xe chất lượng cao sau:\n\n- **" + String.join("**\n- **", busTypes) + "**\n\nBạn muốn trải nghiệm dòng xe nào cho chuyến đi sắp tới?");
            } else {
                response.put("text", "Hiện tại nhà xe chưa cập nhật thông tin loại xe trên hệ thống.");
            }
            response.put("action", "none");
            return response;
        }

        if (nonAccentMsg.contains("hanh ly")) {
            response.put("text", "🚌 **Quy định hành lý:**\n\nMỗi hành khách được mang theo tối đa **20kg** hành lý ký gửi và **1 kiện hành lý xách tay** (nhỏ gọn). Nếu hành lý vượt quá quy định, nhà xe sẽ thu thêm phụ phí tùy theo tuyến đường. Bạn nhớ đóng gói cẩn thận nhé!");
            response.put("action", "none");
            return response;
        }

        if (nonAccentMsg.contains("chinh sach huy") || nonAccentMsg.contains("doi ve") || nonAccentMsg.contains("tra ve") || nonAccentMsg.contains("huy ve ra sao")) {
            response.put("text", "🔄 **Chính sách Đổi/Hủy vé:**\n\n- Hủy vé trước **24 tiếng**: Hoàn 100% tiền vé.\n- Hủy vé trước **12 tiếng**: Hoàn 50% tiền vé.\n- Dưới 12 tiếng hoặc sau khi xe chạy: Không hỗ trợ hoàn tiền.\n\n*Lưu ý: Tiền hoàn sẽ được cộng tự động vào Ví SkyPay của bạn để dùng cho các chuyến sau.*");
            response.put("action", "none");
            return response;
        }
        
        if (nonAccentMsg.contains("thanh toan")) {
            response.put("text", "💳 **Hướng dẫn thanh toán:**\n\nHệ thống SmartBus hỗ trợ 2 hình thức thanh toán chính:\n1. **Thanh toán qua Ví SkyPay:** Thanh toán tức thì bằng cách quét mã QR ngân hàng (Hệ thống tự động duyệt vé sau 1-3 phút).\n2. **Thanh toán Tiền mặt:** Đặt vé giữ chỗ và thanh toán trực tiếp cho tài xế khi lên xe.\n\nBạn có thể thoải mái lựa chọn hình thức phù hợp ở bước Thanh toán nhé!");
            response.put("action", "none");
            return response;
        }
        // ==============================================================

        String from = null;
        String to = null;
        String date = null;

        // 1. NHẬN DIỆN THÀNH PHỐ
        String[] allCities = {
            "Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Nha Trang", "Đà Lạt", 
            "Cần Thơ", "Quy Nhơn", "Hải Phòng", "Vũng Tàu", "Phú Yên", "Quảng Ngãi", 
            "Huế", "Bình Định", "Khánh Hòa", "Bảo Lộc", "Gia Lai", "Đắk Lắk", 
            "Cà Mau", "Bạc Liêu", "Sóc Trăng", "Kiên Giang", "An Giang", "Đồng Tháp",
            "Tiền Giang", "Bến Tre", "Vĩnh Long", "Trà Vinh", "Tây Ninh", "Bình Dương",
            "Đồng Nai", "Bình Phước", "Bình Thuận", "Ninh Thuận", "Đắk Nông", "Kon Tum",
            "Quảng Nam", "Quảng Bình", "Quảng Trị", "Hà Tĩnh", "Nghệ An", "Thanh Hóa",
            "Nam Định", "Thái Bình", "Hải Dương", "Hưng Yên", "Bắc Ninh", "Vĩnh Phúc",
            "Phú Thọ", "Thái Nguyên", "Bắc Giang", "Quảng Ninh", "Lạng Sơn", "Lào Cai"
        };

        Map<String, Integer> foundCities = new HashMap<>();
        for (String city : allCities) {
            String nonAccentCity = removeAccents(city.toLowerCase());
            int index = nonAccentMsg.indexOf(nonAccentCity);
            if (index != -1) foundCities.put(city, index);
            
            // Hỗ trợ alias "Sài Gòn" -> "Hồ Chí Minh"
            if (city.equals("Hồ Chí Minh") && nonAccentMsg.contains("sai gon")) {
                foundCities.put("Hồ Chí Minh", nonAccentMsg.indexOf("sai gon"));
            }
        }

        if (foundCities.size() >= 2) {
            List<Map.Entry<String, Integer>> list = new java.util.ArrayList<>(foundCities.entrySet());
            list.sort(Map.Entry.comparingByValue());
            from = list.get(0).getKey();
            to = list.get(1).getKey();
            
            if ((nonAccentMsg.contains("ve " + removeAccents(from.toLowerCase())) || nonAccentMsg.contains("den " + removeAccents(from.toLowerCase()))) 
                && nonAccentMsg.contains("tu " + removeAccents(to.toLowerCase()))) {
                String temp = from; from = to; to = temp;
            }
        } else if (foundCities.size() == 1) {
            String city = foundCities.keySet().iterator().next();
            String nonAccentCity = removeAccents(city.toLowerCase());
            if (nonAccentMsg.contains("tu " + nonAccentCity) || (city.equals("Hồ Chí Minh") && nonAccentMsg.contains("tu sai gon"))) {
                from = city;
            } else {
                to = city; 
            }
        }

        // 2. NHẬN DIỆN NGÀY THÁNG
        LocalDate targetDate = null;
        LocalDate now = LocalDate.now();
        if (nonAccentMsg.contains("ngay mai") || nonAccentMsg.contains("sang mai") || nonAccentMsg.contains("toi mai")) {
            targetDate = now.plusDays(1);
        } else if (nonAccentMsg.contains("hom nay") || nonAccentMsg.contains("chieu nay") || nonAccentMsg.contains("toi nay") || nonAccentMsg.contains("di luon")) {
            targetDate = now;
        } else if (nonAccentMsg.contains("ngay mot") || nonAccentMsg.contains("kia")) {
            targetDate = now.plusDays(2);
        } else if (nonAccentMsg.contains("cuoi tuan")) {
            targetDate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        } else if (nonAccentMsg.contains("tuan sau")) {
            targetDate = now.plusWeeks(1);
        } else {
            try {
                Pattern pFull = Pattern.compile("ngay\\s+(\\d{1,2})\\s+thang\\s+(\\d{1,2})");
                Matcher mFull = pFull.matcher(nonAccentMsg);
                if (mFull.find()) {
                    int day = Integer.parseInt(mFull.group(1));
                    int month = Integer.parseInt(mFull.group(2));
                    targetDate = now.withMonth(month).withDayOfMonth(day);
                    if (targetDate.isBefore(now)) targetDate = targetDate.plusYears(1);
                } else {
                    Pattern p = Pattern.compile("ngay\\s+(\\d{1,2})");
                    Matcher m = p.matcher(nonAccentMsg);
                    if (m.find()) {
                        int day = Integer.parseInt(m.group(1));
                        if (day >= now.getDayOfMonth()) targetDate = now.withDayOfMonth(day);
                        else targetDate = now.plusMonths(1).withDayOfMonth(day);
                    }
                }
            } catch (Exception e) {}
        }
        
        if (targetDate != null) date = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // ==============================================================
        // TÍNH NĂNG MỚI: TRỢ LÝ GIAO DỊCH (KIỂM TRA VÉ, HỦY VÉ, LẤY MÃ QR)
        // ==============================================================
        if (isRequestingTransaction) {
            com.smartbus.booking.entity.User currentUser = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String phone = jwtService.extractPhone(token);
                    currentUser = userRepository.findByPhone(phone).orElse(null);
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
            
            // 1. Tìm TẤT CẢ vé ĐANG HOẠT ĐỘNG khớp với thành phố (hoặc tất cả vé hoạt động nếu không chỉ định TP)
            for (com.smartbus.booking.entity.Booking b : userBookings) {
                if ("PENDING".equals(b.getStatus()) || "PAID".equals(b.getStatus())) {
                    boolean matchCity = true;
                    if (to != null && from != null) {
                        matchCity = removeAccents(b.getTrip().getArrivalPoint().toLowerCase()).contains(removeAccents(to.toLowerCase())) &&
                                    removeAccents(b.getTrip().getDeparturePoint().toLowerCase()).contains(removeAccents(from.toLowerCase()));
                    } else if (to != null || from != null) {
                        matchCity = (to != null && removeAccents(b.getTrip().getArrivalPoint().toLowerCase()).contains(removeAccents(to.toLowerCase()))) ||
                                    (from != null && removeAccents(b.getTrip().getDeparturePoint().toLowerCase()).contains(removeAccents(from.toLowerCase())));
                    }
                    if (matchCity) {
                        matchingActiveBookings.add(b);
                    }
                }
            }
            
            // Sắp xếp vé để ưu tiên chuyến đi gần nhất lên trước
            matchingActiveBookings.sort(java.util.Comparator.comparing(b -> b.getTrip().getDepartureDate() + " " + b.getTrip().getDepartureTime()));

            com.smartbus.booking.entity.Booking targetBooking = null;
            if (!matchingActiveBookings.isEmpty()) {
                targetBooking = matchingActiveBookings.get(0);
            } else {
                // 2. Nếu không có vé hoạt động, tìm 1 vé BẤT KỲ khớp với thành phố
                if (to != null || from != null) {
                    for (com.smartbus.booking.entity.Booking b : userBookings) {
                        boolean matchCity = (to != null && removeAccents(b.getTrip().getArrivalPoint().toLowerCase()).contains(removeAccents(to.toLowerCase()))) ||
                                          (from != null && removeAccents(b.getTrip().getDeparturePoint().toLowerCase()).contains(removeAccents(from.toLowerCase())));
                        if (matchCity) {
                            targetBooking = b;
                            break;
                        }
                    }
                }
                
                // 3. Nếu vẫn không có, lấy đại vé đầu tiên trong lịch sử
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
                    if (to != null) sb.append("đi **").append(to).append("**:\n\n");
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
        }


        // SUY LUẬN TỪ NGỮ CẢNH (CONTEXT-AWARE FLIP)
        if (from == null && to != null) {
            // Parser vừa tìm thấy 1 thành phố và mặc định gán nó vào Điểm Đến (do không có chữ 'từ')
            // Tuy nhiên, nếu hệ thống đang hỏi "Từ đâu?" (ctx.from rỗng, ctx.to đã có)
            if (ctx.from == null && ctx.to != null) {
                String nonAccentTo = removeAccents(to.toLowerCase());
                // Nếu khách không dùng từ khóa đổi điểm đến như "đến X", "về X", "đi X"
                if (!nonAccentMsg.contains("den " + nonAccentTo) && 
                    !nonAccentMsg.contains("ve " + nonAccentTo) && 
                    !nonAccentMsg.contains("di " + nonAccentTo)) {
                    // Tự động lật ngược thành phố đó thành Điểm Đi!
                    from = to;
                    to = null;
                }
            }
        }

        // TRỘN NGỮ CẢNH (MERGE CONTEXT)
        if (from != null) ctx.from = from;
        if (to != null) ctx.to = to;
        if (date != null) ctx.date = date;
        
        // KIỂM TRA ĐIỀU KIỆN ĐỦ ĐỂ TÌM KIẾM CHƯA?
        if (ctx.from == null && ctx.to == null) {
            response.put("text", "Bạn cần tìm vé xe đi đâu? (Ví dụ: 'Tôi muốn đi từ Hà Nội đến Đà Nẵng')");
            response.put("action", "none");
            return response;
        } else if (ctx.from == null) {
            response.put("text", "Bạn muốn khởi hành từ đâu để đến " + ctx.to + "?");
            response.put("action", "none");
            return response;
        } else if (ctx.to == null) {
            response.put("text", "Bạn muốn đi từ " + ctx.from + " đến đâu?");
            response.put("action", "none");
            return response;
        }

        // 3. XỬ LÝ DATABASE
        if (ctx.from != null && ctx.to != null) {
            String searchDate = (ctx.date != null) ? ctx.date : "";
            boolean isClosestDate = nonAccentMsg.contains("gan nhat") || nonAccentMsg.contains("som nhat") || nonAccentMsg.contains("nhanh nhat");
            
            List<Trip> trips = tripService.searchTrips(ctx.from, ctx.to, searchDate);

            if (isClosestDate && !trips.isEmpty()) {
                LocalDate closestDate = null;
                for (Trip t : trips) {
                    if (t.getDepartureDate() != null && t.getDepartureDate().length() >= 10) {
                        try {
                            LocalDate d = LocalDate.parse(t.getDepartureDate().substring(0, 10));
                            if (!d.isBefore(now)) {
                                if (closestDate == null || d.isBefore(closestDate)) closestDate = d;
                            }
                        } catch (Exception e) {}
                    }
                }
                if (closestDate != null) {
                    date = closestDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String finalDate = date;
                    List<Trip> closestTrips = new java.util.ArrayList<>();
                    for (Trip t : trips) {
                        if (t.getDepartureDate() != null && t.getDepartureDate().startsWith(finalDate)) {
                            closestTrips.add(t);
                        }
                    }
                    trips = closestTrips;
                }
            }

            // 4. NHẬN DIỆN MỨC GIÁ
            String sort = "default";
            if (nonAccentMsg.contains("re nhat") || nonAccentMsg.contains("gia re") || nonAccentMsg.contains("thap nhat") || nonAccentMsg.contains("beo nhat") || nonAccentMsg.contains("tiet kiem")) {
                sort = "price_asc";
            } else if (nonAccentMsg.contains("dat nhat") || nonAccentMsg.contains("vip nhat") || nonAccentMsg.contains("sang nhat") || nonAccentMsg.contains("mac nhat") || nonAccentMsg.contains("cao nhat") || nonAccentMsg.contains("xin nhat")) {
                sort = "price_desc";
            }

            // 5. NHẬN DIỆN HÃNG XE VÀ LOẠI XE
            String detectedCompany = "all";
            String detectedBusType = "all";
            String normalizedMsg = nonAccentMsg.replace("-", "").replace(" ", "");
            
            for (Trip t : trips) {
                String normalizedCompanyName = removeAccents(t.getCompanyName().toLowerCase()).replace("-", "").replace(" ", "");
                if (normalizedMsg.contains(normalizedCompanyName)) {
                    detectedCompany = t.getCompanyName();
                }
                
                if (t.getBusType() != null) {
                    String[] typeWords = removeAccents(t.getBusType().toLowerCase()).split("\\s+");
                    for (String word : typeWords) {
                        if (!word.equals("cho") && !word.equals("phong") && !word.equals("xe") && !word.matches("\\d+")) {
                            String safeWord = word;
                            if (word.equals("premium")) safeWord = "prenium"; 
                            if (word.equals("luxury")) safeWord = "luxyry";
                            
                            if (word.length() > 2 && (nonAccentMsg.contains(word) || nonAccentMsg.contains(safeWord))) {
                                detectedBusType = word; 
                            }
                        }
                    }
                }
            }
            
            if (nonAccentMsg.contains("giuong nam") || nonAccentMsg.contains("giuong")) detectedBusType = "giuong";

            // 6. LỌC
            if (!detectedCompany.equals("all") || !detectedBusType.equals("all")) {
                List<Trip> filteredTrips = new java.util.ArrayList<>();
                for (Trip t : trips) {
                    boolean ok = true;
                    if (!detectedCompany.equals("all") && !t.getCompanyName().equals(detectedCompany)) ok = false;
                    
                    if (!detectedBusType.equals("all") && t.getBusType() != null) {
                        String dbBusType = removeAccents(t.getBusType().toLowerCase());
                        if (!dbBusType.contains(detectedBusType) && !dbBusType.contains(detectedBusType.replace("premium", "prenium"))) ok = false;
                    }
                    if (ok) filteredTrips.add(t);
                }
                trips = filteredTrips;
            }

            if (trips.isEmpty()) {
                String filterMsg = "";
                if (!detectedCompany.equals("all")) filterMsg += " của nhà xe " + detectedCompany;
                if (!detectedBusType.equals("all")) filterMsg += " (Loại " + detectedBusType + ")";
                response.put("text", "Rất tiếc! Tôi vừa tra cứu toàn bộ kho dữ liệu nhưng **không có chuyến xe nào** từ " + ctx.from + " đến " + ctx.to + filterMsg + (ctx.date != null ? " vào ngày " + ctx.date : " sắp tới") + ".\n\nNhưng tôi vẫn sẽ đưa bạn đến trang kết quả để bạn dễ dàng tìm kiếm lịch trình khác nhé!");
            } else {
                String dateText = (ctx.date != null) ? "vào ngày " + ctx.date : "sắp tới";
                double minPrice = trips.stream().mapToDouble(t -> t.getPrice() != null ? t.getPrice() : 0.0).min().orElse(0.0);
                
                String filterMsg = "";
                if (!detectedCompany.equals("all")) filterMsg += " của nhà xe " + detectedCompany;
                if (!detectedBusType.equals("all")) filterMsg += " (Loại " + detectedBusType + ")";

                String sortText = "";
                if (sort.equals("price_asc")) {
                    sortText = " và đã tự động **sắp xếp theo giá Siêu Rẻ** cho bạn";
                    trips.sort(java.util.Comparator.comparingDouble((Trip t) -> t.getPrice() != null ? t.getPrice() : Double.MAX_VALUE));
                }
                else if (sort.equals("price_desc")) {
                    sortText = " và đã tự động **chọn những vé VIP Cao Cấp nhất** cho bạn";
                    trips.sort(java.util.Comparator.comparingDouble((Trip t) -> t.getPrice() != null ? t.getPrice() : 0.0).reversed());
                } else {
                    trips.sort(java.util.Comparator.comparing((Trip t) -> t.getDepartureDate() != null ? t.getDepartureDate() : ""));
                }

                List<Map<String, Object>> tripsPreview = new java.util.ArrayList<>();
                int limit = Math.min(trips.size(), 3);
                for (int i = 0; i < limit; i++) {
                    Trip t = trips.get(i);
                    tripsPreview.add(Map.of(
                        "id", t.getId(),
                        "companyName", t.getCompanyName() != null ? t.getCompanyName() : "",
                        "departureTime", t.getDepartureTime() != null ? t.getDepartureTime() : "",
                        "price", t.getPrice() != null ? t.getPrice() : 0.0,
                        "busType", t.getBusType() != null ? t.getBusType() : ""
                    ));
                }
                response.put("tripsPreview", tripsPreview);

                response.put("text", "Tuyệt vời! Tôi đã quét hệ thống và tìm thấy **" + trips.size() + " chuyến xe** từ " + ctx.from + " đến " + ctx.to + filterMsg + " " + dateText + sortText + ".\n\nGiá vé tốt nhất chỉ từ **" + String.format("%,.0f", minPrice) + "đ**! Mời bạn xem trước chuyến xe ở dưới nhé! 👇");
            }
            
            response.put("action", "search");
            response.put("params", Map.of(
                "from", ctx.from, "to", ctx.to, "date", ctx.date != null ? ctx.date : "",
                "sort", sort, "company", detectedCompany, "busType", detectedBusType
            ));
            
            // Xóa ngữ cảnh sau khi tìm thấy để tránh dính cho các câu tìm kiếm hoàn toàn mới sau này
            activeSessions.remove(sessionId);
        }

        return response;
    }
}
