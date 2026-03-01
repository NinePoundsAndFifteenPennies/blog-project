package com.lost.blog.service;

import com.lost.blog.dto.ReportRequest;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.model.*;
import com.lost.blog.repository.CommentRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.ReportRepository;
import com.lost.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 举报服务实现类（前台用户使用）
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository,
                            UserRepository userRepository,
                            PostRepository postRepository,
                            CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public void submitReport(ReportRequest request, UserDetails currentUser) {
        User reporter = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        ReportTargetType targetType;
        try {
            targetType = ReportTargetType.valueOf(request.getTargetType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的举报类型: " + request.getTargetType());
        }

        User reportedUser;
        String contentPreview;

        if (targetType == ReportTargetType.POST) {
            Post post = postRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("未找到文章: " + request.getTargetId()));
            reportedUser = post.getUser();
            contentPreview = truncateContent(post.getTitle(), 200);
        } else {
            Comment comment = commentRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("未找到评论: " + request.getTargetId()));
            reportedUser = comment.getUser();
            contentPreview = truncateContent(comment.getContent(), 200);
        }

        // 不能举报自己的内容
        if (reporter.getId().equals(reportedUser.getId())) {
            throw new IllegalArgumentException("不能举报自己的内容");
        }

        // 检查是否已有待处理的相同举报
        reportRepository.findByReporterIdAndTargetTypeAndTargetIdAndStatus(
                reporter.getId(), targetType, request.getTargetId(), ReportStatus.PENDING)
                .ifPresent(r -> {
                    throw new IllegalArgumentException("您已举报过该内容，请等待处理");
                });

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(request.getTargetId());
        report.setTargetContentPreview(contentPreview);
        report.setReportedUser(reportedUser);
        report.setReason(request.getReason());
        report.setStatus(ReportStatus.PENDING);
        reportRepository.save(report);

        logger.info("用户 {} 举报了 {} #{}", reporter.getUsername(), targetType, request.getTargetId());
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return null;
        String clean = content.replaceAll("[#*`>\\[\\]()!]", "").trim();
        if (clean.length() <= maxLength) return clean;
        return clean.substring(0, maxLength - 3) + "...";
    }
}
