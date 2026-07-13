-- Chronicles of Time test data.
-- Run after 01_schema.sql and 02_field_upgrades.sql.

USE cot_content;

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

-- All-service generic test data seed for Chronicles of Time.
-- Run after schema and upgrade scripts: 01, 02, 05, 06, 07.
-- It inserts one generic row for every base table in the project databases,
-- then inserts realistic high-school subject data for the course-selection UI.

SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET SESSION group_concat_max_len = 102400;
SET @cot_seed_user_id := 2075127851337654274;
SET @cot_seed_friend_id := 2075127851337654275;

DELIMITER $$

DROP PROCEDURE IF EXISTS cot_seed_one_table $$
CREATE PROCEDURE cot_seed_one_table(
  IN p_schema_name VARCHAR(64),
  IN p_table_name VARCHAR(64),
  IN p_seed_id BIGINT,
  IN p_user_id BIGINT)
BEGIN
  DECLARE v_columns LONGTEXT;
  DECLARE v_values LONGTEXT;
  DECLARE v_first_column VARCHAR(64);

  SELECT GROUP_CONCAT(CONCAT('`', COLUMN_NAME, '`') ORDER BY ORDINAL_POSITION SEPARATOR ', ')
    INTO v_columns
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = p_schema_name
    AND TABLE_NAME = p_table_name;

  SELECT COLUMN_NAME
    INTO v_first_column
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = p_schema_name
    AND TABLE_NAME = p_table_name
  ORDER BY ORDINAL_POSITION
  LIMIT 1;

  SELECT GROUP_CONCAT(
    CASE
      WHEN DATA_TYPE = 'bigint' THEN
        CASE
          WHEN COLUMN_NAME = 'id' AND p_schema_name = 'cot_identity' AND p_table_name = 'iam_user' THEN CAST(p_user_id AS CHAR)
          WHEN COLUMN_NAME = 'id' THEN CAST(p_seed_id AS CHAR)
          WHEN COLUMN_NAME IN ('user_id', 'owner_user_id', 'student_id', 'created_by', 'updated_by', 'auditor_id', 'admin_user_id', 'sender_id', 'receiver_id', 'suggester_id') THEN CAST(p_user_id AS CHAR)
          WHEN COLUMN_NAME = 'friend_id' THEN CAST(p_user_id + 1 AS CHAR)
          WHEN COLUMN_NAME IN ('parent_id', 'root_id', 'reply_to_user_id', 'tenant_id') THEN '0'
          WHEN COLUMN_NAME LIKE '%count%' OR COLUMN_NAME LIKE '%seconds%' OR COLUMN_NAME LIKE '%size%' THEN '1'
          ELSE CAST(p_seed_id AS CHAR)
        END
      WHEN DATA_TYPE IN ('tinyint', 'smallint', 'mediumint', 'int') THEN
        CASE
          WHEN COLUMN_NAME IN ('admission_year') THEN '2026'
          WHEN COLUMN_NAME IN ('founded_year') THEN '1898'
          WHEN COLUMN_NAME IN ('parent_id', 'root_id', 'tenant_id') THEN '0'
          WHEN COLUMN_NAME LIKE '%rank%' THEN '1000'
          ELSE '1'
        END
      WHEN DATA_TYPE IN ('decimal', 'numeric', 'float', 'double') THEN '1.00'
      WHEN DATA_TYPE = 'date' THEN 'CURDATE()'
      WHEN DATA_TYPE IN ('datetime', 'timestamp') THEN CASE WHEN IS_NULLABLE = 'YES' AND COLUMN_NAME IN ('deleted_at', 'revoked_at', 'recalled_at') THEN 'NULL' ELSE 'NOW()' END
      WHEN DATA_TYPE = 'json' THEN 'JSON_OBJECT(''seed'', true)'
      ELSE
        CASE
          WHEN COLUMN_NAME LIKE '%status%' THEN QUOTE('ACTIVE')
          WHEN COLUMN_NAME LIKE '%type%' THEN QUOTE('TEST')
          WHEN COLUMN_NAME LIKE '%code%' THEN QUOTE(LEFT(CONCAT('SEED_', p_seed_id), COALESCE(CHARACTER_MAXIMUM_LENGTH, 64)))
          WHEN COLUMN_NAME LIKE '%name%' THEN QUOTE(LEFT(CONCAT('测试', p_table_name), COALESCE(CHARACTER_MAXIMUM_LENGTH, 64)))
          WHEN COLUMN_NAME LIKE '%title%' THEN QUOTE(LEFT(CONCAT('测试标题-', p_table_name), COALESCE(CHARACTER_MAXIMUM_LENGTH, 64)))
          WHEN COLUMN_NAME LIKE '%email%' THEN QUOTE('seed@example.com')
          WHEN COLUMN_NAME LIKE '%phone%' THEN QUOTE('13800000000')
          WHEN COLUMN_NAME LIKE '%url%' THEN QUOTE('https://example.com/seed')
          WHEN COLUMN_NAME LIKE '%ip%' THEN QUOTE('127.0.0.1')
          WHEN COLUMN_NAME LIKE '%hash%' THEN QUOTE('seed_hash')
          WHEN COLUMN_NAME LIKE '%password%' THEN QUOTE('$2a$10$seed.password.hash')
          ELSE QUOTE(LEFT(CONCAT('测试数据-', p_table_name, '-', COLUMN_NAME), COALESCE(CHARACTER_MAXIMUM_LENGTH, 255)))
        END
    END
    ORDER BY ORDINAL_POSITION SEPARATOR ', '
  ) INTO v_values
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = p_schema_name
    AND TABLE_NAME = p_table_name;

  IF v_columns IS NOT NULL AND v_values IS NOT NULL THEN
    SET @seed_sql = CONCAT(
      'INSERT INTO `', p_schema_name, '`.`', p_table_name, '` (', v_columns, ') VALUES (', v_values, ') ',
      'ON DUPLICATE KEY UPDATE `', v_first_column, '` = `', v_first_column, '`'
    );
    PREPARE stmt FROM @seed_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DROP PROCEDURE IF EXISTS cot_seed_all_tables $$
CREATE PROCEDURE cot_seed_all_tables(IN p_user_id BIGINT)
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE v_schema VARCHAR(64);
  DECLARE v_table VARCHAR(64);
  DECLARE v_counter BIGINT DEFAULT 0;
  DECLARE cur CURSOR FOR
    SELECT TABLE_SCHEMA, TABLE_NAME
    FROM information_schema.TABLES
    WHERE TABLE_TYPE = 'BASE TABLE'
      AND TABLE_SCHEMA IN (
        'cot_identity', 'cot_profile', 'cot_content', 'cot_learning',
        'cot_highschool', 'cot_university', 'cot_platform', 'cot_workplace', 'cot_advanced'
      )
    ORDER BY TABLE_SCHEMA, TABLE_NAME;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  seed_loop: LOOP
    FETCH cur INTO v_schema, v_table;
    IF done = 1 THEN
      LEAVE seed_loop;
    END IF;
    SET v_counter = v_counter + 1;
    CALL cot_seed_one_table(v_schema, v_table, 900000000000000000 + v_counter, p_user_id);
  END LOOP;
  CLOSE cur;
END $$

DELIMITER ;

CALL cot_seed_all_tables(@cot_seed_user_id);

-- Meaningful data for course-selection UI.
USE cot_highschool;

INSERT INTO hs_subject (
  id, subject_code, subject_name, subject_type, description, sort_order, status, created_at, updated_at
) VALUES
  (900001, 'CHN', '语文', 3, '统一高考必考科目', 1, 1, NOW(), NOW()),
  (900002, 'MATH', '数学', 3, '统一高考必考科目', 2, 1, NOW(), NOW()),
  (900003, 'ENG', '英语', 3, '统一高考必考科目', 3, 1, NOW(), NOW()),
  (900101, 'PHY', '物理', 1, '首选科目', 1, 1, NOW(), NOW()),
  (900102, 'HIS', '历史', 1, '首选科目', 2, 1, NOW(), NOW()),
  (900201, 'CHE', '化学', 2, '再选科目', 1, 1, NOW(), NOW()),
  (900202, 'BIO', '生物', 2, '再选科目', 2, 1, NOW(), NOW()),
  (900203, 'POL', '政治', 2, '再选科目', 3, 1, NOW(), NOW()),
  (900204, 'GEO', '地理', 2, '再选科目', 4, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  subject_name = VALUES(subject_name),
  subject_type = VALUES(subject_type),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  updated_at = NOW();

INSERT INTO hs_subject_combination (
  id, combination_code, combination_name,
  first_subject_id, second_subject_1_id, second_subject_2_id,
  first_subject_name, second_subject_1_name, second_subject_2_name,
  major_coverage_rate, recommended_level, description, status, created_at, updated_at
) VALUES
  (901001, 'PHY_CHE_BIO', '物化生', 900101, 900201, 900202, '物理', '化学', '生物', 96.00, 'HIGH', '理工、医学、生命科学方向覆盖面较广', 1, NOW(), NOW()),
  (901002, 'PHY_CHE_POL', '物化政', 900101, 900201, 900203, '物理', '化学', '政治', 92.00, 'HIGH', '理工方向兼顾法学、公安等方向', 1, NOW(), NOW()),
  (901003, 'PHY_CHE_GEO', '物化地', 900101, 900201, 900204, '物理', '化学', '地理', 94.00, 'HIGH', '理工方向兼顾地理、环境、地质相关专业', 1, NOW(), NOW()),
  (901004, 'PHY_BIO_POL', '物生政', 900101, 900202, 900203, '物理', '生物', '政治', 86.00, 'MEDIUM', '适合部分理工、生物、公安和管理方向', 1, NOW(), NOW()),
  (901005, 'PHY_BIO_GEO', '物生地', 900101, 900202, 900204, '物理', '生物', '地理', 88.00, 'MEDIUM', '适合生态、地理信息、环境与部分理工方向', 1, NOW(), NOW()),
  (901006, 'PHY_POL_GEO', '物政地', 900101, 900203, 900204, '物理', '政治', '地理', 82.00, 'MEDIUM', '适合理工基础上兼顾公共管理、地理方向', 1, NOW(), NOW()),
  (901007, 'HIS_CHE_BIO', '史化生', 900102, 900201, 900202, '历史', '化学', '生物', 62.00, 'MEDIUM', '适合部分医学、护理、文史和教育方向', 1, NOW(), NOW()),
  (901008, 'HIS_CHE_POL', '史化政', 900102, 900201, 900203, '历史', '化学', '政治', 58.00, 'MEDIUM', '适合文史、法学、政治学及部分化学相关方向', 1, NOW(), NOW()),
  (901009, 'HIS_CHE_GEO', '史化地', 900102, 900201, 900204, '历史', '化学', '地理', 60.00, 'MEDIUM', '适合文史、地理、环境和部分化学方向', 1, NOW(), NOW()),
  (901010, 'HIS_BIO_POL', '史生政', 900102, 900202, 900203, '历史', '生物', '政治', 55.00, 'MEDIUM', '适合心理学、教育学、法学、护理等方向', 1, NOW(), NOW()),
  (901011, 'HIS_BIO_GEO', '史生地', 900102, 900202, 900204, '历史', '生物', '地理', 56.00, 'MEDIUM', '适合地理、生态、教育和文史方向', 1, NOW(), NOW()),
  (901012, 'HIS_POL_GEO', '史政地', 900102, 900203, 900204, '历史', '政治', '地理', 50.00, 'LOW', '传统文科组合，适合文史哲、法学、新闻传播等方向', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  combination_name = VALUES(combination_name),
  first_subject_id = VALUES(first_subject_id),
  second_subject_1_id = VALUES(second_subject_1_id),
  second_subject_2_id = VALUES(second_subject_2_id),
  first_subject_name = VALUES(first_subject_name),
  second_subject_1_name = VALUES(second_subject_1_name),
  second_subject_2_name = VALUES(second_subject_2_name),
  major_coverage_rate = VALUES(major_coverage_rate),
  recommended_level = VALUES(recommended_level),
  description = VALUES(description),
  status = VALUES(status),
  updated_at = NOW();

DROP PROCEDURE IF EXISTS cot_seed_all_tables;
DROP PROCEDURE IF EXISTS cot_seed_one_table;

/* =========================================================
   Explicit seed data for user 2075127851337654274.
   These rows make the main pages queryable by the real test user id.
   ========================================================= */

USE cot_identity;

INSERT INTO iam_user (id, username, password_hash, display_name, email, phone, avatar_url, introduction, user_type, status, register_channel, last_login_at, last_login_ip, password_changed_at)
VALUES
  (@cot_seed_user_id, 'cot_test_2075127851337654274', '$2a$10$9gF5N9pZ6tVgMZ4EwX6Kz.4xmpuU1TnY5pB7WkV9sGQ8VhQ0F9Q2K', 'COT Test User', 'cot_test_2075127851337654274@example.com', '13820754274', 'https://example.com/avatar/2075127851337654274.png', 'Seed user for local feature testing.', 1, 1, 'seed', NOW(), '127.0.0.1', NOW()),
  (@cot_seed_friend_id, 'cot_friend_2075127851337654275', '$2a$10$9gF5N9pZ6tVgMZ4EwX6Kz.4xmpuU1TnY5pB7WkV9sGQ8VhQ0F9Q2K', 'COT Friend User', 'cot_friend_2075127851337654275@example.com', '13820754275', 'https://example.com/avatar/2075127851337654275.png', 'Friend user for chat testing.', 1, 1, 'seed', NOW(), '127.0.0.1', NOW())
ON DUPLICATE KEY UPDATE username = VALUES(username), password_hash = VALUES(password_hash), display_name = VALUES(display_name), email = VALUES(email), phone = VALUES(phone), avatar_url = VALUES(avatar_url), introduction = VALUES(introduction), status = VALUES(status), updated_at = CURRENT_TIMESTAMP;

INSERT INTO iam_user_role (id, user_id, role_id, tenant_id)
VALUES
  (920000000000000001, @cot_seed_user_id, 1000000000000000003, 0),
  (920000000000000002, @cot_seed_user_id, 1000000000000000006, 0)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO iam_refresh_token (id, user_id, token_jti, device_id, device_name, client_ip, user_agent, expires_at)
VALUES (920000000000000003, @cot_seed_user_id, 'seed-token-2075127851337654274', 'seed-web', 'Local Browser', '127.0.0.1', 'seed-agent', DATE_ADD(NOW(), INTERVAL 30 DAY))
ON DUPLICATE KEY UPDATE expires_at = VALUES(expires_at);

INSERT INTO iam_login_audit (id, user_id, username, login_result, client_ip, user_agent, trace_id)
VALUES (920000000000000004, @cot_seed_user_id, 'cot_test_2075127851337654274', 1, '127.0.0.1', 'seed-agent', 'seed-trace-2075127851337654274')
ON DUPLICATE KEY UPDATE id = id;

USE cot_profile;

INSERT INTO user_profile (id, user_id, real_name, gender, birthday, province, city, school_name, university_name, major_id, major_name, education_stage, grade, class_name, career_direction, profile_visibility, extra_json)
VALUES (920000000000000101, @cot_seed_user_id, 'Test User', 1, '2008-06-01', 'Zhejiang', 'Hangzhou', 'Hangzhou No.1 High School', 'Zhejiang University', 920000000000000701, 'Computer Science', 'high_school', 'Senior 2', 'Class 1', 'AI Engineer', 3, JSON_OBJECT('source', 'seed'))
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), education_stage = VALUES(education_stage), grade = VALUES(grade), class_name = VALUES(class_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_stage (id, user_id, stage_code, stage_name, start_date, status, sort_order)
VALUES
  (920000000000000102, @cot_seed_user_id, 'high_school', 'High School', '2024-09-01', 1, 1),
  (920000000000000103, @cot_seed_user_id, 'university', 'University Plan', '2027-09-01', 1, 2),
  (920000000000000104, @cot_seed_user_id, 'workplace', 'Workplace Plan', '2031-07-01', 1, 3)
ON DUPLICATE KEY UPDATE stage_name = VALUES(stage_name), status = VALUES(status), updated_at = CURRENT_TIMESTAMP;

INSERT INTO resume (id, user_id, resume_name, real_name, gender, birth_date, phone, email, address, job_title, job_status, expected_salary_min, expected_salary_max, work_years, self_evaluation, is_public, status)
VALUES (920000000000000110, @cot_seed_user_id, 'Seed Resume', 'Test User', 'male', '2008-06-01', '13820754274', 'cot_test_2075127851337654274@example.com', 'Hangzhou', 'AI Intern', 'open', 8000, 12000, 0, 'Strong interest in AI and software engineering.', 1, 2)
ON DUPLICATE KEY UPDATE resume_name = VALUES(resume_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO resume_education (id, resume_id, school_name, degree, major, start_date, is_current, description, sort_order)
VALUES (920000000000000111, 920000000000000110, 'Hangzhou No.1 High School', 'High School', 'Science', '2024-09-01', 1, 'STEM track.', 1)
ON DUPLICATE KEY UPDATE school_name = VALUES(school_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO resume_work_experience (id, resume_id, company_name, position, start_date, is_current, description, achievements, sort_order)
VALUES (920000000000000112, 920000000000000110, 'COT Lab', 'Student Developer', '2026-01-01', 1, 'Built campus tools.', 'Completed a course-selection prototype.', 1)
ON DUPLICATE KEY UPDATE company_name = VALUES(company_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO resume_project (id, resume_id, project_name, project_role, start_date, description, responsibilities, tech_stack, project_url, sort_order)
VALUES (920000000000000113, 920000000000000110, 'Course Selection Assistant', 'Developer', '2026-03-01', 'A tool for subject and major planning.', 'Backend and data modeling.', 'Java, Spring Cloud, MySQL', 'https://example.com/projects/course-selection', 1)
ON DUPLICATE KEY UPDATE project_name = VALUES(project_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO resume_skill (id, resume_id, skill_name, skill_level, years_experience, sort_order)
VALUES (920000000000000114, 920000000000000110, 'Java', 'intermediate', 1, 1)
ON DUPLICATE KEY UPDATE skill_level = VALUES(skill_level), updated_at = CURRENT_TIMESTAMP;

INSERT INTO resume_certificate (id, resume_id, certificate_name, issue_authority, issue_date, score, description, sort_order)
VALUES (920000000000000115, 920000000000000110, 'Algorithm Contest Award', 'School', '2026-05-20', 'A', 'Seed certificate.', 1)
ON DUPLICATE KEY UPDATE certificate_name = VALUES(certificate_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO resume_social_experience (id, resume_id, experience_type, title, start_date, description, achievements, sort_order)
VALUES (920000000000000116, 920000000000000110, 'volunteer', 'Campus Tech Volunteer', '2026-04-01', 'Helped classmates with learning tools.', 'Organized a study session.', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = CURRENT_TIMESTAMP;

USE cot_content;

INSERT INTO content_category (id, parent_id, category_code, category_name, description, sort_order, status)
VALUES (920000000000000201, 0, 'seed_growth', 'Growth Notes', 'Seed content category.', 1, 1)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO content_article (id, user_id, category_id, title, summary, content_md, content_html, content_type, cover_image_url, location, weather, mood, visibility, status, publish_at)
VALUES (920000000000000202, @cot_seed_user_id, 920000000000000201, 'My first course-selection plan', 'Seed article for local testing.', '# Course Selection Plan\n\nPhysics, Chemistry and Biology.', '<p>Physics, Chemistry and Biology.</p>', 'journal', 'https://example.com/cover/course-selection.png', 'Hangzhou', 'Sunny', 'Focused', 2, 1, NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title), content_md = VALUES(content_md), updated_at = CURRENT_TIMESTAMP;

INSERT INTO growth_record (id, user_id, stage, semester, record_date, exam_name, exam_rank, exam_score, best_subject, weakest_subject, study_hours, study_notes, competition_name, competition_award, activity_name, activity_role, interest_tested, skill_learned, stress_level, happiness_level, career_interest, dream_college, dream_major, company_name, job_title, job_content, work_skills, work_achievements, work_challenges, career_plan, sleep_hours, exercise_minutes, screen_time_hours, family_communication_quality, is_milestone, milestone_name, achievement_this_period, improvement_needed, next_goal)
VALUES (920000000000000210, @cot_seed_user_id, 'high_school', '2026 Spring', CURDATE(), 'Mock Exam', 'Top 10%', 628.50, 'Physics', 'English', 4.50, 'Focused on mechanics and chemistry.', 'Algorithm Contest', 'School First Prize', 'Tech Club', 'Leader', 'Engineering, AI', 'Java backend basics', 3, 8, 'AI engineer', 'Zhejiang University', 'Computer Science', 'COT Lab', 'Student Developer', 'Built seed features.', 'Java, MySQL, Spring Cloud', 'Completed data initialization.', 'Need more frontend practice.', 'Prepare for computer science major.', 7.50, 45, 2.00, 8, 1, 'Confirmed subject combination', 'Finished the first selection plan.', 'Improve English.', 'Keep physics and chemistry scores stable.')
ON DUPLICATE KEY UPDATE exam_score = VALUES(exam_score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_activity_stats (id, user_id, total_login_days, continuous_login_days, max_continuous_login_days, total_online_seconds, today_online_seconds, last_checkin_date, online_date, last_seen_at, medal_score)
VALUES (920000000000000211, @cot_seed_user_id, 12, 5, 7, 46800, 3600, CURDATE(), CURDATE(), NOW(), 560)
ON DUPLICATE KEY UPDATE total_login_days = VALUES(total_login_days), updated_at = CURRENT_TIMESTAMP;

INSERT INTO chat_friend (id, user_id, friend_id, status)
VALUES (920000000000000213, @cot_seed_user_id, @cot_seed_friend_id, 'ACTIVE'), (920000000000000214, @cot_seed_friend_id, @cot_seed_user_id, 'ACTIVE')
ON DUPLICATE KEY UPDATE status = VALUES(status), updated_at = CURRENT_TIMESTAMP;

INSERT INTO chat_group (id, group_no, name, announcement, owner_id, member_count, searchable)
VALUES (920000000000000215, 'G20754274', 'Seed Study Group', 'Local test group.', @cot_seed_user_id, 2, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), member_count = VALUES(member_count), updated_at = CURRENT_TIMESTAMP;

INSERT INTO chat_group_member (id, group_id, user_id, role, status, last_read_at)
VALUES (920000000000000216, 920000000000000215, @cot_seed_user_id, 'OWNER', 'ACTIVE', NOW()), (920000000000000217, 920000000000000215, @cot_seed_friend_id, 'MEMBER', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE role = VALUES(role), status = VALUES(status);

INSERT INTO chat_message (id, conversation_type, group_id, sender_id, receiver_id, content_type, content)
VALUES (920000000000000218, 'GROUP', 920000000000000215, @cot_seed_user_id, NULL, 'TEXT', 'Seed group message.'), (920000000000000219, 'PRIVATE', NULL, @cot_seed_user_id, @cot_seed_friend_id, 'TEXT', 'Seed private message.')
ON DUPLICATE KEY UPDATE content = VALUES(content);

INSERT INTO chat_message_read (id, message_id, user_id, read_at)
VALUES (920000000000000220, 920000000000000218, @cot_seed_user_id, NOW())
ON DUPLICATE KEY UPDATE read_at = VALUES(read_at);

USE cot_learning;

INSERT INTO learning_subject (id, subject_code, subject_name, category_level, parent_id, sort_order, status)
VALUES (920000000000000301, 'HS_PHY', 'Physics', 'high_school', 0, 1, 1)
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO knowledge_point (id, subject_id, parent_id, point_code, point_name, description, difficulty_level, sort_order, status)
VALUES
  (920000000000000302, 920000000000000301, 0, 'PHY_FORCE', 'Force analysis', 'Seed knowledge point.', 'medium', 1, 1),
  (920000000000000303, 920000000000000301, 920000000000000302, 'PHY_NEWTON', 'Newton laws', 'Seed child knowledge point.', 'medium', 2, 1)
ON DUPLICATE KEY UPDATE point_name = VALUES(point_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO knowledge_edge (id, subject_id, source_point_id, target_point_id, relation_type, weight)
VALUES (920000000000000304, 920000000000000301, 920000000000000302, 920000000000000303, 'prerequisite', 1.0000)
ON DUPLICATE KEY UPDATE weight = VALUES(weight);

INSERT INTO question (id, subject_id, knowledge_point_id, subject_name, question_type, category_level, knowledge_point, question_title, options, correct_answer, answer_analysis, difficulty_level, score_value, source_type, source_name, status, created_by, audit_status, audited_by, audited_at)
VALUES (920000000000000305, 920000000000000301, 920000000000000303, 'Physics', 'single', 'high_school', 'Newton laws', 'Which law explains action and reaction?', JSON_ARRAY(JSON_OBJECT('key','A','text','Newton third law'), JSON_OBJECT('key','B','text','Ohm law')), 'A', 'Action and reaction are described by Newton third law.', 'easy', 5, 'seed', 'Seed Question Bank', 1, @cot_seed_user_id, 'approved', @cot_seed_user_id, NOW())
ON DUPLICATE KEY UPDATE question_title = VALUES(question_title), updated_at = CURRENT_TIMESTAMP;

INSERT INTO question_option (id, question_id, option_key, option_text, is_correct, sort_order)
VALUES (920000000000000306, 920000000000000305, 'A', 'Newton third law', 1, 1), (920000000000000307, 920000000000000305, 'B', 'Ohm law', 0, 2)
ON DUPLICATE KEY UPDATE option_text = VALUES(option_text);

INSERT INTO practice_session (id, user_id, session_type, subject_id, title, category_level, subject_name, knowledge_points, difficulty_level, question_ids, total_questions, answered_questions, correct_count, wrong_count, score_total, score_obtained, duration_seconds, anti_cheat_enabled, suspicious_count, finished_at, status)
VALUES (920000000000000308, @cot_seed_user_id, 'practice', 920000000000000301, 'Physics seed practice', 'high_school', 'Physics', 'Newton laws', 'easy', '920000000000000305', 1, 1, 1, 0, 5, 5, 120, 0, 0, NOW(), 2)
ON DUPLICATE KEY UPDATE score_obtained = VALUES(score_obtained), updated_at = CURRENT_TIMESTAMP;

INSERT INTO answer_record (id, user_id, session_id, question_id, subject_id, knowledge_point_id, subject_name, question_type, category_level, knowledge_point, user_answer, correct_answer, is_correct, score, answer_time_seconds, mistake_added, exam_session)
VALUES (920000000000000309, @cot_seed_user_id, 920000000000000308, 920000000000000305, 920000000000000301, 920000000000000303, 'Physics', 'single', 'high_school', 'Newton laws', 'A', 'A', 1, 5, 60, 0, 'seed-practice')
ON DUPLICATE KEY UPDATE score = VALUES(score);

INSERT INTO mistake_record (id, user_id, question_id, subject_id, knowledge_point_id, last_answer_record_id, subject_name, mistake_name, mistake_type, question_options, student_choice, wrong_answer, correct_answer, answer_analysis, knowledge_point, mistake_reason, correction_notes, mistake_count, review_count, mastered, mistake_date, last_review_date, next_review_date)
VALUES (920000000000000310, @cot_seed_user_id, 920000000000000305, 920000000000000301, 920000000000000303, 920000000000000309, 'Physics', 'Newton laws concept', 'concept', JSON_ARRAY(JSON_OBJECT('key','A','text','Newton third law')), 'B', 'B', 'A', 'Review action and reaction.', 'Newton laws', 'Initial misunderstanding.', 'Reviewed once.', 1, 1, 1, CURDATE(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY))
ON DUPLICATE KEY UPDATE correction_notes = VALUES(correction_notes), updated_at = CURRENT_TIMESTAMP;

INSERT INTO score_record (id, user_id, subject_id, subject_name, exam_name, exam_type, exam_date, score, full_score, class_rank, grade_rank, notes)
VALUES (920000000000000311, @cot_seed_user_id, 920000000000000301, 'Physics', 'Seed Mock Exam', 'mock', CURDATE(), 92.00, 100.00, 3, 21, 'Seed score.')
ON DUPLICATE KEY UPDATE score = VALUES(score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO knowledge_mastery_stat (id, user_id, subject_id, knowledge_point_id, answer_count, correct_count, mistake_count, mastery_score, last_practice_at)
VALUES (920000000000000312, @cot_seed_user_id, 920000000000000301, 920000000000000303, 10, 8, 2, 82.50, NOW())
ON DUPLICATE KEY UPDATE mastery_score = VALUES(mastery_score), updated_at = CURRENT_TIMESTAMP;

USE cot_highschool;

INSERT INTO hs_subject (id, subject_code, subject_name, subject_type, description, sort_order, status)
VALUES
  (900001, 'CHN', 'Chinese', 3, 'Required gaokao subject.', 1, 1),
  (900002, 'MATH', 'Math', 3, 'Required gaokao subject.', 2, 1),
  (900003, 'ENG', 'English', 3, 'Required gaokao subject.', 3, 1),
  (900101, 'PHY', 'Physics', 1, 'First-choice subject.', 1, 1),
  (900102, 'HIS', 'History', 1, 'First-choice subject.', 2, 1),
  (900201, 'CHE', 'Chemistry', 2, 'Second-choice subject.', 1, 1),
  (900202, 'BIO', 'Biology', 2, 'Second-choice subject.', 2, 1),
  (900203, 'POL', 'Politics', 2, 'Second-choice subject.', 3, 1),
  (900204, 'GEO', 'Geography', 2, 'Second-choice subject.', 4, 1)
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), subject_type = VALUES(subject_type), status = VALUES(status), updated_at = CURRENT_TIMESTAMP;

INSERT INTO hs_subject_combination (id, combination_code, combination_name, first_subject_id, second_subject_1_id, second_subject_2_id, first_subject_name, second_subject_1_name, second_subject_2_name, major_coverage_rate, recommended_level, description, status)
VALUES
  (901001, 'PHY_CHE_BIO', 'Physics Chemistry Biology', 900101, 900201, 900202, 'Physics', 'Chemistry', 'Biology', 96.00, 'HIGH', 'Wide coverage for engineering and medicine.', 1),
  (901002, 'PHY_CHE_GEO', 'Physics Chemistry Geography', 900101, 900201, 900204, 'Physics', 'Chemistry', 'Geography', 94.00, 'HIGH', 'Wide coverage for engineering and earth science.', 1),
  (901003, 'HIS_POL_GEO', 'History Politics Geography', 900102, 900203, 900204, 'History', 'Politics', 'Geography', 50.00, 'LOW', 'Traditional humanities combination.', 1)
ON DUPLICATE KEY UPDATE combination_name = VALUES(combination_name), major_coverage_rate = VALUES(major_coverage_rate), updated_at = CURRENT_TIMESTAMP;

INSERT INTO hs_student_selection (id, student_id, student_name, grade, class_name, academic_year, semester, chinese_score, math_score, english_score, first_subject_id, first_subject_name, first_subject_score, first_subject_level, second_subject_1_id, second_subject_1_name, second_subject_1_score, second_subject_1_level, second_subject_2_id, second_subject_2_name, second_subject_2_score, second_subject_2_level, combination_id, combination_name, total_score, total_score_weighted, class_rank, grade_rank, status, is_confirmed, confirm_time, selection_reason, future_plan, teacher_advice, parent_opinion, is_public, remark)
VALUES (920000000000000401, @cot_seed_user_id, 'Test User', 'Senior 2', 'Class 1', '2025-2026', 'Spring', 118.00, 132.00, 125.00, 900101, 'Physics', 92.00, 'A', 900201, 'Chemistry', 88.00, 'A', 900202, 'Biology', 86.00, 'B', 901001, 'Physics Chemistry Biology', 641.00, 641.00, 3, 21, 2, 1, NOW(), 'Matches computer science and engineering goals.', 'Apply for computer science major.', 'Keep Physics and Math advantages.', 'Support this plan.', 1, 'Seed confirmed selection.')
ON DUPLICATE KEY UPDATE combination_id = VALUES(combination_id), status = VALUES(status), is_confirmed = VALUES(is_confirmed), updated_at = CURRENT_TIMESTAMP;

INSERT INTO hs_selection_intention (id, student_id, student_name, grade, class_name, first_subject_intention, second_subject_intention_1, second_subject_intention_2, second_subject_backup_1, second_subject_backup_2, target_major, target_university, reason, strength_subjects, weak_subjects, career_interest, teacher_feedback, parent_feedback, status, submit_time, evaluate_time, evaluate_by)
VALUES (920000000000000402, @cot_seed_user_id, 'Test User', 'Senior 2', 'Class 1', 'Physics', 'Chemistry', 'Biology', 'Geography', 'Politics', 'Computer Science', 'Zhejiang University', 'Interested in AI and software engineering.', 'Math, Physics', 'English', 'AI engineer', 'Recommended.', 'Agreed.', 2, NOW(), NOW(), 'Seed Teacher')
ON DUPLICATE KEY UPDATE target_major = VALUES(target_major), updated_at = CURRENT_TIMESTAMP;

INSERT INTO hs_selection_history (id, selection_id, student_id, student_name, change_type, old_first_subject, new_first_subject, old_second_subject_1, new_second_subject_1, old_second_subject_2, new_second_subject_2, change_reason, approver, approve_status, approve_comment)
VALUES (920000000000000403, 920000000000000401, @cot_seed_user_id, 'Test User', 'CONFIRM', NULL, 'Physics', NULL, 'Chemistry', NULL, 'Biology', 'Initial confirmation.', 'Seed Teacher', 1, 'Approved.')
ON DUPLICATE KEY UPDATE approve_status = VALUES(approve_status);

INSERT INTO hs_grading_scale (id, province, admission_year, subject_id, subject_name, original_score_min, original_score_max, grade_level, assigned_score_min, assigned_score_max, percentile_min, percentile_max, percentage_top, percentage_bottom, raw_score_min, raw_score_max, academic_year, is_active, status)
VALUES (920000000000000404, 'Zhejiang', 2026, 900101, 'Physics', 90.00, 100.00, 'A', 90.00, 100.00, 0.0000, 0.1500, 0.0000, 0.1500, 90.00, 100.00, '2025-2026', 1, 1)
ON DUPLICATE KEY UPDATE assigned_score_max = VALUES(assigned_score_max), updated_at = CURRENT_TIMESTAMP;

INSERT INTO hs_course_guidance (id, student_id, student_name, guidance_date, guidance_type, title, content, recommended_combination_id, recommended_combination_name, suggested_major, strength_analysis, weakness_analysis, opportunity_analysis, threat_analysis, action_plan, risk_analysis, advisor_id, advisor_name, advisor_position, student_feedback, parent_feedback, follow_up_date, status)
VALUES (920000000000000405, @cot_seed_user_id, 'Test User', CURDATE(), 'teacher', 'Physics Chemistry Biology guidance', 'This combination fits computer science and engineering majors.', 901001, 'Physics Chemistry Biology', 'Computer Science', 'Strong math and physics base.', 'English needs steady practice.', 'High major coverage.', 'Competition is strong.', 'Keep weekly physics and chemistry review.', 'Stable plan with high coverage.', @cot_seed_user_id, 'Seed Advisor', 'Course Planner', 'Accepted.', 'Accepted.', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1)
ON DUPLICATE KEY UPDATE content = VALUES(content), updated_at = CURRENT_TIMESTAMP;

INSERT INTO gaokao_university (id, university_code, university_name, province, city, level_tags, type_tags, website, logo_url, description, founded_year, ownership, is_public, status)
VALUES (920000000000000406, 'ZJU', 'Zhejiang University', 'Zhejiang', 'Hangzhou', '985,211,double-first-class', 'comprehensive', 'https://www.zju.edu.cn', 'https://example.com/logo/zju.png', 'Seed university.', 1897, 'public', 1, 1)
ON DUPLICATE KEY UPDATE university_name = VALUES(university_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO gaokao_major (id, major_code, major_name, category, sub_category, duration_years, degree_type, tuition_fee, description, status)
VALUES (920000000000000407, '080901', 'Computer Science and Technology', 'Engineering', 'Computer', 4, 'Bachelor of Engineering', 6000.00, 'Seed major.', 1)
ON DUPLICATE KEY UPDATE major_name = VALUES(major_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO gaokao_admission_plan (id, university_id, university_name, major_id, admission_year, province, student_type, batch_name, planned_num, actual_num, min_score, min_rank, avg_score, max_score, subject_requirement)
VALUES (920000000000000408, 920000000000000406, 'Zhejiang University', 920000000000000407, 2026, 'Zhejiang', 'general', 'Regular', 30, 30, 650, 3000, 662.50, 680, 'Physics required')
ON DUPLICATE KEY UPDATE min_score = VALUES(min_score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO gaokao_major_requirement (id, university_id, major_id, major_code, major_name, category, first_subject_required, second_subject_required, requirement_detail, university_name, university_level, province, admission_year)
VALUES (920000000000000409, 920000000000000406, 920000000000000407, '080901', 'Computer Science and Technology', 'Engineering', 'Physics', 'Chemistry recommended', 'Physics is required for this major.', 'Zhejiang University', '985,211', 'Zhejiang', 2026)
ON DUPLICATE KEY UPDATE requirement_detail = VALUES(requirement_detail), updated_at = CURRENT_TIMESTAMP;

INSERT INTO hs_major_subject_match (id, student_id, combination_id, major_requirement_id, university_id, major_id, major_code, major_name, subject_id, subject_name, importance_level, matched_subjects, missing_subjects, matching_score, match_level, description, explanation)
VALUES (920000000000000410, @cot_seed_user_id, 901001, 920000000000000409, 920000000000000406, 920000000000000407, '080901', 'Computer Science and Technology', 900101, 'Physics', 5, 'Physics,Chemistry', '', 96.00, 'full', 'Seed matching record.', 'The selected combination satisfies the requirement.')
ON DUPLICATE KEY UPDATE matching_score = VALUES(matching_score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_volunteer_plan (id, user_id, plan_name, admission_year, province, score, rank_no, student_type, preference_order, selected_subjects, is_final, submit_time)
VALUES (920000000000000411, @cot_seed_user_id, '2026 Zhejiang CS Volunteer Plan', 2026, 'Zhejiang', 641, 5200, 'general', 1, 'Physics,Chemistry,Biology', 1, NOW())
ON DUPLICATE KEY UPDATE score = VALUES(score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_volunteer_detail (id, volunteer_plan_id, priority_no, university_id, major_id, is_major_adjusted, matching_check, matching_score, risk_level)
VALUES (920000000000000412, 920000000000000411, 1, 920000000000000406, 920000000000000407, 1, 1, 96, 'stable')
ON DUPLICATE KEY UPDATE matching_score = VALUES(matching_score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO admission_simulation (id, volunteer_detail_id, simulation_status, reject_reason, predicted_probability, admitted_time, score_diff, rank_diff, model_version)
VALUES (920000000000000413, 920000000000000412, 'passed', NULL, 0.8200, NOW(), 8, -300, 'seed-v1')
ON DUPLICATE KEY UPDATE predicted_probability = VALUES(predicted_probability), updated_at = CURRENT_TIMESTAMP;

USE cot_university;

INSERT INTO uni_major (id, major_code, major_name, degree_type, total_credits, compulsory_credits, elective_credits, description, status)
VALUES (920000000000000701, 'CS-SEED', 'Computer Science', 'Bachelor', 160.00, 120.00, 40.00, 'Seed university major.', 1)
ON DUPLICATE KEY UPDATE major_name = VALUES(major_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO uni_course_category (id, user_id, major_id, parent_id, category_name, sort_order, icon, color)
VALUES (920000000000000702, @cot_seed_user_id, 920000000000000701, 0, 'Core Courses', 1, 'BookOpen', '#2563eb')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO uni_course (id, user_id, major_id, category_id, course_code, course_name, credit, total_hours, theory_hours, lab_hours, course_type, term_no, exam_type, description, prerequisite_text, sort_order, status)
VALUES
  (920000000000000703, @cot_seed_user_id, 920000000000000701, 920000000000000702, 'CS101', 'Programming Fundamentals', 4.00, 64, 40, 24, 'compulsory', 1, 'exam', 'Seed course.', 'None', 1, 1),
  (920000000000000704, @cot_seed_user_id, 920000000000000701, 920000000000000702, 'CS102', 'Data Structures', 4.00, 64, 40, 24, 'compulsory', 2, 'exam', 'Seed prerequisite course.', 'CS101', 2, 1)
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name), updated_at = CURRENT_TIMESTAMP;

INSERT INTO uni_course_prerequisite (id, course_id, prerequisite_course_id, relation_type)
VALUES (920000000000000705, 920000000000000704, 920000000000000703, 'must_before')
ON DUPLICATE KEY UPDATE relation_type = VALUES(relation_type);

INSERT INTO uni_student_course (id, user_id, course_id, major_id, semester, score, grade_point, is_passed, is_retake, retake_count, status, notes)
VALUES (920000000000000706, @cot_seed_user_id, 920000000000000703, 920000000000000701, '2026 Fall', 92.00, 4.00, 1, 0, 0, 'passed', 'Seed course progress.')
ON DUPLICATE KEY UPDATE score = VALUES(score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO uni_graduation_requirement (id, user_id, major_id, total_credits_required, total_credits_earned, compulsory_credits_required, compulsory_credits_earned, elective_credits_required, elective_credits_earned, gpa, progress_percent, expected_graduation_date, status)
VALUES (920000000000000707, @cot_seed_user_id, 920000000000000701, 160.00, 24.00, 120.00, 20.00, 40.00, 4.00, 3.80, 15.00, '2031-06-30', 'in_progress')
ON DUPLICATE KEY UPDATE progress_percent = VALUES(progress_percent), updated_at = CURRENT_TIMESTAMP;

INSERT INTO thesis_paper (id, user_id, title, supervisor, research_direction, abstract_text, keywords, content_md, stage, status, version_no)
VALUES (920000000000000708, @cot_seed_user_id, 'Learning Path Recommendation Based on Course Selection', 'Seed Supervisor', 'AI Education', 'Seed abstract.', 'AI,education', '# Seed Paper', 'proposal', 'draft', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = CURRENT_TIMESTAMP;

INSERT INTO thesis_paper_version (id, paper_id, version_no, title, content_md, change_summary, created_by)
VALUES (920000000000000709, 920000000000000708, 1, 'Learning Path Recommendation Based on Course Selection', '# Seed Paper', 'Initial seed version.', @cot_seed_user_id)
ON DUPLICATE KEY UPDATE change_summary = VALUES(change_summary);

INSERT INTO thesis_suggestion (id, paper_id, suggester_id, suggestion_type, suggestion_content, position_json, status)
VALUES (920000000000000710, 920000000000000708, @cot_seed_user_id, 'text', 'Add more evaluation metrics.', JSON_OBJECT('section', 'method'), 1)
ON DUPLICATE KEY UPDATE suggestion_content = VALUES(suggestion_content), updated_at = CURRENT_TIMESTAMP;

USE cot_platform;

INSERT INTO file_asset (id, owner_user_id, biz_type, biz_id, original_name, storage_name, bucket_name, object_key, file_url, thumbnail_url, mime_type, file_ext, file_size, file_sha256, width, height, status)
VALUES (920000000000000901, @cot_seed_user_id, 'article', 920000000000000202, 'course-selection.png', 'course-selection-2075127851337654274.png', 'local', 'seed/course-selection.png', 'https://example.com/files/course-selection.png', 'https://example.com/files/course-selection-thumb.png', 'image/png', 'png', 102400, '1111111111111111111111111111111111111111111111111111111111112075', 1200, 800, 1)
ON DUPLICATE KEY UPDATE file_url = VALUES(file_url), updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_notification (id, user_id, notice_type, title, content, biz_type, biz_id, read_status, read_at)
VALUES (920000000000000902, @cot_seed_user_id, 'COURSE_SELECTION', 'Course selection confirmed', 'Your seed course selection has been confirmed.', 'hs_student_selection', 920000000000000401, 0, NULL)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO admin_operation_log (id, admin_user_id, module_name, operation_type, operation_desc, request_method, request_uri, request_params, response_code, client_ip, user_agent, trace_id, cost_ms)
VALUES (920000000000000903, @cot_seed_user_id, 'seed', 'INIT_DATA', 'Seed user data initialized.', 'POST', '/seed/test-data', JSON_OBJECT('userId', @cot_seed_user_id), 200, '127.0.0.1', 'seed-agent', 'seed-admin-log', 12)
ON DUPLICATE KEY UPDATE operation_desc = VALUES(operation_desc);

INSERT INTO sys_api_access_log (id, user_id, service_name, request_method, request_uri, response_code, cost_ms, client_ip, trace_id)
VALUES (920000000000000904, @cot_seed_user_id, 'gateway', 'GET', '/api/high/selection/current', 200, 18, '127.0.0.1', 'seed-api-log')
ON DUPLICATE KEY UPDATE response_code = VALUES(response_code);

USE cot_workplace;

INSERT INTO career_profile (id, user_id, current_title, target_title, industry, city, years_of_experience, strengths, weakness, career_values, salary_expectation, visibility)
VALUES (920000000000001001, @cot_seed_user_id, 'Student', 'AI Engineer', 'Internet', 'Hangzhou', 0, 'Math, physics, backend development', 'Needs more English practice', 'Growth, impact, learning', 'Negotiable', 'PUBLIC')
ON DUPLICATE KEY UPDATE target_title = VALUES(target_title), updated_at = CURRENT_TIMESTAMP;

INSERT INTO career_goal (id, user_id, goal_name, goal_type, priority, status, progress, start_date, target_date, metric, notes)
VALUES (920000000000001002, @cot_seed_user_id, 'Become AI engineer', 'career', 'HIGH', 'ACTIVE', 25, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR), 'Build 3 projects', 'Seed career goal.')
ON DUPLICATE KEY UPDATE progress = VALUES(progress), updated_at = CURRENT_TIMESTAMP;

INSERT INTO career_task (id, user_id, goal_id, task_name, task_type, status, priority, due_date, estimated_minutes, actual_minutes, outcome, notes)
VALUES (920000000000001003, @cot_seed_user_id, 920000000000001002, 'Finish Spring Cloud project', 'project', 'DOING', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1200, 360, 'In progress', 'Seed task.')
ON DUPLICATE KEY UPDATE status = VALUES(status), updated_at = CURRENT_TIMESTAMP;

INSERT INTO interview_prep (id, user_id, company_name, position_name, interview_round, interview_date, status, question_bank, preparation_notes, feedback, result, confidence_score)
VALUES (920000000000001004, @cot_seed_user_id, 'COT Lab', 'AI Intern', 'Mock', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'PREPARING', 'Java, SQL, algorithms', 'Review project architecture.', 'Need clearer API explanation.', NULL, 75)
ON DUPLICATE KEY UPDATE confidence_score = VALUES(confidence_score), updated_at = CURRENT_TIMESTAMP;

INSERT INTO work_review (id, user_id, review_date, review_type, wins, problems, learnings, next_actions, energy_level, communication_score, delivery_score)
VALUES (920000000000001005, @cot_seed_user_id, CURDATE(), 'WEEKLY', 'Completed seed data design.', 'Need more test coverage.', 'SQL idempotency.', 'Run DB scripts locally.', 8, 7, 8)
ON DUPLICATE KEY UPDATE wins = VALUES(wins), updated_at = CURRENT_TIMESTAMP;

USE cot_advanced;

INSERT INTO advancement_roadmap (id, user_id, roadmap_name, stage, target_role, status, progress, start_date, target_date, core_skills, success_metrics, risk_notes)
VALUES (920000000000001101, @cot_seed_user_id, 'AI Engineer Roadmap', 'foundation', 'AI Engineer', 'ACTIVE', 20, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 YEAR), 'Java, algorithms, ML, systems', 'Projects and internship offers', 'Avoid shallow learning.')
ON DUPLICATE KEY UPDATE progress = VALUES(progress), updated_at = CURRENT_TIMESTAMP;

INSERT INTO advancement_milestone (id, user_id, roadmap_id, milestone_name, milestone_type, status, weight, due_date, completed_date, evidence_url, notes)
VALUES (920000000000001102, @cot_seed_user_id, 920000000000001101, 'Complete backend foundation', 'skill', 'DOING', 3, DATE_ADD(CURDATE(), INTERVAL 90 DAY), NULL, 'https://example.com/evidence/backend', 'Seed milestone.')
ON DUPLICATE KEY UPDATE status = VALUES(status), updated_at = CURRENT_TIMESTAMP;

INSERT INTO skill_progress (id, user_id, roadmap_id, skill_name, skill_category, current_level, target_level, progress, practice_hours, last_practiced_at, evidence, next_practice)
VALUES (920000000000001103, @cot_seed_user_id, 920000000000001101, 'Spring Cloud', 'backend', 'basic', 'advanced', 35, 48, CURDATE(), 'Built microservice APIs.', 'Add integration tests.')
ON DUPLICATE KEY UPDATE progress = VALUES(progress), updated_at = CURRENT_TIMESTAMP;

INSERT INTO mentor_session (id, user_id, roadmap_id, mentor_name, session_type, session_date, topic, advice, action_items, value_score)
VALUES (920000000000001104, @cot_seed_user_id, 920000000000001101, 'Seed Mentor', 'monthly', CURDATE(), 'Course and career planning', 'Keep project notes and review weekly.', 'Finish course-selection data validation.', 9)
ON DUPLICATE KEY UPDATE advice = VALUES(advice), updated_at = CURRENT_TIMESTAMP;

