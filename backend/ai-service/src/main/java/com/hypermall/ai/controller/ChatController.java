package com.hypermall.ai.controller;

import com.hypermall.ai.dto.ChatRequest;
import com.hypermall.ai.dto.ChatResponse;
import com.hypermall.ai.dto.ChatSessionResponse;
import com.hypermall.ai.service.ChatService;
import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "AI Chatbot for shopping assistance")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Send a chat message", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get user's chat sessions", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<PageResponse<ChatSessionResponse>>> getSessions(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ChatSessionResponse> sessions = chatService.getUserSessions(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(sessions)));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get a specific chat session with messages", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getSession(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        ChatSessionResponse session = chatService.getSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @PostMapping("/sessions/{sessionId}/close")
    @Operation(summary = "Close a chat session", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> closeSession(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        chatService.closeSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session closed", null));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Delete a chat session", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        chatService.deleteSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session deleted", null));
    }
}
