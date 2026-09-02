package com.smartbus.booking.service.ai;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EntityExtractionService {

    private final TripService tripService;
    private final java.util.Set<String> cachedCities = new java.util.concurrent.CopyOnWriteArraySet<>();
    private volatile boolean isCitiesCached = false;
    private volatile long citiesCacheUpdatedAt = 0L;
    private static final long CITIES_CACHE_TTL_MS = 5 * 60 * 1000L;

    public String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace("đ", "d").replace("Đ", "D");
    }

    private String extractCoreCity(String location) {
        if (location == null) return "";
        // Nếu có dấu phẩy (vd: Bến xe Nước Ngầm, Hà Nội), lấy phần sau cùng
        String[] parts = location.split(",");
        String core = parts[parts.length - 1].trim();
        
        // Loại bỏ các tiền tố không phải tên riêng (Bến xe, Trạm, TP...)
        String[] prefixes = {"Bến xe trung tâm ", "Bến xe khách ", "Bến xe ", "VP ", "Văn phòng ", "Trạm ", "Khách sạn ", "TP. ", "TP ", "Thành phố ", "Tỉnh "};
        
        String lowerCore = core.toLowerCase();
        for (String p : prefixes) {
            if (lowerCore.startsWith(p.toLowerCase())) {
                core = core.substring(p.length()).trim();
                lowerCore = core.toLowerCase(); // Cập nhật lại lowerCore nếu có nhiều tiền tố lồng nhau
            }
        }
        return core;
    }

    private synchronized void ensureCitiesCached() {
        if (!isCitiesCached || System.currentTimeMillis() - citiesCacheUpdatedAt > CITIES_CACHE_TTL_MS) {
            cachedCities.clear();
            try {
                List<Trip> allTrips = tripService.getAllTrips();
                for (Trip t : allTrips) {
                    if (t.getDeparturePoint() != null) {
                        String core = extractCoreCity(t.getDeparturePoint());
                        if (!core.isEmpty()) cachedCities.add(core);
                        cachedCities.add(t.getDeparturePoint().trim()); // Thêm cả tên đầy đủ
                    }
                    if (t.getArrivalPoint() != null) {
                        String core = extractCoreCity(t.getArrivalPoint());
                        if (!core.isEmpty()) cachedCities.add(core);
                        cachedCities.add(t.getArrivalPoint().trim()); // Thêm cả tên đầy đủ
                    }
                }
            } catch (Exception e) {
                log.error("Failed to load trips for city caching", e);
            }
            
            // LUÔN LUÔN nạp sẵn danh sách 63 tỉnh thành để AI đủ thông minh nhận diện địa danh
            // Kể cả khi hệ thống chưa có chuyến xe đi qua đó (để AI có thể trả lời "Không có chuyến")
            cachedCities.addAll(java.util.Arrays.asList(
                "Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Nha Trang", "Đà Lạt", "Cần Thơ", "Vũng Tàu", "Hải Phòng", "Quảng Ngãi", "Sài Gòn",
                "Cà Mau", "Bạc Liêu", "Sóc Trăng", "Hậu Giang", "Kiên Giang", "An Giang", "Đồng Tháp", "Trà Vinh", "Vĩnh Long", "Bến Tre", "Tiền Giang", "Long An",
                "Tây Ninh", "Bình Dương", "Bình Phước", "Đồng Nai", "Bà Rịa", "Bình Thuận", "Ninh Thuận", "Khánh Hòa", "Phú Yên", "Bình Định", "Quảng Nam",
                "Thừa Thiên Huế", "Huế", "Quảng Trị", "Quảng Bình", "Hà Tĩnh", "Nghệ An", "Thanh Hóa", "Ninh Bình", "Nam Định", "Thái Bình", "Hà Nam", "Hưng Yên",
                "Hải Dương", "Quảng Ninh", "Bắc Ninh", "Bắc Giang", "Lạng Sơn", "Thái Nguyên", "Bắc Kạn", "Cao Bằng", "Hà Giang", "Tuyên Quang", "Phú Thọ",
                "Vĩnh Phúc", "Hòa Bình", "Sơn La", "Điện Biên", "Lai Châu", "Lào Cai", "Yên Bái", "Gia Lai", "Kon Tum", "Đắk Lắk", "Đắk Nông", "Lâm Đồng", "Buôn Ma Thuột"
            ));

            isCitiesCached = true;
            citiesCacheUpdatedAt = System.currentTimeMillis();
        }
    }

    public void extractBookingEntities(String nonAccentMsg, ChatContext ctx) {
        String from = null;
        String to = null;
        String date = null;

        ensureCitiesCached();
        Map<String, Integer> foundCities = new HashMap<>();
        
        // Sắp xếp cachedCities theo độ dài giảm dần để ưu tiên match chuỗi dài (tránh "Hà Nội" đè "Bến xe Hà Nội")
        List<String> sortedCities = new java.util.ArrayList<>(cachedCities);
        sortedCities.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (String city : sortedCities) {
            String nonAccentCity = removeAccents(city.toLowerCase());
            int index = nonAccentMsg.indexOf(nonAccentCity);
            if (index != -1) {
                // Kiểm tra xem vị trí này đã được chiếm bởi một chuỗi dài hơn chưa
                boolean isOverlapped = false;
                for (Map.Entry<String, Integer> entry : foundCities.entrySet()) {
                    int existIndex = entry.getValue();
                    int existEnd = existIndex + removeAccents(entry.getKey().toLowerCase()).length();
                    int newEnd = index + nonAccentCity.length();
                    if ((index >= existIndex && index < existEnd) || (newEnd > existIndex && newEnd <= existEnd)) {
                        isOverlapped = true;
                        break;
                    }
                }
                if (!isOverlapped) {
                    String canonicalCity = city.equals("Sài Gòn") ? "Hồ Chí Minh" : city;
                    foundCities.put(canonicalCity, index);
                }
            }
        }

        if (foundCities.size() >= 2) {
            List<Map.Entry<String, Integer>> list = new java.util.ArrayList<>(foundCities.entrySet());
            list.sort(Map.Entry.comparingByValue());
            from = extractCoreCity(list.get(0).getKey());
            to = extractCoreCity(list.get(1).getKey());
            
            if ((nonAccentMsg.contains("ve " + removeAccents(from.toLowerCase())) || nonAccentMsg.contains("den " + removeAccents(from.toLowerCase()))) 
                && nonAccentMsg.contains("tu " + removeAccents(to.toLowerCase()))) {
                String temp = from; from = to; to = temp;
            }
        } else if (foundCities.size() == 1) {
            String rawCity = foundCities.keySet().iterator().next();
            String city = extractCoreCity(rawCity);
            String nonAccentCity = removeAccents(city.toLowerCase());
            if (nonAccentMsg.contains("tu " + nonAccentCity) || (city.equals("Hồ Chí Minh") && nonAccentMsg.contains("tu sai gon"))) {
                from = city;
            } else {
                to = city; 
            }
        }

        LocalDate targetDate = extractDate(nonAccentMsg, LocalDate.now());
        
        if (targetDate != null) date = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (from == null && to != null) {
            if (ctx.from == null && ctx.to != null) {
                String nonAccentTo = removeAccents(to.toLowerCase());
                if (!nonAccentMsg.contains("den " + nonAccentTo) && 
                    !nonAccentMsg.contains("ve " + nonAccentTo) && 
                    !nonAccentMsg.contains("di " + nonAccentTo)) {
                    from = to;
                    to = null;
                }
            }
        }

        if (from != null) ctx.from = from;
        if (to != null) ctx.to = to;
        if (date != null) ctx.date = date;
    }

    LocalDate extractDate(String message, LocalDate now) {
        try {
            Matcher isoOrSlash = Pattern.compile("(?:ngay\\s+)?(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{4}))?").matcher(message);
            if (isoOrSlash.find()) {
                int day = Integer.parseInt(isoOrSlash.group(1));
                int month = Integer.parseInt(isoOrSlash.group(2));
                int year = isoOrSlash.group(3) == null ? now.getYear() : Integer.parseInt(isoOrSlash.group(3));
                LocalDate value = LocalDate.of(year, month, day);
                return isoOrSlash.group(3) == null && value.isBefore(now) ? value.plusYears(1) : value;
            }

            Matcher words = Pattern.compile("ngay\\s+(\\d{1,2})\\s+thang\\s+(\\d{1,2})(?:\\s+nam\\s+(\\d{4}))?").matcher(message);
            if (words.find()) {
                int year = words.group(3) == null ? now.getYear() : Integer.parseInt(words.group(3));
                LocalDate value = LocalDate.of(year, Integer.parseInt(words.group(2)), Integer.parseInt(words.group(1)));
                return words.group(3) == null && value.isBefore(now) ? value.plusYears(1) : value;
            }
        } catch (java.time.DateTimeException ignored) {
            return null;
        }

        if (message.contains("ngay mai") || message.contains("sang mai") || message.contains("toi mai")) return now.plusDays(1);
        if (message.contains("hom nay") || message.contains("chieu nay") || message.contains("toi nay") || message.contains("di luon")) return now;
        if (message.contains("ngay mot") || message.contains("ngay kia")) return now.plusDays(2);
        if (message.contains("cuoi tuan")) return now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        DayOfWeek requestedWeekday = weekdayFromMessage(message);
        if (requestedWeekday != null) {
            if (message.contains("tuan sau")) {
                LocalDate nextMonday = now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return nextMonday.plusDays(requestedWeekday.getValue() - 1L);
            }
            return now.with(TemporalAdjusters.nextOrSame(requestedWeekday));
        }
        if (message.contains("tuan sau")) return now.plusWeeks(1);

        try {
            Matcher dayOnly = Pattern.compile("ngay\\s+(\\d{1,2})(?!\\s*(?:thang|[/-]))").matcher(message);
            if (dayOnly.find()) {
                int day = Integer.parseInt(dayOnly.group(1));
                LocalDate candidate = now.withDayOfMonth(day);
                return candidate.isBefore(now) ? now.plusMonths(1).withDayOfMonth(day) : candidate;
            }
        } catch (java.time.DateTimeException ignored) {
            return null;
        }
        return null;
    }

    private DayOfWeek weekdayFromMessage(String message) {
        if (message.contains("chu nhat")) return DayOfWeek.SUNDAY;
        Matcher matcher = Pattern.compile("thu\\s*([2-7])").matcher(message);
        if (!matcher.find()) return null;
        return DayOfWeek.of(Integer.parseInt(matcher.group(1)) - 1);
    }
}
