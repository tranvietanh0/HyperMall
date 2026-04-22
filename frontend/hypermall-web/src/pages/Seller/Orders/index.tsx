import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Button from '@/components/common/Button';
import Loading from '@/components/common/Loading';
import { sellerOrderService } from '@/services';
import type { OrderStatus, OrderSummary } from '@/types';
import { SELLER_READONLY_ORDER_STATUSES } from '@/types';
import { formatCurrency, formatDateTime, getErrorMessage } from '@/utils';

const statusOptions: OrderStatus[] = [
  'PENDING_PAYMENT',
  'PAID',
  'CONFIRMED',
  'PROCESSING',
  'SHIPPING',
  'DELIVERED',
  'COMPLETED',
  'CANCELLED',
  'RETURNED',
];

export default function SellerOrdersPage() {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<OrderStatus | ''>('');
  const [updatingId, setUpdatingId] = useState<number | null>(null);

  const loadOrders = async (status?: OrderStatus) => {
    setLoading(true);
    setError('');
    try {
      const response = await sellerOrderService.getSellerOrders(0, 50, status);
      setOrders(response.content);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to load seller orders'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOrders(selectedStatus || undefined);
  }, [selectedStatus]);

  const handleStatusUpdate = async (orderId: number, status: OrderStatus) => {
    setUpdatingId(orderId);
    try {
      const updatedOrder = await sellerOrderService.updateSellerOrderStatus(orderId, status);
      setOrders((current) =>
        current.map((order) => (order.id === orderId ? { ...order, status: updatedOrder.status, updatedAt: updatedOrder.updatedAt } : order))
      );
      toast.success('Order status updated successfully');
    } catch (error: unknown) {
      toast.error(getErrorMessage(error, 'Unable to update order status'));
    } finally {
      setUpdatingId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center">
        <Loading size="lg" text="Loading orders..." />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 rounded-3xl bg-white p-6 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Orders</h1>
          <p className="mt-1 text-sm text-gray-500">Review incoming orders and keep fulfillment moving.</p>
        </div>
        <div>
          <select
            value={selectedStatus}
            onChange={(event) => setSelectedStatus((event.target.value as OrderStatus) || '')}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">All statuses</option>
            {statusOptions.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error ? <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-red-700">{error}</div> : null}

      <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Order</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Payment</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Items</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Total</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Created</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 bg-white">
              {orders.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-sm text-gray-500">
                    No orders found.
                  </td>
                </tr>
              ) : (
                orders.map((order) => {
                  const readonly = SELLER_READONLY_ORDER_STATUSES.includes(order.status);
                  return (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4">
                        <p className="font-medium text-gray-900">#{order.orderNumber}</p>
                        <p className="text-sm text-gray-500">Seller order #{order.id}</p>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        <p>{order.paymentMethod}</p>
                        <p className="text-xs text-gray-500">{order.paymentStatus}</p>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">{order.totalItems}</td>
                      <td className="px-6 py-4 text-sm font-medium text-primary-600">{formatCurrency(order.total)}</td>
                      <td className="px-6 py-4 text-sm text-gray-500">{formatDateTime(order.createdAt)}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{order.status}</td>
                      <td className="px-6 py-4">
                        {readonly ? (
                          <span className="text-sm text-gray-400">Locked</span>
                        ) : (
                          <div className="flex items-center gap-2">
                            <select
                              value={order.status}
                              onChange={(event) => void handleStatusUpdate(order.id, event.target.value as OrderStatus)}
                              disabled={updatingId === order.id}
                              className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
                            >
                              {statusOptions.map((status) => (
                                <option key={status} value={status}>
                                  {status}
                                </option>
                              ))}
                            </select>
                            {updatingId === order.id ? <Loading size="sm" /> : <Button size="sm">Update</Button>}
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
