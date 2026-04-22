#!/bin/bash
# HyperMall Development Startup Script for Linux/Mac

set -e

echo "============================================"
echo "   HyperMall Development Environment"
echo "============================================"
echo ""

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

ENV_FILE="$PROJECT_ROOT/backend/.env"
if [ ! -f "$ENV_FILE" ]; then
    echo "[ERROR] Missing env file: $ENV_FILE"
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

if [ -z "$SPRING_PROFILES_ACTIVE" ] || [ -z "$JWT_SECRET" ]; then
    echo "[ERROR] backend/.env must define SPRING_PROFILES_ACTIVE and JWT_SECRET"
    exit 1
fi

export EUREKA_USERNAME="${EUREKA_USERNAME:-eureka}"
export EUREKA_PASSWORD="${EUREKA_PASSWORD:-eureka123}"
export CONFIG_USERNAME="${CONFIG_USERNAME:-config}"
export CONFIG_PASSWORD="${CONFIG_PASSWORD:-config123}"
export CONFIG_SERVER_USERNAME="${CONFIG_SERVER_USERNAME:-$CONFIG_USERNAME}"
export CONFIG_SERVER_PASSWORD="${CONFIG_SERVER_PASSWORD:-$CONFIG_PASSWORD}"

# Check Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "[ERROR] Docker daemon is not running. Please start Docker Desktop/daemon first."
    exit 1
fi

echo "[1/6] Starting Infrastructure (MySQL, Redis, RabbitMQ, Elasticsearch)..."
cd "$PROJECT_ROOT/infrastructure/docker"
docker-compose -f docker-compose.dev.yml up -d

echo ""
echo "[2/6] Waiting for MySQL to be ready..."
until docker exec hypermall-mysql-dev mysqladmin ping -h localhost -u root -proot > /dev/null 2>&1; do
    echo "     Waiting for MySQL..."
    sleep 5
done
echo "     MySQL is ready!"

echo ""
echo "[3/6] Building Backend Services..."
cd "$PROJECT_ROOT/backend"
mvn clean install -DskipTests -q

echo ""
echo "[4/6] Starting Backend Services..."

# Create logs directory
mkdir -p "$PROJECT_ROOT/logs"

# Start Service Registry
echo "     - Starting Service Registry on port 8761..."
cd "$PROJECT_ROOT/backend/service-registry"
nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/service-registry.log" 2>&1 &
sleep 15

# Start Config Server
echo "     - Starting Config Server on port 8888..."
cd "$PROJECT_ROOT/backend/config-server"
nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/config-server.log" 2>&1 &
sleep 10

# Start API Gateway
echo "     - Starting API Gateway on port 8080..."
cd "$PROJECT_ROOT/backend/api-gateway"
nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/api-gateway.log" 2>&1 &
sleep 10

# Start User Service
echo "     - Starting User Service on port 8081..."
cd "$PROJECT_ROOT/backend/user-service"
nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/user-service.log" 2>&1 &

# Start Product Service
echo "     - Starting Product Service on port 8082..."
cd "$PROJECT_ROOT/backend/product-service"
nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/product-service.log" 2>&1 &

# Start Cart Service
echo "     - Starting Cart Service on port 8083..."
cd "$PROJECT_ROOT/backend/cart-service"
nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/cart-service.log" 2>&1 &

echo ""
echo "[5/6] Starting Frontend..."
cd "$PROJECT_ROOT/frontend/hypermall-web"
if [ ! -d "node_modules" ]; then
    echo "     Installing npm dependencies..."
    npm install
fi
nohup npm run dev > "$PROJECT_ROOT/logs/frontend.log" 2>&1 &

echo ""
echo "============================================"
echo "[6/6] All services are starting!"
echo "============================================"
echo ""
echo "Access URLs:"
echo "  - App Entry:       http://localhost:3000"
echo "  - API Gateway:     http://localhost:8080 (internal debug/proxy target)"
echo "  - Eureka:          http://localhost:8761 (eureka/eureka123)"
echo "  - RabbitMQ:        http://localhost:15672 (guest/guest)"
echo ""
echo "Logs are saved in: $PROJECT_ROOT/logs/"
echo ""
echo "Note: Services may take 1-2 minutes to fully start."
echo "      Check Eureka dashboard to verify all services are registered."
echo ""
echo "To stop all services, run: ./scripts/stop-dev.sh"
