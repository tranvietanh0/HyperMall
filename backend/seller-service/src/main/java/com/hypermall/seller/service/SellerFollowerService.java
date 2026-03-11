package com.hypermall.seller.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.seller.dto.response.FollowResponse;
import com.hypermall.seller.entity.Seller;
import com.hypermall.seller.entity.SellerFollower;
import com.hypermall.seller.repository.SellerFollowerRepository;
import com.hypermall.seller.repository.SellerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerFollowerService {

    private final SellerFollowerRepository followerRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public FollowResponse followSeller(Long userId, Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        if (seller.getUserId().equals(userId)) {
            throw new BadRequestException("Cannot follow your own shop");
        }

        if (followerRepository.existsBySellerIdAndUserId(sellerId, userId)) {
            throw new BadRequestException("Already following this seller");
        }

        SellerFollower follower = SellerFollower.builder()
                .sellerId(sellerId)
                .userId(userId)
                .build();

        followerRepository.save(follower);

        seller.setTotalFollowers(seller.getTotalFollowers() + 1);
        sellerRepository.save(seller);

        log.info("User {} followed seller {}", userId, sellerId);

        return FollowResponse.builder()
                .sellerId(sellerId)
                .following(true)
                .totalFollowers(seller.getTotalFollowers())
                .build();
    }

    @Transactional
    public FollowResponse unfollowSeller(Long userId, Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        if (!followerRepository.existsBySellerIdAndUserId(sellerId, userId)) {
            throw new BadRequestException("Not following this seller");
        }

        followerRepository.deleteBySellerIdAndUserId(sellerId, userId);

        seller.setTotalFollowers(Math.max(0, seller.getTotalFollowers() - 1));
        sellerRepository.save(seller);

        log.info("User {} unfollowed seller {}", userId, sellerId);

        return FollowResponse.builder()
                .sellerId(sellerId)
                .following(false)
                .totalFollowers(seller.getTotalFollowers())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long sellerId) {
        return followerRepository.existsBySellerIdAndUserId(sellerId, userId);
    }

    @Transactional(readOnly = true)
    public Page<Long> getFollowerUserIds(Long sellerId, Pageable pageable) {
        return followerRepository.findBySellerIdOrderByFollowedAtDesc(sellerId, pageable)
                .map(SellerFollower::getUserId);
    }

    @Transactional(readOnly = true)
    public Page<Long> getFollowingSellerIds(Long userId, Pageable pageable) {
        return followerRepository.findByUserIdOrderByFollowedAtDesc(userId, pageable)
                .map(SellerFollower::getSellerId);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long sellerId) {
        return followerRepository.countBySellerId(sellerId);
    }
}
