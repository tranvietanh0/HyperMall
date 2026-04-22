export interface ReviewResponse {
  id: number
  productId: number
  variantId?: number
  orderId: number
  userId: number
  userName: string
  userAvatar?: string
  rating: number
  content?: string
  images: string[]
  videos: string[]
  likeCount: number
  liked: boolean
  verifiedPurchase: boolean
  status: 'APPROVED' | 'PENDING' | 'REJECTED'
  sellerReply?: string
  sellerReplyAt?: string
  createdAt: string
  updatedAt?: string
}

export interface CreateReviewRequest {
  productId: number
  orderId: number
  rating: number
  content?: string
  images?: string[]
}
