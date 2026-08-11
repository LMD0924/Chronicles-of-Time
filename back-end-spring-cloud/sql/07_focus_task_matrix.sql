-- Focus task matrix migration. Run once for databases created before this release.
SET NAMES utf8mb4;
SET time_zone = '+08:00';

USE cot_workplace;
ALTER TABLE career_task
  ADD COLUMN quadrant VARCHAR(40) NOT NULL DEFAULT 'NOT_IMPORTANT_NOT_URGENT' AFTER priority,
  ADD COLUMN start_date DATE DEFAULT NULL AFTER quadrant,
  ADD COLUMN tags VARCHAR(255) DEFAULT NULL AFTER notes,
  ADD COLUMN reminder_enabled TINYINT NOT NULL DEFAULT 0 AFTER tags,
  ADD COLUMN reminder_at DATETIME DEFAULT NULL AFTER reminder_enabled,
  ADD COLUMN repeat_rule VARCHAR(32) NOT NULL DEFAULT 'NONE' AFTER reminder_at,
  ADD COLUMN completed_at DATETIME DEFAULT NULL AFTER repeat_rule;

UPDATE career_task
SET quadrant = CASE
  WHEN priority = 'HIGH' AND due_date IS NOT NULL AND due_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY) THEN 'IMPORTANT_URGENT'
  WHEN priority = 'HIGH' THEN 'IMPORTANT_NOT_URGENT'
  WHEN due_date IS NOT NULL AND due_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY) THEN 'URGENT_NOT_IMPORTANT'
  ELSE 'NOT_IMPORTANT_NOT_URGENT'
END
;
USE cot_platform;
ALTER TABLE sys_notification_preference
  ADD COLUMN default_reminder_minutes INT NOT NULL DEFAULT 10 AFTER week_starts_monday,
  ADD COLUMN default_start_time VARCHAR(5) NOT NULL DEFAULT '09:00' AFTER default_reminder_minutes,
  ADD COLUMN default_end_time VARCHAR(5) NOT NULL DEFAULT '18:00' AFTER default_start_time,
  ADD COLUMN browser_notifications_enabled TINYINT NOT NULL DEFAULT 0 AFTER default_end_time;