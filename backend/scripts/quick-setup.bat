@echo off
REM ═══════════════════════════════════════════════════════════════════════════
REM YOUTH CONNECT PLATFORM - Quick PostgreSQL Setup (Batch Script)
REM ═══════════════════════════════════════════════════════════════════════════
REM File: quick-setup.bat
REM Version: 3.1.0
REM Author: Douglas Kings Kato
REM Usage: Double-click this file or run from command prompt
REM ═══════════════════════════════════════════════════════════════════════════

SETLOCAL EnableDelayedExpansion

REM Colors (Windows 10+)
SET "COLOR_RESET=[0m"
SET "COLOR_GREEN=[92m"
SET "COLOR_RED=[91m"
SET "COLOR_YELLOW=[93m"
SET "COLOR_BLUE=[94m"
SET "COLOR_CYAN=[96m"

REM Configuration
SET "PSQL_PATH=F:\Installations\PostgreSql\bin\psql.exe"
SET "POSTGRES_USER=postgres"
SET "BACKEND_PATH=%~dp0.."
SET "INIT_SCRIPT=%BACKEND_PATH%\scripts\init-databases.sql"

REM ═══════════════════════════════════════════════════════════════════════════
REM MAIN SCRIPT
REM ═══════════════════════════════════════════════════════════════════════════

cls
echo.
echo %COLOR_BLUE%╔════════════════════════════════════════════════════════════╗%COLOR_RESET%
echo %COLOR_BLUE%║                                                            ║%COLOR_RESET%
echo %COLOR_BLUE%║     Youth Connect Platform - PostgreSQL Quick Setup       ║%COLOR_RESET%
echo %COLOR_BLUE%║                     Version 3.1.0                          ║%COLOR_RESET%
echo %COLOR_BLUE%║                                                            ║%COLOR_RESET%
echo %COLOR_BLUE%╚════════════════════════════════════════════════════════════╝%COLOR_RESET%
echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM PRE-FLIGHT CHECKS
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_CYAN%Checking prerequisites...%COLOR_RESET%
echo.

REM Check if psql exists
if not exist "%PSQL_PATH%" (
    echo %COLOR_RED%ERROR: PostgreSQL not found at: %PSQL_PATH%%COLOR_RESET%
    echo %COLOR_YELLOW%Please update PSQL_PATH in this script%COLOR_RESET%
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ PostgreSQL found%COLOR_RESET%

REM Check if init script exists
if not exist "%INIT_SCRIPT%" (
    echo %COLOR_RED%ERROR: Initialization script not found%COLOR_RESET%
    echo %COLOR_YELLOW%Expected: %INIT_SCRIPT%%COLOR_RESET%
    pause
    exit /b 1
)
echo %COLOR_GREEN%✓ Initialization script found%COLOR_RESET%

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM CLEANUP WARNING
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_YELLOW%═══════════════════════════════════════════════════════════%COLOR_RESET%
echo %COLOR_YELLOW%WARNING: This will DELETE all existing Youth Connect databases!%COLOR_RESET%
echo %COLOR_YELLOW%═══════════════════════════════════════════════════════════%COLOR_RESET%
echo.
echo Databases that will be recreated:
echo   • youthconnect_auth
echo   • youthconnect_user
echo   • youthconnect_job
echo   • youthconnect_opportunity
echo   • youthconnect_mentor
echo   • youthconnect_content
echo   • youthconnect_notification
echo   • youthconnect_file
echo   • youthconnect_ai
echo   • youthconnect_analytics
echo   • youthconnect_ussd
echo.
set /p CONTINUE="Continue? (yes/no): "
if /i not "%CONTINUE%"=="yes" (
    echo %COLOR_YELLOW%Setup cancelled by user%COLOR_RESET%
    pause
    exit /b 0
)

echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM SET ENCODING
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_CYAN%Setting UTF-8 encoding...%COLOR_RESET%
set PGCLIENTENCODING=UTF8
echo %COLOR_GREEN%✓ Encoding set to UTF-8%COLOR_RESET%
echo.

REM ═══════════════════════════════════════════════════════════════════════════
REM RUN INITIALIZATION SCRIPT
REM ═══════════════════════════════════════════════════════════════════════════

echo %COLOR_CYAN%═══════════════════════════════════════════════════════════%COLOR_RESET%
echo %COLOR_CYAN%Running database initialization script...%COLOR_RESET%
echo %COLOR_CYAN%═══════════════════════════════════════════════════════════%COLOR_RESET%
echo.
echo %COLOR_YELLOW%Please enter your PostgreSQL password when prompted%COLOR_RESET%
echo.

REM Change to backend directory
cd /d "%BACKEND_PATH%"

REM Run the initialization script
"%PSQL_PATH%" -U %POSTGRES_USER% -f "%INIT_SCRIPT%"

if !ERRORLEVEL! NEQ 0 (
    echo.
    echo %COLOR_RED%═══════════════════════════════════════════════════════════%COLOR_RESET%
    echo %COLOR_RED%Setup failed! Check the error messages above.%COLOR_RESET%
    echo %COLOR_RED%═══════════════════════════════════════════════════════════%COLOR_RESET%
    pause
    exit /b 1
)

REM ═══════════════════════════════════════════════════════════════════════════
REM SUCCESS MESSAGE
REM ═══════════════════════════════════════════════════════════════════════════

echo.
echo %COLOR_GREEN%╔════════════════════════════════════════════════════════════╗%COLOR_RESET%
echo %COLOR_GREEN%║                                                            ║%COLOR_RESET%
echo %COLOR_GREEN%║              Setup Completed Successfully! 🎉              ║%COLOR_RESET%
echo %COLOR_GREEN%║                                                            ║%COLOR_RESET%
echo %COLOR_GREEN%╚════════════════════════════════════════════════════════════╝%COLOR_RESET%
echo.
echo %COLOR_CYAN%Database Credentials:%COLOR_RESET%
echo   Username: youthconnect_user
echo   Password: YouthConnect2024!
echo   %COLOR_YELLOW%⚠️  CHANGE PASSWORD IN PRODUCTION!%COLOR_RESET%
echo.
echo %COLOR_CYAN%Next Steps:%COLOR_RESET%
echo   1. Create .env files for each service
echo   2. Create Flyway migration files
echo   3. Start Service Registry: cd service-registry ^&^& mvn spring-boot:run
echo   4. Start Auth Service:     cd auth-service ^&^& mvn spring-boot:run
echo   5. Start User Service:     cd user-service ^&^& mvn spring-boot:run
echo   6. Start API Gateway:      cd api-gateway ^&^& mvn spring-boot:run
echo.
echo %COLOR_CYAN%Verify setup:%COLOR_RESET%
echo   Eureka Dashboard: http://localhost:8761
echo.
echo Press any key to exit...
pause >nul

ENDLOCAL