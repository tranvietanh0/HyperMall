import { useState, useEffect } from 'react';
import {
  MagnifyingGlassIcon,
  CheckIcon,
  XMarkIcon,
  EyeIcon,
} from '@heroicons/react/24/outline';
import clsx from 'clsx';

interface Seller {
  id: number;
  shopName: string;
  ownerName: string;
  email: string;
  phone: string;
  businessType: string;
  status: string;
  rating: number;
  totalProducts: number;
  totalFollowers: number;
  createdAt: string;
}

export default function AdminSellers() {
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    setTimeout(() => {
      setSellers([
        { id: 1, shopName: 'Apple Store Vietnam', ownerName: 'Nguyen Van A', email: 'apple@example.com', phone: '0901234567', businessType: 'COMPANY', status: 'ACTIVE', rating: 4.9, totalProducts: 150, totalFollowers: 25000, createdAt: '2026-01-15' },
        { id: 2, shopName: 'Samsung Official', ownerName: 'Tran Thi B', email: 'samsung@example.com', phone: '0902345678', businessType: 'COMPANY', status: 'ACTIVE', rating: 4.7, totalProducts: 200, totalFollowers: 18000, createdAt: '2026-01-20' },
        { id: 3, shopName: 'Tech Shop 24h', ownerName: 'Le Van C', email: 'techshop@example.com', phone: '0903456789', businessType: 'INDIVIDUAL', status: 'PENDING', rating: 0, totalProducts: 0, totalFollowers: 0, createdAt: '2026-03-10' },
        { id: 4, shopName: 'Mobile World', ownerName: 'Pham Thi D', email: 'mobile@example.com', phone: '0904567890', businessType: 'COMPANY', status: 'SUSPENDED', rating: 3.2, totalProducts: 80, totalFollowers: 5000, createdAt: '2026-02-01' },
        { id: 5, shopName: 'Gadget Hub', ownerName: 'Hoang Van E', email: 'gadget@example.com', phone: '0905678901', businessType: 'INDIVIDUAL', status: 'PENDING', rating: 0, totalProducts: 0, totalFollowers: 0, createdAt: '2026-03-11' },
      ]);
      setLoading(false);
    }, 500);
  }, []);

  const getStatusColor = (status: string) => {
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
  };

  const filteredSellers = sellers.filter((seller) => {
    const matchesSearch =
      seller.shopName.toLowerCase().includes(search.toLowerCase()) ||
      seller.ownerName.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'all' || seller.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleApprove = (id: number) => {
    setSellers(sellers.map(s => s.id === id ? { ...s, status: 'ACTIVE' } : s));
  };

  const handleReject = (id: number) => {
    setSellers(sellers.map(s => s.id === id ? { ...s, status: 'SUSPENDED' } : s));
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
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Seller Management</h1>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="p-4 bg-white rounded-lg shadow">
          <p className="text-sm text-gray-500">Total Sellers</p>
          <p className="text-2xl font-bold text-gray-900">{sellers.length}</p>
        </div>
        <div className="p-4 bg-white rounded-lg shadow">
          <p className="text-sm text-gray-500">Active Sellers</p>
          <p className="text-2xl font-bold text-green-600">
            {sellers.filter(s => s.status === 'ACTIVE').length}
          </p>
        </div>
        <div className="p-4 bg-white rounded-lg shadow">
          <p className="text-sm text-gray-500">Pending Approval</p>
          <p className="text-2xl font-bold text-yellow-600">
            {sellers.filter(s => s.status === 'PENDING').length}
          </p>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-4 sm:flex-row">
        <div className="relative flex-1">
          <MagnifyingGlassIcon className="absolute w-5 h-5 text-gray-400 transform -translate-y-1/2 left-3 top-1/2" />
          <input
            type="text"
            placeholder="Search sellers..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full py-2 pl-10 pr-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        >
          <option value="all">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="PENDING">Pending</option>
          <option value="SUSPENDED">Suspended</option>
        </select>
      </div>

      {/* Sellers Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Shop
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Owner
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Type
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Rating
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Products
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Status
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredSellers.map((seller) => (
                <tr key={seller.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div>
                      <p className="font-medium text-gray-900">{seller.shopName}</p>
                      <p className="text-sm text-gray-500">{seller.totalFollowers.toLocaleString()} followers</p>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div>
                      <p className="text-sm text-gray-900">{seller.ownerName}</p>
                      <p className="text-sm text-gray-500">{seller.email}</p>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {seller.businessType}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {seller.rating > 0 ? `${seller.rating}/5` : '-'}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {seller.totalProducts}
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={clsx(
                        'px-2 py-1 text-xs font-medium rounded-full',
                        getStatusColor(seller.status)
                      )}
                    >
                      {seller.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center space-x-2">
                      {seller.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => handleApprove(seller.id)}
                            className="p-1 text-gray-400 hover:text-green-600"
                            title="Approve"
                          >
                            <CheckIcon className="w-5 h-5" />
                          </button>
                          <button
                            onClick={() => handleReject(seller.id)}
                            className="p-1 text-gray-400 hover:text-red-600"
                            title="Reject"
                          >
                            <XMarkIcon className="w-5 h-5" />
                          </button>
                        </>
                      )}
                      <button className="p-1 text-gray-400 hover:text-blue-600">
                        <EyeIcon className="w-5 h-5" />
                      </button>
                    </div>
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
