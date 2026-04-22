import { useEffect, useState, useCallback } from 'react'
import { useAppSelector } from '@store/hooks'
import { reviewService } from '@services/review.service'
import type { ReviewResponse, ReviewStatistics } from '@/types'
import ReviewStatisticsPanel from './ReviewStatistics'
import ReviewCard from './ReviewCard'
import WriteReviewForm from './WriteReviewForm'

interface Props {
  productId: number
}

const PAGE_SIZE = 5

export default function ReviewSection({ productId }: Props) {
  const { user } = useAppSelector((s) => s.auth)

  const [stats, setStats] = useState<ReviewStatistics | null>(null)
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [totalReviews, setTotalReviews] = useState(0)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [ratingFilter, setRatingFilter] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)

  const loadStats = useCallback(async () => {
    try {
      const res = await reviewService.getProductStatistics(productId)
      setStats((res as any)?.data ?? null)
    } catch {
      // ignore
    }
  }, [productId])

  const loadReviews = useCallback(async (pg: number, rating: number | null) => {
    setLoading(true)
    try {
      const params: Record<string, any> = { page: pg, size: PAGE_SIZE, sort: 'createdAt,desc' }
      if (rating) params.rating = rating
      const res = await reviewService.getProductReviews(productId, params)
      const data = (res as any)?.data
      if (data) {
        if (pg === 0) {
          setReviews(data.content)
        } else {
          setReviews((prev) => [...prev, ...data.content])
        }
        setTotalReviews(data.totalElements)
        setTotalPages(data.totalPages)
      }
    } catch {
      // ignore
    } finally {
      setLoading(false)
    }
  }, [productId])

  useEffect(() => {
    loadStats()
  }, [loadStats])

  useEffect(() => {
    setPage(0)
    loadReviews(0, ratingFilter)
  }, [ratingFilter, loadReviews])

  const handleFilterChange = (rating: number | null) => {
    setRatingFilter(rating)
  }

  const handleLoadMore = () => {
    const next = page + 1
    setPage(next)
    loadReviews(next, ratingFilter)
  }

  const handleNewReview = (review: ReviewResponse) => {
    setReviews((prev) => [review, ...prev])
    setTotalReviews((c) => c + 1)
    loadStats()
  }

  const handleDeleted = (id: number) => {
    setReviews((prev) => prev.filter((r) => r.id !== id))
    setTotalReviews((c) => Math.max(0, c - 1))
    loadStats()
  }

  return (
    <div className="border-t p-6">
      <h2 className="font-semibold text-lg mb-4">
        Đánh giá sản phẩm
        {totalReviews > 0 && <span className="ml-2 text-sm font-normal text-gray-500">({totalReviews})</span>}
      </h2>

      {/* Statistics */}
      {stats && stats.totalReviews > 0 && (
        <div className="mb-5">
          <ReviewStatisticsPanel
            stats={stats}
            activeRating={ratingFilter}
            onFilterChange={handleFilterChange}
          />
          {ratingFilter && (
            <button
              onClick={() => setRatingFilter(null)}
              className="mt-2 text-xs text-primary-600 hover:underline"
            >
              ✕ Xóa bộ lọc {ratingFilter} sao
            </button>
          )}
        </div>
      )}

      {/* Write review */}
      {user ? (
        <div className="mb-5">
          <WriteReviewForm productId={productId} onSuccess={handleNewReview} />
        </div>
      ) : (
        <p className="mb-5 text-sm text-gray-500 italic">
          <a href="/login" className="text-primary-600 hover:underline">Đăng nhập</a> để viết đánh giá
        </p>
      )}

      {/* Reviews list */}
      {loading && reviews.length === 0 ? (
        <div className="text-center py-8 text-gray-400 text-sm">Đang tải đánh giá...</div>
      ) : reviews.length === 0 ? (
        <div className="text-center py-8 text-gray-400 text-sm">
          {ratingFilter
            ? `Chưa có đánh giá ${ratingFilter} sao nào`
            : 'Chưa có đánh giá nào. Hãy là người đầu tiên!'}
        </div>
      ) : (
        <>
          <div>
            {reviews.map((r) => (
              <ReviewCard
                key={r.id}
                review={r}
                currentUserId={user?.id}
                onDeleted={handleDeleted}
              />
            ))}
          </div>

          {page + 1 < totalPages && (
            <button
              onClick={handleLoadMore}
              disabled={loading}
              className="mt-4 w-full py-2 border border-gray-200 rounded-lg text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-50 transition-colors"
            >
              {loading ? 'Đang tải...' : `Xem thêm (${totalReviews - reviews.length} đánh giá)`}
            </button>
          )}
        </>
      )}
    </div>
  )
}
