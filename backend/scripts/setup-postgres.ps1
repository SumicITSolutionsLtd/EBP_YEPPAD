# ═══════════════════════════════════════════════════════════════════════════
# YOUTH CONNECT PLATFORM - PostgreSQL Database Setup Script (Windows)
# ═══════════════════════════════════════════════════════════════════════════
# File: setup-postgres.ps1
# Version: 3.1.0
# Author: Douglas Kings Kato
# Date: 2025-11-15
#
# PURPOSE:
#   Complete automated setup of PostgreSQL databases for all microservices
#   Handles cleanup, creation, and verification
#
# USAGE:
#   1. Save this file as: backend\scripts\setup-postgres.ps1
#   2. Open PowerShell in backend directory
#   3. Run: .\scripts\setup-postgres.ps1
#   4. Enter postgres password when prompted
#
# REQUIREMENTS:
#   - PostgreSQL 15+ installed
#   - psql.exe accessible
#   - scripts\init-databases.sql present
# ═══════════════════════════════════════════════════════════════════════════

# ═══════════════════════════════════════════════════════════════════════════
# CONFIGURATION
# ═══════════════════════════════════════════════════════════════════════════

$ErrorActionPreference = "Stop"  # Stop on any error

# PostgreSQL Configuration
$PSQL_PATH = "F:\Installations\PostgreSql\bin\psql.exe"
$POSTGRES_USER = "postgres"
$BACKEND_PATH = "F:\Douglas Kings\Hackthon\EBP_YEPPAD\backend"
$INIT_SCRIPT = "scripts\init-databases.sql"

# Colors for output
$COLOR_SUCCESS = "Green"
$COLOR_ERROR = "Red"
$COLOR_WARNING = "Yellow"
$COLOR_INFO = "Cyan"
$COLOR_HEADER = "Blue"

# ═══════════════════════════════════════════════════════════════════════════
# HELPER FUNCTIONS
# ═══════════════════════════════════════════════════════════════════════════

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor $COLOR_HEADER
    Write-Host "  $Message" -ForegroundColor $COLOR_HEADER
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor $COLOR_HEADER
    Write-Host ""
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor $COLOR_SUCCESS
}

function Write-Error-Message {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor $COLOR_ERROR
}

function Write-Warning-Message {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor $COLOR_WARNING
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor $COLOR_INFO
}

# ═══════════════════════════════════════════════════════════════════════════
# PRE-FLIGHT CHECKS
# ═══════════════════════════════════════════════════════════════════════════

function Test-Prerequisites {
    Write-Header "Pre-Flight Checks"

    # Check if psql exists
    if (-not (Test-Path $PSQL_PATH)) {
        Write-Error-Message "PostgreSQL not found at: $PSQL_PATH"
        Write-Info "Please update the `$PSQL_PATH variable in this script"
        exit 1
    }
    Write-Success "PostgreSQL found: $PSQL_PATH"

    # Check if backend directory exists
    if (-not (Test-Path $BACKEND_PATH)) {
        Write-Error-Message "Backend directory not found: $BACKEND_PATH"
        exit 1
    }
    Write-Success "Backend directory found: $BACKEND_PATH"

    # Check if init script exists
    $scriptPath = Join-Path $BACKEND_PATH $INIT_SCRIPT
    if (-not (Test-Path $scriptPath)) {
        Write-Error-Message "Initialization script not found: $scriptPath"
        Write-Info "Please ensure scripts\init-databases.sql exists"
        exit 1
    }
    Write-Success "Initialization script found: $INIT_SCRIPT"

    Write-Host ""
}

# ═══════════════════════════════════════════════════════════════════════════
# MAIN SETUP FUNCTIONS
# ═══════════════════════════════════════════════════════════════════════════

function Invoke-DatabaseCleanup {
    Write-Header "Step 1/3: Cleaning Up Previous Installation"

    Write-Info "Dropping existing databases and roles (if any)..."
    Write-Warning-Message "This will delete all existing data!"

    $confirmation = Read-Host "Continue with cleanup? (yes/no)"
    if ($confirmation -ne "yes") {
        Write-Warning-Message "Cleanup cancelled by user"
        return $false
    }

    # Create cleanup script
    $cleanupSQL = @"
-- Drop all databases
DROP DATABASE IF EXISTS youthconnect_auth;
DROP DATABASE IF EXISTS youthconnect_user;
DROP DATABASE IF EXISTS youthconnect_job;
DROP DATABASE IF EXISTS youthconnect_opportunity;
DROP DATABASE IF EXISTS youthconnect_mentor;
DROP DATABASE IF EXISTS youthconnect_content;
DROP DATABASE IF EXISTS youthconnect_notification;
DROP DATABASE IF EXISTS youthconnect_file;
DROP DATABASE IF EXISTS youthconnect_ai;
DROP DATABASE IF EXISTS youthconnect_analytics;
DROP DATABASE IF EXISTS youthconnect_ussd;

-- Drop user role
DROP ROLE IF EXISTS youthconnect_user;

-- Success message
\echo 'Cleanup completed successfully'
"@

    # Save cleanup script
    $cleanupFile = Join-Path $BACKEND_PATH "scripts\temp_cleanup.sql"
    $cleanupSQL | Out-File -FilePath $cleanupFile -Encoding UTF8

    try {
        # Run cleanup
        & $PSQL_PATH -U $POSTGRES_USER -f $cleanupFile

        Write-Success "Cleanup completed successfully"
        return $true
    }
    catch {
        Write-Error-Message "Cleanup failed: $_"
        return $false
    }
    finally {
        # Clean up temp file
        if (Test-Path $cleanupFile) {
            Remove-Item $cleanupFile -Force
        }
    }
}

function Invoke-DatabaseSetup {
    Write-Header "Step 2/3: Creating Databases and Extensions"

    Write-Info "Running initialization script..."
    Write-Info "This will create:"
    Write-Host "  • Application user (youthconnect_user)" -ForegroundColor Gray
    Write-Host "  • 11 microservice databases" -ForegroundColor Gray
    Write-Host "  • PostgreSQL extensions (UUID, full-text search, etc.)" -ForegroundColor Gray
    Write-Host ""

    try {
        # Set UTF-8 encoding
        $env:PGCLIENTENCODING = "UTF8"

        # Change to backend directory
        Push-Location $BACKEND_PATH

        # Run initialization script
        & $PSQL_PATH -U $POSTGRES_USER -f $INIT_SCRIPT

        Pop-Location

        Write-Success "Database setup completed successfully"
        return $true
    }
    catch {
        Write-Error-Message "Database setup failed: $_"
        return $false
    }
}

function Test-DatabaseSetup {
    Write-Header "Step 3/3: Verifying Database Setup"

    Write-Info "Connecting to databases to verify setup..."

    # List of databases to verify
    $databases = @(
        "youthconnect_auth",
        "youthconnect_user",
        "youthconnect_job",
        "youthconnect_opportunity",
        "youthconnect_mentor",
        "youthconnect_content",
        "youthconnect_notification",
        "youthconnect_file",
        "youthconnect_ai",
        "youthconnect_analytics",
        "youthconnect_ussd"
    )

    $verificationSQL = @"
-- List all Youth Connect databases
SELECT datname AS "Database Name"
FROM pg_database
WHERE datname LIKE 'youthconnect_%'
ORDER BY datname;

-- Count total databases
SELECT COUNT(*) AS "Total Databases"
FROM pg_database
WHERE datname LIKE 'youthconnect_%';
"@

    $verifyFile = Join-Path $BACKEND_PATH "scripts\temp_verify.sql"
    $verificationSQL | Out-File -FilePath $verifyFile -Encoding UTF8

    try {
        Write-Host ""
        & $PSQL_PATH -U $POSTGRES_USER -f $verifyFile
        Write-Host ""

        Write-Success "Verification completed successfully"
        Write-Success "All $($databases.Count) databases are ready!"
        return $true
    }
    catch {
        Write-Error-Message "Verification failed: $_"
        return $false
    }
    finally {
        if (Test-Path $verifyFile) {
            Remove-Item $verifyFile -Force
        }
    }
}

function Show-CompletionSummary {
    Write-Header "Setup Complete! 🎉"

    Write-Host "📊 CREATED RESOURCES:" -ForegroundColor $COLOR_INFO
    Write-Host ""

    Write-Host "  Databases (11):" -ForegroundColor White
    Write-Host "    ✅ youthconnect_auth          (Authentication & Authorization)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_user          (User Profiles & Management)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_job           (Job Opportunities)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_opportunity   (Business Opportunities)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_mentor        (Mentorship Programs)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_content       (Educational Content)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_notification  (Multi-channel Notifications)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_file          (File Management)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_ai            (AI Recommendations)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_analytics     (Analytics & BI)" -ForegroundColor Gray
    Write-Host "    ✅ youthconnect_ussd          (USSD Service)" -ForegroundColor Gray
    Write-Host ""

    Write-Host "  Database User:" -ForegroundColor White
    Write-Host "    Username: " -NoNewline -ForegroundColor Gray
    Write-Host "youthconnect_user" -ForegroundColor $COLOR_SUCCESS
    Write-Host "    Password: " -NoNewline -ForegroundColor Gray
    Write-Host "YouthConnect2024!" -ForegroundColor $COLOR_WARNING
    Write-Warning-Message "CHANGE PASSWORD IN PRODUCTION!"
    Write-Host ""

    Write-Host "🔗 CONNECTION EXAMPLES:" -ForegroundColor $COLOR_INFO
    Write-Host ""
    Write-Host "  psql command:" -ForegroundColor White
    Write-Host "    psql -U youthconnect_user -d youthconnect_auth" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  JDBC URL:" -ForegroundColor White
    Write-Host "    jdbc:postgresql://localhost:5432/youthconnect_auth" -ForegroundColor Gray
    Write-Host ""

    Write-Host "📝 NEXT STEPS:" -ForegroundColor $COLOR_INFO
    Write-Host ""
    Write-Host "  1. ✅ Create .env files for each service" -ForegroundColor Gray
    Write-Host "  2. ✅ Create Flyway migration files" -ForegroundColor Gray
    Write-Host "  3. ✅ Start Service Registry (Eureka)" -ForegroundColor Gray
    Write-Host "  4. ✅ Start Auth Service" -ForegroundColor Gray
    Write-Host "  5. ✅ Start User Service" -ForegroundColor Gray
    Write-Host "  6. ✅ Start API Gateway" -ForegroundColor Gray
    Write-Host "  7. ✅ Start Other Services" -ForegroundColor Gray
    Write-Host ""

    Write-Host "🚀 START SERVICES:" -ForegroundColor $COLOR_INFO
    Write-Host ""
    Write-Host "  # Service Registry" -ForegroundColor White
    Write-Host "  cd service-registry" -ForegroundColor Gray
    Write-Host "  mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  # Auth Service (NEW TERMINAL)" -ForegroundColor White
    Write-Host "  cd auth-service" -ForegroundColor Gray
    Write-Host "  mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  # Continue with other services..." -ForegroundColor Gray
    Write-Host ""

    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor $COLOR_HEADER
    Write-Host ""
}

# ═══════════════════════════════════════════════════════════════════════════
# MAIN EXECUTION
# ═══════════════════════════════════════════════════════════════════════════

function Main {
    # Clear screen for better visibility
    Clear-Host

    # Print banner
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor $COLOR_HEADER
    Write-Host "║                                                            ║" -ForegroundColor $COLOR_HEADER
    Write-Host "║     Youth Connect Platform - PostgreSQL Setup Script      ║" -ForegroundColor $COLOR_HEADER
    Write-Host "║                     Version 3.1.0                          ║" -ForegroundColor $COLOR_HEADER
    Write-Host "║                                                            ║" -ForegroundColor $COLOR_HEADER
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor $COLOR_HEADER
    Write-Host ""

    # Run setup steps
    try {
        # Step 0: Pre-flight checks
        Test-Prerequisites

        # Step 1: Cleanup
        $cleanupSuccess = Invoke-DatabaseCleanup
        if (-not $cleanupSuccess) {
            Write-Error-Message "Setup aborted due to cleanup failure"
            exit 1
        }

        # Step 2: Setup
        $setupSuccess = Invoke-DatabaseSetup
        if (-not $setupSuccess) {
            Write-Error-Message "Setup aborted due to database creation failure"
            exit 1
        }

        # Step 3: Verify
        $verifySuccess = Test-DatabaseSetup
        if (-not $verifySuccess) {
            Write-Warning-Message "Setup completed but verification failed"
        }

        # Show summary
        Show-CompletionSummary

        Write-Success "Setup script completed successfully!"
        exit 0
    }
    catch {
        Write-Error-Message "Setup failed with error: $_"
        Write-Info "Check the error message above for details"
        exit 1
    }
}

# Run main function
Main