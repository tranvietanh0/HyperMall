import { Link } from 'react-router-dom';
import type { AiProductSuggestion } from '@/types';
import { formatCurrency } from '@utils/format';

type AiSuggestedProductsProps = {
  products: AiProductSuggestion[];
};

function formatPrice(price?: number) {
  if (typeof price !== 'number') {
    return 'View details';
  }

  return formatCurrency(price);
}

export default function AiSuggestedProducts({ products }: AiSuggestedProductsProps) {
  if (!products.length) {
    return null;
  }

  return (
    <div className="mt-3 grid gap-2">
      {products.map((product) => (
        <Link
          key={product.productId}
          to={`/products/${product.productId}`}
          className="flex items-center gap-3 rounded-xl border border-secondary-100 bg-white px-3 py-2 transition hover:border-primary-200 hover:bg-primary-50/40"
        >
          <img
            src={product.thumbnail || 'https://placehold.co/80x80?text=Item'}
            alt={product.productName}
            className="h-14 w-14 rounded-lg object-cover"
          />
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-semibold text-primary-900">{product.productName}</p>
            <p className="mt-1 text-xs text-secondary-500">{formatPrice(product.price)}</p>
          </div>
        </Link>
      ))}
    </div>
  );
}
