USE cot_university;

CREATE TABLE IF NOT EXISTS uni_campus_organization (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  organization_type VARCHAR(24) NOT NULL DEFAULT 'CLUB',
  organization_name VARCHAR(120) NOT NULL,
  department VARCHAR(80) NULL,
  role_name VARCHAR(80) NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
  description TEXT NULL,
  achievements TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_campus_organization_user (user_id, status, start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student clubs and student union experience';

CREATE TABLE IF NOT EXISTS uni_campus_activity (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  organization_id BIGINT NULL,
  activity_type VARCHAR(24) NOT NULL DEFAULT 'ACTIVITY',
  title VARCHAR(160) NOT NULL,
  start_at DATETIME NULL,
  end_at DATETIME NULL,
  location VARCHAR(160) NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'PLANNED',
  service_hours DECIMAL(8,2) NOT NULL DEFAULT 0,
  responsibility TEXT NULL,
  result_summary TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_campus_activity_user (user_id, status, start_at),
  KEY idx_campus_activity_organization (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Campus organization activities and affairs';
