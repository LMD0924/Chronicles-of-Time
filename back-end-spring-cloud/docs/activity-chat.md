# 打卡勋章与在线聊天

本文档记录每日打卡、在线时长、勋章发放和聊天功能的后端接口、前后台入口与数据表。

## 所属服务

- 业务服务：`general-service`
- 账号资料：`auth-center`
- 网关路径：`/api/activity/**`、`/api/chat/**`、`/api/user/public/search`
- 数据库：`cot_content`
- 建表脚本：`sql/05_activity_chat_upgrade.sql`

## 用户端入口

| 功能 | 路由 | 页面 |
| --- | --- | --- |
| 每日打卡与勋章 | `/DailyCheckin` | `front-end/src/views/social/DailyCheckin.vue` |
| 在线聊天 | `/Chat` | `front-end/src/views/social/ChatCenter.vue` |

## 后台入口

| 功能 | 路由 | 页面 |
| --- | --- | --- |
| 打卡勋章管理 | `/community/activity-medals` | `cot-admin-web/src/views/community/ActivityMedalManage.vue` |
| 在线聊天管理 | `/community/chat` | `cot-admin-web/src/views/community/ChatManage.vue` |

## 打卡与勋章算法

用户每日首次调用 `POST /api/activity/checkin` 时更新：

- `total_login_days`：累计登录天数。
- `continuous_login_days`：连续登录天数，昨天打过卡则加 1，否则重置为 1。
- `max_continuous_login_days`：历史最长连续登录。
- `last_checkin_date`：最近打卡日期。

前台页面每 60 秒调用 `POST /api/activity/heartbeat`，服务端会把本次活跃秒数累加到：

- `today_online_seconds`
- `total_online_seconds`
- `last_seen_at`

综合活跃积分：

```text
medal_score = total_login_days * 10 + continuous_login_days * 15 + floor(total_online_seconds / 3600) * 5
```

勋章规则表 `medal_rule` 支持以下 `medal_type`：

- `LOGIN_DAYS`：累计登录天数。
- `STREAK_DAYS`：连续登录天数。
- `ONLINE_HOURS`：累计在线小时。
- `TODAY_ONLINE_MINUTES`：今日在线分钟。
- `SCORE`：综合活跃积分。

服务端每次打卡或心跳后自动匹配启用规则，满足阈值且用户未获得过该 `code` 时写入 `user_medal`。

## Activity API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/activity/checkin` | 今日打卡并自动发放勋章 |
| `POST` | `/api/activity/heartbeat` | 累计在线时长，body: `{ "activeSeconds": 60 }` |
| `GET` | `/api/activity/summary` | 获取当前用户打卡、在线和勋章汇总 |
| `GET` | `/api/activity/admin/users` | 后台查看用户活跃统计 |
| `GET` | `/api/activity/admin/medal-rules` | 后台查看勋章规则 |
| `POST` | `/api/activity/admin/medal-rules` | 新增或编辑勋章规则 |
| `POST` | `/api/activity/admin/medal-rules/{id}/status` | 启用/停用规则 |

## Chat API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/chat/users/search?keyword=` | 搜索用户账号、昵称、邮箱或手机号 |
| `POST` | `/api/chat/friends/{friendId}` | 添加好友，服务端写入双向关系 |
| `GET` | `/api/chat/friends` | 我的好友列表 |
| `POST` | `/api/chat/groups` | 创建群聊 |
| `GET` | `/api/chat/groups` | 我的群聊 |
| `GET` | `/api/chat/groups/search?groupNo=` | 按群号搜索群聊 |
| `POST` | `/api/chat/groups/join/{groupNo}` | 加入群聊 |
| `GET` | `/api/chat/conversations` | 最近会话 |
| `POST` | `/api/chat/messages` | 发送单聊或群聊消息 |
| `GET` | `/api/chat/messages` | 查询消息列表 |
| `POST` | `/api/chat/messages/read` | 标记单聊或群聊消息已读 |
| `GET` | `/api/chat/admin/groups` | 后台查看群聊 |
| `GET` | `/api/chat/admin/friends` | 后台查看好友关系 |

## 已读设计

- 单聊：消息写入 `chat_message`，收发双方通过 `chat_message_read` 判断已读。发送者发送后自动已读，接收者打开会话后调用已读接口。
- 群聊：每条消息按群成员维度写入 `chat_message_read`。消息列表返回 `readCount` 和 `unreadCount`，前台展示群聊已读统计。

当前版本使用 REST 短轮询：

- 会话列表每 10 秒刷新一次。
- 当前会话消息每 5 秒刷新一次。

后续如需实时推送，可在保留现有表结构和接口语义的前提下新增 WebSocket/SSE 通道。
