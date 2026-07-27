package com.ccshop.ai.service;

import com.ccshop.ai.config.DashScopeConfig;
import com.ccshop.ai.prompt.PromptTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 聊天核心服务
 * 调用通义千问 API，支持普通和 SSE 流式两种模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final DashScopeConfig dashScopeConfig;
    private final WebClient webClient;
    private final ProductContextService productContextService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 普通对话（非流式）
     */
    public String chat(String userMessage) {
        String systemPrompt = buildSystemPrompt(userMessage);

        ObjectNode request = buildRequest(userMessage, systemPrompt, false);

        String response = webClient.post()
                .uri(dashScopeConfig.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + dashScopeConfig.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractContent(response);
    }

    /**
     * SSE 流式对话
     * 使用 SseEmitter 实现服务端推送
     */
    public SseEmitter chatStream(String userMessage) {
        SseEmitter emitter = new SseEmitter(60_000L); // 60 秒超时

        String systemPrompt = buildSystemPrompt(userMessage);
        ObjectNode request = buildRequest(userMessage, systemPrompt, true);

        // 异步处理流式响应
        Flux<String> flux = webClient.post()
                .uri(dashScopeConfig.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + dashScopeConfig.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request.toString())
                .retrieve()
                .bodyToFlux(String.class);

        flux.subscribe(
                chunk -> {
                    try {
                        String content = extractStreamContent(chunk);
                        if (content != null && !content.isEmpty()) {
                            emitter.send(SseEmitter.event()
                                    .data(content, MediaType.TEXT_PLAIN));
                        }
                    } catch (IOException e) {
                        log.warn("SSE 发送失败: {}", e.getMessage());
                    }
                },
                error -> {
                    log.error("SSE 流式请求失败: {}", error.getMessage());
                    try {
                        emitter.send(SseEmitter.event()
                                .data("[抱歉，AI 服务暂时不可用，请稍后再试]", MediaType.TEXT_PLAIN));
                    } catch (IOException e) {
                        // ignore
                    }
                    emitter.completeWithError(error);
                },
                () -> {
                    try {
                        emitter.send(SseEmitter.event().data("[DONE]", MediaType.TEXT_PLAIN));
                    } catch (IOException e) {
                        // ignore
                    }
                    emitter.complete();
                }
        );

        return emitter;
    }

    /**
     * 构建 System Prompt（含 RAG 商品上下文）
     */
    private String buildSystemPrompt(String userMessage) {
        String productContext = productContextService.getProductContext(userMessage);
        return PromptTemplate.buildSystemPrompt(productContext);
    }

    /**
     * 构建 API 请求体（OpenAI 兼容格式）
     */
    private ObjectNode buildRequest(String userMessage, String systemPrompt, boolean stream) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", dashScopeConfig.getModel());
        request.put("stream", stream);

        ArrayNode messages = objectMapper.createArrayNode();

        // System 消息
        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // User 消息
        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        request.set("messages", messages);
        return request;
    }

    /**
     * 从非流式响应中提取内容
     */
    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    return message.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.error("解析响应失败: {}", e.getMessage());
        }
        return "抱歉，AI 回答解析失败";
    }

    /**
     * 从 SSE chunk 中提取内容
     * 通义千问返回格式：data: {"choices":[{"delta":{"content":"字"}}]}
     */
    private String extractStreamContent(String chunk) {
        try {
            // 跳过 [DONE] 标记
            if (chunk.contains("[DONE]")) {
                return null;
            }

            // 解析 SSE data 行
            String data = chunk;
            if (data.startsWith("data:")) {
                data = data.substring(5).trim();
            }
            if (data.isEmpty()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    return delta.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.debug("解析 stream chunk 失败: {}", e.getMessage());
        }
        return null;
    }
}
