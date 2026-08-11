-- Productivity features migration. Run once for databases created before this release.
SET NAMES utf8mb4;
SET time_zone = '+08:00';

USE cot_content;
CREATE TABLE IF NOT EXISTS growth_weekly_report (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  week_start DATE NOT NULL,
  week_end DATE NOT NULL,
  report_json LONGTEXT DEFAULT NULL,
  reflection TEXT DEFAULT NULL,
  next_week_focus TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_weekly_report_user_week (user_id, week_start),
  KEY idx_weekly_report_user_time (user_id, week_end)
) ENGINE=InnoDB COMMENT='Persisted weekly growth review';

USE cot_platform;
ALTER TABLE sys_notification
  ADD COLUMN dedupe_key VARCHAR(160) DEFAULT NULL AFTER biz_id,
  ADD COLUMN action_path VARCHAR(255) DEFAULT NULL AFTER dedupe_key,
  ADD COLUMN due_at VARCHAR(32) DEFAULT NULL AFTER action_path,
  ADD COLUMN dismissed_at DATETIME DEFAULT NULL AFTER read_at,
  ADD UNIQUE KEY uk_notification_user_dedupe (user_id, dedupe_key);

CREATE TABLE IF NOT EXISTS sys_notification_preference (
  user_id BIGINT UNSIGNED NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  quiet_hours_enabled TINYINT NOT NULL DEFAULT 0,
  quiet_start VARCHAR(5) NOT NULL DEFAULT '22:00',
  quiet_end VARCHAR(5) NOT NULL DEFAULT '08:00',
  preferred_stage VARCHAR(32) NOT NULL DEFAULT 'all',
  week_starts_monday TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id)
) ENGINE=InnoDB COMMENT='Notification, quiet hours and growth experience preferences';