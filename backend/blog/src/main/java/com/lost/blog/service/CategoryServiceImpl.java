package com.lost.blog.service;

import com.lost.blog.dto.CategoryRequest;
import com.lost.blog.dto.CategoryResponse;
import com.lost.blog.exception.ResourceNotFoundException;
import com.lost.blog.mapper.CategoryMapper;
import com.lost.blog.model.Category;
import com.lost.blog.model.User;
import com.lost.blog.repository.CategoryRepository;
import com.lost.blog.repository.PostRepository;
import com.lost.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, 
                               UserRepository userRepository,
                               PostRepository postRepository,
                               CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest, UserDetails currentUser) {
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("分类名称已存在：" + categoryRequest.getName());
        }

        // 获取当前用户
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户: " + currentUser.getUsername()));

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setColor(categoryRequest.getColor());
        category.setIcon(categoryRequest.getIcon());
        category.setSortOrder(categoryRequest.getSortOrder());
        category.setCreatedBy(user);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID：" + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在：" + name));
        return categoryMapper.toResponse(category);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    public List<CategoryResponse> getPopularCategories() {
        List<Category> popularCategories = categoryRepository.findPopularCategories();
        return popularCategories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID：" + id));

        // 如果修改了分类名称，需要检查新名称是否已被使用
        if (!category.getName().equals(categoryRequest.getName()) && 
            categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("分类名称已存在：" + categoryRequest.getName());
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setColor(categoryRequest.getColor());
        category.setIcon(categoryRequest.getIcon());
        category.setSortOrder(categoryRequest.getSortOrder());

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID：" + id));
        
        // 检查是否有文章使用该分类
        long postCount = postRepository.countByCategory(category);
        if (postCount > 0) {
            throw new IllegalStateException("无法删除该分类，仍有 " + postCount + " 篇文章使用该分类");
        }
        
        categoryRepository.delete(category);
    }
}
