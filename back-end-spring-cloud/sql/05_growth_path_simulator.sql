-- Chronicles of Time migration: growth path simulator.
-- Version: 20260720_01
-- Run after 01_schema.sql on MySQL 8.0.19+.
-- This migration is idempotent: tables can be created repeatedly and catalog rows are upserted.

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE DATABASE IF NOT EXISTS cot_advanced DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE cot_advanced;

/* =========================================================
   Route catalog
   ========================================================= */

CREATE TABLE IF NOT EXISTS growth_path_template (
  id BIGINT UNSIGNED NOT NULL,
  stage_code VARCHAR(32) NOT NULL COMMENT 'high, university, career',
  goal_code VARCHAR(64) NOT NULL DEFAULT 'all' COMMENT 'goal code, all means shared by the stage',
  path_code VARCHAR(64) NOT NULL,
  path_name VARCHAR(160) NOT NULL,
  tagline VARCHAR(255) DEFAULT NULL,
  preference_code VARCHAR(32) NOT NULL COMMENT 'certainty, growth, exploration',
  duration_label VARCHAR(64) DEFAULT NULL,
  risk_code VARCHAR(32) NOT NULL DEFAULT 'medium',
  return_label VARCHAR(64) DEFAULT NULL,
  base_score TINYINT UNSIGNED NOT NULL DEFAULT 60,
  summary TEXT DEFAULT NULL,
  strengths_json JSON DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_growth_path_template_key (stage_code, goal_code, path_code),
  KEY idx_growth_path_template_stage (stage_code, goal_code, status, sort_order),
  KEY idx_growth_path_template_preference (preference_code, status)
) ENGINE=InnoDB COMMENT='Growth path simulator route catalog';

CREATE TABLE IF NOT EXISTS growth_path_checkpoint_template (
  id BIGINT UNSIGNED NOT NULL,
  template_id BIGINT UNSIGNED NOT NULL COMMENT 'logical id of growth_path_template',
  step_no INT NOT NULL,
  phase_label VARCHAR(64) NOT NULL,
  checkpoint_title VARCHAR(180) NOT NULL,
  checkpoint_description TEXT DEFAULT NULL,
  default_offset_days INT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_growth_path_checkpoint_step (template_id, step_no),
  KEY idx_growth_path_checkpoint_template (template_id, status, step_no)
) ENGINE=InnoDB COMMENT='Growth path route checkpoint catalog';

/* =========================================================
   User plans and immutable version snapshots
   ========================================================= */

CREATE TABLE IF NOT EXISTS growth_path_plan (
  id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  plan_name VARCHAR(160) NOT NULL DEFAULT '我的成长路径',
  stage_code VARCHAR(32) NOT NULL,
  goal_code VARCHAR(64) NOT NULL,
  priority_code VARCHAR(32) NOT NULL DEFAULT 'certainty',
  weekly_hours TINYINT UNSIGNED NOT NULL DEFAULT 8,
  selected_template_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'logical id of selected route template',
  current_score TINYINT UNSIGNED DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'DRAFT, ACTIVE, PAUSED, COMPLETED, ARCHIVED',
  progress TINYINT UNSIGNED NOT NULL DEFAULT 0,
  start_date DATE DEFAULT NULL,
  target_date DATE DEFAULT NULL,
  summary TEXT DEFAULT NULL,
  next_review_at DATE DEFAULT NULL,
  last_reviewed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_growth_path_plan_user_status (user_id, status, updated_at),
  KEY idx_growth_path_plan_user_stage (user_id, stage_code, goal_code),
  KEY idx_growth_path_plan_review (user_id, next_review_at, status),
  KEY idx_growth_path_plan_template (selected_template_id)
) ENGINE=InnoDB COMMENT='User saved growth path plan';

CREATE TABLE IF NOT EXISTS growth_path_plan_version (
  id BIGINT UNSIGNED NOT NULL,
  plan_id BIGINT UNSIGNED NOT NULL COMMENT 'logical id of growth_path_plan',
  user_id BIGINT UNSIGNED NOT NULL,
  version_no INT NOT NULL,
  stage_code VARCHAR(32) NOT NULL,
  goal_code VARCHAR(64) NOT NULL,
  priority_code VARCHAR(32) NOT NULL,
  weekly_hours TINYINT UNSIGNED NOT NULL,
  selected_template_id BIGINT UNSIGNED DEFAULT NULL,
  score TINYINT UNSIGNED DEFAULT NULL,
  input_snapshot JSON DEFAULT NULL COMMENT 'simulation inputs at save time',
  result_snapshot JSON DEFAULT NULL COMMENT 'route result at save time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_growth_path_plan_version (plan_id, version_no),
  KEY idx_growth_path_version_user_time (user_id, created_at),
  KEY idx_growth_path_version_plan_time (plan_id, created_at)
) ENGINE=InnoDB COMMENT='Immutable growth path plan version';

CREATE TABLE IF NOT EXISTS growth_path_plan_checkpoint (
  id BIGINT UNSIGNED NOT NULL,
  plan_id BIGINT UNSIGNED NOT NULL COMMENT 'logical id of growth_path_plan',
  user_id BIGINT UNSIGNED NOT NULL,
  version_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'logical id of growth_path_plan_version',
  template_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'logical id of checkpoint template',
  step_no INT NOT NULL,
  phase_label VARCHAR(64) NOT NULL,
  checkpoint_title VARCHAR(180) NOT NULL,
  checkpoint_description TEXT DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'TODO' COMMENT 'TODO, DOING, DONE, SKIPPED',
  due_date DATE DEFAULT NULL,
  completed_at DATETIME DEFAULT NULL,
  evidence_url VARCHAR(512) DEFAULT NULL,
  review_note TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_growth_path_plan_checkpoint (plan_id, step_no),
  KEY idx_growth_path_plan_checkpoint_user_status (user_id, status, due_date),
  KEY idx_growth_path_plan_checkpoint_plan (plan_id, step_no),
  KEY idx_growth_path_plan_checkpoint_template (template_id)
) ENGINE=InnoDB COMMENT='User growth path action checkpoint';

/* =========================================================
   Initial route catalog
   ========================================================= */

INSERT INTO growth_path_template (
  id, stage_code, goal_code, path_code, path_name, tagline, preference_code,
  duration_label, risk_code, return_label, base_score, summary, strengths_json, status, sort_order
)
VALUES
  (900100001, 'high', 'all', 'steady', '稳健升学', '优先保证选择面和录取确定性', 'certainty', '18 - 24 个月', 'low', '确定性高', 82, '先锁定匹配度高的选科与院校梯度，再用稳定的复习节奏换取更多选择。', JSON_ARRAY('路径清晰', '容错空间大', '适合稳步提升'), 1, 10),
  (900100002, 'high', 'all', 'explore', '专业探索', '先验证兴趣，再决定投入方向', 'exploration', '12 - 20 个月', 'medium', '匹配度高', 78, '通过微项目、学科体验和真实信息访谈，减少只因热门或想象做决定的风险。', JSON_ARRAY('减少盲选', '更关注适配', '方便后续转向'), 1, 20),
  (900100003, 'high', 'all', 'sprint', '目标冲刺', '围绕一个高目标集中配置时间', 'growth', '10 - 18 个月', 'high', '上限更高', 75, '把有限时间集中到关键学科和目标院校，但必须保留清晰的退路与动态监测。', JSON_ARRAY('目标集中', '提升上限高', '反馈速度快'), 1, 30),
  (900200001, 'university', 'all', 'portfolio', '作品集优先', '用连续项目证明真实能力', 'growth', '2 - 4 个学期', 'medium', '就业信号强', 84, '围绕目标岗位选择课程和项目，让每个学期都留下可展示、可复盘的成果。', JSON_ARRAY('成果可见', '利于求职', '反馈及时'), 1, 10),
  (900200002, 'university', 'all', 'research', '深造积累', '围绕学术目标提高长期竞争力', 'certainty', '3 - 6 个学期', 'medium', '长期复利高', 80, '优先守住 GPA、课程先修关系和科研经历，再逐步收敛到研究方向。', JSON_ARRAY('基础扎实', '节奏可控', '适合长期积累'), 1, 20),
  (900200003, 'university', 'all', 'hybrid', '双轨探索', '同时保留就业和深造的转向空间', 'exploration', '2 - 5 个学期', 'high', '选择弹性大', 76, '用一条核心能力线同时服务于实习和深造，但要求更强的时间管理和取舍能力。', JSON_ARRAY('保留选择权', '降低误判', '适合尚在探索'), 1, 30),
  (900300001, 'career', 'all', 'depth', '深度增长', '在一个方向持续积累稀缺能力', 'certainty', '12 - 24 个月', 'low', '稳定复利', 83, '围绕岗位核心能力持续交付，并将成果沉淀为可验证的专业影响力。', JSON_ARRAY('方向稳定', '成果容易累积', '适合专业型人才'), 1, 10),
  (900300002, 'career', 'all', 'leadership', '影响力增长', '从个人交付走向协作和决策', 'growth', '12 - 30 个月', 'medium', '影响范围大', 79, '主动承担跨团队问题，把沟通、判断和带人能力转化为新的职业筹码。', JSON_ARRAY('影响面更广', '利于晋升', '能锻炼综合能力'), 1, 20),
  (900300003, 'career', 'all', 'transition', '转轨试验', '先用小成本验证，再决定是否切换', 'exploration', '6 - 18 个月', 'high', '上升空间大', 74, '利用副项目、内部协作和行业交流验证新方向，避免只凭想象裸辞转行。', JSON_ARRAY('降低转行成本', '更重视证据', '适合探索期'), 1, 30)
AS new ON DUPLICATE KEY UPDATE
  path_name = new.path_name,
  tagline = new.tagline,
  preference_code = new.preference_code,
  duration_label = new.duration_label,
  risk_code = new.risk_code,
  return_label = new.return_label,
  base_score = new.base_score,
  summary = new.summary,
  strengths_json = new.strengths_json,
  status = new.status,
  sort_order = new.sort_order,
  updated_at = CURRENT_TIMESTAMP;

INSERT INTO growth_path_checkpoint_template (
  id, template_id, step_no, phase_label, checkpoint_title, checkpoint_description, default_offset_days, status
)
VALUES
  (901100001, 900100001, 1, '本月', '完成目标专业反向筛选', '整理 3 个目标专业，核对选科要求与近三年位次。', 30, 1),
  (901100002, 900100001, 2, '3 个月内', '建立成绩提升闭环', '用错题和周测结果调整复习权重，每周复盘一次。', 90, 1),
  (901100003, 900100001, 3, '报考前', '完成冲稳保方案', '至少准备两套分数波动下仍然可执行的志愿方案。', 360, 1),
  (901200001, 900100002, 1, '本月', '完成 2 次方向体验', '分别体验目标方向的公开课、题目或小项目，留下真实感受。', 30, 1),
  (901200002, 900100002, 2, '6 个月内', '形成个人证据', '把成绩、作品和反馈整理成自己的专业适配证据。', 180, 1),
  (901200003, 900100002, 3, '报考前', '确定主方向与备选', '用兴趣、能力和录取概率三项指标共同决策。', 360, 1),
  (901300001, 900100003, 1, '本月', '确定单一主目标', '明确目标位次、差距和每周可投入的真实时间。', 30, 1),
  (901300002, 900100003, 2, '3 个月内', '验证提升速度', '用连续三次考试判断目标是否仍然值得继续冲刺。', 90, 1),
  (901300003, 900100003, 3, '报考前', '设置止损线', '提前写下转向条件，避免在信息变化后被沉没成本绑住。', 360, 1),
  (902100001, 900200001, 1, '本学期', '确定能力主线', '从课程树中挑出 2 个核心能力，绑定一个可展示项目。', 120, 1),
  (902100002, 900200001, 2, '下学期', '获得真实反馈', '参加竞赛、实习或开源协作，补充外部评价。', 240, 1),
  (902100003, 900200001, 3, '毕业前', '完成成果包装', '把项目、论文和课程成绩整理为简历和作品集版本。', 540, 1),
  (902200001, 900200002, 1, '本学期', '补齐基础课程', '检查毕业缺口与先修关系，避免后期被动补课。', 120, 1),
  (902200002, 900200002, 2, '1 年内', '进入研究场景', '完成一次导师交流或研究助理经历，验证方向适配度。', 365, 1),
  (902200003, 900200002, 3, '申请前', '整理学术证据', '统一管理论文、课程、推荐信和考试准备的截止时间。', 540, 1),
  (902300001, 900200003, 1, '本学期', '确定共同能力', '找到既能用于作品集，也能用于科研的核心主题。', 120, 1),
  (902300002, 900200003, 2, '1 年内', '完成一次双向验证', '分别获得岗位和导师的反馈，比较真实偏好。', 365, 1),
  (902300003, 900200003, 3, '大三后', '关闭一条支线', '根据证据而不是焦虑，正式选择就业或继续深造。', 540, 1),
  (903100001, 900300001, 1, '本季度', '明确核心指标', '找出岗位中最能体现价值的 1 - 2 个业务指标。', 90, 1),
  (903100002, 900300001, 2, '半年内', '形成标志性成果', '完成一个能被复用、传播或量化的代表项目。', 180, 1),
  (903100003, 900300001, 3, '年度复盘', '争取角色升级', '用成果、反馈和能力证据支持晋升或薪酬谈判。', 365, 1),
  (903200001, 900300002, 1, '本季度', '承担协作任务', '选择一个需要推动他人共同完成的目标，而不是只做个人交付。', 90, 1),
  (903200002, 900300002, 2, '半年内', '建立反馈机制', '固定收集上下游反馈，识别管理能力中的真实短板。', 180, 1),
  (903200003, 900300002, 3, '年度复盘', '验证管理意愿', '比较专家和管理路线的收益、压力与长期匹配度。', 365, 1),
  (903300001, 900300003, 1, '本月', '盘点可迁移能力', '把已有成果拆成方法、工具和业务理解三类证据。', 30, 1),
  (903300002, 900300003, 2, '3 个月内', '完成低成本试验', '通过副项目、公开作品或内部任务获得新方向反馈。', 90, 1),
  (903300003, 900300003, 3, '决定切换前', '计算真实代价', '对比收入、学习成本、城市和家庭因素后再做选择。', 180, 1)
AS new ON DUPLICATE KEY UPDATE
  phase_label = new.phase_label,
  checkpoint_title = new.checkpoint_title,
  checkpoint_description = new.checkpoint_description,
  default_offset_days = new.default_offset_days,
  status = new.status,
  updated_at = CURRENT_TIMESTAMP;

/* Optional verification queries for deployment review:
SELECT stage_code, COUNT(*) AS route_count
FROM growth_path_template
WHERE status = 1
GROUP BY stage_code;

SELECT COUNT(*) AS checkpoint_count
FROM growth_path_checkpoint_template
WHERE status = 1;
*/
