package com.lost.blog.ai.provider;

import com.lost.blog.ai.config.AiProperties;
import com.lost.blog.ai.dto.AiChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeepSeekAiProviderClient implements AiProviderClient {

    private final AiProperties aiProperties;

    @Autowired
    public DeepSeekAiProviderClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }

    @Override
    public boolean isConfigured() {
        return aiProperties.getDeepseek().isConfigured();
    }

    @Override
    public String getModel() {
        return aiProperties.getDeepseek().getModel();
    }

    @Override
    public String getBaseUrl() {
        return aiProperties.getDeepseek().getBaseUrl();
    }

    @Override
    public AiChatResponse chat(String prompt) {
        AiChatResponse response = new AiChatResponse();
        response.setProvider(getProviderName());
        response.setModel(getModel());
        response.setSimulated(true);
        response.setContent("DeepSeek 基础链路已接通（当前为模拟响应）。收到内容：" + prompt);
        return response;
    }
}
