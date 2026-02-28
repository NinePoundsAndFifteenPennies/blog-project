package com.lost.blog.service;

import com.lost.blog.dto.AdminReportActionRequest;
import com.lost.blog.dto.AdminReportQueryRequest;
import com.lost.blog.dto.AdminReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 举报管理服务接口（后台管理员使用）
 */
public interface AdminReportService {

    /**
     * 查询举报列表（支持多条件搜索）
     */
    Page<AdminReportResponse> searchReports(AdminReportQueryRequest query, Pageable pageable);

    /**
     * 获取举报详情
     */
    AdminReportResponse getReportDetail(Long reportId);

    /**
     * 处理举报（通过/驳回）
     */
    void processReport(AdminReportActionRequest request, UserDetails currentUser);

    /**
     * 获取待处理举报数量
     */
    long getPendingCount();
}
