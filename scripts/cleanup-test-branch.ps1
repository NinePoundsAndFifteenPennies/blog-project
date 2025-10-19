# Switch back to localdevelop branch and delete test branch after local testing
# This script switches back to localdevelop and deletes the test branch
# NOTE: This script performs `git fetch --prune origin` first to remove stale remote-tracking refs.

Write-Host "Synchronizing with remote and pruning stale refs..." -ForegroundColor Cyan
git fetch --prune origin
if ($LASTEXITCODE -ne 0) {
    Write-Host "Warning: Failed to fetch/prune from remote, continuing with local information." -ForegroundColor Yellow
}

Write-Host "Getting current branch information..." -ForegroundColor Cyan
Write-Host ""

# Get current branch name
$currentBranch = git rev-parse --abbrev-ref HEAD

if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($currentBranch)) {
    Write-Host "Error: Failed to get current branch information" -ForegroundColor Red
    exit 1
}

Write-Host "Current branch: $currentBranch" -ForegroundColor Yellow
Write-Host ""

# Check if already on localdevelop
if ($currentBranch -eq "localdevelop") {
    Write-Host "You are already on the localdevelop branch" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Do you want to delete other local test branches? (y/n): " -NoNewline -ForegroundColor Cyan
    $deleteOther = Read-Host

    if ($deleteOther -ne 'y' -and $deleteOther -ne 'Y') {
        Write-Host "Operation cancelled" -ForegroundColor Yellow
        exit 0
    }

    # Show all local branches (exclude localdevelop, main, master)
    Write-Host ""
    Write-Host "Local branches:" -ForegroundColor Green
    $branches = git branch | ForEach-Object { $_.Trim() -replace '^\*\s*', '' } | Where-Object { $_ -and $_ -notin @('localdevelop','main','master') }

    if (-not $branches -or $branches.Count -eq 0) {
        Write-Host "No test branches to delete" -ForegroundColor Yellow
        exit 0
    }

    $index = 1
    $branchList = @()
    foreach ($branch in $branches) {
        Write-Host "  [$index] $branch" -ForegroundColor Yellow
        $branchList += $branch
        $index++
    }

    Write-Host ""
    Write-Host "Enter branch number to delete: " -NoNewline -ForegroundColor Cyan
    $selection = Read-Host

    if ($selection -match '^\d+$' -and [int]$selection -ge 1 -and [int]$selection -le $branchList.Count) {
        $branchToDelete = $branchList[[int]$selection - 1]
    } else {
        Write-Host "Error: Invalid selection" -ForegroundColor Red
        exit 1
    }
} else {
    $branchToDelete = $currentBranch

    Write-Host "Will perform the following operations:" -ForegroundColor Cyan
    Write-Host "  1. Switch to localdevelop branch" -ForegroundColor White
    Write-Host "  2. Pull latest changes" -ForegroundColor White
    Write-Host "  3. Delete test branch: $branchToDelete" -ForegroundColor White
    Write-Host ""
    Write-Host "Continue? (y/n): " -NoNewline -ForegroundColor Yellow
    $confirm = Read-Host

    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host ""
        Write-Host "Operation cancelled" -ForegroundColor Yellow
        exit 0
    }

    Write-Host ""
    Write-Host "Switching to localdevelop branch..." -ForegroundColor Cyan

    git checkout localdevelop

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "Error: Failed to checkout localdevelop branch" -ForegroundColor Red
        exit 1
    }

    Write-Host ""
    Write-Host "Pulling latest changes..." -ForegroundColor Cyan

    git pull origin localdevelop

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "Warning: Failed to pull latest changes, but will continue to delete branch" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Deleting branch: $branchToDelete..." -ForegroundColor Cyan

git branch -d $branchToDelete

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Failed with -d, the branch may contain unmerged changes" -ForegroundColor Yellow
    Write-Host "Force delete? (y/n): " -NoNewline -ForegroundColor Yellow
    $forceDelete = Read-Host

    if ($forceDelete -eq 'y' -or $forceDelete -eq 'Y') {
        git branch -D $branchToDelete

        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "Successfully force deleted branch $branchToDelete!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "Error: Failed to force delete branch" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host ""
        Write-Host "Operation cancelled, branch not deleted" -ForegroundColor Yellow
        exit 0
    }
} else {
    Write-Host ""
    Write-Host "Successfully deleted branch $branchToDelete!" -ForegroundColor Green
}

Write-Host ""
Write-Host "Operation completed!" -ForegroundColor Green
