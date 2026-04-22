import { useState } from 'react'
import { StarIcon } from '@heroicons/react/24/solid'
import { StarIcon as StarOutline } from '@heroicons/react/24/outline'
import { reviewService } from '@services/review.service'
import type { ReviewResponse } from '@/types'

interface Props {
  productId: number
  onSuccess: (review: ReviewResponse) => void
}

const LABELS = ['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Xuất sắc']

export default function WriteReviewForm({ productId, onSuccess }: Props) {
  const [rating, setRating] = useState(0)
  const [hovered, setHovered] = useState(0)
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [open, setOpen] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (rating === 0) { setError('Vui lòng chọn số sao'); return }
    setSubmitting(true)
    setError(null)
    try {
      const res = await reviewService.createReview({
        productId,
        orderId: 0,
        rating,
        content: content.trim() || undefined,
      })
      const review = (res as any)?.data as ReviewResponse
      if (review) {
        onSuccess(review)
        setRating(0)
        setContent('')
        setOpen(false)
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? 'Không thể gửi đánh giá'
      setError(msg)
    } finally {
      setSubmitting(false)
    }
  }

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="w-full py-2.5 border-2 border-dashed border-gray-300 rounded-lg text-sm text-gray-500 hover:border-primary-400 hover:text-primary-600 transition-colors"
      >
        + Viết đánh giá của bạn
      </button>
    )
  }

  const display = hovered || rating

  return (
    <form onSubmit={handleSubmit} className="bg-yellow-50 border border-yellow-200 rounded-xl p-4 space-y-3">
      <h4 className="font-semibold text-gray-800 text-sm">Đánh giá sản phẩm</h4>

      {/* Star selector */}
      <div>
        <p className="text-xs text-gray-500 mb-1">Xếp hạng *</p>
        <div className="flex items-center gap-1">
          {[1, 2, 3, 4, 5].map((s) => (
            <button
              key={s}
              type="button"
              onMouseEnter={() => setHovered(s)}
              onMouseLeave={() => setHovered(0)}
              onClick={() => setRating(s)}
              className="focus:outline-none"
            >
              {s <= display ? (
                <StarIcon className="w-7 h-7 text-yellow-400 hover:scale-110 transition-transform" />
              ) : (
                <StarOutline className="w-7 h-7 text-gray-300 hover:text-yellow-300 transition-colors" />
              )}
            </button>
          ))}
          {rating > 0 && (
            <span className="ml-2 text-sm text-gray-500">{LABELS[rating]}</span>
          )}
        </div>
      </div>

      {/* Content */}
      <div>
        <p className="text-xs text-gray-500 mb-1">Nhận xét (tùy chọn)</p>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={2000}
          rows={3}
          placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
          className="w-full border border-gray-200 rounded-lg p-2.5 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary-300 bg-white"
        />
        <p className="text-xs text-gray-400 text-right">{content.length}/2000</p>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <div className="flex gap-2 justify-end">
        <button
          type="button"
          onClick={() => { setOpen(false); setError(null); setRating(0); setContent('') }}
          className="px-4 py-1.5 text-sm text-gray-600 hover:text-gray-800"
        >
          Hủy
        </button>
        <button
          type="submit"
          disabled={submitting}
          className="px-4 py-1.5 bg-primary-600 text-white text-sm rounded-lg hover:bg-primary-700 disabled:opacity-60 transition-colors"
        >
          {submitting ? 'Đang gửi...' : 'Gửi đánh giá'}
        </button>
      </div>
    </form>
  )
}
