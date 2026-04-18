package com.hypermall.ai.controller;

import com.hypermall.ai.dto.request.ChatRequest;
import com.hypermall.ai.dto.response.ChatResponse;
import com.hypermall.ai.service.ChatService;
import com.hypermall.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Shopper-facing AI assistant APIs")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with HyperMall AI assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
