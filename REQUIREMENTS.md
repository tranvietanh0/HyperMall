# HyperMall - Requirements

## 1. Công cụ cần cài đặt (Tools to Install)

### Java Development Kit (JDK) 17
- **Phiên bản**: JDK 17 (LTS)
- **Tải về**: https://adoptium.net/ (Eclipse Temurin) hoặc https://www.oracle.com/java/technologies/downloads/#java17
- **Kiểm tra**: `java -version` → phải hiện `openjdk 17.x.x` hoặc `java 17.x.x`

### Apache Maven 3.9+
- **Phiên bản**: 3.9.x trở lên
- **Tải về**: https://maven.apache.org/download.cgi
- **Kiểm tra**: `mvn -version` → phải hiện `Apache Maven 3.9.x`
- **Lưu ý**: Thêm `MAVEN_HOME` vào biến môi trường và `%MAVEN_HOME%\bin` vào PATH

### Node.js 20 LTS
- **Phiên bản**: 20.x LTS (Long-Term Support)
- **Tải về**: https://nodejs.org/en/download/
- **Kiểm tra**:
  - `node -version` → phải hiện `v20.x.x`
  - `npm -version` → phải hiện `10.x.x`

### Docker Desktop
- **Phiên bản**: Docker Desktop mới nhất
- **Tải về**: https://www.docker.com/products/docker-desktop/
- **Kiểm tra**: `docker -version` và `docker compose version`
- **Lưu ý**: Bật WSL2 backend trên Windows để tăng hiệu năng

---

## 2. Infrastructure (chạy qua Docker)

Docker sẽ tự tải các image sau, không cần cài thủ công:

| Service        | Image                                   | Port  | Mục đích                |
|----------------|-----------------------------------------|-------|-------------------------|
| MySQL 8.0      | `mysql:8.0`                             | 3306  | Cơ sở dữ liệu chính     |
| Redis 7        | `redis:7-alpine`                        | 6379  | Cache, JWT token storage|
| RabbitMQ 3     | `rabbitmq:3-management-alpine`          | 5672, 15672 | Message broker    |
| Elasticsearch 8| `docker.elastic.co/elasticsearch:8.12.0`| 9200  | Full-text search        |

---

## 3. Frontend Dependencies (npm)

Được quản lý bởi `frontend/hypermall-web/package.json`, chạy `npm install` để cài.

### Runtime Dependencies
| Package               | Version  | Mục đích                        |
|-----------------------|----------|---------------------------------|
| react                 | ^18.2.0  | UI library                      |
| react-dom             | ^18.2.0  | React DOM rendering             |
| react-router-dom      | ^6.22.1  | Client-side routing             |
| @reduxjs/toolkit      | ^2.2.1   | State management                |
| react-redux           | ^9.1.0   | React-Redux binding             |
| zustand               | ^4.5.1   | Lightweight state management    |
| axios                 | ^1.6.7   | HTTP client                     |
| formik                | ^2.4.5   | Form handling                   |
| yup                   | ^1.3.3   | Form validation schema          |
| @headlessui/react     | ^2.0.0   | Accessible UI components        |
| @heroicons/react      | ^2.1.1   | Icon library                    |
| react-hot-toast       | ^2.4.1   | Toast notifications             |
| swiper                | ^11.0.6  | Slider/Carousel                 |
| date-fns              | ^3.3.1   | Date utility                    |
| clsx                  | ^2.1.0   | Conditional classNames          |

### Dev Dependencies
| Package                        | Version  | Mục đích                    |
|--------------------------------|----------|-----------------------------|
| vite                           | ^5.1.3   | Build tool & dev server     |
| typescript                     | ^5.3.3   | TypeScript compiler         |
| tailwindcss                    | ^3.4.1   | CSS utility framework       |
| @vitejs/plugin-react           | ^4.2.1   | Vite React plugin           |
| eslint                         | ^8.56.0  | Code linting                |
| vitest                         | ^1.3.1   | Unit testing framework      |
| @testing-library/react         | ^14.2.1  | React testing utilities     |
| postcss + autoprefixer         | latest   | CSS processing              |

---

## 4. Backend Dependencies (Maven)

Được quản lý bởi Maven, tự tải khi chạy `mvn clean install`.

### Spring Boot & Cloud
| Dependency                   | Version      |
|------------------------------|--------------|
| Spring Boot                  | 3.4.3        |
| Spring Cloud                 | 2024.0.0     |
| Spring Web (MVC/WebFlux)     | via Boot     |
| Spring Security              | via Boot     |
| Spring Data JPA              | via Boot     |
| Spring Data Redis            | via Boot     |
| Spring Cloud Gateway         | via Cloud    |
| Spring Cloud Netflix Eureka  | via Cloud    |
| Spring Cloud Config          | via Cloud    |

### Libraries
| Dependency       | Version      | Mục đích              |
|------------------|--------------|-----------------------|
| Lombok           | 1.18.30      | Boilerplate reduction |
| MapStruct        | 1.5.5.Final  | DTO mapping           |
| JJWT (JWT)       | 0.12.5       | JWT token handling    |
| SpringDoc OpenAPI| 2.3.0        | Swagger UI / API docs |
| MySQL Connector  | via Boot     | MySQL driver          |

---

## 5. IDE (Khuyến nghị)

| IDE                  | Mục đích       | Download |
|----------------------|----------------|----------|
| IntelliJ IDEA (Community hoặc Ultimate) | Backend Java | https://www.jetbrains.com/idea/ |
| VS Code              | Frontend TypeScript | https://code.visualstudio.com/ |

### VS Code Extensions khuyến nghị
- ESLint
- Prettier
- Tailwind CSS IntelliSense
- TypeScript + JavaScript Language Features (built-in)

### IntelliJ Plugins khuyến nghị
- Lombok (built-in hoặc plugin)
- Spring Boot (built-in trong Ultimate)

---

## 6. Tóm tắt cài đặt nhanh

```bash
# 1. Kiểm tra đã cài đủ chưa
java -version      # → 17.x.x
mvn -version       # → 3.9.x
node -version      # → 20.x.x
npm -version       # → 10.x.x
docker -version    # → 27.x.x

# 2. Cài frontend dependencies
cd frontend/hypermall-web
npm install

# 3. Build backend
cd backend
mvn clean install -DskipTests

# 4. Khởi động infrastructure
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```
