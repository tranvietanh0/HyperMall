import { useEffect, useState } from 'react';
import {
  MagnifyingGlassIcon,
  FunnelIcon,
  ChevronRightIcon,
  PrinterIcon,
  TruckIcon,
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  CreditCardIcon,
  UserIcon,
  ClipboardDocumentListIcon,
  EyeIcon,
  ArrowPathIcon,
} from '@heroicons/react/24/outline';
import Loading from '@/components/common/Loading';
import { sellerOrderService } from '@/services';
import type { OrderSummary } from '@/types';
import { formatCurrency, formatDate, getErrorMessage } from '@/utils';
import clsx from 'clsx';
import toast from 'react-hot-toast';

const ORDER_TABS = [
  { id: 'ALL', label: 'All Orders' },
  { id: 'PENDING', label: 'Pending' },
  { id: 'PAID', label: 'Processing' },
  { id: 'SHIPPING', label: 'Shipping' },
  { id: 'COMPLETED', label: 'Completed' },
  { id: 'CANCELLED', label: 'Cancelled' },
];

export default function SellerOrdersPage() {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const loadOrders = async () => {
    setLoading(true);
    try {
      const data = await sellerOrderService.getSellerOrders(0, 10);
      setOrders(data.content);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Unable to load orders'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOrders();
  }, []);

  const getStatusConfig = (status: string) => {
    switch (status) {
      case 'PAID':
      case 'CONFIRMED':
        return { color: 'bg-blue-50 text-blue-600 ring-blue-500/20', icon: ClockIcon, label: 'Processing' };
      case 'SHIPPING':
        return { color: 'bg-indigo-50 text-indigo-600 ring-indigo-500/20', icon: TruckIcon, label: 'Shipping' };
      case 'COMPLETED':
        return { color: 'bg-emerald-50 text-emerald-600 ring-emerald-500/20', icon: CheckCircleIcon, label: 'Completed' };
      case 'CANCELLED':
        return { color: 'bg-rose-50 text-rose-600 ring-rose-500/20', icon: XCircleIcon, label: 'Cancelled' };
      default:
        return { color: 'bg-gray-50 text-gray-500 ring-gray-400/20', icon: CreditCardIcon, label: status };
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-[400px] flex-col items-center justify-center">
        <Loading size="lg" />
        <p className="mt-4 text-sm font-medium text-gray-500">Syncing orders...</p>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-700 pb-10">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight text-gray-900">Order Management</h1>
        <p className="text-gray-500">Track and process your store's customer orders.</p>
      </div>

      {/* Tabs Navigation */}
      <div className="flex items-center gap-1 overflow-x-auto rounded-[2rem] bg-gray-100 p-1.5 no-scrollbar">
        {ORDER_TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={clsx(
              'flex-1 whitespace-nowrap rounded-2xl px-6 py-2.5 text-sm font-bold transition-all',
              activeTab === tab.id 
                ? 'bg-white text-slate-900 shadow-sm' 
                : 'text-gray-500 hover:text-gray-700'
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Filters Bar */}
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center">
        <div className="relative flex-1">
          <MagnifyingGlassIcon className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search by order ID or customer..."
            className="w-full rounded-2xl border-none bg-white py-3.5 pl-12 pr-4 text-sm font-medium shadow-sm ring-1 ring-gray-200 focus:ring-2 focus:ring-cyan-500/20"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-3">
          <button className="flex items-center gap-2 rounded-2xl bg-white px-5 py-3.5 text-sm font-bold text-gray-700 shadow-sm ring-1 ring-gray-200 hover:bg-gray-50">
            <FunnelIcon className="h-5 w-5 text-gray-400" />
            More Filters
          </button>
          <button className="flex items-center gap-2 rounded-2xl bg-slate-900 px-5 py-3.5 text-sm font-bold text-white shadow-lg shadow-slate-900/20 hover:bg-slate-800">
            <PrinterIcon className="h-5 w-5" />
            Bulk Print
          </button>
        </div>
      </div>

      {/* Orders List */}
      <div className="space-y-4">
        {orders.length === 0 ? (
          <div className="rounded-[2.5rem] border-2 border-dashed border-gray-200 bg-white py-20 text-center">
            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-gray-50">
              <ClipboardDocumentListIcon className="h-8 w-8 text-gray-300" />
            </div>
            <p className="mt-4 text-lg font-bold text-gray-900">No orders yet</p>
            <p className="text-sm text-gray-500">When customers buy your products, they will appear here.</p>
          </div>
        ) : (
          orders.map((order) => {
            const status = getStatusConfig(order.status);
            return (
              <div 
                key={order.id} 
                className="group relative overflow-hidden rounded-[2rem] border border-gray-100 bg-white p-6 shadow-sm transition-all hover:border-cyan-500/30 hover:shadow-xl hover:shadow-cyan-500/5"
              >
                <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
                  <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
                    <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gray-50 text-cyan-600">
                      <CreditCardIcon className="h-7 w-7" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-lg font-black text-gray-900">#{order.orderNumber}</span>
                        <span className={clsx(
                          'inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-[10px] font-black uppercase tracking-wider ring-1 ring-inset',
                          status.color
                        )}>
                          <status.icon className="h-3 w-3" />
                          {status.label}
                        </span>
                      </div>
                      <div className="mt-1 flex items-center gap-4 text-sm text-gray-500">
                        <span className="flex items-center gap-1.5 font-medium">
                          <UserIcon className="h-4 w-4" />
                          Customer #{order.userId}
                        </span>
                        <span className="h-1 w-1 rounded-full bg-gray-300"></span>
                        <span className="font-medium">{formatDate(order.createdAt)}</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between gap-8 border-t border-gray-50 pt-4 lg:border-none lg:pt-0">
                    <div className="text-right">
                      <p className="text-xs font-bold uppercase tracking-widest text-gray-400">Total Amount</p>
                      <p className="text-xl font-black text-cyan-600">{formatCurrency(order.total)}</p>
                      <p className="text-xs font-medium text-gray-500">{order.totalItems} items</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <button className="rounded-xl bg-gray-50 p-3 text-gray-600 hover:bg-gray-100 transition-colors">
                        <EyeIcon className="h-5 w-5" />
                      </button>
                      <button className="flex items-center gap-2 rounded-xl bg-cyan-50 px-4 py-2.5 text-sm font-bold text-cyan-700 hover:bg-cyan-100 transition-colors">
                        Process Order
                        <ChevronRightIcon className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                </div>
                
                {/* Visual Progress Bar (Mock) */}
                <div className="absolute bottom-0 left-0 h-1 w-full bg-gray-50">
                  <div 
                    className={clsx(
                      'h-full transition-all duration-1000',
                      order.status === 'COMPLETED' ? 'w-full bg-emerald-500' : 
                       order.status === 'SHIPPING' ? 'w-2/3 bg-indigo-500' : 

                      order.status === 'CANCELLED' ? 'w-full bg-rose-500' : 'w-1/3 bg-blue-500'
                    )} 
                  />
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Load More (Mock) */}
      <div className="flex justify-center">
        <button className="group flex items-center gap-2 rounded-2xl border border-gray-200 bg-white px-8 py-3 text-sm font-bold text-gray-600 hover:border-cyan-500 hover:text-cyan-600 transition-all">
          <ArrowPathIcon className="h-5 w-5 group-hover:rotate-180 transition-transform duration-500" />
          Load More Orders
        </button>
      </div>
    </div>
  );
}
