package com.lost.blog.ai.provider;

import com.lost.blog.ai.config.AiProperties;
import com.lost.blog.ai.dto.AiChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QwenAiProviderClient implements AiProviderClient {

    private final AiProperties aiProperties;

    @Autowired
    public QwenAiProviderClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public String getProviderName() {
        return "qwen";
    }

    @Override
    public boolean isConfigured() {
        return aiProperties.getQwen().isConfigured();
    }

    @Override
    public String getModel() {
        return aiProperties.getQwen().getModel();
    }

    @Override
    public String getBaseUrl() {
        return aiProperties.getQwen().getBaseUrl();
    }

    @Override
    public String getApiKeyHint() {
        return "请设置环境变量 AI_QWEN_API_KEY";
    }

    @Override
    public AiChatResponse chat(String prompt) {
        AiChatResponse response = new AiChatResponse();
        response.setProvider(getProviderName());
        response.setModel(getModel());
        response.setSimulated(true);
        response.setContent("Qwen 基础链路已接通（当前为模拟响应）。收到内容：" + prompt);
        return response;
    }
}
