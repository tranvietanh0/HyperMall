export default function ProductDetailPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="aspect-square bg-gray-100 rounded-lg"></div>
          <div>
            <h1 className="text-2xl font-bold mb-4">Tên sản phẩm</h1>
            <p className="text-3xl text-primary-600 font-bold mb-4">₫150.000</p>
            <p className="text-gray-600 mb-6">Mô tả sản phẩm sẽ hiển thị ở đây...</p>
            <button className="w-full bg-primary-600 text-white py-3 rounded-lg font-semibold hover:bg-primary-700">
              Thêm vào giỏ hàng
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
