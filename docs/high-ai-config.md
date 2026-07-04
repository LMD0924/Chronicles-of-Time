# 高中模块 AI 分析配置

高中模块的 AI 分析接口在 `high-service` 中，配置位置：

`back-end-spring-cloud/high-service/src/main/resources/application.yml`

对应配置段：

```yaml
high:
  ai:
    enabled: true
    provider: openai-compatible
    base-url: ${HIGH_AI_BASE_URL:https://api.openai.com/v1}
    model: ${HIGH_AI_MODEL:gpt-4o-mini}
    api-key: ${HIGH_AI_API_KEY:}
    timeout-seconds: ${HIGH_AI_TIMEOUT_SECONDS:30}
    max-tokens: ${HIGH_AI_MAX_TOKENS:1200}
    temperature: ${HIGH_AI_TEMPERATURE:0.2}
```

推荐把大模型 API Key 写到环境变量，不要直接提交到代码仓库：

```powershell
$env:HIGH_AI_API_KEY="你的大模型 API Key"
```

如果使用 OpenAI 兼容接口，可以按需调整：

```powershell
$env:HIGH_AI_BASE_URL="https://api.openai.com/v1"
$env:HIGH_AI_MODEL="gpt-4o-mini"
```

修改环境变量或配置文件后，需要重启 `high-service` 和 `gateway`。

未配置 `HIGH_AI_API_KEY` 时，前端的 AI 分析入口仍可使用，但后端会返回本地规则分析结果，页面会提示当前没有启用大模型分析。
