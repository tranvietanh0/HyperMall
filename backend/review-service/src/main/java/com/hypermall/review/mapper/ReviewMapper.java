package com.hypermall.review.mapper;

import com.hypermall.review.dto.CreateReviewRequest;
import com.hypermall.review.dto.ReviewResponse;
import com.hypermall.review.entity.Review;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "liked", ignore = true)
    ReviewResponse toReviewResponse(Review review);

    List<ReviewResponse> toReviewResponseList(List<Review> reviews);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "likeCount", constant = "0")
    @Mapping(target = "verifiedPurchase", constant = "true")
    @Mapping(target = "status", constant = "APPROVED")
    @Mapping(target = "sellerId", ignore = true)
    @Mapping(target = "sellerReply", ignore = true)
    @Mapping(target = "sellerReplyAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toReview(CreateReviewRequest request);
}
