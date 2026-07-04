# Chronicles of Time 企业级数据库设计文档

## 1. 项目业务识别

根据 `front-end`、`cot-admin-web` 和 `back-end-spring-cloud` 当前结构，系统包含这些核心业务：

- 用户端：登录注册、个人档案、简历、记录拾光、内容发布/阅读、学习仪表盘、题库、错题本、知识图谱、高中选科、志愿填报、大学课程树、毕业论文。
- 后台管理端：目前页面较轻，数据库侧预留 RBAC、字典、文件、内容审核、后台操作审计，后续可直接支撑管理后台扩展。
- 后端服务：`auth-center`、`user-center`、`general-service`、`high-service`、`university-service`、`file-upload`、`gateway`。

## 2. 总体设计原则

- 按服务域拆库：认证、用户画像、内容、学习、高中、大学、平台基础能力分库，减少单库耦合。
- 不做跨库物理外键：微服务之间使用逻辑外键，避免跨库 DDL 和分片扩容困难。
- 统一主键：所有核心业务表使用 `BIGINT UNSIGNED`，推荐雪花 ID；导入类维表也统一升级为 BIGINT。
- 统一审计列：`created_at`、`updated_at`、`deleted_at`，流水表只保留必要时间列。
- 高频表预留分片键：答题、内容互动、日志、通知都以 `user_id` 和时间作为核心分片维度。
- 结构化替代字符串堆叠：题目选项、内容媒体、标签、课程先修关系、论文建议拆为子表，减少 JSON/逗号字符串滥用。

## 3. 数据库边界

| 数据库 | 对应服务 | 说明 |
| --- | --- | --- |
| `cot_identity` | `auth-center`、`gateway`、`cot-admin-web` | 用户账号、角色权限、令牌、登录审计 |
| `cot_profile` | `user-center` | 用户资料、人生阶段、简历模块 |
| `cot_content` | `general-service` | 内容文章、媒体、标签、评论、点赞收藏、成长记录 |
| `cot_learning` | `general-service` | 科目、知识点、题库、练习会话、答题流水、错题、成绩 |
| `cot_highschool` | `high-service` | 高中选科、赋分、指导、志愿填报、院校专业录取 |
| `cot_university` | `university-service` | 大学专业、课程树、修课、毕业进度、论文 |
| `cot_platform` | `file-upload`、后台基础能力 | 文件资产、字典、通知、后台审计、API 日志 |

## 4. 核心表设计

### 4.1 身份认证库 `cot_identity`

- `iam_user`：统一账号表，对应当前 `user`。保留用户名、密码哈希、邮箱、手机号、头像、状态、最近登录等字段。
- `iam_role`：角色表，预置 `SUPER_ADMIN`、`ADMIN`、`USER`、`TEACHER`、`PARENT`。
- `iam_permission`：菜单、按钮、API 权限统一表，后续后台管理可以直接接入。
- `iam_user_role`、`iam_role_permission`：RBAC 关联表。
- `iam_refresh_token`：刷新令牌与登录设备，替代只依赖 Redis 的不可追踪模式。
- `iam_login_audit`：登录审计，用于安全风控和后台查看。

关系说明：

- `iam_user` 1:N `iam_user_role`
- `iam_role` N:M `iam_permission`
- `iam_user` 1:N `iam_refresh_token`

### 4.2 用户画像库 `cot_profile`

- `user_profile`：个人档案，对应当前 `user_info`，增加教育阶段、班级、城市、职业方向、可见性和扩展 JSON。
- `user_stage`：用户人生阶段，高中/大学/职场可按时间线管理。
- `resume`：简历主表。
- `resume_education`、`resume_work_experience`、`resume_project`、`resume_skill`、`resume_certificate`、`resume_social_experience`：简历子模块。

设计调整：

- `resume.expected_salary` 拆成 `expected_salary_min`、`expected_salary_max`，更适合筛选。
- 简历子模块统一 `sort_order`，前端可稳定拖拽排序。
- 个人资料与账号解耦，账号归身份库，资料归用户中心。

### 4.3 内容社区库 `cot_content`

- `content_article`：文章/小札/记录主表。替代当前 `content` 的宽字段设计，把图片和标签拆出去。
- `content_media`：文章图片、附件、视频。
- `content_tag`、`content_article_tag`：标签及关联，支持标签统计和知识图谱。
- `content_comment`：评论树，使用 `parent_id` 和 `root_id` 支持二级/多级回复。
- `content_reaction`：点赞记录，统一文章和评论点赞。
- `content_favorite`：收藏记录。
- `content_audit`：内容审核，后台管理预留。
- `growth_record`：成长记录，承接当前 `growth` 宽表能力，保留学习、竞赛、活动、情绪、家庭、目标、里程碑等字段。

设计调整：

- `content_article` 的统计字段作为冗余计数，真实互动记录在子表中。
- `content_reaction` 使用 `biz_type + biz_id`，后续可扩展到评论、论文建议等对象。
- `growth_record` 保留宽表是有意选择：成长记录字段多但单用户查询为主，拆得太碎会增加前端表单复杂度。

### 4.4 学习库 `cot_learning`

- `learning_subject`：学习科目，覆盖高中、大学、考证等阶段。
- `knowledge_point`：知识点树。
- `knowledge_edge`：知识图谱边。
- `question`：题库主表，对应当前 `question_bank`。
- `question_option`：题目选项，替代 JSON 字符串选项。
- `practice_session`：练习/考试会话。
- `answer_record`：答题流水，对应当前 `answer_records`，建议分片。
- `mistake_record`：错题本，对应当前 `mistake_records`。
- `score_record`：成绩记录，对应当前 `score_records`。
- `knowledge_mastery_stat`：知识点掌握度统计，支撑图谱、雷达、热力图。

设计调整：

- 题库、错题、答题记录都用 `subject_id`、`knowledge_point_id` 关联，避免只靠中文名称关联。
- `answer_record` 是增长最快的表，按 `user_id` hash 和 `answer_at` 时间分片。
- 掌握度统计从答题流水异步聚合，避免每次打开图谱实时扫描流水。

### 4.5 高中库 `cot_highschool`

- `hs_subject`：高中科目。
- `hs_subject_combination`：新高考选科组合。
- `hs_student_selection`：学生选科记录。
- `hs_selection_intention`：选科意向。
- `hs_selection_history`：选科变更历史。
- `hs_grading_scale`：等级赋分规则。
- `hs_course_guidance`：选科指导记录。
- `gaokao_university`：高考院校库。
- `gaokao_major`：高考专业库。
- `gaokao_admission_plan`：招生/录取计划。
- `gaokao_major_requirement`：专业选科要求。
- `user_volunteer_plan`、`user_volunteer_detail`、`admission_simulation`：用户志愿方案、明细、模拟结果。

设计调整：

- 现有 `university`、`major` 在高中志愿域下容易和大学课程域撞名，因此统一升级为 `gaokao_university`、`gaokao_major`。
- 志愿方案主表和明细表拆分，支持一个用户多套方案、多次模拟。
- 招生计划按省份、年份、考生类型、批次建立复合索引，支撑志愿推荐筛选。

### 4.6 大学库 `cot_university`

- `uni_major`：大学专业培养方案。
- `uni_course_category`：课程分类树。
- `uni_course`：课程。
- `uni_course_prerequisite`：课程先修关系，替代课程表中的纯文本先修字段。
- `uni_student_course`：学生修课记录。
- `uni_graduation_requirement`：毕业要求进度。
- `thesis_paper`：论文。
- `thesis_suggestion`：论文修改建议。

设计调整：

- 大学域表统一加 `uni_` 前缀，避免和高考专业库混淆。
- 课程树是用户端核心功能，`major_id + category_id + term_no` 都建立查询索引。
- 论文保留正文 Markdown，建议后续把版本内容抽到 `thesis_paper_version` 表。

### 4.7 平台库 `cot_platform`

- `file_asset`：文件资产，承接 `file-upload`，支持本地、OSS、MinIO 等存储。
- `sys_dict_type`、`sys_dict_item`：系统字典。
- `sys_notification`：用户通知。
- `admin_operation_log`：后台操作审计。
- `sys_api_access_log`：网关/API 访问日志。

## 5. 与当前代码的兼容建议

当前 Java 实体里的表名多为单库短表名，例如 `user`、`content`、`question_bank`、`student_course_selection`。本次设计是“重新设计数据库”，不是直接无缝替换现有 ORM 映射。若后续要落地到代码，有两种路径：

- 渐进兼容路径：先创建新库新表，再用视图或迁移脚本兼容旧表名，逐步改 `@TableName`。
- 一次性重构路径：服务配置切换到对应库，统一修改实体表名、字段名、Mapper XML 和接口 DTO。

建议先采用渐进兼容路径，风险低，也方便校验数据迁移质量。

## 6. 关键查询场景

- 首页时间线：`cot_content.growth_record` 按 `user_id + record_date` 查询。
- 内容列表：`cot_content.content_article` 按 `visibility + status + is_top + publish_at` 查询。
- 文章详情：文章主表 + 媒体 + 标签 + 评论分页 + 用户点赞收藏状态。
- 学习图谱：`knowledge_mastery_stat` + `knowledge_point` + `knowledge_edge`。
- 练习记录：`practice_session` 查会话，`answer_record` 查答题明细。
- 错题复习：`mistake_record` 按 `user_id + mastered + next_review_date` 查询。
- 高中选科：`hs_student_selection`、`hs_subject_combination`、`hs_grading_scale`。
- 志愿推荐：`gaokao_admission_plan` 按省份、年份、类型、分数/位次筛选，再关联院校/专业。
- 大学课程树：`uni_course_category` + `uni_course` + `uni_course_prerequisite`。
- 毕业进度：`uni_graduation_requirement` + `uni_student_course`。

