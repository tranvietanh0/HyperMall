@echo off
REM HyperMall Development Startup Script for Windows
REM This script starts all infrastructure and backend services

echo ============================================
echo    HyperMall Development Environment
echo ============================================
echo.

REM Check Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

REM Navigate to project root
cd /d "%~dp0\.."
set PROJECT_ROOT=%cd%

echo [1/6] Starting Infrastructure (MySQL, Redis, RabbitMQ, Elasticsearch)...
cd "%PROJECT_ROOT%\infrastructure\docker"
docker-compose -f docker-compose.dev.yml up -d

echo.
echo [2/6] Waiting for MySQL to be ready...
:wait_mysql
docker exec hypermall-mysql-dev mysqladmin ping -h localhost -u root -proot >nul 2>&1
if errorlevel 1 (
    echo      Waiting for MySQL...
    timeout /t 5 /nobreak >nul
    goto wait_mysql
)
echo      MySQL is ready!

echo.
echo [3/6] Building Backend Services...
cd "%PROJECT_ROOT%\backend"
call mvn clean install -DskipTests -q

if errorlevel 1 (
    echo [ERROR] Maven build failed!
    pause
    exit /b 1
)

echo.
echo [4/6] Starting Backend Services...
echo      Starting services in new windows...
echo.

REM Start Service Registry
start "Service Registry (8761)" cmd /k "cd /d %PROJECT_ROOT%\backend\service-registry && mvn spring-boot:run"
echo      - Service Registry starting on port 8761
timeout /t 15 /nobreak >nul

REM Start Config Server
start "Config Server (8888)" cmd /k "cd /d %PROJECT_ROOT%\backend\config-server && mvn spring-boot:run"
echo      - Config Server starting on port 8888
timeout /t 10 /nobreak >nul

REM Start API Gateway
start "API Gateway (8080)" cmd /k "cd /d %PROJECT_ROOT%\backend\api-gateway && mvn spring-boot:run"
echo      - API Gateway starting on port 8080
timeout /t 10 /nobreak >nul

REM Start User Service
start "User Service (8081)" cmd /k "cd /d %PROJECT_ROOT%\backend\user-service && mvn spring-boot:run"
echo      - User Service starting on port 8081

REM Start Product Service
start "Product Service (8082)" cmd /k "cd /d %PROJECT_ROOT%\backend\product-service && mvn spring-boot:run"
echo      - Product Service starting on port 8082

REM Start Cart Service
start "Cart Service (8083)" cmd /k "cd /d %PROJECT_ROOT%\backend\cart-service && mvn spring-boot:run"
echo      - Cart Service starting on port 8083

echo.
echo [5/6] Starting Frontend...
cd "%PROJECT_ROOT%\frontend\hypermall-web"
if not exist node_modules (
    echo      Installing npm dependencies...
    call npm install
)
start "Frontend (5173)" cmd /k "cd /d %PROJECT_ROOT%\frontend\hypermall-web && npm run dev"
echo      - Frontend starting on port 5173

echo.
echo ============================================
echo [6/6] All services are starting!
echo ============================================
echo.
echo Access URLs:
echo   - Frontend:        http://localhost:5173
echo   - API Gateway:     http://localhost:8080
echo   - Eureka:          http://localhost:8761 (eureka/eureka123)
echo   - RabbitMQ:        http://localhost:15672 (guest/guest)
echo.
echo Note: Services may take 1-2 minutes to fully start.
echo       Check Eureka dashboard to verify all services are registered.
echo.
pause
