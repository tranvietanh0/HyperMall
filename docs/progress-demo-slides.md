# Slide Báo cáo Tiến độ — HyperMall
**Mục đích:** Giới thiệu tổng quan + demo tiến độ hiện tại
**Số slides:** ~12 slides | **Thời gian:** 10–15 phút

> Lưu ý: Không cần backend chạy. Screenshot lấy từ IDE + frontend tĩnh.

---

## Slide 1 — Trang bìa

**HyperMall**
*Nền tảng thương mại điện tử đa dịch vụ*

- Môn học / Học kỳ
- Tên nhóm + MSSV
- Ngày báo cáo

---

## Slide 2 — Giới thiệu bài toán

**Bài toán:** Xây dựng marketplace thương mại điện tử (tương tự Shopee/Lazada)

**Yêu cầu chính:**
- Nhiều người bán trên cùng một nền tảng
- Người mua có thể tìm kiếm, đặt hàng, thanh toán online
- Hỗ trợ nhiều phương thức thanh toán & vận chuyển
- Hệ thống có khả năng mở rộng khi lượng người dùng tăng

---

## Slide 3 — Công nghệ sử dụng

**Tổng quan tech stack:**

| Thành phần | Công nghệ |
|------------|-----------|
| Frontend | React + TypeScript |
| Backend | Spring Boot (Java) — kiến trúc Microservices |
| Cơ sở dữ liệu | MySQL, Redis, Elasticsearch |
| Hạ tầng | Docker, RabbitMQ |

> Kiến trúc Microservices cho phép tách riêng từng chức năng thành các service độc lập, dễ phát triển và mở rộng.

📸 **Screenshot:** VS Code — thư mục gốc thấy 3 folder: `backend/`, `frontend/`, `infrastructure/`

---

## Slide 4 — Kiến trúc hệ thống (tổng quan)

**Sơ đồ đơn giản:**

```
[Trình duyệt]
      ↓
[API Gateway]  ←  Điểm vào duy nhất, xác thực JWT
      ↓
┌──────────────────────────────────┐
│  user    product   cart   order  │
│  payment  inventory  search ... │
│      (18 microservices)          │
└──────────────────────────────────┘
      ↓
[MySQL · Redis · Elasticsearch]
```

**Đặc điểm:**
- Mỗi service phụ trách 1 chức năng nghiệp vụ
- Giao tiếp qua REST API và Message Queue
- Database riêng cho từng service

---

## Slide 5 — Các chức năng chính (Người mua)

| # | Chức năng | Mô tả |
|---|-----------|-------|
| 1 | Đăng ký / Đăng nhập | Tạo tài khoản, đăng nhập bằng email + mật khẩu |
| 2 | Tìm kiếm sản phẩm | Tìm theo tên, lọc theo danh mục / giá / đánh giá |
| 3 | Xem chi tiết sản phẩm | Hình ảnh, mô tả, giá, biến thể, đánh giá |
| 4 | Giỏ hàng | Thêm / sửa / xóa sản phẩm trong giỏ |
| 5 | Đặt hàng & Thanh toán | Chọn địa chỉ, vận chuyển, thanh toán (VNPay, MoMo, ZaloPay, COD) |
| 6 | Theo dõi đơn hàng | Xem trạng thái đơn hàng, lịch sử mua |
| 7 | Đánh giá sản phẩm | Viết review + cho điểm sao |

---

## Slide 6 — Các chức năng chính (Người bán & Admin)

**Người bán (Seller):**
- Đăng ký trở thành seller
- Quản lý sản phẩm (thêm / sửa / xóa)
- Xử lý đơn hàng
- Xem thống kê doanh thu

**Quản trị viên (Admin):**
- Quản lý danh mục sản phẩm
- Quản lý người dùng & seller
- Xem analytics tổng thể
- Cấu hình hệ thống

---

## Slide 7 — Tiến độ Backend

**18 services đã có code:**

| Nhóm | Services | Trạng thái |
|------|----------|-----------|
| Hạ tầng | Service Registry, Config Server, API Gateway | ✅ Hoàn chỉnh |
| Người dùng | User Service (đăng ký, đăng nhập, JWT) | ✅ Hoàn chỉnh |
| Mua sắm | Product, Cart, Order, Payment, Inventory | ✅ Hoàn chỉnh |
| Mở rộng | Shipping, Promotion, Review, Search | ✅ Hoàn chỉnh |
| Hỗ trợ | Notification, Media, Seller, Analytics | ✅ Có cấu trúc |

📸 **Screenshot:** VS Code — expand `backend/` thấy đủ 18 thư mục service

---

## Slide 8 — Tiến độ Frontend

**20 trang giao diện + đầy đủ API layer:**

| Nhóm trang | Các trang |
|------------|-----------|
| Xác thực | Đăng nhập, Đăng ký |
| Mua sắm | Trang chủ, Danh sách SP, Chi tiết SP |
| Đơn hàng | Giỏ hàng, Checkout, Lịch sử đơn, Chi tiết đơn |
| Cá nhân | Trang hồ sơ |
| Admin | Dashboard, Quản lý SP, Đơn hàng, Users, Danh mục, Sellers, Analytics, Settings |

📸 **Screenshot:** VS Code — expand `src/pages/` thấy đầy đủ files

---

## Slide 9 — Demo giao diện (chạy `npm run dev`)

> Frontend chạy độc lập, không cần backend.

📸 **Chụp 3 màn hình:**

**[1] Trang chủ** `http://localhost:3000`
- Header: logo, thanh tìm kiếm, giỏ hàng

**[2] Trang đăng nhập** `http://localhost:3000/login`
- Form email + mật khẩu

**[3] Trang sản phẩm** `http://localhost:3000/products`
- Bộ lọc bên trái + lưới sản phẩm bên phải

---

## Slide 10 — Demo giao diện (tiếp)

📸 **Chụp thêm 3 màn hình:**

**[4] Giỏ hàng** `http://localhost:3000/cart`
- Giao diện giỏ hàng

**[5] Checkout** `http://localhost:3000/checkout`
- Form địa chỉ, chọn vận chuyển, chọn thanh toán (VNPay/MoMo/ZaloPay/COD)

**[6] Lịch sử đơn hàng** `http://localhost:3000/orders`
- Danh sách đơn, filter theo trạng thái

---

## Slide 11 — Tổng kết tiến độ

| Hạng mục | Tình trạng |
|----------|-----------|
| Backend — 18 microservices | ✅ Hoàn thành |
| Frontend — 20 trang giao diện | ✅ Hoàn thành |
| API — REST endpoints đầy đủ | ✅ Hoàn thành |
| Database — MySQL + Redis + Elasticsearch | ✅ Hoàn thành |
| Xác thực — JWT + phân quyền (User/Seller/Admin) | ✅ Hoàn thành |
| Docker — môi trường dev | ✅ Hoàn thành |
| Tích hợp thanh toán & vận chuyển | ✅ Hoàn thành (code) |
| Chạy end-to-end + test tích hợp | ⏳ Đang triển khai |

**Việc tiếp theo:**
1. Chạy thật end-to-end (backend + frontend + infra)
2. Seed dữ liệu test mẫu
3. Test luồng mua hàng hoàn chỉnh
4. Fix bugs phát sinh

---

## Slide 12 — Q&A

**HyperMall — Nền tảng thương mại điện tử**

Cảm ơn! Câu hỏi?

---

## Checklist Screenshot (không cần backend)

| # | Lấy ở đâu | Cần thấy gì |
|---|-----------|------------|
| 1 | VS Code — thư mục gốc | 3 folder: backend/, frontend/, infrastructure/ |
| 2 | VS Code — `backend/` | 18 thư mục service |
| 3 | VS Code — `frontend/src/pages/` | Đầy đủ file tsx |
| 4 | `http://localhost:3000` | Trang chủ |
| 5 | `http://localhost:3000/login` | Form đăng nhập |
| 6 | `http://localhost:3000/products` | Layout danh sách sản phẩm |
| 7 | `http://localhost:3000/checkout` | Form checkout + logo thanh toán |
| 8 | `http://localhost:3000/orders` | Layout lịch sử đơn |
