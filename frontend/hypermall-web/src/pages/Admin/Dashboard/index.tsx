import { useState, useEffect } from 'react';
import {
  CurrencyDollarIcon,
  ShoppingCartIcon,
  UsersIcon,
  ShoppingBagIcon,
  ArrowUpIcon,
  ArrowDownIcon,
} from '@heroicons/react/24/outline';
import clsx from 'clsx';

interface StatCard {
  name: string;
  value: string;
  change: number;
  icon: React.ComponentType<{ className?: string }>;
}

interface RecentOrder {
  id: string;
  customer: string;
  total: string;
  status: string;
  date: string;
}

export default function AdminDashboard() {
  const [stats, setStats] = useState<StatCard[]>([
    { name: 'Total Revenue', value: '0', change: 0, icon: CurrencyDollarIcon },
    { name: 'Total Orders', value: '0', change: 0, icon: ShoppingCartIcon },
    { name: 'Total Users', value: '0', change: 0, icon: UsersIcon },
    { name: 'Total Products', value: '0', change: 0, icon: ShoppingBagIcon },
  ]);

  const [recentOrders, setRecentOrders] = useState<RecentOrder[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Mock data - replace with actual API call
    setTimeout(() => {
      setStats([
        { name: 'Total Revenue', value: '125,430,000', change: 12.5, icon: CurrencyDollarIcon },
        { name: 'Total Orders', value: '1,234', change: 8.2, icon: ShoppingCartIcon },
        { name: 'Total Users', value: '5,678', change: 15.3, icon: UsersIcon },
        { name: 'Total Products', value: '890', change: -2.1, icon: ShoppingBagIcon },
      ]);
      setRecentOrders([
        { id: 'ORD001', customer: 'Nguyen Van A', total: '1,250,000', status: 'Completed', date: '2026-03-11' },
        { id: 'ORD002', customer: 'Tran Thi B', total: '890,000', status: 'Processing', date: '2026-03-11' },
        { id: 'ORD003', customer: 'Le Van C', total: '2,100,000', status: 'Pending', date: '2026-03-10' },
        { id: 'ORD004', customer: 'Pham Thi D', total: '550,000', status: 'Shipped', date: '2026-03-10' },
        { id: 'ORD005', customer: 'Hoang Van E', total: '3,200,000', status: 'Completed', date: '2026-03-09' },
      ]);
      setLoading(false);
    }, 500);
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Completed':
        return 'bg-green-100 text-green-800';
      case 'Processing':
        return 'bg-blue-100 text-blue-800';
      case 'Pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'Shipped':
        return 'bg-purple-100 text-purple-800';
      case 'Cancelled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-4 border-blue-600 rounded-full animate-spin border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <div
            key={stat.name}
            className="p-6 bg-white rounded-lg shadow"
          >
            <div className="flex items-center justify-between">
              <stat.icon className="w-8 h-8 text-gray-400" />
              <span
                className={clsx(
                  'flex items-center text-sm font-medium',
                  stat.change >= 0 ? 'text-green-600' : 'text-red-600'
                )}
              >
                {stat.change >= 0 ? (
                  <ArrowUpIcon className="w-4 h-4 mr-1" />
                ) : (
                  <ArrowDownIcon className="w-4 h-4 mr-1" />
                )}
                {Math.abs(stat.change)}%
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold text-gray-900">{stat.value}</p>
            <p className="text-sm text-gray-500">{stat.name}</p>
          </div>
        ))}
      </div>

      {/* Recent Orders */}
      <div className="bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">Recent Orders</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Order ID
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Customer
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Total
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Status
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Date
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {recentOrders.map((order) => (
                <tr key={order.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm font-medium text-blue-600">
                    {order.id}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {order.customer}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {order.total} VND
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={clsx(
                        'px-2 py-1 text-xs font-medium rounded-full',
                        getStatusColor(order.status)
                      )}
                    >
                      {order.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {order.date}
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
