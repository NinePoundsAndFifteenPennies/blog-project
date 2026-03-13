# AI API 文档（第一步基建）

本文档描述当前后端已落地的 AI 基础接口。  
本阶段目标是打通统一网关与供应商抽象链路，返回为**模拟响应**，用于后续真实 LLM 接入前的联调。

## 1. 基础约束

1. 所有 AI 接口都在 `/api/ai` 下。
2. 所有 AI 接口都要求已登录用户（JWT）。
3. 仅允许国产模型供应商：
   - `qwen`（阿里云百炼 / 通义）
   - `deepseek`
   - `kimi`（Moonshot AI）

## 2. 认证方式

- Header:

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

未携带或无效 JWT 时，返回 `401 Unauthorized`。

## 3. 获取可用供应商状态

- **Method**: `GET`
- **Path**: `/api/ai/providers`
- **权限**: 登录用户

### 3.1 响应示例

```json
[
  {
    "provider": "deepseek",
    "baseUrl": "https://api.deepseek.com",
    "model": "deepseek-chat",
    "configured": false,
    "apiKeyHint": "请设置环境变量 AI_DEEPSEEK_API_KEY"
  },
  {
    "provider": "kimi",
    "baseUrl": "https://api.moonshot.cn/v1",
    "model": "moonshot-v1-8k",
    "configured": false,
    "apiKeyHint": "请设置环境变量 AI_KIMI_API_KEY"
  },
  {
    "provider": "qwen",
    "baseUrl": "https://dashscope.aliyuncs.com/compatible-mode/v1",
    "model": "qwen-plus",
    "configured": true,
    "apiKeyHint": "请设置环境变量 AI_QWEN_API_KEY"
  }
]
```

## 4. AI 对话接口（模拟链路）

- **Method**: `POST`
- **Path**: `/api/ai/chat`
- **权限**: 登录用户

### 4.1 请求体

```json
{
  "prompt": "请帮我润色这一段博客开头",
  "provider": "kimi"
}
```

字段说明：
- `prompt`：必填，最大长度 4000。
- `provider`：可选，`qwen` / `deepseek` / `kimi`。不传时走 `ai.default-provider`。

### 4.2 成功响应示例

```json
{
  "provider": "kimi",
  "model": "moonshot-v1-8k",
  "content": "Kimi 基础链路已接通（当前为模拟响应）。收到内容：请帮我润色这一段博客开头",
  "simulated": true
}
```

### 4.3 错误响应示例

统一错误结构沿用项目 `ErrorResponse`：

```json
{
  "timestamp": "2026-03-13T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "不支持的模型提供商: xxx，目前仅支持 deepseek / kimi / qwen",
  "path": "/api/ai/chat"
}
```

常见状态码：
- `400`：请求参数错误（如 provider 不支持）。
- `401`：未登录。
- `503`：供应商未配置 API Key 或暂不可用。

## 5. 配置项与环境变量

`backend/blog/src/main/resources/application.properties`：

```properties
ai.default-provider=qwen

ai.qwen.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1
ai.qwen.model=qwen-plus
ai.qwen.api-key=${AI_QWEN_API_KEY:}

ai.deepseek.base-url=https://api.deepseek.com
ai.deepseek.model=deepseek-chat
ai.deepseek.api-key=${AI_DEEPSEEK_API_KEY:}

ai.kimi.base-url=https://api.moonshot.cn/v1
ai.kimi.model=moonshot-v1-8k
ai.kimi.api-key=${AI_KIMI_API_KEY:}
```

## 6. 后续计划

1. 将 `chat` 从模拟响应切换为真实供应商调用。
2. 增加调用审计字段（requestId、userId、token 消耗、延迟、错误码）。
3. 增加限流、重试、熔断与成本配额策略。
