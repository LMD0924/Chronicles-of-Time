# gateway

## 定位

`gateway` 是拾光记后端统一入口，负责 API 路由转发、JWT 鉴权、白名单放行和跨服务访问边界控制。前台和后台都应优先访问网关，而不是直接访问业务服务端口。

## 端口

```yaml
server:
  port: 8500
```

统一访问前缀：

```text
http://localhost:8500/api
```

## 主要配置

配置文件：

`gateway/src/main/resources/application.yml`

关键配置：

- `spring.cloud.gateway.server.webflux.routes`：路由规则
- `auth.white-list`：无需 JWT 的白名单
- `jwt.secret`、`jwt.expiration`：JWT 校验配置
- `spring.data.redis`：Redis 连接配置

## 当前路由

| 路径 | 目标服务 | 说明 |
| --- | --- | --- |
| `/api/auth/**` | `auth-center` | 登录、注册、刷新、登出、校验 |
| `/api/user/**` | `auth-center` | 当前账号基础资料 |
| `/api/admin/**` | `auth-center` | 后台账号/权限扩展接口 |
| `/api/upload/**` | `file-upload-service` | 文件、图片、批量上传 |
| `/api/userInfo/**` | `user-center` | 用户扩展资料 |
| `/api/resume/**` | `user-center` | 简历主体和子模块 |
| `/api/high/ai/**` | `high-service` | 高中 AI 分析 |
| `/api/selection/**` | `high-service` | 新高考选科 |
| `/api/subject/**`、`/api/subject-combination/**` | `high-service` | 科目和组合 |
| `/api/intention/**`、`/api/guidance/**` | `high-service` | 选科意向、选科指导 |
| `/api/grading/**` | `high-service` | 赋分规则 |
| `/api/major/**` | `high-service` | 高考专业选科要求 |
| `/api/volunteer/**` | `high-service` | 志愿填报、推荐、模拟 |
| `/api/university/**`、`/api/university/major/**` | `university-service` | 大学和专业 |
| `/api/course/**`、`/api/course-category/**` | `university-service` | 课程和课程分类 |
| `/api/student-course/**` | `university-service` | 学生课程和学业进度 |
| `/api/paper/**` | `university-service` | 论文和建议 |
| `/api/growth/**` | `general-service` | 成长记录 |
| `/api/question/**` | `general-service` | 题库、练习、考试 |
| `/api/mistake/**` | `general-service` | 错题本 |
| `/api/score/**` | `general-service` | 成绩记录 |
| `/api/content/**` | `general-service` | 内容社区 |
| `/api/notes/**` | `general-service` | 笔记扩展路径 |
| `/api/graph/**` | `general-service` | 知识图谱 |
| `/api/workplace/**` | `workplace-service` | 职场阶段 |
| `/api/advanced/**` | `advanced-service` | 进阶成长 |

## 鉴权逻辑

过滤器：

`gateway/src/main/java/org/example/gateway/filter/AuthGlobalFilter.java`

逻辑：

1. 读取请求路径。
2. 命中 `auth.white-list` 则直接放行。
3. 读取 `Authorization: Bearer <token>`。
4. 校验 JWT。
5. 校验通过后转发到目标服务。
6. 校验失败返回未授权响应。

默认白名单：

- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/refresh`
- `/actuator/health`

## Redis

网关配置了 Redis：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

如果本地没有 Redis，需要确认鉴权过滤器是否依赖 Redis 状态；若依赖，应先启动 Redis。

## 新增服务路由步骤

1. 服务控制器路径保持 `/api/<module>/**`。
2. 在 `gateway` 的 `routes` 中增加 `Path`。
3. 确认目标服务端口和 `uri`。
4. 前端通过 `/api/<module>` 或 `http://localhost:8500/api/<module>` 调用。
5. 更新对应模块文档。

## 常见问题

- 前端 404：检查网关是否有对应 `Path`。
- 前端 401：检查 token 是否存在、是否过期、接口是否应加入白名单。
- 业务服务可直连但网关失败：检查 `uri` 端口和路径前缀是否一致。
- CORS 或代理问题：后台管理端可通过 `VITE_API_BASE_URL=http://localhost:8500/api` 明确指定网关地址。
