export default function ProductListPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Sản phẩm</h1>
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((item) => (
          <div key={item} className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="aspect-square bg-gray-100"></div>
            <div className="p-3">
              <h3 className="text-sm text-gray-700 line-clamp-2 mb-2">Sản phẩm mẫu {item}</h3>
              <p className="text-primary-600 font-bold">₫150.000</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
