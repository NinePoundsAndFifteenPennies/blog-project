package com.lost.blog.service;

import com.lost.blog.dto.TagRequest;
import com.lost.blog.dto.TagResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.mapper.TagMapper;
import com.lost.blog.model.Tag;
import com.lost.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Override
    @Transactional
    public TagResponse createTag(TagRequest tagRequest) {
        // 检查标签名称是否已存在
        if (tagRepository.existsByName(tagRequest.getName())) {
            throw new IllegalArgumentException("标签名称已存在：" + tagRequest.getName());
        }

        Tag tag = new Tag();
        tag.setName(tagRequest.getName());
        tag.setDescription(tagRequest.getDescription());

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponse(savedTag);
    }

    @Override
    public TagResponse getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在，ID：" + id));
        return tagMapper.toResponse(tag);
    }

    @Override
    public TagResponse getTagByName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在：" + name));
        return tagMapper.toResponse(tag);
    }

    @Override
    public Page<TagResponse> getAllTags(Pageable pageable) {
        Page<Tag> tags = tagRepository.findAll(pageable);
        return tags.map(tagMapper::toResponse);
    }

    @Override
    public List<TagResponse> getPopularTags() {
        List<Tag> popularTags = tagRepository.findPopularTags();
        return popularTags.stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TagResponse updateTag(Long id, TagRequest tagRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在，ID：" + id));

        // 如果修改了标签名称，需要检查新名称是否已被使用
        if (!tag.getName().equals(tagRequest.getName()) && 
            tagRepository.existsByName(tagRequest.getName())) {
            throw new IllegalArgumentException("标签名称已存在：" + tagRequest.getName());
        }

        tag.setName(tagRequest.getName());
        tag.setDescription(tagRequest.getDescription());

        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toResponse(updatedTag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在，ID：" + id));
        
        // 删除标签（会自动从post_tags中间表中移除关联关系）
        tagRepository.delete(tag);
    }
}
