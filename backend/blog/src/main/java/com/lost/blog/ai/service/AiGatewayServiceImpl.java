package com.lost.blog.ai.service;

import com.lost.blog.ai.config.AiProperties;
import com.lost.blog.ai.dto.AiChatResponse;
import com.lost.blog.ai.dto.AiProviderStatusResponse;
import com.lost.blog.ai.provider.AiProviderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiGatewayServiceImpl implements AiGatewayService {

    private final Map<String, AiProviderClient> providerClientMap;
    private final AiProperties aiProperties;

    @Autowired
    public AiGatewayServiceImpl(List<AiProviderClient> providerClients, AiProperties aiProperties) {
        this.providerClientMap = providerClients.stream()
                .collect(Collectors.toMap(
                        client -> client.getProviderName().toLowerCase(),
                        client -> client
                ));
        this.aiProperties = aiProperties;
    }

    @Override
    public List<AiProviderStatusResponse> getProviderStatus() {
        return providerClientMap.values().stream()
                .sorted(Comparator.comparing(AiProviderClient::getProviderName))
                .map(this::buildStatus)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public AiChatResponse chat(String prompt, String requestedProvider) {
        String providerName = resolveProviderName(requestedProvider);
        AiProviderClient client = providerClientMap.get(providerName);

        if (client == null) {
            throw new IllegalArgumentException("不支持的模型提供商: " + providerName + "，目前仅支持 qwen / deepseek");
        }
        if (!client.isConfigured()) {
            throw new IllegalStateException("模型提供商 " + providerName + " 尚未配置 API Key");
        }

        return client.chat(prompt);
    }

    private AiProviderStatusResponse buildStatus(AiProviderClient client) {
        AiProviderStatusResponse response = new AiProviderStatusResponse();
        response.setProvider(client.getProviderName());
        response.setApiKeyHint(client.getApiKeyHint());
        response.setBaseUrl(client.getBaseUrl());
        response.setModel(client.getModel());
        response.setConfigured(client.isConfigured());
        return response;
    }

    private String resolveProviderName(String requestedProvider) {
        if (StringUtils.hasText(requestedProvider)) {
            return requestedProvider.toLowerCase();
        }
        if (StringUtils.hasText(aiProperties.getDefaultProvider())) {
            return aiProperties.getDefaultProvider().toLowerCase();
        }
        return "qwen";
    }
}
