package com.lost.blog.service;

import com.lost.blog.dto.AdminReportActionRequest;
import com.lost.blog.dto.AdminReportQueryRequest;
import com.lost.blog.dto.AdminReportResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.*;
import com.lost.blog.repository.AdminFormRepository;
import com.lost.blog.repository.NotificationRepository;
import com.lost.blog.repository.ReportRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 举报管理服务实现类（后台管理员使用）
 */
@Service
public class AdminReportServiceImpl implements AdminReportService {

    private static final Logger logger = LoggerFactory.getLogger(AdminReportServiceImpl.class);

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AdminFormRepository adminFormRepository;
    private final NotificationRepository notificationRepository;
    private final AdminLogService adminLogService;

    @Autowired
    public AdminReportServiceImpl(ReportRepository reportRepository,
                                  UserRepository userRepository,
                                  AdminFormRepository adminFormRepository,
                                  NotificationRepository notificationRepository,
                                  AdminLogService adminLogService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.adminFormRepository = adminFormRepository;
        this.notificationRepository = notificationRepository;
        this.adminLogService = adminLogService;
    }

    @Override
    public Page<AdminReportResponse> searchReports(AdminReportQueryRequest query, Pageable pageable) {
        ReportStatus status = null;
        ReportTargetType targetType = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            try {
                status = ReportStatus.valueOf(query.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        if (query.getTargetType() != null && !query.getTargetType().isEmpty()) {
            try {
                targetType = ReportTargetType.valueOf(query.getTargetType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        if (query.getStartDate() != null && !query.getStartDate().isEmpty()) {
            startDate = LocalDate.parse(query.getStartDate(), DateTimeFormatter.ISO_DATE).atStartOfDay();
        }

        if (query.getEndDate() != null && !query.getEndDate().isEmpty()) {
            endDate = LocalDate.parse(query.getEndDate(), DateTimeFormatter.ISO_DATE).atTime(LocalTime.MAX);
        }

        String reporterUsername = (query.getReporterUsername() != null && !query.getReporterUsername().isEmpty())
                ? query.getReporterUsername() : null;
        String reportedUsername = (query.getReportedUsername() != null && !query.getReportedUsername().isEmpty())
                ? query.getReportedUsername() : null;

        Page<Report> reports = reportRepository.searchReports(
                status, targetType, reporterUsername, reportedUsername, startDate, endDate, pageable);

        return reports.map(this::toResponse);
    }

    @Override
    public AdminReportResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到举报: " + reportId));
        return toResponse(report);
    }

    @Override
    @Transactional
    public void processReport(AdminReportActionRequest request, UserDetails currentUser) {
        User admin = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到管理员: " + currentUser.getUsername()));

        Report report = reportRepository.findById(request.getReportId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到举报: " + request.getReportId()));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalArgumentException("该举报已被处理，状态: " + report.getStatus());
        }

        String action = request.getAction().toUpperCase();

        if ("APPROVE".equals(action)) {
            processApproval(report, request, admin);
        } else if ("REJECT".equals(action)) {
            processRejection(report, request, admin);
        } else {
            throw new IllegalArgumentException("不支持的操作类型: " + action);
        }
    }

    @Override
    public long getPendingCount() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }

    private void processApproval(Report report, AdminReportActionRequest request, User admin) {
        // 创建管理表单
        AdminForm form = new AdminForm();
        form.setTitle(request.getFormTitle());
        form.setFormType(AdminFormType.REPORT_APPROVAL);
        form.setReason(request.getReason());
        form.setExtraFields(request.getExtraFields());
        form.setTargetUser(report.getReportedUser());
        form.setAdmin(admin);
        form.setSent(true);
        form.setSentAt(LocalDateTime.now());

        if (report.getTargetType() == ReportTargetType.POST) {
            form.setPostId(report.getTargetId());
            form.setPostTitle(report.getTargetContentPreview());
        } else {
            form.setCommentId(report.getTargetId());
            form.setCommentContentPreview(report.getTargetContentPreview());
        }

        adminFormRepository.save(form);

        // 更新举报状态
        report.setStatus(ReportStatus.APPROVED);
        report.setProcessedAt(LocalDateTime.now());
        report.setAdminFormId(form.getId());
        reportRepository.save(report);

        // 发送通知给被举报者（以"系统管理员"名义）
        Notification reportedNotification = new Notification();
        reportedNotification.setType(NotificationType.REPORTED_CONTENT);
        reportedNotification.setRecipient(report.getReportedUser());
        reportedNotification.setActor(admin);
        String targetDesc = report.getTargetType() == ReportTargetType.POST ? "文章" : "评论";
        reportedNotification.setContent("系统管理员通知：您的" + targetDesc + "「" +
                truncateContent(report.getTargetContentPreview(), 30) + "」被举报且已确认违规，请查看通知单了解详情");
        notificationRepository.save(reportedNotification);

        // 发送通知给举报者
        Notification reporterNotification = new Notification();
        reporterNotification.setType(NotificationType.REPORT_RESULT);
        reporterNotification.setRecipient(report.getReporter());
        reporterNotification.setActor(admin);
        reporterNotification.setContent("您举报的" + targetDesc + "「" +
                truncateContent(report.getTargetContentPreview(), 30) + "」已被确认违规，感谢您的举报");
        notificationRepository.save(reporterNotification);

        // 记录审计日志
        Long logPostId = report.getTargetType() == ReportTargetType.POST ? report.getTargetId() : null;
        Long logCommentId = report.getTargetType() == ReportTargetType.COMMENT ? report.getTargetId() : null;
        adminLogService.log(AdminLogType.REPORT_APPROVE,
                request.getFormTitle(), request.getReason(), request.getExtraFields(),
                admin, logPostId, null, logCommentId, null, null, null, null, null,
                report.getReportedUser().getId(), report.getReportedUser().getUsername(),
                form.getId());

        logger.info("管理员 {} 通过了举报 #{}", admin.getUsername(), report.getId());
    }

    private void processRejection(Report report, AdminReportActionRequest request, User admin) {
        // 创建管理表单
        AdminForm form = new AdminForm();
        form.setTitle(request.getFormTitle());
        form.setFormType(AdminFormType.REPORT_REJECTION);
        form.setReason(request.getReason());
        form.setExtraFields(request.getExtraFields());
        form.setTargetUser(report.getReporter());
        form.setAdmin(admin);
        form.setSent(true);
        form.setSentAt(LocalDateTime.now());
        adminFormRepository.save(form);

        // 更新举报状态
        report.setStatus(ReportStatus.REJECTED);
        report.setProcessedAt(LocalDateTime.now());
        report.setAdminFormId(form.getId());
        reportRepository.save(report);

        // 只发送通知给举报者
        Notification notification = new Notification();
        notification.setType(NotificationType.REPORT_RESULT);
        notification.setRecipient(report.getReporter());
        notification.setActor(admin);
        String targetDesc = report.getTargetType() == ReportTargetType.POST ? "文章" : "评论";
        notification.setContent("您举报的" + targetDesc + "「" +
                truncateContent(report.getTargetContentPreview(), 30) + "」经审核未确认违规");
        notificationRepository.save(notification);

        // 记录审计日志
        Long logPostId = report.getTargetType() == ReportTargetType.POST ? report.getTargetId() : null;
        Long logCommentId = report.getTargetType() == ReportTargetType.COMMENT ? report.getTargetId() : null;
        adminLogService.log(AdminLogType.REPORT_REJECT,
                request.getFormTitle(), request.getReason(), request.getExtraFields(),
                admin, logPostId, null, logCommentId, null, null, null, null, null,
                report.getReporter().getId(), report.getReporter().getUsername(),
                form.getId());

        logger.info("管理员 {} 驳回了举报 #{}", admin.getUsername(), report.getId());
    }

    private AdminReportResponse toResponse(Report report) {
        AdminReportResponse response = new AdminReportResponse();
        response.setId(report.getId());
        response.setTargetType(report.getTargetType().name());
        response.setTargetId(report.getTargetId());
        response.setTargetContentPreview(report.getTargetContentPreview());
        response.setReason(report.getReason());
        response.setStatus(report.getStatus().name());
        response.setAdminFormId(report.getAdminFormId());
        response.setCreatedAt(report.getCreatedAt());
        response.setProcessedAt(report.getProcessedAt());

        User reporter = report.getReporter();
        if (reporter != null) {
            response.setReporterId(reporter.getId());
            response.setReporterUsername(reporter.getUsername());
            response.setReporterNickname(reporter.getNickname());
            response.setReporterAvatarUrl(reporter.getAvatarUrl());
        }

        User reportedUser = report.getReportedUser();
        if (reportedUser != null) {
            response.setReportedUserId(reportedUser.getId());
            response.setReportedUsername(reportedUser.getUsername());
            response.setReportedNickname(reportedUser.getNickname());
            response.setReportedAvatarUrl(reportedUser.getAvatarUrl());
        }

        return response;
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return null;
        String clean = content.replaceAll("[#*`>\\[\\]()!]", "").trim();
        if (clean.length() <= maxLength) return clean;
        return clean.substring(0, maxLength - 3) + "...";
    }
}
