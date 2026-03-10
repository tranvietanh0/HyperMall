package com.hypermall.review.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.review.dto.*;
import com.hypermall.review.entity.Review;
import com.hypermall.review.entity.ReviewLike;
import com.hypermall.review.entity.ReviewStatus;
import com.hypermall.review.mapper.ReviewMapper;
import com.hypermall.review.repository.ReviewLikeRepository;
import com.hypermall.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(Long userId, String userName, String userAvatar, CreateReviewRequest request) {
        // Check if user already reviewed this product for this order
        if (reviewRepository.existsByOrderIdAndProductIdAndUserId(request.getOrderId(), request.getProductId(), userId)) {
            throw new BadRequestException("You have already reviewed this product for this order");
        }

        Review review = reviewMapper.toReview(request);
        review.setUserId(userId);
        review.setUserName(userName);
        review.setUserAvatar(userAvatar);

        Review saved = reviewRepository.save(review);
        log.info("Review created: {} for product {} by user {}", saved.getId(), request.getProductId(), userId);

        return reviewMapper.toReviewResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id, Long currentUserId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        ReviewResponse response = reviewMapper.toReviewResponse(review);
        if (currentUserId != null) {
            response.setLiked(reviewLikeRepository.existsByReviewIdAndUserId(id, currentUserId));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Integer rating, boolean withImages, Long currentUserId, Pageable pageable) {
        Page<Review> reviews;

        if (withImages) {
            reviews = reviewRepository.findByProductIdWithImages(productId, ReviewStatus.APPROVED, pageable);
        } else if (rating != null) {
            reviews = reviewRepository.findByProductIdAndStatusAndRating(productId, ReviewStatus.APPROVED, rating, pageable);
        } else {
            reviews = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable);
        }

        // Get liked status for current user
        Set<Long> likedReviewIds = new HashSet<>();
        if (currentUserId != null && !reviews.isEmpty()) {
            List<Long> reviewIds = reviews.getContent().stream().map(Review::getId).collect(Collectors.toList());
            likedReviewIds.addAll(reviewLikeRepository.findLikedReviewIds(currentUserId, reviewIds));
        }

        return reviews.map(review -> {
            ReviewResponse response = reviewMapper.toReviewResponse(review);
            response.setLiked(likedReviewIds.contains(review.getId()));
            return response;
        });
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(reviewMapper::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getSellerReviews(Long sellerId, Pageable pageable) {
        return reviewRepository.findBySellerId(sellerId, pageable)
                .map(reviewMapper::toReviewResponse);
    }

    @Transactional
    public ReviewResponse updateReview(Long id, Long userId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        if (!review.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only update your own reviews");
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }
        if (request.getVideos() != null) {
            review.setVideos(request.getVideos());
        }

        Review saved = reviewRepository.save(review);
        log.info("Review updated: {}", id);

        return reviewMapper.toReviewResponse(saved);
    }

    @Transactional
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        if (!review.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        log.info("Review deleted: {} by user {}", id, userId);
    }

    @Transactional
    public ReviewResponse addSellerReply(Long id, Long sellerId, SellerReplyRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        // TODO: Verify sellerId owns the product
        review.setSellerId(sellerId);
        review.setSellerReply(request.getReply());
        review.setSellerReplyAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        log.info("Seller reply added to review: {}", id);

        return reviewMapper.toReviewResponse(saved);
    }

    @Transactional
    public boolean toggleLike(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);

        if (existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get());
            reviewRepository.decrementLikeCount(reviewId);
            log.info("User {} unliked review {}", userId, reviewId);
            return false;
        } else {
            ReviewLike like = ReviewLike.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .build();
            reviewLikeRepository.save(like);
            reviewRepository.incrementLikeCount(reviewId);
            log.info("User {} liked review {}", userId, reviewId);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getProductStatistics(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Integer totalReviews = reviewRepository.countByProductIdAndStatusApproved(productId);
        List<Object[]> distribution = reviewRepository.getRatingDistribution(productId);
        Integer withImages = reviewRepository.countWithImages(productId);
        Integer withVideos = reviewRepository.countWithVideos(productId);
        Integer verifiedPurchases = reviewRepository.countVerifiedPurchases(productId);

        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0);
        }
        for (Object[] row : distribution) {
            ratingDistribution.put((Integer) row[0], ((Long) row[1]).intValue());
        }

        return ReviewStatisticsResponse.builder()
                .productId(productId)
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0)
                .ratingDistribution(ratingDistribution)
                .withImages(withImages != null ? withImages : 0)
                .withVideos(withVideos != null ? withVideos : 0)
                .verifiedPurchases(verifiedPurchases != null ? verifiedPurchases : 0)
                .build();
    }

    @Transactional
    public ReviewResponse updateReviewStatus(Long id, ReviewStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        review.setStatus(status);
        Review saved = reviewRepository.save(review);
        log.info("Review {} status updated to {}", id, status);

        return reviewMapper.toReviewResponse(saved);
    }
}
