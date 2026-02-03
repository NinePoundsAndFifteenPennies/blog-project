package com.lost.blog.mapper;

import com.lost.blog.dto.AdminFormResponse;
import com.lost.blog.dto.AdminPostResponse;
import com.lost.blog.model.AdminForm;
import com.lost.blog.model.Post;
import com.lost.blog.repository.AdminFormRepository;
import com.lost.blog.service.CommentService;
import com.lost.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 管理员文章映射器
 * 用于将Post实体转换为AdminPostResponse
 */
@Component
public class AdminPostMapper {

    private final LikeService likeService;
    private final CommentService commentService;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    private final AdminFormRepository adminFormRepository;

    @Autowired
    public AdminPostMapper(LikeService likeService, 
                          CommentService commentService, 
                          TagMapper tagMapper, 
                          CategoryMapper categoryMapper,
                          AdminFormRepository adminFormRepository) {
        this.likeService = likeService;
        this.commentService = commentService;
        this.tagMapper = tagMapper;
        this.categoryMapper = categoryMapper;
        this.adminFormRepository = adminFormRepository;
    }

    /**
     * 将Post实体转换为AdminPostResponse
     */
    public AdminPostResponse toResponse(Post post) {
        return toResponse(post, false);
    }

    /**
     * 将Post实体转换为AdminPostResponse
     * @param post 文章实体
     * @param includeRejectionForm 是否包含拒绝表单详情
     */
    public AdminPostResponse toResponse(Post post, boolean includeRejectionForm) {
        if (post == null) {
            return null;
        }
        
        AdminPostResponse response = new AdminPostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setContentType(post.getContentType());
        response.setStatus(post.getStatus());
        response.setDraft(post.getDraft());
        
        // 作者信息
        if (post.getUser() != null) {
            response.setAuthorId(post.getUser().getId());
            response.setAuthorUsername(post.getUser().getUsername());
            response.setAuthorNickname(post.getUser().getNickname());
            response.setAuthorAvatarUrl(post.getUser().getAvatarUrl());
            response.setAuthorEnabled(post.getUser().getEnabled());
        }
        
        // 时间信息
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setPublishedAt(post.getPublishedAt());
        
        // 统计信息
        response.setViewCount(post.getViewCount());
        response.setLikeCount(likeService.getLikeCount(post.getId()));
        response.setCommentCount(commentService.getCommentCount(post.getId()));
        
        // 分类和标签
        if (post.getCategory() != null) {
            response.setCategory(categoryMapper.toResponse(post.getCategory()));
        }
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            response.setTags(
                post.getTags().stream()
                    .map(tagMapper::toResponse)
                    .collect(Collectors.toList())
            );
        }
        
        // 封面图片
        response.setCoverImageUrl(post.getCoverImageUrl());
        
        // 审核相关
        response.setPreviousTitle(post.getPreviousTitle());
        response.setPreviousContent(post.getPreviousContent());
        response.setRejectionFormId(post.getRejectionFormId());
        
        // 加载拒绝表单详情
        if (includeRejectionForm && post.getRejectionFormId() != null) {
            AdminForm form = adminFormRepository.findById(post.getRejectionFormId()).orElse(null);
            if (form != null) {
                response.setRejectionForm(AdminFormResponse.fromEntity(form));
            }
        }
        
        return response;
    }
}
