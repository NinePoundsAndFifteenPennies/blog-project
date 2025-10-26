package com.lost.blog.controller;

import com.lost.blog.dto.TagRequest;
import com.lost.blog.dto.TagResponse;
import com.lost.blog.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 创建新标签
     */
    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest tagRequest) {
        TagResponse createdTag = tagService.createTag(tagRequest);
        return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
    }

    /**
     * 获取单个标签详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        TagResponse tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    /**
     * 根据名称获取标签
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<TagResponse> getTagByName(@PathVariable String name) {
        TagResponse tag = tagService.getTagByName(name);
        return ResponseEntity.ok(tag);
    }

    /**
     * 获取所有标签（分页）
     */
    @GetMapping
    public ResponseEntity<Page<TagResponse>> getAllTags(Pageable pageable) {
        Page<TagResponse> tags = tagService.getAllTags(pageable);
        return ResponseEntity.ok(tags);
    }

    /**
     * 获取热门标签（按文章数排序）
     */
    @GetMapping("/popular")
    public ResponseEntity<List<TagResponse>> getPopularTags() {
        List<TagResponse> popularTags = tagService.getPopularTags();
        return ResponseEntity.ok(popularTags);
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest tagRequest) {
        TagResponse updatedTag = tagService.updateTag(id, tagRequest);
        return ResponseEntity.ok(updatedTag);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok("标签删除成功");
    }
}
