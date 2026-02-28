package com.lost.blog.repository;

import com.lost.blog.model.Report;
import com.lost.blog.model.ReportStatus;
import com.lost.blog.model.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r " +
           "LEFT JOIN FETCH r.reporter " +
           "LEFT JOIN FETCH r.reportedUser " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:targetType IS NULL OR r.targetType = :targetType) " +
           "AND (:reporterUsername IS NULL OR r.reporter.username LIKE %:reporterUsername%) " +
           "AND (:reportedUsername IS NULL OR r.reportedUser.username LIKE %:reportedUsername%) " +
           "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.createdAt <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    Page<Report> searchReports(
            @Param("status") ReportStatus status,
            @Param("targetType") ReportTargetType targetType,
            @Param("reporterUsername") String reporterUsername,
            @Param("reportedUsername") String reportedUsername,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByStatus(ReportStatus status);

    Optional<Report> findByReporterIdAndTargetTypeAndTargetIdAndStatus(
            Long reporterId, ReportTargetType targetType, Long targetId, ReportStatus status);
}
