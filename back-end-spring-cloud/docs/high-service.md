# high-service

## 定位

`high-service` 是高中阶段与升学规划服务，覆盖新高考选科、赋分规则、选科指导、专业选科要求、志愿填报、院校推荐、录取模拟和 AI 辅助分析。

## 端口和数据库

```yaml
server:
  port: 8082
```

数据库：

```yaml
cot_highschool
```

## 关键目录

```text
high-service
├─ src/main/java/org/example/highservice
│  ├─ config              # Jackson 和高中 AI 配置
│  ├─ controller          # 选科、专业、志愿、AI 接口
│  ├─ dto                 # 查询和 AI 请求响应 DTO
│  ├─ entity              # 高中业务实体
│  ├─ mapper              # MyBatis-Plus Mapper
│  └─ service             # 业务服务
└─ src/main/resources
   ├─ application.yml
   └─ mapper
```

## 主要对象

选科：

- `Subject`
- `SubjectCombination`
- `StudentCourseSelection`
- `CourseSelectionHistory`
- `CourseSelectionIntention`
- `CourseGuidance`
- `GradingScale`

专业和志愿：

- `MajorRequirement`
- `MajorSubjectMatching`
- `University`
- `Major`
- `AdmissionPlan`
- `AdmissionSimulation`
- `UserVolunteer`
- `VolunteerDetail`

AI：

- `HighAiAnalyzeRequest`
- `HighAiAnalyzeResponse`
- `HighAiProperties`

## 网关路径

| 路径 | 说明 |
| --- | --- |
| `/api/subject/**` | 科目 |
| `/api/subject-combination/**` | 科目组合 |
| `/api/selection/**` | 用户选科 |
| `/api/intention/**` | 选科意向 |
| `/api/guidance/**` | 选科指导 |
| `/api/history/**` | 选科历史 |
| `/api/grading/**` | 赋分规则 |
| `/api/major/**` | 高考专业选科要求 |
| `/api/volunteer/**` | 志愿填报 |
| `/api/high/ai/**` | 高中 AI 分析 |

## 选科接口

基础路径：

```text
/api/selection
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/add` | 用户提交选科 |
| `PUT` | `/confirm/{id}` | 确认选科 |
| `PUT` | `/modify` | 修改选科 |
| `DELETE` | `/cancel/{id}` | 退选 |
| `GET` | `/user/{userId}` | 获取用户选科记录 |
| `GET` | `/statistics/grade` | 年级选科统计 |
| `GET` | `/recommend` | 根据专业推荐组合 |
| `POST` | `/query` | 分页查询选科记录 |
| `GET` | `/hot-combinations` | 热门组合 |
| `GET` | `/advice/{userId}` | 获取选科建议 |

科目和组合：

- `/api/subject/all`
- `/api/subject/first`
- `/api/subject/second`
- `/api/subject/statistics`
- `/api/subject-combination/hot`
- `/api/subject-combination/all`
- `/api/subject-combination/by-first-subject/{firstSubject}`

## 专业选科接口

基础路径：

```text
/api/major
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/match` | 根据当前选科匹配专业 |
| `GET` | `/hot` | 热门专业 |
| `GET` | `/level/{level}` | 按院校层次查询专业 |
| `GET` | `/detail/{majorCode}` | 专业详情 |
| `GET` | `/category/statistics` | 专业类别统计 |
| `GET` | `/search` | 搜索专业 |
| `GET` | `/list` | 专业库列表 |
| `POST` | `/save` | 保存专业选科要求 |
| `POST` | `/delete` | 删除专业 |
| `GET` | `/matching/list` | 专业科目匹配规则列表 |
| `POST` | `/matching/save` | 保存匹配规则 |

## 志愿接口

基础路径：

```text
/api/volunteer
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/plan/save` | 保存志愿方案 |
| `PUT` | `/plan/update` | 更新志愿方案 |
| `DELETE` | `/plan/delete/{id}` | 删除志愿方案 |
| `GET` | `/plan/{id}` | 获取方案详情 |
| `GET` | `/plan/list/{userId}` | 获取用户方案列表 |
| `POST` | `/detail/add` | 添加志愿明细 |
| `POST` | `/detail/batchAdd` | 批量添加志愿 |
| `PUT` | `/detail/update` | 更新志愿明细 |
| `DELETE` | `/detail/delete/{id}` | 删除志愿明细 |
| `GET` | `/detail/list/{volunteerId}` | 获取志愿明细 |
| `POST` | `/recommend/universities` | 综合推荐院校专业 |
| `GET` | `/recommend/byMajor` | 按专业推荐院校 |
| `POST` | `/matching/check/{detailId}` | 单志愿选科匹配检查 |
| `GET` | `/matching/report/{volunteerId}` | 志愿匹配报告 |
| `POST` | `/simulate/single/{detailId}` | 单志愿模拟录取 |
| `GET` | `/simulate/batch/{volunteerId}` | 批量模拟录取 |
| `GET` | `/simulate/analysis/{volunteerId}` | 录取分析报告 |
| `GET` | `/statistics/{userId}` | 志愿统计 |
| `GET` | `/chance/{userId}` | 录取机会分析 |
| `GET` | `/search/universities` | 搜索大学 |
| `GET` | `/search/majors` | 搜索专业 |
| `GET` | `/admission/history` | 招生历史 |
| `GET` | `/filter/provinces` | 省份筛选项 |
| `GET` | `/filter/levels` | 院校层次筛选项 |
| `GET` | `/filter/categories` | 学科门类筛选项 |

## 高中 AI 分析

接口：

```http
POST /api/high/ai/analyze
```

场景：

- `subject_selection`：选科分析
- `major`：专业方向分析
- `volunteer`：志愿推荐和志愿方案分析

请求结构：

```json
{
  "userId": 1,
  "scenario": "volunteer",
  "profile": {},
  "candidates": [],
  "question": "请分析当前志愿方案"
}
```

响应结构：

```json
{
  "scenario": "volunteer",
  "summary": "总体结论",
  "strengths": [],
  "risks": [],
  "suggestions": [],
  "actionPlan": "下一步行动计划",
  "aiEnabled": true,
  "model": "gpt-4o-mini",
  "providerStatus": "大模型分析"
}
```

配置位置：

`high-service/src/main/resources/application.yml`

```yaml
high:
  ai:
    enabled: true
    provider: openai-compatible
    base-url: ${HIGH_AI_BASE_URL:https://api.openai.com/v1}
    model: ${HIGH_AI_MODEL:gpt-4o-mini}
    api-key: ${HIGH_AI_API_KEY:}
```

详细说明见：

`../../docs/high-ai-config.md`

未配置 `HIGH_AI_API_KEY` 时，接口返回本地规则分析，不影响页面使用。

## 前端对接

用户端：

- `front-end/src/views/high/CourseSelection/CourseSelection.vue`
- `front-end/src/views/high/CourseSelection/SelectionCenterPanel.vue`
- `front-end/src/views/high/CourseSelection/MajorRecommendPanel.vue`
- `front-end/src/views/high/volunteer/volunteer.vue`
- `front-end/src/views/high/volunteer/PlanManagement.vue`
- `front-end/src/views/high/volunteer/RecommendPanel.vue`
- `front-end/src/views/high/components/AiInsightPanel.vue`

后台端：

- `cot-admin-web/src/views/planning/CourseSelectionManage.vue`
- `cot-admin-web/src/views/planning/SelectionApprovalManage.vue`
- `cot-admin-web/src/views/planning/GradingScaleManage.vue`
- `cot-admin-web/src/views/planning/CourseGuidanceManage.vue`
- `cot-admin-web/src/views/planning/VolunteerPlanManage.vue`
- `cot-admin-web/src/views/planning/UniversityMajorManage.vue`

## 业务注意事项

- 选科和志愿数据属于用户隐私数据，查询时必须按用户隔离。
- 志愿推荐算法当前基于分数、位次、历年录取线、选科和策略分层，最终录取以官方招生规则为准。
- AI 分析只做规划建议，不替代官方招生章程和人工审核。
- 新增专业或院校字段时，要同步专业匹配、志愿推荐、后台专业库和前台展示。
