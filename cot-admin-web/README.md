# 拾光记后台管理端

`cot-admin-web` 是拾光记的管理后台，基于 Vue 3、Vite、Element Plus、Pinia 和 Vue Router 构建。后台用于管理员维护用户、题库、学习数据、高中升学规划、大学学业、内容资源、文件资源和系统监控。

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 构建工具 | Vite 8 |
| 前端框架 | Vue 3 |
| UI 组件 | Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router |
| 请求 | Axios、`src/utils/request.js` |
| 图表 | ECharts |
| 进度条 | NProgress |
| 测试 | Vitest、Playwright |

## 目录结构

```text
cot-admin-web
├─ src
│  ├─ api                 # mock 或接口辅助模块
│  ├─ layout              # 后台整体布局
│  ├─ router              # 固定路由、动态菜单路由
│  ├─ stores              # 登录用户、主题、缓存等状态
│  ├─ utils               # request、auth 等工具
│  └─ views
│     ├─ dashboard        # 首页控制台
│     ├─ system           # 用户、角色、权限、菜单、日志
│     ├─ learning         # 题目、错题、答题、成绩、知识图谱
│     ├─ planning         # 高中选科、志愿、赋分、院校专业
│     ├─ academic         # 大学课程、专业、进度、论文
│     ├─ content          # 内容、评论、相册、时光笺、图谱
│     ├─ stage            # 高中、大学、职场、进阶阶段管理
│     ├─ resource         # 文件资源管理
│     └─ monitor          # 服务、在线用户、接口统计
├─ package.json
└─ vite.config.js
```

## 菜单和路由

后台菜单唯一配置源：

`src/router/menus.js`

业务页面由 `adminMenus` 自动生成路由。新增后台页面时，一般只需要：

1. 在 `src/views` 下创建页面。
2. 在 `adminMenus` 增加菜单项，填写 `path`、`name`、`title`、`icon`、`component`。
3. 确认 `component` 对应 `src/views/<component>.vue`。

固定路由在：

`src/router/index.js`

包含：

- `/login`：管理员登录
- `/`：后台布局，重定向到 `/dashboard`
- `/403`：无权限页面
- 兜底路由：重定向到 `/dashboard`

## 后台功能

| 菜单 | 页面 |
| --- | --- |
| 首页控制台 | `/dashboard` |
| 系统管理 | 用户、角色、菜单、权限、日志 |
| 学习中心管理 | 题目、错题、答题记录、成绩记录、知识图谱、知识热力图 |
| 人生阶段管理 | 高中阶段、大学阶段、职场阶段、进阶成长 |
| 升学规划管理 | 选科、选科审批、赋分规则、选科指导、志愿方案、院校专业库 |
| 用户内容管理 | 笔记、动态、相册、时光笺、文章、评论、内容图谱 |
| 大学学业管理 | 专业树、课程树、学生课程、学业进度、毕业差距、GPA、证书、论文 |
| 文件资源管理 | 文件和资源维护 |
| 系统监控 | 服务状态、在线用户、接口访问统计 |

## 登录和权限

后台登录状态由 `src/stores/user.js` 管理，token 存储键为：

```text
cot_admin_token
```

路由守卫会检查：

- 是否登录
- 是否管理员
- 页面是否需要缓存

普通账号访问后台会跳转到 `/403`。

## 后端接口

统一请求工具：

`src/utils/request.js`

默认基地址：

```js
baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
```

本地联调推荐设置 `.env.local`：

```env
VITE_API_BASE_URL=http://localhost:8500/api
```

请求会自动附加：

```http
Authorization: Bearer <cot_admin_token>
```

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
npm.cmd run dev -- --host 0.0.0.0 --port 5174
```

生产构建：

```powershell
npm.cmd run build
```

单元测试：

```powershell
npm.cmd run test:unit
```

端到端测试：

```powershell
npm.cmd run test:e2e
```

格式化：

```powershell
npm.cmd run format
```

## 联调注意事项

- 后台依赖 `gateway`，建议所有接口通过 `http://localhost:8500/api` 访问。
- 题库审核、选科审批等管理页面依赖后端对应审核接口。
- 新增菜单后如果页面空白，优先检查 `component` 路径是否与 `src/views` 文件一致。
- 新增接口时要同步维护网关路由、后台 request 调用和后端模块文档。
