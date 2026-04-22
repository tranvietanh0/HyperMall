import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ClipboardDocumentListIcon,
  ExclamationTriangleIcon,
  ShoppingBagIcon,
  StarIcon,
  UserGroupIcon,
} from '@heroicons/react/24/outline';
import Loading from '@/components/common/Loading';
import Button from '@/components/common/Button';
import { sellerOrderService, sellerProductService, sellerService } from '@/services';
import type { OrderSummary, SellerDashboard, SellerProfile } from '@/types';
import { formatCurrency, formatDate, getErrorMessage } from '@/utils';

interface StatCardProps {
  title: string;
  value: string;
  helper: string;
  icon: React.ComponentType<{ className?: string }>;
}

function StatCard({ title, value, helper, icon: Icon }: StatCardProps) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm text-gray-500">{title}</p>
          <p className="mt-2 text-2xl font-semibold text-gray-900">{value}</p>
          <p className="mt-1 text-sm text-gray-500">{helper}</p>
        </div>
        <div className="rounded-xl bg-primary-50 p-3 text-primary-600">
          <Icon className="h-6 w-6" />
        </div>
      </div>
    </div>
  );
}

function getStatusTone(status?: string) {
  switch (status) {
    case 'ACTIVE':
      return 'border-green-200 bg-green-50 text-green-700';
    case 'PENDING':
      return 'border-yellow-200 bg-yellow-50 text-yellow-700';
    case 'SUSPENDED':
      return 'border-red-200 bg-red-50 text-red-700';
    default:
      return 'border-gray-200 bg-gray-50 text-gray-700';
  }
}

export default function SellerDashboardPage() {
  const [profile, setProfile] = useState<SellerProfile | null>(null);
  const [dashboard, setDashboard] = useState<SellerDashboard | null>(null);
  const [recentOrders, setRecentOrders] = useState<OrderSummary[]>([]);
  const [productCount, setProductCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDashboard = async () => {
      setLoading(true);
      setError('');
      try {
        const [profileData, dashboardData, productsData, ordersData] = await Promise.all([
          sellerService.getMySellerProfile(),
          sellerService.getSellerDashboard(),
          sellerProductService.getMyProducts(0, 5),
          sellerOrderService.getSellerOrders(0, 5),
        ]);

        setProfile(profileData);
        setDashboard(dashboardData);
        setProductCount(productsData.totalElements);
        setRecentOrders(ordersData.content);
      } catch (err) {
        setError(getErrorMessage(err, 'Unable to load seller dashboard'));
      } finally {
        setLoading(false);
      }
    };

    void loadDashboard();
  }, []);

  const pendingOrders = useMemo(
    () => recentOrders.filter((order) => ['PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'PROCESSING'].includes(order.status)).length,
    [recentOrders]
  );

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center">
        <Loading size="lg" text="Loading dashboard..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">
        <p className="font-medium">{error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {profile?.status && profile.status !== 'ACTIVE' ? (
        <div className={`rounded-2xl border p-4 ${getStatusTone(profile.status)}`}>
          <div className="flex items-start gap-3">
            <ExclamationTriangleIcon className="mt-0.5 h-5 w-5" />
            <div>
              <p className="font-semibold">Seller status: {profile.status}</p>
              <p className="mt-1 text-sm">
                {profile.status === 'PENDING'
                  ? 'Your shop is waiting for approval. Complete your profile to speed up review.'
                  : 'Your shop is currently suspended. Review your settings and contact support if needed.'}
              </p>
            </div>
          </div>
        </div>
      ) : null}

      <div className="flex flex-col gap-4 rounded-3xl bg-slate-950 p-6 text-white lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.2em] text-cyan-300">Seller dashboard</p>
          <h2 className="mt-2 text-3xl font-semibold">{dashboard?.shopName || profile?.shopName || 'Your store'}</h2>
          <p className="mt-2 max-w-2xl text-sm text-slate-300">
            Track catalog health, recent orders, and storefront progress in one place.
          </p>
        </div>
        <div className="flex flex-wrap gap-3">
          <Link to="/seller/products/new">
            <Button>Create product</Button>
          </Link>
          <Link to="/seller/settings">
            <Button variant="outline" className="border-white/20 text-white hover:bg-white/10 hover:text-white">
              Update profile
            </Button>
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Products"
          value={String(dashboard?.totalProducts ?? productCount)}
          helper="Active catalog entries"
          icon={ShoppingBagIcon}
        />
        <StatCard
          title="Followers"
          value={String(dashboard?.totalFollowers ?? profile?.totalFollowers ?? 0)}
          helper="Store audience"
          icon={UserGroupIcon}
        />
        <StatCard
          title="Rating"
          value={(dashboard?.rating ?? profile?.rating ?? 0).toFixed(1)}
          helper="Average shop rating"
          icon={StarIcon}
        />
        <StatCard
          title="Pending orders"
          value={String(pendingOrders)}
          helper="Need seller action"
          icon={ClipboardDocumentListIcon}
        />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="rounded-2xl border border-gray-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-900">Recent orders</h3>
              <p className="text-sm text-gray-500">Latest order activity in your shop</p>
            </div>
            <Link to="/seller/orders" className="text-sm font-medium text-primary-600 hover:underline">
              View all
            </Link>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Order</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Items</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Total</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Created</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {recentOrders.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-10 text-center text-sm text-gray-500">
                      No orders yet.
                    </td>
                  </tr>
                ) : (
                  recentOrders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">#{order.orderNumber}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{order.status}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{order.totalItems}</td>
                      <td className="px-6 py-4 text-sm font-medium text-primary-600">{formatCurrency(order.total)}</td>
                      <td className="px-6 py-4 text-sm text-gray-500">{formatDate(order.createdAt)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="space-y-6">
          <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
            <h3 className="text-lg font-semibold text-gray-900">Store summary</h3>
            <dl className="mt-4 space-y-4 text-sm">
              <div className="flex items-center justify-between gap-4">
                <dt className="text-gray-500">Shop slug</dt>
                <dd className="font-medium text-gray-900">{dashboard?.shopSlug || profile?.shopSlug || '-'}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-gray-500">Business type</dt>
                <dd className="font-medium text-gray-900">{profile?.businessType || '-'}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-gray-500">Joined</dt>
                <dd className="font-medium text-gray-900">{profile?.createdAt ? formatDate(profile.createdAt) : '-'}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-gray-500">Last updated</dt>
                <dd className="font-medium text-gray-900">
                  {dashboard?.lastUpdatedAt ? formatDate(dashboard.lastUpdatedAt) : profile?.updatedAt ? formatDate(profile.updatedAt) : '-'}
                </dd>
              </div>
            </dl>
          </div>

          <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
            <h3 className="text-lg font-semibold text-gray-900">Recommended next steps</h3>
            <div className="mt-4 space-y-3 text-sm text-gray-600">
              <p>• Add more products to improve store discoverability.</p>
              <p>• Keep bank and tax info up to date before scaling sales.</p>
              <p>• Review pending orders daily to reduce cancellation risk.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
