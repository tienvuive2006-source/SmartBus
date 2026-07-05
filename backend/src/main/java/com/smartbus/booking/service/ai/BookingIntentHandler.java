package com.smartbus.booking.service.ai;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(3)
@RequiredArgsConstructor
public class BookingIntentHandler implements AiIntentHandler {
    private final TripService tripService;
    private final EntityExtractionService entityService;

    @Override
    public boolean canHandle(String nonAccentMsg) {
        return true; // Fallback handler, always handles if nothing else does
    }

    @Override
    public Map<String, Object> handle(String nonAccentMsg, String authHeader, ChatContext ctx, String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
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

        String searchDate = (ctx.date != null) ? ctx.date : "";
        boolean isClosestDate = nonAccentMsg.contains("gan nhat") || nonAccentMsg.contains("som nhat") || nonAccentMsg.contains("nhanh nhat");
        
        List<Trip> trips = tripService.searchTrips(ctx.from, ctx.to, searchDate);
        LocalDate now = LocalDate.now();

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
                ctx.date = closestDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String finalDate = ctx.date;
                List<Trip> closestTrips = new java.util.ArrayList<>();
                for (Trip t : trips) {
                    if (t.getDepartureDate() != null && t.getDepartureDate().startsWith(finalDate)) {
                        closestTrips.add(t);
                    }
                }
                trips = closestTrips;
            }
        }

        String sort = "default";
        if (nonAccentMsg.contains("re nhat") || nonAccentMsg.contains("gia re") || nonAccentMsg.contains("thap nhat") || nonAccentMsg.contains("beo nhat") || nonAccentMsg.contains("tiet kiem")) {
            sort = "price_asc";
        } else if (nonAccentMsg.contains("dat nhat") || nonAccentMsg.contains("vip nhat") || nonAccentMsg.contains("sang nhat") || nonAccentMsg.contains("mac nhat") || nonAccentMsg.contains("cao nhat") || nonAccentMsg.contains("xin nhat")) {
            sort = "price_desc";
        }

        String detectedCompany = "all";
        String detectedBusType = "all";
        String normalizedMsg = nonAccentMsg.replace("-", "").replace(" ", "");
        
        for (Trip t : trips) {
            String normalizedCompanyName = entityService.removeAccents(t.getCompanyName().toLowerCase()).replace("-", "").replace(" ", "");
            if (normalizedMsg.contains(normalizedCompanyName)) {
                detectedCompany = t.getCompanyName();
            }
            
            if (t.getBusType() != null) {
                String[] typeWords = entityService.removeAccents(t.getBusType().toLowerCase()).split("\\s+");
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

        if (!detectedCompany.equals("all") || !detectedBusType.equals("all")) {
            List<Trip> filteredTrips = new java.util.ArrayList<>();
            for (Trip t : trips) {
                boolean ok = true;
                if (!detectedCompany.equals("all") && !t.getCompanyName().equals(detectedCompany)) ok = false;
                
                if (!detectedBusType.equals("all") && t.getBusType() != null) {
                    String dbBusType = entityService.removeAccents(t.getBusType().toLowerCase());
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
        
        return response;
    }
}
