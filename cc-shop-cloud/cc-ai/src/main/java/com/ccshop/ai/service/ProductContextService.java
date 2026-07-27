package com.ccshop.ai.service;

import com.ccshop.ai.feign.ProductClient;
import com.ccshop.ai.prompt.PromptTemplate;
import com.ccshop.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 商品上下文服务
 * 根据用户问题检索相关商品信息，注入到 Prompt 中
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductContextService {

    private final ProductClient productClient;

    /**
     * 根据用户问题获取相关商品上下文
     * 策略：从用户消息中提取关键词搜索商品，如果没有关键词则返回热门商品
     */
    public String getProductContext(String userMessage) {
        try {
            // 尝试用关键词搜索
            String keyword = extractKeyword(userMessage);
            if (keyword != null) {
                return searchByKeyword(keyword);
            }

            // 无关键词时返回热门商品（前 10 个）
            return getHotProducts();

        } catch (Exception e) {
            log.warn("获取商品上下文失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从用户消息中提取搜索关键词
     * 简单实现：提取常见商品类目词
     */
    private String extractKeyword(String message) {
        // 常见商品关键词
        String[] keywords = {"手机", "电脑", "笔记本", "平板", "耳机", "手表", "相机",
                "电视", "冰箱", "洗衣机", "空调", "音箱", "键盘", "鼠标",
                "充电器", "数据线", "保护壳", "背包", "鞋", "衣服",
                "推荐", "便宜", "优惠", "打折", "新品"};

        for (String kw : keywords) {
            if (message.contains(kw)) {
                return kw;
            }
        }
        return null;
    }

    /**
     * 按关键词搜索商品
     */
    private String searchByKeyword(String keyword) {
        Result<List<ProductClient.ProductItem>> result = productClient.search(keyword);
        if (result == null || !result.ok() || result.getData() == null || result.getData().isEmpty()) {
            log.debug("搜索商品无结果: {}", keyword);
            return null;
        }

        List<ProductClient.ProductItem> products = result.getData().stream()
                .limit(5) // 最多 5 个商品
                .collect(Collectors.toList());

        return products.stream()
                .map(p -> PromptTemplate.formatProduct(
                        p.getName(),
                        p.getPrice() != null ? p.getPrice().toString() : "未知",
                        p.getStock(),
                        truncate(p.getDescription(), 50)))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 获取热门商品（前 10 个）
     */
    private String getHotProducts() {
        Result<ProductClient.ProductListResult> result = productClient.list(1, 10);
        if (result == null || !result.ok() || result.getData() == null) {
            return null;
        }

        List<ProductClient.ProductItem> products = result.getData().getRecords();
        if (products == null || products.isEmpty()) {
            return null;
        }

        return products.stream()
                .map(p -> PromptTemplate.formatProduct(
                        p.getName(),
                        p.getPrice() != null ? p.getPrice().toString() : "未知",
                        p.getStock(),
                        truncate(p.getDescription(), 50)))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 截断字符串
     */
    private String truncate(String text, int maxLen) {
        if (text == null) return null;
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
