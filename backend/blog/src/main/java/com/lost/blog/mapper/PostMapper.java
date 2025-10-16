package com.lost.blog.mapper;

import com.lost.blog.dto.PostResponse;
import com.lost.blog.model.Post;
import com.lost.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private final LikeService likeService;

    @Autowired
    public PostMapper(LikeService likeService) {
        this.likeService = likeService;
    }

    public PostResponse toResponse(Post post) {
        return toResponse(post, null);
    }

    public PostResponse toResponse(Post post, UserDetails currentUser) {
        if (post == null) {
            return null;
        }
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setTitle(post.getTitle());
        postResponse.setContent(post.getContent());
        postResponse.setContentType(post.getContentType());
        postResponse.setCreatedAt(post.getCreatedAt());
        postResponse.setUpdatedAt(post.getUpdatedAt());
        postResponse.setPublishedAt(post.getPublishedAt());  // 新增
        postResponse.setDraft(post.getDraft());  // 新增
        postResponse.setAuthorUsername(post.getUser().getUsername());
        postResponse.setAuthorAvatarUrl(post.getUser().getAvatarUrl());  // 新增：作者头像URL
        
        // 添加点赞信息
        postResponse.setLikeCount(likeService.getLikeCount(post.getId()));
        postResponse.setIsLiked(likeService.isPostLikedByUser(post.getId(), currentUser));
        
        return postResponse;
    }
}