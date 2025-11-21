@echo off
REM ═══════════════════════════════════════════════════════════════════════════
REM YOUTH CONNECT PLATFORM - Auth Service Startup (FIXED)
REM ═══════════════════════════════════════════════════════════════════════════
REM This script handles complete auth service startup with proper error handling
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
echo %COLOR_CYAN%  Youth Connect Platform - Auth Service Startup (FIXED)%COLOR_RESET%
echo %COLOR_CYAN%════════════════════════════════════════════════════════════════%COLOR_RESET%
echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 1: PRE-FLIGHT CHECKS
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%[1/6] Running pre-flight checks...%COLOR_RESET%

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo %COLOR_RED%ERROR: Java not found%COLOR_RESET%
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ Java installed%COLOR_RESET%

REM Check PostgreSQL
psql -U youthconnect_user -d youthconnect_auth -c "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo %COLOR_RED%ERROR: Cannot connect to PostgreSQL%COLOR_RESET%
    echo   Database: youthconnect_auth
    echo   User: youthconnect_user
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ PostgreSQL connection OK%COLOR_RESET%

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 2: CLEAN DATABASE (OPTIONAL - COMMENT OUT FOR PRODUCTION)
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%[2/6] Cleaning database schema...%COLOR_RESET%

psql -U youthconnect_user -d youthconnect_auth -h localhost -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;" >nul 2>&1
if errorlevel 1 (
    echo %COLOR_RED%ERROR: Failed to clean database%COLOR_RESET%
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ Database schema cleaned%COLOR_RESET%

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 3: CLEAN BUILD
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%[3/6] Cleaning build artifacts...%COLOR_RESET%

if exist "target\" (
    rmdir /s /q target
    echo %COLOR_GREEN%✓ Removed target directory%COLOR_RESET%
)

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 4: BUILD SERVICE
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%[4/6] Building Auth Service...%COLOR_RESET%

call mvn clean install -DskipTests
if errorlevel 1 (
    echo %COLOR_RED%ERROR: Build failed%COLOR_RESET%
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ Build completed successfully%COLOR_RESET%

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 5: LOAD ENVIRONMENT VARIABLES
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%[5/6] Loading environment variables...%COLOR_RESET%

REM Set default values
set SPRING_PROFILES_ACTIVE=local
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=youthconnect_auth
set DB_USER=youthconnect_user
set DB_PASSWORD=YouthConnect2024!

REM Load from .env file if exists
if exist ".env" (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%b"=="" (
            set "%%a=%%b"
        )
    )
    echo %COLOR_GREEN%✓ Environment variables loaded from .env%COLOR_RESET%
) else (
    echo %COLOR_YELLOW%⚠ Using default environment variables%COLOR_RESET%
)

echo   Profile: %SPRING_PROFILES_ACTIVE%
echo   Database: %DB_HOST%:%DB_PORT%/%DB_NAME%
echo   User: %DB_USER%

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM STEP 6: START SERVICE
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%[6/6] Starting Auth Service...%COLOR_RESET%
echo.
echo %COLOR_CYAN%════════════════════════════════════════════════════════════════%COLOR_RESET%
echo.

java -Dspring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
     -Dspring.datasource.url=jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME% ^
     -Dspring.datasource.username=%DB_USER% ^
     -Dspring.datasource.password=%DB_PASSWORD% ^
     -jar target\auth-service.jar

pause