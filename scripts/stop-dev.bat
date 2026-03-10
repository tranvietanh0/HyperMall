@echo off
REM HyperMall Development Stop Script for Windows

echo ============================================
echo    Stopping HyperMall Development
echo ============================================
echo.

REM Navigate to project root
cd /d "%~dp0\.."
set PROJECT_ROOT=%cd%

echo [1/2] Stopping Backend Services...
echo      Closing all service windows...

REM Kill Java processes (Spring Boot services)
taskkill /F /FI "WINDOWTITLE eq Service Registry*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Config Server*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq API Gateway*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq User Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Product Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Cart Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Frontend*" >nul 2>&1

echo      Done!

echo.
echo [2/2] Stopping Infrastructure...
cd "%PROJECT_ROOT%\infrastructure\docker"
docker-compose -f docker-compose.dev.yml down

echo.
echo ============================================
echo    All services stopped!
echo ============================================
echo.
echo To also remove data volumes, run:
echo   docker-compose -f docker-compose.dev.yml down -v
echo.
pause
