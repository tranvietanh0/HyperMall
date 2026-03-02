import { Link } from 'react-router-dom';
import Button from '@/components/common/Button';

export default function CartPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Giỏ hàng</h1>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <p className="text-gray-500 text-center py-8">Giỏ hàng của bạn đang trống</p>
          </div>
        </div>
        <div>
          <div className="bg-white rounded-lg shadow-sm p-6 sticky top-24">
            <h2 className="font-semibold mb-4">Tổng đơn hàng</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span>Tạm tính</span>
                <span>₫0</span>
              </div>
              <div className="flex justify-between">
                <span>Phí vận chuyển</span>
                <span>-</span>
              </div>
              <hr />
              <div className="flex justify-between font-semibold text-lg">
                <span>Tổng cộng</span>
                <span className="text-primary-600">₫0</span>
              </div>
            </div>
            <Link to="/checkout">
              <Button fullWidth className="mt-4">
                Tiến hành thanh toán
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
