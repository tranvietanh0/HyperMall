import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ClipboardDocumentListIcon,
  ExclamationTriangleIcon,
  ShoppingBagIcon,
  StarIcon,
  UserGroupIcon,
  PlusIcon,
  ArrowTrendingUpIcon,
  ChartBarIcon,
  Cog6ToothIcon,
} from '@heroicons/react/24/outline';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import Loading from '@/components/common/Loading';
import Button from '@/components/common/Button';
import { sellerOrderService, sellerProductService, sellerService } from '@/services';
import type { OrderSummary, SellerDashboard, SellerProfile } from '@/types';
import { formatCurrency, formatDate, getErrorMessage } from '@/utils';
import clsx from 'clsx';

// Mock data for the chart
const chartData = [
  { name: 'Mon', sales: 4000, orders: 24 },
  { name: 'Tue', sales: 3000, orders: 18 },
  { name: 'Wed', sales: 2000, orders: 15 },
  { name: 'Thu', sales: 2780, orders: 20 },
  { name: 'Fri', sales: 1890, orders: 12 },
  { name: 'Sat', sales: 2390, orders: 25 },
  { name: 'Sun', sales: 3490, orders: 30 },
];

interface StatCardProps {
  title: string;
  value: string;
  helper: string;
  icon: React.ComponentType<{ className?: string }>;
  trend?: { value: string; positive: boolean };
}

function StatCard({ title, value, helper, icon: Icon, trend }: StatCardProps) {
  return (
    <div className="group relative overflow-hidden rounded-2xl border border-gray-200 bg-white p-6 transition-all hover:border-cyan-500/50 hover:shadow-lg hover:shadow-cyan-500/10">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-gray-500">{title}</p>
          <div className="mt-2 flex items-baseline gap-2">
            <p className="text-3xl font-bold tracking-tight text-gray-900">{value}</p>
            {trend && (
              <span className={clsx(
                'flex items-center text-xs font-semibold',
                trend.positive ? 'text-emerald-500' : 'text-rose-500'
              )}>
                {trend.positive ? '+' : '-'}{trend.value}%
              </span>
            )}
          </div>
          <p className="mt-1 text-sm text-gray-400">{helper}</p>
        </div>
        <div className="rounded-xl bg-slate-50 p-3 text-slate-600 transition-colors group-hover:bg-cyan-50 group-hover:text-cyan-600">
          <Icon className="h-6 w-6" />
        </div>
      </div>
      <div className="absolute bottom-0 left-0 h-1 w-0 bg-cyan-500 transition-all group-hover:w-full" />
    </div>
  );
}

function QuickAction({
  title,
  icon: Icon,
  to,
  color,
}: {
  title: string;
  icon: React.ComponentType<{ className?: string }>;
  to: string;
  color: string;
}) {
  return (
    <Link to={to} className="group flex flex-col items-center gap-3 rounded-2xl border border-gray-100 bg-gray-50/50 p-4 transition-all hover:bg-white hover:shadow-md">
      <div className={clsx('rounded-xl p-3 text-white shadow-sm transition-transform group-hover:scale-110', color)}>
        <Icon className="h-6 w-6" />
      </div>
      <span className="text-sm font-medium text-gray-700">{title}</span>
    </Link>
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
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4">
        <Loading size="lg" />
        <p className="animate-pulse text-sm font-medium text-gray-500">Initializing workspace...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">
        <div className="flex items-center gap-3">
          <ExclamationTriangleIcon className="h-6 w-6" />
          <p className="font-medium">{error}</p>
        </div>
        <Button onClick={() => window.location.reload()} variant="outline" className="mt-4 border-red-200 text-red-700 hover:bg-red-100">
          Try again
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-8 pb-10">
      {/* Top Banner & Profile Overview */}
      <div className="relative overflow-hidden rounded-[2.5rem] bg-slate-950 px-8 py-10 text-white shadow-2xl">
        <div className="absolute right-0 top-0 h-full w-1/3 bg-gradient-to-l from-cyan-500/10 to-transparent" />
        <div className="relative z-10 flex flex-col gap-8 lg:flex-row lg:items-center lg:justify-between">
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <span className="inline-flex items-center rounded-full bg-cyan-500/20 px-3 py-1 text-xs font-medium text-cyan-300 ring-1 ring-inset ring-cyan-500/20">
                Seller Pro
              </span>
              {profile?.status && (
                <span className={clsx(
                  'inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ring-1 ring-inset',
                  profile.status === 'ACTIVE' ? 'bg-emerald-500/20 text-emerald-300 ring-emerald-500/20' : 'bg-amber-500/20 text-amber-300 ring-amber-500/20'
                )}>
                  {profile.status}
                </span>
              )}
            </div>
            <div>
              <h2 className="text-4xl font-bold tracking-tight">{dashboard?.shopName || profile?.shopName || 'Your store'}</h2>
              <p className="mt-3 max-w-xl text-lg text-slate-400">
                Welcome back to your command center. Everything you need to grow your brand is right here.
              </p>
            </div>
          </div>
          <div className="flex flex-wrap gap-4">
            <Link to="/seller/products/new">
              <Button size="lg" className="rounded-2xl bg-cyan-500 px-8 hover:bg-cyan-400">
                <PlusIcon className="mr-2 h-5 w-5" />
                Add Product
              </Button>
            </Link>
            <Link to="/seller/settings">
              <Button size="lg" variant="outline" className="rounded-2xl border-white/10 bg-white/5 px-8 text-white backdrop-blur-sm hover:bg-white/10 hover:text-white">
                Store Settings
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {profile?.status && profile.status !== 'ACTIVE' ? (
        <div className={`rounded-2xl border p-5 ${getStatusTone(profile.status)}`}>
          <div className="flex items-start gap-4">
            <div className="rounded-full bg-white/50 p-2">
              <ExclamationTriangleIcon className="h-6 w-6" />
            </div>
            <div>
              <p className="text-lg font-bold">Action Required</p>
              <p className="mt-1">
                {profile.status === 'PENDING'
                  ? 'Your shop is currently in review. Our team will notify you once approved.'
                  : 'Account suspended due to policy violation. Please contact support immediately.'}
              </p>
            </div>
          </div>
        </div>
      ) : null}

      {/* Main Stats Grid */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Products"
          value={String(dashboard?.totalProducts ?? productCount)}
          helper="Active listings"
          icon={ShoppingBagIcon}
          trend={{ value: '12', positive: true }}
        />
        <StatCard
          title="Followers"
          value={String(dashboard?.totalFollowers ?? profile?.totalFollowers ?? 0)}
          helper="Loyal audience"
          icon={UserGroupIcon}
          trend={{ value: '5.4', positive: true }}
        />
        <StatCard
          title="Rating"
          value={(dashboard?.rating ?? profile?.rating ?? 0).toFixed(1)}
          helper="Customer satisfaction"
          icon={StarIcon}
        />
        <StatCard
          title="Pending Orders"
          value={String(pendingOrders)}
          helper="Needs fulfillment"
          icon={ClipboardDocumentListIcon}
          trend={{ value: '2', positive: false }}
        />
      </div>

      <div className="grid grid-cols-1 gap-8 xl:grid-cols-[1fr_350px]">
        <div className="space-y-8">
          {/* Sales Chart Section */}
          <div className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm">
            <div className="mb-8 flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold text-gray-900">Sales Overview</h3>
                <p className="text-sm text-gray-500">Weekly performance tracking</p>
              </div>
              <div className="flex items-center gap-2 rounded-xl bg-gray-50 p-1">
                <button className="rounded-lg bg-white px-4 py-1.5 text-xs font-bold text-gray-900 shadow-sm">Sales</button>
                <button className="rounded-lg px-4 py-1.5 text-xs font-bold text-gray-500 transition-colors hover:text-gray-700">Orders</button>
              </div>
            </div>
            <div className="h-[350px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.1}/>
                      <stop offset="95%" stopColor="#06b6d4" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis 
                    dataKey="name" 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#94a3b8', fontSize: 12 }} 
                    dy={10}
                  />
                  <YAxis 
                    axisLine={false} 
                    tickLine={false} 
                    tick={{ fill: '#94a3b8', fontSize: 12 }}
                  />
                  <Tooltip 
                    contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="sales" 
                    stroke="#06b6d4" 
                    strokeWidth={3}
                    fillOpacity={1} 
                    fill="url(#colorSales)" 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Recent Orders Table */}
          <div className="overflow-hidden rounded-3xl border border-gray-100 bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-gray-50 px-6 py-5">
              <div>
                <h3 className="text-xl font-bold text-gray-900">Recent Orders</h3>
                <p className="text-sm text-gray-500">Monitor your latest transactions</p>
              </div>
              <Link to="/seller/orders">
                <Button variant="outline" size="sm" className="rounded-xl border-gray-200">View All</Button>
              </Link>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full">
                <thead className="bg-gray-50/50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Order ID</th>
                    <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Customer</th>
                    <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Status</th>
                    <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Total</th>
                    <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-wider text-gray-400">Date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {recentOrders.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-12 text-center">
                        <div className="flex flex-col items-center gap-2 text-gray-400">
                          <ShoppingBagIcon className="h-10 w-10 opacity-20" />
                          <p className="text-sm">No orders found</p>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    recentOrders.map((order) => (
                      <tr key={order.id} className="group transition-colors hover:bg-gray-50/50">
                        <td className="px-6 py-4">
                          <span className="font-mono text-sm font-bold text-gray-900">#{order.orderNumber}</span>
                        </td>
                        <td className="px-6 py-4">
                          <span className="text-sm text-gray-600">Customer #{order.userId}</span>
                        </td>
                        <td className="px-6 py-4">
                          <span className={clsx(
                            'inline-flex items-center rounded-lg px-2.5 py-1 text-xs font-bold',
                            order.status === 'COMPLETED' ? 'bg-emerald-50 text-emerald-600' : 'bg-blue-50 text-blue-600'
                          )}>
                            {order.status}
                          </span>
                        </td>
                        <td className="px-6 py-4">
                          <span className="text-sm font-bold text-cyan-600">{formatCurrency(order.total)}</span>
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500">
                          {formatDate(order.createdAt)}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div className="space-y-8">
          {/* Quick Actions Grid */}
          <div className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm">
            <h3 className="mb-5 text-lg font-bold text-gray-900">Quick Actions</h3>
            <div className="grid grid-cols-2 gap-3">
              <QuickAction title="Add Product" icon={PlusIcon} to="/seller/products/new" color="bg-cyan-500" />
              <QuickAction title="All Orders" icon={ClipboardDocumentListIcon} to="/seller/orders" color="bg-indigo-500" />
              <QuickAction title="Analytics" icon={ChartBarIcon} to="/seller/analytics" color="bg-rose-500" />
              <QuickAction title="Settings" icon={Cog6ToothIcon} to="/seller/settings" color="bg-slate-700" />
            </div>
          </div>

          {/* Business Insights */}
          <div className="rounded-3xl border border-gray-100 bg-white p-6 shadow-sm">
            <div className="flex items-center gap-2">
              <ArrowTrendingUpIcon className="h-5 w-5 text-cyan-500" />
              <h3 className="text-lg font-bold text-gray-900">Growth Tips</h3>
            </div>
            <div className="mt-5 space-y-4">
              <div className="rounded-2xl bg-amber-50 p-4">
                <p className="text-xs font-bold uppercase tracking-wider text-amber-700">Optimization</p>
                <p className="mt-1 text-sm text-amber-900">Complete your shop profile to gain 15% more visibility.</p>
              </div>
              <div className="rounded-2xl bg-blue-50 p-4">
                <p className="text-xs font-bold uppercase tracking-wider text-blue-700">Marketing</p>
                <p className="mt-1 text-sm text-blue-900">Run a Flash Sale to clear old inventory faster.</p>
              </div>
            </div>
          </div>

          {/* Store Info Mini Card */}
          <div className="rounded-3xl border border-gray-100 bg-slate-900 p-6 text-white">
            <h3 className="text-lg font-bold">Store Details</h3>
            <div className="mt-4 space-y-3 text-sm opacity-80">
              <div className="flex justify-between">
                <span>Slug</span>
                <span className="font-mono">{dashboard?.shopSlug || profile?.shopSlug || '-'}</span>
              </div>
              <div className="flex justify-between">
                <span>Type</span>
                <span>{profile?.businessType || 'Standard'}</span>
              </div>
              <div className="flex justify-between">
                <span>Joined</span>
                <span>{profile?.createdAt ? formatDate(profile.createdAt) : '-'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
