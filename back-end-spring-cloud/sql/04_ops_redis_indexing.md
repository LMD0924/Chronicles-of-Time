# 索引、缓存与运维建议

## 1. 索引策略

### 1.1 通用规范

- 所有业务表主键使用 `BIGINT UNSIGNED`。
- 所有列表页必须有稳定排序字段，优先 `created_at` 或业务时间。
- 高频查询使用组合索引，组合顺序遵循：等值字段、范围字段、排序字段。
- 跨库关联不做物理外键，通过唯一键、业务校验和数据巡检保证一致性。
- 大文本字段不参与普通索引，搜索用 FULLTEXT 或 Elasticsearch。

### 1.2 重点索引

- 内容流：`content_article(visibility, status, is_top, publish_at)`
- 用户文章：`content_article(user_id, created_at)`
- 评论分页：`content_comment(content_id, created_at)`
- 用户答题：`answer_record(user_id, answer_at)`
- 知识图谱统计：`knowledge_mastery_stat(user_id, subject_id, mastery_score)`
- 错题复习：`mistake_record(user_id, mastered, next_review_date)`
- 志愿计划筛选：`gaokao_admission_plan(province, admission_year, student_type, min_rank)`
- 大学课程树：`uni_course(major_id, term_no, status)`
- 用户修课：`uni_student_course(user_id, major_id, semester)`

## 2. Redis Key 设计

### 2.1 认证与会话

```text
cot:auth:access:{jti} -> user_id, TTL = access_token_expire
cot:auth:refresh:{jti} -> user_id, TTL = refresh_token_expire
cot:auth:blacklist:{jti} -> 1, TTL = token_remaining_seconds
cot:auth:captcha:{scene}:{uuid} -> code, TTL = 5min
cot:auth:login_fail:{username}:{ip} -> count, TTL = 15min
```

### 2.2 用户与权限

```text
cot:user:profile:{user_id} -> user profile JSON, TTL = 30min
cot:user:roles:{user_id} -> role codes, TTL = 30min
cot:rbac:permissions:{user_id} -> permission codes, TTL = 30min
```

### 2.3 内容社区

```text
cot:content:detail:{content_id} -> article detail, TTL = 10min
cot:content:feed:public:{page_cursor} -> content ids, TTL = 2min
cot:content:stat:{content_id} -> view/like/favorite/comment counters, TTL = 30min
cot:content:user:liked:{user_id}:{content_id} -> 1, TTL = 1day
```

计数建议：

- 点赞、收藏、评论写 MySQL 明细。
- 计数先写 Redis Hash，再定时批量回刷 `content_article`。
- 删除或审核隐藏内容时主动清理详情缓存。

### 2.4 学习模块

```text
cot:learning:question:{question_id} -> question JSON, TTL = 1h
cot:learning:practice:{session_id} -> session runtime state, TTL = 2h
cot:learning:mastery:{user_id}:{subject_id} -> mastery graph, TTL = 10min
cot:learning:weak_points:{user_id} -> sorted set, TTL = 30min
```

答题记录必须落库，Redis 只做会话暂存和图谱缓存。

### 2.5 志愿填报

```text
cot:gaokao:plan:{province}:{year}:{student_type}:{score_band} -> admission plan ids, TTL = 1day
cot:gaokao:university:{university_id} -> university detail, TTL = 1day
cot:volunteer:simulation:{plan_id} -> simulation result, TTL = 30min
```

招生计划属于读多写少数据，适合长 TTL 缓存。

## 3. 后台管理预留

`cot-admin-web` 当前路由为空，但数据库已预留：

- RBAC：`iam_role`、`iam_permission`、`iam_user_role`、`iam_role_permission`
- 内容审核：`content_audit`
- 字典管理：`sys_dict_type`、`sys_dict_item`
- 文件管理：`file_asset`
- 通知管理：`sys_notification`
- 操作审计：`admin_operation_log`

后台扩展时优先使用这些通用表，避免为每个页面临时造孤立表。

## 4. 监控与巡检

建议定时任务：

- 每 5 分钟：回刷内容计数、用户通知未读数。
- 每 10 分钟：聚合答题记录到 `knowledge_mastery_stat`。
- 每天凌晨：更新题目 `use_count`、`mistake_count`、`mistake_rate`。
- 每天凌晨：检测孤儿数据，例如文章无作者、课程无专业、志愿明细无主方案。
- 每月：归档 `admin_operation_log`、`sys_api_access_log`。

## 5. 备份策略

- 身份库、用户库：每日全量 + binlog 实时增量，保留 180 天。
- 内容库、学习库：每日全量 + binlog 增量，保留 90 天；分片表按月冷归档。
- 高中/大学基础数据：每周全量，导入前先备份。
- 文件资产：数据库和对象存储必须同时备份，`file_asset.object_key` 是恢复关键。

## 6. 安全建议

- `iam_user.password_hash` 禁止返回前端。
- 手机号、邮箱、真实姓名属于敏感信息，后台查看应记录 `admin_operation_log`。
- 简历公开状态默认私有。
- API 日志不要长期保存完整请求体，避免写入密码、token、身份证等敏感字段。
