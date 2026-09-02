package com.smartbus.booking.service;

import com.smartbus.booking.entity.Booking;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TicketExchangeEmailService {

    private final JavaMailSender mailSender;
    private final QrCodeGeneratorService qrCodeGeneratorService;

    public void sendExchangeConfirmation(
            Booking booking,
            Long oldTripId,
            String oldSeats,
            double oldPrice,
            double newPrice,
            double priceDifference) {
        if (booking.getCustomerEmail() == null
                || booking.getCustomerEmail().isBlank()
                || "no-email@smartbus.com".equalsIgnoreCase(booking.getCustomerEmail())) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String qrText = String.format(
                        "Mã đặt vé: %s\nKhách: %s\nGhế mới: %s\nTrạng thái: ĐÃ ĐỔI VÉ",
                        booking.getTicketCode(),
                        booking.getCustomerName(),
                        String.join(", ", booking.getSeatNumbers()));
                byte[] qrCode = qrCodeGeneratorService.generateQrCodeImage(qrText, 500, 500);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom("tienvuive2006@gmail.com", "Trung Nam Limousine");
                helper.setTo(booking.getCustomerEmail());
                helper.setSubject("Đổi vé thành công - Mã vé " + booking.getTicketCode());
                helper.setText(buildHtml(
                        booking, oldTripId, oldSeats, oldPrice, newPrice, priceDifference), true);
                helper.addInline("exchangeQrCode", new ByteArrayResource(qrCode), "image/png");
                mailSender.send(message);
            } catch (Exception exception) {
                System.err.println("[TicketExchangeEmail] Gửi email đổi vé thất bại: " + exception.getMessage());
            }
        });
    }

    private String buildHtml(
            Booking booking,
            Long oldTripId,
            String oldSeats,
            double oldPrice,
            double newPrice,
            double difference) {
        String differenceContent;
        if (difference < 0) {
            differenceContent = "<span style='color:#059669;font-weight:800'>Hoàn vào ví +"
                    + money(Math.abs(difference)) + "</span>";
        } else if (difference > 0) {
            differenceContent = "<span style='color:#dc2626;font-weight:800'>Đã thanh toán thêm "
                    + money(difference) + "</span>";
        } else {
            differenceContent = "<span style='color:#475569;font-weight:800'>Không phát sinh chênh lệch</span>";
        }

        return "<!doctype html><html><body style='margin:0;background:#f1f5f9;font-family:Arial,sans-serif;color:#0f172a'>"
                + "<div style='max-width:620px;margin:24px auto;background:#fff;border-radius:20px;overflow:hidden;border:1px solid #e2e8f0'>"
                + "<div style='padding:30px;background:#075955;color:#fff;text-align:center'>"
                + "<div style='font-size:13px;font-weight:700;letter-spacing:2px;color:#a7f3d0'>TRUNG NAM LIMOUSINE</div>"
                + "<h1 style='margin:10px 0 4px;font-size:26px'>Đổi vé thành công</h1>"
                + "<p style='margin:0;color:#d1fae5'>Vé điện tử của bạn đã được cập nhật</p></div>"
                + "<div style='padding:30px'>"
                + "<p>Xin chào <b>" + escape(booking.getCustomerName()) + "</b>,</p>"
                + "<p style='color:#475569;line-height:1.6'>Yêu cầu đổi vé <b>" + booking.getTicketCode()
                + "</b> đã hoàn tất. Mã QR cũ không còn được sử dụng; vui lòng dùng mã QR mới bên dưới.</p>"
                + "<div style='background:#f8fafc;border:1px dashed #cbd5e1;border-radius:14px;padding:20px'>"
                + row("Thay đổi", oldTripId.equals(booking.getTrip().getId()) ? "Đổi ghế cùng chuyến" : "Đổi ngày/chuyến")
                + row("Chuyến cũ", "#" + oldTripId + " · Ghế " + escape(oldSeats))
                + row("Chuyến mới", "#" + booking.getTrip().getId() + " · Ghế " + escape(String.join(", ", booking.getSeatNumbers())))
                + row("Hành trình", escape(booking.getTrip().getDeparturePoint()) + " → " + escape(booking.getTrip().getArrivalPoint()))
                + row("Khởi hành", escape(booking.getTrip().getDepartureDate()) + " lúc " + escape(booking.getTrip().getDepartureTime()))
                + row("Giá vé cũ", money(oldPrice))
                + row("Giá vé mới", money(newPrice))
                + "<div style='padding-top:12px;border-top:1px solid #e2e8f0'>" + differenceContent + "</div>"
                + "</div>"
                + "<div style='margin-top:24px;text-align:center;background:#ecfdf5;border-radius:14px;padding:20px'>"
                + "<p style='margin:0 0 12px;font-weight:800;color:#047857'>MÃ QR VÉ MỚI</p>"
                + "<img src='cid:exchangeQrCode' width='210' height='210' style='background:#fff;padding:8px;border-radius:12px' alt='Mã QR vé mới'/>"
                + "<p style='font-size:12px;color:#047857'>Xuất trình mã này cho nhân viên khi lên xe.</p></div>"
                + "</div><div style='padding:18px;text-align:center;background:#f8fafc;color:#94a3b8;font-size:12px'>"
                + "Email xác nhận thay đổi vé từ Trung Nam Limousine</div></div></body></html>";
    }

    private String row(String label, String value) {
        return "<div style='display:flex;justify-content:space-between;gap:20px;margin-bottom:12px'>"
                + "<span style='color:#64748b'>" + label + "</span><b style='text-align:right'>" + value + "</b></div>";
    }

    private String money(double value) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
