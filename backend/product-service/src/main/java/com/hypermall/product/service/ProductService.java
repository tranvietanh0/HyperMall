package com.hypermall.product.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.product.dto.request.ProductRequest;
import com.hypermall.product.dto.response.ProductDetailResponse;
import com.hypermall.product.dto.response.ProductResponse;
import com.hypermall.product.entity.*;
import com.hypermall.product.mapper.ProductMapper;
import com.hypermall.product.repository.BrandRepository;
import com.hypermall.product.repository.CategoryRepository;
import com.hypermall.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

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
        Product product = productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        return productMapper.toProductDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findWithDetailsBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found with slug: " + slug);
        }

        return productMapper.toProductDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        // Validate category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

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
        // Validate slug uniqueness
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Product with slug '" + request.getSlug() + "' already exists");
        }

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        // Validate brand exists if provided
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
        }

        // Validate sale price is less than base price
        if (request.getSalePrice() != null && request.getSalePrice().compareTo(request.getBasePrice()) >= 0) {
            throw new BadRequestException("Sale price must be less than base price");
        }

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

        // Add product images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            request.getImages().forEach(imageRequest -> {
                ProductImage image = ProductImage.builder()
                        .url(imageRequest.getUrl())
                        .sortOrder(imageRequest.getSortOrder() != null ? imageRequest.getSortOrder() : 0)
                        .isMain(imageRequest.getIsMain() != null ? imageRequest.getIsMain() : false)
                        .build();
                product.addImage(image);
            });
        }

        // Add product variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            product.setHasVariants(true);
            request.getVariants().forEach(variantRequest -> {
                ProductVariant variant = ProductVariant.builder()
                        .sku(variantRequest.getSku())
                        .name(variantRequest.getName())
                        .price(variantRequest.getPrice())
                        .salePrice(variantRequest.getSalePrice())
                        .image(variantRequest.getImage())
                        .attributes(variantRequest.getAttributes())
                        .stock(variantRequest.getStock() != null ? variantRequest.getStock() : 0)
                        .isActive(variantRequest.getIsActive() != null ? variantRequest.getIsActive() : true)
                        .build();
                product.addVariant(variant);
            });
        }

        product = productRepository.save(product);
        log.info("Product created: {} (ID: {}) by seller {}", product.getName(), product.getId(), sellerId);

        return productMapper.toProductDetailResponse(product);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long sellerId, Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Verify seller owns this product
        if (!product.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You don't have permission to update this product");
        }

        // Validate slug uniqueness (excluding current product)
        if (!product.getSlug().equals(request.getSlug()) &&
                productRepository.existsBySlugAndIdNot(request.getSlug(), productId)) {
            throw new BadRequestException("Product with slug '" + request.getSlug() + "' already exists");
        }

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        // Validate brand exists if provided
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
        }

        // Validate sale price is less than base price
        if (request.getSalePrice() != null && request.getSalePrice().compareTo(request.getBasePrice()) >= 0) {
            throw new BadRequestException("Sale price must be less than base price");
        }

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

        // Update images
        if (request.getImages() != null) {
            product.getImages().clear();
            request.getImages().forEach(imageRequest -> {
                ProductImage image = ProductImage.builder()
                        .url(imageRequest.getUrl())
                        .sortOrder(imageRequest.getSortOrder() != null ? imageRequest.getSortOrder() : 0)
                        .isMain(imageRequest.getIsMain() != null ? imageRequest.getIsMain() : false)
                        .build();
                product.addImage(image);
            });
        }

        // Update variants
        if (request.getVariants() != null) {
            product.getVariants().clear();
            product.setHasVariants(!request.getVariants().isEmpty());
            request.getVariants().forEach(variantRequest -> {
                ProductVariant variant = ProductVariant.builder()
                        .sku(variantRequest.getSku())
                        .name(variantRequest.getName())
                        .price(variantRequest.getPrice())
                        .salePrice(variantRequest.getSalePrice())
                        .image(variantRequest.getImage())
                        .attributes(variantRequest.getAttributes())
                        .stock(variantRequest.getStock() != null ? variantRequest.getStock() : 0)
                        .isActive(variantRequest.getIsActive() != null ? variantRequest.getIsActive() : true)
                        .build();
                product.addVariant(variant);
            });
        } else if (request.getHasVariants() != null) {
            product.setHasVariants(request.getHasVariants());
        }

        product = productRepository.save(product);
        log.info("Product updated: {} (ID: {}) by seller {}", product.getName(), product.getId(), sellerId);

        return productMapper.toProductDetailResponse(product);
    }

    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Verify seller owns this product
        if (!product.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You don't have permission to delete this product");
        }

        productRepository.delete(product);
        log.info("Product deleted: {} (ID: {}) by seller {}", product.getName(), product.getId(), sellerId);
    }
}
