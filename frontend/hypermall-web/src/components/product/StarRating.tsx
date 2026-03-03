import { StarIcon } from '@heroicons/react/24/solid'
import { StarIcon as StarOutlineIcon } from '@heroicons/react/24/outline'

interface StarRatingProps {
  rating: number
  maxStars?: number
  size?: 'sm' | 'md' | 'lg'
  showValue?: boolean
  count?: number
}

const sizeMap = { sm: 'w-3 h-3', md: 'w-4 h-4', lg: 'w-5 h-5' }

export default function StarRating({ rating, maxStars = 5, size = 'md', showValue, count }: StarRatingProps) {
  return (
    <div className="flex items-center gap-1">
      <div className="flex">
        {Array.from({ length: maxStars }).map((_, i) => {
          const filled = i < Math.floor(rating)
          const half = !filled && i < rating
          return filled ? (
            <StarIcon key={i} className={`${sizeMap[size]} text-yellow-400`} />
          ) : half ? (
            <div key={i} className={`relative ${sizeMap[size]}`}>
              <StarOutlineIcon className={`${sizeMap[size]} text-yellow-400 absolute`} />
              <div className="overflow-hidden w-1/2">
                <StarIcon className={`${sizeMap[size]} text-yellow-400`} />
              </div>
            </div>
          ) : (
            <StarOutlineIcon key={i} className={`${sizeMap[size]} text-gray-300`} />
          )
        })}
      </div>
      {showValue && <span className="text-sm text-gray-600">{rating.toFixed(1)}</span>}
      {count !== undefined && <span className="text-sm text-gray-500">({count.toLocaleString()})</span>}
    </div>
  )
}
