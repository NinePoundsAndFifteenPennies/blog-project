package com.lost.blog.service;

import com.lost.blog.dto.AiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceImpl.class);

    private final ChatClient chatClient;
    private final boolean aiEnabled;

    public AiServiceImpl(
            ChatClient.Builder chatClientBuilder,
            @Value("${app.ai.enabled:true}") boolean aiEnabled) {
        this.chatClient = chatClientBuilder.build();
        this.aiEnabled = aiEnabled;
    }

    @Override
    public AiResponse generateSummary(String content) {
        checkAvailability();

        String truncated = truncateContent(content, 3000);
        String result = chatClient.prompt()
                .system("你是一个博客文章摘要助手。请根据文章内容生成一段简洁的中文摘要，"
                        + "不超过150字，要求准确概括文章的核心观点。只输出摘要内容，不要添加任何前缀或解释。")
                .user(truncated)
                .call()
                .content();

        return new AiResponse(result);
    }

    @Override
    public AiResponse suggestTitles(String content) {
        checkAvailability();

        String truncated = truncateContent(content, 2000);
        String result = chatClient.prompt()
                .system("你是一个博客标题推荐助手。请根据文章内容推荐5个吸引人的中文标题，"
                        + "每个标题一行，不要编号，不要添加任何其他文字。")
                .user(truncated)
                .call()
                .content();

        List<String> titles = parseLines(result);
        return new AiResponse(titles);
    }

    @Override
    public AiResponse suggestTags(String title, String content) {
        checkAvailability();

        String truncated = truncateContent(content, 2000);
        String input = "标题：" + (title != null ? title : "") + "\n内容：" + truncated;

        String result = chatClient.prompt()
                .system("你是一个博客标签推荐助手。请根据文章的标题和内容推荐5-8个合适的标签，"
                        + "每个标签一行，标签应简洁（1-4个词），不要编号，不要添加任何其他文字。")
                .user(input)
                .call()
                .content();

        List<String> tags = parseLines(result);
        return new AiResponse(tags);
    }

    @Override
    public Flux<String> assistWriting(String prompt, String content) {
        checkAvailability();

        String userMessage = content != null && !content.isBlank()
                ? "已有内容：\n" + truncateContent(content, 2000) + "\n\n用户需求：" + prompt
                : prompt;

        return chatClient.prompt()
                .system("你是一个专业的博客写作助手。请根据用户的需求和已有内容，"
                        + "用Markdown格式提供高质量的中文写作辅助。保持内容专业、流畅、有深度。")
                .user(userMessage)
                .stream()
                .content();
    }

    @Override
    public boolean isAvailable() {
        return aiEnabled;
    }

    private void checkAvailability() {
        if (!aiEnabled) {
            throw new IllegalStateException("AI 功能未启用，请配置 API Key");
        }
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
    }

    private List<String> parseLines(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
