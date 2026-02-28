package com.lost.blog.service;

import com.lost.blog.dto.ReportRequest;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 举报服务接口（前台用户使用）
 */
public interface ReportService {

    /**
     * 用户提交举报
     * @param request 举报请求
     * @param currentUser 当前用户
     */
    void submitReport(ReportRequest request, UserDetails currentUser);
}
