# 编辑器改进说明 / Editor Improvements

## 概述 / Overview

本次更新大幅提升了 Markdown 和 HTML 编辑器的写作体验，参考了 Notion、Typora、Obsidian 等流行笔记应用的设计理念。

This update significantly improves the Markdown and HTML editor writing experience, inspired by popular note-taking applications like Notion, Typora, and Obsidian.

## 主要功能 / Key Features

### 1. 富文本格式化工具栏 / Rich Formatting Toolbar

新增的工具栏提供了一键插入常用 Markdown 元素的功能：

#### 标题按钮 / Heading Buttons
- **H1**: 插入一级标题 `# 标题 1`
- **H2**: 插入二级标题 `## 标题 2`
- **H3**: 插入三级标题 `### 标题 3`

#### 文本格式化 / Text Formatting
- **B (粗体)**: 插入粗体文本 `**文本**`
- **I (斜体)**: 插入斜体文本 `*文本*`
- **S (删除线)**: 插入删除线文本 `~~文本~~`
- **代码**: 插入行内代码 `` `代码` ``

#### 列表和引用 / Lists and Quotes
- **无序列表**: 插入无序列表 `- 列表项`
- **有序列表**: 插入有序列表 `1. 列表项`
- **引用**: 插入引用块 `> 引用内容`

#### 插入元素 / Insert Elements
- **链接**: 插入链接 `[文本](url)`
- **图片**: 插入图片 `![描述](url)`
- **代码块**: 插入代码块 ` ```language\n代码\n``` `
- **表格**: 插入表格模板
- **分隔线**: 插入水平分隔线 `---`

### 2. 键盘快捷键 / Keyboard Shortcuts

为提高效率，添加了常用的键盘快捷键：

| 快捷键 | 功能 | Markdown 语法 |
|--------|------|---------------|
| `Ctrl/Cmd + B` | 粗体 | `**text**` |
| `Ctrl/Cmd + I` | 斜体 | `*text*` |
| `Ctrl/Cmd + K` | 插入链接 | `[text](url)` |
| `Ctrl/Cmd + \`` | 行内代码 | `` `code` `` |
| `Ctrl/Cmd + 1` | 一级标题 | `# heading` |
| `Ctrl/Cmd + 2` | 二级标题 | `## heading` |
| `Ctrl/Cmd + 3` | 三级标题 | `### heading` |
| `Tab` | 插入两个空格 | 用于缩进 |

### 3. 智能文本处理 / Smart Text Handling

- **选中文本格式化**: 选中文本后点击格式化按钮，会自动用 Markdown 语法包裹选中内容
- **智能光标定位**: 插入格式后自动将光标定位到合适位置，方便继续输入
- **Tab 键支持**: 按 Tab 键插入两个空格，便于代码和列表的缩进

### 4. 用户体验优化 / UX Improvements

- **视觉分组**: 工具栏按钮按功能分组，用分隔线区分
- **悬停提示**: 每个按钮都有 tooltip 提示其功能和快捷键
- **实时字数统计**: 显示当前内容字数
- **仅 Markdown 显示**: 工具栏仅在选择 Markdown 模式时显示

## 技术实现 / Technical Implementation

### 文件修改 / Modified Files

- `frontend/src/views/PostEdit.vue`: 主要修改文件
  - 添加了格式化工具栏 UI
  - 实现了 `insertMarkdown()` 函数处理格式插入
  - 添加了 `handleKeydown()` 函数处理键盘快捷键
  - 新增 `contentTextarea` ref 用于 DOM 操作
  - 添加了工具栏按钮的 CSS 样式

### 代码示例 / Code Example

```vue
<!-- 工具栏按钮示例 -->
<button @click="insertMarkdown('bold')" type="button" class="toolbar-btn" title="粗体 (Ctrl+B)">
  <span class="font-bold">B</span>
</button>

<!-- 键盘快捷键处理 -->
const handleKeydown = (e) => {
  if (e.ctrlKey || e.metaKey) {
    switch (e.key.toLowerCase()) {
      case 'b':
        e.preventDefault()
        insertMarkdown('bold')
        break
      // ... 其他快捷键
    }
  }
}
```

## 兼容性 / Compatibility

- ✅ 与现有功能完全兼容
- ✅ 不影响 HTML 编辑模式
- ✅ 保持原有的预览功能
- ✅ 支持草稿保存和发布功能

## 测试 / Testing

已测试的功能：
- [x] 工具栏按钮点击插入
- [x] 键盘快捷键
- [x] 选中文本格式化
- [x] Tab 键缩进
- [x] 光标位置定位
- [x] 实时预览更新
- [x] 字数统计
- [x] Markdown/HTML 模式切换

## 使用指南 / Usage Guide

### 基本操作 / Basic Operations

1. **选择 Markdown 模式**: 在内容类型选择器中选择 "Markdown"
2. **使用工具栏**: 点击工具栏上的按钮快速插入格式
3. **键盘快捷键**: 使用 Ctrl/Cmd + 快捷键快速格式化
4. **格式化选中文本**: 先选中文本，再点击格式按钮

### 最佳实践 / Best Practices

1. **使用快捷键**: 熟练使用快捷键可大幅提升写作效率
2. **Tab 缩进**: 使用 Tab 键而不是空格键进行缩进
3. **实时预览**: 随时查看右侧预览确认格式正确
4. **选中后格式化**: 先输入文本，选中后再格式化更高效

## 后续计划 / Future Plans

可能的增强功能：
- [ ] 添加更多 Markdown 扩展语法支持（如任务列表）
- [ ] 支持拖拽上传图片
- [ ] 添加 Markdown 模板
- [ ] 实现代码块语言自动补全
- [ ] 添加全屏编辑模式
- [ ] 实现自动保存草稿

## 截图 / Screenshots

### 编辑器工具栏 / Editor Toolbar
![编辑器工具栏](https://github.com/user-attachments/assets/d721f876-686b-4fef-868e-f2b5df0801cc)

### 完整编辑界面 / Complete Editor Interface
![完整编辑界面](https://github.com/user-attachments/assets/c834eb83-c7f8-41fb-8307-1ed0fe8be43a)

## 参考资料 / References

本次改进参考了以下优秀的笔记应用：
- [Notion](https://notion.so) - 富文本编辑器和快捷键设计
- [Typora](https://typora.io) - 所见即所得的 Markdown 编辑体验
- [Obsidian](https://obsidian.md) - 工具栏布局和键盘快捷键

---

**作者**: GitHub Copilot Agent  
**日期**: 2025-10-12  
**版本**: 1.0
