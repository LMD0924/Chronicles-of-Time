-- One-time migration for existing databases with duplicate resume records.
-- Keeps the most recently updated resume for each user and moves child records to it.

USE cot_profile;

CREATE TEMPORARY TABLE resume_dedup_keep AS
SELECT current_resume.user_id, current_resume.id AS keep_id
FROM resume current_resume
WHERE NOT EXISTS (
    SELECT 1
    FROM resume newer_resume
    WHERE newer_resume.user_id = current_resume.user_id
      AND (
          newer_resume.updated_at > current_resume.updated_at
          OR (newer_resume.updated_at = current_resume.updated_at AND newer_resume.id > current_resume.id)
      )
);

UPDATE resume_education child
JOIN resume duplicate_resume ON child.resume_id = duplicate_resume.id
JOIN resume_dedup_keep keep_resume ON duplicate_resume.user_id = keep_resume.user_id
SET child.resume_id = keep_resume.keep_id
WHERE duplicate_resume.id <> keep_resume.keep_id;

UPDATE resume_work_experience child
JOIN resume duplicate_resume ON child.resume_id = duplicate_resume.id
JOIN resume_dedup_keep keep_resume ON duplicate_resume.user_id = keep_resume.user_id
SET child.resume_id = keep_resume.keep_id
WHERE duplicate_resume.id <> keep_resume.keep_id;

UPDATE resume_project child
JOIN resume duplicate_resume ON child.resume_id = duplicate_resume.id
JOIN resume_dedup_keep keep_resume ON duplicate_resume.user_id = keep_resume.user_id
SET child.resume_id = keep_resume.keep_id
WHERE duplicate_resume.id <> keep_resume.keep_id;

UPDATE resume_skill child
JOIN resume duplicate_resume ON child.resume_id = duplicate_resume.id
JOIN resume_dedup_keep keep_resume ON duplicate_resume.user_id = keep_resume.user_id
SET child.resume_id = keep_resume.keep_id
WHERE duplicate_resume.id <> keep_resume.keep_id;

UPDATE resume_certificate child
JOIN resume duplicate_resume ON child.resume_id = duplicate_resume.id
JOIN resume_dedup_keep keep_resume ON duplicate_resume.user_id = keep_resume.user_id
SET child.resume_id = keep_resume.keep_id
WHERE duplicate_resume.id <> keep_resume.keep_id;

UPDATE resume_social_experience child
JOIN resume duplicate_resume ON child.resume_id = duplicate_resume.id
JOIN resume_dedup_keep keep_resume ON duplicate_resume.user_id = keep_resume.user_id
SET child.resume_id = keep_resume.keep_id
WHERE duplicate_resume.id <> keep_resume.keep_id;

DELETE duplicate_resume
FROM resume duplicate_resume
JOIN resume_dedup_keep keep_resume ON duplicate_resume.user_id = keep_resume.user_id
WHERE duplicate_resume.id <> keep_resume.keep_id;

SET @resume_unique_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'resume' AND index_name = 'uk_resume_user'
);
SET @resume_unique_index_sql = IF(
    @resume_unique_index_exists = 0,
    'ALTER TABLE resume ADD UNIQUE KEY uk_resume_user (user_id)',
    'SELECT 1'
);
PREPARE resume_unique_index_statement FROM @resume_unique_index_sql;
EXECUTE resume_unique_index_statement;
DEALLOCATE PREPARE resume_unique_index_statement;