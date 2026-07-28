package com.smartbus.booking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiArticleGeneratorService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param topic
     * @return
     */
    public Map<String, String> generateArticle(String topic) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new RuntimeException(
                    "Chưa cấu hình API Key. Vui lòng thêm gemini.api.key vào file application.properties.");
        }

        // Danh sách các model từ xịn/mới nhất đến các bản nhẹ để dự phòng
        List<String> models = List.of(
                "gemini-2.5-flash",
                "gemini-3.5-flash",
                "gemini-2.0-flash-lite",
                "gemini-flash-latest",
                "gemini-pro-latest");

        String lastErrorMessage = "Tất cả các máy chủ AI đều đang bận.";

        for (String modelName : models) {
            System.out.println("Đang thử gọi AI Model: " + modelName + "...");
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName
                        + ":generateContent?key=" + geminiApiKey;

                                String systemPrompt = "Bạn là chuyên gia Content Marketing của Nhà xe Trung Nam.\n"
                        + "\n"
                        + "Nhiệm vụ của bạn là viết một bài PR/Khuyến mãi chuyên nghiệp, chuẩn SEO, có văn phong lôi cuốn và thuyết phục khách hàng đặt vé.\n"
                        + "\n"
                        + "YÊU CẦU ĐỊNH DẠNG ĐẦU RA (QUAN TRỌNG NHẤT):\n"
                        + "Bạn bắt buộc phải trả về duy nhất một chuỗi JSON hợp lệ với đúng 3 khóa sau (không có khóa nào khác):\n"
                        + "{\n"
                        + "  \"title\": \"(Một tiêu đề bài viết giật tít, hấp dẫn, độ dài 10-15 từ)\",\n"
                        + "  \"summary\": \"(Một đoạn tóm tắt ngắn gọn khoảng 2-3 câu, chứa các từ khóa SEO để hiển thị ra trang chủ)\",\n"
                        + "  \"content\": \"(Toàn bộ nội dung bài viết dưới dạng HTML RAW như mô tả bên dưới)\"\n"
                        + "}\n"
                        + "\n"
                        + "TUYỆT ĐỐI KHÔNG BỌC TRONG KHỐI ```json, KHÔNG GIẢI THÍCH, CHỈ TRẢ VỀ CHUỖI JSON HỢP LỆ ĐỂ MÁY TÍNH CÓ THỂ PARSE.\n"
                        + "\n"
                        + "Yêu cầu cho phần 'content':\n"
                        + "- Viết tối thiểu 800 từ.\n"
                        + "- Không sử dụng Markdown trong content.\n"
                        + "- Không sử dụng CSS hoặc JavaScript.\n"
                        + "- Không bọc trong ```html.\n"
                        + "- Không tạo thẻ <html>, <head>, <body>.\n"
                        + "- Không tạo tiêu đề <h1>.\n"
                        + "- Chỉ sử dụng các thẻ: <h2>, <h3>, <p>, <ul>, <li>, <strong>, <em>.\n"
                        + "- TUYỆT ĐỐI KHÔNG sử dụng thẻ <br>. Hãy dùng thẻ <p> để ngắt đoạn.\n"
                        + "- TUYỆT ĐỐI KHÔNG tạo các thẻ <p> rỗng (ví dụ: <p></p> hoặc <p>&nbsp;</p>) để làm khoảng trắng. Không tạo khoảng trắng dư thừa giữa các dòng.\n"
                        + "- TUYỆT ĐỐI KHÔNG chèn emoji/icon vào trường 'title' và các thẻ tiêu đề (<h2>, <h3>) trong bài viết.\n"
                        + "- Chỉ được phép chèn emoji/icon (🚌, ✨, 🎉, 📞, 📍...) ở các gạch đầu dòng (<li>) hoặc nội dung (<p>) quan trọng, không nhồi nhét quá nhiều.\n"
                        + "- BẮT BUỘC chèn các đoạn gợi ý vị trí đặt ảnh vào những chỗ hợp lý trong bài viết bằng đoạn mã HTML sau: <p style=\"text-align: center; color: #10b981;\"><em>[📸 Gợi ý chèn ảnh: Mô tả bức ảnh cần chèn tại đây]</em></p>\n"
                        + "\n"
                        + "Bài viết phần 'content' phải bao gồm đầy đủ:\n"
                        + "- Giới thiệu\n"
                        + "- Điểm nổi bật\n"
                        + "- Quyền lợi khách hàng\n"
                        + "- Giá vé hoặc ưu đãi (chỉ nếu được cung cấp)\n"
                        + "- Lý do nên chọn Nhà xe Trung Nam\n"
                        + "- Hướng dẫn đặt vé\n"
                        + "- Câu hỏi thường gặp (FAQ)\n"
                        + "- Kết luận và lời kêu gọi hành động.\n"
                        + "\n"
                        + "Nếu chủ đề không cung cấp số liệu hoặc giá vé thì KHÔNG được tự bịa ra.\n"
                        + "\n"
                        + "Trong mục \"Hướng dẫn đặt vé\", BẠN PHẢI CHÈN CHÍNH XÁC ĐOẠN MÃ HTML DƯỚI ĐÂY (không được tự ý sửa hay bỏ sót):\n"
                        + "<p><strong>Bước 1:</strong> Truy cập trang chủ website, chọn Điểm khởi hành, Điểm đến và Ngày đi mong muốn.</p>\n"
                        + "<p><strong>Bước 2:</strong> Lựa chọn chuyến xe phù hợp, chọn vị trí ghế/giường nằm yêu thích và điền thông tin hành khách.</p>\n"
                        + "<p><strong>Bước 3:</strong> Xác nhận thông tin, tiến hành thanh toán an toàn và nhận mã vé điện tử ngay lập tức.</p>\n"
                        + "<p><em>*Lưu ý: Nếu cần hỗ trợ khẩn cấp, quý khách vui lòng liên hệ trực tiếp qua Hotline/Zalo: 0367093771.</em></p>\n"
                        + "\n"
                        + "Chủ đề bài viết là: ";

                Map<String, Object> requestBody = new HashMap<>();
                Map<String, Object> content = new HashMap<>();
                Map<String, Object> part = new HashMap<>();

                part.put("text", systemPrompt + topic);
                        
                        
                content.put("parts", List.of(part));
                requestBody.put("contents", List.of(content));

                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("responseMimeType", "application/json");
                requestBody.put("generationConfig", generationConfig);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                        
                        
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                String generatedText = rootNode
                        .path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();

                        
                        
                // Clean the generated JSON string just in case it's wrapped in markdown
                generatedText = generatedText.replaceAll("^```json\\s*", "")
                        .replaceAll("^```\\s*", "")
                        .replaceAll("(?s)```$", "")
                        .trim();

                // Parse the JSON string into a Map
                Map<String, String> result = objectMapper.readValue(generatedText,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                        });

                System.out.println("✅ Thành công với model: " + modelName);
                return result;

            } catch (Exception e) {
                System.err.println("❌ Model " + modelName + " thất bại: " + e.getMessage());
                lastErrorMessage = e.getMessage();
                // Bỏ qua lỗi và chạy tiếp vòng lặp để thử model tiếp theo
            }
        }

        // Nếu tất cả các model đều thất bại
        throw new RuntimeException("Toàn bộ máy chủ AI đều báo lỗi hoặc quá tải. Lỗi cuối cùng: " + lastErrorMessage);
    }
}
