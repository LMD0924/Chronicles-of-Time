# workplace-service

## 定位

`workplace-service` 是职场阶段服务，面向从校园进入工作后的持续成长场景。当前模块已经提供服务骨架和一组职场管理接口，可继续扩展为职业档案、目标、任务、面试记录、项目复盘和职业方向管理。

## 端口和数据库

```yaml
server:
  port: 8085
```

数据库：

```yaml
cot_workplace
```

网关路径：

```text
/api/workplace/**
```

## 关键目录

```text
workplace-service
├─ src/main/java/org/example/workplaceservice
│  ├─ controller/WorkplaceController.java
│  ├─ entity
│  ├─ mapper
│  └─ service
└─ src/main/resources/application.yml
```

## 当前接口

基础路径：

```text
/api/workplace
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/dashboard` | 职场阶段仪表盘 |
| `GET` | `/profile` | 职业档案详情 |
| `POST` | `/profile` | 保存职业档案 |
| `GET` | `/goals` | 职业目标列表 |
| `POST` | `/goals` | 保存职业目标 |
| `DELETE` | `/goals/{id}` | 删除职业目标 |
| `GET` | `/tasks` | 职场任务列表 |
| `POST` | `/tasks` | 保存职场任务 |
| `DELETE` | `/tasks/{id}` | 删除职场任务 |
| `GET` | `/interviews` | 面试记录列表 |
| `POST` | `/interviews` | 保存面试记录 |
| `GET` | `/reviews` | 工作/项目复盘列表 |
| `POST` | `/reviews` | 保存工作/项目复盘 |

## 建议数据对象

后续扩展可按以下对象建模：

- `WorkProfile`：职业档案，如当前公司、岗位、城市、工作年限、职业方向。
- `WorkGoal`：职业目标，如晋升、跳槽、技能提升、证书、作品集。
- `WorkTask`：阶段任务，如面试准备、项目交付、学习计划。
- `InterviewRecord`：面试记录，如公司、岗位、轮次、结果、问题复盘。
- `WorkReview`：项目或工作复盘，如成果、问题、改进项、影响力。
- `SkillGrowth`：职场技能成长记录。
- `OfferCompare`：Offer 对比。

## 前端对接

用户端：

- `front-end/src/views/career/WorkRecords.vue`

后台端：

- `cot-admin-web/src/views/stage/WorkplaceStage.vue`

## 开发建议

- 职场数据应严格按用户隔离，避免用户看到他人的公司、面试和 Offer 信息。
- 面试记录和薪资相关字段属于敏感信息，默认不应公开。
- 若未来要做 Offer 对比，应把薪资、城市、成长空间、稳定性、行业前景拆成结构化指标，便于排序和图表展示。
- 可以与 `user-center` 的简历模块联动，形成“简历版本、投递、面试、复盘”的闭环。
