@echo off
REM ═══════════════════════════════════════════════════════════════════════════
REM YOUTH CONNECT PLATFORM - Auth Service Startup Script (FIXED)
REM ═══════════════════════════════════════════════════════════════════════════
REM FIXED: Proper environment variable loading and error handling
REM ═══════════════════════════════════════════════════════════════════════════

setlocal EnableDelayedExpansion

REM Colors
set "COLOR_GREEN=[92m"
set "COLOR_RED=[91m"
set "COLOR_YELLOW=[93m"
set "COLOR_CYAN=[96m"
set "COLOR_RESET=[0m"

echo.
echo %COLOR_CYAN%════════════════════════════════════════════════════════════════%COLOR_RESET%
echo %COLOR_CYAN%  Entrepreneurship Booster Platform - Auth Service Startup%COLOR_RESET%
echo %COLOR_CYAN%════════════════════════════════════════════════════════════════%COLOR_RESET%
echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM PRE-FLIGHT CHECKS
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%Step 1: Pre-flight checks...%COLOR_RESET%

REM Check Java installation
java -version >nul 2>&1
if errorlevel 1 (
    echo %COLOR_RED%ERROR: Java not found. Please install Java 17+%COLOR_RESET%
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ Java installed%COLOR_RESET%

REM Check PostgreSQL connection
psql -U youthconnect_user -d youthconnect_auth -c "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo %COLOR_RED%ERROR: Cannot connect to PostgreSQL%COLOR_RESET%
    echo   Database: youthconnect_auth
    echo   User: youthconnect_user
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ PostgreSQL connection OK%COLOR_RESET%

REM Check Redis connection
redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo %COLOR_YELLOW%WARNING: Redis not responding (will use fallback)%COLOR_RESET%
) else (
    echo %COLOR_GREEN%✓ Redis connection OK%COLOR_RESET%
)

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM LOAD ENVIRONMENT VARIABLES
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%Step 2: Loading environment variables...%COLOR_RESET%

if exist ".env" (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%b"=="" (
            set "%%a=%%b"
        )
    )
    echo %COLOR_GREEN%✓ Environment variables loaded from .env%COLOR_RESET%
) else (
    echo %COLOR_YELLOW%WARNING: .env file not found, using default values%COLOR_RESET%
)

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM BUILD SERVICE
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%Step 3: Building Auth Service...%COLOR_RESET%

if not exist "target\auth-service.jar" (
    echo Building service...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo %COLOR_RED%ERROR: Build failed%COLOR_RESET%
        pause
        exit /b 1
    )
    echo %COLOR_GREEN%✓ Build completed%COLOR_RESET%
) else (
    echo %COLOR_GREEN%✓ JAR file already exists%COLOR_RESET%
)

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM START SERVICE
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%Step 4: Starting Auth Service...%COLOR_RESET%

REM Set default values if not in .env
if not defined SPRING_PROFILES_ACTIVE set SPRING_PROFILES_ACTIVE=local
if not defined DB_HOST set DB_HOST=localhost
if not defined DB_PORT set DB_PORT=5432
if not defined DB_NAME set DB_NAME=youthconnect_auth
if not defined DB_USER set DB_USER=youthconnect_user
if not defined DB_PASSWORD set DB_PASSWORD=YouthConnect2024!

echo Starting with profile: %SPRING_PROFILES_ACTIVE%
echo Database: %DB_HOST%:%DB_PORT%/%DB_NAME%
echo.

REM Start the service
java -Dspring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
     -Dspring.datasource.url=jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME% ^
     -Dspring.datasource.username=%DB_USER% ^
     -Dspring.datasource.password=%DB_PASSWORD% ^
     -jar target\auth-service.jar

pause