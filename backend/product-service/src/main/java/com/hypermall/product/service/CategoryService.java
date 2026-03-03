package com.hypermall.product.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.product.dto.request.CategoryRequest;
import com.hypermall.product.dto.response.CategoryResponse;
import com.hypermall.product.entity.Category;
import com.hypermall.product.mapper.ProductMapper;
import com.hypermall.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        log.debug("Retrieved {} categories", categories.size());
        return productMapper.toCategoryResponseList(categories);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();
        List<CategoryResponse> tree = buildCategoryTree(allCategories, null);
        log.debug("Built category tree with {} root categories", tree.size());
        return tree;
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryById(id);
        return productMapper.toCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return productMapper.toCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Validate slug uniqueness
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
        }

        // Validate name uniqueness
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        // Validate parent category exists if provided
        if (request.getParentId() != null) {
            findCategoryById(request.getParentId());
        }

        // Calculate level based on parent
        Integer level = 0;
        if (request.getParentId() != null) {
            Category parent = findCategoryById(request.getParentId());
            level = parent.getLevel() + 1;
        } else if (request.getLevel() != null) {
            level = request.getLevel();
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .image(request.getImage())
                .parentId(request.getParentId())
                .level(level)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        category = categoryRepository.save(category);
        log.info("Category created: {} (ID: {})", category.getName(), category.getId());

        return productMapper.toCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);

        // Validate slug uniqueness (excluding current category)
        if (!category.getSlug().equals(request.getSlug()) && categoryRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
        }

        // Validate name uniqueness (excluding current category)
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        // Validate parent category change
        if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            findCategoryById(request.getParentId());
        }

        // Update level if parent changed
        if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            Category parent = findCategoryById(request.getParentId());
            category.setLevel(parent.getLevel() + 1);
        } else if (request.getParentId() == null && category.getParentId() != null) {
            category.setLevel(0);
        } else if (request.getLevel() != null) {
            category.setLevel(request.getLevel());
        }

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());
        category.setParentId(request.getParentId());

        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        category = categoryRepository.save(category);
        log.info("Category updated: {} (ID: {})", category.getName(), category.getId());

        return productMapper.toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);

        // Check if category has children
        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new BadRequestException("Cannot delete category with " + childCount + " child categories. Delete children first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {} (ID: {})", category.getName(), category.getId());
    }

    /**
     * Helper method to build category tree structure
     */
    private List<CategoryResponse> buildCategoryTree(List<Category> categories, Long parentId) {
        return categories.stream()
                .filter(category -> {
                    if (parentId == null) {
                        return category.getParentId() == null;
                    }
                    return parentId.equals(category.getParentId());
                })
                .map(category -> {
                    CategoryResponse response = productMapper.toCategoryResponse(category);
                    List<CategoryResponse> children = buildCategoryTree(categories, category.getId());
                    response.setChildren(children.isEmpty() ? null : children);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}
