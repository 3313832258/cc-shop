package com.ccshop.ai.controller;

import com.ccshop.ai.dto.ChatRequest;
import com.ccshop.ai.service.ChatService;
import com.ccshop.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 智能客服接口
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI 智能客服")
public class ChatController {

    private final ChatService chatService;

    /**
     * 普通对话（等待完整回答）
     */
    @PostMapping("/chat")
    @Operation(summary = "AI 对话")
    public Result<String> chat(@RequestBody ChatRequest request) {
        String reply = chatService.chat(request.getMessage());
        return Result.success(reply);
    }

    /**
     * SSE 流式对话（打字机效果）
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 流式对话（SSE）")
    public SseEmitter chatStream(@RequestParam String message) {
        return chatService.chatStream(message);
    }
}
