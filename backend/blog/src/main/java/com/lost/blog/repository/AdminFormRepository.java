package com.lost.blog.repository;

import com.lost.blog.model.AdminForm;
import com.lost.blog.model.AdminFormType;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 管理表单数据访问接口
 */
@Repository
public interface AdminFormRepository extends JpaRepository<AdminForm, Long> {

    /**
     * 根据文章ID查找表单
     */
    List<AdminForm> findByPostId(Long postId);

    /**
     * 根据目标用户查找表单
     */
    Page<AdminForm> findByTargetUserOrderByCreatedAtDesc(User targetUser, Pageable pageable);

    /**
     * 根据管理员查找表单
     */
    Page<AdminForm> findByAdminOrderByCreatedAtDesc(User admin, Pageable pageable);

    /**
     * 根据表单类型查找表单
     */
    Page<AdminForm> findByFormTypeOrderByCreatedAtDesc(AdminFormType formType, Pageable pageable);

    /**
     * 查找未发送的表单
     */
    List<AdminForm> findBySentFalse();

    /**
     * 根据文章ID和表单类型查找最新表单
     */
    AdminForm findFirstByPostIdAndFormTypeOrderByCreatedAtDesc(Long postId, AdminFormType formType);
}
