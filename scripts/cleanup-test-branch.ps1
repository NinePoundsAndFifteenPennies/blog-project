# 本地测试完后换回localdevelop分支并删除测试功能的分支
# This script switches back to localdevelop and deletes the test branch

Write-Host "正在获取当前分支信息..." -ForegroundColor Cyan
Write-Host "Getting current branch information..." -ForegroundColor Cyan
Write-Host ""

# 获取当前分支名
$currentBranch = git rev-parse --abbrev-ref HEAD

if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: 无法获取当前分支信息" -ForegroundColor Red
    Write-Host "Error: Failed to get current branch" -ForegroundColor Red
    exit 1
}

Write-Host "当前分支 (Current branch): $currentBranch" -ForegroundColor Yellow
Write-Host ""

# 检查是否已经在localdevelop分支
if ($currentBranch -eq "localdevelop") {
    Write-Host "您已经在 localdevelop 分支上" -ForegroundColor Yellow
    Write-Host "You are already on localdevelop branch" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "是否要删除其他测试分支？(y/n): " -NoNewline -ForegroundColor Cyan
    Write-Host "Do you want to delete other test branches? (y/n): " -NoNewline -ForegroundColor Cyan
    $deleteOther = Read-Host
    
    if ($deleteOther -ne 'y' -and $deleteOther -ne 'Y') {
        Write-Host "操作已取消" -ForegroundColor Yellow
        Write-Host "Operation cancelled" -ForegroundColor Yellow
        exit 0
    }
    
    # 显示所有本地分支（排除localdevelop和main）
    Write-Host ""
    Write-Host "本地分支列表 (Local branches):" -ForegroundColor Green
    $branches = git branch | Where-Object { $_ -notmatch 'localdevelop' -and $_ -notmatch '\bmain\b' -and $_ -notmatch '\bmaster\b' } | ForEach-Object { $_.Trim() -replace '^\*\s*', '' }
    
    if ($branches.Count -eq 0) {
        Write-Host "没有可删除的测试分支" -ForegroundColor Yellow
        Write-Host "No test branches to delete" -ForegroundColor Yellow
        exit 0
    }
    
    $index = 1
    $branchList = @()
    foreach ($branch in $branches) {
        if ($branch) {
            Write-Host "  [$index] $branch" -ForegroundColor Yellow
            $branchList += $branch
            $index++
        }
    }
    
    Write-Host ""
    Write-Host "请输入要删除的分支编号 (Enter branch number to delete): " -NoNewline -ForegroundColor Cyan
    $selection = Read-Host
    
    if ($selection -match '^\d+$' -and [int]$selection -ge 1 -and [int]$selection -le $branchList.Count) {
        $branchToDelete = $branchList[[int]$selection - 1]
    } else {
        Write-Host "错误: 无效的选择" -ForegroundColor Red
        Write-Host "Error: Invalid selection" -ForegroundColor Red
        exit 1
    }
} else {
    $branchToDelete = $currentBranch
    
    Write-Host "将要执行以下操作 (Will perform the following operations):" -ForegroundColor Cyan
    Write-Host "  1. 切换到 localdevelop 分支 (Switch to localdevelop branch)" -ForegroundColor White
    Write-Host "  2. 拉取最新代码 (Pull latest changes)" -ForegroundColor White
    Write-Host "  3. 删除测试分支: $branchToDelete (Delete test branch: $branchToDelete)" -ForegroundColor White
    Write-Host ""
    Write-Host "是否继续？(y/n): " -NoNewline -ForegroundColor Yellow
    Write-Host "Continue? (y/n): " -NoNewline -ForegroundColor Yellow
    $confirm = Read-Host
    
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host ""
        Write-Host "操作已取消" -ForegroundColor Yellow
        Write-Host "Operation cancelled" -ForegroundColor Yellow
        exit 0
    }
    
    Write-Host ""
    Write-Host "正在切换到 localdevelop 分支..." -ForegroundColor Cyan
    Write-Host "Switching to localdevelop branch..." -ForegroundColor Cyan
    
    git checkout localdevelop
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "错误: 无法切换到 localdevelop 分支" -ForegroundColor Red
        Write-Host "Error: Failed to checkout localdevelop branch" -ForegroundColor Red
        exit 1
    }
    
    Write-Host ""
    Write-Host "正在拉取最新代码..." -ForegroundColor Cyan
    Write-Host "Pulling latest changes..." -ForegroundColor Cyan
    
    git pull
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "警告: 拉取最新代码失败，但将继续删除分支" -ForegroundColor Yellow
        Write-Host "Warning: Failed to pull latest changes, but will continue to delete branch" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "正在删除分支: $branchToDelete..." -ForegroundColor Cyan
Write-Host "Deleting branch: $branchToDelete..." -ForegroundColor Cyan

git branch -d $branchToDelete

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "使用 -d 删除失败，该分支可能包含未合并的更改" -ForegroundColor Yellow
    Write-Host "Failed with -d, the branch may contain unmerged changes" -ForegroundColor Yellow
    Write-Host "是否强制删除？(y/n): " -NoNewline -ForegroundColor Yellow
    Write-Host "Force delete? (y/n): " -NoNewline -ForegroundColor Yellow
    $forceDelete = Read-Host
    
    if ($forceDelete -eq 'y' -or $forceDelete -eq 'Y') {
        git branch -D $branchToDelete
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "成功强制删除分支 $branchToDelete！" -ForegroundColor Green
            Write-Host "Successfully force deleted branch $branchToDelete!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "错误: 强制删除分支失败" -ForegroundColor Red
            Write-Host "Error: Failed to force delete branch" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host ""
        Write-Host "操作已取消，分支未被删除" -ForegroundColor Yellow
        Write-Host "Operation cancelled, branch not deleted" -ForegroundColor Yellow
        exit 0
    }
} else {
    Write-Host ""
    Write-Host "成功删除分支 $branchToDelete！" -ForegroundColor Green
    Write-Host "Successfully deleted branch $branchToDelete!" -ForegroundColor Green
}

Write-Host ""
Write-Host "操作完成！" -ForegroundColor Green
Write-Host "Operation completed!" -ForegroundColor Green
