package com.lost.blog.service;

import com.lost.blog.dto.TagRequest;
import com.lost.blog.dto.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 创建标签
     */
    TagResponse createTag(TagRequest tagRequest);

    /**
     * 获取标签详情
     */
    TagResponse getTagById(Long id);

    /**
     * 根据名称获取标签
     */
    TagResponse getTagByName(String name);

    /**
     * 获取所有标签（分页）
     */
    Page<TagResponse> getAllTags(Pageable pageable);

    /**
     * 获取热门标签（按文章数排序）
     */
    List<TagResponse> getPopularTags();

    /**
     * 更新标签
     */
    TagResponse updateTag(Long id, TagRequest tagRequest);

    /**
     * 删除标签
     */
    void deleteTag(Long id);
}
