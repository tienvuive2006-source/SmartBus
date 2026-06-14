package com.smartbus.booking.service;

import com.smartbus.booking.entity.Booking;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private QrCodeGeneratorService qrCodeGeneratorService;

    /**
     * Sends a booking confirmation email with an embedded QR code asynchronously.
     *
     * @param booking The booking entity.
     */
    public void sendBookingConfirmation(Booking booking) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                
                // Set true to indicate multi-part message (for HTML and inline resources)
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(booking.getCustomerEmail());
                helper.setSubject("🎫 Xác nhận đặt vé xe thành công - Mã vé #" + booking.getId());

                // Generate ticket info text for the QR code scanning
                String qrText = String.format(
                        "Mã đặt vé: #%d\nKhách hàng: %s\nSĐT: %s\nHành trình: %s -> %s\nNgày đi: %s\nGiờ đi: %s\nSố ghế: %s\nTổng tiền: %s VND\nTrạng thái: %s",
                        booking.getId(),
                        booking.getCustomerName(),
                        booking.getCustomerPhone(),
                        booking.getTrip().getDeparturePoint(),
                        booking.getTrip().getArrivalPoint(),
                        booking.getTrip().getDepartureDate() != null ? booking.getTrip().getDepartureDate() : "N/A",
                        booking.getTrip().getDepartureTime(),
                        String.join(", ", booking.getSeatNumbers()),
                        formatPrice(booking.getTotalPrice()),
                        booking.getStatus()
                );

                // Generate the QR Code image (250x250 pixels)
                byte[] qrCodeBytes = qrCodeGeneratorService.generateQrCodeImage(qrText, 250, 250);

                // Build the premium HTML template
                String htmlBody = buildHtmlTemplate(booking);
                helper.setText(htmlBody, true);

                // Embed the QR Code image inline (CID matching 'qrcodeImage')
                helper.addInline("qrcodeImage", new ByteArrayResource(qrCodeBytes), "image/png");

                // Send the email
                mailSender.send(message);
                System.out.println("📧 [EmailService] Gửi email xác nhận thành công cho: " + booking.getCustomerEmail());
            } catch (Exception e) {
                System.err.println("❌ [EmailService] Gửi email thất bại: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Formats a double price to standard Vietnamese currency format (e.g. 150.000).
     */
    private String formatPrice(Double price) {
        if (price == null) return "0";
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return currencyFormat.format(price);
    }

    /**
     * Generates a modern, beautifully designed HTML receipt template.
     */
    private String buildHtmlTemplate(Booking booking) {
        String departureDateStr = booking.getTrip().getDepartureDate() != null ? booking.getTrip().getDepartureDate() : "Chưa xác định";
        String seatsJoined = String.join(", ", booking.getSeatNumbers());
        String formattedPrice = formatPrice(booking.getTotalPrice());

        String introText = "CASH".equalsIgnoreCase(booking.getPaymentMethod())
            ? "Cảm ơn bạn đã đặt vé. Yêu cầu đặt vé của bạn đã được ghi nhận thành công. Vui lòng <b>thanh toán trực tiếp cho nhân viên khi lên xe</b>. Dưới đây là thông tin chi tiết vé xe của bạn:"
            : "Cảm ơn bạn đã tin tưởng lựa chọn dịch vụ đặt vé của chúng tôi. Yêu cầu đặt vé của bạn đã được thanh toán và xác nhận thành công. Dưới đây là thông tin chi tiết vé xe của bạn:";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='utf-8'>" +
                "    <title>Xác nhận đặt vé xe thành công</title>" +
                "    <style>" +
                "        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f8fafc; color: #334155; margin: 0; padding: 20px; }" +
                "        .email-container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); border: 1px solid #e2e8f0; }" +
                "        .header { background: linear-gradient(135deg, #4f46e5, #06b6d4); padding: 30px 20px; text-align: center; color: #ffffff; }" +
                "        .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }" +
                "        .header p { margin: 5px 0 0 0; font-size: 14px; opacity: 0.9; }" +
                "        .content { padding: 30px 25px; }" +
                "        .greeting { font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 10px; }" +
                "        .intro { font-size: 14px; line-height: 1.6; color: #64748b; margin-bottom: 25px; }" +
                "        .ticket-card { background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 12px; padding: 20px; margin-bottom: 25px; position: relative; }" +
                "        .ticket-row { display: table; width: 100%; margin-bottom: 12px; }" +
                "        .ticket-cell { display: table-cell; font-size: 14px; padding: 4px 0; }" +
                "        .ticket-label { color: #64748b; width: 35%; font-weight: 500; }" +
                "        .ticket-value { color: #0f172a; font-weight: 600; }" +
                "        .qr-section { text-align: center; padding: 20px; background: #e0f2fe; border-radius: 12px; margin-top: 15px; }" +
                "        .qr-title { font-size: 15px; font-weight: 700; color: #0369a1; margin-bottom: 8px; }" +
                "        .qr-img { border: 4px solid #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); background-color: #ffffff; }" +
                "        .qr-desc { font-size: 12px; color: #0284c7; margin-top: 8px; line-height: 1.4; font-weight: 500; }" +
                "        .footer { background: #f1f5f9; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }" +
                "        .footer a { color: #4f46e5; text-decoration: none; font-weight: 500; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='email-container'>" +
                "        <div class='header'>" +
                "            <h1>🎫 SMART BUS BOOKING</h1>" +
                "            <p>Vé xe điện tử thông minh của bạn</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='greeting'>Xin chào " + booking.getCustomerName() + ",</div>" +
                "            <div class='intro'>" + introText + "</div>" +
                "            " +
                "            <div class='ticket-card'>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Mã đặt vé:</div>" +
                "                    <div class='ticket-cell ticket-value' style='color: #4f46e5; font-size: 16px;'>#" + booking.getId() + "</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Nhà xe:</div>" +
                "                    <div class='ticket-cell ticket-value'>" + booking.getTrip().getCompanyName() + "</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Loại xe:</div>" +
                "                    <div class='ticket-cell ticket-value'>" + booking.getTrip().getBusType() + "</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Hành trình:</div>" +
                "                    <div class='ticket-cell ticket-value' style='color: #0f172a;'>" + booking.getTrip().getDeparturePoint() + " ➔ " + booking.getTrip().getArrivalPoint() + "</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Ngày khởi hành:</div>" +
                "                    <div class='ticket-cell ticket-value'>" + departureDateStr + "</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Giờ xuất phát:</div>" +
                "                    <div class='ticket-cell ticket-value' style='color: #e11d48;'>" + booking.getTrip().getDepartureTime() + "</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Số ghế đặt:</div>" +
                "                    <div class='ticket-cell ticket-value' style='background: #cbd5e1; padding: 2px 8px; border-radius: 4px; display: inline-block; font-weight: bold;'>" + seatsJoined + "</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Tổng tiền:</div>" +
                "                    <div class='ticket-cell ticket-value' style='color: #10b981; font-size: 16px;'>" + formattedPrice + " VND</div>" +
                "                </div>" +
                "                <div class='ticket-row'>" +
                "                    <div class='ticket-cell ticket-label'>Hình thức:</div>" +
                "                    <div class='ticket-cell ticket-value'>" + booking.getPaymentMethod() + "</div>" +
                "                </div>" +
                "            </div>" +
                "            " +
                "            <div class='qr-section'>" +
                "                <div class='qr-title'>MÃ SỐ VÉ ĐIỆN TỬ (QR CODE)</div>" +
                "                <img src='cid:qrcodeImage' class='qr-img' alt='Mã QR Vé Xe' width='200' height='200'>" +
                "                <div class='qr-desc'>Vui lòng xuất trình mã QR này cho tài xế hoặc nhân viên soát vé khi lên xe để xác thực thông tin nhanh chóng.</div>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Hệ thống Đặt Vé Xe Thông Minh Smart Bus Booking</p>" +
                "            <p>Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ <a href='mailto:support@smartbus.com'>support@smartbus.com</a></p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
