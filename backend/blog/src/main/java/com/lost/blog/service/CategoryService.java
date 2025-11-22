package com.lost.blog.service;

import com.lost.blog.dto.CategoryRequest;
import com.lost.blog.dto.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 创建分类
     */
    CategoryResponse createCategory(CategoryRequest categoryRequest, UserDetails currentUser);

    /**
     * 获取分类详情
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 根据名称获取分类
     */
    CategoryResponse getCategoryByName(String name);

    /**
     * 获取所有分类（分页）
     */
    Page<CategoryResponse> getAllCategories(Pageable pageable);

    /**
     * 获取热门分类（按文章数排序）
     */
    List<CategoryResponse> getPopularCategories();

    /**
     * 更新分类
     */
    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
