package com.smartbus.booking.service.ai;

import com.smartbus.booking.repository.BusTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(1) // Priority 1
@RequiredArgsConstructor
public class FaqIntentHandler implements AiIntentHandler {
    private final BusTypeRepository busTypeRepository;

    @Override
    public boolean canHandle(String nonAccentMsg) {
        return nonAccentMsg.contains("cac loai xe") || nonAccentMsg.contains("nhung loai xe") || nonAccentMsg.contains("co loai xe nao") || nonAccentMsg.contains("nhung dong xe") ||
               nonAccentMsg.contains("hanh ly") ||
               nonAccentMsg.contains("chinh sach huy") || nonAccentMsg.contains("doi ve") || nonAccentMsg.contains("tra ve") || nonAccentMsg.contains("huy ve ra sao") ||
               nonAccentMsg.contains("thanh toan");
    }

    @Override
    public Map<String, Object> handle(String nonAccentMsg, String authHeader, ChatContext ctx, String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        if (nonAccentMsg.contains("cac loai xe") || nonAccentMsg.contains("nhung loai xe") || nonAccentMsg.contains("co loai xe nao") || nonAccentMsg.contains("nhung dong xe")) {
            java.util.Set<String> busTypes = new java.util.TreeSet<>();
            busTypeRepository.findAll().forEach(type -> {
                if (type.getName() != null && !type.getName().isBlank()) busTypes.add(type.getName().trim());
            });
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
            response.put("text", "🔄 **Chính sách hủy vé:**\n\n- Hủy trước giờ khởi hành từ **24 tiếng trở lên**: hoàn **95%** tiền vé.\n- Hủy trước giờ khởi hành từ **12 đến dưới 24 tiếng**: hoàn **70%** tiền vé.\n- Còn **dưới 12 tiếng** hoặc xe đã chạy: không thể hủy.\n\nMức hoàn áp dụng cho vé đã thanh toán không dùng tiền mặt. Vé tiền mặt chưa thanh toán không phát sinh khoản hoàn.");
            response.put("action", "none");
            return response;
        }
        
        if (nonAccentMsg.contains("thanh toan")) {
            response.put("text", "💳 **Hướng dẫn thanh toán:**\n\nHệ thống SmartBus hỗ trợ 2 hình thức thanh toán chính:\n1. **Thanh toán qua Ví SkyPay:** Thanh toán tức thì bằng cách quét mã QR ngân hàng (Hệ thống tự động duyệt vé sau 1-3 phút).\n2. **Thanh toán Tiền mặt:** Đặt vé giữ chỗ và thanh toán trực tiếp cho tài xế khi lên xe.\n\nBạn có thể thoải mái lựa chọn hình thức phù hợp ở bước Thanh toán nhé!");
            response.put("action", "none");
            return response;
        }
        
        return null;
    }
}
