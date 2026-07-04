-- 文件说明：在线练习/考试/错题本功能升级脚本，适用于已初始化过的 cot_learning 数据库。
-- Target: MySQL 8.0+
SET NAMES utf8mb4;
USE cot_learning;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_column_if_missing $$
CREATE PROCEDURE cot_add_column_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_column_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DROP PROCEDURE IF EXISTS cot_modify_question_id_nullable $$
CREATE PROCEDURE cot_modify_question_id_nullable()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mistake_record'
      AND COLUMN_NAME = 'question_id'
      AND IS_NULLABLE = 'NO'
  ) THEN
    ALTER TABLE mistake_record MODIFY COLUMN question_id BIGINT UNSIGNED DEFAULT NULL;
  END IF;
END $$

DELIMITER ;

CALL cot_add_column_if_missing('question', 'knowledge_point', '`knowledge_point` VARCHAR(512) DEFAULT NULL AFTER `category_level`');
CALL cot_add_column_if_missing('question', 'options', '`options` JSON DEFAULT NULL AFTER `question_title`');
CALL cot_add_column_if_missing('question', 'audit_status', '`audit_status` VARCHAR(32) NOT NULL DEFAULT ''pending'' AFTER `created_by`');
CALL cot_add_column_if_missing('question', 'audit_remark', '`audit_remark` VARCHAR(255) DEFAULT NULL AFTER `audit_status`');
CALL cot_add_column_if_missing('question', 'audited_by', '`audited_by` BIGINT UNSIGNED DEFAULT NULL AFTER `audit_remark`');
CALL cot_add_column_if_missing('question', 'audited_at', '`audited_at` DATETIME DEFAULT NULL AFTER `audited_by`');

CALL cot_add_column_if_missing('practice_session', 'category_level', '`category_level` VARCHAR(64) DEFAULT NULL AFTER `title`');
CALL cot_add_column_if_missing('practice_session', 'subject_name', '`subject_name` VARCHAR(96) DEFAULT NULL AFTER `category_level`');
CALL cot_add_column_if_missing('practice_session', 'knowledge_points', '`knowledge_points` VARCHAR(512) DEFAULT NULL AFTER `subject_name`');
CALL cot_add_column_if_missing('practice_session', 'difficulty_level', '`difficulty_level` VARCHAR(32) DEFAULT NULL AFTER `knowledge_points`');
CALL cot_add_column_if_missing('practice_session', 'question_ids', '`question_ids` TEXT DEFAULT NULL AFTER `difficulty_level`');
CALL cot_add_column_if_missing('practice_session', 'wrong_count', '`wrong_count` INT NOT NULL DEFAULT 0 AFTER `correct_count`');
CALL cot_add_column_if_missing('practice_session', 'duration_seconds', '`duration_seconds` INT DEFAULT NULL AFTER `score_obtained`');
CALL cot_add_column_if_missing('practice_session', 'anti_cheat_enabled', '`anti_cheat_enabled` TINYINT NOT NULL DEFAULT 0 AFTER `duration_seconds`');
CALL cot_add_column_if_missing('practice_session', 'suspicious_count', '`suspicious_count` INT NOT NULL DEFAULT 0 AFTER `anti_cheat_enabled`');

CALL cot_add_column_if_missing('answer_record', 'knowledge_point', '`knowledge_point` VARCHAR(512) DEFAULT NULL AFTER `category_level`');

CALL cot_modify_question_id_nullable();
CALL cot_add_column_if_missing('mistake_record', 'subject_name', '`subject_name` VARCHAR(96) DEFAULT NULL AFTER `last_answer_record_id`');
CALL cot_add_column_if_missing('mistake_record', 'mistake_name', '`mistake_name` VARCHAR(255) DEFAULT NULL AFTER `subject_name`');
CALL cot_add_column_if_missing('mistake_record', 'mistake_type', '`mistake_type` VARCHAR(32) DEFAULT NULL AFTER `mistake_name`');
CALL cot_add_column_if_missing('mistake_record', 'question_options', '`question_options` JSON DEFAULT NULL AFTER `mistake_type`');
CALL cot_add_column_if_missing('mistake_record', 'student_choice', '`student_choice` TEXT DEFAULT NULL AFTER `question_options`');
CALL cot_add_column_if_missing('mistake_record', 'wrong_answer', '`wrong_answer` TEXT DEFAULT NULL AFTER `student_choice`');
CALL cot_add_column_if_missing('mistake_record', 'correct_answer', '`correct_answer` TEXT DEFAULT NULL AFTER `wrong_answer`');
CALL cot_add_column_if_missing('mistake_record', 'answer_analysis', '`answer_analysis` TEXT DEFAULT NULL AFTER `correct_answer`');
CALL cot_add_column_if_missing('mistake_record', 'knowledge_point', '`knowledge_point` VARCHAR(512) DEFAULT NULL AFTER `answer_analysis`');
CALL cot_add_column_if_missing('mistake_record', 'mistake_date', '`mistake_date` DATE DEFAULT NULL AFTER `mastered`');
CALL cot_add_column_if_missing('mistake_record', 'last_review_date', '`last_review_date` DATE DEFAULT NULL AFTER `mistake_date`');

UPDATE question
SET audit_status = 'approved'
WHERE audit_status IS NULL OR audit_status = '';

DROP PROCEDURE IF EXISTS cot_modify_question_id_nullable;
DROP PROCEDURE IF EXISTS cot_add_column_if_missing;
