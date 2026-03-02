import { Link } from 'react-router-dom';

export default function HomePage() {
  // Placeholder categories
  const categories = [
    { id: 1, name: 'Thời Trang Nam', image: '/assets/category-men.jpg' },
    { id: 2, name: 'Thời Trang Nữ', image: '/assets/category-women.jpg' },
    { id: 3, name: 'Điện Thoại & Phụ Kiện', image: '/assets/category-phone.jpg' },
    { id: 4, name: 'Máy Tính & Laptop', image: '/assets/category-laptop.jpg' },
    { id: 5, name: 'Đồ Gia Dụng', image: '/assets/category-home.jpg' },
    { id: 6, name: 'Sắc Đẹp', image: '/assets/category-beauty.jpg' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Banner */}
      <section className="bg-primary-600 text-white py-12">
        <div className="container mx-auto px-4">
          <div className="max-w-2xl">
            <h1 className="text-4xl font-bold mb-4">
              Chào mừng đến với HyperMall
            </h1>
            <p className="text-xl mb-6 text-primary-100">
              Sàn thương mại điện tử hàng đầu với hàng triệu sản phẩm chất lượng
            </p>
            <Link
              to="/products"
              className="inline-block bg-white text-primary-600 px-6 py-3 rounded-lg font-semibold hover:bg-primary-50 transition"
            >
              Mua sắm ngay
            </Link>
          </div>
        </div>
      </section>

      {/* Categories */}
      <section className="py-12">
        <div className="container mx-auto px-4">
          <h2 className="text-2xl font-bold mb-6">Danh mục</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {categories.map((category) => (
              <Link
                key={category.id}
                to={`/category/${category.id}`}
                className="bg-white rounded-lg p-4 text-center hover:shadow-lg transition group"
              >
                <div className="w-16 h-16 mx-auto mb-3 bg-gray-100 rounded-full flex items-center justify-center group-hover:bg-primary-100">
                  <span className="text-2xl">🛍️</span>
                </div>
                <h3 className="text-sm font-medium text-gray-700 group-hover:text-primary-600">
                  {category.name}
                </h3>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Flash Sale */}
      <section className="py-12 bg-primary-50">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-4">
              <h2 className="text-2xl font-bold text-primary-600">⚡ Flash Sale</h2>
              <div className="flex items-center gap-2">
                <span className="bg-primary-600 text-white px-2 py-1 rounded text-sm">00</span>
                <span>:</span>
                <span className="bg-primary-600 text-white px-2 py-1 rounded text-sm">00</span>
                <span>:</span>
                <span className="bg-primary-600 text-white px-2 py-1 rounded text-sm">00</span>
              </div>
            </div>
            <Link to="/flash-sale" className="text-primary-600 hover:underline">
              Xem tất cả &gt;
            </Link>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {[1, 2, 3, 4, 5, 6].map((item) => (
              <div key={item} className="bg-white rounded-lg shadow-sm overflow-hidden group">
                <div className="aspect-square bg-gray-100 relative">
                  <span className="absolute top-2 left-2 bg-primary-600 text-white text-xs px-2 py-1 rounded">
                    -50%
                  </span>
                </div>
                <div className="p-3">
                  <p className="text-primary-600 font-bold">₫100.000</p>
                  <p className="text-gray-400 text-sm line-through">₫200.000</p>
                  <div className="mt-2 bg-primary-100 rounded-full h-2 overflow-hidden">
                    <div className="bg-primary-600 h-full w-3/4"></div>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Đã bán 75%</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="py-12">
        <div className="container mx-auto px-4">
          <h2 className="text-2xl font-bold mb-6">Sản phẩm nổi bật</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((item) => (
              <Link
                key={item}
                to={`/products/${item}`}
                className="bg-white rounded-lg shadow-sm overflow-hidden group hover:shadow-lg transition"
              >
                <div className="aspect-square bg-gray-100"></div>
                <div className="p-3">
                  <h3 className="text-sm text-gray-700 line-clamp-2 mb-2 group-hover:text-primary-600">
                    Sản phẩm mẫu {item}
                  </h3>
                  <div className="flex items-center justify-between">
                    <p className="text-primary-600 font-bold">₫150.000</p>
                    <p className="text-xs text-gray-400">Đã bán 1.2k</p>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
