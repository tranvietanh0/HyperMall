# HyperMall - Kế Hoạch Triển Khai Chi Tiết

## Tổng Quan Dự Án

**HyperMall** là hệ thống thương mại điện tử quy mô lớn với đầy đủ chức năng như Shopee/Lazada/Tiki, sử dụng kiến trúc Microservices, tích hợp AI (Chatbot, Recommendation, Image Search).

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React 18 + TypeScript + TailwindCSS + Redux Toolkit |
| **Backend** | Spring Boot 3.2 (Java 17+) + Spring Cloud |
| **Database** | MySQL 8.0 (PlanetScale free tier) |
| **Cache** | Redis (Upstash free tier) |
| **Message Queue** | RabbitMQ |
| **Search Engine** | Elasticsearch 8.x |
| **API Gateway** | Spring Cloud Gateway |
| **Service Discovery** | Netflix Eureka |
| **Config Management** | Spring Cloud Config |
| **Deployment** | Vercel (Frontend) + Railway/Render (Backend) |

---

## Cấu Trúc Thư Mục

```
HyperMall/
├── frontend/                    # React Frontend
│   └── hypermall-web/
│       ├── src/
│       │   ├── components/      # Reusable components
│       │   ├── pages/           # Page components
│       │   ├── hooks/           # Custom hooks
│       │   ├── services/        # API services
│       │   ├── store/           # Redux store
│       │   ├── types/           # TypeScript types
│       │   ├── utils/           # Utility functions
│       │   └── config/          # Configuration
│       └── package.json
│
├── backend/                     # Spring Boot Microservices
│   ├── pom.xml                  # Parent POM
│   ├── common-lib/              # Shared library
│   ├── service-registry/        # Eureka Server (8761)
│   ├── config-server/           # Config Server (8888)
│   ├── api-gateway/             # API Gateway (8080)
│   ├── user-service/            # User & Auth (8081)
│   ├── product-service/         # Products (8082)
│   ├── cart-service/            # Cart (8083)
│   ├── order-service/           # Orders (8084)
│   ├── payment-service/         # Payments (8085)
│   ├── inventory-service/       # Inventory (8086)
│   ├── shipping-service/        # Shipping (8087)
│   ├── promotion-service/       # Promotions (8088)
│   ├── review-service/          # Reviews (8089)
│   ├── search-service/          # Search (8090)
│   ├── notification-service/    # Notifications (8091)
│   ├── ai-service/              # AI Features (8092)
│   ├── media-service/           # Media Upload (8093)
│   ├── seller-service/          # Seller Center (8094)
│   └── analytics-service/       # Analytics (8095)
│
├── infrastructure/
│   ├── docker/
│   │   ├── docker-compose.yml
│   │   ├── docker-compose.dev.yml
│   │   └── mysql/init.sql
│   └── kubernetes/              # K8s configs (optional)
│
└── docs/                        # Documentation
```

---

## Phân Chia Phase

### Tổng quan Timeline

| Phase | Nội dung | Thời gian | Priority |
|-------|----------|-----------|----------|
| 1 | Foundation & Infrastructure | 2 tuần | P0 |
| 2 | User & Authentication | 2 tuần | P0 |
| 3 | Product & Category | 3 tuần | P0 |
| 4 | Cart & Checkout | 2 tuần | P0 |
| 5 | Payment Integration | 1 tuần | P0 |
| 6 | Inventory & Shipping | 2 tuần | P1 |
| 7 | Promotion & Voucher | 1 tuần | P1 |
| 8 | Review & Rating | 1 tuần | P1 |
| 9 | Search Service | 1 tuần | P1 |
| 10 | Notification Service | 1 tuần | P1 |
| 11 | AI Services | 3 tuần | P2 |
| 12 | Seller Center | 2 tuần | P1 |
| 13 | Media Service | 1 tuần | P1 |
| 14 | Analytics Service | 1 tuần | P2 |
| 15 | Admin Dashboard | 1 tuần | P1 |
| 16 | Testing & DevOps | 2 tuần | P1 |

**Tổng thời gian: ~26 tuần (6 tháng)**

---

## PHASE 1: Foundation & Infrastructure ✅ COMPLETED

### 1.1 Common Library
```
backend/common-lib/
├── dto/
│   ├── ApiResponse.java
│   ├── PageResponse.java
│   └── ErrorResponse.java
├── exception/
│   ├── BaseException.java
│   ├── ResourceNotFoundException.java
│   ├── ValidationException.java
│   ├── BadRequestException.java
│   ├── UnauthorizedException.java
│   ├── ForbiddenException.java
│   ├── ConflictException.java
│   └── GlobalExceptionHandler.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── SecurityConstants.java
│   ├── UserPrincipal.java
│   └── CurrentUser.java
├── util/
│   ├── DateTimeUtil.java
│   ├── StringUtil.java
│   └── ValidationUtil.java
├── event/
│   ├── BaseEvent.java
│   └── EventPublisher.java
└── config/
    ├── JacksonConfig.java
    ├── AsyncConfig.java
    └── RedisConfig.java
```

### 1.2 Service Registry (Eureka)
- Port: 8761
- URL: http://localhost:8761
- Credentials: eureka/eureka123

### 1.3 Config Server
- Port: 8888
- URL: http://localhost:8888
- Credentials: config/config123
- Native profile với configurations trong classpath

### 1.4 API Gateway
- Port: 8080
- Features:
  - Route forwarding to microservices
  - JWT Authentication Filter
  - CORS Configuration
  - Rate Limiting
  - Circuit Breaker với Resilience4j
  - Fallback handlers

### 1.5 Frontend Foundation
```
frontend/hypermall-web/
├── src/
│   ├── components/
│   │   ├── common/        # Button, Input, Modal, Loading
│   │   └── layout/        # Header, Footer, MainLayout
│   ├── pages/             # Home, Auth, Product, Cart, etc.
│   ├── hooks/             # useAuth, useCart, useDebounce
│   ├── services/          # API services
│   ├── store/             # Redux slices
│   ├── types/             # TypeScript definitions
│   └── utils/             # format, validation, storage
├── package.json
├── tsconfig.json
├── vite.config.ts
└── tailwind.config.js
```

---

## PHASE 2: User & Authentication (Tuần 3-4)

### 2.1 User Service

**Entities:**
```java
User {
    id: Long
    email: String
    password: String (hashed)
    fullName: String
    phone: String
    avatar: String
    role: UserRole (BUYER, SELLER, ADMIN)
    status: UserStatus (ACTIVE, INACTIVE, BANNED)
    emailVerified: Boolean
    createdAt: LocalDateTime
    updatedAt: LocalDateTime
}

Address {
    id: Long
    userId: Long
    fullName: String
    phone: String
    province: String
    district: String
    ward: String
    addressDetail: String
    isDefault: Boolean
    type: AddressType (HOME, OFFICE)
}
```

**API Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Đăng ký tài khoản |
| POST | /api/auth/login | Đăng nhập |
| POST | /api/auth/refresh-token | Làm mới token |
| POST | /api/auth/forgot-password | Quên mật khẩu |
| POST | /api/auth/reset-password | Đặt lại mật khẩu |
| POST | /api/auth/verify-email | Xác thực email |
| POST | /api/auth/logout | Đăng xuất |
| GET | /api/users/me | Lấy thông tin user hiện tại |
| PUT | /api/users/me | Cập nhật profile |
| PUT | /api/users/me/password | Đổi mật khẩu |
| POST | /api/users/me/avatar | Upload avatar |
| GET | /api/users/addresses | Danh sách địa chỉ |
| POST | /api/users/addresses | Thêm địa chỉ |
| PUT | /api/users/addresses/{id} | Sửa địa chỉ |
| DELETE | /api/users/addresses/{id} | Xóa địa chỉ |

### 2.2 Frontend Auth Module
- LoginForm với validation
- RegisterForm với password strength
- ForgotPasswordForm
- SocialLogin (Google, Facebook)
- Protected Routes

---

## PHASE 3: Product & Category (Tuần 5-7)

### 3.1 Product Service

**Entities:**
```java
Category {
    id: Long
    name: String
    slug: String
    description: String
    image: String
    parentId: Long
    level: Integer
    sortOrder: Integer
    isActive: Boolean
    children: List<Category>
}

Product {
    id: Long
    sellerId: Long
    categoryId: Long
    brandId: Long
    name: String
    slug: String
    description: String (HTML)
    shortDescription: String
    thumbnail: String
    basePrice: BigDecimal
    salePrice: BigDecimal
    status: ProductStatus (DRAFT, PENDING, ACTIVE, INACTIVE)
    totalSold: Integer
    avgRating: Double
    totalReviews: Integer
    hasVariants: Boolean
    createdAt: LocalDateTime
}

ProductVariant {
    id: Long
    productId: Long
    sku: String
    name: String
    price: BigDecimal
    salePrice: BigDecimal
    image: String
    attributes: Map<String, String>
    stock: Integer
    isActive: Boolean
}

ProductImage {
    id: Long
    productId: Long
    url: String
    sortOrder: Integer
    isMain: Boolean
}

Brand {
    id: Long
    name: String
    slug: String
    logo: String
}
```

**API Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/categories | Danh sách categories |
| GET | /api/categories/tree | Category tree |
| GET | /api/products | Danh sách sản phẩm (với filter) |
| GET | /api/products/{id} | Chi tiết sản phẩm |
| GET | /api/products/category/{id} | Sản phẩm theo category |
| GET | /api/brands | Danh sách brands |
| POST | /api/seller/products | Tạo sản phẩm (seller) |
| PUT | /api/seller/products/{id} | Sửa sản phẩm (seller) |

### 3.2 Frontend Product Module
- ProductCard, ProductGrid
- ProductDetail với image gallery
- ProductFilter (price, category, brand, rating)
- ProductSort
- CategoryNavigation

---

## PHASE 4: Cart & Checkout (Tuần 8-9)

### 4.1 Cart Service

**Entity:**
```java
Cart {
    id: Long
    userId: Long
    items: List<CartItem>
    totalItems: Integer
    subtotal: BigDecimal
}

CartItem {
    id: Long
    productId: Long
    variantId: Long
    sellerId: Long
    quantity: Integer
    price: BigDecimal
    selected: Boolean
}
```

**API Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/cart | Lấy giỏ hàng |
| POST | /api/cart/items | Thêm sản phẩm |
| PUT | /api/cart/items/{id} | Cập nhật số lượng |
| DELETE | /api/cart/items/{id} | Xóa sản phẩm |
| DELETE | /api/cart/clear | Xóa toàn bộ |
| POST | /api/cart/checkout-preview | Preview checkout |

### 4.2 Order Service

**Entity:**
```java
Order {
    id: Long
    orderNumber: String
    userId: Long
    sellerId: Long
    status: OrderStatus
    paymentStatus: PaymentStatus
    paymentMethod: PaymentMethod
    subtotal: BigDecimal
    shippingFee: BigDecimal
    discount: BigDecimal
    total: BigDecimal
    shippingAddress: Address
    note: String
    items: List<OrderItem>
    createdAt: LocalDateTime
}

OrderStatus: PENDING_PAYMENT, PAID, CONFIRMED, PROCESSING,
             SHIPPING, DELIVERED, COMPLETED, CANCELLED, RETURNED
```

**API Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/orders | Tạo đơn hàng |
| GET | /api/orders | Danh sách đơn hàng |
| GET | /api/orders/{id} | Chi tiết đơn hàng |
| PUT | /api/orders/{id}/cancel | Hủy đơn |
| GET | /api/orders/tracking/{orderNumber} | Tracking |

---

## PHASE 5: Payment Integration (Tuần 10)

### 5.1 Payment Service

**Supported Methods:**
- VNPay
- MoMo
- ZaloPay
- Bank Transfer
- COD (Cash on Delivery)

**Entity:**
```java
Payment {
    id: Long
    orderId: Long
    userId: Long
    amount: BigDecimal
    method: PaymentMethod
    status: PaymentStatus (PENDING, SUCCESS, FAILED, REFUNDED)
    transactionId: String
    gatewayResponse: String (JSON)
    paidAt: LocalDateTime
}
```

**API Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/payments/create | Tạo payment |
| GET | /api/payments/{id} | Chi tiết payment |
| GET | /api/payments/vnpay/callback | VNPay callback |
| POST | /api/payments/momo/callback | MoMo callback |
| POST | /api/payments/zalopay/callback | ZaloPay callback |

---

## PHASE 6: Inventory & Shipping (Tuần 11-12)

### 6.1 Inventory Service

**Entity:**
```java
Inventory {
    id: Long
    productId: Long
    variantId: Long
    sellerId: Long
    quantity: Integer
    reservedQuantity: Integer
    availableQuantity: Integer (computed)
    lowStockThreshold: Integer
}

StockMovement {
    id: Long
    inventoryId: Long
    type: MovementType (IN, OUT, RESERVE, RELEASE)
    quantity: Integer
    referenceType: String
    referenceId: Long
    note: String
}
```

### 6.2 Shipping Service

**Providers:**
- Giao Hàng Nhanh (GHN)
- Giao Hàng Tiết Kiệm (GHTK)
- Viettel Post

**API Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/shipping/calculate | Tính phí ship |
| GET | /api/shipping/methods | Phương thức vận chuyển |
| GET | /api/shipping/track/{trackingNumber} | Tracking |

---

## PHASE 7: Promotion & Voucher (Tuần 13)

### 7.1 Promotion Service

**Entities:**
```java
Voucher {
    id: Long
    code: String
    name: String
    type: VoucherType (PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING)
    value: BigDecimal
    maxDiscount: BigDecimal
    minOrderValue: BigDecimal
    usageLimit: Integer
    usedCount: Integer
    userLimit: Integer
    applicableCategories: List<Long>
    applicableProducts: List<Long>
    startDate: LocalDateTime
    endDate: LocalDateTime
    isActive: Boolean
}

FlashSale {
    id: Long
    name: String
    startTime: LocalDateTime
    endTime: LocalDateTime
    status: FlashSaleStatus
    products: List<FlashSaleProduct>
}
```

---

## PHASE 8: Review & Rating (Tuần 14)

### 8.1 Review Service

**Entity:**
```java
Review {
    id: Long
    productId: Long
    variantId: Long
    orderId: Long
    userId: Long
    rating: Integer (1-5)
    content: String
    images: List<String>
    videos: List<String>
    likeCount: Integer
    isVerifiedPurchase: Boolean
    status: ReviewStatus
    sellerReply: String
    sellerReplyAt: LocalDateTime
}

ReviewStatistics {
    productId: Long
    averageRating: Double
    totalReviews: Integer
    ratingDistribution: Map<Integer, Integer>
    withImages: Integer
}
```

---

## PHASE 9: Search Service (Tuần 15)

### 9.1 Elasticsearch Integration

**Document:**
```java
ProductDocument {
    id: String
    name: String
    nameKeyword: String
    description: String
    categoryId: Long
    categoryPath: List<String>
    brandId: Long
    brandName: String
    sellerId: Long
    sellerName: String
    price: Double
    salePrice: Double
    thumbnail: String
    rating: Double
    totalSold: Integer
    attributes: Map<String, String>
}
```

**Features:**
- Full-text search
- Autocomplete/Suggestions
- Faceted filtering
- Trending keywords

---

## PHASE 10: Notification Service (Tuần 16)

### 10.1 Notification Service

**Channels:**
- Email (SendGrid)
- SMS (Twilio/Stringee)
- Push Notification (Firebase)
- In-app Notification

**Notification Types:**
- ORDER_CREATED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED
- PROMOTION, FLASH_SALE, PRICE_DROP
- REVIEW_REPLY

---

## PHASE 11: AI Services (Tuần 17-19)

### 11.1 AI Chatbot
- Intent Detection
- Context Management
- Response Generation
- Providers: OpenAI, Claude, Gemini

### 11.2 Product Recommendation
- Collaborative Filtering
- Content-Based Filtering
- User Behavior Analysis

### 11.3 Image Search
- Image Embedding
- Vector Store
- Similar Product Search

---

## PHASE 12: Seller Center (Tuần 20-21)

### 12.1 Seller Service

**Entity:**
```java
Seller {
    id: Long
    userId: Long
    shopName: String
    shopSlug: String
    logo: String
    banner: String
    description: String
    businessType: BusinessType
    businessLicense: String
    taxCode: String
    bankAccount: BankAccount
    status: SellerStatus (PENDING, ACTIVE, SUSPENDED)
    rating: Double
    totalProducts: Integer
    totalFollowers: Integer
}
```

**Features:**
- Seller Registration & Verification
- Shop Management
- Product Management
- Order Management
- Statistics Dashboard

---

## PHASE 13: Media Service (Tuần 22)

### 13.1 Media Service

**Features:**
- Image Upload & Resize
- Video Upload
- Storage Providers: S3, Cloudinary, Local

---

## PHASE 14: Analytics Service (Tuần 23)

### 14.1 Analytics Service

**Features:**
- Event Tracking
- Sales Reports
- User Reports
- Real-time Analytics

---

## PHASE 15: Admin Dashboard (Tuần 24)

### 15.1 Admin Features
- User Management
- Product Moderation
- Order Management
- Seller Approval
- Category Management
- Promotion Management
- Reports & Analytics
- System Settings

---

## PHASE 16: Testing & DevOps (Tuần 25-26)

### 16.1 Testing
- Unit Tests (JUnit, Vitest)
- Integration Tests
- E2E Tests (Playwright/Cypress)

### 16.2 DevOps
- Docker Compose
- CI/CD (GitHub Actions)
- Monitoring (Prometheus, Grafana)
- Logging (ELK Stack)

---

## Deployment Strategy (Free Tier)

| Service | Platform | Free Tier |
|---------|----------|-----------|
| Frontend | Vercel | Unlimited |
| Backend | Railway/Render | 5 services |
| Database | PlanetScale | 5GB MySQL |
| Cache | Upstash | 10K commands/day |
| Search | Algolia | 10K records |
| Queue | CloudAMQP | Little Lemur |
| Storage | Cloudinary | 25GB |
| AI | OpenAI/Claude | Pay-as-you-go |

---

## Quick Start

### 1. Start Infrastructure
```bash
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

### 2. Build Backend
```bash
cd backend
mvn clean install -DskipTests
```

### 3. Start Services (theo thứ tự)
```bash
# Terminal 1: Service Registry
cd backend/service-registry
mvn spring-boot:run

# Terminal 2: Config Server
cd backend/config-server
mvn spring-boot:run

# Terminal 3: API Gateway
cd backend/api-gateway
mvn spring-boot:run

# Terminal 4+: Other services...
```

### 4. Start Frontend
```bash
cd frontend/hypermall-web
npm install
npm run dev
```

### 5. Access URLs
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- RabbitMQ: http://localhost:15672

---

## Progress Tracking

### Completed ✅

#### Phase 1: Foundation & Infrastructure (02/03/2026)

**1.1 Backend Parent POM & Common Library** ✅
- `backend/pom.xml` - Parent POM với dependency management
- `backend/common-lib/pom.xml`
- DTOs: `ApiResponse`, `PageResponse`, `ErrorResponse`
- Exceptions: `BaseException`, `ResourceNotFoundException`, `ValidationException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ConflictException`, `GlobalExceptionHandler`
- Security: `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConstants`, `UserPrincipal`, `CurrentUser`
- Utils: `DateTimeUtil`, `StringUtil`, `ValidationUtil`
- Events: `BaseEvent`, `EventPublisher`
- Config: `JacksonConfig`, `AsyncConfig`, `RedisConfig`

**1.2 Service Registry (Eureka Server)** ✅
- `backend/service-registry/` - Port 8761
- `ServiceRegistryApplication.java`
- `SecurityConfig.java`
- `application.yml`

**1.3 Config Server** ✅
- `backend/config-server/` - Port 8888
- `ConfigServerApplication.java`
- `SecurityConfig.java`
- Configurations: `application.yml`, `user-service.yml`, `product-service.yml`, `cart-service.yml`, `order-service.yml`, `payment-service.yml`, `api-gateway.yml`

**1.4 API Gateway** ✅
- `backend/api-gateway/` - Port 8080
- `ApiGatewayApplication.java`
- `GatewayConfig.java` - Route definitions
- `CorsConfig.java` - CORS settings
- `RateLimitConfig.java` - Rate limiting
- `AuthenticationFilter.java` - JWT validation
- `LoggingFilter.java` - Request/Response logging
- `FallbackController.java` - Circuit breaker fallbacks

**1.5 Frontend Foundation** ✅
- `frontend/hypermall-web/package.json` - Dependencies
- `vite.config.ts`, `tsconfig.json`, `tailwind.config.js`
- **Config**: `api.config.ts`, `constants.ts`
- **Types**: `api.types.ts`, `user.types.ts`, `product.types.ts`, `order.types.ts`
- **Services**: `api.service.ts`, `auth.service.ts`, `product.service.ts`, `cart.service.ts`, `order.service.ts`
- **Store**: Redux slices - `authSlice`, `cartSlice`, `productSlice`, `uiSlice`
- **Hooks**: `useAuth`, `useCart`, `useDebounce`, `useInfiniteScroll`
- **Utils**: `format.ts`, `validation.ts`, `storage.ts`
- **Components**: `Button`, `Input`, `Modal`, `Loading`, `Header`, `Footer`, `MainLayout`
- **Pages**: `Home`, `LoginPage`, `RegisterPage`, `ProductListPage`, `ProductDetailPage`, `Cart`, `Checkout`, `OrderListPage`, `OrderDetailPage`, `Profile`, `NotFound`
- **Routes**: `routes/index.tsx`, `ProtectedRoute.tsx`

**1.6 Infrastructure** ✅
- `infrastructure/docker/docker-compose.yml`
- `infrastructure/docker/docker-compose.dev.yml`
- `infrastructure/docker/mysql/init.sql`

---

#### Phase 2: User & Authentication (03/03/2026)

**2.1 User Service** ✅
- `backend/user-service/` - Port 8081
- `UserServiceApplication.java`

**2.2 Entities** ✅
- `User.java` - User entity với email, password, fullName, phone, avatar, role, status
- `Address.java` - Address entity với province, district, ward, addressDetail
- `UserRole.java` - Enum: BUYER, SELLER, ADMIN
- `UserStatus.java` - Enum: ACTIVE, INACTIVE, BANNED
- `AddressType.java` - Enum: HOME, OFFICE

**2.3 Repositories** ✅
- `UserRepository.java` - findByEmail, existsByEmail, findByVerificationToken, findByResetPasswordToken
- `AddressRepository.java` - findByUserId, clearDefaultAddress

**2.4 DTOs** ✅
- Auth: `RegisterRequest`, `LoginRequest`, `AuthResponse`, `RefreshTokenRequest`, `ForgotPasswordRequest`, `ResetPasswordRequest`
- User: `UserResponse`, `UpdateProfileRequest`, `ChangePasswordRequest`
- Address: `AddressRequest`, `AddressResponse`

**2.5 Services** ✅
- `AuthService.java` - register, login, refreshToken, logout, forgotPassword, resetPassword, verifyEmail
- `UserService.java` - getCurrentUser, updateProfile, changePassword, updateAvatar
- `AddressService.java` - getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress

**2.6 Controllers** ✅
- `AuthController.java` - /api/auth/*
- `UserController.java` - /api/users/*
- `AddressController.java` - /api/users/addresses/*

**2.7 Config** ✅
- `SecurityConfig.java` - JWT authentication, BCrypt password encoder
- `OpenApiConfig.java` - Swagger/OpenAPI configuration
- `application.yml` - Database, Redis, Eureka, JWT config

---

### In Progress 🔄
- [ ] Phase 3: Product & Category

---

### Pending ⏳
- [ ] Phase 4: Cart & Checkout
- [ ] Phase 5: Payment Integration
- [ ] Phase 6: Inventory & Shipping
- [ ] Phase 7: Promotion & Voucher
- [ ] Phase 8: Review & Rating
- [ ] Phase 9: Search Service
- [ ] Phase 10: Notification Service
- [ ] Phase 11: AI Services
- [ ] Phase 12: Seller Center
- [ ] Phase 13: Media Service
- [ ] Phase 14: Analytics Service
- [ ] Phase 15: Admin Dashboard
- [ ] Phase 16: Testing & DevOps

---

## Notes

- Tất cả services đều có health check endpoint: `/actuator/health`
- JWT secret cần được thay đổi trong production
- Cần setup SSL/TLS cho production
- Database migration sử dụng Flyway (tùy chọn)
- Eureka credentials: `eureka/eureka123`
- Config Server credentials: `config/config123`
