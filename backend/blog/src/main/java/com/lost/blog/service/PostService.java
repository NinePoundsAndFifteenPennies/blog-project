package com.lost.blog.service;

import com.lost.blog.dto.PostRequest;
import com.lost.blog.dto.PostResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    /**
     * 创建一篇新的文章
     * @param postRequest 文章请求数据
     * @param currentUser 当前登录的用户
     * @return 创建后的文章响应数据
     */
    PostResponse createPost(PostRequest postRequest, UserDetails currentUser);

    /**
     * 根据ID获取一篇文章
     * @param id 文章ID
     * @param currentUser 当前用户（可能为null，用于草稿权限检查）
     * @return 文章响应数据
     */
    PostResponse getPostById(Long id, UserDetails currentUser);

    /**
     * 获取所有已发布的文章（分页）
     * @param pageable 分页信息
     * @return 分页后的文章响应数据
     */
    Page<PostResponse> getAllPosts(Pageable pageable);

    /**
     * 获取当前用户的所有文章（包括草稿）
     * @param currentUser 当前登录的用户
     * @param pageable 分页信息
     * @return 分页后的文章响应数据
     */
    Page<PostResponse> getMyPosts(UserDetails currentUser, Pageable pageable);

    /**
     * 更新一篇文章
     * @param id 文章ID
     * @param postRequest 更新的文章数据
     * @param currentUser 当前登录的用户
     * @return 更新后的文章响应数据
     */
    PostResponse updatePost(Long id, PostRequest postRequest, UserDetails currentUser);

    /**
     * 删除一篇文章
     * @param id 文章ID
     * @param currentUser 当前登录的用户
     */
    void deletePost(Long id, UserDetails currentUser);
}