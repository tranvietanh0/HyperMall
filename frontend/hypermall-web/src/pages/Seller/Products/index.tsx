import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  PencilSquareIcon,
  PlusIcon,
  TrashIcon,
} from '@heroicons/react/24/outline';
import Button from '@/components/common/Button';
import Loading from '@/components/common/Loading';
import { sellerProductService } from '@/services';
import type { Product } from '@/types';
import { formatCurrency, formatDate, getErrorMessage } from '@/utils';

export default function SellerProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const loadProducts = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await sellerProductService.getMyProducts(0, 50);
      setProducts(response.content);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to load seller products'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadProducts();
  }, []);

  const summary = useMemo(() => ({
    total: products.length,
    active: products.filter((product) => product.status === 'ACTIVE').length,
    draft: products.filter((product) => product.status === 'DRAFT').length,
  }), [products]);

  const handleDelete = async (productId: number) => {
    if (!window.confirm('Delete this product?')) {
      return;
    }

    setDeletingId(productId);
    try {
      await sellerProductService.deleteProduct(productId);
      setProducts((current) => current.filter((product) => product.id !== productId));
      toast.success('Product deleted successfully');
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Unable to delete product'));
    } finally {
      setDeletingId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center">
        <Loading size="lg" text="Loading products..." />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 rounded-3xl bg-white p-6 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Products</h1>
          <p className="mt-1 text-sm text-gray-500">Manage your catalog, update pricing, and keep inventory ready.</p>
        </div>
        <Link to="/seller/products/new">
          <Button leftIcon={<PlusIcon className="h-4 w-4" />}>Add product</Button>
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-gray-500">Total products</p>
          <p className="mt-2 text-2xl font-semibold text-gray-900">{summary.total}</p>
        </div>
        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-gray-500">Active</p>
          <p className="mt-2 text-2xl font-semibold text-green-600">{summary.active}</p>
        </div>
        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-gray-500">Draft</p>
          <p className="mt-2 text-2xl font-semibold text-yellow-600">{summary.draft}</p>
        </div>
      </div>

      {error ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-red-700">{error}</div>
      ) : null}

      <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Product</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Category</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Price</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Created</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 bg-white">
              {products.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-sm text-gray-500">
                    No products yet. Add your first product to start selling.
                  </td>
                </tr>
              ) : (
                products.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-4">
                        <img
                          src={product.thumbnail}
                          alt={product.name}
                          className="h-14 w-14 rounded-xl border border-gray-200 object-cover"
                        />
                        <div>
                          <p className="font-medium text-gray-900">{product.name}</p>
                          <p className="text-sm text-gray-500">SKU slug: {product.slug}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">{product.categoryName}</td>
                    <td className="px-6 py-4 text-sm font-medium text-primary-600">
                      {formatCurrency(product.salePrice ?? product.basePrice)}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">{product.status}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{formatDate(product.createdAt)}</td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <Link to={`/seller/products/${product.id}/edit`}>
                          <Button variant="ghost" size="sm" leftIcon={<PencilSquareIcon className="h-4 w-4" />}>
                            Edit
                          </Button>
                        </Link>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-red-600 hover:bg-red-50 hover:text-red-700"
                          leftIcon={<TrashIcon className="h-4 w-4" />}
                          isLoading={deletingId === product.id}
                          onClick={() => void handleDelete(product.id)}
                        >
                          Delete
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
