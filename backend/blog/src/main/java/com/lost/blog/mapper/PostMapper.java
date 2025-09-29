package com.lost.blog.mapper;

import com.lost.blog.dto.PostResponse;
import com.lost.blog.model.Post;
import org.springframework.stereotype.Component;

@Component // 声明为Spring组件
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
        postResponse.setAuthorUsername(post.getUser().getUsername());
        return postResponse;
    }
}
