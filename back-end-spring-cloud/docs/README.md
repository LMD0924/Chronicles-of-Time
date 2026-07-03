# 后端模块文档总览

本文档同步 `back-end-spring-cloud` 当前源码结构和 `sql/01_cot_enterprise_schema.sql` 的数据库拆分，用于本地联调、建库、接口对接和毕业设计说明。

## 工程结构

父工程 `back-end-spring-cloud/pom.xml` 聚合模块如下：

| 模块 | 文档 | 定位 |
| --- | --- | --- |
| `common-core` | [common-core.md](common-core.md) | 通用工具、JWT、Jackson 配置 |
| `common-db` | [common-db.md](common-db.md) | 统一响应、结果码、MyBatis-Plus 分页配置 |
| `auth-center` | [auth-center.md](auth-center.md) | 登录、注册、Token、基础用户资料 |
| `gateway` | [gateway.md](gateway.md) | 统一入口、路由、JWT 鉴权过滤 |
| `file-upload` | [file-upload.md](file-upload.md) | 本地文件/图片上传 |
| `user-center` | [user-center.md](user-center.md) | 用户扩展资料和简历 |
| `high-service` | [high-service.md](high-service.md) | 高中选科、赋分、志愿和升学数据 |
| `university-service` | [university-service.md](university-service.md) | 大学课程体系、专业、论文建议 |
| `general-service` | [general-service.md](general-service.md) | 成长记录、题库错题、成绩、内容社区 |
| `advanced-service` | [advanced-service.md](advanced-service.md) | 进阶成长服务骨架 |
| `workplace-service` | [workplace-service.md](workplace-service.md) | 职场阶段服务骨架 |

## 服务端口和数据库

| 服务 | 端口 | 数据库/依赖 |
| --- | --- | --- |
| `gateway` | `8500` | Redis `localhost:6379`，路由到各业务服务 |
| `auth-center` | 网关指向 `8080` | MySQL `cot_identity`，Redis `localhost:6379` |
| `file-upload-service` | `8090` | 本地上传目录 |
| `user-center` | `8081` | MySQL `cot_profile` |
| `high-service` | `8082` | MySQL `cot_highschool` |
| `university-service` | `8083` | MySQL `cot_university` |
| `general-service` | `8084` | MySQL `cot_content`、`cot_learning` |
| `workplace-service` | `8085` | 职场阶段扩展服务 |
| `advanced-service` | `8086` | 进阶成长扩展服务 |

## 数据库拆分

| 数据库 | 主要模块 | 主要表前缀/表 |
| --- | --- | --- |
| `cot_identity` | `auth-center` | `iam_user` |
| `cot_profile` | `user-center` | `user_profile`、`resume_*` |
| `cot_highschool` | `high-service` | `hs_*`、`gaokao_*`、`user_volunteer_*`、`admission_*` |
| `cot_university` | `university-service` | `uni_*`、`thesis_*` |
| `cot_content` | `general-service` | `content_*`、`growth_record` |
| `cot_learning` | `general-service` | `question`、`mistake_record`、`answer_record`、`score_record` |

## 当前注意事项

- 所有源码、配置和文档已统一为 UTF-8 无 BOM，避免 Java 编译出现 `非法字符: '\ufeff'`。
- `gateway` 统一暴露 `/api/**` 前缀，前端后台 `cot-admin-web` 默认通过 `/api` 网关访问后端。
- 若本地联调发现服务名或端口不一致，优先检查各模块 `application.yml` 与网关路由。