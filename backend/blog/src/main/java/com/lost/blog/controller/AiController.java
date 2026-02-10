package com.lost.blog.controller;

import com.lost.blog.dto.AiRequest;
import com.lost.blog.dto.AiResponse;
import com.lost.blog.service.AiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AiController {

    private final AiService aiService;

    @Autowired
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 检查 AI 服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus() {
        return ResponseEntity.ok(Map.of("available", aiService.isAvailable()));
    }

    /**
     * 生成文章摘要
     */
    @PostMapping("/summary")
    public ResponseEntity<AiResponse> generateSummary(@Valid @RequestBody AiRequest request) {
        AiResponse response = aiService.generateSummary(request.getContent());
        return ResponseEntity.ok(response);
    }

    /**
     * 推荐文章标题
     */
    @PostMapping("/suggest-titles")
    public ResponseEntity<AiResponse> suggestTitles(@Valid @RequestBody AiRequest request) {
        AiResponse response = aiService.suggestTitles(request.getContent());
        return ResponseEntity.ok(response);
    }

    /**
     * 推荐文章标签
     */
    @PostMapping("/suggest-tags")
    public ResponseEntity<AiResponse> suggestTags(@Valid @RequestBody AiRequest request) {
        AiResponse response = aiService.suggestTags(request.getTitle(), request.getContent());
        return ResponseEntity.ok(response);
    }

    /**
     * AI 写作辅助（流式输出）
     */
    @PostMapping(value = "/assist", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> assistWriting(@Valid @RequestBody AiRequest request) {
        return aiService.assistWriting(request.getPrompt(), request.getContent());
    }
}
