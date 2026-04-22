import { useState } from 'react'
import { HandThumbUpIcon, TrashIcon, CheckBadgeIcon } from '@heroicons/react/24/outline'
import { HandThumbUpIcon as HandThumbUpSolid, StarIcon } from '@heroicons/react/24/solid'
import type { ReviewResponse } from '@/types'
import { reviewService } from '@services/review.service'

interface Props {
  review: ReviewResponse
  currentUserId?: number
  onDeleted?: (id: number) => void
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export default function ReviewCard({ review, currentUserId, onDeleted }: Props) {
  const [liked, setLiked] = useState(review.liked)
  const [likeCount, setLikeCount] = useState(review.likeCount)
  const [deleting, setDeleting] = useState(false)

  const handleLike = async () => {
    if (!currentUserId) return
    try {
      const res = await reviewService.toggleLike(review.id)
      const nowLiked = (res as any)?.data ?? !liked
      setLiked(nowLiked)
      setLikeCount((c) => nowLiked ? c + 1 : c - 1)
    } catch {
      // ignore
    }
  }

  const handleDelete = async () => {
    if (!confirm('Xóa đánh giá này?')) return
    setDeleting(true)
    try {
      await reviewService.deleteReview(review.id)
      onDeleted?.(review.id)
    } catch {
      setDeleting(false)
    }
  }

  const initials = (review.userName ?? 'U').charAt(0).toUpperCase()

  return (
    <div className="py-4 border-b last:border-b-0">
      <div className="flex items-start gap-3">
        {/* Avatar */}
        <div className="w-9 h-9 rounded-full bg-primary-100 flex items-center justify-center flex-shrink-0">
          {review.userAvatar ? (
            <img src={review.userAvatar} alt={review.userName} className="w-9 h-9 rounded-full object-cover" />
          ) : (
            <span className="text-sm font-semibold text-primary-600">{initials}</span>
          )}
        </div>

        <div className="flex-1 min-w-0">
          {/* Header */}
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-medium text-gray-900 text-sm">{review.userName}</span>
            {review.verifiedPurchase && (
              <span className="flex items-center gap-0.5 text-xs text-green-600">
                <CheckBadgeIcon className="w-3.5 h-3.5" />
                Đã mua hàng
              </span>
            )}
            <span className="text-xs text-gray-400 ml-auto">{formatDate(review.createdAt)}</span>
          </div>

          {/* Stars */}
          <div className="flex mt-0.5 mb-1">
            {[1, 2, 3, 4, 5].map((s) => (
              <StarIcon key={s} className={`w-4 h-4 ${s <= review.rating ? 'text-yellow-400' : 'text-gray-200'}`} />
            ))}
          </div>

          {/* Content */}
          {review.content && (
            <p className="text-sm text-gray-700 leading-relaxed">{review.content}</p>
          )}

          {/* Images */}
          {review.images?.length > 0 && (
            <div className="flex gap-2 mt-2 flex-wrap">
              {review.images.map((url, i) => (
                <img
                  key={i}
                  src={url}
                  alt=""
                  className="w-16 h-16 rounded object-cover border cursor-pointer hover:opacity-90"
                  onClick={() => window.open(url, '_blank')}
                />
              ))}
            </div>
          )}

          {/* Seller reply */}
          {review.sellerReply && (
            <div className="mt-2 bg-gray-50 border-l-2 border-primary-300 pl-3 py-2 rounded-r text-sm">
              <p className="text-xs font-medium text-primary-600 mb-0.5">Phản hồi của người bán:</p>
              <p className="text-gray-700">{review.sellerReply}</p>
            </div>
          )}

          {/* Actions */}
          <div className="flex items-center gap-3 mt-2">
            <button
              onClick={handleLike}
              disabled={!currentUserId}
              className={`flex items-center gap-1 text-xs transition-colors
                ${liked ? 'text-primary-600' : 'text-gray-400 hover:text-gray-600'}
                ${!currentUserId ? 'cursor-default' : 'cursor-pointer'}`}
            >
              {liked ? <HandThumbUpSolid className="w-4 h-4" /> : <HandThumbUpIcon className="w-4 h-4" />}
              <span>Hữu ích ({likeCount})</span>
            </button>

            {currentUserId === review.userId && (
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="flex items-center gap-1 text-xs text-red-400 hover:text-red-600 transition-colors"
              >
                <TrashIcon className="w-4 h-4" />
                {deleting ? 'Đang xóa...' : 'Xóa'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
