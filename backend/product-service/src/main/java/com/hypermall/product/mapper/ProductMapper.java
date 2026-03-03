package com.hypermall.product.mapper;

import com.hypermall.product.dto.response.*;
import com.hypermall.product.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    // Brand mappings
    BrandResponse toBrandResponse(Brand brand);

    List<BrandResponse> toBrandResponseList(List<Brand> brands);

    // Category mappings
    CategoryResponse toCategoryResponse(Category category);

    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    // Product Image mappings
    @Mapping(source = "product.id", target = "productId")
    ProductImageResponse toProductImageResponse(ProductImage productImage);

    List<ProductImageResponse> toProductImageResponseList(List<ProductImage> productImages);

    // Product Variant mappings
    @Mapping(source = "product.id", target = "productId")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    List<ProductVariantResponse> toProductVariantResponseList(List<ProductVariant> productVariants);

    // Product mappings
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.name", target = "brandName")
    ProductResponse toProductResponse(Product product);

    List<ProductResponse> toProductResponseList(List<Product> products);

    // Product Detail mappings
    ProductDetailResponse toProductDetailResponse(Product product);
}
