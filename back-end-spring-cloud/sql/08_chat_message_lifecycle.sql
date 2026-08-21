USE cot_content;

SET @add_recalled_at = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_message' AND COLUMN_NAME = 'recalled_at') = 0,
    'ALTER TABLE chat_message ADD COLUMN recalled_at DATETIME NULL AFTER created_at',
    'SELECT 1'
);
PREPARE statement_recalled_at FROM @add_recalled_at;
EXECUTE statement_recalled_at;
DEALLOCATE PREPARE statement_recalled_at;

CREATE TABLE IF NOT EXISTS chat_message_hidden (
  id BIGINT NOT NULL PRIMARY KEY,
  message_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  hidden_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_message_hidden (message_id, user_id),
  KEY idx_chat_message_hidden_user (user_id, hidden_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Per-user hidden chat messages';
