package com.hypermall.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IntentDetectionService {

    private static final Map<String, Pattern> INTENT_PATTERNS = Map.ofEntries(
            Map.entry("SEARCH_PRODUCT", Pattern.compile(
                    ".*(tìm|kiếm|search|muốn mua|cần mua|có bán|xem|cho tôi|gợi ý|recommend).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("CHECK_ORDER", Pattern.compile(
                    ".*(đơn hàng|order|tracking|theo dõi|giao hàng|ship|vận chuyển).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("COMPARE_PRODUCTS", Pattern.compile(
                    ".*(so sánh|compare|khác nhau|giống nhau|tốt hơn|nên chọn).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("PRICE_INQUIRY", Pattern.compile(
                    ".*(giá|price|bao nhiêu|cost|tiền|rẻ|đắt|khuyến mãi|giảm giá|voucher).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("PRODUCT_INFO", Pattern.compile(
                    ".*(thông tin|chi tiết|spec|tính năng|feature|mô tả|description|chất liệu|size|kích thước).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("PAYMENT", Pattern.compile(
                    ".*(thanh toán|payment|pay|trả|chuyển khoản|ví|momo|vnpay|cod).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("RETURN_REFUND", Pattern.compile(
                    ".*(đổi trả|refund|hoàn tiền|return|bảo hành|warranty|hỏng|lỗi).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("SUPPORT", Pattern.compile(
                    ".*(hỗ trợ|support|giúp|help|liên hệ|contact|hotline|cskh).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("GREETING", Pattern.compile(
                    "^(hi|hello|xin chào|chào|hey|alo).*", Pattern.CASE_INSENSITIVE)),
            Map.entry("THANKS", Pattern.compile(
                    ".*(cảm ơn|thank|thanks|tks).*", Pattern.CASE_INSENSITIVE))
    );

    public String detectIntent(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }

        String normalizedMessage = message.toLowerCase().trim();

        for (Map.Entry<String, Pattern> entry : INTENT_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(normalizedMessage).matches()) {
                log.debug("Detected intent: {} for message: {}", entry.getKey(), message);
                return entry.getKey();
            }
        }

        return "GENERAL";
    }

    public double getConfidence(String message, String intent) {
        if ("UNKNOWN".equals(intent) || "GENERAL".equals(intent)) {
            return 0.5;
        }

        Pattern pattern = INTENT_PATTERNS.get(intent);
        if (pattern != null && pattern.matcher(message.toLowerCase()).matches()) {
            return 0.85;
        }

        return 0.6;
    }
}
