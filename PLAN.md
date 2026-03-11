# HyperMall - Kế Hoạch Triển Khai Chi Tiết

## Tổng Quan Dự Án

**HyperMall** là hệ thống thương mại điện tử quy mô lớn với đầy đủ chức năng như Shopee/Lazada/Tiki, sử dụng kiến trúc Microservices.

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

| Phase | Nội dung | Thời gian | Priority | Status |
|-------|----------|-----------|----------|--------|
| 1 | Foundation & Infrastructure | 2 tuần | P0 | ✅ Done |
| 2 | User & Authentication | 2 tuần | P0 | ✅ Done |
| 3 | Product & Category | 3 tuần | P0 | ✅ Done |
| 4 | Cart & Checkout | 2 tuần | P0 | ✅ Done |
| 5 | Payment Integration | 1 tuần | P0 | ✅ Done |
| 6 | Inventory & Shipping | 2 tuần | P1 | ✅ Done |
| 7 | Promotion & Voucher | 1 tuần | P1 | ✅ Done |
| 8 | Review & Rating | 1 tuần | P1 | ✅ Done |
| 9 | Search Service | 1 tuần | P1 | ✅ Done |
| 10 | Notification Service | 1 tuần | P1 | ✅ Done |
| 11 | AI Services | 3 tuần | P2 | 🚫 Removed |
| 12 | Seller Center | 2 tuần | P1 | ✅ Done |
| 13 | Media Service | 1 tuần | P1 | ✅ Done |
| 14 | Analytics Service | 1 tuần | P2 | ✅ Done |
| 15 | Admin Dashboard | 1 tuần | P1 | ✅ Done |
| 16 | Testing & DevOps | 2 tuần | P1 | ✅ Done |

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

## PHASE 2: User & Authentication ✅ COMPLETED

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

## PHASE 3: Product & Category ✅ COMPLETED

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

## PHASE 4: Cart & Checkout ✅ COMPLETED

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

## PHASE 5: Payment Integration ✅ COMPLETED

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

## PHASE 6: Inventory & Shipping ✅ COMPLETED

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

## PHASE 7: Promotion & Voucher ✅ COMPLETED

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

## PHASE 8: Review & Rating ✅ COMPLETED

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

## PHASE 11: AI Services (Tuần 17-19) - REMOVED

- Module `backend/ai-service` da duoc loai khoi repository.
- Maven parent, API Gateway routes/fallback, env vars, va tai lieu lien quan da duoc don dep.
- Khong tiep tuc scope AI trong ke hoach hien tai.

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
- Configurations: `application.yml`, `user-service.yml`, `product-service.yml`, `cart-service.yml`, `order-service.yml`, `payment-service.yml`, `api-gateway.yml`, `seller-service.yml`

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

### Completed ✅

#### Phase 3: Product & Category (03/03/2026)

**3.1 Product Service (Backend)** ✅
- `backend/product-service/pom.xml` - Maven configuration
- `ProductServiceApplication.java` - Main Spring Boot application
- **Entities (5)**:  `Product`, `Category`, `Brand`, `ProductVariant`, `ProductImage`, `ProductStatus` enum
- **DTOs (11)**: Request - `ProductRequest`, `CategoryRequest`, `BrandRequest`, `ProductVariantRequest`, `ProductImageRequest`; Response - `ProductResponse`, `ProductDetailResponse`, `CategoryResponse`, `BrandResponse`, `ProductVariantResponse`, `ProductImageResponse`
- **Mapper**: `ProductMapper` - MapStruct interface for entity-to-DTO conversion
- **Repositories (5)**: `ProductRepository`, `CategoryRepository`, `BrandRepository`, `ProductVariantRepository`, `ProductImageRepository`
- **Services (3)**: `ProductService`, `CategoryService`, `BrandService`
- **Controllers (4)**: `ProductController`, `CategoryController`, `BrandController`, `SellerProductController`
- **Config**: `SecurityConfig`, `OpenApiConfig`, `application.yml`

**3.2 Frontend (Partial)** ✅
- `src/types/product.types.ts` - TypeScript type definitions
- `src/config/api.config.ts` - Updated with BRANDS endpoints
- Types exported in `src/types/index.ts`

**Note**: Frontend services, Redux, hooks, and components for Phase 3 are pending implementation.

---

---

#### Phase 4: Cart & Checkout (03/03/2026)

**4.1 Cart Service (Backend)** ✅
- `backend/cart-service/pom.xml` - Maven configuration (Redis only, no MySQL)
- `CartServiceApplication.java` - Main Spring Boot application
- **Model (2)**: `Cart`, `CartItem` - POJOs serialized to Redis (key: `cart:{userId}`)
- **DTOs (5)**: `AddCartItemRequest`, `UpdateCartItemRequest`, `CartItemResponse`, `CartResponse`, `CheckoutPreviewResponse`
- **Service**: `CartService` - getCart, addItem, updateItem, removeItem, clearCart, checkoutPreview, selectAll
- **Controller**: `CartController` - GET/POST/PUT/DELETE `/api/cart`, `/api/cart/items/**`, `/api/cart/checkout-preview`, `/api/cart/select-all`
- **Config**: `SecurityConfig`, `OpenApiConfig`, `application.yml`

**4.2 Order Service (Backend)** ✅
- `backend/order-service/pom.xml` - Maven configuration (MySQL + Redis + RabbitMQ)
- `OrderServiceApplication.java` - Main Spring Boot application
- **Entities (7)**: `Order`, `OrderItem`, `ShippingAddress` (Embeddable), `OrderStatus`, `PaymentStatus`, `PaymentMethod`
- **DTOs (7)**: `CreateOrderRequest`, `OrderItemRequest`, `ShippingAddressRequest`, `CancelOrderRequest`, `OrderResponse`, `OrderDetailResponse`, `OrderItemResponse`, `ShippingAddressResponse`
- **Mapper**: `OrderMapper` - MapStruct interface
- **Repositories (2)**: `OrderRepository`, `OrderItemRepository`
- **Service**: `OrderService` - createOrder, getUserOrders, getOrderById, getOrderByNumber, cancelOrder, getSellerOrders, updateOrderStatus
- **Controllers (2)**: `OrderController` (`/api/orders/**`), `SellerOrderController` (`/api/seller/orders/**`)
- **Config**: `SecurityConfig`, `OpenApiConfig`, `application.yml`

**4.3 Parent POM** ✅
- Added `cart-service` and `order-service` as Maven modules

---

#### Phase 5: Payment Integration (03/03/2026)

**5.1 Payment Service (Backend)** ✅
- `backend/payment-service/pom.xml` - Maven configuration (MySQL + WebFlux for HTTP client)
- `PaymentServiceApplication.java` - Main Spring Boot application
- **Entity**: `Payment` - stores payment record with status, transactionId, gatewayResponse
- **Enums**: `PaymentMethod` (COD, VNPAY, MOMO, ZALOPAY, BANK_TRANSFER, WALLET), `PaymentStatus`
- **DTOs (5)**: `CreatePaymentRequest`, `PaymentResponse`, `VNPayCallbackRequest`, `MoMoCallbackRequest`, `ZaloPayCallbackRequest`
- **Gateways (3)**:
  - `VNPayGateway` - HMAC SHA512 signing, payment URL generation, callback verification
  - `MoMoGateway` - HMAC SHA256, REST call to MoMo API sandbox, IPN callback handling
  - `ZaloPayGateway` - HMAC SHA256, REST call to ZaloPay sandbox, callback handling
- **Properties (3)**: `VNPayProperties`, `MoMoProperties`, `ZaloPayProperties` (ConfigurationProperties)
- **Mapper**: `PaymentMapper`, **Repository**: `PaymentRepository`
- **Service**: `PaymentService` - createPayment, getPaymentById, handleVNPayCallback, handleMoMoCallback, handleZaloPayCallback
- **Controller**: `PaymentController` - POST /create, GET /{id}, GET /vnpay/callback, POST /momo/callback, POST /zalopay/callback
- **Config**: `SecurityConfig` (callbacks public), `OpenApiConfig`, `WebClientConfig`, `application.yml`

**5.2 Parent POM** ✅
- Added `payment-service` as Maven module

---

### In Progress 🔄
- [ ] Phase 3: Frontend implementation (Services, Redux, Hooks, Components, Pages)

---

#### Phase 12: Seller Center (11/03/2026) ✅

**12.1 Seller Service (Backend)** ✅
- `backend/seller-service/` - Port 8094
- Added base module and Spring Boot application (`SellerServiceApplication`)
- **Entity/Enums**: `Seller`, `SellerFollower`, `BusinessType`, `SellerStatus`
- **DTOs**: `CreateSellerRequest`, `UpdateSellerRequest`, `SellerResponse`, `SellerDashboardResponse`, `FollowResponse`
- **Repository/Mapper**: `SellerRepository`, `SellerFollowerRepository`, `SellerMapper`
- **Service**: `SellerService` - register/get/update seller profile, seller dashboard summary, public/admin filter-search by status or keyword, update status
- **Service**: `SellerFollowerService` - follow/unfollow seller, check following status, get follower/following lists
- **Controllers**: `SellerController`, `AdminSellerController`
- **Config**: `SecurityConfig`, `OpenApiConfig`, `application.yml`
- Added `seller-service` module to `backend/pom.xml`
- Added central config file `backend/config-server/src/main/resources/configurations/seller-service.yml`
- **Endpoints**:
  - `POST /api/sellers/register` - Register as seller
  - `GET /api/sellers/me` - Get current seller profile
  - `PUT /api/sellers/me` - Update seller profile
  - `GET /api/sellers/me/dashboard` - Get seller dashboard summary
  - `GET /api/sellers` - Public seller listing
  - `GET /api/sellers/{id}` - Get seller by ID
  - `GET /api/sellers/slug/{slug}` - Get seller by shop slug
  - `POST /api/sellers/{id}/follow` - Follow a seller
  - `DELETE /api/sellers/{id}/follow` - Unfollow a seller
  - `GET /api/sellers/{id}/following` - Check if following
  - `GET /api/sellers/me/following` - Get list of followed sellers
  - `GET /api/admin/sellers/search` - Admin seller search
  - `PUT /api/admin/sellers/{id}/status` - Update seller status

---

#### Phase 13: Media Service (11/03/2026) ✅

**13.1 Media Service (Backend)** ✅
- `backend/media-service/` - Port 8093
- Added base module and Spring Boot application (`MediaServiceApplication`)
- **Entity/Enums**: `Media`, `MediaType`, `StorageProvider`
- **DTOs**: `MediaResponse`, `UploadResponse`
- **Repository**: `MediaRepository`
- **Storage**: `StorageService` interface, `LocalStorageService` implementation
- **Mapper**: `MediaMapper`
- **Service**: `MediaService` - uploadFiles, uploadSingleFile, getMediaById, getUserMedia, getMediaByReference, deleteMedia, loadFile
- **Controller**: `MediaController`
- **Config**: `SecurityConfig`, `OpenApiConfig`, `application.yml`
- **Features**:
  - Image upload with automatic thumbnail generation (Thumbnailator)
  - Video upload support (mp4, webm)
  - Document upload support (pdf, doc, docx)
  - File validation (size, type)
  - Local storage (with S3 ready for integration)
  - Reference-based media lookup (for products, reviews, etc.)
- **Endpoints**:
  - `POST /api/media/upload` - Upload multiple files
  - `POST /api/media/upload/single` - Upload single file
  - `GET /api/media/{id}` - Get media by ID
  - `GET /api/media/my` - Get current user's media
  - `GET /api/media/reference/{type}/{id}` - Get media by reference
  - `DELETE /api/media/{id}` - Delete media
  - `GET /api/media/files/**` - Serve files (public)

---

#### Phase 14: Analytics Service (11/03/2026) ✅

**14.1 Analytics Service (Backend)** ✅
- `backend/analytics-service/` - Port 8095
- Added base module and Spring Boot application (`AnalyticsServiceApplication`)
- **Entity/Enums**: `AnalyticsEvent`, `DailyStats`, `EventType`
- **DTOs**: `TrackEventRequest`, `DashboardStatsResponse`, `DailyStatsItem`, `TopProductItem`, `TopCategoryItem`
- **Repository**: `AnalyticsEventRepository`, `DailyStatsRepository`
- **Service**: `AnalyticsService` - trackEvent, getDashboardStats, getTopSearchQueries
- **Controller**: `AnalyticsController`
- **Config**: `SecurityConfig`, `OpenApiConfig`, `application.yml`
- **Features**:
  - Event tracking (page views, product views, add to cart, checkout, search, etc.)
  - Dashboard statistics with daily aggregation
  - Seller-specific analytics
  - Top products and categories by views
  - Trending search queries
  - Conversion rate and average order value calculation
  - Device type detection
  - IP address tracking
- **Endpoints**:
  - `POST /api/analytics/track` - Track an event (public)
  - `GET /api/analytics/dashboard` - Get dashboard stats
  - `GET /api/analytics/seller/dashboard` - Get seller dashboard stats
  - `GET /api/analytics/search/trending` - Get trending search queries

#### Scope Update (11/03/2026)

**11.1 AI Services** 🚫
- Removed `backend/ai-service` from the repository scope
- Removed `ai-service` from `backend/pom.xml`
- Removed AI route/fallback from API Gateway
- Removed AI env keys and plan references

---

#### Phase 6: Inventory & Shipping (10/03/2026)

**6.1 Inventory Service** ✅
- `backend/inventory-service/` - Port 8086
- **Entities**: `Inventory`, `StockMovement`, `MovementType` enum
- **DTOs**: `CreateInventoryRequest`, `UpdateStockRequest`, `ReserveStockRequest`, `InventoryResponse`, `StockMovementResponse`, `StockCheckResponse`
- **Repository**: `InventoryRepository`, `StockMovementRepository` (with pessimistic locking for reserve)
- **Service**: `InventoryService` - createInventory, updateStock, reserveStock, releaseStock, confirmStock, checkStock, getMovements
- **Controller**: `InventoryController` - full CRUD, stock management, low stock alerts, out of stock alerts
- **Features**: Stock tracking, reservation system, movement history, low stock threshold alerts

**6.2 Shipping Service** ✅
- `backend/shipping-service/` - Port 8087
- **Entities**: `Shipment`, `TrackingEvent`, `ShipmentStatus`, `ShippingProvider` enums
- **DTOs**: `CalculateShippingRequest`, `CreateShipmentRequest`, `ShippingOptionResponse`, `ShipmentResponse`, `TrackingResponse`
- **Repository**: `ShipmentRepository`, `TrackingEventRepository`
- **Service**: `ShippingService` - calculateShipping, createShipment, trackShipment, updateStatus
- **Controller**: `ShippingController` - calculate fees, create/track shipments
- **Providers**: GHN, GHTK, ViettelPost (mock implementation, ready for real API integration)
- **Features**: Multi-provider support, fee calculation, tracking events, COD support

---

#### Phase 10: Notification Service (10/03/2026)

**10.1 Notification Service** ✅
- `backend/notification-service/` - Port 8091
- **Entities**: `Notification`, `NotificationTemplate`, `NotificationPreference`, `DeviceToken`
- **Enums**: `NotificationType`, `NotificationChannel`, `NotificationStatus`
- **DTOs**: `SendNotificationRequest`, `BulkNotificationRequest`, `NotificationResponse`, `NotificationPreferenceRequest`, `NotificationPreferenceResponse`, `DeviceTokenRequest`, `UnreadCountResponse`
- **Repository**: `NotificationRepository`, `NotificationTemplateRepository`, `NotificationPreferenceRepository`, `DeviceTokenRepository`
- **Providers**: `EmailProvider` (SMTP/SendGrid), `SmsProvider` (Twilio/Stringee), `PushProvider` (Firebase FCM)
- **Service**: `NotificationService` - sendNotification, sendBulkNotification, getUserNotifications, markAsRead, getUnreadCount, updatePreference, registerDeviceToken
- **Controllers**: `NotificationController`, `AdminNotificationController`
- **Events**: `NotificationEvent`, `NotificationEventListener` (RabbitMQ consumer)
- **Config**: `SecurityConfig`, `OpenApiConfig`, `RabbitMQConfig`, `AsyncConfig`
- **Templates**: Thymeleaf email templates
- **Features**: Multi-channel (Email, SMS, Push, In-app), User preferences, Device token management, Async processing, Dead letter queue

---

### Pending ⏳
- [ ] Phase 4: Frontend (Cart UI, Checkout flow)

---

#### Phase 16: Testing & DevOps (11/03/2026) ✅

**16.1 Testing** ✅
- **Frontend Unit Tests** (Vitest):
  - `src/utils/format.test.ts` - Format utilities tests (currency, date, file size, etc.)
  - `src/utils/validation.test.ts` - Validation schema tests (Yup schemas, email, phone)
  - `src/hooks/useDebounce.test.ts` - Hook tests
  - `src/test/setup.ts` - Test setup with mocks (localStorage, matchMedia, IntersectionObserver)
  - `vitest.config.ts` - Vitest configuration with coverage
- **Backend Unit Tests** (JUnit 5):
  - `user-service/src/test/java/com/hypermall/user/service/AuthServiceTest.java` - AuthService tests
  - `user-service/src/test/java/com/hypermall/user/controller/AuthControllerTest.java` - Controller tests
  - `user-service/src/test/resources/application-test.yml` - Test configuration with H2

**16.2 CI/CD** ✅
- `.github/workflows/ci.yml` - GitHub Actions workflow:
  - Frontend: lint, test with coverage, build
  - Backend: test with MySQL/Redis services, build
  - Docker build (on main branch)
  - Artifact upload

**16.3 Docker** ✅
- `infrastructure/docker/docker-compose.prod.yml` - Production docker-compose with health checks
- `backend/user-service/Dockerfile` - Multi-stage build for backend services
- `frontend/hypermall-web/Dockerfile` - Multi-stage build with nginx
- `frontend/hypermall-web/nginx.conf` - Nginx configuration with gzip, caching, SPA fallback

**16.4 Monitoring** ✅
- `infrastructure/docker/docker-compose.monitoring.yml` - Full monitoring stack
- **Prometheus**: `monitoring/prometheus.yml` - Metrics collection from all services
- **Grafana**: Provisioned dashboards, datasources
  - `monitoring/grafana/dashboards/spring-boot-overview.json` - JVM, requests, errors dashboard
- **AlertManager**: `monitoring/alertmanager.yml` - Alert routing and notifications
- **ELK Stack**: Elasticsearch, Logstash, Kibana
  - `monitoring/logstash/logstash.conf` - Log parsing and indexing

---

#### Phase 15: Admin Dashboard (11/03/2026) ✅

**15.1 Admin Layout & Navigation** ✅
- `frontend/hypermall-web/src/components/admin/AdminLayout.tsx`
- Responsive sidebar with mobile support
- Navigation: Dashboard, Users, Products, Orders, Sellers, Categories, Analytics, Settings
- Logout functionality

**15.2 Admin Pages** ✅
- **Dashboard** (`/admin`) - Overview stats, recent orders, charts
- **Users** (`/admin/users`) - User management with search, filter by status, role badges
- **Products** (`/admin/products`) - Product moderation, approve/reject pending products
- **Orders** (`/admin/orders`) - Order management, status updates
- **Sellers** (`/admin/sellers`) - Seller approval, stats summary, suspend/activate
- **Categories** (`/admin/categories`) - Category tree management, add/edit/toggle status
- **Analytics** (`/admin/analytics`) - Revenue charts, top products, conversion metrics
- **Settings** (`/admin/settings`) - Site settings, feature toggles, maintenance mode

**15.3 Features** ✅
- TailwindCSS styling with responsive design
- Mock data (ready for API integration)
- Search and filter functionality
- Status badges with color coding
- Action buttons (view, edit, delete, approve, reject)
- Statistics cards with trend indicators

---

## Notes

- Tất cả services đều có health check endpoint: `/actuator/health`
- JWT secret cần được thay đổi trong production
- Cần setup SSL/TLS cho production
- Database migration sử dụng Flyway (tùy chọn)
- Eureka credentials: `eureka/eureka123`
- Config Server credentials: `config/config123`
