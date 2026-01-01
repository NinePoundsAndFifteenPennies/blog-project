# Fetch remote branches and checkout a specific branch for testing
# This script fetches remote branches and allows you to checkout a specific branch for testing
# It prunes stale refs and uses git ls-remote to list the actual remote branches.

Write-Host "Fetching remote branches and pruning stale refs..." -ForegroundColor Cyan
git fetch --prune origin 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Warning: fetch --prune failed; attempting to continue using ls-remote." -ForegroundColor Yellow
}

Write-Host "Getting available remote branches from origin..." -ForegroundColor Cyan
Write-Host ""

# Use git ls-remote --heads origin to get authoritative remote branch list
$ls = git ls-remote --heads origin 2>$null
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($ls)) {
    Write-Host "Error: Failed to query remote branches via ls-remote" -ForegroundColor Red
    exit 1
}

# Parse ls-remote output: each line "<sha>    refs/heads/<branch>"
$remoteBranches = @()
$ls -split "`n" | ForEach-Object {
    $line = $_.Trim()
    if ($line) {
        $parts = $line -split '\s+'
        if ($parts.Count -ge 2) {
            $ref = $parts[1]
            $name = $ref -replace '^refs/heads/',''
            if ($name) {
                $remoteBranches += $name
            }
        }
    }
}

if ($remoteBranches.Count -eq 0) {
    Write-Host "No remote branches found on origin." -ForegroundColor Yellow
    exit 0
}

Write-Host "Available remote branches:" -ForegroundColor Green
$index = 1
foreach ($b in $remoteBranches) {
    Write-Host "  [$index] $b" -ForegroundColor Yellow
    $index++
}

Write-Host ""
Write-Host "Enter the number of the branch to test: " -NoNewline -ForegroundColor Cyan
$selection = Read-Host

# Validate input
if ($selection -match '^\d+$' -and
        [int]$selection -ge 1 -and
        [int]$selection -le $remoteBranches.Count) {

    $selectedBranch = $remoteBranches[[int]$selection - 1]

    Write-Host ""
    Write-Host "Checking out branch: $selectedBranch" -ForegroundColor Cyan

    # ---- FIX: safely check local branch existence ----
    $branchOutput = git branch --list -- $selectedBranch 2>$null
    $localExists = -not [string]::IsNullOrWhiteSpace($branchOutput)

    if ($localExists) {
        # Local branch exists: checkout and pull
        git checkout -- $selectedBranch 2>$null
        if ($LASTEXITCODE -eq 0) {

            git pull origin $selectedBranch 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Host ""
                Write-Host "Successfully checked out and updated branch $selectedBranch!" -ForegroundColor Green
            } else {
                Write-Host ""
                Write-Host "Warning: Checkout succeeded but pull failed" -ForegroundColor Yellow
            }

        } else {
            Write-Host ""
            Write-Host "Error: Failed to checkout branch" -ForegroundColor Red
            exit 1
        }

    } else {
        # Local branch does not exist: create and track remote
        git checkout -b $selectedBranch origin/$selectedBranch 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "Successfully created and checked out branch $selectedBranch!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "Error: Failed to create branch" -ForegroundColor Red
            exit 1
        }
    }

} else {
    Write-Host ""
    Write-Host "Error: Invalid selection" -ForegroundColor Red
    exit 1
}



