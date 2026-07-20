# 分库分表与迁移上线方案

## 1. 分库策略

本设计默认拆为七个逻辑库：

- `cot_identity`
- `cot_profile`
- `cot_content`
- `cot_learning`
- `cot_highschool`
- `cot_university`
- `cot_platform`

开发/小规模部署可以把七个库放在同一 MySQL 实例。生产环境建议按写入压力拆实例：

- 实例 A：`cot_identity`、`cot_profile`、`cot_platform`
- 实例 B：`cot_content`
- 实例 C：`cot_learning`
- 实例 D：`cot_highschool`、`cot_university`

当学习流水、内容互动增长较快时，优先独立扩容 `cot_learning` 和 `cot_content`。

## 2. 推荐分表对象

| 表 | 分片键 | 时间维度 | 初始分片 | 原因 |
| --- | --- | --- | --- | --- |
| `cot_learning.answer_record` | `user_id` | `answer_at` 按月 | 16 张/月 | 答题流水高频写入 |
| `cot_content.content_reaction` | `user_id` | `created_at` 按年 | 8 张/年 | 点赞反应高频写入 |
| `cot_content.content_comment` | `content_id` | `created_at` 按月 | 8 张/月 | 热门内容评论可能集中 |
| `cot_platform.sys_notification` | `user_id` | `created_at` 按年 | 8 张/年 | 用户消息持续增长 |
| `cot_platform.admin_operation_log` | `created_at` | 按月 | 1 张/月 | 只按时间归档 |
| `cot_platform.sys_api_access_log` | `created_at` | 按日/月 | 1 张/日或写日志平台 | 网关访问量大 |

命名建议：

```text
answer_record_202606_00
answer_record_202606_01
...
answer_record_202606_15
```

路由规则：

```text
month = format(answer_at, 'yyyyMM')
shard = crc32(user_id) % 16
table = answer_record_${month}_${shard}
```

如果暂时不引入 ShardingSphere，可先使用单表 `answer_record`，等单表超过 2000 万行后迁移到分片表。

## 3. 分片中间件建议

优先级：

1. 开发期/毕业设计/小项目：单库多表，不使用中间件。
2. 成长期：Apache ShardingSphere-JDBC，接入成本相对低，适合 Spring Boot/MyBatis。
3. 大规模生产：业务自研路由 + Canal/Flink 聚合 + ClickHouse/Elasticsearch 查询。

## 4. 数据冷热分层

### 热数据

- 近 6 个月答题记录。
- 近 12 个月内容互动、评论。
- 近 12 个月通知和操作日志。

### 温数据

- 6 到 24 个月答题记录。
- 历史成长记录、历史志愿方案。
- 历史论文版本。

### 冷数据

- 24 个月以前 API 访问日志。
- 已删除内容、历史审核记录。
- 长期不再访问的文件资产。

冷数据建议归档到对象存储或 ClickHouse，不建议长期堆在业务 MySQL 主库。

## 5. 从现有表迁移到新设计

### 5.1 准备阶段

- 冻结旧库 DDL，不再新增旧表字段。
- 给所有旧表补充主键、唯一键和更新时间。
- 新建七个目标库，执行 `01_schema.sql`。
- 编写字段映射表，明确旧字段到新字段的转换。

### 5.2 迁移映射建议

| 当前表/实体 | 目标表 |
| --- | --- |
| `user` | `cot_identity.iam_user` |
| `user_info` | `cot_profile.user_profile` |
| `resume` | `cot_profile.resume` |
| `resume_education` | `cot_profile.resume_education` |
| `resume_work_experience` | `cot_profile.resume_work_experience` |
| `resume_project` | `cot_profile.resume_project` |
| `resume_skill` | `cot_profile.resume_skill` |
| `resume_certificate` | `cot_profile.resume_certificate` |
| `resume_social_experience` | `cot_profile.resume_social_experience` |
| `content` | `cot_content.content_article`、`content_media`、`content_tag` |
| `comment` | `cot_content.content_comment` |
| `like_record` | `cot_content.content_reaction` |
| `favorite_record` | `cot_content.content_favorite` |
| `growth` | `cot_content.growth_record` |
| `question_bank` | `cot_learning.question`、`question_option` |
| `answer_records` | `cot_learning.answer_record` |
| `mistake_records` | `cot_learning.mistake_record` |
| `score_records` | `cot_learning.score_record` |
| `subject` | `cot_highschool.hs_subject` |
| `subject_combination` | `cot_highschool.hs_subject_combination` |
| `student_course_selection` | `cot_highschool.hs_student_selection` |
| `course_selection_intention` | `cot_highschool.hs_selection_intention` |
| `course_selection_history` | `cot_highschool.hs_selection_history` |
| `grading_scale` | `cot_highschool.hs_grading_scale` |
| `course_guidance` | `cot_highschool.hs_course_guidance` |
| `university` | `cot_highschool.gaokao_university` |
| `major` in high-service | `cot_highschool.gaokao_major` |
| `admission_plan` | `cot_highschool.gaokao_admission_plan` |
| `major_requirement` | `cot_highschool.gaokao_major_requirement` |
| `user_volunteer` | `cot_highschool.user_volunteer_plan` |
| `volunteer_detail` | `cot_highschool.user_volunteer_detail` |
| `admission_simulation` | `cot_highschool.admission_simulation` |
| `major` in university-service | `cot_university.uni_major` |
| `course_category` | `cot_university.uni_course_category` |
| `course` | `cot_university.uni_course` |
| `student_course` | `cot_university.uni_student_course` |
| `graduation_requirement` | `cot_university.uni_graduation_requirement` |
| `paper` | `cot_university.thesis_paper` |
| `suggestion` | `cot_university.thesis_suggestion` |

### 5.3 上线步骤

1. 全量迁移：旧库导出，按映射导入新库。
2. 校验总量：逐表比较记录数、核心唯一键数、空值异常。
3. 双写灰度：应用层同时写旧表和新表，读仍走旧表。
4. 新读灰度：按用户 ID 灰度读新库，异常时回退旧库。
5. 切流完成：全部读写切到新库，旧库只读保留 30 天。
6. 归档旧库：导出备份后下线旧表。

## 6. 迁移风险点

- `content.images`、`content.tags` 如果当前是逗号字符串，需要拆成 `content_media` 和 `content_article_tag`。
- `question_bank.options` 如果当前是 JSON 字符串，需要解析成 `question_option`。
- 当前高中志愿域和大学课程域都有 `major` 表，新设计通过 `gaokao_major` 和 `uni_major` 拆开，迁移时不能混淆。
- 当前部分实体主键是 `Integer`，新设计统一 `BIGINT`，代码落地时 DTO/VO 也要同步。
- Redis 中的 token、计数、验证码不是数据库全量替代，迁移期间要统一过期策略。


