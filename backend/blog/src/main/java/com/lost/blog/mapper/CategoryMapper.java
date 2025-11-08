package com.lost.blog.mapper;

import com.lost.blog.dto.CategoryResponse;
import com.lost.blog.model.Category;
import com.lost.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Category实体与DTO转换工具
 */
@Component
public class CategoryMapper {

    private final PostRepository postRepository;

    @Autowired
    public CategoryMapper(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 将Category实体转换为CategoryResponse
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setColor(category.getColor());
        response.setIcon(category.getIcon());
        response.setSortOrder(category.getSortOrder());
        
        // 设置创建者信息
        if (category.getCreatedBy() != null) {
            response.setCreatedById(category.getCreatedBy().getId());
            response.setCreatedByUsername(category.getCreatedBy().getUsername());
        }
        
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        // 计算使用该分类的文章数量
        long postCount = postRepository.countByCategory(category);
        response.setPostCount(postCount);

        return response;
    }
}
