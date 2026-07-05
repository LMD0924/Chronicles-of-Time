-- Activity check-in, medal and chat upgrade for cot_content.
-- Run this script on the cot_content database.

CREATE TABLE IF NOT EXISTS user_activity_stats (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  total_login_days INT NOT NULL DEFAULT 0,
  continuous_login_days INT NOT NULL DEFAULT 0,
  max_continuous_login_days INT NOT NULL DEFAULT 0,
  total_online_seconds BIGINT NOT NULL DEFAULT 0,
  today_online_seconds BIGINT NOT NULL DEFAULT 0,
  last_checkin_date DATE NULL,
  online_date DATE NULL,
  last_seen_at DATETIME NULL,
  medal_score INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_activity_user (user_id),
  KEY idx_user_activity_seen (last_seen_at),
  KEY idx_user_activity_checkin (last_checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User activity, check-in and online duration stats';

CREATE TABLE IF NOT EXISTS medal_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  medal_type VARCHAR(32) NOT NULL COMMENT 'LOGIN_DAYS/STREAK_DAYS/ONLINE_HOURS/TODAY_ONLINE_MINUTES/SCORE',
  threshold_value INT NOT NULL,
  icon VARCHAR(32) NULL,
  color VARCHAR(32) NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_medal_rule_code (code),
  KEY idx_medal_rule_type_enabled (medal_type, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Medal issuing rules';

CREATE TABLE IF NOT EXISTS user_medal (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  rule_id BIGINT NULL,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  medal_type VARCHAR(32) NOT NULL,
  source_value INT NOT NULL DEFAULT 0,
  icon VARCHAR(32) NULL,
  color VARCHAR(32) NULL,
  awarded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_medal_code (user_id, code),
  KEY idx_user_medal_user_time (user_id, awarded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Awarded medals';

INSERT INTO medal_rule (id, code, name, description, medal_type, threshold_value, icon, color, enabled)
VALUES
  (910000000000000001, 'LOGIN_DAYS_1', '初次打卡', '完成第一次每日打卡', 'LOGIN_DAYS', 1, 'CheckCircle', '#16a34a', 1),
  (910000000000000002, 'LOGIN_DAYS_7', '七日同行', '累计登录 7 天', 'LOGIN_DAYS', 7, 'CalendarCheck', '#2563eb', 1),
  (910000000000000003, 'LOGIN_DAYS_30', '月度坚持者', '累计登录 30 天', 'LOGIN_DAYS', 30, 'Trophy', '#7c3aed', 1),
  (910000000000000004, 'STREAK_DAYS_3', '三日连击', '连续登录 3 天', 'STREAK_DAYS', 3, 'Flame', '#ea580c', 1),
  (910000000000000005, 'STREAK_DAYS_14', '稳定节奏', '连续登录 14 天', 'STREAK_DAYS', 14, 'Medal', '#db2777', 1),
  (910000000000000006, 'ONLINE_HOURS_10', '在线十小时', '累计在线时长达到 10 小时', 'ONLINE_HOURS', 10, 'Clock', '#0891b2', 1),
  (910000000000000007, 'TODAY_ONLINE_MINUTES_60', '今日专注', '今日在线时长达到 60 分钟', 'TODAY_ONLINE_MINUTES', 60, 'Timer', '#4f46e5', 1),
  (910000000000000008, 'SCORE_500', '拾光达人', '综合活跃积分达到 500', 'SCORE', 500, 'Award', '#ca8a04', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  medal_type = VALUES(medal_type),
  threshold_value = VALUES(threshold_value),
  icon = VALUES(icon),
  color = VALUES(color),
  enabled = VALUES(enabled);

CREATE TABLE IF NOT EXISTS chat_friend (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_friend_pair (user_id, friend_id),
  KEY idx_chat_friend_friend (friend_id),
  KEY idx_chat_friend_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='One-way friend relation, stored symmetrically';

CREATE TABLE IF NOT EXISTS chat_group (
  id BIGINT NOT NULL PRIMARY KEY,
  group_no VARCHAR(16) NOT NULL,
  name VARCHAR(80) NOT NULL,
  announcement VARCHAR(500) NULL,
  owner_id BIGINT NOT NULL,
  member_count INT NOT NULL DEFAULT 0,
  searchable TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_group_no (group_no),
  KEY idx_chat_group_owner (owner_id),
  KEY idx_chat_group_search (searchable, group_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chat groups';

CREATE TABLE IF NOT EXISTS chat_group_member (
  id BIGINT NOT NULL PRIMARY KEY,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_read_at DATETIME NULL,
  UNIQUE KEY uk_chat_group_member (group_id, user_id),
  KEY idx_chat_group_member_user (user_id),
  KEY idx_chat_group_member_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chat group members';

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT NOT NULL PRIMARY KEY,
  conversation_type VARCHAR(16) NOT NULL COMMENT 'PRIVATE/GROUP',
  group_id BIGINT NULL,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NULL,
  content_type VARCHAR(16) NOT NULL DEFAULT 'TEXT',
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  recalled_at DATETIME NULL,
  KEY idx_chat_message_private (conversation_type, sender_id, receiver_id, id),
  KEY idx_chat_message_group (conversation_type, group_id, id),
  KEY idx_chat_message_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chat messages';

CREATE TABLE IF NOT EXISTS chat_message_read (
  id BIGINT NOT NULL PRIMARY KEY,
  message_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_message_read (message_id, user_id),
  KEY idx_chat_read_user (user_id, read_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Message read receipts';
