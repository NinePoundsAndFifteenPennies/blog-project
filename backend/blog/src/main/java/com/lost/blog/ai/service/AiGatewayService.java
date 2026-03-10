package com.lost.blog.ai.service;

import com.lost.blog.ai.dto.AiChatResponse;
import com.lost.blog.ai.dto.AiProviderStatusResponse;

import java.util.List;

public interface AiGatewayService {
    List<AiProviderStatusResponse> getProviderStatus();

    AiChatResponse chat(String prompt, String requestedProvider);
}
