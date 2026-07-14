-- Chronicles of Time field upgrades.
-- Adds columns required by current entities and upgraded features.
-- Run after 01_schema.sql.


/* =========================================================
   From 02_learning_practice_upgrade.sql
   ========================================================= */
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


/* =========================================================
   From 06_growth_workplace_fields.sql
   ========================================================= */
-- Growth record workplace fields upgrade for cot_content.
-- Run this script on environments that already created growth_record from an older schema.

SET NAMES utf8mb4;
USE cot_content;

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

DELIMITER ;

CALL cot_add_column_if_missing('growth_record', 'company_name', '`company_name` VARCHAR(160) DEFAULT NULL AFTER `dream_major`');
CALL cot_add_column_if_missing('growth_record', 'job_title', '`job_title` VARCHAR(160) DEFAULT NULL AFTER `company_name`');
CALL cot_add_column_if_missing('growth_record', 'job_content', '`job_content` TEXT DEFAULT NULL AFTER `job_title`');
CALL cot_add_column_if_missing('growth_record', 'work_skills', '`work_skills` TEXT DEFAULT NULL AFTER `job_content`');
CALL cot_add_column_if_missing('growth_record', 'work_achievements', '`work_achievements` TEXT DEFAULT NULL AFTER `work_skills`');
CALL cot_add_column_if_missing('growth_record', 'work_challenges', '`work_challenges` TEXT DEFAULT NULL AFTER `work_achievements`');
CALL cot_add_column_if_missing('growth_record', 'career_plan', '`career_plan` TEXT DEFAULT NULL AFTER `work_challenges`');

DROP PROCEDURE IF EXISTS cot_add_column_if_missing;

/* =========================================================
   From 07_high_service_entity_fields.sql
   ========================================================= */
-- High-service entity field compatibility upgrade for cot_highschool.
-- Run this script on environments that already created high-service tables from an older schema.

SET NAMES utf8mb4;
USE cot_highschool;

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

DELIMITER ;

CALL cot_add_column_if_missing('hs_course_guidance', 'student_name', '`student_name` VARCHAR(80) DEFAULT NULL AFTER `student_id`');
CALL cot_add_column_if_missing('hs_course_guidance', 'guidance_date', '`guidance_date` DATE DEFAULT NULL AFTER `student_name`');
CALL cot_add_column_if_missing('hs_course_guidance', 'suggested_major', '`suggested_major` VARCHAR(160) DEFAULT NULL AFTER `recommended_combination_name`');
CALL cot_add_column_if_missing('hs_course_guidance', 'strength_analysis', '`strength_analysis` TEXT DEFAULT NULL AFTER `suggested_major`');
CALL cot_add_column_if_missing('hs_course_guidance', 'weakness_analysis', '`weakness_analysis` TEXT DEFAULT NULL AFTER `strength_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'opportunity_analysis', '`opportunity_analysis` TEXT DEFAULT NULL AFTER `weakness_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'threat_analysis', '`threat_analysis` TEXT DEFAULT NULL AFTER `opportunity_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'action_plan', '`action_plan` TEXT DEFAULT NULL AFTER `threat_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'advisor_name', '`advisor_name` VARCHAR(80) DEFAULT NULL AFTER `advisor_id`');
CALL cot_add_column_if_missing('hs_course_guidance', 'advisor_position', '`advisor_position` VARCHAR(120) DEFAULT NULL AFTER `advisor_name`');
CALL cot_add_column_if_missing('hs_course_guidance', 'student_feedback', '`student_feedback` TEXT DEFAULT NULL AFTER `advisor_position`');
CALL cot_add_column_if_missing('hs_course_guidance', 'parent_feedback', '`parent_feedback` TEXT DEFAULT NULL AFTER `student_feedback`');
CALL cot_add_column_if_missing('hs_course_guidance', 'follow_up_date', '`follow_up_date` DATE DEFAULT NULL AFTER `parent_feedback`');

CALL cot_add_column_if_missing('hs_selection_intention', 'strength_subjects', '`strength_subjects` VARCHAR(255) DEFAULT NULL AFTER `target_university`');
CALL cot_add_column_if_missing('hs_selection_intention', 'weak_subjects', '`weak_subjects` VARCHAR(255) DEFAULT NULL AFTER `strength_subjects`');
CALL cot_add_column_if_missing('hs_selection_intention', 'career_interest', '`career_interest` VARCHAR(255) DEFAULT NULL AFTER `weak_subjects`');
CALL cot_add_column_if_missing('hs_selection_intention', 'teacher_feedback', '`teacher_feedback` TEXT DEFAULT NULL AFTER `career_interest`');
CALL cot_add_column_if_missing('hs_selection_intention', 'parent_feedback', '`parent_feedback` TEXT DEFAULT NULL AFTER `teacher_feedback`');
CALL cot_add_column_if_missing('hs_selection_intention', 'submit_time', '`submit_time` DATETIME DEFAULT NULL AFTER `status`');
CALL cot_add_column_if_missing('hs_selection_intention', 'evaluate_time', '`evaluate_time` DATETIME DEFAULT NULL AFTER `submit_time`');
CALL cot_add_column_if_missing('hs_selection_intention', 'evaluate_by', '`evaluate_by` VARCHAR(80) DEFAULT NULL AFTER `evaluate_time`');

CALL cot_add_column_if_missing('hs_selection_history', 'approver', '`approver` VARCHAR(80) DEFAULT NULL AFTER `change_reason`');

CALL cot_add_column_if_missing('hs_grading_scale', 'percentage_top', '`percentage_top` DECIMAL(8,4) DEFAULT NULL AFTER `grade_level`');
CALL cot_add_column_if_missing('hs_grading_scale', 'percentage_bottom', '`percentage_bottom` DECIMAL(8,4) DEFAULT NULL AFTER `percentage_top`');
CALL cot_add_column_if_missing('hs_grading_scale', 'raw_score_min', '`raw_score_min` DECIMAL(8,2) DEFAULT NULL AFTER `assigned_score_max`');
CALL cot_add_column_if_missing('hs_grading_scale', 'raw_score_max', '`raw_score_max` DECIMAL(8,2) DEFAULT NULL AFTER `raw_score_min`');
CALL cot_add_column_if_missing('hs_grading_scale', 'academic_year', '`academic_year` VARCHAR(32) DEFAULT NULL AFTER `raw_score_max`');
CALL cot_add_column_if_missing('hs_grading_scale', 'is_active', '`is_active` TINYINT NOT NULL DEFAULT 1 AFTER `academic_year`');

CALL cot_add_column_if_missing('hs_major_subject_match', 'subject_id', '`subject_id` BIGINT UNSIGNED DEFAULT NULL AFTER `major_name`');
CALL cot_add_column_if_missing('hs_major_subject_match', 'subject_name', '`subject_name` VARCHAR(96) DEFAULT NULL AFTER `subject_id`');
CALL cot_add_column_if_missing('hs_major_subject_match', 'importance_level', '`importance_level` TINYINT NOT NULL DEFAULT 3 AFTER `subject_name`');
CALL cot_add_column_if_missing('hs_major_subject_match', 'description', '`description` TEXT DEFAULT NULL AFTER `matching_score`');

CALL cot_add_column_if_missing('gaokao_admission_plan', 'university_name', '`university_name` VARCHAR(160) DEFAULT NULL AFTER `university_id`');
CALL cot_add_column_if_missing('gaokao_university', 'is_public', '`is_public` TINYINT DEFAULT NULL AFTER `ownership`');

DROP PROCEDURE IF EXISTS cot_add_column_if_missing;

/* =========================================================
   User ownership scope fields
   ========================================================= */
-- All mutable business rows should carry a user_id. user_id = 0 is reserved for platform/global data.
-- Application rule: normal users filter by their own user_id; administrators can query and operate across users.
USE cot_identity;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing $$
CREATE PROCEDURE cot_add_user_scope_if_missing(
  IN p_table_name VARCHAR(64)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = 'user_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''owner user id, 0 means platform/global data'' AFTER id');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = CONCAT('idx_', p_table_name, '_user_id')
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX idx_', p_table_name, '_user_id (user_id)');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;
CALL cot_add_user_scope_if_missing('iam_role');
CALL cot_add_user_scope_if_missing('iam_permission');
CALL cot_add_user_scope_if_missing('iam_role_permission');

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing;

USE cot_profile;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing $$
CREATE PROCEDURE cot_add_user_scope_if_missing(
  IN p_table_name VARCHAR(64)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = 'user_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''owner user id, 0 means platform/global data'' AFTER id');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = CONCAT('idx_', p_table_name, '_user_id')
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX idx_', p_table_name, '_user_id (user_id)');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;
CALL cot_add_user_scope_if_missing('resume_education');
CALL cot_add_user_scope_if_missing('resume_work_experience');
CALL cot_add_user_scope_if_missing('resume_project');
CALL cot_add_user_scope_if_missing('resume_skill');
CALL cot_add_user_scope_if_missing('resume_certificate');
CALL cot_add_user_scope_if_missing('resume_social_experience');

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing;

USE cot_content;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing $$
CREATE PROCEDURE cot_add_user_scope_if_missing(
  IN p_table_name VARCHAR(64)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = 'user_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''owner user id, 0 means platform/global data'' AFTER id');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = CONCAT('idx_', p_table_name, '_user_id')
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX idx_', p_table_name, '_user_id (user_id)');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;
CALL cot_add_user_scope_if_missing('content_category');
CALL cot_add_user_scope_if_missing('content_media');
CALL cot_add_user_scope_if_missing('content_tag');
CALL cot_add_user_scope_if_missing('content_article_tag');
CALL cot_add_user_scope_if_missing('content_audit');
CALL cot_add_user_scope_if_missing('medal_rule');
CALL cot_add_user_scope_if_missing('chat_group');

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing;

USE cot_learning;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing $$
CREATE PROCEDURE cot_add_user_scope_if_missing(
  IN p_table_name VARCHAR(64)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = 'user_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''owner user id, 0 means platform/global data'' AFTER id');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = CONCAT('idx_', p_table_name, '_user_id')
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX idx_', p_table_name, '_user_id (user_id)');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;
CALL cot_add_user_scope_if_missing('learning_subject');
CALL cot_add_user_scope_if_missing('knowledge_point');
CALL cot_add_user_scope_if_missing('knowledge_edge');
CALL cot_add_user_scope_if_missing('question_option');

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing;

USE cot_highschool;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing $$
CREATE PROCEDURE cot_add_user_scope_if_missing(
  IN p_table_name VARCHAR(64)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = 'user_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''owner user id, 0 means platform/global data'' AFTER id');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = CONCAT('idx_', p_table_name, '_user_id')
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX idx_', p_table_name, '_user_id (user_id)');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;
CALL cot_add_user_scope_if_missing('hs_subject');
CALL cot_add_user_scope_if_missing('hs_subject_combination');
CALL cot_add_user_scope_if_missing('hs_grading_scale');
CALL cot_add_user_scope_if_missing('gaokao_university');
CALL cot_add_user_scope_if_missing('gaokao_major');
CALL cot_add_user_scope_if_missing('gaokao_admission_plan');
CALL cot_add_user_scope_if_missing('gaokao_major_requirement');
CALL cot_add_user_scope_if_missing('user_volunteer_detail');
CALL cot_add_user_scope_if_missing('admission_simulation');

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing;

USE cot_university;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing $$
CREATE PROCEDURE cot_add_user_scope_if_missing(
  IN p_table_name VARCHAR(64)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = 'user_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''owner user id, 0 means platform/global data'' AFTER id');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = CONCAT('idx_', p_table_name, '_user_id')
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX idx_', p_table_name, '_user_id (user_id)');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;
CALL cot_add_user_scope_if_missing('uni_major');
CALL cot_add_user_scope_if_missing('uni_course_prerequisite');
CALL cot_add_user_scope_if_missing('thesis_suggestion');

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing;

USE cot_platform;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing $$
CREATE PROCEDURE cot_add_user_scope_if_missing(
  IN p_table_name VARCHAR(64)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = 'user_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''owner user id, 0 means platform/global data'' AFTER id');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND INDEX_NAME = CONCAT('idx_', p_table_name, '_user_id')
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX idx_', p_table_name, '_user_id (user_id)');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;
CALL cot_add_user_scope_if_missing('sys_dict_type');
CALL cot_add_user_scope_if_missing('sys_dict_item');

DROP PROCEDURE IF EXISTS cot_add_user_scope_if_missing;

