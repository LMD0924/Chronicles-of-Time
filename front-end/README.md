# 拾光记用户端前台

`front-end` 是拾光记的用户端 Web 应用，基于 Vue 3、Vite、Element Plus、ECharts、Pinia 和 Tailwind CSS 构建。它面向普通用户，承载个人成长记录、高中升学规划、学习练习、内容社区和大学阶段学业规划。

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 构建工具 | Vite 7 |
| 前端框架 | Vue 3 |
| UI 组件 | Element Plus、Ant Design Vue |
| 状态和路由 | Pinia、Vue Router |
| 图表/图谱 | ECharts、Three.js、Cesium |
| 请求 | Axios、`src/utils/request.js` |
| 大整数处理 | `json-bigint` |
| 样式 | Tailwind CSS、自定义主题工具 |

## 目录结构

```text
front-end
├─ src
│  ├─ components         # 通用组件，如导航、图谱组件
│  ├─ router             # 前台路由
│  ├─ utils              # request、token、theme 等工具
│  ├─ views
│  │  ├─ auth            # 登录、首页、个人资料、简历、设置
│  │  ├─ high            # 高中中心、选科、志愿、AI 分析组件
│  │  ├─ StudyDashboard  # 学习仪表盘、题库、练习、错题、成绩、图谱
│  │  ├─ content         # 内容发布、列表、详情、内容图谱
│  │  ├─ university      # 大学准备、课程树、论文和学业功能
│  │  └─ career          # 职场和进阶记录页面
│  └─ main.js
├─ package.json
└─ vite.config.js
```

## 页面模块

| 路由 | 页面 | 说明 |
| --- | --- | --- |
| `/` | 欢迎页 | 项目入口和引导 |
| `/login` | 登录页 | 登录后保存 `token` 和 `refresh_token` |
| `/home` | 首页 | 用户登录后的主页入口 |
| `/PersonalProfile` | 个人档案 | 用户基础资料展示和维护 |
| `/Resume` | 简历 | 简历主体、教育、项目、技能、证书等资料 |
| `/Settings` | 设置 | 主题、登录偏好和本地设置 |
| `/Records` | 记录拾光 | 成长记录入口 |
| `/CourseSelection` | 选科系统 | 新高考选科、选科意向、选科指导、专业推荐、赋分、审批、历史 |
| `/Volunteer` | 志愿填报 | 志愿方案、推荐、模拟录取、院校专业查询、AI 志愿分析 |
| `/StudyDashboard` | 学习仪表盘 | 在线练习、考试历史、错题本、题库管理、成绩分析、答题记录 |
| `/GraphView` | 学习图谱 | 学习知识图谱可视化 |
| `/Publish`、`/List`、`/View/:id` | 内容社区 | 内容发布、列表、详情 |
| `/ContentKnowledgeGraph` | 内容图谱 | 内容标签和知识关系分析 |
| `/PrePare` | 大学准备 | 大学阶段规划入口 |
| `/Paper` | 论文 | 论文主题和建议管理 |
| `/CourseTree` | 课程树 | 大学课程体系展示 |

## 后端接口

统一请求工具：

`src/utils/request.js`

默认基地址：

```js
baseURL: 'http://localhost:8500/api/'
```

请求拦截器会自动添加：

```http
Authorization: Bearer <token>
```

响应拦截器负责：

- 统一识别后端 `RestBean` 响应结构
- 自动处理 `401`
- 尝试使用 refresh token 刷新 access token
- 网络异常统一提示

## 高中 AI 分析

前台通用组件：

`src/views/high/components/AiInsightPanel.vue`

目前接入位置：

- `CourseSelection/SelectionCenterPanel.vue`：AI 选科分析
- `CourseSelection/MajorRecommendPanel.vue`：AI 专业方向分析
- `volunteer/RecommendPanel.vue`：AI 志愿推荐分析、按专业志愿分析
- `volunteer/PlanManagement.vue`：AI 志愿方案分析

接口：

```http
POST /api/high/ai/analyze
```

后端 API Key 配置见：

`../docs/high-ai-config.md`

## 开发命令

安装依赖：

```powershell
npm.cmd install
```

启动开发服务：

```powershell
npm.cmd run dev
```

指定端口启动：

```powershell
npm.cmd run dev -- --host 0.0.0.0 --port 5173
```

生产构建：

```powershell
npm.cmd run build
```

预览构建结果：

```powershell
npm.cmd run preview
```

格式化：

```powershell
npm.cmd run format
```

## 联调前置条件

- 网关 `gateway` 已启动，端口 `8500`
- 认证中心 `auth-center` 可用
- 对应页面依赖的业务服务已启动，例如：
  - 高中页面：`high-service`
  - 学习页面：`general-service`
  - 大学页面：`university-service`
  - 文件上传：`file-upload`

## 开发约定

- 新页面优先通过 `src/utils/request.js` 请求后端，不直接绕过网关。
- 需要登录的页面由路由守卫检查 `token`。
- 页面文案、注释和配置统一 UTF-8。
- 涉及用户数据的查询必须携带当前用户 ID 或通过后端从 JWT 解析用户，避免看到其他用户数据。
- 新增后端接口后，需要确认 `gateway` 是否已配置对应 `/api/**` 路由。
