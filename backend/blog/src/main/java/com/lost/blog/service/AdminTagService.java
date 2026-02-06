package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员标签服务接口
 * 提供标签列表查询、创建、更新、软删除、硬删除等功能
 */
public interface AdminTagService {

    /**
     * 分页查询标签列表（支持多条件搜索，按热度排序）
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 标签分页结果
     */
    Page<AdminTagResponse> searchTags(AdminTagQueryRequest query, Pageable pageable);

    /**
     * 获取标签详细信息
     * @param tagId 标签ID
     * @return 标签详细信息
     */
    AdminTagResponse getTagDetail(Long tagId);

    /**
     * 管理员创建标签
     * @param tagRequest 标签创建请求
     * @param admin 操作管理员
     * @return 创建的标签
     */
    AdminTagResponse createTag(TagRequest tagRequest, User admin);

    /**
     * 管理员更新标签
     * @param tagId 标签ID
     * @param tagRequest 标签更新请求
     * @return 更新后的标签
     */
    AdminTagResponse updateTag(Long tagId, TagRequest tagRequest);

    /**
     * 批量软删除标签（只移除标签与文章的关联）
     * @param tagIds 标签ID列表
     * @param postIds 指定要解除关联的文章ID列表（为null时解除所有关联）
     * @param formTitle 表单标题
     * @param reason 删除理由
     * @param extraFields 扩展字段（JSON格式）
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse softDeleteTags(java.util.List<Long> tagIds, java.util.List<Long> postIds,
                                            String formTitle, String reason, String extraFields, User admin);

    /**
     * 批量硬删除标签（删除标签本身及所有关联）
     * @param tagIds 标签ID列表
     * @param formTitle 表单标题
     * @param reason 删除理由
     * @param extraFields 扩展字段（JSON格式）
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse hardDeleteTags(java.util.List<Long> tagIds, String formTitle,
                                            String reason, String extraFields, User admin);
}
