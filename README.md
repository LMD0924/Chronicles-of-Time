# 拾光记 Chronicles of Time

拾光记是一个覆盖高中、大学、职场和长期成长阶段的个人成长管理系统。项目采用前后台分离和 Spring Cloud 多服务结构，包含用户端、后台管理端、统一网关、认证中心、文件上传、学习题库、高中升学规划、大学学业规划、职场和进阶成长服务。

## 模块入口

| 模块 | 路径 | 说明 |
| --- | --- | --- |
| 用户端前台 | `front-end` | Vue 3 + Vite 用户端，包含登录、个人档案、高中选科志愿、学习练习、内容社区、大学学业等页面 |
| 后台管理端 | `cot-admin-web` | Vue 3 + Vite 管理端，包含用户、题库、升学规划、大学学业、内容、文件和系统监控管理 |
| 后端服务 | `back-end-spring-cloud` | Spring Boot/Spring Cloud 聚合工程，提供网关、认证、文件、用户、高中、大学、学习内容等业务接口 |
| 数据库脚本 | `back-end-spring-cloud/sql` | MySQL 建库建表、数据库边界说明、迁移和运维建议 |
| 项目文档 | `docs`、`back-end-spring-cloud/docs` | 项目总文档、AI 配置说明、后端各模块详细说明 |

## 快速启动

### 1. 数据库

准备 MySQL 8.0+，执行：

```sql
source back-end-spring-cloud/sql/01_schema.sql;
```

当前脚本按服务拆分数据库，主要包含：

- `cot_identity`：账号、角色、权限、登录审计
- `cot_profile`：用户资料、简历、人生阶段资料
- `cot_highschool`：高中选科、赋分、志愿、院校专业和招生数据
- `cot_university`：大学专业、课程树、学生课程、论文
- `cot_content`：成长记录、内容发布、评论收藏
- `cot_learning`：题库、练习、错题、答题和成绩
- `cot_platform`：文件资产、通知、字典、平台日志

### 2. 后端

后端父工程在 `back-end-spring-cloud`。本地常用编译命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-24'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn.cmd "-Dmaven.repo.local=E:\GitHub\Chronicles-Of-Time\.m2\repository" -DskipTests compile
```

常用启动顺序：

1. `auth-center`
2. `user-center`
3. `high-service`
4. `university-service`
5. `general-service`
6. `file-upload`
7. `gateway`

网关统一入口为 `http://localhost:8500/api`。

### 3. 用户端

```powershell
cd front-end
npm.cmd install
npm.cmd run dev
```

默认开发地址：`http://localhost:5173`

### 4. 后台管理端

```powershell
cd cot-admin-web
npm.cmd install
npm.cmd run dev
```

后台默认通过 `VITE_API_BASE_URL` 或 `/api` 访问网关。

## 高中 AI 分析

高中模块已提供 AI 辅助分析能力，覆盖选科、专业方向和志愿方案。配置文档见：

[docs/high-ai-config.md](docs/high-ai-config.md)

推荐使用环境变量配置大模型密钥：

```powershell
$env:HIGH_AI_API_KEY="你的大模型 API Key"
```

未配置密钥时，后端会返回本地规则分析结果，页面不会失效。

## 文档索引

- [项目文档总览](docs/README.md)
- [用户端前台文档](front-end/README.md)
- [后台管理端文档](cot-admin-web/README.md)
- [后端模块总览](back-end-spring-cloud/docs/README.md)
- [数据库文档](back-end-spring-cloud/sql/README.md)

## 编码和协作约定

- 新增源码、配置和文档统一使用 UTF-8。
- 前端请求优先通过封装的 `request` 工具走网关，不在页面里直接拼接服务端口。
- 后端新接口应保持 `/api/**` 前缀，并同步更新 `gateway` 路由。
- 不要把真实数据库密码、JWT 密钥或大模型 API Key 提交到仓库；本地开发优先使用环境变量覆盖配置。

