# 项目文档总览

本文档是拾光记项目的总索引，便于从业务、前端、后台、后端服务和数据库几个维度快速定位资料。

## 业务模块

| 模块 | 用户端入口 | 后台入口 | 后端服务 | 说明 |
| --- | --- | --- | --- | --- |
| 账号认证 | `/login` | `/login` | `auth-center`、`gateway` | 登录、注册、JWT、刷新 Token、用户基础信息 |
| 个人资料和简历 | `/PersonalProfile`、`/Resume` | 系统管理、用户管理 | `user-center` | 个人资料、简历、教育/项目/工作/技能/证书经历 |
| 高中阶段 | `/CourseSelection`、`/Volunteer`、`/HighSchoolHub` | 升学规划管理 | `high-service` | 新高考选科、赋分、志愿方案、录取模拟、AI 分析 |
| 学习中心 | `/StudyDashboard` | 学习中心管理 | `general-service` | 题库、在线练习、考试、错题、成绩、知识图谱 |
| 内容社区 | `/Publish`、`/List`、`/View/:id` | 用户内容管理 | `general-service` | 内容发布、评论、点赞、收藏、内容知识图谱 |
| 大学阶段 | `/PrePare`、`/Paper`、`/CourseTree` | 大学学业管理 | `university-service` | 专业、课程树、课程成绩、毕业差距、GPA、论文建议 |
| 文件资源 | 多处上传入口 | 文件资源管理 | `file-upload` | 图片、文档和通用文件上传，本地静态访问 |
| 职场阶段 | 职场记录页面 | 人生阶段管理 | `workplace-service` | 职业档案、目标、任务、面试、复盘 |
| 进阶成长 | 进阶记录页面 | 人生阶段管理 | `advanced-service` | 能力路线、里程碑、技能和导师会话 |

## 文档位置

| 文档 | 路径 |
| --- | --- |
| 项目根说明 | [../README.md](../README.md) |
| 用户端说明 | [../front-end/README.md](../front-end/README.md) |
| 后台管理端说明 | [../cot-admin-web/README.md](../cot-admin-web/README.md) |
| 后端服务总览 | [../back-end-spring-cloud/docs/README.md](../back-end-spring-cloud/docs/README.md) |
| 高中 AI 配置 | [high-ai-config.md](high-ai-config.md) |
| 数据库总览 | [../back-end-spring-cloud/sql/README.md](../back-end-spring-cloud/sql/README.md) |

## 本地联调要点

- 网关端口：`8500`
- 用户端默认开发端口：`5173`
- 后台管理端默认开发端口：由 Vite 自动分配，可通过 `--port` 指定
- 用户端请求基地址：`http://localhost:8500/api/`
- 后台管理端请求基地址：`VITE_API_BASE_URL` 或 `/api`
- 后端服务接口都应经由网关暴露，新增服务接口后要同步维护 `gateway/src/main/resources/application.yml`

## 环境变量

| 变量 | 用途 |
| --- | --- |
| `HIGH_AI_API_KEY` | 高中 AI 分析大模型密钥 |
| `HIGH_AI_BASE_URL` | OpenAI 兼容接口地址，默认 `https://api.openai.com/v1` |
| `HIGH_AI_MODEL` | 高中 AI 分析模型，默认 `gpt-4o-mini` |
| `VITE_API_BASE_URL` | 后台管理端 API 网关地址 |

## 维护建议

- 修改接口路径时同步更新：控制器、网关路由、前端 request 调用、对应模块文档。
- 修改数据库结构时同步更新：SQL 脚本、实体类、Mapper XML、模块文档。
- 涉及用户隐私的数据，例如题库、错题、志愿方案、选科记录，应保持用户隔离。
- 文档中不要写入真实生产密钥、生产数据库密码或不可公开的第三方服务凭证。
