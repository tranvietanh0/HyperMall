# Hướng Dẫn Test Local - HyperMall

## Mục Lục
1. [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
2. [Khởi Động Infrastructure](#khởi-động-infrastructure)
3. [Chạy Backend Services](#chạy-backend-services)
4. [Chạy Frontend](#chạy-frontend)
5. [Chạy Tests](#chạy-tests)
6. [Test API với cURL/Postman](#test-api)
7. [Troubleshooting](#troubleshooting)

---

## Yêu Cầu Hệ Thống

### Bắt buộc
- **Java 17+**: `java -version`
- **Maven 3.8+**: `mvn -version`
- **Node.js 18+**: `node -v`
- **Docker & Docker Compose**: `docker --version`

### Cài đặt nhanh (Windows)
```powershell
# Dùng Chocolatey
choco install openjdk17 maven nodejs docker-desktop
```

### Cài đặt nhanh (macOS)
```bash
brew install openjdk@17 maven node docker
```

---

## Khởi Động Infrastructure

### 1. Start Docker containers

```bash
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

### 2. Kiểm tra containers đang chạy

```bash
docker ps
```

**Expected output:**
| Container | Port | Status |
|-----------|------|--------|
| hypermall-mysql-dev | 3306 | Up |
| hypermall-redis-dev | 6379 | Up |
| hypermall-rabbitmq-dev | 5672, 15672 | Up |
| hypermall-elasticsearch-dev | 9200 | Up |

### 3. Kiểm tra kết nối

```bash
# MySQL
docker exec -it hypermall-mysql-dev mysql -uroot -proot -e "SHOW DATABASES;"

# Redis
docker exec -it hypermall-redis-dev redis-cli ping
# Expected: PONG

# RabbitMQ Management UI
# Mở browser: http://localhost:15672 (guest/guest)

# Elasticsearch
curl http://localhost:9200
```

---

## Chạy Backend Services

### Thứ tự khởi động (QUAN TRỌNG!)

```
1. service-registry (Eureka)
2. config-server
3. api-gateway
4. Các business services khác
```

### Option 1: Chạy từng service (Development)

**Terminal 1 - Service Registry:**
```bash
cd backend/service-registry
mvn spring-boot:run
# Đợi thấy: "Started ServiceRegistryApplication"
# Mở http://localhost:8761 để xem Eureka Dashboard
```

**Terminal 2 - Config Server:**
```bash
cd backend/config-server
mvn spring-boot:run
# Đợi thấy: "Started ConfigServerApplication"
```

**Terminal 3 - API Gateway:**
```bash
cd backend/api-gateway
mvn spring-boot:run
# Đợi thấy: "Started ApiGatewayApplication"
```

**Terminal 4 - User Service:**
```bash
cd backend/user-service
mvn spring-boot:run
```

**Terminal 5 - Product Service:**
```bash
cd backend/product-service
mvn spring-boot:run
```

**Thêm services khác tùy nhu cầu test...**

### Option 2: Build tất cả trước

```bash
cd backend
mvn clean install -DskipTests

# Sau đó chạy từng service với java -jar
java -jar service-registry/target/*.jar &
java -jar config-server/target/*.jar &
# ... đợi mỗi service start xong rồi chạy tiếp
```

### Kiểm tra services đã register

Mở **Eureka Dashboard**: http://localhost:8761

Sẽ thấy danh sách services đã đăng ký:
- API-GATEWAY
- USER-SERVICE
- PRODUCT-SERVICE
- ...

---

## Chạy Frontend

### 1. Install dependencies

```bash
cd frontend/hypermall-web
npm install
```

### 2. Start development server

```bash
npm run dev
```

### 3. Mở browser

```
http://localhost:3000
```

Frontend sẽ tự động proxy requests `/api/*` đến `http://localhost:8080` (API Gateway).

---

## Chạy Tests

### Backend Tests

```bash
# Chạy tất cả tests
cd backend
mvn test

# Chạy test cho 1 service cụ thể
cd backend/user-service
mvn test

# Chạy 1 test class
mvn test -Dtest=AuthServiceTest

# Chạy 1 test method
mvn test -Dtest=AuthServiceTest#register_WithValidData_ShouldReturnAuthResponse

# Chạy với coverage report
mvn test jacoco:report
# Report tại: target/site/jacoco/index.html
```

### Frontend Tests

```bash
cd frontend/hypermall-web

# Chạy tests (watch mode)
npm run test

# Chạy tests 1 lần
npx vitest run

# Chạy với coverage
npm run test:coverage
# Report tại: coverage/index.html

# Chạy 1 file test cụ thể
npx vitest run src/utils/format.test.ts

# Chạy tests matching pattern
npx vitest run -t "formatCurrency"
```

---

## Test API

### Sử dụng cURL

#### Health Check
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service (qua Gateway)
curl http://localhost:8080/api/users/actuator/health
```

#### Authentication

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password1@",
    "fullName": "Test User",
    "phone": "0987654321"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password1@"
  }'
```

**Response sẽ trả về:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "test@example.com",
      "fullName": "Test User"
    }
  }
}
```

#### Authenticated Requests

```bash
# Lưu token vào biến
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Get current user
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"

# Get products
curl http://localhost:8080/api/products

# Get categories
curl http://localhost:8080/api/categories/tree
```

### Sử dụng Postman

1. Import collection từ Swagger:
   - Mở: `http://localhost:8081/swagger-ui.html` (User Service)
   - Mở: `http://localhost:8082/swagger-ui.html` (Product Service)

2. Hoặc tạo collection thủ công với base URL: `http://localhost:8080/api`

### Swagger UI URLs

| Service | Swagger URL |
|---------|-------------|
| User Service | http://localhost:8081/swagger-ui.html |
| Product Service | http://localhost:8082/swagger-ui.html |
| Cart Service | http://localhost:8083/swagger-ui.html |
| Order Service | http://localhost:8084/swagger-ui.html |
| Payment Service | http://localhost:8085/swagger-ui.html |
| Inventory Service | http://localhost:8086/swagger-ui.html |
| Shipping Service | http://localhost:8087/swagger-ui.html |
| Promotion Service | http://localhost:8088/swagger-ui.html |
| Review Service | http://localhost:8089/swagger-ui.html |
| Search Service | http://localhost:8090/swagger-ui.html |
| Notification Service | http://localhost:8091/swagger-ui.html |
| Media Service | http://localhost:8093/swagger-ui.html |
| Seller Service | http://localhost:8094/swagger-ui.html |
| Analytics Service | http://localhost:8095/swagger-ui.html |

---

## Troubleshooting

### Service không start được

**Lỗi: "Connection refused" đến MySQL/Redis**
```bash
# Kiểm tra Docker containers
docker ps

# Restart containers nếu cần
docker-compose -f infrastructure/docker/docker-compose.dev.yml restart
```

**Lỗi: "Config Server not available"**
```bash
# Đảm bảo config-server đã start trước
curl http://localhost:8888/actuator/health

# Nếu chưa, start lại theo đúng thứ tự:
# 1. service-registry → 2. config-server → 3. api-gateway → 4. other services
```

**Lỗi: Port already in use**
```bash
# Windows - tìm process dùng port
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Database issues

**Reset database:**
```bash
# Xóa và tạo lại containers
docker-compose -f infrastructure/docker/docker-compose.dev.yml down -v
docker-compose -f infrastructure/docker/docker-compose.dev.yml up -d
```

**Xem logs MySQL:**
```bash
docker logs hypermall-mysql-dev
```

### Frontend issues

**Lỗi: "CORS error"**
- Đảm bảo API Gateway đang chạy
- Kiểm tra `vite.config.ts` có proxy config đúng

**Lỗi: "Network Error"**
```bash
# Kiểm tra API Gateway
curl http://localhost:8080/actuator/health
```

### Xem logs service

```bash
# Backend logs đang chạy với mvn
# Logs hiển thị trực tiếp trong terminal

# Hoặc chạy với nohup và xem log file
cd backend/user-service
nohup mvn spring-boot:run > user-service.log 2>&1 &
tail -f user-service.log
```

---

## Quick Start Script

Tạo file `start-local.sh` (Linux/Mac) hoặc `start-local.bat` (Windows):

### Linux/Mac: start-local.sh
```bash
#!/bin/bash

echo "Starting infrastructure..."
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
cd ../..

echo "Waiting for MySQL to be ready..."
sleep 10

echo "Starting Service Registry..."
cd backend/service-registry
mvn spring-boot:run &
sleep 15

echo "Starting Config Server..."
cd ../config-server
mvn spring-boot:run &
sleep 10

echo "Starting API Gateway..."
cd ../api-gateway
mvn spring-boot:run &
sleep 10

echo "Starting User Service..."
cd ../user-service
mvn spring-boot:run &

echo "Starting Product Service..."
cd ../product-service
mvn spring-boot:run &

echo "All services starting. Check Eureka at http://localhost:8761"
```

### Windows: start-local.bat
```batch
@echo off

echo Starting infrastructure...
cd infrastructure\docker
docker-compose -f docker-compose.dev.yml up -d
cd ..\..

echo Waiting for MySQL...
timeout /t 10

echo Starting Service Registry...
start cmd /k "cd backend\service-registry && mvn spring-boot:run"
timeout /t 15

echo Starting Config Server...
start cmd /k "cd backend\config-server && mvn spring-boot:run"
timeout /t 10

echo Starting API Gateway...
start cmd /k "cd backend\api-gateway && mvn spring-boot:run"
timeout /t 10

echo Starting User Service...
start cmd /k "cd backend\user-service && mvn spring-boot:run"

echo Starting Product Service...
start cmd /k "cd backend\product-service && mvn spring-boot:run"

echo.
echo All services starting!
echo Eureka Dashboard: http://localhost:8761
echo API Gateway: http://localhost:8080
echo.
pause
```

---

## Useful Commands Cheatsheet

```bash
# Infrastructure
docker-compose -f infrastructure/docker/docker-compose.dev.yml up -d    # Start
docker-compose -f infrastructure/docker/docker-compose.dev.yml down     # Stop
docker-compose -f infrastructure/docker/docker-compose.dev.yml logs -f  # View logs

# Backend
cd backend && mvn clean install -DskipTests  # Build all
cd backend/<service> && mvn spring-boot:run  # Run service
cd backend/<service> && mvn test             # Run tests

# Frontend
cd frontend/hypermall-web && npm install     # Install deps
cd frontend/hypermall-web && npm run dev     # Start dev server
cd frontend/hypermall-web && npm run test    # Run tests
cd frontend/hypermall-web && npm run build   # Build production

# Health checks
curl http://localhost:8761/actuator/health   # Eureka
curl http://localhost:8888/actuator/health   # Config Server
curl http://localhost:8080/actuator/health   # API Gateway
```

---

## Kết Luận

1. **Start infrastructure** với Docker Compose
2. **Start services** theo thứ tự: registry → config → gateway → business services
3. **Start frontend** với `npm run dev`
4. **Test APIs** qua `http://localhost:8080/api/*`
5. **Xem Swagger** tại `http://localhost:{port}/swagger-ui.html`

Nếu gặp vấn đề, kiểm tra:
- Docker containers đang chạy
- Services đã register trên Eureka
- Logs của từng service
