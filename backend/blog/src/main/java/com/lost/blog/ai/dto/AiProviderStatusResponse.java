package com.lost.blog.ai.dto;

public class AiProviderStatusResponse {

    private String provider;
    private String baseUrl;
    private String model;
    private boolean configured;
    private String apiKeyHint;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

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

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public String getApiKeyHint() {
        return apiKeyHint;
    }

    public void setApiKeyHint(String apiKeyHint) {
        this.apiKeyHint = apiKeyHint;
    }
}
