package com.lost.blog.repository;

import com.lost.blog.model.AdminLog;
import com.lost.blog.model.AdminLogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 管理操作日志数据访问接口
 */
@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    /**
     * 多条件搜索日志
     */
    @Query("SELECT l FROM AdminLog l WHERE " +
           "(:operationType IS NULL OR l.operationType = :operationType) AND " +
           "(:adminUsername IS NULL OR l.admin.username LIKE %:adminUsername%) AND " +
           "(:title IS NULL OR l.title LIKE %:title%) AND " +
           "(:startTime IS NULL OR l.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR l.createdAt <= :endTime) " +
           "ORDER BY l.createdAt DESC")
    Page<AdminLog> searchLogs(
            @Param("operationType") AdminLogType operationType,
            @Param("adminUsername") String adminUsername,
            @Param("title") String title,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
