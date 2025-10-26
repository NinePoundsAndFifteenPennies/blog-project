package com.lost.blog.mapper;

import com.lost.blog.dto.PostResponse;
import com.lost.blog.dto.TagResponse;
import com.lost.blog.model.Post;
import com.lost.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostMapper {

    private final LikeService likeService;
    private final com.lost.blog.service.CommentService commentService;
    private final TagMapper tagMapper;

    @Autowired
    public PostMapper(LikeService likeService, com.lost.blog.service.CommentService commentService, TagMapper tagMapper) {
        this.likeService = likeService;
        this.commentService = commentService;
        this.tagMapper = tagMapper;
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
        
        // 添加评论数
        postResponse.setCommentCount(commentService.getCommentCount(post.getId()));
        
        // 添加标签信息
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            postResponse.setTags(
                post.getTags().stream()
                    .map(tag -> {
                        TagResponse tagResponse = new TagResponse();
                        tagResponse.setId(tag.getId());
                        tagResponse.setName(tag.getName());
                        tagResponse.setDescription(tag.getDescription());
                        return tagResponse;
                    })
                    .collect(Collectors.toList())
            );
        }
        
        return postResponse;
    }
}