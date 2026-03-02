import { Link } from 'react-router-dom';
import Button from '@/components/common/Button';

export default function NotFoundPage() {
  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-9xl font-bold text-gray-200">404</h1>
        <h2 className="text-2xl font-semibold text-gray-800 mb-4">Trang không tìm thấy</h2>
        <p className="text-gray-500 mb-8">
          Xin lỗi, trang bạn đang tìm kiếm không tồn tại.
        </p>
        <Link to="/">
          <Button>Về trang chủ</Button>
        </Link>
      </div>
    </div>
  );
}
