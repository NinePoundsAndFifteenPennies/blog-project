# Resolve project root (scripts is one level below root)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$rootDir   = Split-Path -Parent $scriptDir

$backendPom  = Join-Path $rootDir "backend\blog\pom.xml"
$frontendDir = Join-Path $rootDir "frontend"

Write-Host "Project root: $rootDir" -ForegroundColor DarkGray
Write-Host ""

# -------------------------------
# Start Backend
# -------------------------------
Write-Host "Starting backend (Spring Boot)..." -ForegroundColor Cyan

# Force the Spring JVM to use project root as user.dir so relative paths like "uploads" resolve to project root.
# This makes application.properties with file.upload-dir=uploads behave the same as IDEA.
$jvmArg = "-Dspring-boot.run.jvmArguments=-Duser.dir=$rootDir"

Start-Process -FilePath "mvn" `
    -ArgumentList "-f", $backendPom, $jvmArg, "spring-boot:run" `
    -WorkingDirectory $rootDir

Write-Host "Backend started in a new window." -ForegroundColor Green
Write-Host ""

# Optional: wait a bit to let backend initialize
Start-Sleep -Seconds 3

# -------------------------------
# Start Frontend
# -------------------------------
Write-Host "Starting frontend (npm run serve)..." -ForegroundColor Cyan

# Start frontend with working directory = project root/frontend
Start-Process -FilePath "powershell.exe" `
    -ArgumentList "-NoExit", "-Command", "Set-Location '$frontendDir'; npm run serve"

Write-Host "Frontend started in a new window." -ForegroundColor Green