package com.lost.blog.controller;

import com.lost.blog.dto.ReportRequest;
import com.lost.blog.service.ReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 举报控制器（前台用户使用）
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 提交举报
     */
    @PostMapping
    public ResponseEntity<?> submitReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        reportService.submitReport(request, currentUser);
        return ResponseEntity.ok().body("举报提交成功，我们将尽快处理");
    }
}
