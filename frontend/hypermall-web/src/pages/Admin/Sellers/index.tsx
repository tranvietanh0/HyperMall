import { useCallback, useEffect, useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import {
  MagnifyingGlassIcon,
  CheckIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';
import clsx from 'clsx';
import Loading from '@/components/common/Loading';
import { sellerService } from '@/services';
import type { SellerProfile, SellerStatus } from '@/types';
import { formatDate, formatNumber, getErrorMessage } from '@/utils';

const statusOptions: Array<{ value: 'all' | SellerStatus; label: string }> = [
  { value: 'all', label: 'All status' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'SUSPENDED', label: 'Suspended' },
];

function getStatusColor(status: SellerStatus) {
  switch (status) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-800';
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800';
    case 'SUSPENDED':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

export default function AdminSellers() {
  const [sellers, setSellers] = useState<SellerProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | SellerStatus>('all');
  const [updatingSellerId, setUpdatingSellerId] = useState<number | null>(null);

  const loadSellers = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await sellerService.searchAdminSellers({
        keyword: search.trim() || undefined,
        status: statusFilter === 'all' ? undefined : statusFilter,
        page: 0,
        size: 100,
      });
      setSellers(response.content);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to load sellers'));
    } finally {
      setLoading(false);
    }
  }, [search, statusFilter]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadSellers();
    }, 250);

    return () => window.clearTimeout(timer);
  }, [loadSellers]);

  const summary = useMemo(
    () => ({
      total: sellers.length,
      active: sellers.filter((seller) => seller.status === 'ACTIVE').length,
      pending: sellers.filter((seller) => seller.status === 'PENDING').length,
    }),
    [sellers]
  );

  const handleStatusUpdate = async (sellerId: number, status: SellerStatus) => {
    setUpdatingSellerId(sellerId);
    try {
      const updatedSeller = await sellerService.updateAdminSellerStatus(sellerId, status);
      setSellers((current) => current.map((seller) => (seller.id === sellerId ? updatedSeller : seller)));
      toast.success('Seller status updated successfully');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Unable to update seller status'));
    } finally {
      setUpdatingSellerId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loading size="lg" text="Loading sellers..." />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Seller Management</h1>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-lg bg-white p-4 shadow">
          <p className="text-sm text-gray-500">Total Sellers</p>
          <p className="text-2xl font-bold text-gray-900">{summary.total}</p>
        </div>
        <div className="rounded-lg bg-white p-4 shadow">
          <p className="text-sm text-gray-500">Active Sellers</p>
          <p className="text-2xl font-bold text-green-600">{summary.active}</p>
        </div>
        <div className="rounded-lg bg-white p-4 shadow">
          <p className="text-sm text-gray-500">Pending Approval</p>
          <p className="text-2xl font-bold text-yellow-600">{summary.pending}</p>
        </div>
      </div>

      <div className="flex flex-col gap-4 sm:flex-row">
        <div className="relative flex-1">
          <MagnifyingGlassIcon className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 transform text-gray-400" />
          <input
            type="text"
            placeholder="Search sellers..."
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            className="w-full rounded-lg border border-gray-300 py-2 pl-10 pr-4 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(event) => setStatusFilter(event.target.value as 'all' | SellerStatus)}
          className="rounded-lg border border-gray-300 px-4 py-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
        >
          {statusOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {error ? <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-600">{error}</div> : null}

      <div className="overflow-hidden rounded-lg bg-white shadow">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Shop</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Type</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Rating</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Products</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Followers</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Created</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {sellers.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-6 py-12 text-center text-sm text-gray-500">
                    No sellers found.
                  </td>
                </tr>
              ) : (
                sellers.map((seller) => (
                  <tr key={seller.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div>
                        <p className="font-medium text-gray-900">{seller.shopName}</p>
                        <p className="text-sm text-gray-500">/{seller.shopSlug}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">{seller.businessType}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{seller.rating > 0 ? `${seller.rating.toFixed(1)}/5` : '-'}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{formatNumber(seller.totalProducts)}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{formatNumber(seller.totalFollowers)}</td>
                    <td className="px-6 py-4">
                      <span className={clsx('rounded-full px-2 py-1 text-xs font-medium', getStatusColor(seller.status))}>
                        {seller.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">{formatDate(seller.createdAt)}</td>
                    <td className="px-6 py-4">
                      <div className="flex items-center space-x-2">
                        {seller.status === 'PENDING' ? (
                          <>
                            <button
                              onClick={() => void handleStatusUpdate(seller.id, 'ACTIVE')}
                              disabled={updatingSellerId === seller.id}
                              className="p-1 text-gray-400 hover:text-green-600 disabled:opacity-50"
                              title="Approve"
                            >
                              <CheckIcon className="h-5 w-5" />
                            </button>
                            <button
                              onClick={() => void handleStatusUpdate(seller.id, 'SUSPENDED')}
                              disabled={updatingSellerId === seller.id}
                              className="p-1 text-gray-400 hover:text-red-600 disabled:opacity-50"
                              title="Reject"
                            >
                              <XMarkIcon className="h-5 w-5" />
                            </button>
                          </>
                        ) : seller.status === 'ACTIVE' ? (
                          <button
                            onClick={() => void handleStatusUpdate(seller.id, 'SUSPENDED')}
                            disabled={updatingSellerId === seller.id}
                            className="rounded-lg border border-red-200 px-3 py-1 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-50"
                          >
                            Suspend
                          </button>
                        ) : (
                          <button
                            onClick={() => void handleStatusUpdate(seller.id, 'ACTIVE')}
                            disabled={updatingSellerId === seller.id}
                            className="rounded-lg border border-green-200 px-3 py-1 text-sm font-medium text-green-600 hover:bg-green-50 disabled:opacity-50"
                          >
                            Activate
                          </button>
                        )}
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
