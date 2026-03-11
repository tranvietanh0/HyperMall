import { useState, useEffect } from 'react';
import {
  ArrowUpIcon,
  ArrowDownIcon,
} from '@heroicons/react/24/outline';
import clsx from 'clsx';

interface DailyStat {
  date: string;
  orders: number;
  revenue: number;
  users: number;
}

interface TopProduct {
  id: number;
  name: string;
  views: number;
  sales: number;
  revenue: number;
}

export default function AdminAnalytics() {
  const [loading, setLoading] = useState(true);
  const [dateRange, setDateRange] = useState('7d');
  const [dailyStats, setDailyStats] = useState<DailyStat[]>([]);
  const [topProducts, setTopProducts] = useState<TopProduct[]>([]);
  const [summary, setSummary] = useState({
    totalOrders: 0,
    totalRevenue: 0,
    totalUsers: 0,
    conversionRate: 0,
    ordersChange: 0,
    revenueChange: 0,
    usersChange: 0,
  });

  useEffect(() => {
    setTimeout(() => {
      setDailyStats([
        { date: '2026-03-05', orders: 45, revenue: 125000000, users: 320 },
        { date: '2026-03-06', orders: 52, revenue: 142000000, users: 380 },
        { date: '2026-03-07', orders: 38, revenue: 98000000, users: 290 },
        { date: '2026-03-08', orders: 61, revenue: 168000000, users: 420 },
        { date: '2026-03-09', orders: 55, revenue: 152000000, users: 395 },
        { date: '2026-03-10', orders: 72, revenue: 198000000, users: 485 },
        { date: '2026-03-11', orders: 48, revenue: 132000000, users: 350 },
      ]);
      setTopProducts([
        { id: 1, name: 'iPhone 15 Pro Max', views: 15420, sales: 89, revenue: 3114110000 },
        { id: 2, name: 'MacBook Pro 14"', views: 8950, sales: 42, revenue: 2099580000 },
        { id: 3, name: 'Samsung Galaxy S24 Ultra', views: 7820, sales: 65, revenue: 1689350000 },
        { id: 4, name: 'AirPods Pro 2', views: 6540, sales: 156, revenue: 1090440000 },
        { id: 5, name: 'iPad Pro 12.9"', views: 5230, sales: 38, revenue: 1139620000 },
      ]);
      setSummary({
        totalOrders: 371,
        totalRevenue: 1015000000,
        totalUsers: 2640,
        conversionRate: 3.2,
        ordersChange: 12.5,
        revenueChange: 8.3,
        usersChange: 15.7,
      });
      setLoading(false);
    }, 500);
  }, [dateRange]);

  const formatCurrency = (value: number) => {
    if (value >= 1000000000) {
      return (value / 1000000000).toFixed(1) + 'B';
    }
    if (value >= 1000000) {
      return (value / 1000000).toFixed(1) + 'M';
    }
    return value.toLocaleString();
  };

  const maxRevenue = Math.max(...dailyStats.map(s => s.revenue));

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-4 border-blue-600 rounded-full animate-spin border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Analytics</h1>
        <select
          value={dateRange}
          onChange={(e) => setDateRange(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        >
          <option value="7d">Last 7 days</option>
          <option value="30d">Last 30 days</option>
          <option value="90d">Last 90 days</option>
        </select>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center justify-between">
            <p className="text-sm font-medium text-gray-500">Total Orders</p>
            <span
              className={clsx(
                'flex items-center text-sm font-medium',
                summary.ordersChange >= 0 ? 'text-green-600' : 'text-red-600'
              )}
            >
              {summary.ordersChange >= 0 ? (
                <ArrowUpIcon className="w-4 h-4 mr-1" />
              ) : (
                <ArrowDownIcon className="w-4 h-4 mr-1" />
              )}
              {Math.abs(summary.ordersChange)}%
            </span>
          </div>
          <p className="mt-2 text-3xl font-bold text-gray-900">{summary.totalOrders}</p>
        </div>
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center justify-between">
            <p className="text-sm font-medium text-gray-500">Total Revenue</p>
            <span
              className={clsx(
                'flex items-center text-sm font-medium',
                summary.revenueChange >= 0 ? 'text-green-600' : 'text-red-600'
              )}
            >
              {summary.revenueChange >= 0 ? (
                <ArrowUpIcon className="w-4 h-4 mr-1" />
              ) : (
                <ArrowDownIcon className="w-4 h-4 mr-1" />
              )}
              {Math.abs(summary.revenueChange)}%
            </span>
          </div>
          <p className="mt-2 text-3xl font-bold text-gray-900">{formatCurrency(summary.totalRevenue)} VND</p>
        </div>
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center justify-between">
            <p className="text-sm font-medium text-gray-500">Total Users</p>
            <span
              className={clsx(
                'flex items-center text-sm font-medium',
                summary.usersChange >= 0 ? 'text-green-600' : 'text-red-600'
              )}
            >
              {summary.usersChange >= 0 ? (
                <ArrowUpIcon className="w-4 h-4 mr-1" />
              ) : (
                <ArrowDownIcon className="w-4 h-4 mr-1" />
              )}
              {Math.abs(summary.usersChange)}%
            </span>
          </div>
          <p className="mt-2 text-3xl font-bold text-gray-900">{summary.totalUsers}</p>
        </div>
        <div className="p-6 bg-white rounded-lg shadow">
          <p className="text-sm font-medium text-gray-500">Conversion Rate</p>
          <p className="mt-2 text-3xl font-bold text-gray-900">{summary.conversionRate}%</p>
        </div>
      </div>

      {/* Revenue Chart */}
      <div className="p-6 bg-white rounded-lg shadow">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">Revenue Overview</h2>
        <div className="flex items-end h-64 space-x-2">
          {dailyStats.map((stat) => (
            <div key={stat.date} className="flex flex-col items-center flex-1">
              <div
                className="w-full transition-all bg-blue-500 rounded-t hover:bg-blue-600"
                style={{ height: `${(stat.revenue / maxRevenue) * 100}%` }}
              />
              <p className="mt-2 text-xs text-gray-500">
                {new Date(stat.date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* Top Products */}
      <div className="bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">Top Products</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Product
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Views
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Sales
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Revenue
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {topProducts.map((product, index) => (
                <tr key={product.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center">
                      <span className="flex items-center justify-center w-8 h-8 mr-3 text-sm font-medium text-gray-600 bg-gray-100 rounded-full">
                        {index + 1}
                      </span>
                      <span className="font-medium text-gray-900">{product.name}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {product.views.toLocaleString()}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {product.sales}
                  </td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">
                    {formatCurrency(product.revenue)} VND
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
