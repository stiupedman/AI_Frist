-- 家教业务扩展功能（在 tutoring.sql 之后执行一次）
SET NAMES utf8mb4;
USE `ry-cloud`;

ALTER TABLE tutor_profile
  ADD COLUMN student_card_url varchar(500) default '' COMMENT '学生证图片',
  ADD COLUMN qualification_url varchar(500) default '' COMMENT '资格证图片';

CREATE TABLE tutoring_lesson (
  lesson_id       bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '上课记录ID',
  match_id        bigint(20)    NOT NULL COMMENT '订单ID',
  lesson_date     date          NOT NULL COMMENT '上课日期',
  hours           decimal(5,2)  NOT NULL COMMENT '课时数',
  content         varchar(500)  NOT NULL COMMENT '授课内容',
  amount          decimal(10,2) NOT NULL COMMENT '本次课时费',
  create_by       varchar(64)   default '',
  create_time     datetime,
  PRIMARY KEY (lesson_id),
  KEY idx_lesson_match (match_id, lesson_date)
) ENGINE=InnoDB COMMENT='上课记录';

CREATE TABLE tutoring_notification (
  notification_id bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  user_id          bigint(20)   NOT NULL COMMENT '接收用户ID',
  title            varchar(100) NOT NULL COMMENT '标题',
  content          varchar(500) NOT NULL COMMENT '内容',
  read_status      char(1)      default '0' COMMENT '是否已读（0否 1是）',
  create_time      datetime,
  PRIMARY KEY (notification_id),
  KEY idx_notification_user (user_id, read_status, create_time)
) ENGINE=InnoDB COMMENT='家教业务通知';

CREATE TABLE tutoring_complaint (
  complaint_id    bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
  match_id        bigint(20)   NOT NULL COMMENT '订单ID',
  complainant_id  bigint(20)   NOT NULL COMMENT '投诉人用户ID',
  reason          varchar(500) NOT NULL COMMENT '投诉原因',
  status          char(1)      default '0' COMMENT '状态（0待处理 1已解决 2已驳回）',
  handle_remark   varchar(500) default '' COMMENT '处理意见',
  handle_by       varchar(64)  default '' COMMENT '处理人',
  create_time     datetime,
  update_time     datetime,
  PRIMARY KEY (complaint_id),
  UNIQUE KEY uk_complaint_match_user (match_id, complainant_id),
  KEY idx_complaint_status (status, create_time)
) ENGINE=InnoDB COMMENT='订单投诉';

CREATE TABLE tutor_favorite (
  favorite_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  user_id     bigint(20) NOT NULL COMMENT '收藏人用户ID',
  tutor_id    bigint(20) NOT NULL COMMENT '教员用户ID',
  create_time datetime,
  PRIMARY KEY (favorite_id),
  UNIQUE KEY uk_favorite_user_tutor (user_id, tutor_id)
) ENGINE=InnoDB COMMENT='收藏教员';

CREATE TABLE tutoring_invitation (
  invitation_id  bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  publisher_id   bigint(20)    NOT NULL COMMENT '家长用户ID',
  tutor_id       bigint(20)    NOT NULL COMMENT '教员用户ID',
  learner_grade  varchar(50)   NOT NULL COMMENT '学员年级',
  subject        varchar(50)   NOT NULL COMMENT '辅导科目',
  area           varchar(100)  NOT NULL COMMENT '授课区域',
  schedule_text  varchar(200)  NOT NULL COMMENT '期望时间',
  offered_rate   decimal(10,2) NOT NULL COMMENT '预约课时费',
  message        varchar(500)  default '' COMMENT '预约说明',
  status         char(1)       default '0' COMMENT '状态（0待处理 1已接受 2已拒绝）',
  create_by      varchar(64)   default '',
  create_time    datetime,
  update_by      varchar(64)   default '',
  update_time    datetime,
  PRIMARY KEY (invitation_id),
  KEY idx_invitation_tutor (tutor_id, status, create_time),
  KEY idx_invitation_publisher (publisher_id, create_time)
) ENGINE=InnoDB COMMENT='家教预约邀请';

INSERT IGNORE INTO sys_menu VALUES
  (2011, '查找教员', 2001, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:tutor:list', '#', 'admin', sysdate(), '', null, ''),
  (2012, '业务监管', 2001, 11, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:business:monitor', '#', 'admin', sysdate(), '', null, '');

DELETE FROM sys_role_menu WHERE role_id = 101 AND menu_id = 2004;
INSERT IGNORE INTO sys_role_menu VALUES (101, 2011);
