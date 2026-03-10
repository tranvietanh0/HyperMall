# HyperMall Deployment Guide

## Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Infrastructure Setup](#infrastructure-setup)
- [Backend Deployment](#backend-deployment)
- [Frontend Deployment](#frontend-deployment)
- [Verification](#verification)
- [Production Deployment](#production-deployment)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

| Software | Version | Download |
|----------|---------|----------|
| Docker Desktop | 24.x+ | https://www.docker.com/products/docker-desktop |
| Java JDK | 17 | https://adoptium.net/ |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |
| Node.js | 18+ | https://nodejs.org/ |

### Verify Installation

```bash
# Check all prerequisites
docker --version        # Docker version 24.x+
docker-compose --version # Docker Compose version 2.x+
java -version           # openjdk 17.x
mvn -version            # Apache Maven 3.8+
node -v                 # v18.x+
npm -v                  # 9.x+
```

### System Requirements

| Environment | RAM | CPU | Disk |
|-------------|-----|-----|------|
| Development | 8GB+ | 4 cores | 20GB |
| Production | 16GB+ | 8 cores | 100GB |

---

## Quick Start

For developers who want to get started quickly:

```bash
# 1. Start infrastructure
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d

# 2. Build backend
cd ../../backend
mvn clean install -DskipTests

# 3. Start services (each in separate terminal)
cd service-registry && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run

# 4. Start frontend
cd ../../frontend/hypermall-web
npm install
npm run dev
```

---

## Infrastructure Setup

### Development Environment

The development environment uses Docker Compose to run infrastructure services locally.

```bash
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

### Infrastructure Services

| Service | Container Name | Port(s) | Credentials |
|---------|---------------|---------|-------------|
| MySQL 8.0 | hypermall-mysql-dev | 3306 | root / root |
| Redis 7 | hypermall-redis-dev | 6379 | - |
| RabbitMQ 3 | hypermall-rabbitmq-dev | 5672, 15672 | guest / guest |
| Elasticsearch 8.12 | hypermall-elasticsearch-dev | 9200 | - |

### Verify Infrastructure

```bash
# Check running containers
docker ps

# Check MySQL
docker exec hypermall-mysql-dev mysqladmin ping -h localhost -u root -proot

# Check Redis
docker exec hypermall-redis-dev redis-cli ping

# Check Elasticsearch
curl http://localhost:9200

# Check RabbitMQ
curl -u guest:guest http://localhost:15672/api/overview
```

### Database Schema

MySQL databases are auto-created on first startup:

| Database | Service |
|----------|---------|
| hypermall_users | user-service |
| hypermall_products | product-service |
| hypermall_order | order-service |
| hypermall_payment | payment-service |
| hypermall_inventory | inventory-service |
| hypermall_promotion | promotion-service |
| hypermall_review | review-service |
| hypermall_notification | notification-service |
| hypermall_seller | seller-service |
| hypermall_media | media-service |
| hypermall_analytics | analytics-service |

---

## Backend Deployment

### Build All Services

```bash
cd backend
mvn clean install -DskipTests
```

### Service Startup Order

**IMPORTANT**: Services must be started in this exact order due to dependencies.

```
1. service-registry (Eureka Server)
       ↓
2. config-server (Configuration)
       ↓
3. api-gateway (API Router)
       ↓
4. Business services (user, product, cart, etc.)
```

### Starting Services

#### Option 1: Manual Start (Development)

Open separate terminals for each service:

```bash
# Terminal 1 - Service Registry
cd backend/service-registry
mvn spring-boot:run
# Wait for: "Started ServiceRegistryApplication"

# Terminal 2 - Config Server
cd backend/config-server
mvn spring-boot:run
# Wait for: "Started ConfigServerApplication"

# Terminal 3 - API Gateway
cd backend/api-gateway
mvn spring-boot:run
# Wait for: "Started ApiGatewayApplication"

# Terminal 4+ - Business Services
cd backend/user-service && mvn spring-boot:run
cd backend/product-service && mvn spring-boot:run
cd backend/cart-service && mvn spring-boot:run
```

#### Option 2: Using JAR files

```bash
# Build JARs
cd backend
mvn clean package -DskipTests

# Run JARs
java -jar service-registry/target/service-registry-1.0.0-SNAPSHOT.jar
java -jar config-server/target/config-server-1.0.0-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar
java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar
java -jar product-service/target/product-service-1.0.0-SNAPSHOT.jar
java -jar cart-service/target/cart-service-1.0.0-SNAPSHOT.jar
```

### Service Ports

| Service | Port | Health Check |
|---------|------|--------------|
| service-registry | 8761 | http://localhost:8761/actuator/health |
| config-server | 8888 | http://localhost:8888/actuator/health |
| api-gateway | 8080 | http://localhost:8080/actuator/health |
| user-service | 8081 | http://localhost:8081/actuator/health |
| product-service | 8082 | http://localhost:8082/actuator/health |
| cart-service | 8083 | http://localhost:8083/actuator/health |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_PROFILES_ACTIVE | dev | Active Spring profile |
| DB_USERNAME | root | MySQL username |
| DB_PASSWORD | root | MySQL password |
| REDIS_HOST | localhost | Redis host |
| REDIS_PORT | 6379 | Redis port |
| JWT_SECRET | (default key) | JWT signing secret |

---

## Frontend Deployment

### Development

```bash
cd frontend/hypermall-web

# Install dependencies
npm install

# Start development server
npm run dev
```

Development server runs at: **http://localhost:5173**

### Production Build

```bash
cd frontend/hypermall-web

# Build for production
npm run build

# Preview production build
npm run preview
```

Build output is in `dist/` folder.

### Environment Configuration

Create `.env` file from example:

```bash
cp .env.example .env
```

Edit `.env`:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=HyperMall
```

---

## Verification

### 1. Check Eureka Dashboard

Open http://localhost:8761 (credentials: eureka / eureka123)

You should see registered services:
- API-GATEWAY
- USER-SERVICE
- PRODUCT-SERVICE
- CART-SERVICE

### 2. Test API Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Get products (public)
curl http://localhost:8080/api/products

# Get categories (public)
curl http://localhost:8080/api/categories
```

### 3. Test Frontend

Open http://localhost:5173 in browser.

---

## Production Deployment

### Using Docker Compose (Full Stack)

```bash
cd infrastructure/docker
docker-compose up -d
```

This starts all infrastructure AND backend services in containers.

### Environment Variables for Production

Create `.env` file:

```env
# Database
DB_USERNAME=hypermall_user
DB_PASSWORD=<strong-password>

# JWT
JWT_SECRET=<256-bit-random-string>

# Redis
REDIS_PASSWORD=<redis-password>

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<email>
MAIL_PASSWORD=<app-password>
```

### Docker Build for Services

Each service can be containerized:

```dockerfile
# Example Dockerfile for user-service
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/user-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Nginx Configuration (Frontend)

```nginx
server {
    listen 80;
    server_name hypermall.com;

    root /var/www/hypermall/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### SSL/HTTPS (Let's Encrypt)

```bash
# Install certbot
sudo apt install certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d hypermall.com -d www.hypermall.com
```

---

## Troubleshooting

### Common Issues

#### Port Already in Use

```bash
# Windows - Find process
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

#### MySQL Connection Failed

```bash
# Check MySQL is running
docker ps | grep mysql

# Check logs
docker logs hypermall-mysql-dev

# Wait for MySQL to be ready (30s after start)
docker exec hypermall-mysql-dev mysqladmin ping -h localhost -u root -proot
```

#### Service Not Registering with Eureka

1. Ensure `service-registry` is running first
2. Check network connectivity
3. Verify Eureka URL in application.yml:
   ```yaml
   eureka:
     client:
       service-url:
         defaultZone: http://eureka:eureka123@localhost:8761/eureka/
   ```

#### Redis Connection Failed

```bash
# Check Redis is running
docker exec hypermall-redis-dev redis-cli ping
# Expected: PONG
```

#### Out of Memory

Increase Docker memory allocation:
- Docker Desktop > Settings > Resources > Memory > 8GB+

Or reduce Elasticsearch memory:
```yaml
# docker-compose.dev.yml
elasticsearch:
  environment:
    - "ES_JAVA_OPTS=-Xms256m -Xmx256m"
```

### Logs

```bash
# Docker container logs
docker logs -f hypermall-mysql-dev
docker logs -f hypermall-redis-dev

# Spring Boot logs
# Check console output or:
tail -f backend/user-service/logs/app.log
```

### Reset Everything

```bash
# Stop all services (Ctrl+C in each terminal)

# Stop and remove containers + volumes
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml down -v

# Clean Maven build
cd backend
mvn clean

# Clean node_modules
cd frontend/hypermall-web
rm -rf node_modules
npm install
```

---

## Support

- GitHub Issues: https://github.com/your-repo/hypermall/issues
- Documentation: https://github.com/your-repo/hypermall/wiki
