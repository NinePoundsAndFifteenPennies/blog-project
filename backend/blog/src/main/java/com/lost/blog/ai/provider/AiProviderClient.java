package com.lost.blog.ai.provider;

import com.lost.blog.ai.dto.AiChatResponse;

public interface AiProviderClient {

    String getProviderName();

    boolean isConfigured();

    String getModel();

    String getBaseUrl();

    String getApiKeyHint();

    AiChatResponse chat(String prompt);
}
