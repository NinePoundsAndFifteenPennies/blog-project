# 博客项目 AI 功能集成 —— 详细计划书

## 一、项目现状评估

### 1.1 技术栈概览

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.7-SNAPSHOT |
| 编程语言 | Java | 17 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis (Lettuce) | — |
| ORM | Spring Data JPA / Hibernate | — |
| 认证 | Spring Security + JWT (JJWT 0.11.5) | — |
| 前端框架 | Vue.js | 3.2.13 |
| 状态管理 | Vuex | 4.0.2 |
| 路由 | Vue Router | 4.0.3 |
| HTTP 客户端 | Axios | 0.27.2 |
| CSS 框架 | TailwindCSS | 3.0.24 |
| Markdown | marked + highlight.js | 4.3.0 / 11.8.0 |
| 图表 | Chart.js + vue-chartjs | 4.5.1 / 5.3.3 |
| 构建工具 | Maven (后端) / Vue CLI (前端) | — |

### 1.2 已有功能模块

| 模块 | 功能 | 涉及的 Controller / 组件 |
|------|------|--------------------------|
| 用户系统 | 注册、登录、JWT 认证、个人资料、头像上传 | UserController, FileController |
| 文章系统 | 创建、编辑、草稿、发布、Markdown/HTML 编辑器、封面图、浏览量统计 | PostController, PostEdit.vue |
| 评论系统 | 三级嵌套评论、回复、编辑、点赞 | CommentController |
| 社交系统 | 关注/取关、粉丝列表、互关好友、隐私设置 | FollowController |
| 点赞系统 | 文章和评论点赞 | LikeController |
| 私信系统 | 私信对话、未读计数、防骚扰 | PrivateMessageController |
| 通知系统 | 11种通知类型、已读标记、类型筛选 | NotificationController |
| 搜索系统 | 多维度搜索（关键词、作者、标题、标签）、排序 | PostController (search) |
| 标签/分类 | CRUD、热门标签、文章关联 | TagController, CategoryController |
| 热门作者 | 加权对数热度排名 | HotAuthorController |
| 管理后台 | 仪表盘、用户/文章/评论/标签/分类管理、审核流程 | AdminController |
| 数据统计 | 社区统计、管理后台图表 | StatisticsController, DashboardService |

### 1.3 架构特点

```
┌─────────────┐      HTTP/JSON       ┌─────────────┐
│   Vue.js    │ ◄──────────────────► │  Spring Boot │
│  (Port 3000)│                      │  (Port 8080) │
└─────────────┘                      └──────┬───────┘
                                            │
                       ┌────────────────────┼────────────────────┐
                       ▼                    ▼                    ▼
                ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
                │   MySQL     │     │   Redis     │     │ File System │
                └─────────────┘     └─────────────┘     └─────────────┘
```

- **前后端分离** + RESTful API
- **三层架构**：Controller → Service (接口+实现) → Repository
- **JWT 无状态认证**，支持 Token 刷新
- **Redis 缓存**加速热点数据
- **AdminForm 审计跟踪**，完整的内容审核流

---

## 二、AI 框架选型对比

### 2.1 Java 生态的大模型应用开发框架

目前 Java 生态中有三个主流的大模型应用开发框架，以下是详细对比：

#### ① Spring AI

| 维度 | 说明 |
|------|------|
| **官方背景** | Spring 官方项目，Pivotal/VMware 维护 |
| **最新版本** | 1.0.0 (2025年5月正式GA) |
| **定位** | Spring 生态的 AI 抽象层，类似 Spring Data 对数据库的抽象 |
| **核心能力** | ChatClient（对话）、Embedding、Image、Audio、向量数据库集成 |
| **支持模型** | OpenAI、Azure OpenAI、Anthropic、Google Vertex AI、Ollama（本地）、Mistral、MiniMax、ZhiPu 等 |
| **高级特性** | 流式输出(SSE)、Function Calling、Structured Output(JSON映射)、RAG(检索增强生成) |
| **Spring 集成** | 原生集成，自动配置、依赖注入、属性配置，学习成本极低 |
| **优势** | ✅ 与 Spring Boot 无缝集成，配置驱动 ✅ 社区活跃，文档完善 ✅ 已 GA，生产可用 ✅ 支持向量数据库（Milvus、PgVector、Redis等） |
| **劣势** | ❌ 相比 LangChain4j 的"编排"能力偏弱 ❌ Agent/Chain 高级编排功能还在发展中 |

#### ② LangChain4j

| 维度 | 说明 |
|------|------|
| **官方背景** | 社区开源项目，Python LangChain 的 Java 移植 |
| **最新版本** | 1.0.0-beta1 (2025年) |
| **定位** | 完整的 LLM 应用编排框架，类似 Python LangChain |
| **核心能力** | Chat/Embedding + Chain（链式调用）+ Agent（自主决策）+ Memory（对话记忆）+ Tool（工具调用） |
| **支持模型** | OpenAI、Anthropic、Google、Ollama、DashScope（阿里）、HuggingFace 等 |
| **高级特性** | AI Service（声明式接口）、RAG Pipeline、Chain of Thought、Agent 自主决策、Memory 持久化 |
| **Spring 集成** | 有 `langchain4j-spring-boot-starter`，但集成深度不如 Spring AI |
| **优势** | ✅ 功能最丰富（Chain、Agent、Memory） ✅ 类似 Python LangChain 的理念，AI 应用开发者熟悉 ✅ AI Service 声明式接口，简化开发 ✅ 文档和示例丰富 |
| **劣势** | ❌ 仍处于 Beta 阶段，API 可能变动 ❌ 非 Spring 官方，长期维护存在不确定性 ❌ 与 Spring 的集成是"外挂式"而非原生 |

#### ③ Semantic Kernel (Java)

| 维度 | 说明 |
|------|------|
| **官方背景** | 微软开源，C# 为主，Java 为次要实现 |
| **定位** | 微软 AI 应用开发框架的 Java 移植 |
| **核心能力** | Kernel（核心编排）、Plugin（插件）、Planner（规划器）、Memory |
| **优势** | ✅ 微软背书 ✅ Plugin 架构设计良好 |
| **劣势** | ❌ Java 版本功能落后于 C# ❌ 社区活跃度低 ❌ 不适合 Spring 生态 |

### 2.2 框架选型建议

| 场景 | 推荐框架 | 理由 |
|------|---------|------|
| **本项目（博客AI功能）** | **Spring AI** ⭐ | 项目已是 Spring Boot 架构，Spring AI 原生集成最顺滑，且当前需求（对话、生成、摘要）不需要复杂的 Chain/Agent |
| 需要复杂 AI 编排（Agent 自主决策、多步推理链） | **LangChain4j** | Chain/Agent/Memory 体系成熟 |
| 后续若需 Agent 能力 | **Spring AI + LangChain4j 混用** | Spring AI 做基础调用，LangChain4j 做复杂编排 |
| 微软 Azure 生态 | Semantic Kernel | 仅推荐深度 Azure 用户 |

**对于本项目的推荐结论：**

> **首选 Spring AI**。理由：
> 1. 项目已使用 Spring Boot 3.5.7，Spring AI 是原生集成，零额外配置成本
> 2. 当前 Phase 1 的需求（文章摘要、标题推荐、标签推荐、写作辅助）都是简单的 Prompt→Response 模式，不需要 LangChain4j 的复杂编排
> 3. Spring AI 已经 GA 1.0.0，生产可用
> 4. 如果后续 Phase 2+ 需要 RAG（检索增强生成）或 Agent 能力，Spring AI 已原生支持 RAG，也可以按需引入 LangChain4j 做补充

---

## 三、AI 功能规划

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 Vue.js                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────────┐  │
│  │ AI 助手  │  │ 智能搜索 │  │ 内容审核 │  │ 个性化推荐    │  │
│  │ 写作辅助 │  │ 语义搜索 │  │ 情感分析 │  │ 用户画像      │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────┬────────┘  │
│       │              │              │               │           │
│       └──────────────┴──────────────┴───────────────┘           │
│                              │  HTTP/SSE                        │
└──────────────────────────────┼──────────────────────────────────┘
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                     后端 Spring Boot                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    AiController                           │   │
│  │  /api/ai/summary  /api/ai/suggest-*  /api/ai/assist(SSE) │   │
│  └──────────────────────┬───────────────────────────────────┘   │
│                         ▼                                        │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    AiService                              │   │
│  │  - 文章摘要生成    - 标题/标签推荐    - 写作辅助          │   │
│  │  - 内容审核       - 情感分析        - SEO 优化           │   │
│  └──────────────────────┬───────────────────────────────────┘   │
│                         ▼                                        │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Spring AI ChatClient                         │   │
│  │  统一 AI 模型抽象层，支持切换不同模型提供商                  │   │
│  └──────────────────────┬───────────────────────────────────┘   │
│                         ▼                                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ OpenAI   │  │ DeepSeek │  │  通义千问 │  │ Ollama(本地) │   │
│  │ GPT-4o   │  │ V3       │  │  Qwen     │  │ Llama等      │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### 3.2 分阶段实施计划

---

### Phase 1：写作辅助（核心功能）✅ 优先实施

**目标**：为文章编辑器集成 AI 写作辅助能力

**功能列表**：

| 功能 | 说明 | 技术实现 |
|------|------|----------|
| 文章摘要生成 | 根据文章内容自动生成 150 字以内的摘要 | Spring AI ChatClient，单次请求 |
| 标题推荐 | 根据内容推荐 5 个吸引人的标题 | Spring AI ChatClient，返回列表 |
| 标签推荐 | 根据内容智能推荐 5-8 个标签 | Spring AI ChatClient，返回列表 |
| 写作辅助 | 根据提示词续写、润色、扩写内容 | Spring AI ChatClient，SSE 流式输出 |

**后端新增文件**：

| 文件 | 说明 |
|------|------|
| `pom.xml` 修改 | 添加 `spring-ai-openai-spring-boot-starter` + `spring-boot-starter-webflux` 依赖 |
| `application.properties` 修改 | 添加 AI 配置项（API Key、Base URL、Model、Temperature 等），全部通过环境变量注入 |
| `AiController.java` | 新增控制器，提供 `/api/ai/status`(GET)、`/summary`(POST)、`/suggest-titles`(POST)、`/suggest-tags`(POST)、`/assist`(POST,SSE) |
| `AiService.java` | 新增服务接口 |
| `AiServiceImpl.java` | 服务实现，使用 `ChatClient.Builder` 构建请求，包含系统提示词、内容截断、开关控制 |
| `AiRequest.java` | 请求 DTO，含 `content`、`title`、`prompt` 字段，带 Jakarta Validation 校验 |
| `AiResponse.java` | 响应 DTO，含 `result`(文本) 和 `suggestions`(列表) |
| `SecurityConfig.java` 修改 | 添加 `/api/ai/status` 公开访问，其余 AI 端点需认证 |

**前端新增文件**：

| 文件 | 说明 |
|------|------|
| `src/api/ai.js` | AI API 模块，包含各 AI 接口调用，写作辅助使用原生 `fetch` + `ReadableStream` 处理 SSE |
| `src/components/AiAssistant.vue` | AI 助手组件，集成在文章编辑器右侧栏 |
| `PostEdit.vue` 修改 | 引入 `AiAssistant` 组件，添加事件处理（标题/标签/内容更新） |

**配置方式**：

```bash
# 必选：API Key（通过环境变量注入，不硬编码）
export SPRING_AI_OPENAI_API_KEY=your-api-key

# 可选：自定义 API 地址（兼容 DeepSeek / 通义千问等 OpenAI 兼容接口）
export SPRING_AI_OPENAI_BASE_URL=https://api.openai.com
# 或
export SPRING_AI_OPENAI_BASE_URL=https://api.deepseek.com

# 可选：模型名
export SPRING_AI_OPENAI_MODEL=gpt-4o-mini
# 或
export SPRING_AI_OPENAI_MODEL=deepseek-chat

# 可选：功能开关
export AI_ENABLED=true
```

**安全设计**：
- API Key 仅在后端使用，通过环境变量注入，绝不暴露给前端
- AI 端点（除状态检查）需要 JWT 认证
- 请求内容做长度限制（内容 ≤ 10000 字、标题 ≤ 200 字、提示词 ≤ 500 字）
- 服务端可通过 `app.ai.enabled` 全局开关控制

**交互设计**：
- AI 助手面板位于文章编辑器右侧栏（预览区下方）
- 三个快捷按钮：推荐标题、推荐标签、生成摘要
- 自由文本框用于写作辅助提示词输入
- 流式输出实时展示 AI 生成结果
- 标题建议点击即可替换当前标题
- 标签建议点击即可添加到文章标签列表
- 摘要可一键复制
- 写作内容可插入到文章末尾

**预估工作量**：3-5 天

---

### Phase 2：内容审核与质量提升

**目标**：利用 AI 提升内容质量和审核效率

| 功能 | 说明 |
|------|------|
| AI 辅助内容审核 | 文章提交审核时，AI 预检敏感内容、违规信息，辅助管理员决策 |
| 评论自动过滤 | 评论发布前 AI 检测垃圾内容、广告、攻击性言论 |
| 文章质量评分 | 对文章的可读性、结构完整性、原创性进行评分 |
| 写作建议 | 发布前提供改进建议（结构优化、语法检查、表达润色） |

---

### Phase 3：智能搜索与发现

**目标**：升级搜索系统，提供智能内容发现

| 功能 | 说明 |
|------|------|
| 语义搜索 | 基于向量数据库的语义搜索，替代关键词匹配（Spring AI Embedding + 向量数据库） |
| 相似文章推荐 | 文章详情页展示相似内容推荐 |
| 智能分类 | 新文章自动归类到最匹配的分类 |
| SEO 优化 | 自动生成 SEO 友好的 meta description 和关键词 |

---

### Phase 4：个性化与智能交互

**目标**：构建个性化体验和智能交互

| 功能 | 说明 |
|------|------|
| 个性化推荐 | 基于用户阅读历史、关注关系、点赞行为的个性化文章推荐流 |
| AI 聊天助手 | 站内 AI 聊天机器人，解答博客使用问题、推荐内容 |
| 评论情感分析 | 展示评论区情感倾向（正面/中性/负面），辅助作者了解读者反馈 |
| 智能通知摘要 | AI 总结一段时间内的通知，生成摘要推送 |

---

## 四、模型提供商兼容性

Spring AI 的 OpenAI starter 支持所有兼容 OpenAI API 格式的提供商，切换只需修改环境变量：

| 提供商 | base-url | 推荐模型 | 特点 |
|--------|----------|----------|------|
| OpenAI | `https://api.openai.com` | gpt-4o-mini / gpt-4o | 综合能力最强 |
| DeepSeek | `https://api.deepseek.com` | deepseek-chat / deepseek-reasoner | 性价比极高，中文好 |
| 通义千问 (DashScope) | `https://dashscope.aliyuncs.com/compatible-mode` | qwen-plus / qwen-turbo | 阿里云，国内访问快 |
| 智谱 AI | `https://open.bigmodel.cn/api/paas` | glm-4-flash | 国产，中文理解好 |
| Ollama (本地) | `http://localhost:11434` | llama3 / qwen2 | 完全本地运行，无需API Key |

---

## 五、总结

| 阶段 | 功能 | 框架 | 优先级 | 预估工作量 |
|------|------|------|--------|-----------|
| **Phase 1** | 写作辅助（摘要、标题、标签、续写） | Spring AI | ⭐⭐⭐ 最高 | 3-5 天 |
| **Phase 2** | 内容审核、质量评分 | Spring AI | ⭐⭐ 高 | 5-7 天 |
| **Phase 3** | 语义搜索、相似推荐、SEO | Spring AI + 向量数据库 | ⭐⭐ 中 | 7-10 天 |
| **Phase 4** | 个性化推荐、AI 聊天、情感分析 | Spring AI (可选混合 LangChain4j) | ⭐ 低 | 10+ 天 |

**核心结论**：
- **框架选择 Spring AI**，原生适配当前 Spring Boot 架构，且已 GA 可用于生产
- 若后续有复杂的 Agent/Chain 编排需求，可按需引入 LangChain4j 做补充
- Phase 1 聚焦写作辅助，是 ROI 最高的切入点
- 所有配置通过环境变量管理，支持一键切换不同 AI 提供商
