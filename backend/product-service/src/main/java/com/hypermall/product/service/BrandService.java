package com.hypermall.product.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.product.dto.request.BrandRequest;
import com.hypermall.product.dto.response.BrandResponse;
import com.hypermall.product.entity.Brand;
import com.hypermall.product.mapper.ProductMapper;
import com.hypermall.product.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        List<Brand> brands = brandRepository.findAllByOrderByNameAsc();
        log.debug("Retrieved {} brands", brands.size());
        return productMapper.toBrandResponseList(brands);
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        Brand brand = findBrandById(id);
        return productMapper.toBrandResponse(brand);
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with slug: " + slug));
        return productMapper.toBrandResponse(brand);
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        // Validate slug uniqueness
        if (brandRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Brand with slug '" + request.getSlug() + "' already exists");
        }

        // Validate name uniqueness
        if (brandRepository.existsByName(request.getName())) {
            throw new BadRequestException("Brand with name '" + request.getName() + "' already exists");
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .logo(request.getLogo())
                .description(request.getDescription())
                .build();

        brand = brandRepository.save(brand);
        log.info("Brand created: {} (ID: {})", brand.getName(), brand.getId());

        return productMapper.toBrandResponse(brand);
    }

    @Transactional
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = findBrandById(id);

        // Validate slug uniqueness (excluding current brand)
        if (!brand.getSlug().equals(request.getSlug()) && brandRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Brand with slug '" + request.getSlug() + "' already exists");
        }

        // Validate name uniqueness (excluding current brand)
        if (!brand.getName().equals(request.getName()) && brandRepository.existsByName(request.getName())) {
            throw new BadRequestException("Brand with name '" + request.getName() + "' already exists");
        }

        brand.setName(request.getName());
        brand.setSlug(request.getSlug());
        brand.setLogo(request.getLogo());
        brand.setDescription(request.getDescription());

        brand = brandRepository.save(brand);
        log.info("Brand updated: {} (ID: {})", brand.getName(), brand.getId());

        return productMapper.toBrandResponse(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = findBrandById(id);
        brandRepository.delete(brand);
        log.info("Brand deleted: {} (ID: {})", brand.getName(), brand.getId());
    }

    private Brand findBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
    }
}
