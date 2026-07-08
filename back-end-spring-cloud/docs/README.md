# 后端模块文档总览

`back-end-spring-cloud` 是拾光记后端父工程，采用 Maven 多模块组织。它负责提供统一 API 网关、认证中心、文件上传、用户资料、高中升学规划、大学学业规划、学习内容、职场阶段和进阶成长等服务。

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 运行时 | Java 17+ |
| 框架 | Spring Boot 3.5、Spring Cloud 2025 |
| 数据访问 | MyBatis-Plus、MySQL Connector/J |
| 鉴权 | JWT |
| 网关 | Spring Cloud Gateway |
| 缓存/登录态 | Redis |
| 构建 | Maven |
| 工具 | Lombok、Jackson |

## 工程模块

| 模块 | 文档 | 类型 | 职责 |
| --- | --- | --- | --- |
| `common-core` | [common-core.md](common-core.md) | 公共库 | JWT、Jackson、Bean 工具等基础能力 |
| `common-db` | [common-db.md](common-db.md) | 公共库 | 统一响应、结果码、MyBatis-Plus 配置 |
| `auth-center` | [auth-center.md](auth-center.md) | 服务 | 登录、注册、Token、基础账号资料 |
| `auth-center` | [admin-users.md](admin-users.md) | 服务 | 后台个人用户管理、注册字段映射、角色绑定 |
| `gateway` | [gateway.md](gateway.md) | 服务 | 统一入口、路由转发、JWT 鉴权 |
| `file-upload` | [file-upload.md](file-upload.md) | 服务 | 文件、图片、文档上传和访问 |
| `user-center` | [user-center.md](user-center.md) | 服务 | 用户扩展资料和简历 |
| `high-service` | [high-service.md](high-service.md) | 服务 | 高中选科、赋分、志愿、AI 分析 |
| `university-service` | [university-service.md](university-service.md) | 服务 | 专业、课程、学业进度、论文 |
| `general-service` | [general-service.md](general-service.md) | 服务 | 成长记录、学习题库、错题、成绩、内容 |
| `general-service` | [activity-chat.md](activity-chat.md) | 服务 | 每日打卡、在线时长、勋章发放、好友聊天和群聊 |
| `workplace-service` | [workplace-service.md](workplace-service.md) | 服务 | 职场档案、目标、任务、面试、复盘 |
| `advanced-service` | [advanced-service.md](advanced-service.md) | 服务 | 成长路线、里程碑、技能和导师会话 |

## 服务端口

| 服务 | 端口 | 网关路径 |
| --- | --- | --- |
| `auth-center` | 网关指向 `8080` | `/api/auth/**`、`/api/user/**`、`/api/admin/**` |
| `gateway` | `8500` | `/api/**` |
| `file-upload-service` | `8090` | `/api/upload/**` |
| `user-center` | `8081` | `/api/userInfo/**`、`/api/resume/**` |
| `high-service` | `8082` | `/api/selection/**`、`/api/volunteer/**`、`/api/high/ai/**` 等 |
| `university-service` | `8083` | `/api/university/**`、`/api/course/**`、`/api/paper/**` 等 |
| `general-service` | `8084` | `/api/growth/**`、`/api/activity/**`、`/api/chat/**`、`/api/question/**`、`/api/content/**` 等 |
| `workplace-service` | `8085` | `/api/workplace/**` |
| `advanced-service` | `8086` | `/api/advanced/**` |

## 数据库边界

| 数据库 | 主要服务 | 说明 |
| --- | --- | --- |
| `cot_identity` | `auth-center` | 账号、角色、权限、刷新 Token、登录审计 |
| `cot_profile` | `user-center` | 用户资料、人生阶段、简历和简历子模块 |
| `cot_content` | `general-service` | 成长记录、内容发布、评论、点赞、收藏 |
| `cot_learning` | `general-service` | 题库、练习、答题、错题、成绩、知识点 |
| `cot_highschool` | `high-service` | 新高考选科、赋分、院校专业、志愿方案 |
| `cot_university` | `university-service` | 大学专业、课程树、学生课程、毕业进度、论文 |
| `cot_workplace` | `workplace-service` | 职场阶段扩展数据 |
| `cot_advanced` | `advanced-service` | 进阶成长扩展数据 |
| `cot_platform` | `file-upload`、平台能力 | 文件资产、字典、通知、日志 |

## 目录说明

```text
back-end-spring-cloud
├─ pom.xml                 # 父工程，统一依赖和插件版本
├─ common-core             # 公共核心工具
├─ common-db               # 公共数据库和响应工具
├─ auth-center             # 认证中心
├─ gateway                 # API 网关
├─ file-upload             # 文件上传服务
├─ user-center             # 用户资料和简历
├─ high-service            # 高中模块
├─ university-service      # 大学模块
├─ general-service         # 学习、内容、成长记录
├─ workplace-service       # 职场阶段
├─ advanced-service        # 进阶成长
├─ sql                     # 数据库脚本和数据库设计文档
└─ docs                    # 后端模块文档
```

## 构建命令

编译全部模块：

```powershell
mvn.cmd -DskipTests compile
```

只编译某个服务及其依赖：

```powershell
mvn.cmd -pl high-service -am -DskipTests compile
```

使用本仓库本地 Maven 缓存：

```powershell
mvn.cmd "-Dmaven.repo.local=E:\GitHub\Chronicles-Of-Time\.m2\repository" -pl high-service -am -DskipTests compile
```

## 接口约定

- 服务控制器路径统一包含 `/api/**` 前缀。
- 前端和后台通过 `gateway` 访问后端，不直接访问业务服务端口。
- 统一响应结构来自 `common-db` 的 `RestBean<T>`：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

- 需要登录的接口通过请求头传递：

```http
Authorization: Bearer <token>
```

## 配置约定

- 每个服务配置文件位于 `src/main/resources/application.yml`。
- MySQL 连接、Redis 地址、JWT 密钥、大模型 API Key 等敏感项应通过环境变量或本地配置覆盖，不建议提交真实生产值。
- 新增路由后必须同步维护 `gateway/src/main/resources/application.yml`。

## 当前重点能力

- 高中 AI 分析接口：`POST /api/high/ai/analyze`
- 打卡勋章：`/api/activity/checkin`、`/api/activity/heartbeat`、`/api/activity/summary`
- 在线聊天：`/api/chat/friends`、`/api/chat/groups`、`/api/chat/messages`
- 题库在线考试：`/api/question/exam/start`、`/api/question/exam/submit`、历史和详情接口
- 后台题库审核：`/api/question/admin/audit-list`、`/api/question/admin/audit/{id}`
- 文件上传：`/api/upload/file`、`/api/upload/image`、`/api/upload/files`

## 维护 checklist

- 新增后端接口：控制器、服务、Mapper、网关路由、前端调用、文档同步。
- 新增数据库字段：SQL、实体、Mapper XML、VO/DTO、页面展示、文档同步。
- 新增业务模块：父 `pom.xml`、服务端口、网关路由、数据库边界、前后台入口、文档同步。
- 修改鉴权逻辑：同时检查网关白名单、前台路由守卫、后台路由守卫和 request 拦截器。
