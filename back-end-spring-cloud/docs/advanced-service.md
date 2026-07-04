# advanced-service

## 定位

`advanced-service` 是进阶成长服务，面向长期成长和自我提升场景。当前模块提供服务骨架和一组进阶成长接口，可继续扩展为年度目标、能力雷达、成长路线、里程碑、技能树、导师会话和长期复盘。

## 端口和数据库

```yaml
server:
  port: 8086
```

数据库：

```yaml
cot_advanced
```

网关路径：

```text
/api/advanced/**
```

## 关键目录

```text
advanced-service
├─ src/main/java/org/example/advancedservice
│  ├─ controller/AdvancedController.java
│  ├─ entity
│  ├─ mapper
│  └─ service
└─ src/main/resources/application.yml
```

## 当前接口

基础路径：

```text
/api/advanced
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/dashboard` | 进阶成长仪表盘 |
| `GET` | `/roadmaps` | 成长路线列表 |
| `POST` | `/roadmaps` | 保存成长路线 |
| `DELETE` | `/roadmaps/{id}` | 删除成长路线 |
| `GET` | `/milestones` | 里程碑列表 |
| `POST` | `/milestones` | 保存里程碑 |
| `GET` | `/skills` | 技能列表 |
| `POST` | `/skills` | 保存技能 |
| `GET` | `/mentor-sessions` | 导师会话列表 |
| `POST` | `/mentor-sessions` | 保存导师会话 |

## 建议数据对象

后续扩展可按以下对象建模：

- `GrowthRoadmap`：长期路线，如技术专家、产品能力、管理能力、科研方向。
- `Milestone`：阶段里程碑，如证书、作品、项目、论文、竞赛。
- `SkillNode`：技能点，如基础、进阶、应用、输出。
- `SkillRadar`：能力雷达，如表达、学习、执行、协作、技术。
- `MentorSession`：导师会话，如主题、建议、行动项、复盘状态。
- `AnnualGoal`：年度目标。
- `ReviewReport`：周期复盘。

## 前端对接

用户端：

- `front-end/src/views/career/AdvanceRecords.vue`

后台端：

- `cot-admin-web/src/views/stage/AdvancedStage.vue`

## 开发建议

- 进阶成长模块适合做长期记录，不建议把它和短期学习题库强耦合。
- 路线、里程碑、技能建议使用结构化字段，便于后续生成雷达图、时间线和成长报告。
- 导师会话应包含行动项和下次复盘时间，方便形成闭环。
- 如果未来接入 AI 复盘，应优先把用户目标、阶段记录、成果和阻塞项结构化后再分析。
