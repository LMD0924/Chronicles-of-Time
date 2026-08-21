-- Chronicles of Time database schema.
-- Creates databases, tables, indexes, and base schema objects.
-- Run this first.

-- 文件说明：拾光记微服务后端数据库脚本，用于初始化表结构、索引和基础业务数据。
/*
 Chronicles of Time enterprise database schema
 Target: MySQL 8.0.19+

 Design notes:
 - Databases are split by service and business boundary.
 - Cross-service relations use logical IDs instead of physical foreign keys.
 - Core tables use BIGINT UNSIGNED IDs and consistent audit columns.
 - High-growth tables keep shard-friendly indexes on user_id and time.
*/

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE DATABASE IF NOT EXISTS cot_identity DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_profile DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_content DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_learning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_highschool DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_university DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_workplace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS cot_advanced DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

/* =========================================================
   cot_identity: auth-center, gateway, admin identity
   ========================================================= */

USE cot_identity;

CREATE TABLE IF NOT EXISTS iam_user (
  id BIGINT UNSIGNED NOT NULL COMMENT 'Snowflake user id',
  username VARCHAR(64) NOT NULL COMMENT 'login account',
  password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt or Argon2 hash',
  display_name VARCHAR(80) NOT NULL DEFAULT '',
  email VARCHAR(128) DEFAULT NULL,
  phone VARCHAR(32) DEFAULT NULL,
  avatar_url VARCHAR(512) DEFAULT NULL,
  introduction VARCHAR(512) DEFAULT NULL,
  user_type TINYINT NOT NULL DEFAULT 1 COMMENT '1 user, 2 admin',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled, 2 locked, 3 cancelled',
  register_channel VARCHAR(32) NOT NULL DEFAULT 'web',
  last_login_at DATETIME DEFAULT NULL,
  last_login_ip VARCHAR(64) DEFAULT NULL,
  password_changed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_iam_user_username (username),
  UNIQUE KEY uk_iam_user_email (email),
  UNIQUE KEY uk_iam_user_phone (phone),
  KEY idx_iam_user_status_type (status, user_type),
  KEY idx_iam_user_created_at (created_at)
) ENGINE=InnoDB COMMENT='Unified account table';

CREATE TABLE IF NOT EXISTS iam_role (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(80) NOT NULL,
  role_scope TINYINT NOT NULL DEFAULT 1 COMMENT '1 platform, 2 tenant/school, 3 user',
  description VARCHAR(255) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_iam_role_user_id (user_id),
  UNIQUE KEY uk_iam_role_code (role_code)

) ENGINE=InnoDB COMMENT='RBAC role';

CREATE TABLE IF NOT EXISTS iam_permission (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  permission_code VARCHAR(128) NOT NULL,
  permission_name VARCHAR(128) NOT NULL,
  permission_type TINYINT NOT NULL COMMENT '1 menu, 2 button, 3 api',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  route_path VARCHAR(255) DEFAULT NULL,
  api_method VARCHAR(16) DEFAULT NULL,
  api_path VARCHAR(255) DEFAULT NULL,
  icon VARCHAR(80) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_iam_permission_user_id (user_id),
  UNIQUE KEY uk_iam_permission_code (permission_code),
  KEY idx_iam_permission_parent (parent_id, sort_order)

) ENGINE=InnoDB COMMENT='Menu, button and API permission';

CREATE TABLE IF NOT EXISTS iam_user_role (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  tenant_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'reserved for school or tenant isolation',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_iam_user_role (user_id, role_id, tenant_id),
  KEY idx_iam_user_role_role (role_id)
) ENGINE=InnoDB COMMENT='User-role relation';

CREATE TABLE IF NOT EXISTS iam_role_permission (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  role_id BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_iam_role_permission_user_id (user_id),
  UNIQUE KEY uk_iam_role_permission (role_id, permission_id),
  KEY idx_iam_role_permission_perm (permission_id)

) ENGINE=InnoDB COMMENT='Role-permission relation';

CREATE TABLE IF NOT EXISTS iam_refresh_token (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  token_jti VARCHAR(96) NOT NULL,
  device_id VARCHAR(96) DEFAULT NULL,
  device_name VARCHAR(128) DEFAULT NULL,
  client_ip VARCHAR(64) DEFAULT NULL,
  user_agent VARCHAR(512) DEFAULT NULL,
  expires_at DATETIME NOT NULL,
  revoked_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_iam_refresh_token_jti (token_jti),
  KEY idx_iam_refresh_user_expires (user_id, expires_at)
) ENGINE=InnoDB COMMENT='Refresh token and login device';

CREATE TABLE IF NOT EXISTS iam_login_audit (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED DEFAULT NULL,
  username VARCHAR(64) NOT NULL DEFAULT '',
  login_result TINYINT NOT NULL COMMENT '0 failed, 1 success',
  fail_reason VARCHAR(255) DEFAULT NULL,
  client_ip VARCHAR(64) DEFAULT NULL,
  user_agent VARCHAR(512) DEFAULT NULL,
  trace_id VARCHAR(64) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_iam_login_user_time (user_id, created_at),
  KEY idx_iam_login_username_time (username, created_at),
  KEY idx_iam_login_trace (trace_id)
) ENGINE=InnoDB COMMENT='Login audit log';

/* =========================================================
   cot_profile: user-center profile and resume
   ========================================================= */

USE cot_profile;

CREATE TABLE IF NOT EXISTS user_profile (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  real_name VARCHAR(80) DEFAULT NULL,
  gender TINYINT DEFAULT NULL COMMENT '0 unknown, 1 male, 2 female',
  birthday DATE DEFAULT NULL,
  province VARCHAR(64) DEFAULT NULL,
  city VARCHAR(64) DEFAULT NULL,
  school_name VARCHAR(128) DEFAULT NULL,
  university_name VARCHAR(128) DEFAULT NULL,
  major_id BIGINT UNSIGNED DEFAULT NULL,
  major_name VARCHAR(128) DEFAULT NULL,
  education_stage VARCHAR(32) DEFAULT NULL COMMENT 'high_school, university, workplace',
  grade VARCHAR(32) DEFAULT NULL,
  class_name VARCHAR(64) DEFAULT NULL,
  career_direction VARCHAR(128) DEFAULT NULL,
  profile_visibility TINYINT NOT NULL DEFAULT 1 COMMENT '1 private, 2 friends, 3 public',
  extra_json JSON DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_profile_user (user_id),
  KEY idx_user_profile_school (school_name, grade, class_name),
  KEY idx_user_profile_major (major_id)
) ENGINE=InnoDB COMMENT='Extended user profile';

CREATE TABLE IF NOT EXISTS user_stage (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  stage_code VARCHAR(32) NOT NULL COMMENT 'high_school, university, workplace',
  stage_name VARCHAR(64) NOT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 archived, 1 active',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_stage (user_id, stage_code, start_date),
  KEY idx_user_stage_user_status (user_id, status)
) ENGINE=InnoDB COMMENT='User life and education stage';

CREATE TABLE IF NOT EXISTS resume (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  resume_name VARCHAR(128) NOT NULL DEFAULT 'Default resume',
  real_name VARCHAR(80) DEFAULT NULL,
  gender VARCHAR(16) DEFAULT NULL,
  birth_date DATE DEFAULT NULL,
  phone VARCHAR(32) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  address VARCHAR(255) DEFAULT NULL,
  avatar_url VARCHAR(512) DEFAULT NULL,
  job_title VARCHAR(128) DEFAULT NULL,
  job_status VARCHAR(64) DEFAULT NULL,
  expected_salary_min INT DEFAULT NULL,
  expected_salary_max INT DEFAULT NULL,
  work_years INT NOT NULL DEFAULT 0,
  self_evaluation TEXT DEFAULT NULL,
  is_public TINYINT NOT NULL DEFAULT 0,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 deleted, 1 draft, 2 published',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_resume_user (user_id),
  KEY idx_resume_user_status (user_id, status),
  KEY idx_resume_public (is_public, updated_at)
) ENGINE=InnoDB COMMENT='Resume master table';

CREATE TABLE IF NOT EXISTS resume_education (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  resume_id BIGINT UNSIGNED NOT NULL,
  school_name VARCHAR(128) NOT NULL,
  degree VARCHAR(64) DEFAULT NULL,
  major VARCHAR(128) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  is_current TINYINT NOT NULL DEFAULT 0,
  description TEXT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_resume_education_user_id (user_id),
  KEY idx_resume_education_resume (resume_id, sort_order)

) ENGINE=InnoDB COMMENT='Resume education experience';

CREATE TABLE IF NOT EXISTS resume_work_experience (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  resume_id BIGINT UNSIGNED NOT NULL,
  company_name VARCHAR(128) NOT NULL,
  position VARCHAR(128) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  is_current TINYINT NOT NULL DEFAULT 0,
  description TEXT DEFAULT NULL,
  achievements TEXT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_resume_work_experience_user_id (user_id),
  KEY idx_resume_work_resume (resume_id, sort_order)

) ENGINE=InnoDB COMMENT='Resume work experience';

CREATE TABLE IF NOT EXISTS resume_project (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  resume_id BIGINT UNSIGNED NOT NULL,
  project_name VARCHAR(160) NOT NULL,
  project_role VARCHAR(128) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  description TEXT DEFAULT NULL,
  responsibilities TEXT DEFAULT NULL,
  tech_stack VARCHAR(512) DEFAULT NULL,
  project_url VARCHAR(512) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_resume_project_user_id (user_id),
  KEY idx_resume_project_resume (resume_id, sort_order)

) ENGINE=InnoDB COMMENT='Resume project experience';

CREATE TABLE IF NOT EXISTS resume_skill (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  resume_id BIGINT UNSIGNED NOT NULL,
  skill_name VARCHAR(128) NOT NULL,
  skill_level VARCHAR(64) DEFAULT NULL,
  years_experience INT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_resume_skill_user_id (user_id),
  KEY idx_resume_skill_resume (resume_id, sort_order),
  KEY idx_resume_skill_name (skill_name)

) ENGINE=InnoDB COMMENT='Resume skill';

CREATE TABLE IF NOT EXISTS resume_certificate (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  resume_id BIGINT UNSIGNED NOT NULL,
  certificate_name VARCHAR(160) NOT NULL,
  issue_authority VARCHAR(160) DEFAULT NULL,
  issue_date DATE DEFAULT NULL,
  score VARCHAR(64) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_resume_certificate_user_id (user_id),
  KEY idx_resume_certificate_resume (resume_id, sort_order)

) ENGINE=InnoDB COMMENT='Resume certificate and honor';

CREATE TABLE IF NOT EXISTS resume_social_experience (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  resume_id BIGINT UNSIGNED NOT NULL,
  experience_type VARCHAR(64) DEFAULT NULL,
  title VARCHAR(160) NOT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  description TEXT DEFAULT NULL,
  achievements TEXT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_resume_social_experience_user_id (user_id),
  KEY idx_resume_social_resume (resume_id, sort_order)

) ENGINE=InnoDB COMMENT='Resume social experience';

/* =========================================================
   cot_content: general-service content and growth records
   ========================================================= */

USE cot_content;

CREATE TABLE IF NOT EXISTS content_category (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  category_code VARCHAR(64) NOT NULL,
  category_name VARCHAR(96) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_content_category_user_id (user_id),
  UNIQUE KEY uk_content_category_code (category_code),
  KEY idx_content_category_parent (parent_id, sort_order)

) ENGINE=InnoDB COMMENT='Content category';

CREATE TABLE IF NOT EXISTS content_article (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED DEFAULT NULL,
  title VARCHAR(180) NOT NULL,
  summary VARCHAR(512) DEFAULT NULL,
  content_md LONGTEXT NOT NULL,
  content_html LONGTEXT DEFAULT NULL,
  content_type VARCHAR(32) NOT NULL DEFAULT 'journal',
  cover_image_url VARCHAR(512) DEFAULT NULL,
  location VARCHAR(128) DEFAULT NULL,
  weather VARCHAR(64) DEFAULT NULL,
  mood VARCHAR(64) DEFAULT NULL,
  visibility TINYINT NOT NULL DEFAULT 2 COMMENT '1 private, 2 public, 3 friends',
  is_top TINYINT NOT NULL DEFAULT 0,
  is_essence TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 draft, 1 published, 2 hidden, 3 rejected',
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  like_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  favorite_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  comment_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  publish_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_article_user_time (user_id, created_at),
  KEY idx_article_category_time (category_id, status, publish_at),
  KEY idx_article_feed (visibility, status, is_top, publish_at),
  FULLTEXT KEY ft_article_title_summary (title, summary)
) ENGINE=InnoDB COMMENT='Article, diary, note and timeline content';

CREATE TABLE IF NOT EXISTS content_media (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  content_id BIGINT UNSIGNED NOT NULL,
  file_id BIGINT UNSIGNED DEFAULT NULL,
  media_type TINYINT NOT NULL COMMENT '1 image, 2 video, 3 attachment',
  media_url VARCHAR(512) NOT NULL,
  thumbnail_url VARCHAR(512) DEFAULT NULL,
  width INT DEFAULT NULL,
  height INT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_content_media_user_id (user_id),
  KEY idx_content_media_content (content_id, sort_order)

) ENGINE=InnoDB COMMENT='Content media';

CREATE TABLE IF NOT EXISTS content_tag (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  tag_name VARCHAR(64) NOT NULL,
  normalized_name VARCHAR(64) NOT NULL,
  usage_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_content_tag_user_id (user_id),
  UNIQUE KEY uk_content_tag_normalized (normalized_name),
  KEY idx_content_tag_usage (usage_count)

) ENGINE=InnoDB COMMENT='Content tag';

CREATE TABLE IF NOT EXISTS content_article_tag (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  content_id BIGINT UNSIGNED NOT NULL,
  tag_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_content_article_tag_user_id (user_id),
  UNIQUE KEY uk_article_tag (content_id, tag_id),
  KEY idx_article_tag_tag (tag_id)

) ENGINE=InnoDB COMMENT='Article-tag relation';

CREATE TABLE IF NOT EXISTS content_comment (
  id BIGINT UNSIGNED NOT NULL,
  content_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  root_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  reply_to_user_id BIGINT UNSIGNED DEFAULT NULL,
  comment_text TEXT NOT NULL,
  like_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 normal, 2 hidden, 3 deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_comment_content_time (content_id, created_at),
  KEY idx_comment_parent_time (parent_id, created_at),
  KEY idx_comment_user_time (user_id, created_at)
) ENGINE=InnoDB COMMENT='Content comment';

CREATE TABLE IF NOT EXISTS content_reaction (
  id BIGINT UNSIGNED NOT NULL,
  biz_type VARCHAR(32) NOT NULL COMMENT 'article, comment, suggestion',
  biz_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  reaction_type VARCHAR(32) NOT NULL DEFAULT 'like',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_content_reaction (biz_type, biz_id, user_id, reaction_type),
  KEY idx_content_reaction_user (user_id, created_at)
) ENGINE=InnoDB COMMENT='Like and reaction record';

CREATE TABLE IF NOT EXISTS content_favorite (
  id BIGINT UNSIGNED NOT NULL,
  content_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  folder_name VARCHAR(96) NOT NULL DEFAULT 'Default',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_content_favorite (content_id, user_id),
  KEY idx_content_favorite_user_time (user_id, created_at)
) ENGINE=InnoDB COMMENT='Content favorite';

CREATE TABLE IF NOT EXISTS content_audit (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  content_id BIGINT UNSIGNED NOT NULL,
  audit_status TINYINT NOT NULL COMMENT '0 pending, 1 pass, 2 reject',
  audit_reason VARCHAR(255) DEFAULT NULL,
  auditor_id BIGINT UNSIGNED DEFAULT NULL,
  audited_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_content_audit_user_id (user_id),
  KEY idx_content_audit_content (content_id, created_at),
  KEY idx_content_audit_status (audit_status, created_at)

) ENGINE=InnoDB COMMENT='Content audit';

CREATE TABLE IF NOT EXISTS growth_record (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  stage VARCHAR(32) NOT NULL COMMENT 'high_school, university, workplace',
  semester VARCHAR(64) DEFAULT NULL,
  record_date DATE NOT NULL,
  exam_name VARCHAR(128) DEFAULT NULL,
  exam_rank VARCHAR(64) DEFAULT NULL,
  exam_score DECIMAL(8,2) DEFAULT NULL,
  best_subject VARCHAR(64) DEFAULT NULL,
  weakest_subject VARCHAR(64) DEFAULT NULL,
  study_hours DECIMAL(6,2) DEFAULT NULL,
  study_notes TEXT DEFAULT NULL,
  competition_name VARCHAR(160) DEFAULT NULL,
  competition_award VARCHAR(160) DEFAULT NULL,
  activity_name VARCHAR(160) DEFAULT NULL,
  activity_role VARCHAR(128) DEFAULT NULL,
  interest_tested VARCHAR(255) DEFAULT NULL,
  interest_continued VARCHAR(255) DEFAULT NULL,
  skill_learned VARCHAR(255) DEFAULT NULL,
  stress_level INT DEFAULT NULL,
  happiness_level INT DEFAULT NULL,
  mood_notes TEXT DEFAULT NULL,
  challenge_text TEXT DEFAULT NULL,
  help_needed TEXT DEFAULT NULL,
  close_friends_count INT DEFAULT NULL,
  new_friends INT DEFAULT NULL,
  conflict_experience TEXT DEFAULT NULL,
  leadership_exp TEXT DEFAULT NULL,
  self_awareness TEXT DEFAULT NULL,
  career_interest VARCHAR(255) DEFAULT NULL,
  dream_college VARCHAR(128) DEFAULT NULL,
  dream_major VARCHAR(128) DEFAULT NULL,
  company_name VARCHAR(160) DEFAULT NULL,
  job_title VARCHAR(160) DEFAULT NULL,
  job_content TEXT DEFAULT NULL,
  work_skills TEXT DEFAULT NULL,
  work_achievements TEXT DEFAULT NULL,
  work_challenges TEXT DEFAULT NULL,
  career_plan TEXT DEFAULT NULL,
  sleep_hours DECIMAL(5,2) DEFAULT NULL,
  exercise_minutes INT DEFAULT NULL,
  screen_time_hours DECIMAL(5,2) DEFAULT NULL,
  family_communication_quality INT DEFAULT NULL,
  family_support TEXT DEFAULT NULL,
  is_milestone TINYINT NOT NULL DEFAULT 0,
  milestone_name VARCHAR(160) DEFAULT NULL,
  achievement_this_period TEXT DEFAULT NULL,
  improvement_needed TEXT DEFAULT NULL,
  next_goal TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_growth_user_date (user_id, record_date),
  KEY idx_growth_stage_date (stage, record_date),
  KEY idx_growth_milestone (user_id, is_milestone, record_date)
) ENGINE=InnoDB COMMENT='Growth timeline record';

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

/* =========================================================
   cot_learning: question bank, practice, score and graph
   ========================================================= */

USE cot_learning;

CREATE TABLE IF NOT EXISTS learning_subject (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  subject_code VARCHAR(64) NOT NULL,
  subject_name VARCHAR(96) NOT NULL,
  category_level VARCHAR(64) NOT NULL COMMENT 'high_school, university, exam, certificate',
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_learning_subject_user_id (user_id),
  UNIQUE KEY uk_learning_subject_code (subject_code),
  KEY idx_learning_subject_parent (parent_id, sort_order),
  KEY idx_learning_subject_category (category_level, status)

) ENGINE=InnoDB COMMENT='Learning subject';

CREATE TABLE IF NOT EXISTS knowledge_point (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  subject_id BIGINT UNSIGNED NOT NULL,
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  point_code VARCHAR(96) DEFAULT NULL,
  point_name VARCHAR(160) NOT NULL,
  description TEXT DEFAULT NULL,
  difficulty_level VARCHAR(32) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_knowledge_point_user_id (user_id),
  UNIQUE KEY uk_knowledge_point_code (subject_id, point_code),
  KEY idx_knowledge_point_subject_parent (subject_id, parent_id, sort_order),
  KEY idx_knowledge_point_name (point_name)

) ENGINE=InnoDB COMMENT='Knowledge point tree';

CREATE TABLE IF NOT EXISTS knowledge_edge (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  subject_id BIGINT UNSIGNED NOT NULL,
  source_point_id BIGINT UNSIGNED NOT NULL,
  target_point_id BIGINT UNSIGNED NOT NULL,
  relation_type VARCHAR(32) NOT NULL COMMENT 'prerequisite, related, contains',
  weight DECIMAL(6,4) NOT NULL DEFAULT 1.0000,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_knowledge_edge_user_id (user_id),
  UNIQUE KEY uk_knowledge_edge (source_point_id, target_point_id, relation_type),
  KEY idx_knowledge_edge_subject (subject_id, relation_type),
  KEY idx_knowledge_edge_target (target_point_id)

) ENGINE=InnoDB COMMENT='Knowledge graph edge';

CREATE TABLE IF NOT EXISTS question (
  id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED DEFAULT NULL,
  knowledge_point_id BIGINT UNSIGNED DEFAULT NULL,
  subject_name VARCHAR(96) DEFAULT NULL,
  question_type VARCHAR(32) NOT NULL COMMENT 'single, multiple, judge, blank, answer',
  category_level VARCHAR(64) DEFAULT NULL,
  knowledge_point VARCHAR(512) DEFAULT NULL,
  question_title TEXT NOT NULL,
  options JSON DEFAULT NULL,
  correct_answer TEXT NOT NULL,
  answer_analysis TEXT DEFAULT NULL,
  difficulty_level VARCHAR(32) DEFAULT NULL,
  score_value INT NOT NULL DEFAULT 0,
  source_type VARCHAR(64) DEFAULT NULL,
  source_name VARCHAR(160) DEFAULT NULL,
  use_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  mistake_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  mistake_rate DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
  status TINYINT NOT NULL DEFAULT 1,
  created_by BIGINT UNSIGNED DEFAULT NULL,
  audit_status VARCHAR(32) NOT NULL DEFAULT 'pending',
  audit_remark VARCHAR(255) DEFAULT NULL,
  audited_by BIGINT UNSIGNED DEFAULT NULL,
  audited_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_question_subject_point (subject_id, knowledge_point_id),
  KEY idx_question_user_audit (created_by, audit_status, category_level),
  KEY idx_question_type_diff (question_type, difficulty_level),
  KEY idx_question_usage (use_count, mistake_rate),
  FULLTEXT KEY ft_question_title (question_title)
) ENGINE=InnoDB COMMENT='Question bank';

CREATE TABLE IF NOT EXISTS question_option (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  question_id BIGINT UNSIGNED NOT NULL,
  option_key VARCHAR(16) NOT NULL,
  option_text TEXT NOT NULL,
  is_correct TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_question_option_user_id (user_id),
  UNIQUE KEY uk_question_option_key (question_id, option_key),
  KEY idx_question_option_question (question_id, sort_order)

) ENGINE=InnoDB COMMENT='Question option';

CREATE TABLE IF NOT EXISTS practice_session (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  session_type VARCHAR(32) NOT NULL DEFAULT 'practice' COMMENT 'practice, exam, review',
  subject_id BIGINT UNSIGNED DEFAULT NULL,
  title VARCHAR(160) DEFAULT NULL,
  category_level VARCHAR(64) DEFAULT NULL,
  subject_name VARCHAR(96) DEFAULT NULL,
  knowledge_points VARCHAR(512) DEFAULT NULL,
  difficulty_level VARCHAR(32) DEFAULT NULL,
  question_ids TEXT DEFAULT NULL,
  total_questions INT NOT NULL DEFAULT 0,
  answered_questions INT NOT NULL DEFAULT 0,
  correct_count INT NOT NULL DEFAULT 0,
  wrong_count INT NOT NULL DEFAULT 0,
  score_total INT NOT NULL DEFAULT 0,
  score_obtained INT NOT NULL DEFAULT 0,
  duration_seconds INT DEFAULT NULL,
  anti_cheat_enabled TINYINT NOT NULL DEFAULT 0,
  suspicious_count INT NOT NULL DEFAULT 0,
  started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  finished_at DATETIME DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 running, 2 finished, 3 abandoned',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_practice_user_time (user_id, started_at),
  KEY idx_practice_subject_time (subject_id, started_at)
) ENGINE=InnoDB COMMENT='Practice or exam session';

CREATE TABLE IF NOT EXISTS answer_record (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  session_id BIGINT UNSIGNED DEFAULT NULL,
  question_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED DEFAULT NULL,
  knowledge_point_id BIGINT UNSIGNED DEFAULT NULL,
  subject_name VARCHAR(96) DEFAULT NULL,
  question_type VARCHAR(32) DEFAULT NULL,
  category_level VARCHAR(64) DEFAULT NULL,
  knowledge_point VARCHAR(512) DEFAULT NULL,
  user_answer TEXT DEFAULT NULL,
  correct_answer TEXT DEFAULT NULL,
  is_correct TINYINT NOT NULL DEFAULT 0,
  score INT NOT NULL DEFAULT 0,
  answer_time_seconds INT DEFAULT NULL,
  mistake_added TINYINT NOT NULL DEFAULT 0,
  exam_session VARCHAR(128) DEFAULT NULL,
  answer_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_answer_user_time (user_id, answer_at),
  KEY idx_answer_session (session_id, question_id),
  KEY idx_answer_question_time (question_id, answer_at),
  KEY idx_answer_point (user_id, knowledge_point_id, answer_at)
) ENGINE=InnoDB COMMENT='Answer event record';

CREATE TABLE IF NOT EXISTS mistake_record (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  question_id BIGINT UNSIGNED DEFAULT NULL,
  subject_id BIGINT UNSIGNED DEFAULT NULL,
  knowledge_point_id BIGINT UNSIGNED DEFAULT NULL,
  last_answer_record_id BIGINT UNSIGNED DEFAULT NULL,
  subject_name VARCHAR(96) DEFAULT NULL,
  mistake_name VARCHAR(255) DEFAULT NULL,
  mistake_type VARCHAR(32) DEFAULT NULL,
  question_options JSON DEFAULT NULL,
  student_choice TEXT DEFAULT NULL,
  wrong_answer TEXT DEFAULT NULL,
  correct_answer TEXT DEFAULT NULL,
  answer_analysis TEXT DEFAULT NULL,
  knowledge_point VARCHAR(512) DEFAULT NULL,
  mistake_reason VARCHAR(255) DEFAULT NULL,
  correction_notes TEXT DEFAULT NULL,
  mistake_count INT NOT NULL DEFAULT 1,
  review_count INT NOT NULL DEFAULT 0,
  mastered TINYINT NOT NULL DEFAULT 0,
  mistake_date DATE DEFAULT NULL,
  last_review_date DATE DEFAULT NULL,
  next_review_date DATE DEFAULT NULL,
  last_mistake_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mistake_user_question (user_id, question_id),
  KEY idx_mistake_review (user_id, mastered, next_review_date),
  KEY idx_mistake_point (user_id, knowledge_point_id)
) ENGINE=InnoDB COMMENT='Mistake book';

CREATE TABLE IF NOT EXISTS score_record (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED DEFAULT NULL,
  subject_name VARCHAR(96) NOT NULL,
  exam_name VARCHAR(160) NOT NULL,
  exam_type VARCHAR(64) DEFAULT NULL,
  exam_date DATE NOT NULL,
  score DECIMAL(8,2) NOT NULL,
  full_score DECIMAL(8,2) DEFAULT NULL,
  class_rank INT DEFAULT NULL,
  grade_rank INT DEFAULT NULL,
  notes TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_score_user_date (user_id, exam_date),
  KEY idx_score_subject_date (subject_id, exam_date)
) ENGINE=InnoDB COMMENT='Score record';

CREATE TABLE IF NOT EXISTS knowledge_mastery_stat (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  knowledge_point_id BIGINT UNSIGNED NOT NULL,
  answer_count INT NOT NULL DEFAULT 0,
  correct_count INT NOT NULL DEFAULT 0,
  mistake_count INT NOT NULL DEFAULT 0,
  mastery_score DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  last_practice_at DATETIME DEFAULT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mastery_user_point (user_id, knowledge_point_id),
  KEY idx_mastery_subject_score (user_id, subject_id, mastery_score)
) ENGINE=InnoDB COMMENT='Knowledge mastery aggregate';

/* =========================================================
   cot_highschool: course selection and gaokao volunteer
   ========================================================= */

USE cot_highschool;

CREATE TABLE IF NOT EXISTS hs_subject (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  subject_code VARCHAR(64) NOT NULL,
  subject_name VARCHAR(96) NOT NULL,
  subject_type TINYINT NOT NULL COMMENT '1 first-choice, 2 second-choice, 3 basic',
  description VARCHAR(255) DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hs_subject_user_id (user_id),
  UNIQUE KEY uk_hs_subject_code (subject_code),
  KEY idx_hs_subject_type (subject_type, status)

) ENGINE=InnoDB COMMENT='High school subject';

CREATE TABLE IF NOT EXISTS hs_subject_combination (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  combination_code VARCHAR(64) NOT NULL,
  combination_name VARCHAR(128) NOT NULL,
  first_subject_id BIGINT UNSIGNED NOT NULL,
  second_subject_1_id BIGINT UNSIGNED NOT NULL,
  second_subject_2_id BIGINT UNSIGNED NOT NULL,
  first_subject_name VARCHAR(96) DEFAULT NULL,
  second_subject_1_name VARCHAR(96) DEFAULT NULL,
  second_subject_2_name VARCHAR(96) DEFAULT NULL,
  major_coverage_rate DECIMAL(6,2) DEFAULT NULL,
  recommended_level VARCHAR(32) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hs_subject_combination_user_id (user_id),
  UNIQUE KEY uk_hs_subject_combination_code (combination_code),
  UNIQUE KEY uk_hs_subject_combination_subjects (first_subject_id, second_subject_1_id, second_subject_2_id),
  KEY idx_hs_subject_combination_rate (major_coverage_rate)

) ENGINE=InnoDB COMMENT='Subject selection combination';

CREATE TABLE IF NOT EXISTS hs_student_selection (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL COMMENT 'logical user id',
  user_name VARCHAR(80) DEFAULT NULL,
  grade VARCHAR(32) DEFAULT NULL,
  class_name VARCHAR(64) DEFAULT NULL,
  academic_year VARCHAR(32) DEFAULT NULL,
  semester VARCHAR(32) DEFAULT NULL,
  chinese_score DECIMAL(8,2) DEFAULT NULL,
  math_score DECIMAL(8,2) DEFAULT NULL,
  english_score DECIMAL(8,2) DEFAULT NULL,
  first_subject_id BIGINT UNSIGNED DEFAULT NULL,
  first_subject_name VARCHAR(96) DEFAULT NULL,
  first_subject_score DECIMAL(8,2) DEFAULT NULL,
  first_subject_level VARCHAR(32) DEFAULT NULL,
  second_subject_1_id BIGINT UNSIGNED DEFAULT NULL,
  second_subject_1_name VARCHAR(96) DEFAULT NULL,
  second_subject_1_score DECIMAL(8,2) DEFAULT NULL,
  second_subject_1_level VARCHAR(32) DEFAULT NULL,
  second_subject_2_id BIGINT UNSIGNED DEFAULT NULL,
  second_subject_2_name VARCHAR(96) DEFAULT NULL,
  second_subject_2_score DECIMAL(8,2) DEFAULT NULL,
  second_subject_2_level VARCHAR(32) DEFAULT NULL,
  combination_id BIGINT UNSIGNED DEFAULT NULL,
  combination_name VARCHAR(128) DEFAULT NULL,
  total_score DECIMAL(8,2) DEFAULT NULL,
  total_score_weighted DECIMAL(8,2) DEFAULT NULL,
  class_rank INT DEFAULT NULL,
  grade_rank INT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 draft, 1 submitted, 2 approved, 3 rejected',
  is_confirmed TINYINT NOT NULL DEFAULT 0,
  confirm_time DATETIME DEFAULT NULL,
  selection_reason TEXT DEFAULT NULL,
  future_plan TEXT DEFAULT NULL,
  admin_advice TEXT DEFAULT NULL,
  additional_opinion TEXT DEFAULT NULL,
  is_public TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(512) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_hs_selection_user_year (user_id, academic_year, semester),
  KEY idx_hs_selection_combination (combination_id, status),
  KEY idx_hs_selection_grade_class (grade, class_name, status)
) ENGINE=InnoDB COMMENT='User course selection';

CREATE TABLE IF NOT EXISTS hs_selection_intention (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  user_name VARCHAR(80) DEFAULT NULL,
  grade VARCHAR(32) DEFAULT NULL,
  class_name VARCHAR(64) DEFAULT NULL,
  first_subject_intention VARCHAR(96) DEFAULT NULL,
  second_subject_intention_1 VARCHAR(96) DEFAULT NULL,
  second_subject_intention_2 VARCHAR(96) DEFAULT NULL,
  second_subject_backup_1 VARCHAR(96) DEFAULT NULL,
  second_subject_backup_2 VARCHAR(96) DEFAULT NULL,
  target_major VARCHAR(160) DEFAULT NULL,
  target_university VARCHAR(160) DEFAULT NULL,
  reason TEXT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hs_intention_user (user_id, created_at),
  KEY idx_hs_intention_grade_class (grade, class_name, status)
) ENGINE=InnoDB COMMENT='Subject selection intention';

CREATE TABLE IF NOT EXISTS hs_selection_history (
  id BIGINT UNSIGNED NOT NULL,
  selection_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  user_name VARCHAR(80) DEFAULT NULL,
  change_type VARCHAR(64) NOT NULL,
  old_first_subject VARCHAR(96) DEFAULT NULL,
  new_first_subject VARCHAR(96) DEFAULT NULL,
  old_second_subject_1 VARCHAR(96) DEFAULT NULL,
  new_second_subject_1 VARCHAR(96) DEFAULT NULL,
  old_second_subject_2 VARCHAR(96) DEFAULT NULL,
  new_second_subject_2 VARCHAR(96) DEFAULT NULL,
  change_reason TEXT DEFAULT NULL,
  approve_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending, 1 approved, 2 rejected',
  approve_comment VARCHAR(512) DEFAULT NULL,
  change_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hs_history_selection (selection_id, change_time),
  KEY idx_hs_history_user (user_id, change_time),
  KEY idx_hs_history_approve (approve_status, change_time)
) ENGINE=InnoDB COMMENT='Selection change history';

CREATE TABLE IF NOT EXISTS hs_grading_scale (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  province VARCHAR(64) NOT NULL,
  admission_year INT NOT NULL,
  subject_id BIGINT UNSIGNED DEFAULT NULL,
  subject_name VARCHAR(96) NOT NULL,
  original_score_min DECIMAL(8,2) NOT NULL,
  original_score_max DECIMAL(8,2) NOT NULL,
  grade_level VARCHAR(16) NOT NULL,
  assigned_score_min DECIMAL(8,2) NOT NULL,
  assigned_score_max DECIMAL(8,2) NOT NULL,
  percentile_min DECIMAL(8,4) DEFAULT NULL,
  percentile_max DECIMAL(8,4) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hs_grading_scale_user_id (user_id),
  KEY idx_hs_grading_province_year_subject (province, admission_year, subject_name),
  KEY idx_hs_grading_level (grade_level)

) ENGINE=InnoDB COMMENT='Gaokao grading scale';

CREATE TABLE IF NOT EXISTS hs_course_guidance (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  guidance_type VARCHAR(64) NOT NULL COMMENT 'admin, ai, system',
  title VARCHAR(160) NOT NULL,
  content TEXT NOT NULL,
  recommended_combination_id BIGINT UNSIGNED DEFAULT NULL,
  recommended_combination_name VARCHAR(128) DEFAULT NULL,
  risk_analysis TEXT DEFAULT NULL,
  advisor_id BIGINT UNSIGNED DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hs_guidance_user (user_id, created_at),
  KEY idx_hs_guidance_type (guidance_type, status)
) ENGINE=InnoDB COMMENT='Course selection guidance';

CREATE TABLE IF NOT EXISTS gaokao_university (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  university_code VARCHAR(64) NOT NULL,
  university_name VARCHAR(160) NOT NULL,
  province VARCHAR(64) DEFAULT NULL,
  city VARCHAR(64) DEFAULT NULL,
  level_tags VARCHAR(255) DEFAULT NULL COMMENT '985, 211, double-first-class, etc',
  type_tags VARCHAR(255) DEFAULT NULL,
  website VARCHAR(255) DEFAULT NULL,
  logo_url VARCHAR(512) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  founded_year INT DEFAULT NULL,
  ownership VARCHAR(32) DEFAULT NULL COMMENT 'public/private',
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_gaokao_university_user_id (user_id),
  UNIQUE KEY uk_gaokao_university_code (university_code),
  KEY idx_gaokao_university_region (province, city),
  KEY idx_gaokao_university_level (level_tags)

) ENGINE=InnoDB COMMENT='Gaokao university catalog';

CREATE TABLE IF NOT EXISTS gaokao_major (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  major_code VARCHAR(64) NOT NULL,
  major_name VARCHAR(160) NOT NULL,
  category VARCHAR(96) DEFAULT NULL,
  sub_category VARCHAR(96) DEFAULT NULL,
  duration_years INT DEFAULT NULL,
  degree_type VARCHAR(64) DEFAULT NULL,
  tuition_fee DECIMAL(10,2) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_gaokao_major_user_id (user_id),
  UNIQUE KEY uk_gaokao_major_code (major_code),
  KEY idx_gaokao_major_category (category, sub_category)

) ENGINE=InnoDB COMMENT='Gaokao major catalog';

CREATE TABLE IF NOT EXISTS gaokao_admission_plan (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  university_id BIGINT UNSIGNED NOT NULL,
  major_id BIGINT UNSIGNED NOT NULL,
  admission_year INT NOT NULL,
  province VARCHAR(64) NOT NULL,
  student_type VARCHAR(64) NOT NULL,
  batch_name VARCHAR(64) NOT NULL DEFAULT '',
  planned_num INT DEFAULT NULL,
  actual_num INT DEFAULT NULL,
  min_score INT DEFAULT NULL,
  min_rank INT DEFAULT NULL,
  avg_score DECIMAL(8,2) DEFAULT NULL,
  max_score INT DEFAULT NULL,
  subject_requirement VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_gaokao_admission_plan_user_id (user_id),
  UNIQUE KEY uk_gaokao_plan (university_id, major_id, admission_year, province, student_type, batch_name),
  KEY idx_gaokao_plan_score (province, admission_year, student_type, min_score),
  KEY idx_gaokao_plan_rank (province, admission_year, student_type, min_rank)

) ENGINE=InnoDB COMMENT='Admission plan and score line';

CREATE TABLE IF NOT EXISTS gaokao_major_requirement (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  university_id BIGINT UNSIGNED DEFAULT NULL,
  major_id BIGINT UNSIGNED DEFAULT NULL,
  major_code VARCHAR(64) DEFAULT NULL,
  major_name VARCHAR(160) NOT NULL,
  category VARCHAR(96) DEFAULT NULL,
  first_subject_required VARCHAR(128) DEFAULT NULL,
  second_subject_required VARCHAR(255) DEFAULT NULL,
  requirement_detail TEXT DEFAULT NULL,
  university_name VARCHAR(160) DEFAULT NULL,
  university_level VARCHAR(128) DEFAULT NULL,
  province VARCHAR(64) DEFAULT NULL,
  admission_year INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_gaokao_major_requirement_user_id (user_id),
  KEY idx_major_requirement_major (major_code, province, admission_year),
  KEY idx_major_requirement_subject (first_subject_required, province),
  KEY idx_major_requirement_university (university_id, major_id)

) ENGINE=InnoDB COMMENT='Major subject requirement';

CREATE TABLE IF NOT EXISTS hs_major_subject_match (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED DEFAULT NULL,
  combination_id BIGINT UNSIGNED DEFAULT NULL,
  major_requirement_id BIGINT UNSIGNED DEFAULT NULL,
  university_id BIGINT UNSIGNED DEFAULT NULL,
  major_id BIGINT UNSIGNED DEFAULT NULL,
  major_code VARCHAR(64) DEFAULT NULL,
  major_name VARCHAR(160) NOT NULL,
  matched_subjects VARCHAR(255) DEFAULT NULL,
  missing_subjects VARCHAR(255) DEFAULT NULL,
  matching_score DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  match_level VARCHAR(32) DEFAULT NULL COMMENT 'full, partial, none',
  explanation TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hs_major_match_user_score (user_id, matching_score),
  KEY idx_hs_major_match_combination (combination_id, matching_score),
  KEY idx_hs_major_match_major (major_code, university_id)
) ENGINE=InnoDB COMMENT='Subject-major matching result';

CREATE TABLE IF NOT EXISTS user_volunteer_plan (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  plan_name VARCHAR(128) NOT NULL,
  admission_year INT NOT NULL,
  province VARCHAR(64) NOT NULL,
  score INT DEFAULT NULL,
  `rank_no` INT DEFAULT NULL,
  student_type VARCHAR(64) DEFAULT NULL,
  preference_order TINYINT NOT NULL DEFAULT 1 COMMENT '1 parallel, 2 sequence',
  selected_subjects VARCHAR(255) DEFAULT NULL,
  is_final TINYINT NOT NULL DEFAULT 0,
  submit_time DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_volunteer_plan_user_year (user_id, admission_year),
  KEY idx_volunteer_plan_final (user_id, is_final)
) ENGINE=InnoDB COMMENT='User volunteer plan';

CREATE TABLE IF NOT EXISTS user_volunteer_detail (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  volunteer_plan_id BIGINT UNSIGNED NOT NULL,
  priority_no INT NOT NULL,
  university_id BIGINT UNSIGNED NOT NULL,
  major_id BIGINT UNSIGNED NOT NULL,
  is_major_adjusted TINYINT NOT NULL DEFAULT 0,
  matching_check TINYINT NOT NULL DEFAULT 0,
  matching_score INT DEFAULT NULL,
  risk_level VARCHAR(32) DEFAULT NULL COMMENT 'sprint, stable, safe',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_volunteer_detail_user_id (user_id),
  UNIQUE KEY uk_volunteer_detail_priority (volunteer_plan_id, priority_no),
  KEY idx_volunteer_detail_uni_major (university_id, major_id)

) ENGINE=InnoDB COMMENT='Volunteer plan detail';

CREATE TABLE IF NOT EXISTS admission_simulation (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  volunteer_detail_id BIGINT UNSIGNED NOT NULL,
  simulation_status VARCHAR(32) NOT NULL DEFAULT 'pending',
  reject_reason VARCHAR(255) DEFAULT NULL,
  predicted_probability DECIMAL(6,4) DEFAULT NULL,
  admitted_time DATETIME DEFAULT NULL,
  score_diff INT DEFAULT NULL,
  rank_diff INT DEFAULT NULL,
  model_version VARCHAR(64) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_admission_simulation_user_id (user_id),
  KEY idx_admission_sim_detail (volunteer_detail_id),
  KEY idx_admission_sim_status (simulation_status, created_at)

) ENGINE=InnoDB COMMENT='Admission simulation result';

/* =========================================================
   cot_university: university-service major, course and thesis
   ========================================================= */

USE cot_university;

CREATE TABLE IF NOT EXISTS uni_major (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  major_code VARCHAR(64) NOT NULL,
  major_name VARCHAR(160) NOT NULL,
  degree_type VARCHAR(64) DEFAULT NULL,
  total_credits DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  compulsory_credits DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  elective_credits DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  description TEXT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_uni_major_user_id (user_id),
  UNIQUE KEY uk_uni_major_code (major_code),
  KEY idx_uni_major_name (major_name)

) ENGINE=InnoDB COMMENT='University major training plan';

CREATE TABLE IF NOT EXISTS uni_course_category (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED DEFAULT NULL,
  major_id BIGINT UNSIGNED NOT NULL,
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  category_name VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  icon VARCHAR(64) DEFAULT NULL,
  color VARCHAR(32) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_uni_category_major_parent (major_id, parent_id, sort_order),
  KEY idx_uni_category_user (user_id)
) ENGINE=InnoDB COMMENT='Course category tree';

CREATE TABLE IF NOT EXISTS uni_course (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED DEFAULT NULL,
  major_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED DEFAULT NULL,
  course_code VARCHAR(64) NOT NULL,
  course_name VARCHAR(160) NOT NULL,
  credit DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  total_hours INT DEFAULT NULL,
  theory_hours INT DEFAULT NULL,
  lab_hours INT DEFAULT NULL,
  course_type VARCHAR(64) DEFAULT NULL,
  term_no INT DEFAULT NULL,
  exam_type VARCHAR(64) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  prerequisite_text TEXT DEFAULT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_uni_course_code_major (major_id, course_code),
  KEY idx_uni_course_category (category_id, sort_order),
  KEY idx_uni_course_term (major_id, term_no, status),
  FULLTEXT KEY ft_uni_course_name_desc (course_name, description)
) ENGINE=InnoDB COMMENT='University course';

CREATE TABLE IF NOT EXISTS uni_course_prerequisite (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  course_id BIGINT UNSIGNED NOT NULL,
  prerequisite_course_id BIGINT UNSIGNED NOT NULL,
  relation_type VARCHAR(32) NOT NULL DEFAULT 'must_before',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_uni_course_prerequisite_user_id (user_id),
  UNIQUE KEY uk_uni_course_prerequisite (course_id, prerequisite_course_id),
  KEY idx_uni_prerequisite_target (prerequisite_course_id)

) ENGINE=InnoDB COMMENT='Course prerequisite relation';

CREATE TABLE IF NOT EXISTS uni_student_course (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  course_id BIGINT UNSIGNED NOT NULL,
  major_id BIGINT UNSIGNED NOT NULL,
  semester VARCHAR(64) DEFAULT NULL,
  score DECIMAL(6,2) DEFAULT NULL,
  grade_point DECIMAL(4,2) DEFAULT NULL,
  is_passed TINYINT NOT NULL DEFAULT 0,
  is_retake TINYINT NOT NULL DEFAULT 0,
  retake_count INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'planned',
  notes TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_uni_student_course (user_id, course_id, semester),
  KEY idx_uni_student_major_semester (user_id, major_id, semester),
  KEY idx_uni_student_status (user_id, status)
) ENGINE=InnoDB COMMENT='User course progress';

CREATE TABLE IF NOT EXISTS uni_graduation_requirement (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  major_id BIGINT UNSIGNED NOT NULL,
  total_credits_required DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  total_credits_earned DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  compulsory_credits_required DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  compulsory_credits_earned DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  elective_credits_required DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  elective_credits_earned DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  gpa DECIMAL(4,2) DEFAULT NULL,
  progress_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  expected_graduation_date DATE DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'in_progress',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_uni_grad_user_major (user_id, major_id),
  KEY idx_uni_grad_status (status, progress_percent)
) ENGINE=InnoDB COMMENT='Graduation requirement progress';

CREATE TABLE IF NOT EXISTS thesis_paper (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(200) NOT NULL,
  supervisor VARCHAR(80) DEFAULT NULL,
  research_direction VARCHAR(128) DEFAULT NULL,
  abstract_text TEXT DEFAULT NULL,
  keywords VARCHAR(255) DEFAULT NULL,
  content_md LONGTEXT DEFAULT NULL,
  stage VARCHAR(64) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  version_no INT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_paper_user_status (user_id, status, updated_at),
  FULLTEXT KEY ft_paper_title_abstract (title, abstract_text)
) ENGINE=InnoDB COMMENT='Thesis or course paper';

CREATE TABLE IF NOT EXISTS thesis_paper_version (
  id BIGINT UNSIGNED NOT NULL,
  paper_id BIGINT UNSIGNED NOT NULL,
  version_no INT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content_md LONGTEXT DEFAULT NULL,
  change_summary VARCHAR(512) DEFAULT NULL,
  created_by BIGINT UNSIGNED DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_paper_version (paper_id, version_no),
  KEY idx_paper_version_time (paper_id, created_at)
) ENGINE=InnoDB COMMENT='Paper version history';

CREATE TABLE IF NOT EXISTS thesis_suggestion (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  paper_id BIGINT UNSIGNED NOT NULL,
  suggester_id BIGINT UNSIGNED DEFAULT NULL,
  suggestion_type VARCHAR(32) NOT NULL DEFAULT 'text',
  suggestion_content TEXT NOT NULL,
  position_json JSON DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 open, 2 accepted, 3 rejected, 4 resolved',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_thesis_suggestion_user_id (user_id),
  KEY idx_suggestion_paper (paper_id, created_at),
  KEY idx_suggestion_status (status, created_at)

) ENGINE=InnoDB COMMENT='Paper revision suggestion';

/* =========================================================
   cot_platform: upload, system dictionary and operations
   ========================================================= */


USE cot_platform;

CREATE TABLE IF NOT EXISTS file_asset (
  id BIGINT UNSIGNED NOT NULL,
  owner_user_id BIGINT UNSIGNED DEFAULT NULL,
  biz_type VARCHAR(64) DEFAULT NULL,
  biz_id BIGINT UNSIGNED DEFAULT NULL,
  original_name VARCHAR(255) NOT NULL,
  storage_name VARCHAR(255) NOT NULL,
  bucket_name VARCHAR(128) NOT NULL DEFAULT 'local',
  object_key VARCHAR(512) NOT NULL,
  file_url VARCHAR(512) NOT NULL,
  thumbnail_url VARCHAR(512) DEFAULT NULL,
  mime_type VARCHAR(128) DEFAULT NULL,
  file_ext VARCHAR(32) DEFAULT NULL,
  file_size BIGINT UNSIGNED NOT NULL DEFAULT 0,
  file_sha256 CHAR(64) DEFAULT NULL,
  width INT DEFAULT NULL,
  height INT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 normal, 2 temp, 3 deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_asset_sha (file_sha256),
  KEY idx_file_asset_owner_time (owner_user_id, created_at),
  KEY idx_file_asset_biz (biz_type, biz_id)
) ENGINE=InnoDB COMMENT='Unified file asset';

CREATE TABLE IF NOT EXISTS sys_dict_type (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  dict_code VARCHAR(96) NOT NULL,
  dict_name VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sys_dict_type_user_id (user_id),
  UNIQUE KEY uk_dict_type_code (dict_code)

) ENGINE=InnoDB COMMENT='System dictionary type';

CREATE TABLE IF NOT EXISTS sys_dict_item (

  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  dict_code VARCHAR(96) NOT NULL,
  item_value VARCHAR(128) NOT NULL,
  item_label VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  remark VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sys_dict_item_user_id (user_id),
  UNIQUE KEY uk_dict_item (dict_code, item_value),
  KEY idx_dict_item_sort (dict_code, status, sort_order)

) ENGINE=InnoDB COMMENT='System dictionary item';

CREATE TABLE IF NOT EXISTS sys_notification (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  notice_type VARCHAR(64) NOT NULL,
  title VARCHAR(160) NOT NULL,
  content TEXT DEFAULT NULL,
  biz_type VARCHAR(64) DEFAULT NULL,
  biz_id BIGINT UNSIGNED DEFAULT NULL,
  dedupe_key VARCHAR(160) DEFAULT NULL,
  action_path VARCHAR(255) DEFAULT NULL,
  due_at VARCHAR(32) DEFAULT NULL,
  read_status TINYINT NOT NULL DEFAULT 0,
  read_at DATETIME DEFAULT NULL,
  dismissed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_user_dedupe (user_id, dedupe_key),
  KEY idx_notification_user_read (user_id, read_status, created_at),
  KEY idx_notification_biz (biz_type, biz_id)
) ENGINE=InnoDB COMMENT='User notification';

CREATE TABLE IF NOT EXISTS sys_notification_preference (
  user_id BIGINT UNSIGNED NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  quiet_hours_enabled TINYINT NOT NULL DEFAULT 0,
  quiet_start VARCHAR(5) NOT NULL DEFAULT '22:00',
  quiet_end VARCHAR(5) NOT NULL DEFAULT '08:00',
  preferred_stage VARCHAR(32) NOT NULL DEFAULT 'all',
  week_starts_monday TINYINT NOT NULL DEFAULT 1,
  default_reminder_minutes INT NOT NULL DEFAULT 10,
  default_start_time VARCHAR(5) NOT NULL DEFAULT '09:00',
  default_end_time VARCHAR(5) NOT NULL DEFAULT '18:00',
  browser_notifications_enabled TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id)
) ENGINE=InnoDB COMMENT='Notification, quiet hours and growth experience preferences';

CREATE TABLE IF NOT EXISTS admin_operation_log (
  id BIGINT UNSIGNED NOT NULL,
  admin_user_id BIGINT UNSIGNED NOT NULL,
  module_name VARCHAR(96) NOT NULL,
  operation_type VARCHAR(64) NOT NULL,
  operation_desc VARCHAR(255) DEFAULT NULL,
  request_method VARCHAR(16) DEFAULT NULL,
  request_uri VARCHAR(255) DEFAULT NULL,
  request_params JSON DEFAULT NULL,
  response_code INT DEFAULT NULL,
  client_ip VARCHAR(64) DEFAULT NULL,
  user_agent VARCHAR(512) DEFAULT NULL,
  trace_id VARCHAR(64) DEFAULT NULL,
  cost_ms INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_admin_log_user_time (admin_user_id, created_at),
  KEY idx_admin_log_module_time (module_name, operation_type, created_at),
  KEY idx_admin_log_trace (trace_id)
) ENGINE=InnoDB COMMENT='Admin operation audit log';

CREATE TABLE IF NOT EXISTS sys_api_access_log (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED DEFAULT NULL,
  service_name VARCHAR(96) DEFAULT NULL,
  request_method VARCHAR(16) DEFAULT NULL,
  request_uri VARCHAR(255) DEFAULT NULL,
  response_code INT DEFAULT NULL,
  cost_ms INT DEFAULT NULL,
  client_ip VARCHAR(64) DEFAULT NULL,
  trace_id VARCHAR(64) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_api_log_service_time (service_name, created_at),
  KEY idx_api_log_user_time (user_id, created_at),
  KEY idx_api_log_trace (trace_id)
) ENGINE=InnoDB COMMENT='Gateway and API access log';


/* =========================================================
   cot_workplace: career goals, interviews and work reviews
   ========================================================= */

USE cot_workplace;

CREATE TABLE IF NOT EXISTS career_profile (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  current_title VARCHAR(128) DEFAULT NULL,
  target_title VARCHAR(128) DEFAULT NULL,
  industry VARCHAR(128) DEFAULT NULL,
  city VARCHAR(80) DEFAULT NULL,
  years_of_experience INT DEFAULT 0,
  strengths TEXT DEFAULT NULL,
  weakness TEXT DEFAULT NULL,
  career_values TEXT DEFAULT NULL,
  salary_expectation VARCHAR(128) DEFAULT NULL,
  visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_career_profile_user (user_id)
) ENGINE=InnoDB COMMENT='Career profile';

CREATE TABLE IF NOT EXISTS career_goal (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  goal_name VARCHAR(160) NOT NULL,
  goal_type VARCHAR(64) DEFAULT NULL,
  priority VARCHAR(32) DEFAULT 'MEDIUM',
  status VARCHAR(32) DEFAULT 'ACTIVE',
  progress INT NOT NULL DEFAULT 0,
  start_date DATE DEFAULT NULL,
  target_date DATE DEFAULT NULL,
  metric VARCHAR(255) DEFAULT NULL,
  notes TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_career_goal_user_status (user_id, status),
  KEY idx_career_goal_target_date (target_date)
) ENGINE=InnoDB COMMENT='Career goal';

CREATE TABLE IF NOT EXISTS career_task (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  goal_id BIGINT UNSIGNED DEFAULT NULL,
  task_name VARCHAR(180) NOT NULL,
  task_type VARCHAR(64) DEFAULT NULL,
  status VARCHAR(32) DEFAULT 'TODO',
  priority VARCHAR(32) DEFAULT 'MEDIUM',
  quadrant VARCHAR(40) NOT NULL DEFAULT 'NOT_IMPORTANT_NOT_URGENT',
  start_date DATE DEFAULT NULL,
  due_date DATE DEFAULT NULL,
  estimated_minutes INT DEFAULT NULL,
  actual_minutes INT DEFAULT NULL,
  outcome TEXT DEFAULT NULL,
  notes TEXT DEFAULT NULL,
  tags VARCHAR(255) DEFAULT NULL,
  reminder_enabled TINYINT NOT NULL DEFAULT 0,
  reminder_at DATETIME DEFAULT NULL,
  repeat_rule VARCHAR(32) NOT NULL DEFAULT 'NONE',
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_career_task_user_status (user_id, status),
  KEY idx_career_task_goal (goal_id),
  KEY idx_career_task_due (due_date)
) ENGINE=InnoDB COMMENT='Career action task';

CREATE TABLE IF NOT EXISTS interview_prep (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  company_name VARCHAR(160) DEFAULT NULL,
  position_name VARCHAR(160) DEFAULT NULL,
  interview_round VARCHAR(64) DEFAULT NULL,
  interview_date DATE DEFAULT NULL,
  status VARCHAR(32) DEFAULT 'PREPARING',
  question_bank TEXT DEFAULT NULL,
  preparation_notes TEXT DEFAULT NULL,
  feedback TEXT DEFAULT NULL,
  result VARCHAR(64) DEFAULT NULL,
  confidence_score INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_interview_user_status (user_id, status),
  KEY idx_interview_date (interview_date)
) ENGINE=InnoDB COMMENT='Interview preparation';

CREATE TABLE IF NOT EXISTS work_review (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  review_date DATE NOT NULL,
  review_type VARCHAR(64) DEFAULT 'WEEKLY',
  wins TEXT DEFAULT NULL,
  problems TEXT DEFAULT NULL,
  learnings TEXT DEFAULT NULL,
  next_actions TEXT DEFAULT NULL,
  energy_level INT DEFAULT NULL,
  communication_score INT DEFAULT NULL,
  delivery_score INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_work_review_user_date (user_id, review_date)
) ENGINE=InnoDB COMMENT='Work review and reflection';

/* =========================================================
   cot_advanced: long-term advancement roadmaps
   ========================================================= */

USE cot_advanced;

CREATE TABLE IF NOT EXISTS advancement_roadmap (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  roadmap_name VARCHAR(160) NOT NULL,
  stage VARCHAR(64) DEFAULT NULL,
  target_role VARCHAR(128) DEFAULT NULL,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  progress INT NOT NULL DEFAULT 0,
  start_date DATE DEFAULT NULL,
  target_date DATE DEFAULT NULL,
  core_skills TEXT DEFAULT NULL,
  success_metrics TEXT DEFAULT NULL,
  risk_notes TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_adv_roadmap_user_status (user_id, status)
) ENGINE=InnoDB COMMENT='Advancement roadmap';

CREATE TABLE IF NOT EXISTS advancement_milestone (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  roadmap_id BIGINT UNSIGNED NOT NULL,
  milestone_name VARCHAR(180) NOT NULL,
  milestone_type VARCHAR(64) DEFAULT NULL,
  status VARCHAR(32) DEFAULT 'TODO',
  weight INT NOT NULL DEFAULT 1,
  due_date DATE DEFAULT NULL,
  completed_date DATE DEFAULT NULL,
  evidence_url VARCHAR(512) DEFAULT NULL,
  notes TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_adv_milestone_user_status (user_id, status),
  KEY idx_adv_milestone_roadmap (roadmap_id)
) ENGINE=InnoDB COMMENT='Advancement milestone';

CREATE TABLE IF NOT EXISTS skill_progress (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  roadmap_id BIGINT UNSIGNED DEFAULT NULL,
  skill_name VARCHAR(128) NOT NULL,
  skill_category VARCHAR(80) DEFAULT NULL,
  current_level VARCHAR(64) DEFAULT NULL,
  target_level VARCHAR(64) DEFAULT NULL,
  progress INT NOT NULL DEFAULT 0,
  practice_hours INT DEFAULT 0,
  last_practiced_at DATE DEFAULT NULL,
  evidence TEXT DEFAULT NULL,
  next_practice TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_skill_progress_user_category (user_id, skill_category),
  KEY idx_skill_progress_roadmap (roadmap_id)
) ENGINE=InnoDB COMMENT='Skill progress';

CREATE TABLE IF NOT EXISTS mentor_session (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  roadmap_id BIGINT UNSIGNED DEFAULT NULL,
  mentor_name VARCHAR(128) DEFAULT NULL,
  session_type VARCHAR(64) DEFAULT NULL,
  session_date DATE DEFAULT NULL,
  topic VARCHAR(180) DEFAULT NULL,
  advice TEXT DEFAULT NULL,
  action_items TEXT DEFAULT NULL,
  value_score INT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_mentor_session_user_date (user_id, session_date),
  KEY idx_mentor_session_roadmap (roadmap_id)
) ENGINE=InnoDB COMMENT='Mentor and review session';

/* Seed minimal roles and dictionaries. */

USE cot_identity;

INSERT INTO iam_role (id, role_code, role_name, role_scope, description, status, sort_order)
VALUES
  (1000000000000000001, 'SUPER_ADMIN', 'Super Admin', 1, 'Full platform permissions', 1, 1),
  (1000000000000000002, 'ADMIN', 'Admin', 1, 'Admin console permissions', 1, 2),
  (1000000000000000003, 'USER', 'User', 3, 'Default user role', 1, 10),
  (1000000000000000006, 'WORKPLACE_USER', 'Workplace User', 3, 'Career workplace permissions', 1, 40),
  (1000000000000000007, 'MENTOR', 'Mentor', 2, 'Career and advancement guidance permissions', 1, 50) AS new ON DUPLICATE KEY UPDATE
  role_name = new.role_name,
  description = new.description,
  updated_at = CURRENT_TIMESTAMP;


INSERT INTO iam_permission (id, permission_code, permission_name, permission_type, api_method, api_path, sort_order, status)
VALUES
  (1000000000000000101, 'workplace:read', 'Workplace read', 3, 'GET', '/api/workplace/**', 101, 1),
  (1000000000000000102, 'workplace:write', 'Workplace write', 3, 'POST', '/api/workplace/**', 102, 1),
  (1000000000000000103, 'advanced:read', 'Advanced read', 3, 'GET', '/api/advanced/**', 103, 1),
  (1000000000000000104, 'advanced:write', 'Advanced write', 3, 'POST', '/api/advanced/**', 104, 1),
  (1000000000000000105, 'career:mentor', 'Career mentor view', 3, '*', '/api/workplace/**', 105, 1) AS new ON DUPLICATE KEY UPDATE
  permission_name = new.permission_name,
  api_method = new.api_method,
  api_path = new.api_path,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO iam_role_permission (id, role_id, permission_id)
VALUES
  (1000000000000000201, 1000000000000000001, 1000000000000000101),
  (1000000000000000202, 1000000000000000001, 1000000000000000102),
  (1000000000000000203, 1000000000000000001, 1000000000000000103),
  (1000000000000000204, 1000000000000000001, 1000000000000000104),
  (1000000000000000205, 1000000000000000002, 1000000000000000101),
  (1000000000000000206, 1000000000000000002, 1000000000000000102),
  (1000000000000000207, 1000000000000000002, 1000000000000000103),
  (1000000000000000208, 1000000000000000002, 1000000000000000104),
  (1000000000000000209, 1000000000000000003, 1000000000000000101),
  (1000000000000000210, 1000000000000000003, 1000000000000000102),
  (1000000000000000211, 1000000000000000003, 1000000000000000103),
  (1000000000000000212, 1000000000000000003, 1000000000000000104),
  (1000000000000000215, 1000000000000000006, 1000000000000000101),
  (1000000000000000216, 1000000000000000006, 1000000000000000102),
  (1000000000000000217, 1000000000000000006, 1000000000000000103),
  (1000000000000000218, 1000000000000000006, 1000000000000000104),
  (1000000000000000219, 1000000000000000007, 1000000000000000101),
  (1000000000000000220, 1000000000000000007, 1000000000000000103),
  (1000000000000000221, 1000000000000000007, 1000000000000000105) AS new ON DUPLICATE KEY UPDATE created_at = created_at;
USE cot_platform;

INSERT INTO sys_dict_type (id, dict_code, dict_name, status, remark)
VALUES
  (2000000000000000001, 'education_stage', 'Education Stage', 1, NULL),
  (2000000000000000002, 'content_type', 'Content Type', 1, NULL),
  (2000000000000000003, 'question_type', 'Question Type', 1, NULL),
  (2000000000000000004, 'volunteer_risk_level', 'Volunteer Risk Level', 1, NULL) AS new ON DUPLICATE KEY UPDATE
  dict_name = new.dict_name,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO sys_dict_item (id, dict_code, item_value, item_label, sort_order, status)
VALUES
  (2000000000000000101, 'education_stage', 'high_school', 'High School', 1, 1),
  (2000000000000000102, 'education_stage', 'university', 'University', 2, 1),
  (2000000000000000103, 'education_stage', 'workplace', 'Workplace', 3, 1),
  (2000000000000000201, 'content_type', 'journal', 'Journal', 1, 1),
  (2000000000000000202, 'content_type', 'record', 'Record', 2, 1),
  (2000000000000000203, 'content_type', 'note', 'Note', 3, 1),
  (2000000000000000301, 'question_type', 'single', 'Single Choice', 1, 1),
  (2000000000000000302, 'question_type', 'multiple', 'Multiple Choice', 2, 1),
  (2000000000000000303, 'question_type', 'judge', 'True Or False', 3, 1),
  (2000000000000000304, 'question_type', 'blank', 'Blank Filling', 4, 1),
  (2000000000000000305, 'question_type', 'answer', 'Written Answer', 5, 1),
  (2000000000000000401, 'volunteer_risk_level', 'sprint', 'Sprint', 1, 1),
  (2000000000000000402, 'volunteer_risk_level', 'stable', 'Stable', 2, 1),
  (2000000000000000403, 'volunteer_risk_level', 'safe', 'Safe', 3, 1) AS new ON DUPLICATE KEY UPDATE
  item_label = new.item_label,
  sort_order = new.sort_order,
  status = new.status,
  updated_at = CURRENT_TIMESTAMP;




/* =========================================================
   cot_content: activity, medals and chat tables
   ========================================================= */

-- Activity check-in, medal and chat tables for cot_content.
USE cot_content;

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
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
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

CREATE TABLE IF NOT EXISTS chat_friend (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  remark VARCHAR(40) DEFAULT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_friend_pair (user_id, friend_id),
  KEY idx_chat_friend_friend (friend_id),
  KEY idx_chat_friend_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='One-way friend relation, stored symmetrically';

CREATE TABLE IF NOT EXISTS chat_group (

  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'owner user id, 0 means platform/global data',
  group_no VARCHAR(16) NOT NULL,
  name VARCHAR(80) NOT NULL,
  announcement VARCHAR(500) NULL,
  owner_id BIGINT NOT NULL,
  member_count INT NOT NULL DEFAULT 0,
  searchable TINYINT(1) NOT NULL DEFAULT 1,
  muted_all TINYINT(1) NOT NULL DEFAULT 0,
  pinned_message_id BIGINT NULL,
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
  muted_until DATETIME NULL,
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

CREATE TABLE IF NOT EXISTS chat_message_hidden (
  id BIGINT NOT NULL PRIMARY KEY,
  message_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  hidden_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_message_hidden (message_id, user_id),
  KEY idx_chat_message_hidden_user (user_id, hidden_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Per-user hidden chat messages';

-- Chronicles of Time field upgrades.
-- Adds columns required by current entities and upgraded features.
-- Run after 01_schema.sql.


/* =========================================================
   From 02_learning_practice_upgrade.sql
   ========================================================= */
-- 文件说明：在线练习/考试/错题本功能升级脚本，适用于已初始化过的 cot_learning 数据库。
-- Target: MySQL 8.0.19+
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

CALL cot_add_column_if_missing('hs_course_guidance', 'user_name', '`user_name` VARCHAR(80) DEFAULT NULL AFTER `user_id`');
CALL cot_add_column_if_missing('hs_course_guidance', 'guidance_date', '`guidance_date` DATE DEFAULT NULL AFTER `user_name`');
CALL cot_add_column_if_missing('hs_course_guidance', 'suggested_major', '`suggested_major` VARCHAR(160) DEFAULT NULL AFTER `recommended_combination_name`');
CALL cot_add_column_if_missing('hs_course_guidance', 'strength_analysis', '`strength_analysis` TEXT DEFAULT NULL AFTER `suggested_major`');
CALL cot_add_column_if_missing('hs_course_guidance', 'weakness_analysis', '`weakness_analysis` TEXT DEFAULT NULL AFTER `strength_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'opportunity_analysis', '`opportunity_analysis` TEXT DEFAULT NULL AFTER `weakness_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'threat_analysis', '`threat_analysis` TEXT DEFAULT NULL AFTER `opportunity_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'action_plan', '`action_plan` TEXT DEFAULT NULL AFTER `threat_analysis`');
CALL cot_add_column_if_missing('hs_course_guidance', 'advisor_name', '`advisor_name` VARCHAR(80) DEFAULT NULL AFTER `advisor_id`');
CALL cot_add_column_if_missing('hs_course_guidance', 'advisor_position', '`advisor_position` VARCHAR(120) DEFAULT NULL AFTER `advisor_name`');
CALL cot_add_column_if_missing('hs_course_guidance', 'user_feedback', '`user_feedback` TEXT DEFAULT NULL AFTER `advisor_position`');
CALL cot_add_column_if_missing('hs_course_guidance', 'additional_feedback', '`additional_feedback` TEXT DEFAULT NULL AFTER `user_feedback`');
CALL cot_add_column_if_missing('hs_course_guidance', 'follow_up_date', '`follow_up_date` DATE DEFAULT NULL AFTER `additional_feedback`');

CALL cot_add_column_if_missing('hs_selection_intention', 'strength_subjects', '`strength_subjects` VARCHAR(255) DEFAULT NULL AFTER `target_university`');
CALL cot_add_column_if_missing('hs_selection_intention', 'weak_subjects', '`weak_subjects` VARCHAR(255) DEFAULT NULL AFTER `strength_subjects`');
CALL cot_add_column_if_missing('hs_selection_intention', 'career_interest', '`career_interest` VARCHAR(255) DEFAULT NULL AFTER `weak_subjects`');
CALL cot_add_column_if_missing('hs_selection_intention', 'admin_feedback', '`admin_feedback` TEXT DEFAULT NULL AFTER `career_interest`');
CALL cot_add_column_if_missing('hs_selection_intention', 'additional_feedback', '`additional_feedback` TEXT DEFAULT NULL AFTER `admin_feedback`');
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




