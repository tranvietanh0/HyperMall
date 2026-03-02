export default function ProfilePage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Tài khoản của tôi</h1>
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="font-semibold mb-4">Menu</h2>
          <ul className="space-y-2 text-sm">
            <li><a href="#" className="text-primary-600">Thông tin tài khoản</a></li>
            <li><a href="#" className="text-gray-600 hover:text-primary-600">Địa chỉ</a></li>
            <li><a href="#" className="text-gray-600 hover:text-primary-600">Đổi mật khẩu</a></li>
            <li><a href="#" className="text-gray-600 hover:text-primary-600">Đơn hàng</a></li>
          </ul>
        </div>
        <div className="md:col-span-3 bg-white rounded-lg shadow-sm p-6">
          <h2 className="font-semibold mb-4">Thông tin tài khoản</h2>
          <p className="text-gray-500">Form cập nhật thông tin sẽ hiển thị ở đây</p>
        </div>
      </div>
    </div>
  );
}
