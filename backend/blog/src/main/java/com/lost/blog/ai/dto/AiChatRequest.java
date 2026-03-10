package com.lost.blog.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiChatRequest {

    @NotBlank(message = "prompt 不能为空")
    @Size(max = 4000, message = "prompt 长度不能超过 4000 个字符")
    private String prompt;

    // 可选：qwen / deepseek；不传时走默认 provider
    private String provider;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
