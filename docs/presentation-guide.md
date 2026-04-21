# Hướng dẫn làm Slide PPT - HyperMall

## Cấu trúc tổng quan: ~25-30 slides

---

## MODULE 1: GIỚI THIỆU (3-4 slides)

### Slide 1 — Trang bìa
- Tên project: **HyperMall — E-Commerce Platform**
- Subtitle: "Microservices-based Marketplace inspired by Shopee & Lazada"
- Tên nhóm, MSSV, ngày báo cáo

### Slide 2 — Mục lục
- 6 phần chính: Giới thiệu → Kiến trúc → Backend → Frontend → Demo → Kết luận

### Slide 3 — Bài toán & Động lực
**Nội dung:**
- Thị trường TMĐT Việt Nam đang bùng nổ (Shopee, Lazada, Tiki)
- Vấn đề: thiếu platform tích hợp đa seller + đa cổng thanh toán + đa vận chuyển
- HyperMall giải quyết bài toán này bằng kiến trúc microservices hiện đại

**Screenshot cần chụp:** Logo/giao diện Shopee & Lazada để minh hoạ bài toán so sánh

### Slide 4 — Mục tiêu & Phạm vi
**Nội dung:**
- 3 nhóm người dùng chính:

| Người dùng | Chức năng chính |
|------------|----------------|
| Shopper | Tìm kiếm, mua hàng, theo dõi đơn, đánh giá |
| Seller | Quản lý sản phẩm, đơn hàng, doanh thu |
| Admin | Quản lý danh mục, người dùng, thống kê |

- Mục tiêu: hỗ trợ 100,000 concurrent users, uptime 99.9%

---

## MODULE 2: KIẾN TRÚC HỆ THỐNG (5-6 slides)

### Slide 5 — Tổng quan kiến trúc (High-Level)
**Nội dung — sơ đồ tầng:**
```
Internet → CDN (CloudFlare) → Load Balancer (Nginx)
         → API Gateway (8080)
         → 15 Business Microservices (8081–8095)
         → Data Layer: MySQL | Redis | RabbitMQ | Elasticsearch
```

**Screenshot cần chụp:**
- `http://localhost:8761` — Eureka Dashboard
- Chụp toàn bộ trang, thấy bảng **"Instances currently registered with Eureka"** với 18 services

### Slide 6 — API Gateway
**Nội dung:**
- Single entry point cho mọi request
- Tính năng: JWT Authentication, Rate Limiting (100/500 req/min), Circuit Breaker, CORS, Request Routing
- Bảng route:

| Path Pattern | Service |
|--------------|---------|
| `/api/auth/**`, `/api/users/**` | user-service |
| `/api/products/**`, `/api/categories/**` | product-service |
| `/api/cart/**` | cart-service |
| `/api/orders/**` | order-service |
| `/api/payments/**` | payment-service |
| `/api/search/**` | search-service |

**Screenshot cần chụp:**
- `http://localhost:8080/swagger-ui.html` — Swagger UI của API Gateway
- Hoặc log terminal khi gateway nhận và routing request

### Slide 7 — Danh sách 18 Microservices
**Nội dung — chia 3 nhóm:**

**Infrastructure Services:**
| Service | Port | Mục đích |
|---------|------|---------|
| service-registry | 8761 | Netflix Eureka — Service Discovery |
| config-server | 8888 | Spring Cloud Config — Centralized Config |
| api-gateway | 8080 | Spring Cloud Gateway — Entry point |

**Core Business Services:**
| Service | Port | Database |
|---------|------|---------|
| user-service | 8081 | MySQL |
| product-service | 8082 | MySQL |
| cart-service | 8083 | Redis |
| order-service | 8084 | MySQL |
| payment-service | 8085 | MySQL |
| inventory-service | 8086 | MySQL |

**Extended Services:**
| Service | Port | Tính năng |
|---------|------|---------|
| shipping-service | 8087 | GHN, GHTK, ViettelPost |
| promotion-service | 8088 | Voucher, Flash Sale |
| review-service | 8089 | Review & Rating |
| search-service | 8090 | Elasticsearch |
| notification-service | 8091 | Email/SMS/Push |
| media-service | 8093 | File Upload |
| seller-service | 8094 | Seller Portal |
| analytics-service | 8095 | Thống kê |

**Screenshot cần chụp:**
- `http://localhost:8761` — focus vào bảng instances, thấy tất cả services đang **UP**

### Slide 8 — Technology Stack
**Nội dung:**

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + TypeScript + Vite + TailwindCSS + Redux Toolkit |
| Backend | Spring Boot 3.4.3 + Spring Cloud 2024.0.0 (Java 17) |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway + Resilience4j |
| Database | MySQL 8.0 (12 schemas) |
| Cache | Redis 7 (Cart, Session, Rate Limit) |
| Message Queue | RabbitMQ 3 (Async events) |
| Search | Elasticsearch 8.x (Full-text search) |
| Security | JWT RS256 + BCrypt + RBAC |
| DevOps | Docker + Docker Compose + GitHub Actions |

**Screenshot cần chụp:**
- `http://localhost:15672` (guest/guest) — RabbitMQ Management tab **Overview**

### Slide 9 — Data Flow: Luồng tạo đơn hàng
**Nội dung — sơ đồ sequence:**
```
Client
  → API Gateway (xác thực JWT)
  → cart-service (lấy giỏ hàng từ Redis)
  → order-service (tạo đơn, lưu MySQL)
  → inventory-service (reserve stock)
  → RabbitMQ publish: "order.created"
  → notification-service (gửi email xác nhận)
  → payment-service (tạo link thanh toán)
```

**Screenshot cần chụp:**
- `http://localhost:15672` → tab **Queues** — thấy các queue: `order.created`, `payment.done`, `stock.reserved`

### Slide 10 — Security Architecture
**Nội dung:**
- **Authentication Flow:** Login → user-service → JWT Access Token (15 phút) + Refresh Token (7 ngày, lưu Redis)
- **Authorization:** RBAC — 3 role: ROLE_USER, ROLE_SELLER, ROLE_ADMIN
- **Security Layers:**
  1. Network Level: Firewall, Load Balancer
  2. Application Level: API Gateway JWT validation
  3. Service Level: @PreAuthorize method security
  4. Data Level: BCrypt password hashing, input validation

**Screenshot cần chụp:**
- Browser DevTools → Network tab → POST `/api/auth/login` → response JSON thấy `accessToken` và `refreshToken`

---

## MODULE 3: BACKEND DEMO (5-6 slides)

### Slide 11 — Common Library (common-lib)
**Nội dung:**
- Thư viện dùng chung cho tất cả 15 business services
- **DTOs:** `ApiResponse<T>`, `PageResponse<T>`, `ErrorResponse`
- **Exceptions:** `GlobalExceptionHandler`, `ResourceNotFoundException`, `ValidationException`...
- **Security:** `JwtTokenProvider`, `JwtAuthenticationFilter`, `@CurrentUser` annotation
- **Utils:** `DateTimeUtil`, `StringUtil`, `ValidationUtil`
- **Events:** `BaseEvent`, `EventPublisher` (RabbitMQ)

**Screenshot cần chụp:**
- VS Code / IntelliJ — cấu trúc thư mục `backend/common-lib/src/main/java/` expand các package

### Slide 12 — User Service & Authentication
**Nội dung:**
- Chức năng: Register, Login, Refresh Token, Password Reset, Địa chỉ giao hàng
- Database: `hypermall_users` — tables: users, roles, user_roles, addresses
- JWT: Access Token 15 phút, Refresh Token 7 ngày

**Screenshot cần chụp:**
- `http://localhost:8081/swagger-ui.html` — Swagger UI user-service, expand section Auth
- Hoặc Postman: POST `/api/auth/login` body `{email, password}` → response 200 với token

### Slide 13 — Product Service
**Nội dung:**
- CRUD sản phẩm với variants (size, màu, v.v.)
- Category hierarchy (danh mục cha/con)
- Brand management
- Product images upload (via media-service)
- Database: `hypermall_products` — tables: products, product_variants, product_images, categories, brands

**Screenshot cần chụp:**
- `http://localhost:8082/swagger-ui.html` — Swagger UI product-service
- GET `/api/products` response JSON với pagination: `{currentPage, totalPages, totalElements, items: [...]}`

### Slide 14 — Order & Payment Flow
**Nội dung:**
- Order status lifecycle:
  ```
  PENDING → CONFIRMED → SHIPPING → DELIVERED
                    ↘ CANCELLED
  ```
- Payment methods: **VNPay, MoMo, ZaloPay, COD**
- Shipping: **GHN, GHTK, ViettelPost** — tính phí realtime

**Screenshot cần chụp:**
- `http://localhost:8085/swagger-ui.html` — Payment Service Swagger
- Postman: POST `/api/orders` body đầy đủ → response 201 `{orderId, status: "PENDING", totalAmount}`

### Slide 15 — Search Service (Elasticsearch)
**Nội dung:**
- Full-text search sản phẩm theo: name, description, category, brand
- Filters: category, brand, price range, rating
- API: `GET /api/search?q={keyword}&category={id}&minPrice={}&maxPrice={}`

**Screenshot cần chụp:**
- Browser: `http://localhost:9200/_cat/indices?v` — thấy index `products` với số documents
- Hoặc GET `http://localhost:9200/products/_search` response JSON

### Slide 16 — Database Design
**Nội dung:**
- **Database per Service pattern** — mỗi service có schema riêng
- 12 MySQL schemas + Redis (cart) + Elasticsearch (search)
- Không share database trực tiếp giữa các services

| Database | Service | Bảng chính |
|----------|---------|-----------|
| hypermall_users | user-service | users, roles, addresses |
| hypermall_products | product-service | products, product_variants, categories, brands |
| hypermall_order | order-service | orders, order_items |
| hypermall_payment | payment-service | payments, refunds |
| hypermall_inventory | inventory-service | inventory, stock_movements |
| hypermall_promotion | promotion-service | vouchers, flash_sales |
| hypermall_reviews | review-service | reviews, review_images |

**Screenshot cần chụp:**
- MySQL Workbench / DBeaver / TablePlus — mở `hypermall_products`, thấy cấu trúc bảng (columns của bảng `products`)

---

## MODULE 4: FRONTEND DEMO (5-6 slides)

### Slide 17 — Kiến trúc Frontend
**Nội dung:**
- **Stack:** React 18 + TypeScript + Vite + TailwindCSS + Redux Toolkit
- **State Management:** Redux Toolkit (auth, cart) + React Query (server state)
- **Forms:** Formik + Yup validation
- **API Layer:** Axios, proxied qua `localhost:8080/api`
- **Cấu trúc:**
  ```
  src/
  ├── components/   # Button, Input, Modal, ProductCard...
  ├── pages/        # Home, Product, Cart, Checkout, Order, Auth...
  ├── store/slices/ # authSlice, cartSlice, ...
  ├── services/     # authService, productService, orderService...
  ├── hooks/        # useAuth, useCart, usePagination...
  └── types/        # TypeScript interfaces
  ```

**Screenshot cần chụp:**
- VS Code — expand thư mục `frontend/hypermall-web/src/` thấy tất cả folders

### Slide 18 — Trang chủ (Home Page)
**Screenshot cần chụp tại `http://localhost:3000`:**
- Chụp **full page** (scroll lên đầu)
- Chú thích các vùng:
  - `[1]` Header: Logo + Search bar + Cart icon + Login/Avatar
  - `[2]` Banner/Carousel: ảnh khuyến mãi
  - `[3]` Danh mục nhanh (Category shortcuts)
  - `[4]` Flash Sale countdown (nếu có)
  - `[5]` Sản phẩm nổi bật

### Slide 19 — Trang Danh sách Sản phẩm & Tìm kiếm
**Screenshot cần chụp tại `http://localhost:3000/products`:**
- Chụp **layout 2 cột**:
  - Trái: Sidebar filter — Category, Brand, Price range (slider), Rating stars
  - Phải: Product grid — mỗi card có ảnh, tên, giá, đánh giá sao, nút Add to cart
- Chú thích: Sort by (Mới nhất / Bán chạy / Giá tăng/giảm), Pagination

**Screenshot bonus:**
- Search bar đang nhập → thấy autocomplete dropdown suggestions

### Slide 20 — Trang Chi tiết Sản phẩm
**Screenshot cần chụp tại `http://localhost:3000/products/{id}`:**
- Phần **trên fold:**
  - Ảnh sản phẩm (gallery, thumbnail)
  - Tên sản phẩm, thương hiệu, rating sao + số review
  - Giá gốc / giá sale (highlight đỏ nếu đang sale)
  - Variant selector (màu sắc / size)
  - Số lượng tồn kho, quantity input
  - Nút "Thêm vào giỏ" + "Mua ngay"
- Phần **dưới fold:**
  - Tab: Mô tả / Thông số / Review
  - Review list với avatar, rating, comment, hình ảnh

### Slide 21 — Giỏ hàng & Checkout
**Screenshot 1 tại `http://localhost:3000/cart`:**
- Danh sách items: ảnh thumbnail, tên, variant, quantity controls (+/-), giá, nút xoá
- Tổng tiền, nút "Tiến hành thanh toán"

**Screenshot 2 tại `http://localhost:3000/checkout`:**
- Form chọn địa chỉ giao hàng
- Chọn đơn vị vận chuyển (GHN / GHTK / ViettelPost) + phí ship hiển thị
- Chọn phương thức thanh toán (VNPay / MoMo / ZaloPay / COD) với logo
- Ô nhập mã voucher + nút áp dụng
- Order summary: subtotal, shipping fee, discount, **tổng cộng**
- Nút "Đặt hàng"

### Slide 22 — Quản lý Đơn hàng
**Screenshot 1 tại `http://localhost:3000/orders`:**
- Danh sách đơn với status badge màu sắc:
  - 🟡 PENDING, 🔵 CONFIRMED, 🟣 SHIPPING, 🟢 DELIVERED, 🔴 CANCELLED

**Screenshot 2 tại `http://localhost:3000/orders/{id}`:**
- Order status timeline (dạng progress bar hoặc steps)
- Chi tiết items đã đặt
- Thông tin giao hàng, phương thức thanh toán
- Nút "Huỷ đơn" (nếu còn trong trạng thái PENDING)

### Slide 23 — Đăng ký / Đăng nhập
**Screenshot 1 tại `http://localhost:3000/login`:**
- Form: Email + Password, nút Login
- Link "Quên mật khẩu", "Chưa có tài khoản? Đăng ký"

**Screenshot 2 tại `http://localhost:3000/register`:**
- Form: Họ tên, Email, Phone, Password, Confirm Password
- Validation errors hiển thị ngay dưới field (Formik + Yup)
- Chú thích: validation realtime khi blur khỏi field

---

## MODULE 5: TESTING & CI/CD (2-3 slides)

### Slide 24 — Testing Strategy
**Nội dung:**

| Layer | Framework | Lệnh |
|-------|-----------|------|
| Backend Unit Test | JUnit 5 + Mockito | `mvn test` |
| Backend Integration | Spring Boot Test | `mvn test -Dtest=AuthServiceTest` |
| Frontend Unit Test | Vitest + Testing Library | `npx vitest run` |
| Frontend Coverage | Vitest Coverage | `npm run test:coverage` |

**Screenshot cần chụp:**
- Terminal chạy `npx vitest run` — thấy output xanh với `✓ X tests passed`
- Hoặc `npm run test:coverage` — bảng coverage (% Statements, Branches, Functions, Lines)

### Slide 25 — CI/CD Pipeline
**Nội dung:**
- GitHub Actions: tự động chạy khi push / pull request
- Pipeline steps:
  ```
  Push code → Trigger GitHub Actions
    → Build Backend (mvn clean install)
    → Run Backend Tests
    → Build Frontend (npm run build)
    → Run Frontend Tests (npx vitest run)
    → (Optional) Docker Build
  ```

**Screenshot cần chụp:**
- GitHub repository → tab **Actions** → workflow run thành công (dấu ✅ xanh)
- Click vào job để thấy từng step đã pass

---

## MODULE 6: KẾT LUẬN (2-3 slides)

### Slide 26 — Kết quả đạt được
**Nội dung — checklist:**

| Tính năng | Trạng thái |
|-----------|-----------|
| 18 Microservices đầy đủ | ✅ |
| JWT Authentication + RBAC (3 roles) | ✅ |
| Product Search với Elasticsearch | ✅ |
| Shopping Cart (Redis persistence) | ✅ |
| Đa cổng thanh toán (VNPay, MoMo, ZaloPay, COD) | ✅ |
| Đa vận chuyển (GHN, GHTK, ViettelPost) | ✅ |
| Review & Rating system | ✅ |
| Voucher & Flash Sale | ✅ |
| Seller Portal | ✅ |
| Message Queue async (RabbitMQ) | ✅ |
| Centralized Config (Spring Cloud Config) | ✅ |
| Docker Compose infrastructure | ✅ |

### Slide 27 — Hạn chế & Hướng phát triển
**Hạn chế:**
- Chưa deploy lên production (cloud)
- AI Service (8092) chưa fully implemented
- Chưa có real-time notification (WebSocket)
- Test coverage chưa đạt 80%+

**Hướng phát triển:**
- Kubernetes deployment với auto-scaling
- Mobile app (React Native / Flutter)
- AI recommendation engine (ai-service)
- Real-time chat hỗ trợ khách hàng (WebSocket)
- Monitoring dashboard (Prometheus + Grafana)

### Slide 28 — Q&A
```
Cảm ơn các thầy/cô và các bạn đã lắng nghe!

HyperMall — E-Commerce Platform
[Tên nhóm] | [MSSV] | [Học kỳ]

Source code: github.com/[repo]
```

---

## Checklist Screenshot (tổng hợp)

| # | Địa chỉ / Nơi chụp | Nội dung cần thấy | Dùng cho slide |
|---|-------------------|-------------------|----------------|
| 1 | `http://localhost:8761` | Bảng Instances với 18 services UP | Slide 5, 7 |
| 2 | `http://localhost:15672` → Overview | RabbitMQ tổng quan | Slide 8 |
| 3 | `http://localhost:15672` → Queues | Queue names: order.created, payment.done | Slide 9 |
| 4 | Browser DevTools → Network | POST /api/auth/login response với accessToken | Slide 10 |
| 5 | IntelliJ/VSCode → common-lib folder | Package structure | Slide 11 |
| 6 | `http://localhost:8081/swagger-ui.html` | Auth API endpoints | Slide 12 |
| 7 | `http://localhost:8082/swagger-ui.html` | Product API endpoints | Slide 13 |
| 8 | `http://localhost:8085/swagger-ui.html` | Payment API endpoints | Slide 14 |
| 9 | `http://localhost:9200/_cat/indices?v` | Index products với doc count | Slide 15 |
| 10 | MySQL tool → hypermall_products | Bảng products, columns | Slide 16 |
| 11 | VS Code → `frontend/hypermall-web/src/` | Folder structure | Slide 17 |
| 12 | `http://localhost:3000` | Trang chủ full | Slide 18 |
| 13 | `http://localhost:3000/products` | Sidebar filter + product grid | Slide 19 |
| 14 | `http://localhost:3000/products/{id}` | Chi tiết sản phẩm | Slide 20 |
| 15 | `http://localhost:3000/cart` | Giỏ hàng với items | Slide 21 |
| 16 | `http://localhost:3000/checkout` | Form checkout đầy đủ | Slide 21 |
| 17 | `http://localhost:3000/orders` | Danh sách đơn, status badges | Slide 22 |
| 18 | `http://localhost:3000/orders/{id}` | Chi tiết + timeline | Slide 22 |
| 19 | `http://localhost:3000/login` | Form đăng nhập | Slide 23 |
| 20 | `http://localhost:3000/register` | Form + validation errors | Slide 23 |
| 21 | Terminal | `npx vitest run` — test pass output | Slide 24 |
| 22 | GitHub Actions | Workflow run ✅ | Slide 25 |

---

## Tips khi chụp màn hình

- **Browser zoom:** Đặt 90% để thấy nhiều nội dung hơn, dùng F11 full-screen
- **Dữ liệu test:** Seed đủ dữ liệu trước khi chụp (sản phẩm, đơn hàng, user)
- **Eureka Dashboard:** Đảm bảo đủ 18 services **UP** (màu xanh) trước khi chụp
- **Annotation:** Dùng mũi tên + khoanh đỏ để highlight phần quan trọng trong slide
- **Thứ tự ưu tiên:** Slide 5 (Eureka), 18 (Home), 19 (Products), 21 (Checkout) là quan trọng nhất — cần chụp đẹp nhất
