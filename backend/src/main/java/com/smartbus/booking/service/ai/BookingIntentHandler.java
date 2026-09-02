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
import java.util.Comparator;

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
    public boolean usesBookingContext() {
        return true;
    }

    @Override
    public Map<String, Object> handle(String nonAccentMsg, String authHeader, ChatContext ctx, String sessionId) {
        Map<String, Object> response = new HashMap<>();

        detectAndRememberFilters(nonAccentMsg, ctx);
        
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
        boolean isClosestDate = nonAccentMsg.contains("gan nhat");
        
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

        String sort = ctx.sort;
        if (nonAccentMsg.contains("re nhat") || nonAccentMsg.contains("gia re") || nonAccentMsg.contains("thap nhat") || nonAccentMsg.contains("beo nhat") || nonAccentMsg.contains("tiet kiem")) {
            sort = "price_asc";
        } else if (nonAccentMsg.contains("dat nhat") || nonAccentMsg.contains("vip nhat") || nonAccentMsg.contains("sang nhat") || nonAccentMsg.contains("mac nhat") || nonAccentMsg.contains("cao nhat") || nonAccentMsg.contains("xin nhat")) {
            sort = "price_desc";
        } else if (nonAccentMsg.contains("som nhat")) {
            sort = "time_asc";
        } else if (nonAccentMsg.contains("nhanh nhat")) {
            sort = "duration_asc";
        }
        ctx.sort = sort;

        String detectedCompany = ctx.company;
        String detectedBusType = ctx.busType;

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
            double minPrice = trips.stream().map(Trip::getPrice).filter(p -> p != null && p > 0)
                    .mapToDouble(Double::doubleValue).min().orElse(0.0);
            
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
            } else if (sort.equals("duration_asc")) {
                sortText = " và đã ưu tiên **chuyến có thời gian di chuyển ngắn nhất**";
                trips.sort(Comparator.comparingInt(this::durationMinutes));
            } else {
                trips.sort(Comparator.comparing(this::departureSortValue));
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

    private void detectAndRememberFilters(String message, ChatContext ctx) {
        String normalizedMsg = compact(message);
        for (Trip trip : tripService.getAllTrips()) {
            if (trip.getCompanyName() != null && !trip.getCompanyName().isBlank()
                    && normalizedMsg.contains(compact(entityService.removeAccents(trip.getCompanyName().toLowerCase())))) {
                ctx.company = trip.getCompanyName();
            }
            if (trip.getBusType() != null) {
                String normalizedType = entityService.removeAccents(trip.getBusType().toLowerCase());
                for (String word : normalizedType.split("\\s+")) {
                    if (word.length() > 2 && !word.matches("\\d+")
                            && !java.util.Set.of("cho", "phong", "dong", "loai", "xe", "trung", "nam", "smartbus").contains(word)
                            && message.contains(word)) {
                        ctx.busType = word;
                    }
                }
            }
        }
        if (message.contains("giuong nam") || message.contains("xe giuong")) ctx.busType = "giuong";
        if (message.contains("limousine")) ctx.busType = "limousine";
        if (message.contains("tat ca nha xe") || message.contains("bo loc nha xe")) ctx.company = "all";
        if (message.contains("tat ca loai xe") || message.contains("bo loc loai xe")) ctx.busType = "all";
    }

    private String compact(String value) {
        return value == null ? "" : value.replace("-", "").replaceAll("\\s+", "");
    }

    private String departureSortValue(Trip trip) {
        return (trip.getDepartureDate() == null ? "9999-12-31" : trip.getDepartureDate()) + " "
                + (trip.getDepartureTime() == null ? "23:59" : trip.getDepartureTime());
    }

    private int durationMinutes(Trip trip) {
        String duration = entityService.removeAccents(trip.getDuration() == null ? "" : trip.getDuration().toLowerCase());
        java.util.regex.Matcher hours = java.util.regex.Pattern.compile("(\\d+)\\s*(?:gio|h)").matcher(duration);
        java.util.regex.Matcher minutes = java.util.regex.Pattern.compile("(\\d+)\\s*(?:phut|p)").matcher(duration);
        int total = 0;
        if (hours.find()) total += Integer.parseInt(hours.group(1)) * 60;
        if (minutes.find()) total += Integer.parseInt(minutes.group(1));
        return total > 0 ? total : Integer.MAX_VALUE;
    }
}
