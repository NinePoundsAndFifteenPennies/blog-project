package com.lost.blog.mapper;

import com.lost.blog.dto.CommentResponse;
import com.lost.blog.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    /**
     * Convert Comment entity to CommentResponse DTO.
     * Note: This method does NOT populate like information (likeCount and isLiked).
     * The service layer is responsible for setting these fields to avoid N+1 query problems.
     */
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setPostId(comment.getPost().getId());
        response.setPostTitle(comment.getPost().getTitle());
        response.setAuthorUsername(comment.getUser().getUsername());
        response.setAuthorAvatarUrl(comment.getUser().getAvatarUrl());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        
        // Initialize like fields to default values
        // Service layer will populate these fields
        response.setLikeCount(0);
        response.setLiked(false);
        
        return response;
    }
}
