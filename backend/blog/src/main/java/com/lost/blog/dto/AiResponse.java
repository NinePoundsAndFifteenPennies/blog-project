package com.lost.blog.dto;

import java.util.List;

public class AiResponse {

    private String result;
    private List<String> suggestions;

    public AiResponse() {}

    public AiResponse(String result) {
        this.result = result;
    }

    public AiResponse(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
}
