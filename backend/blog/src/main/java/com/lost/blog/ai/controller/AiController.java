package com.lost.blog.ai.controller;

import com.lost.blog.ai.dto.AiChatRequest;
import com.lost.blog.ai.dto.AiChatResponse;
import com.lost.blog.ai.dto.AiProviderStatusResponse;
import com.lost.blog.ai.service.AiGatewayService;
import com.lost.blog.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public AiController(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    @GetMapping("/providers")
    public ResponseEntity<List<AiProviderStatusResponse>> getProviders() {
        List<AiProviderStatusResponse> providers = aiGatewayService.getProviderStatus();
        return ResponseEntity.ok(providers);
    }

    @PostMapping("/chat")
    public ResponseEntity<Object> chat(@Valid @RequestBody AiChatRequest request,
                                       HttpServletRequest httpServletRequest) {
        try {
            AiChatResponse response = aiGatewayService.chat(request.getPrompt(), request.getProvider());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), httpServletRequest);
        } catch (IllegalStateException ex) {
            return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), httpServletRequest);
        }
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}
