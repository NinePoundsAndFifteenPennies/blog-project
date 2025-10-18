package com.lost.blog.mapper;

import com.lost.blog.dto.CommentResponse;
import com.lost.blog.model.Comment;
import com.lost.blog.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    private final LikeRepository likeRepository;

    @Autowired
    public CommentMapper(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public CommentResponse toResponse(Comment comment) {
        return toResponse(comment, null);
    }

    public CommentResponse toResponse(Comment comment, UserDetails currentUser) {
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
        
        // Set like information
        response.setLikeCount(likeRepository.countByComment(comment));
        response.setLiked(false);  // Default to false for anonymous users
        
        return response;
    }
}
