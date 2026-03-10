package com.lost.blog.ai.controller;

import com.lost.blog.ai.config.AiProperties;
import com.lost.blog.ai.dto.AiChatRequest;
import com.lost.blog.ai.dto.AiChatResponse;
import com.lost.blog.ai.dto.AiProviderStatusResponse;
import com.lost.blog.ai.service.AiGatewayService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiGatewayService aiGatewayService;
    private final AiProperties aiProperties;

    @Autowired
    public AiController(AiGatewayService aiGatewayService, AiProperties aiProperties) {
        this.aiGatewayService = aiGatewayService;
        this.aiProperties = aiProperties;
    }

    @GetMapping("/providers")
    public ResponseEntity<?> getProviders(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        List<AiProviderStatusResponse> providers = aiGatewayService.getProviderStatus();
        return ResponseEntity.ok(providers);
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@AuthenticationPrincipal UserDetails currentUser,
                                  @Valid @RequestBody AiChatRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        String prompt = request.getPrompt();
        if (prompt != null && prompt.length() > aiProperties.getMaxPromptLength()) {
            return ResponseEntity.badRequest()
                    .body("prompt 长度不能超过 " + aiProperties.getMaxPromptLength() + " 个字符");
        }

        try {
            AiChatResponse response = aiGatewayService.chat(prompt, request.getProvider());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        }
    }
}
