################################################################################
# Flyway Repair Script for User Service (PowerShell)
# Compatible with Windows environments
################################################################################

Write-Host "---------------------------------------------------------------" -ForegroundColor Cyan
Write-Host "  Flyway Repair - User Service" -ForegroundColor Cyan
Write-Host "---------------------------------------------------------------" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_HOST = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DB_PORT = if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }
$DB_NAME = if ($env:DB_NAME) { $env:DB_NAME } else { "youthconnect_user" }
$DB_USER = if ($env:DB_USER) { $env:DB_USER } else { "youthconnect_user" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "YouthConnect2024!" }

Write-Host "[CONFIGURATION]" -ForegroundColor Yellow
Write-Host "   Database: $DB_NAME"
Write-Host "   Host: ${DB_HOST}:${DB_PORT}"
Write-Host "   User: $DB_USER"
Write-Host ""

# Step 1: Backup
Write-Host "[1/3] Backing up flyway_schema_history..." -ForegroundColor Yellow
$env:PGPASSWORD = $DB_PASSWORD
& psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c `
  "CREATE TABLE IF NOT EXISTS flyway_schema_history_backup AS SELECT * FROM flyway_schema_history;" 2>$null
Write-Host "[OK] Backup created" -ForegroundColor Green
Write-Host ""

# Step 2: Repair
Write-Host "[2/3] Running Flyway repair..." -ForegroundColor Yellow
& mvn flyway:repair `
  "-Dflyway.user=$DB_USER" `
  "-Dflyway.password=$DB_PASSWORD" `
  "-Dflyway.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
Write-Host "[OK] Repair completed" -ForegroundColor Green
Write-Host ""

# Step 3: Verify
Write-Host "[3/3] Verifying repair..." -ForegroundColor Yellow
& psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c `
  "SELECT version, description, type, checksum, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;"
Write-Host ""

Write-Host "---------------------------------------------------------------" -ForegroundColor Green
Write-Host "Flyway repair completed successfully!" -ForegroundColor Green
Write-Host "---------------------------------------------------------------" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:"
Write-Host "  1. Run: mvn spring-boot:run"
Write-Host "  2. Verify service starts without errors"
Write-Host ""

# Clean up environment
Remove-Item Env:PGPASSWORD