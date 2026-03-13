# HyperMall - E-Commerce Platform

A comprehensive microservices-based e-commerce platform inspired by Shopee and Lazada, built with Spring Cloud (backend) and React (frontend).

## Overview

HyperMall is a full-featured online marketplace supporting:
- Multi-vendor seller portals
- Advanced product search (Elasticsearch)
- Multiple payment methods (VNPay, MoMo, ZaloPay, COD)
- Multiple shipping carriers (GHN, GHTK, ViettelPost)
- Real-time inventory management
- Product reviews and ratings
- Vouchers and flash sales
- Order tracking

## Tech Stack

### Frontend
- React 18 + TypeScript
- Vite (build tool)
- TailwindCSS (styling)
- Redux Toolkit (state management)
- React Router DOM 6
- Formik + Yup (forms)
- Vitest (testing)

### Backend
- Spring Boot 3.4.3 (Java 17)
- Spring Cloud 2024.0.0
- Netflix Eureka (service discovery)
- Spring Cloud Config (configuration)
- Spring Cloud Gateway (API gateway)

### Infrastructure
- MySQL 8.0 (databases)
- Redis 7 (caching, sessions, cart)
- RabbitMQ 3 (message queue)
- Elasticsearch 8.x (search)
- Docker & Docker Compose

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker Desktop

### 1. Start Infrastructure
```bash
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

### 2. Start Backend Services
```bash
# Build all services
cd backend
mvn clean install -DskipTests

# Start services in order (separate terminals)
cd service-registry && mvn spring-boot:run     # Port 8761
cd config-server && mvn spring-boot:run         # Port 8888
cd api-gateway && mvn spring-boot:run          # Port 8080
cd user-service && mvn spring-boot:run         # Port 8081
cd product-service && mvn spring-boot:run      # Port 8082
cd cart-service && mvn spring-boot:run         # Port 8083
```

### 3. Start Frontend
```bash
cd frontend/hypermall-web
npm install
npm run dev
```

Access the application:
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080/api
- Eureka Dashboard: http://localhost:8761 (eureka/eureka123)

## Project Structure

```
HyperMall/
├── backend/                    # 18 microservices
│   ├── common-lib/            # Shared library
│   ├── service-registry/      # Eureka Server
│   ├── config-server/         # Config Server
│   ├── api-gateway/           # API Gateway
│   ├── user-service/          # Auth & Users
│   ├── product-service/       # Products & Categories
│   ├── cart-service/          # Shopping Cart (Redis)
│   ├── order-service/         # Orders
│   ├── payment-service/       # Payments
│   ├── inventory-service/     # Stock Management
│   ├── shipping-service/      # Shipping Integration
│   ├── promotion-service/     # Vouchers & Flash Sales
│   ├── review-service/        # Reviews & Ratings
│   ├── search-service/        # Elasticsearch
│   ├── notification-service/  # Notifications
│   ├── media-service/         # File Uploads
│   ├── seller-service/        # Seller Portal
│   └── analytics-service/    # Analytics
├── frontend/
│   └── hypermall-web/         # React frontend
├── infrastructure/
│   └── docker/               # Docker Compose
├── docs/                      # Documentation
└── scripts/                   # Utility scripts
```

## Service Ports

| Service | Port | Database |
|---------|------|----------|
| service-registry | 8761 | N/A |
| config-server | 8888 | N/A |
| api-gateway | 8080 | Redis |
| user-service | 8081 | MySQL |
| product-service | 8082 | MySQL |
| cart-service | 8083 | Redis |
| order-service | 8084 | MySQL |
| payment-service | 8085 | MySQL |
| inventory-service | 8086 | MySQL |
| shipping-service | 8087 | MySQL |
| promotion-service | 8088 | MySQL |
| review-service | 8089 | MySQL |
| search-service | 8090 | Elasticsearch |
| notification-service | 8091 | MySQL |

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

### Products
- `GET /api/products` - List products
- `GET /api/products/{id}` - Product details
- `GET /api/categories` - List categories

### Cart
- `GET /api/cart` - Get cart
- `POST /api/cart/items` - Add to cart

### Orders
- `POST /api/orders` - Create order
- `GET /api/orders` - Order history

See [API Documentation](./docs/API.md) for complete endpoint reference.

## Key Features

### User Features
- User registration and authentication
- Product browsing with filters
- Shopping cart management
- Order placement and tracking
- Product reviews and ratings

### Seller Features
- Seller registration
- Product management
- Order fulfillment
- Sales analytics

### Admin Features
- Category and brand management
- User management
- Platform analytics

## Documentation

- [API Documentation](./docs/API.md)
- [Deployment Guide](./docs/DEPLOYMENT.md)
- [Local Testing Guide](./docs/LOCAL_TESTING.md)
- [Project Overview & PDR](./docs/project-overview-pdr.md)
- [Code Standards](./docs/code-standards.md)
- [System Architecture](./docs/system-architecture.md)
- [Codebase Summary](./docs/codebase-summary.md)

## Security

- JWT authentication (15 min access, 7 day refresh)
- Role-based access control (USER, SELLER, ADMIN)
- Rate limiting (100-500 req/min)
- Password hashing with BCrypt

## Testing

### Backend
```bash
cd backend
mvn test

# Single service
cd backend/user-service
mvn test -Dtest=AuthServiceTest
```

### Frontend
```bash
cd frontend/hypermall-web

# Watch mode
npm run test

# Single run
npx vitest run

# Coverage
npm run test:coverage
```

## Configuration

### Environment Variables
```env
DB_USERNAME=root
DB_PASSWORD=root
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=<base64-encoded-secret>
```

## Contributing

1. Fork the repository
2. Create a feature branch: `feature/TICKET-description`
3. Make your changes
4. Add tests
5. Submit a pull request

## License

Private - All rights reserved

## Support

- Documentation: See `/docs` folder
- Issues: Create GitHub issue
