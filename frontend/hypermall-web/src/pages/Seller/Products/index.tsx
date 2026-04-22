import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  PlusIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  EllipsisVerticalIcon,
  PencilSquareIcon,
  TrashIcon,
  ArchiveBoxIcon,
  ArrowPathIcon,
} from '@heroicons/react/24/outline';
import Loading from '@/components/common/Loading';
import Button from '@/components/common/Button';
import { sellerProductService } from '@/services';
import type { Product } from '@/types';
import { formatCurrency, getErrorMessage } from '@/utils';
import clsx from 'clsx';
import toast from 'react-hot-toast';

export default function SellerProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const loadProducts = useCallback(async () => {
    setLoading(true);
    try {
      const data = await sellerProductService.getMyProducts(page, 10);
      setProducts(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Unable to load products'));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void loadProducts();
  }, [loadProducts]);

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
      await sellerProductService.deleteProduct(id);
      toast.success('Product deleted successfully');
      void loadProducts();
    } catch (err) {
      toast.error(getErrorMessage(err, 'Failed to delete product'));
    }
  };

  const filteredProducts = products.filter(p => 
    p.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading && page === 0) {
    return (
      <div className="flex min-h-[400px] flex-col items-center justify-center">
        <Loading size="lg" />
        <p className="mt-4 text-sm font-medium text-gray-500 tracking-wide">Retrieving catalog...</p>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-700">
      {/* Header Section */}
      <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">Products Catalog</h1>
          <p className="mt-2 text-gray-500">Manage your store inventory and listing details.</p>
        </div>
        <Link to="/seller/products/new">
          <Button size="lg" className="rounded-2xl bg-cyan-600 px-6 shadow-lg shadow-cyan-600/20">
            <PlusIcon className="mr-2 h-5 w-5" />
            Add New Product
          </Button>
        </Link>
      </div>

      {/* Filters & Actions Bar */}
      <div className="flex flex-col gap-4 rounded-[2rem] border border-gray-100 bg-white p-4 shadow-sm lg:flex-row lg:items-center">
        <div className="relative flex-1">
          <MagnifyingGlassIcon className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search products by name, SKU..."
            className="w-full rounded-2xl border-none bg-gray-50 py-3 pl-12 pr-4 text-sm font-medium focus:ring-2 focus:ring-cyan-500/20"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-3">
          <button className="flex items-center gap-2 rounded-2xl border border-gray-100 bg-white px-5 py-3 text-sm font-bold text-gray-700 hover:bg-gray-50">
            <FunnelIcon className="h-5 w-5 text-gray-400" />
            Filters
          </button>
          <button 
            onClick={() => void loadProducts()}
            className="rounded-2xl border border-gray-100 bg-white p-3 text-gray-400 hover:bg-gray-50 hover:text-cyan-600"
          >
            <ArrowPathIcon className={clsx('h-5 w-5', loading && 'animate-spin')} />
          </button>
        </div>
      </div>

      {/* Products Table */}
      <div className="overflow-hidden rounded-[2rem] border border-gray-100 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-50">
            <thead className="bg-gray-50/50">
              <tr>
                <th className="px-8 py-5 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Product Details</th>
                <th className="px-6 py-5 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Category</th>
                <th className="px-6 py-5 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Price</th>
                <th className="px-6 py-5 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Stock</th>
                <th className="px-6 py-5 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Status</th>
                <th className="px-8 py-5 text-right text-xs font-bold uppercase tracking-wider text-gray-400">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 bg-white">
              {filteredProducts.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-8 py-20 text-center">
                    <div className="flex flex-col items-center gap-4">
                      <div className="rounded-full bg-gray-50 p-6">
                        <ArchiveBoxIcon className="h-12 w-12 text-gray-200" />
                      </div>
                      <div className="max-w-xs space-y-1">
                        <p className="text-lg font-bold text-gray-900">No products found</p>
                        <p className="text-sm text-gray-500">Try adjusting your filters or add your first product to get started.</p>
                      </div>
                      <Link to="/seller/products/new">
                        <Button variant="outline" className="mt-2 rounded-xl">Create Listing</Button>
                      </Link>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredProducts.map((product) => (
                  <tr key={product.id} className="group hover:bg-gray-50/50 transition-colors">
                    <td className="px-8 py-5">
                      <div className="flex items-center gap-4">
                        <div className="h-14 w-14 overflow-hidden rounded-2xl bg-gray-100 ring-1 ring-gray-200/50">
                          {product.thumbnail ? (
                            <img src={product.thumbnail} alt={product.name} className="h-full w-full object-cover transition-transform group-hover:scale-110" />
                          ) : (
                            <div className="flex h-full w-full items-center justify-center">
                              <ArchiveBoxIcon className="h-6 w-6 text-gray-300" />
                            </div>
                          )}
                        </div>
                        <div className="flex flex-col">
                          <span className="max-w-[200px] truncate text-sm font-bold text-gray-900 group-hover:text-cyan-600 transition-colors">
                            {product.name}
                          </span>
                          <span className="text-[10px] font-mono font-medium text-gray-400 uppercase tracking-tighter">Slug: {product.slug}</span>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-5">
                      <span className="inline-flex items-center rounded-lg bg-slate-100 px-2.5 py-1 text-xs font-bold text-slate-600">
                        {product.categoryName || 'General'}
                      </span>
                    </td>
                    <td className="px-6 py-5">
                      <span className="text-sm font-black text-gray-900">{formatCurrency(product.salePrice ?? product.basePrice)}</span>
                    </td>
                    <td className="px-6 py-5 text-sm font-medium text-gray-500">
                      Sold: {product.totalSold}
                    </td>
                    <td className="px-6 py-5">
                      <span className={clsx(
                        'inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-[10px] font-black uppercase tracking-wider ring-1 ring-inset',
                        product.status === 'ACTIVE'
                          ? 'bg-emerald-50 text-emerald-600 ring-emerald-500/20'
                          : product.status === 'PENDING'
                            ? 'bg-amber-50 text-amber-600 ring-amber-500/20'
                            : 'bg-gray-50 text-gray-500 ring-gray-400/20'
                      )}>
                        <span className={clsx(
                          'h-1.5 w-1.5 rounded-full',
                          product.status === 'ACTIVE'
                            ? 'bg-emerald-500'
                            : product.status === 'PENDING'
                              ? 'bg-amber-500'
                              : 'bg-gray-400'
                        )}></span>
                        {product.status}
                      </span>
                    </td>
                    <td className="px-8 py-5 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Link to={`/seller/products/${product.id}/edit`}>
                          <button className="rounded-xl p-2.5 text-gray-400 hover:bg-cyan-50 hover:text-cyan-600 transition-all">
                            <PencilSquareIcon className="h-5 w-5" />
                          </button>
                        </Link>
                        <button 
                          onClick={() => handleDelete(product.id)}
                          className="rounded-xl p-2.5 text-gray-400 hover:bg-rose-50 hover:text-rose-600 transition-all"
                        >
                          <TrashIcon className="h-5 w-5" />
                        </button>
                        <div className="relative group/menu">
                          <button className="rounded-xl p-2.5 text-gray-400 hover:bg-gray-100">
                            <EllipsisVerticalIcon className="h-5 w-5" />
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        
        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between border-t border-gray-50 bg-gray-50/30 px-8 py-4">
            <p className="text-sm font-medium text-gray-500">
              Page <span className="text-gray-900 font-bold">{page + 1}</span> of <span className="text-gray-900 font-bold">{totalPages}</span>
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="rounded-xl border-gray-200"
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="rounded-xl border-gray-200"
                disabled={page === totalPages - 1}
                onClick={() => setPage(p => p + 1)}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
