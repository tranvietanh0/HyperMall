package com.hypermall.ai.service;

import com.hypermall.ai.client.AiClient;
import com.hypermall.ai.client.ClaudeClient;
import com.hypermall.ai.client.GeminiClient;
import com.hypermall.ai.client.OpenAiClient;
import com.hypermall.ai.dto.ChatRequest;
import com.hypermall.ai.dto.ChatResponse;
import com.hypermall.ai.dto.ChatSessionResponse;
import com.hypermall.ai.entity.ChatMessage;
import com.hypermall.ai.entity.ChatSession;
import com.hypermall.ai.repository.ChatMessageRepository;
import com.hypermall.ai.repository.ChatSessionRepository;
import com.hypermall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final OpenAiClient openAiClient;
    private final ClaudeClient claudeClient;
    private final GeminiClient geminiClient;
    private final IntentDetectionService intentDetectionService;

    @Value("${app.ai.default-provider:openai}")
    private String defaultProvider;

    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý mua sắm AI của HyperMall, một sàn thương mại điện tử.

        Nhiệm vụ của bạn:
        1. Giúp khách hàng tìm kiếm sản phẩm phù hợp
        2. Trả lời câu hỏi về sản phẩm, đơn hàng, thanh toán
        3. Hỗ trợ so sánh sản phẩm
        4. Gợi ý sản phẩm dựa trên nhu cầu
        5. Hướng dẫn quy trình mua hàng

        Quy tắc:
        - Trả lời ngắn gọn, chuyên nghiệp, thân thiện
        - Sử dụng tiếng Việt
        - Không đưa ra thông tin sai lệch về giá, khuyến mãi
        - Khi không chắc chắn, hãy hướng dẫn khách hàng liên hệ CSKH
        """;

    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        ChatSession session;

        if (request.getSessionId() != null) {
            session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        } else {
            session = createSession(userId, request.getProvider());
        }

        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getMessage())
                .build();

        // Detect intent
        String intent = intentDetectionService.detectIntent(request.getMessage());
        userMessage.setIntent(intent);

        messageRepository.save(userMessage);

        // Get conversation history
        List<AiClient.Message> history = getConversationHistory(session.getId());

        // Get AI response
        AiClient client = getClient(session.getAiProvider());
        String aiResponse = client.chat(history, SYSTEM_PROMPT);

        // Save AI message
        ChatMessage aiMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content(aiResponse)
                .build();
        messageRepository.save(aiMessage);

        // Update session
        if (session.getTitle() == null && messageRepository.countBySessionId(session.getId()) == 2) {
            session.setTitle(generateTitle(request.getMessage()));
        }
        sessionRepository.save(session);

        return ChatResponse.builder()
                .sessionId(session.getId())
                .messageId(aiMessage.getId())
                .message(aiResponse)
                .intent(intent)
                .timestamp(aiMessage.getCreatedAt())
                .build();
    }

    private ChatSession createSession(Long userId, String provider) {
        String aiProvider = (provider != null && !provider.isBlank()) ? provider : defaultProvider;

        ChatSession session = ChatSession.builder()
                .userId(userId)
                .status(ChatSession.SessionStatus.ACTIVE)
                .aiProvider(aiProvider)
                .build();

        return sessionRepository.save(session);
    }

    private List<AiClient.Message> getConversationHistory(Long sessionId) {
        List<ChatMessage> messages = messageRepository.findRecentMessages(sessionId, PageRequest.of(0, 10));

        // Reverse to get chronological order
        return messages.stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .map(m -> new AiClient.Message(
                        m.getRole() == ChatMessage.MessageRole.USER ? "user" : "assistant",
                        m.getContent()))
                .collect(Collectors.toList());
    }

    private AiClient getClient(String provider) {
        return switch (provider.toLowerCase()) {
            case "claude" -> claudeClient;
            case "gemini" -> geminiClient;
            default -> openAiClient;
        };
    }

    private String generateTitle(String firstMessage) {
        if (firstMessage.length() <= 50) {
            return firstMessage;
        }
        return firstMessage.substring(0, 47) + "...";
    }

    public Page<ChatSessionResponse> getUserSessions(Long userId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
                .map(this::mapToSessionResponse);
    }

    public ChatSessionResponse getSession(Long userId, Long sessionId) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        ChatSessionResponse response = mapToSessionResponse(session);

        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        response.setMessages(messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public void closeSession(Long userId, Long sessionId) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        session.setStatus(ChatSession.SessionStatus.CLOSED);
        session.setClosedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        sessionRepository.delete(session);
    }

    private ChatSessionResponse mapToSessionResponse(ChatSession session) {
        return ChatSessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .status(session.getStatus().name())
                .aiProvider(session.getAiProvider())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private ChatSessionResponse.MessageResponse mapToMessageResponse(ChatMessage message) {
        return ChatSessionResponse.MessageResponse.builder()
                .id(message.getId())
                .role(message.getRole().name())
                .content(message.getContent())
                .intent(message.getIntent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
