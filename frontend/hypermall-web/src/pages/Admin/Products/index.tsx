import { useState, useEffect } from 'react';
import {
  MagnifyingGlassIcon,
  PencilIcon,
  TrashIcon,
  EyeIcon,
  CheckIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';
import clsx from 'clsx';

interface Product {
  id: number;
  name: string;
  thumbnail: string;
  category: string;
  seller: string;
  price: number;
  stock: number;
  status: string;
  createdAt: string;
}

export default function AdminProducts() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    setTimeout(() => {
      setProducts([
        { id: 1, name: 'iPhone 15 Pro Max', thumbnail: '/placeholder.jpg', category: 'Dien thoai', seller: 'Apple Store', price: 34990000, stock: 50, status: 'ACTIVE', createdAt: '2026-03-01' },
        { id: 2, name: 'MacBook Pro 14"', thumbnail: '/placeholder.jpg', category: 'Laptop', seller: 'Apple Store', price: 49990000, stock: 20, status: 'ACTIVE', createdAt: '2026-03-02' },
        { id: 3, name: 'Samsung Galaxy S24', thumbnail: '/placeholder.jpg', category: 'Dien thoai', seller: 'Samsung Vietnam', price: 25990000, stock: 0, status: 'INACTIVE', createdAt: '2026-03-03' },
        { id: 4, name: 'AirPods Pro 2', thumbnail: '/placeholder.jpg', category: 'Phu kien', seller: 'Apple Store', price: 6990000, stock: 100, status: 'PENDING', createdAt: '2026-03-04' },
        { id: 5, name: 'Dell XPS 15', thumbnail: '/placeholder.jpg', category: 'Laptop', seller: 'Dell Official', price: 42990000, stock: 15, status: 'ACTIVE', createdAt: '2026-03-05' },
      ]);
      setLoading(false);
    }, 500);
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'INACTIVE':
        return 'bg-gray-100 text-gray-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'DRAFT':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN').format(price) + ' VND';
  };

  const filteredProducts = products.filter((product) => {
    const matchesSearch = product.name.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'all' || product.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleApprove = (id: number) => {
    setProducts(products.map(p => p.id === id ? { ...p, status: 'ACTIVE' } : p));
  };

  const handleReject = (id: number) => {
    setProducts(products.map(p => p.id === id ? { ...p, status: 'INACTIVE' } : p));
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
        <h1 className="text-2xl font-bold text-gray-900">Product Management</h1>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-4 sm:flex-row">
        <div className="relative flex-1">
          <MagnifyingGlassIcon className="absolute w-5 h-5 text-gray-400 transform -translate-y-1/2 left-3 top-1/2" />
          <input
            type="text"
            placeholder="Search products..."
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
          <option value="INACTIVE">Inactive</option>
          <option value="PENDING">Pending</option>
          <option value="DRAFT">Draft</option>
        </select>
      </div>

      {/* Products Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Product
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Category
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Seller
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Price
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Stock
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
              {filteredProducts.map((product) => (
                <tr key={product.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center">
                      <div className="w-10 h-10 bg-gray-200 rounded-lg" />
                      <div className="ml-3">
                        <p className="font-medium text-gray-900">{product.name}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {product.category}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {product.seller}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {formatPrice(product.price)}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    <span className={product.stock === 0 ? 'text-red-600' : ''}>
                      {product.stock}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={clsx(
                        'px-2 py-1 text-xs font-medium rounded-full',
                        getStatusColor(product.status)
                      )}
                    >
                      {product.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center space-x-2">
                      {product.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => handleApprove(product.id)}
                            className="p-1 text-gray-400 hover:text-green-600"
                            title="Approve"
                          >
                            <CheckIcon className="w-5 h-5" />
                          </button>
                          <button
                            onClick={() => handleReject(product.id)}
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
                      <button className="p-1 text-gray-400 hover:text-yellow-600">
                        <PencilIcon className="w-5 h-5" />
                      </button>
                      <button className="p-1 text-gray-400 hover:text-red-600">
                        <TrashIcon className="w-5 h-5" />
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
