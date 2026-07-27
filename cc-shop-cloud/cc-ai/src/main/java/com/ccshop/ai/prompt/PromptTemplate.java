package com.ccshop.ai.prompt;

/**
 * Prompt 模板管理
 * 设计 System Prompt 和 RAG 上下文注入模板
 */
public class PromptTemplate {

    /** System Prompt：设定 AI 客服角色 */
    public static final String SYSTEM_PROMPT = """
            你是 CC-Shop 电商平台的智能客服助手，名叫"小C"。

            你的职责：
            1. 解答用户关于商品的咨询（价格、规格、库存、推荐）
            2. 帮助用户了解购物流程（下单、支付、物流、售后）
            3. 提供友好的购物建议

            回答规范：
            - 简洁明了，不超过 200 字
            - 如果有商品信息，基于真实数据回答
            - 如果不确定，诚实告知并建议联系人工客服
            - 语气友好专业，使用中文
            """;

    /** RAG 上下文模板：注入商品信息 */
    public static final String PRODUCT_CONTEXT_TEMPLATE = """
            以下是平台上的相关商品信息，请基于这些信息回答用户问题：
            %s
            """;

    /** 无商品上下文时的提示 */
    public static final String NO_CONTEXT = "（当前没有查询到相关商品信息，请基于通用电商知识回答）";

    /**
     * 构建带商品上下文的 System Prompt
     */
    public static String buildSystemPrompt(String productContext) {
        if (productContext == null || productContext.isBlank()) {
            return SYSTEM_PROMPT + "\n" + NO_CONTEXT;
        }
        return SYSTEM_PROMPT + "\n" + String.format(PRODUCT_CONTEXT_TEMPLATE, productContext);
    }

    /**
     * 格式化单个商品信息为文本
     */
    public static String formatProduct(String name, String price, Integer stock, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("- 商品：").append(name);
        sb.append(" | 价格：").append(price).append("元");
        if (stock != null) {
            sb.append(" | 库存：").append(stock > 0 ? stock + "件" : "缺货");
        }
        if (description != null && !description.isBlank()) {
            sb.append(" | 描述：").append(description);
        }
        return sb.toString();
    }
}
