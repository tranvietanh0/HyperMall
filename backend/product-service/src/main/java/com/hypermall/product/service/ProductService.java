package com.hypermall.product.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.product.dto.request.ProductImageRequest;
import com.hypermall.product.dto.request.ProductRequest;
import com.hypermall.product.dto.request.ProductVariantRequest;
import com.hypermall.product.dto.response.ProductDetailResponse;
import com.hypermall.product.dto.response.ProductResponse;
import com.hypermall.product.entity.*;
import com.hypermall.product.mapper.ProductMapper;
import com.hypermall.product.repository.BrandRepository;
import com.hypermall.product.repository.CategoryRepository;
import com.hypermall.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.searchProducts(
                keyword,
                categoryId,
                brandId,
                minPrice,
                maxPrice,
                minRating,
                ProductStatus.ACTIVE,
                pageable
        );

        log.debug("Retrieved {} products matching search criteria", products.getTotalElements());
        return products.map(productMapper::toProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = findActiveProductById(id);

        return productMapper.toProductDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = findActiveProductBySlug(slug);

        return productMapper.toProductDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        findCategoryById(categoryId);

        Page<Product> products = productRepository.findByCategoryIdAndStatus(
                categoryId,
                ProductStatus.ACTIVE,
                pageable
        );

        log.debug("Retrieved {} products for category {}", products.getTotalElements(), categoryId);
        return products.map(productMapper::toProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsBySeller(Long sellerId, Pageable pageable) {
        Page<Product> products = productRepository.findBySellerIdAndStatus(
                sellerId,
                ProductStatus.ACTIVE,
                pageable
        );

        log.debug("Retrieved {} active products for seller {}", products.getTotalElements(), sellerId);
        return products.map(productMapper::toProductResponse);
    }

    @Transactional
    public ProductDetailResponse createProduct(Long sellerId, ProductRequest request) {
        validateCreateSlug(request.getSlug());

        Category category = findCategoryById(request.getCategoryId());
        Brand brand = findBrandByIdIfPresent(request.getBrandId());
        validateSalePrice(request.getBasePrice(), request.getSalePrice());

        Product product = Product.builder()
                .sellerId(sellerId)
                .category(category)
                .brand(brand)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .thumbnail(request.getThumbnail())
                .basePrice(request.getBasePrice())
                .salePrice(request.getSalePrice())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT)
                .hasVariants(request.getHasVariants() != null ? request.getHasVariants() : false)
                .images(new ArrayList<>())
                .variants(new ArrayList<>())
                .build();

        addImagesIfPresent(product, request.getImages());

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            product.setHasVariants(true);
            addVariants(product, request.getVariants());
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {} (ID: {}) by seller {}", savedProduct.getName(), savedProduct.getId(), sellerId);

        return productMapper.toProductDetailResponse(savedProduct);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long sellerId, Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        validateSellerOwnership(product, sellerId, "update");
        validateUpdateSlug(product, request.getSlug(), productId);

        Category category = findCategoryById(request.getCategoryId());
        Brand brand = findBrandByIdIfPresent(request.getBrandId());
        validateSalePrice(request.getBasePrice(), request.getSalePrice());

        product.setCategory(category);
        product.setBrand(brand);
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setThumbnail(request.getThumbnail());
        product.setBasePrice(request.getBasePrice());
        product.setSalePrice(request.getSalePrice());

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        if (request.getImages() != null) {
            replaceImages(product, request.getImages());
        }

        if (request.getVariants() != null) {
            clearVariants(product);
            product.setHasVariants(!request.getVariants().isEmpty());
            addVariants(product, request.getVariants());
        } else if (request.getHasVariants() != null) {
            product.setHasVariants(request.getHasVariants());
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product updated: {} (ID: {}) by seller {}", savedProduct.getName(), savedProduct.getId(), sellerId);

        return productMapper.toProductDetailResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        validateSellerOwnership(product, sellerId, "delete");

        // Soft delete - set status to DELETED and record deletion time
        product.setStatus(ProductStatus.DELETED);
        product.setDeletedAt(java.time.LocalDateTime.now());
        productRepository.save(product);
        log.info("Product soft deleted: {} (ID: {}) by seller {}", product.getName(), product.getId(), sellerId);
    }

    private Product findActiveProductById(Long id) {
        Product product = productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        Hibernate.initialize(product.getImages());
        Hibernate.initialize(product.getVariants());
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        return product;
    }

    private Product findActiveProductBySlug(String slug) {
        Product product = productRepository.findWithDetailsBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        Hibernate.initialize(product.getImages());
        Hibernate.initialize(product.getVariants());
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found with slug: " + slug);
        }
        return product;
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private Brand findBrandByIdIfPresent(Long brandId) {
        if (brandId == null) {
            return null;
        }
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));
    }

    private void validateCreateSlug(String slug) {
        if (productRepository.existsBySlug(slug)) {
            throw new BadRequestException("Product with slug '" + slug + "' already exists");
        }
    }

    private void validateUpdateSlug(Product product, String slug, Long productId) {
        if (!product.getSlug().equals(slug) && productRepository.existsBySlugAndIdNot(slug, productId)) {
            throw new BadRequestException("Product with slug '" + slug + "' already exists");
        }
    }

    private void validateSalePrice(BigDecimal basePrice, BigDecimal salePrice) {
        if (salePrice != null && salePrice.compareTo(basePrice) >= 0) {
            throw new BadRequestException("Sale price must be less than base price");
        }
    }

    private void validateSellerOwnership(Product product, Long sellerId, String action) {
        if (!product.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You don't have permission to " + action + " this product");
        }
    }

    private void addImagesIfPresent(Product product, List<ProductImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }
        imageRequests.forEach(imageRequest -> product.addImage(toProductImage(imageRequest)));
    }

    private void replaceImages(Product product, List<ProductImageRequest> imageRequests) {
        product.getImages().clear();
        addImagesIfPresent(product, imageRequests);
    }

    private void clearVariants(Product product) {
        product.getVariants().clear();
    }

    private void addVariants(Product product, List<ProductVariantRequest> variantRequests) {
        variantRequests.forEach(variantRequest -> product.addVariant(toProductVariant(variantRequest)));
    }

    private ProductImage toProductImage(ProductImageRequest imageRequest) {
        return ProductImage.builder()
                .url(imageRequest.getUrl())
                .sortOrder(imageRequest.getSortOrder() != null ? imageRequest.getSortOrder() : 0)
                .isMain(imageRequest.getIsMain() != null ? imageRequest.getIsMain() : false)
                .build();
    }

    private ProductVariant toProductVariant(ProductVariantRequest variantRequest) {
        return ProductVariant.builder()
                .sku(variantRequest.getSku())
                .name(variantRequest.getName())
                .price(variantRequest.getPrice())
                .salePrice(variantRequest.getSalePrice())
                .image(variantRequest.getImage())
                .attributes(variantRequest.getAttributes())
                .stock(variantRequest.getStock() != null ? variantRequest.getStock() : 0)
                .isActive(variantRequest.getIsActive() != null ? variantRequest.getIsActive() : true)
                .build();
    }
}
