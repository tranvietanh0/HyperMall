import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-gray-100 border-t">
      <div className="container mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Customer Service */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-4">Chăm sóc khách hàng</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li><Link to="/help" className="hover:text-primary-600">Trung tâm trợ giúp</Link></li>
              <li><Link to="/guide" className="hover:text-primary-600">Hướng dẫn mua hàng</Link></li>
              <li><Link to="/shipping" className="hover:text-primary-600">Vận chuyển</Link></li>
              <li><Link to="/returns" className="hover:text-primary-600">Trả hàng & Hoàn tiền</Link></li>
              <li><Link to="/contact" className="hover:text-primary-600">Liên hệ</Link></li>
            </ul>
          </div>

          {/* About HyperMall */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-4">Về HyperMall</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li><Link to="/about" className="hover:text-primary-600">Giới thiệu</Link></li>
              <li><Link to="/careers" className="hover:text-primary-600">Tuyển dụng</Link></li>
              <li><Link to="/terms" className="hover:text-primary-600">Điều khoản</Link></li>
              <li><Link to="/privacy" className="hover:text-primary-600">Chính sách bảo mật</Link></li>
              <li><Link to="/seller/register" className="hover:text-primary-600">Bán hàng cùng HyperMall</Link></li>
            </ul>
          </div>

          {/* Payment */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-4">Thanh toán</h3>
            <div className="flex flex-wrap gap-2">
              <div className="bg-white border rounded px-3 py-1 text-sm">VNPay</div>
              <div className="bg-white border rounded px-3 py-1 text-sm">MoMo</div>
              <div className="bg-white border rounded px-3 py-1 text-sm">ZaloPay</div>
              <div className="bg-white border rounded px-3 py-1 text-sm">COD</div>
            </div>

            <h3 className="font-semibold text-gray-900 mb-4 mt-6">Đơn vị vận chuyển</h3>
            <div className="flex flex-wrap gap-2">
              <div className="bg-white border rounded px-3 py-1 text-sm">GHN</div>
              <div className="bg-white border rounded px-3 py-1 text-sm">GHTK</div>
              <div className="bg-white border rounded px-3 py-1 text-sm">ViettelPost</div>
            </div>
          </div>

          {/* Social & App */}
          <div>
            <h3 className="font-semibold text-gray-900 mb-4">Kết nối với chúng tôi</h3>
            <div className="flex gap-3 mb-6">
              <a href="#" className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white">
                f
              </a>
              <a href="#" className="w-10 h-10 bg-pink-600 rounded-full flex items-center justify-center text-white">
                IG
              </a>
              <a href="#" className="w-10 h-10 bg-red-600 rounded-full flex items-center justify-center text-white">
                YT
              </a>
            </div>

            <h3 className="font-semibold text-gray-900 mb-4">Tải ứng dụng</h3>
            <div className="flex flex-col gap-2">
              <a href="#" className="flex items-center gap-2 bg-black text-white px-3 py-2 rounded-lg text-sm">
                <span>App Store</span>
              </a>
              <a href="#" className="flex items-center gap-2 bg-black text-white px-3 py-2 rounded-lg text-sm">
                <span>Google Play</span>
              </a>
            </div>
          </div>
        </div>

        <div className="border-t mt-8 pt-8 text-center text-sm text-gray-500">
          <p>&copy; 2024 HyperMall. Tất cả quyền được bảo lưu.</p>
        </div>
      </div>
    </footer>
  );
}
