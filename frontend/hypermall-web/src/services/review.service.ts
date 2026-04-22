import { api } from './api.service'
import type { ReviewResponse, ReviewStatistics, CreateReviewRequest, ApiResponse, PageResponse } from '@/types'

export const reviewService = {
  getProductReviews: (
    productId: number,
    params?: { rating?: number; page?: number; size?: number; sort?: string }
  ) =>
    api.get<ApiResponse<PageResponse<ReviewResponse>>>(`/reviews/product/${productId}`, { params }),

  getProductStatistics: (productId: number) =>
    api.get<ApiResponse<ReviewStatistics>>(`/reviews/product/${productId}/statistics`),

  createReview: (data: CreateReviewRequest) =>
    api.post<ApiResponse<ReviewResponse>>('/reviews', data),

  toggleLike: (id: number) =>
    api.post<ApiResponse<boolean>>(`/reviews/${id}/like`),

  deleteReview: (id: number) =>
    api.delete<ApiResponse<void>>(`/reviews/${id}`),
}
