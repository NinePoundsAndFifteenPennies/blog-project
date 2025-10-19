# 从远程拉取最新开发好的分支来测试
# This script fetches remote branches and allows you to checkout a specific branch for testing

Write-Host "正在获取远程分支..." -ForegroundColor Cyan
Write-Host "Fetching remote branches..." -ForegroundColor Cyan
Write-Host ""

# 从远程拉取最新的分支信息
git fetch origin

if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: 无法从远程获取分支信息" -ForegroundColor Red
    Write-Host "Error: Failed to fetch from remote" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "可用的远程分支 (Available remote branches):" -ForegroundColor Green
Write-Host ""

# 获取所有远程分支并显示（排除HEAD）
$remoteBranches = git branch -r | Where-Object { $_ -notmatch 'HEAD' } | ForEach-Object { $_.Trim() }
$branches = @()
$index = 1

foreach ($branch in $remoteBranches) {
    $branchName = $branch -replace 'origin/', ''
    Write-Host "  [$index] $branchName" -ForegroundColor Yellow
    $branches += $branchName
    $index++
}

Write-Host ""
Write-Host "请输入要测试的分支编号 (Enter the number of the branch to test): " -NoNewline -ForegroundColor Cyan
$selection = Read-Host

# 验证输入
if ($selection -match '^\d+$' -and [int]$selection -ge 1 -and [int]$selection -le $branches.Count) {
    $selectedBranch = $branches[[int]$selection - 1]
    Write-Host ""
    Write-Host "正在切换到分支: $selectedBranch" -ForegroundColor Cyan
    Write-Host "Checking out branch: $selectedBranch" -ForegroundColor Cyan
    
    # 检查本地是否已存在该分支
    $localBranch = git branch --list $selectedBranch
    
    if ($localBranch) {
        # 如果本地已存在，切换并拉取最新
        git checkout $selectedBranch
        if ($LASTEXITCODE -eq 0) {
            git pull origin $selectedBranch
            if ($LASTEXITCODE -eq 0) {
                Write-Host ""
                Write-Host "成功切换到分支 $selectedBranch 并更新到最新版本！" -ForegroundColor Green
                Write-Host "Successfully checked out and updated branch $selectedBranch!" -ForegroundColor Green
            } else {
                Write-Host ""
                Write-Host "警告: 切换成功但拉取更新失败" -ForegroundColor Yellow
                Write-Host "Warning: Checkout succeeded but pull failed" -ForegroundColor Yellow
            }
        } else {
            Write-Host ""
            Write-Host "错误: 切换分支失败" -ForegroundColor Red
            Write-Host "Error: Failed to checkout branch" -ForegroundColor Red
            exit 1
        }
    } else {
        # 如果本地不存在，创建新分支并跟踪远程分支
        git checkout -b $selectedBranch origin/$selectedBranch
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "成功创建并切换到分支 $selectedBranch！" -ForegroundColor Green
            Write-Host "Successfully created and checked out branch $selectedBranch!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "错误: 创建分支失败" -ForegroundColor Red
            Write-Host "Error: Failed to create branch" -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host ""
    Write-Host "错误: 无效的选择" -ForegroundColor Red
    Write-Host "Error: Invalid selection" -ForegroundColor Red
    exit 1
}
