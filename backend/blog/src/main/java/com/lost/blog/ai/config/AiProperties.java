package com.lost.blog.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String defaultProvider = "qwen";
    private int maxPromptLength = 4000;
    private ProviderSettings qwen = new ProviderSettings();
    private ProviderSettings deepseek = new ProviderSettings();

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public int getMaxPromptLength() {
        return maxPromptLength;
    }

    public void setMaxPromptLength(int maxPromptLength) {
        this.maxPromptLength = maxPromptLength;
    }

    public ProviderSettings getQwen() {
        return qwen;
    }

    public void setQwen(ProviderSettings qwen) {
        this.qwen = qwen;
    }

    public ProviderSettings getDeepseek() {
        return deepseek;
    }

    public void setDeepseek(ProviderSettings deepseek) {
        this.deepseek = deepseek;
    }

    public ProviderSettings getProviderSettings(String provider) {
        if ("deepseek".equalsIgnoreCase(provider)) {
            return deepseek;
        }
        return qwen;
    }

    public static class ProviderSettings {
        private String baseUrl;
        private String model;
        private String apiKey;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public boolean isConfigured() {
            return StringUtils.hasText(apiKey);
        }
    }
}
