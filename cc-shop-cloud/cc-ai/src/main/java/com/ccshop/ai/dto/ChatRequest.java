package com.ccshop.ai.dto;

import lombok.Data;

/**
 * 聊天请求
 */
@Data
public class ChatRequest {

    /** 用户消息 */
    private String message;

    /** 会话 ID（用于多轮对话，可选） */
    private String conversationId;
}
