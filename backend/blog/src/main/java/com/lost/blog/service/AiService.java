package com.lost.blog.service;

import com.lost.blog.dto.AiResponse;
import reactor.core.publisher.Flux;

/**
 * AI 服务接口
 * 提供基于 AI 的文章辅助功能
 */
public interface AiService {

    /**
     * 生成文章摘要
     * @param content 文章内容
     * @return AI生成的摘要
     */
    AiResponse generateSummary(String content);

    /**
     * 推荐文章标题
     * @param content 文章内容
     * @return 推荐的标题列表
     */
    AiResponse suggestTitles(String content);

    /**
     * 推荐文章标签
     * @param title 文章标题
     * @param content 文章内容
     * @return 推荐的标签列表
     */
    AiResponse suggestTags(String title, String content);

    /**
     * AI 写作辅助（流式输出）
     * 根据用户提示和已有内容，辅助续写或润色
     * @param prompt 用户提示
     * @param content 已有内容（可选）
     * @return 流式文本输出
     */
    Flux<String> assistWriting(String prompt, String content);

    /**
     * 检查 AI 服务是否可用
     * @return 是否可用
     */
    boolean isAvailable();
}
