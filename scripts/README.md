# 项目管理脚本 / Project Management Scripts

本目录包含用于管理项目 Git 分支的 PowerShell 脚本。

This directory contains PowerShell scripts for managing project Git branches.

## 脚本列表 / Script List

### 1. fetch-test-branch.ps1

**功能**: 从远程仓库拉取新分支并切换到该分支进行测试

**用途**: When you need to test a new feature branch from the remote repository

**使用方法**:
```powershell
cd scripts
.\fetch-test-branch.ps1
```

**操作流程**:
1. 自动从远程获取最新的分支信息 (`git fetch origin`)
2. 显示所有可用的远程分支列表
3. 提示您输入要测试的分支编号
4. 自动切换到选定的分支（如果本地不存在则创建）

**示例输出**:
```
正在获取远程分支...
Fetching remote branches...

可用的远程分支 (Available remote branches):

  [1] main
  [2] localdevelop
  [3] feature/user-profile
  [4] feature/article-tags

请输入要测试的分支编号 (Enter the number of the branch to test): 3

正在切换到分支: feature/user-profile
Checking out branch: feature/user-profile

成功创建并切换到分支 feature/user-profile！
Successfully created and checked out branch feature/user-profile!
```

---

### 2. cleanup-test-branch.ps1

**功能**: 测试完成后，切换回 localdevelop 分支，同步最新代码，并删除测试分支

**用途**: After testing is complete, clean up the test branch and return to development

**使用方法**:
```powershell
cd scripts
.\cleanup-test-branch.ps1
```

**操作流程**:
1. 获取当前所在的分支名称
2. 切换回 localdevelop 分支
3. 拉取远程 localdevelop 分支的最新代码 (`git pull`)
4. 删除之前用于测试的分支
5. 如果分支包含未合并的更改，会提示是否强制删除

**示例输出**:
```
正在获取当前分支信息...
Getting current branch information...

当前分支 (Current branch): feature/user-profile

将要执行以下操作 (Will perform the following operations):
  1. 切换到 localdevelop 分支 (Switch to localdevelop branch)
  2. 拉取最新代码 (Pull latest changes)
  3. 删除测试分支: feature/user-profile (Delete test branch: feature/user-profile)

是否继续？(y/n): y

正在切换到 localdevelop 分支...
正在拉取最新代码...
正在删除分支: feature/user-profile...

成功删除分支 feature/user-profile！
操作完成！
```
---

## 使用场景 / Usage Scenarios

### 场景 1: 测试新功能分支
```powershell
# 步骤 1: 拉取并切换到测试分支
.\fetch-test-branch.ps1
# 选择要测试的分支，比如 feature/new-feature

# 步骤 2: 进行测试...
# (在这里运行项目，测试新功能)

# 步骤 3: 测试完成后清理
.\cleanup-test-branch.ps1
```

### 场景 2: 快速切换多个分支进行测试
```powershell
# 测试第一个分支
.\fetch-test-branch.ps1  # 选择 feature/branch-1
# ... 测试 ...
.\cleanup-test-branch.ps1

# 测试第二个分支
.\fetch-test-branch.ps1  # 选择 feature/branch-2
# ... 测试 ...
.\cleanup-test-branch.ps1
```

---

### 3. start-dev.ps1

**功能**: 一键启动项目开发环境，先启动后端（Spring Boot），再启动前端（npm）

**用途**:
When developing locally, start backend and frontend services with a single command

**使用方法**:

```powershell
cd scripts
.\start-dev.ps1
```

**操作流程**:

1. 自动定位项目根目录（无需关心当前执行路径）
2. 在新 PowerShell 窗口中启动后端 Spring Boot 应用（`mvn spring-boot:run`）
3. 等待后端初始化后，在当前控制台启动前端（`npm run serve`）
4. 后端与前端进程相互独立，互不阻塞

**示例输出**:

```
Project root: F:\javaCode\blog-project

Starting backend (Spring Boot)...
Backend started in a new window.

Starting frontend (npm run serve)...

> frontend@0.1.0 serve
> vue-cli-service serve

App running at:
- Local:   http://localhost:3000/
- Network: http://192.168.1.9:3000/
```

**说明**:

* 后端运行在 **独立的 PowerShell 窗口** 中，方便查看日志和单独关闭
* 前端运行在 **新建控制台**，可使用 `Ctrl + C` 停止
* 后端代码修改后，**重新运行脚本即可生效**
* 不涉及打包（jar）或 Docker，始终运行最新源码

---

## 使用场景 / Usage Scenarios

### 场景 1: 测试新功能分支

```powershell
# 步骤 1: 拉取并切换到测试分支
.\fetch-test-branch.ps1
# 选择要测试的分支，比如 feature/new-feature

# 步骤 2: 启动前后端进行测试
.\start-dev.ps1

# 步骤 3: 测试完成后清理
.\cleanup-test-branch.ps1
```

### 场景 2: 日常本地开发

```powershell
# 直接启动完整开发环境
.\start-dev.ps1
```

### 场景 3: 快速切换多个分支进行测试

```powershell
# 测试第一个分支
.\fetch-test-branch.ps1  # 选择 feature/branch-1
.\start-dev.ps1
# ... 测试 ...
.\cleanup-test-branch.ps1

# 测试第二个分支
.\fetch-test-branch.ps1  # 选择 feature/branch-2
.\start-dev.ps1
# ... 测试 ...
.\cleanup-test-branch.ps1
```
---

## 注意事项 / Notes

1. **执行策略**: 如果您是第一次运行 PowerShell 脚本，可能需要设置执行策略：
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

2. **工作目录**: 这些脚本可以在项目的任何位置运行，它们会自动在当前 Git 仓库中执行操作

3. **分支保护**: `cleanup-test-branch.ps1` 脚本会保护 `main`、`master` 和 `localdevelop` 分支，不会将它们列为可删除的分支

4. **未合并更改**: 如果要删除的分支包含未合并的更改，脚本会提示您是否要强制删除

5. **错误处理**: 所有脚本都包含错误处理逻辑，如果操作失败会显示清晰的错误消息

---

## 系统要求 / Requirements

- Windows 操作系统 with PowerShell 5.0+
- Git 已安装并配置
- 在 Git 仓库目录中运行

---

## 故障排除 / Troubleshooting

### 问题: 脚本无法运行，显示"无法加载，因为在此系统上禁止运行脚本"

**解决方案**: 
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 问题: 无法连接到远程仓库

**解决方案**: 
- 检查网络连接
- 确保 Git 远程仓库配置正确: `git remote -v`
- 确保您有访问远程仓库的权限

### 问题: 分支删除失败

**解决方案**: 
- 脚本会提示您是否强制删除
- 或者手动使用: `git branch -D <branch-name>`

---

## 许可 / License

这些脚本是项目的一部分，遵循项目的许可证。
