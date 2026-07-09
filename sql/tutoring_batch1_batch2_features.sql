-- Existing databases that already ran tutoring_features.sql can run this safely more than once.
SET NAMES utf8mb4;
USE `ry-cloud`;

DROP PROCEDURE IF EXISTS add_tutoring_column_if_missing;
DELIMITER //
CREATE PROCEDURE add_tutoring_column_if_missing(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64),
  IN p_column_sql text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = p_table_name
       AND column_name = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', REPLACE(p_table_name, '`', '``'), '` ADD COLUMN ', p_column_sql);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_tutoring_column_if_missing(
  'tutor_profile',
  'availability_text',
  '`availability_text` varchar(200) default '''' COMMENT ''可授课时间'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_match',
  'cancel_reason',
  '`cancel_reason` varchar(500) default '''' COMMENT ''取消原因'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_match',
  'reschedule_text',
  '`reschedule_text` varchar(200) default '''' COMMENT ''调整后时间'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_match',
  'trial_time',
  '`trial_time` datetime COMMENT ''试听课时间'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_match',
  'trial_remark',
  '`trial_remark` varchar(500) default '''' COMMENT ''试听备注'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_match',
  'trial_status',
  '`trial_status` char(1) default ''0'' COMMENT ''试听状态（0未安排 1待试听 2已完成）'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_lesson',
  'confirm_status',
  '`confirm_status` char(1) default ''0'' COMMENT ''确认状态（0待确认 1已确认）'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_lesson',
  'student_performance',
  '`student_performance` varchar(500) default '''' COMMENT ''课堂表现'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_lesson',
  'homework',
  '`homework` varchar(500) default '''' COMMENT ''课后作业'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_lesson',
  'next_plan',
  '`next_plan` varchar(500) default '''' COMMENT ''下节计划'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_lesson',
  'confirm_by',
  '`confirm_by` varchar(64) default '''' COMMENT ''确认人'''
);

CALL add_tutoring_column_if_missing(
  'tutoring_lesson',
  'confirm_time',
  '`confirm_time` datetime'
);

DROP PROCEDURE IF EXISTS add_tutoring_column_if_missing;

UPDATE tutoring_lesson SET confirm_status = '0' WHERE confirm_status IS NULL;
UPDATE tutoring_match SET trial_status = '0' WHERE trial_status IS NULL;

CREATE TABLE IF NOT EXISTS tutoring_complaint_log (
  log_id        bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '投诉记录ID',
  complaint_id  bigint(20)   NOT NULL COMMENT '投诉ID',
  action_text   varchar(500) NOT NULL COMMENT '处理记录',
  create_by     varchar(64)  default '',
  create_time   datetime,
  PRIMARY KEY (log_id),
  KEY idx_complaint_log (complaint_id, create_time)
) ENGINE=InnoDB COMMENT='投诉处理记录';

CREATE TABLE IF NOT EXISTS tutoring_learner (
  learner_id     bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '学员ID',
  user_id        bigint(20)   NOT NULL COMMENT '家长用户ID',
  learner_name   varchar(50)  NOT NULL COMMENT '学员姓名',
  grade          varchar(50)  NOT NULL COMMENT '年级',
  school         varchar(100) default '' COMMENT '学校',
  weak_subjects  varchar(200) default '' COMMENT '薄弱科目',
  target_score   varchar(100) default '' COMMENT '目标',
  available_time varchar(200) default '' COMMENT '可上课时间',
  remark         varchar(500) default '' COMMENT '备注',
  create_by      varchar(64)  default '',
  create_time    datetime,
  update_by      varchar(64)  default '',
  update_time    datetime,
  PRIMARY KEY (learner_id),
  KEY idx_learner_user (user_id, create_time)
) ENGINE=InnoDB COMMENT='学员档案';

CREATE TABLE IF NOT EXISTS tutor_availability (
  availability_id  bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '可授课时间ID',
  user_id          bigint(20)  NOT NULL COMMENT '教员用户ID',
  week_day         varchar(20) NOT NULL COMMENT '星期',
  start_time       time        NOT NULL COMMENT '开始时间',
  end_time         time        NOT NULL COMMENT '结束时间',
  available_status char(1)    default '1' COMMENT '状态（1可约 0不可约）',
  remark           varchar(200) default '' COMMENT '备注',
  create_by        varchar(64) default '',
  create_time      datetime,
  update_by        varchar(64) default '',
  update_time      datetime,
  PRIMARY KEY (availability_id),
  KEY idx_availability_user (user_id, week_day, start_time)
) ENGINE=InnoDB COMMENT='教员可授课日历';

CREATE TABLE IF NOT EXISTS tutoring_settlement (
  settlement_id bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '结算ID',
  lesson_id     bigint(20)    NOT NULL COMMENT '课时ID',
  match_id      bigint(20)    NOT NULL COMMENT '订单ID',
  tutor_id      bigint(20)    NOT NULL COMMENT '教员用户ID',
  amount        decimal(10,2) NOT NULL COMMENT '结算金额',
  status        char(1)       default '0' COMMENT '状态（0待结算 1已结算）',
  settle_by     varchar(64)   default '' COMMENT '结算人',
  settle_time   datetime,
  create_time   datetime,
  PRIMARY KEY (settlement_id),
  UNIQUE KEY uk_settlement_lesson (lesson_id),
  KEY idx_settlement_tutor (tutor_id, status, create_time)
) ENGINE=InnoDB COMMENT='课时费结算';

CREATE TABLE IF NOT EXISTS tutoring_ticket (
  ticket_id     bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  user_id       bigint(20)   NOT NULL COMMENT '提交用户ID',
  title         varchar(100) NOT NULL COMMENT '标题',
  content       varchar(500) NOT NULL COMMENT '问题描述',
  status        char(1)      default '0' COMMENT '状态（0待处理 1已解决 2已关闭）',
  handle_remark varchar(500) default '' COMMENT '处理意见',
  handle_by     varchar(64)  default '' COMMENT '处理人',
  create_by     varchar(64)  default '',
  create_time   datetime,
  update_time   datetime,
  PRIMARY KEY (ticket_id),
  KEY idx_ticket_status (status, create_time),
  KEY idx_ticket_user (user_id, create_time)
) ENGINE=InnoDB COMMENT='客服工单';

CREATE TABLE IF NOT EXISTS tutoring_announcement (
  announcement_id bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  title           varchar(100) NOT NULL COMMENT '标题',
  content         varchar(1000) NOT NULL COMMENT '内容',
  status          char(1)      default '0' COMMENT '状态（0草稿 1发布）',
  publish_time    datetime,
  create_by       varchar(64)  default '',
  create_time     datetime,
  update_by       varchar(64)  default '',
  update_time     datetime,
  PRIMARY KEY (announcement_id),
  KEY idx_announcement_status (status, publish_time)
) ENGINE=InnoDB COMMENT='平台公告';

CREATE TABLE IF NOT EXISTS tutoring_material (
  material_id bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '资料ID',
  match_id    bigint(20)   NOT NULL COMMENT '订单ID',
  uploader_id bigint(20)   NOT NULL COMMENT '上传用户ID',
  title       varchar(100) NOT NULL COMMENT '资料标题',
  file_url    varchar(500) NOT NULL COMMENT '资料链接',
  remark      varchar(500) default '' COMMENT '备注',
  create_by   varchar(64)  default '',
  create_time datetime,
  PRIMARY KEY (material_id),
  KEY idx_material_match (match_id, create_time)
) ENGINE=InnoDB COMMENT='课程资料';

CREATE TABLE IF NOT EXISTS tutoring_message (
  message_id  bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  match_id    bigint(20)   NOT NULL COMMENT '订单ID',
  sender_id   bigint(20)   NOT NULL COMMENT '发送用户ID',
  content     varchar(500) NOT NULL COMMENT '留言内容',
  create_by   varchar(64)  default '',
  create_time datetime,
  PRIMARY KEY (message_id),
  KEY idx_message_match (match_id, create_time)
) ENGINE=InnoDB COMMENT='订单沟通留言';

CREATE TABLE IF NOT EXISTS tutoring_payment (
  payment_id    bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '付款流水ID',
  match_id      bigint(20)    NOT NULL COMMENT '订单ID',
  payer_id      bigint(20)    NOT NULL COMMENT '付款用户ID',
  amount        decimal(10,2) NOT NULL COMMENT '付款金额',
  proof_url     varchar(500)  NOT NULL COMMENT '付款凭证',
  remark        varchar(500)  default '' COMMENT '付款备注',
  status        char(1)       default '0' COMMENT '状态（0待确认 1已确认 2已驳回）',
  handle_remark varchar(500)  default '' COMMENT '处理意见',
  handle_by     varchar(64)   default '' COMMENT '处理人',
  handle_time   datetime,
  create_by     varchar(64)   default '',
  create_time   datetime,
  PRIMARY KEY (payment_id),
  KEY idx_payment_match (match_id, create_time),
  KEY idx_payment_status (status, create_time)
) ENGINE=InnoDB COMMENT='订单付款流水';

CREATE TABLE IF NOT EXISTS tutoring_followup (
  followup_id bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '回访ID',
  match_id    bigint(20)   NOT NULL COMMENT '订单ID',
  content     varchar(500) NOT NULL COMMENT '回访内容',
  next_action varchar(500) default '' COMMENT '后续动作',
  status      char(1)      default '0' COMMENT '状态（0待跟进 1已完成）',
  create_by   varchar(64)  default '',
  create_time datetime,
  PRIMARY KEY (followup_id),
  KEY idx_followup_match (match_id, create_time),
  KEY idx_followup_status (status, create_time)
) ENGINE=InnoDB COMMENT='订单回访记录';
