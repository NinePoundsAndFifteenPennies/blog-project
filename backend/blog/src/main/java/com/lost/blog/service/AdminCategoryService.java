package com.lost.blog.service;

import com.lost.blog.dto.*;
import com.lost.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员分类服务接口
 * 提供分类列表查询、创建、更新、删除等功能
 */
public interface AdminCategoryService {

    /**
     * 分页查询分类列表（支持多条件搜索，按文章数排序）
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 分类分页结果
     */
    Page<AdminCategoryResponse> searchCategories(AdminCategoryQueryRequest query, Pageable pageable);

    /**
     * 获取分类详细信息
     * @param categoryId 分类ID
     * @return 分类详细信息
     */
    AdminCategoryResponse getCategoryDetail(Long categoryId);

    /**
     * 管理员创建分类
     * @param categoryRequest 分类创建请求
     * @param admin 操作管理员
     * @return 创建的分类
     */
    AdminCategoryResponse createCategory(CategoryRequest categoryRequest, User admin);

    /**
     * 管理员更新分类
     * @param categoryId 分类ID
     * @param categoryRequest 分类更新请求
     * @return 更新后的分类
     */
    AdminCategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest);

    /**
     * 将文章归入指定分类
     * @param categoryId 分类ID
     * @param postId 文章ID
     * @param admin 操作管理员
     * @return 更新后的分类
     */
    AdminCategoryResponse assignPostToCategory(Long categoryId, Long postId, User admin);

    /**
     * 将文章从指定分类移除
     * @param categoryId 分类ID
     * @param postId 文章ID
     * @param admin 操作管理员
     * @return 更新后的分类
     */
    AdminCategoryResponse removePostFromCategory(Long categoryId, Long postId, User admin);

    /**
     * 按标题搜索文章（用于分类管理时选择文章）
     * @param title 文章标题关键词
     * @return 匹配的文章列表（ID和标题）
     */
    java.util.List<AdminCategoryResponse.PostInfo> searchPostsByTitle(String title);

    /**
     * 批量删除分类（删除分类并清除文章关联的分类ID）
     * @param categoryIds 分类ID列表
     * @param reason 删除理由
     * @param extraFields 扩展字段（JSON格式）
     * @param admin 操作管理员
     * @return 批量操作结果
     */
    AdminBatchActionResponse deleteCategories(java.util.List<Long> categoryIds, String reason,
                                               String extraFields, User admin);
}
