# 编辑器改进说明 / Editor Improvements

## 概述 / Overview

本次更新大幅提升了 Markdown 和 HTML 编辑器的写作体验，参考了 Notion、Typora、Obsidian 等流行笔记应用的设计理念。

This update significantly improves the Markdown and HTML editor writing experience, inspired by popular note-taking applications like Notion, Typora, and Obsidian.

## 最新更新 (2025-10-13) / Latest Updates

### Bug 修复 / Bug Fixes

1. ✅ **有序列表序号显示** - 修复预览中有序列表不显示序号的问题，现在正确显示 1. 2. 3. ...
2. ✅ **代码语言选择器定位** - 修复模态框显示位置问题，现在正确居中显示
3. ✅ **任务列表自动延续** - 修复只自增 `-` 的 bug，现在正确自增 `- [ ]`
4. ✅ **引用块换行** - 优化引用块的 Enter 键行为，支持多行引用

### 新增功能 / New Features (2025-10-12)

1. **代码语言选择器** - 点击代码块按钮弹出语言选择器，支持 24 种编程语言
2. **代码语法高亮** - 集成 highlight.js，自动高亮代码块，使用 Atom One Dark 主题
3. **HTML 编辑器工具栏** - 为 HTML 模式添加了完整的格式化工具栏
4. **列表自动延续** - 有序列表、无序列表和任务列表按 Enter 自动创建下一项
5. **引用块智能处理** - 按 Enter 自动延续引用，连续两次空引用退出
6. **任务列表支持** - 新增任务列表按钮和自动延续功能
7. **表格边框优化** - 表格预览显示清晰的边框和样式

## 主要功能 / Key Features

### 1. 富文本格式化工具栏 / Rich Formatting Toolbar

#### Markdown 工具栏

**标题按钮 / Heading Buttons**
- **H1**: 插入一级标题 `# 标题 1` (Ctrl+1)
- **H2**: 插入二级标题 `## 标题 2` (Ctrl+2)
- **H3**: 插入三级标题 `### 标题 3` (Ctrl+3)

**文本格式化 / Text Formatting**
- **B (粗体)**: 插入粗体文本 `**文本**` (Ctrl+B)
- **I (斜体)**: 插入斜体文本 `*文本*` (Ctrl+I)
- **S (删除线)**: 插入删除线文本 `~~文本~~`
- **代码**: 插入行内代码 `` `代码` `` (Ctrl+`)

**列表和引用 / Lists and Quotes**
- **无序列表**: 插入无序列表 `- 列表项`
  - 按 Enter 自动创建新列表项
  - 空行按 Enter 退出列表
- **有序列表**: 插入有序列表 `1. 列表项`
  - 按 Enter 自动创建下一项，序号自动递增
  - 空行按 Enter 退出列表
- **任务列表**: 插入任务列表 `- [ ] 任务项` ✨ NEW
  - 按 Enter 自动创建新任务项
  - 空行按 Enter 退出列表
- **引用**: 插入引用块 `> 引用内容`
  - 按 Enter 自动延续引用
  - 空行按 Enter 退出引用

**插入元素 / Insert Elements**
- **链接**: 插入链接 `[文本](url)` (Ctrl+K)
- **图片**: 插入图片 `![描述](url)`
- **代码块**: 插入代码块并选择语言 ✨ NEW
  - 点击代码块按钮弹出语言选择器
  - 支持 24 种编程语言
  - 自动语法高亮（Atom One Dark 主题）
  - 支持语言：JavaScript, TypeScript, Python, Java, C++, Go, Rust, PHP, Ruby, Swift, Kotlin, HTML, CSS, JSON, SQL, Bash 等
- **表格**: 插入表格模板
- **分隔线**: 插入水平分隔线 `---`

#### HTML 工具栏 ✨ NEW

**标题标签 / Heading Tags**
- **H1, H2, H3**: 插入 HTML 标题 `<h1>标题</h1>`

**文本格式 / Text Formatting**
- **B (粗体)**: `<strong>文本</strong>` (Ctrl+B)
- **I (斜体)**: `<em>文本</em>` (Ctrl+I)
- **U (下划线)**: `<u>文本</u>`
- **代码**: `<code>代码</code>`

**列表和结构 / Lists and Structure**
- **无序列表**: `<ul><li>列表项</li></ul>`
- **有序列表**: `<ol><li>列表项</li></ol>`
- **引用**: `<blockquote>引用内容</blockquote>`
- **段落**: `<p>段落内容</p>`

**媒体和其他 / Media and Others**
- **链接**: `<a href="url">文本</a>` (Ctrl+K)
- **图片**: `<img src="url" alt="描述" />`
- **预格式化**: `<pre><code>代码</code></pre>`
- **表格**: 完整的 HTML 表格模板
- **容器**: `<div>内容</div>`
- **分隔线**: `<hr />`

### 2. 键盘快捷键 / Keyboard Shortcuts

为提高效率，添加了常用的键盘快捷键（同时支持 Markdown 和 HTML 模式）：

| 快捷键 | Markdown | HTML | 功能 |
|--------|----------|------|------|
| `Ctrl/Cmd + B` | `**text**` | `<strong>text</strong>` | 粗体 |
| `Ctrl/Cmd + I` | `*text*` | `<em>text</em>` | 斜体 |
| `Ctrl/Cmd + K` | `[text](url)` | `<a href="url">text</a>` | 链接 |
| `Ctrl/Cmd + \`` | `` `code` `` | - | 行内代码 |
| `Ctrl/Cmd + 1` | `# heading` | `<h1>heading</h1>` | 一级标题 |
| `Ctrl/Cmd + 2` | `## heading` | `<h2>heading</h2>` | 二级标题 |
| `Ctrl/Cmd + 3` | `### heading` | `<h3>heading</h3>` | 三级标题 |
| `Tab` | 插入两个空格 | 插入两个空格 | 缩进 |
| `Enter` | **列表/引用自动延续** ✨ | - | 智能换行 |

### 3. 智能文本处理 / Smart Text Handling

#### 列表自动延续 ✨ NEW

**有序列表自动序号递增：**
```markdown
1. 第一项 [按 Enter]
2. [自动生成，光标在这里]
```

**无序列表自动创建：**
```markdown
- 列表项 [按 Enter]
- [自动生成，光标在这里]
```

**任务列表自动创建：**
```markdown
- [ ] 任务项 [按 Enter]
- [ ] [自动生成，光标在这里]
```

**空行退出列表：**
- 在空列表项上按 Enter 自动退出列表模式

#### 引用块智能处理 ✨ NEW

**引用自动延续：**
```markdown
> 引用内容 [按 Enter]
> [自动生成，光标在这里]
```

**空行退出引用：**
- 在空引用行上按 Enter 自动退出引用模式

#### 其他智能功能

- **选中文本格式化**: 选中文本后点击格式化按钮，会自动用 Markdown/HTML 语法包裹选中内容
- **智能光标定位**: 插入格式后自动将光标定位到合适位置，方便继续输入
- **Tab 键支持**: 按 Tab 键插入两个空格，便于代码和列表的缩进

### 4. 用户体验优化 / UX Improvements

#### 表格样式增强 ✨ NEW

- **清晰的边框**: 表格单元格显示清晰的边框线
- **表头样式**: 表头有灰色背景色区分
- **悬停效果**: 鼠标悬停时行背景高亮
- **对齐方式**: 单元格内容左对齐，更易阅读

#### 其他优化

- **视觉分组**: 工具栏按钮按功能分组，用分隔线区分
- **悬停提示**: 每个按钮都有 tooltip 提示其功能和快捷键
- **实时字数统计**: 显示当前内容字数
- **模式切换**: Markdown 和 HTML 分别显示对应的工具栏

## 技术实现 / Technical Implementation

### 文件修改 / Modified Files

#### `frontend/src/views/PostEdit.vue`

**新增功能：**
1. HTML 格式化工具栏 UI
2. `insertHTML()` 函数 - 处理 HTML 标签插入
3. 增强的 `handleKeydown()` 函数 - 支持 Enter 键智能处理
   - 有序列表序号自增
   - 无序列表自动延续
   - 任务列表自动延续
   - 引用块自动延续
   - 空行自动退出
4. 键盘快捷键同时支持 Markdown 和 HTML 模式

**代码示例：**
```javascript
// Enter 键智能处理 - 有序列表序号自增
const olMatch = currentLine.match(/^(\s*)(\d+)\.\s+(.*)$/)
if (olMatch) {
  const num = parseInt(olMatch[2])
  // 继续列表，序号自增
  formData.content = beforeText + '\n' + indent + (num + 1) + '. ' + afterText
}

// HTML 标签插入
const insertHTML = (tag) => {
  switch (tag) {
    case 'strong':
      insertText = `<strong>${selectedText || '粗体文本'}</strong>`
      break
    // ... 其他标签
  }
}
```

#### `frontend/src/assets/main.css`

**新增样式：**
```css
/* 表格样式 */
.markdown-body table {
  @apply w-full border-collapse mb-4;
}

.markdown-body table th,
.markdown-body table td {
  @apply border border-gray-300 px-4 py-2 text-left;
}

.markdown-body table thead {
  @apply bg-gray-100;
}

.markdown-body table tr:hover {
  @apply bg-gray-50;
}

/* 有序列表序号显示修复 */
.markdown-body ol {
  @apply ml-6 mb-4;
  list-style-type: decimal;  /* 显示数字序号 */
}

.markdown-body ul {
  @apply ml-6 mb-4;
  list-style-type: disc;  /* 显示圆点 */
}

.markdown-body li {
  @apply mb-2;
  display: list-item;  /* 确保列表项正确显示 */
}
```

**代码语言选择器模态框：**
```vue
<div v-if="showLanguageModal" 
     class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
     @click.self="showLanguageModal = false">
  <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative">
    <!-- 语言选择下拉框 -->
    <select v-model="codeLanguage" class="w-full px-3 py-2 border rounded-lg">
      <option v-for="lang in availableLanguages" :value="lang.value">
        {{ lang.label }}
      </option>
    </select>
  </div>
</div>
```
- 使用 `z-[9999]` 确保模态框始终在最上层
- `fixed inset-0` 确保覆盖整个视口
- `flex items-center justify-center` 确保内容居中显示

## 兼容性 / Compatibility

- ✅ 与现有功能完全兼容
- ✅ 不影响 HTML 编辑模式（现在 HTML 模式也有工具栏）
- ✅ 保持原有的预览功能
- ✅ 支持草稿保存和发布功能
- ✅ 支持所有主流浏览器

## 测试 / Testing

已测试的功能：
- [x] Markdown 工具栏按钮点击插入
- [x] HTML 工具栏按钮点击插入 ✨ NEW
- [x] 键盘快捷键（Markdown 和 HTML）
- [x] 选中文本格式化
- [x] Tab 键缩进
- [x] **有序列表序号显示修复** ✅ FIXED
- [x] 有序列表 Enter 键序号自增 ✨ NEW
- [x] 无序列表 Enter 键自动延续 ✨ NEW
- [x] **任务列表 Enter 键正确自增 `- [ ]`** ✅ FIXED
- [x] 引用块 Enter 键自动延续 ✨ NEW
- [x] 空行 Enter 键退出列表/引用 ✨ NEW
- [x] **代码语言选择器居中显示** ✅ FIXED
- [x] 代码语法高亮显示 ✨ NEW
- [x] 光标智能定位
- [x] 实时预览更新
- [x] 表格边框显示 ✨ NEW
- [x] 字数统计
- [x] Markdown/HTML 模式切换
- [x] 光标智能定位
- [x] 实时预览更新
- [x] 表格边框显示 ✨ NEW
- [x] 字数统计
- [x] Markdown/HTML 模式切换

## 使用指南 / Usage Guide

### 基本操作 / Basic Operations

1. **选择编辑模式**: 在内容类型选择器中选择 "Markdown" 或 "HTML"
2. **使用工具栏**: 点击工具栏上的按钮快速插入格式
3. **键盘快捷键**: 使用 Ctrl/Cmd + 快捷键快速格式化
4. **格式化选中文本**: 先选中文本，再点击格式按钮
5. **列表自动延续**: 在列表项中按 Enter 自动创建下一项
6. **退出列表**: 在空列表项上按 Enter 退出列表模式

### 最佳实践 / Best Practices

1. **使用快捷键**: 熟练使用快捷键可大幅提升写作效率
2. **Tab 缩进**: 使用 Tab 键而不是空格键进行缩进
3. **实时预览**: 随时查看右侧预览确认格式正确
4. **选中后格式化**: 先输入文本，选中后再格式化更高效
5. **列表编写**: 利用自动延续功能快速创建多项列表
6. **表格使用**: 使用工具栏插入表格模板，然后修改内容

### 进阶技巧 / Advanced Tips

1. **有序列表序号管理**: 系统会自动管理序号递增，无需手动输入
2. **嵌套列表**: 使用 Tab 键缩进创建嵌套列表
3. **任务列表**: 使用任务列表管理待办事项，点击预览中的复选框可切换状态
4. **引用块**: 在引用中按 Enter 继续引用，适合长段引用
5. **混合格式**: 可以在文章中混合使用各种格式元素

## 后续计划 / Future Plans

可能的增强功能：
- [ ] **斜线命令菜单** - 输入 `/` 触发快捷插入菜单（类似 Notion）
- [ ] 添加更多 Markdown 扩展语法支持（如脚注、定义列表）
- [ ] 支持拖拽上传图片
- [ ] 添加 Markdown 模板
- [ ] 实现代码块语言自动补全
- [ ] 添加全屏编辑模式
- [ ] 实现自动保存草稿

## 参考资料 / References

本次改进参考了以下优秀的笔记应用：
- [Notion](https://notion.so) - 富文本编辑器、列表自动延续和快捷键设计
- [Typora](https://typora.io) - 所见即所得的 Markdown 编辑体验
- [Obsidian](https://obsidian.md) - 工具栏布局和键盘快捷键

---

**作者**: GitHub Copilot Agent  
**日期**: 2025-10-12  
**版本**: 2.0
