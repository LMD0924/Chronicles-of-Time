# general-service

## 定位

`general-service` 是通用成长、学习和内容服务，覆盖成长记录、题库、在线练习/考试、错题本、答题记录、成绩记录、学习知识图谱、内容社区和内容知识图谱。

## 端口和数据库

```yaml
server:
  port: 8084
```

动态数据源：

- `cot_content`：成长记录、内容社区。
- `cot_learning`：题库、练习、错题、成绩、知识图谱。

默认数据源：

```yaml
primary: cot_content
```

## 关键目录

```text
general-service
├─ src/main/java/org/example/generalservice
│  ├─ controller
│  │  ├─ content
│  │  ├─ growth
│  │  └─ question
│  ├─ dto
│  ├─ entity
│  ├─ mapper
│  ├─ service
│  └─ vo
└─ src/main/resources
   ├─ application.yml
   └─ mapper
```

## 主要对象

成长和内容：

- `Growth`
- `Content`
- `Comment`
- `LikeRecord`
- `FavoriteRecord`
- `KnowledgeGraph`
- `KnowledgeNode`
- `KnowledgeEdge`
- `ContentKnowledgeGraph`

学习：

- `QuestionBank`
- `MistakeRecord`
- `AnswerRecords`
- `ScoreRecord`
- `PracticeSession`
- 在线考试 DTO/VO，如 `ExamStartVO`、`ExamSubmitVO`、`ExamHistoryVO`、`ExamDetailVO`

## 成长记录接口

基础路径：

```text
/api/growth
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/add` | 新增成长记录 |
| `POST` | `/update` | 更新成长记录 |
| `POST` | `/delete` | 删除成长记录 |
| `POST` | `/batchDelete` | 批量删除 |
| `GET` | `/detail` | 记录详情 |
| `POST` | `/list` | 列表查询 |
| `GET` | `/stats` | 统计 |
| `GET` | `/trend` | 趋势 |
| `GET` | `/milestones` | 里程碑 |
| `GET` | `/countByStage` | 按阶段统计 |

## 题库和考试接口

基础路径：

```text
/api/question
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/add` | 用户新增题目 |
| `GET` | `/random` | 随机抽题 |
| `GET` | `/list` | 题目列表 |
| `GET` | `/detail/{id}` | 题目详情 |
| `DELETE` | `/delete/{id}` | 删除题目 |
| `GET` | `/filters` | 公共筛选项 |
| `GET` | `/filters/{userId}` | 用户题库筛选项 |
| `GET` | `/admin/audit-list` | 管理员审核列表 |
| `PUT` | `/admin/audit/{id}` | 管理员审核题目 |
| `POST` | `/exam/start` | 开始在线考试或错题练习 |
| `POST` | `/exam/submit` | 提交考试 |
| `GET` | `/exam/history/{userId}` | 历史考试 |
| `GET` | `/exam/detail/{userId}/{sessionId}` | 考试详情 |
| `POST` | `/record` | 记录单次答题 |
| `POST` | `/record-batch` | 批量记录答题 |
| `GET` | `/answer-records` | 答题记录 |
| `GET` | `/knowledge-graph/{userId}` | 学习知识图谱 |
| `GET` | `/knowledge-heatmap/{userId}` | 知识热力图 |
| `GET` | `/learning-path/{userId}` | 学习路径 |
| `GET` | `/knowledge-trend/{userId}` | 知识趋势 |
| `GET` | `/knowledge-radar/{userId}` | 知识雷达 |

## 在线练习/考试逻辑

核心规则：

- 题目由用户自己创建，普通用户只能看到自己的题目。
- 用户新增题目后需要后台管理员审核通过。
- 考试可按第一层分类、知识点和难度随机抽题。
- 一个题目可以绑定多个知识点。
- 考试记录包含计时、评分、答题明细。
- 考后可查看历史考试和考试详情。
- 错题自动进入错题本。
- 错题练习与考试流程类似，但不需要防作弊。
- 错题练习中再次答错的题继续回流到错题本。

抽题建议：

1. 按用户 ID 限定题目范围。
2. 只抽审核通过题目。
3. 根据分类、知识点、难度过滤候选题。
4. 多知识点题目只要命中任一目标知识点即可进入候选。
5. 根据近期开考记录降低重复题权重。
6. 随机抽取不足时，按难度或知识点邻近规则补题。

## 错题接口

基础路径：

```text
/api/mistake
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/add` | 新增错题 |
| `GET` | `/list/{userId}` | 错题列表 |
| `GET` | `/unmastered/{userId}` | 未掌握错题 |
| `PUT` | `/master/{id}` | 标记掌握 |
| `PUT` | `/unmaster/{id}` | 标记未掌握 |
| `PUT` | `/review/{id}` | 记录复习 |
| `GET` | `/statistics/{userId}` | 错题统计 |
| `DELETE` | `/delete/{id}` | 删除错题 |
| `GET` | `/filters/{userId}` | 错题筛选项 |

## 成绩接口

基础路径：

```text
/api/score
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/add` | 新增成绩 |
| `GET` | `/list/{userId}` | 成绩列表 |
| `GET` | `/weak-subject/{userId}` | 薄弱科目 |
| `GET` | `/trend/{userId}/{subjectName}` | 单科趋势 |
| `GET` | `/overall-avg/{userId}` | 总体平均分 |
| `DELETE` | `/delete/{id}` | 删除成绩 |

## 内容接口

基础路径：

```text
/api/content
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/save` | 发布或保存内容 |
| `GET` | `/detail/{id}` | 内容详情 |
| `GET` | `/public/list` | 公开内容列表 |
| `GET` | `/my/list` | 我的内容列表 |
| `GET` | `/user/{userId}` | 某用户内容 |
| `DELETE` | `/delete/{id}` | 删除内容 |
| `GET` | `/hot` | 热门内容 |
| `GET` | `/search` | 搜索内容 |
| `POST` | `/like` | 点赞 |
| `DELETE` | `/unlike` | 取消点赞 |
| `GET` | `/isLiked` | 是否点赞 |
| `POST` | `/favorite` | 收藏 |
| `DELETE` | `/unfavorite` | 取消收藏 |
| `GET` | `/isFavorited` | 是否收藏 |
| `GET` | `/knowledge-graph` | 内容知识图谱 |
| `GET` | `/tag-cloud` | 标签云 |
| `GET` | `/category-stats` | 分类统计 |
| `GET` | `/tag-cooccurrence` | 标签共现 |
| `GET` | `/related-by-tag` | 标签相关推荐 |
| `GET` | `/user-topics/{userId}` | 用户主题画像 |

## 前端对接

用户端：

- `front-end/src/views/StudyDashboard/StudyDashboard.vue`
- `front-end/src/views/StudyDashboard/PracticeCenter.vue`
- `front-end/src/views/StudyDashboard/QuestionBank.vue`
- `front-end/src/views/StudyDashboard/MistakeBook.vue`
- `front-end/src/views/StudyDashboard/ScoreAnalysis.vue`
- `front-end/src/views/StudyDashboard/AnswerRecords.vue`
- `front-end/src/views/content/*`
- `front-end/src/views/growth/GrowthHub.vue`

后台端：

- `cot-admin-web/src/views/learning/*`
- `cot-admin-web/src/views/content/*`

## 业务注意事项

- 题库、考试、错题必须按用户隔离。
- 管理员审核通过前，题目不应进入普通考试候选池。
- 动态数据源切换要谨慎，Mapper 和服务方法要确认落到正确数据库。
- 内容社区需要区分公开内容和用户私有内容。
