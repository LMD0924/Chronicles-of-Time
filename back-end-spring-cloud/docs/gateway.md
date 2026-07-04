# gateway

## 定位

统一 API 网关，负责请求入口、路由转发、跨域、JWT 鉴权过滤和白名单放行。

## 端口

- `8500`

## 主要路由

| 路径 | 目标服务 |
| --- | --- |
| `/api/auth/**`、`/api/user/**`、`/api/admin/**` | `auth-center` |
| `/api/upload/**` | `file-upload-service` |
| `/api/resume/**`、`/api/userInfo/**` | `user-center` |
| `/api/volunteer/**`、`/api/selection/**`、`/api/subject/**` 等 | `high-service` |
| `/api/university/**`、`/api/paper/**`、`/api/course/**`、`/api/student-course/**` | `university-service` |
| `/api/growth/**`、`/api/mistake/**`、`/api/question/**`、`/api/score/**`、`/api/content/**`、`/api/graph/**` | `general-service` |
| `/api/workplace/**` | `workplace-service` |
| `/api/advanced/**` | `advanced-service` |

## 鉴权

白名单默认包含登录、注册、刷新 Token 和健康检查接口，其余接口通过 JWT 过滤器校验。