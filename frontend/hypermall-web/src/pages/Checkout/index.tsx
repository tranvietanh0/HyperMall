export default function CheckoutPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Thanh toán</h1>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="font-semibold mb-4">Địa chỉ giao hàng</h2>
            <p className="text-gray-500">Chọn hoặc thêm địa chỉ giao hàng</p>
          </div>
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="font-semibold mb-4">Phương thức thanh toán</h2>
            <p className="text-gray-500">Chọn phương thức thanh toán</p>
          </div>
        </div>
        <div>
          <div className="bg-white rounded-lg shadow-sm p-6 sticky top-24">
            <h2 className="font-semibold mb-4">Đơn hàng</h2>
            <p className="text-gray-500">Chi tiết đơn hàng sẽ hiển thị ở đây</p>
          </div>
        </div>
      </div>
    </div>
  );
}
