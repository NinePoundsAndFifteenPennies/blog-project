package com.lost.blog.mapper;

import com.lost.blog.dto.TagResponse;
import com.lost.blog.model.Tag;
import org.springframework.stereotype.Component;

/**
 * Tag实体与DTO转换工具
 */
@Component
public class TagMapper {

    /**
     * 将Tag实体转换为TagResponse
     */
    public TagResponse toResponse(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setDescription(tag.getDescription());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());
        
        // 计算使用该标签的文章数量
        if (tag.getPosts() != null) {
            response.setPostCount((long) tag.getPosts().size());
        } else {
            response.setPostCount(0L);
        }

        return response;
    }
}
