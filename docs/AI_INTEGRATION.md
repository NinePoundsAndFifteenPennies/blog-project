# AI 功能集成方案

本文档描述了博客项目的 AI 功能集成方案，包括已实现的功能、配置指南和未来规划。

## 项目评估

### 当前技术栈
- **后端**: Spring Boot 3.5.7 + Spring Security + JPA + MySQL + Redis
- **前端**: Vue.js 3 + Vuex + Vue Router + TailwindCSS + Axios
- **认证**: JWT 无状态认证
- **架构**: 前后端分离，RESTful API

### AI 集成切入点分析

基于项目现有功能，AI 能力可以在以下场景带来显著价值：

| 场景 | 价值 | 优先级 |
|------|------|--------|
| **写作辅助** | 帮助用户创作更高质量的文章内容 | ⭐⭐⭐ 高 |
| **文章摘要** | 自动生成文章摘要，提升阅读体验 | ⭐⭐⭐ 高 |
| **标题推荐** | 根据内容推荐吸引人的标题 | ⭐⭐⭐ 高 |
| **标签推荐** | 智能推荐合适的文章标签 | ⭐⭐⭐ 高 |
| 内容审核 | AI 辅助审核文章和评论 | ⭐⭐ 中 |
| 智能推荐 | 个性化文章推荐系统 | ⭐⭐ 中 |
| 评论情感分析 | 分析评论情感倾向 | ⭐ 低 |
| SEO 优化 | 自动生成 SEO 元数据 | ⭐ 低 |

---

## Phase 1: 已实现功能（当前版本）

### 技术选型

**Spring AI + OpenAI**
- 使用 [Spring AI](https://docs.spring.io/spring-ai/reference/) 作为 AI 框架
- 兼容 OpenAI API 格式，支持切换到任何兼容接口（如 DeepSeek、通义千问等）
- 支持流式输出（SSE），写作辅助实时返回

### 架构设计

```
┌─────────────┐      HTTP/SSE       ┌─────────────┐     HTTPS      ┌─────────────┐
│   前端 Vue   │ ◄────────────────► │  后端 API   │ ──────────────► │  OpenAI API │
│  AiAssistant │                    │ AiController │               │  (或兼容API) │
│  组件        │                    │ AiService    │               │             │
└─────────────┘                    └─────────────┘               └─────────────┘
```

### 后端实现

#### 1. 依赖配置 (pom.xml)

```xml
<!-- Spring AI (OpenAI) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- WebFlux for streaming support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

#### 2. 配置文件 (application.properties)

```properties
# OpenAI API Settings
# 通过环境变量设置 API Key（推荐）
spring.ai.openai.api-key=${SPRING_AI_OPENAI_API_KEY:sk-placeholder}
# 如需使用国内兼容 API（如 DeepSeek），修改 base-url
spring.ai.openai.base-url=${SPRING_AI_OPENAI_BASE_URL:https://api.openai.com}

# Chat Model Settings
spring.ai.openai.chat.options.model=${SPRING_AI_OPENAI_MODEL:gpt-4o-mini}
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=2000

# AI Feature Toggle
app.ai.enabled=${AI_ENABLED:true}
```

**环境变量配置方式：**

```bash
# Linux/macOS
export SPRING_AI_OPENAI_API_KEY=your-api-key-here
export SPRING_AI_OPENAI_BASE_URL=https://api.openai.com  # 或其他兼容API地址
export SPRING_AI_OPENAI_MODEL=gpt-4o-mini

# 使用 DeepSeek 示例
export SPRING_AI_OPENAI_API_KEY=your-deepseek-key
export SPRING_AI_OPENAI_BASE_URL=https://api.deepseek.com
export SPRING_AI_OPENAI_MODEL=deepseek-chat
```

#### 3. API 端点

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/ai/status` | 检查 AI 服务状态 | 公开 |
| POST | `/api/ai/summary` | 生成文章摘要 | 需要登录 |
| POST | `/api/ai/suggest-titles` | 推荐文章标题 | 需要登录 |
| POST | `/api/ai/suggest-tags` | 推荐文章标签 | 需要登录 |
| POST | `/api/ai/assist` | 写作辅助（流式） | 需要登录 |

#### 4. 代码结构

```
backend/blog/src/main/java/com/lost/blog/
├── controller/
│   └── AiController.java          # AI API 控制器
├── service/
│   ├── AiService.java             # AI 服务接口
│   └── AiServiceImpl.java         # AI 服务实现
└── dto/
    ├── AiRequest.java             # AI 请求 DTO
    └── AiResponse.java            # AI 响应 DTO
```

### 前端实现

#### 1. API 模块 (src/api/ai.js)

提供以下 API 调用函数：
- `getAiStatus()` - 检查 AI 服务状态
- `generateSummary(content)` - 生成摘要
- `suggestTitles(content)` - 推荐标题
- `suggestTags(title, content)` - 推荐标签
- `assistWriting(prompt, content, onChunk, onDone, onError)` - 流式写作辅助

#### 2. AI 助手组件 (src/components/AiAssistant.vue)

集成在文章编辑页右侧栏，提供：
- **推荐标题**：根据文章内容推荐5个标题，点击即可使用
- **推荐标签**：根据内容推荐5-8个标签，点击即可添加
- **生成摘要**：一键生成文章摘要
- **写作辅助**：输入提示词，AI 实时流式返回写作内容

### 安全设计

- AI 端点（除状态检查外）均需 JWT 认证
- 请求内容做长度限制（内容 ≤ 10000 字，标题 ≤ 200 字，提示词 ≤ 500 字）
- AI 服务可通过 `app.ai.enabled` 开关控制
- API Key 通过环境变量注入，不硬编码

---

## 后续规划

### Phase 2: 内容审核与推荐
- AI 辅助文章审核（自动检测敏感内容）
- 基于内容相似度的文章推荐
- 评论情感分析与自动标记

### Phase 3: 智能搜索与 SEO
- AI 增强搜索（语义搜索）
- 自动生成 SEO 元数据（meta description、关键词）
- 多语言内容翻译

### Phase 4: 个性化体验
- 个性化内容推荐系统
- 用户兴趣画像分析
- 智能通知推送优化
