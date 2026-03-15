package com.lost.blog.ai.provider;

import com.lost.blog.ai.config.AiProperties;
import com.lost.blog.ai.dto.AiChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KimiAiProviderClient implements AiProviderClient {

    private final AiProperties aiProperties;

    @Autowired
    public KimiAiProviderClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public String getProviderName() {
        return "kimi";
    }

    @Override
    public boolean isConfigured() {
        return aiProperties.getKimi().isConfigured();
    }

    @Override
    public String getModel() {
        return aiProperties.getKimi().getModel();
    }

    @Override
    public String getBaseUrl() {
        return aiProperties.getKimi().getBaseUrl();
    }

    @Override
    public String getApiKeyHint() {
        return "请设置环境变量 AI_KIMI_API_KEY";
    }

    @Override
    public AiChatResponse chat(String prompt) {
        AiChatResponse response = new AiChatResponse();
        response.setProvider(getProviderName());
        response.setModel(getModel());
        response.setSimulated(true);
        response.setContent("Kimi 基础链路已接通（当前为模拟响应）。收到内容：" + prompt);
        return response;
    }
}
