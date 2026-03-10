#!/bin/bash
# HyperMall Development Stop Script for Linux/Mac

echo "============================================"
echo "   Stopping HyperMall Development"
echo "============================================"
echo ""

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "[1/2] Stopping Backend Services..."

# Kill Java processes (Spring Boot services)
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "service-registry" 2>/dev/null || true
pkill -f "config-server" 2>/dev/null || true
pkill -f "api-gateway" 2>/dev/null || true
pkill -f "user-service" 2>/dev/null || true
pkill -f "product-service" 2>/dev/null || true
pkill -f "cart-service" 2>/dev/null || true

# Kill Node.js (Frontend)
pkill -f "vite" 2>/dev/null || true

echo "     Done!"

echo ""
echo "[2/2] Stopping Infrastructure..."
cd "$PROJECT_ROOT/infrastructure/docker"
docker-compose -f docker-compose.dev.yml down

echo ""
echo "============================================"
echo "   All services stopped!"
echo "============================================"
echo ""
echo "To also remove data volumes, run:"
echo "  docker-compose -f docker-compose.dev.yml down -v"
