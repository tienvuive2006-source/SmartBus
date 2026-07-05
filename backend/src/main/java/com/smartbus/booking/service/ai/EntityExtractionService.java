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
    private boolean isCitiesCached = false;

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
        if (!isCitiesCached) {
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
                    foundCities.put(city, index);
                }
            }
            
            if ((city.equals("Hồ Chí Minh") || city.equals("Sài Gòn")) && nonAccentMsg.contains("sai gon")) {
                foundCities.put("Hồ Chí Minh", nonAccentMsg.indexOf("sai gon"));
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
}
