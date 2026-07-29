USE cot_content;

SET @remark_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_friend'
      AND COLUMN_NAME = 'remark'
);
SET @remark_sql = IF(
    @remark_column_exists = 0,
    'ALTER TABLE chat_friend ADD COLUMN remark VARCHAR(40) NULL AFTER friend_id',
    'SELECT 1'
);
PREPARE remark_statement FROM @remark_sql;
EXECUTE remark_statement;
DEALLOCATE PREPARE remark_statement;