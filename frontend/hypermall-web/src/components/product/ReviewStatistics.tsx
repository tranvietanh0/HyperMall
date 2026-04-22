import { StarIcon } from '@heroicons/react/24/solid'
import type { ReviewStatistics } from '@/types'

interface Props {
  stats: ReviewStatistics
  activeRating: number | null
  onFilterChange: (rating: number | null) => void
}

export default function ReviewStatisticsPanel({ stats, activeRating, onFilterChange }: Props) {
  const { averageRating, totalReviews, ratingDistribution } = stats

  return (
    <div className="flex flex-col sm:flex-row gap-6 p-5 bg-gray-50 rounded-xl">
      {/* Average */}
      <div className="flex flex-col items-center justify-center min-w-[120px]">
        <span className="text-5xl font-bold text-gray-900">{averageRating.toFixed(1)}</span>
        <div className="flex mt-1">
          {[1, 2, 3, 4, 5].map((s) => (
            <StarIcon
              key={s}
              className={`w-5 h-5 ${s <= Math.round(averageRating) ? 'text-yellow-400' : 'text-gray-200'}`}
            />
          ))}
        </div>
        <span className="text-sm text-gray-500 mt-1">{totalReviews.toLocaleString()} đánh giá</span>
      </div>

      {/* Distribution */}
      <div className="flex-1 flex flex-col gap-1.5">
        {[5, 4, 3, 2, 1].map((star) => {
          const count = ratingDistribution[star] ?? 0
          const pct = totalReviews > 0 ? (count / totalReviews) * 100 : 0
          const isActive = activeRating === star
          return (
            <button
              key={star}
              onClick={() => onFilterChange(isActive ? null : star)}
              className={`flex items-center gap-2 group rounded px-1 py-0.5 transition-colors
                ${isActive ? 'bg-yellow-50' : 'hover:bg-gray-100'}`}
            >
              <span className="text-sm text-gray-600 w-4 text-right">{star}</span>
              <StarIcon className="w-3.5 h-3.5 text-yellow-400 flex-shrink-0" />
              <div className="flex-1 bg-gray-200 rounded-full h-2 overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all ${isActive ? 'bg-yellow-400' : 'bg-yellow-300 group-hover:bg-yellow-400'}`}
                  style={{ width: `${pct}%` }}
                />
              </div>
              <span className="text-xs text-gray-500 w-8 text-right">{count}</span>
            </button>
          )
        })}
      </div>
    </div>
  )
}
