USE cot_content;

SET @add_muted_all = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_group' AND COLUMN_NAME = 'muted_all') = 0,
    'ALTER TABLE chat_group ADD COLUMN muted_all TINYINT(1) NOT NULL DEFAULT 0 AFTER searchable',
    'SELECT 1'
);
PREPARE statement_muted_all FROM @add_muted_all;
EXECUTE statement_muted_all;
DEALLOCATE PREPARE statement_muted_all;

SET @add_pinned_message = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_group' AND COLUMN_NAME = 'pinned_message_id') = 0,
    'ALTER TABLE chat_group ADD COLUMN pinned_message_id BIGINT NULL AFTER muted_all',
    'SELECT 1'
);
PREPARE statement_pinned_message FROM @add_pinned_message;
EXECUTE statement_pinned_message;
DEALLOCATE PREPARE statement_pinned_message;

SET @add_muted_until = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_group_member' AND COLUMN_NAME = 'muted_until') = 0,
    'ALTER TABLE chat_group_member ADD COLUMN muted_until DATETIME NULL AFTER status',
    'SELECT 1'
);
PREPARE statement_muted_until FROM @add_muted_until;
EXECUTE statement_muted_until;
DEALLOCATE PREPARE statement_muted_until;