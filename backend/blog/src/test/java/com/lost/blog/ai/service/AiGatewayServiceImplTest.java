package com.lost.blog.ai.service;

import com.lost.blog.ai.config.AiProperties;
import com.lost.blog.ai.dto.AiChatResponse;
import com.lost.blog.ai.provider.AiProviderClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AiGatewayServiceImplTest {

    @Test
    void shouldUseDefaultProviderWhenRequestedProviderMissing() {
        AiProperties properties = new AiProperties();
        properties.setDefaultProvider("qwen");
        AiGatewayService service = new AiGatewayServiceImpl(
                List.of(
                        new FakeProvider("qwen", true),
                        new FakeProvider("deepseek", true)
                ),
                properties
        );

        AiChatResponse response = service.chat("hello", null);
        Assertions.assertEquals("qwen", response.getProvider());
    }

    @Test
    void shouldUseRequestedProviderWhenProvided() {
        AiProperties properties = new AiProperties();
        properties.setDefaultProvider("qwen");
        AiGatewayService service = new AiGatewayServiceImpl(
                List.of(
                        new FakeProvider("qwen", true),
                        new FakeProvider("deepseek", true)
                ),
                properties
        );

        AiChatResponse response = service.chat("hello", "deepseek");
        Assertions.assertEquals("deepseek", response.getProvider());
    }

    @Test
    void shouldThrowWhenProviderApiKeyNotConfigured() {
        AiProperties properties = new AiProperties();
        properties.setDefaultProvider("qwen");
        AiGatewayService service = new AiGatewayServiceImpl(
                List.of(
                        new FakeProvider("qwen", false),
                        new FakeProvider("deepseek", true)
                ),
                properties
        );

        Assertions.assertThrows(IllegalStateException.class, () -> service.chat("hello", null));
    }

    private static class FakeProvider implements AiProviderClient {
        private final String providerName;
        private final boolean configured;

        private FakeProvider(String providerName, boolean configured) {
            this.providerName = providerName;
            this.configured = configured;
        }

        @Override
        public String getProviderName() {
            return providerName;
        }

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public String getModel() {
            return providerName + "-model";
        }

        @Override
        public String getBaseUrl() {
            return "https://example.com";
        }

        @Override
        public AiChatResponse chat(String prompt) {
            AiChatResponse response = new AiChatResponse();
            response.setProvider(providerName);
            response.setModel(getModel());
            response.setContent(prompt);
            response.setSimulated(true);
            return response;
        }
    }
}
