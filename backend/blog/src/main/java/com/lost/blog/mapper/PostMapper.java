package com.lost.blog.mapper;

import com.lost.blog.dto.PostResponse;
import com.lost.blog.model.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostResponse toResponse(Post post) {
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
        return postResponse;
    }
}