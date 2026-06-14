package com.smartbus.booking.service;

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

    public Map<String, Object> processMessage(String message, String sessionId) {
        String lowerMsg = message.toLowerCase();
        String nonAccentMsg = removeAccents(lowerMsg); // Loại bỏ hoàn toàn dấu tiếng Việt để so khớp dễ hơn
        Map<String, Object> response = new HashMap<>();
        
        ChatContext ctx = activeSessions.computeIfAbsent(sessionId, k -> new ChatContext());
        ctx.lastUpdated = System.currentTimeMillis();
        
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
