-- 家教业务扩展功能（在 tutoring.sql 之后执行一次）
SET NAMES utf8mb4;
USE `ry-cloud`;

ALTER TABLE tutor_profile
  ADD COLUMN student_card_url varchar(500) default '' COMMENT '学生证图片',
  ADD COLUMN qualification_url varchar(500) default '' COMMENT '资格证图片',
  ADD COLUMN availability_text varchar(200) default '' COMMENT '可授课时间';

ALTER TABLE tutoring_match
  ADD COLUMN cancel_reason varchar(500) default '' COMMENT '取消原因',
  ADD COLUMN reschedule_text varchar(200) default '' COMMENT '调整后时间',
  ADD COLUMN trial_time datetime COMMENT '试听课时间',
  ADD COLUMN trial_remark varchar(500) default '' COMMENT '试听备注',
  ADD COLUMN trial_status char(1) default '0' COMMENT '试听状态（0未安排 1待试听 2已完成）';

ALTER TABLE tutoring_request
  ADD COLUMN source_channel varchar(50) default '平台发布' COMMENT '来源渠道';

CREATE TABLE tutoring_lesson (
  lesson_id       bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '上课记录ID',
  match_id        bigint(20)    NOT NULL COMMENT '订单ID',
  lesson_date     date          NOT NULL COMMENT '上课日期',
  start_time      time          NULL COMMENT '开始时间',
  end_time        time          NULL COMMENT '结束时间',
  hours           decimal(5,2)  NOT NULL COMMENT '课时数',
  content         varchar(500)  NOT NULL COMMENT '授课内容',
  amount          decimal(10,2) NOT NULL COMMENT '本次课时费',
  student_performance varchar(500) default '' COMMENT '课堂表现',
  homework       varchar(500)  default '' COMMENT '课后作业',
  next_plan      varchar(500)  default '' COMMENT '下节计划',
  attendance_status char(1)    default '1' COMMENT '出勤状态（1到课 2请假 3缺勤）',
  phase_feedback varchar(500)  default '' COMMENT '阶段反馈',
  confirm_status  char(1)       default '0' COMMENT '确认状态（0待确认 1已确认）',
  confirm_by      varchar(64)   default '' COMMENT '确认人',
  confirm_time    datetime,
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
  channel          varchar(20)  default 'site' COMMENT '渠道',
  template_code    varchar(50)  default 'system' COMMENT '模板',
  send_status      char(1)      default '0' COMMENT '发送状态（0待发送 1已发送）',
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

CREATE TABLE tutoring_complaint_log (
  log_id       bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '投诉记录ID',
  complaint_id bigint(20)   NOT NULL COMMENT '投诉ID',
  action_text  varchar(500) NOT NULL COMMENT '处理记录',
  create_by    varchar(64)  default '',
  create_time  datetime,
  PRIMARY KEY (log_id),
  KEY idx_complaint_log (complaint_id, create_time)
) ENGINE=InnoDB COMMENT='投诉处理记录';

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

CREATE TABLE tutoring_learner (
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

CREATE TABLE tutor_availability (
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

CREATE TABLE tutoring_settlement (
  settlement_id bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '结算ID',
  lesson_id     bigint(20)    NOT NULL COMMENT '课时ID',
  match_id      bigint(20)    NOT NULL COMMENT '订单ID',
  tutor_id      bigint(20)    NOT NULL COMMENT '教员用户ID',
  amount        decimal(10,2) NOT NULL COMMENT '结算金额',
  platform_fee  decimal(10,2) default 0 COMMENT '平台抽成',
  net_amount    decimal(10,2) default 0 COMMENT '教员净收入',
  status        char(1)       default '0' COMMENT '状态（0待结算 1已结算）',
  settle_by     varchar(64)   default '' COMMENT '结算人',
  settle_time   datetime,
  create_time   datetime,
  PRIMARY KEY (settlement_id),
  UNIQUE KEY uk_settlement_lesson (lesson_id),
  KEY idx_settlement_tutor (tutor_id, status, create_time)
) ENGINE=InnoDB COMMENT='课时费结算';

CREATE TABLE tutoring_ticket (
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

CREATE TABLE tutoring_announcement (
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

CREATE TABLE tutoring_material (
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

CREATE TABLE tutoring_message (
  message_id  bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  match_id    bigint(20)   NOT NULL COMMENT '订单ID',
  sender_id   bigint(20)   NOT NULL COMMENT '发送用户ID',
  content     varchar(500) NOT NULL COMMENT '留言内容',
  create_by   varchar(64)  default '',
  create_time datetime,
  PRIMARY KEY (message_id),
  KEY idx_message_match (match_id, create_time)
) ENGINE=InnoDB COMMENT='订单沟通留言';

CREATE TABLE tutoring_payment (
  payment_id    bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '付款流水ID',
  match_id      bigint(20)    NOT NULL COMMENT '订单ID',
  payer_id      bigint(20)    NOT NULL COMMENT '付款用户ID',
  amount        decimal(10,2) NOT NULL COMMENT '付款金额',
  platform_fee  decimal(10,2) default 0 COMMENT '平台抽成',
  refund_amount decimal(10,2) default 0 COMMENT '退款金额',
  proof_url     varchar(500)  NOT NULL COMMENT '付款凭证',
  pay_method    varchar(50)   default '' COMMENT '支付方式',
  trade_no      varchar(100)  default '' COMMENT '交易号',
  invoice_no    varchar(100)  default '' COMMENT '发票号',
  receipt_no    varchar(100)  default '' COMMENT '收据号',
  reconciled_status char(1)   default '0' COMMENT '对账状态',
  remark        varchar(500)  default '' COMMENT '付款备注',
  status        char(1)       default '0' COMMENT '状态（0待确认 1已确认 2已驳回 3已退款）',
  handle_remark varchar(500)  default '' COMMENT '处理意见',
  handle_by     varchar(64)   default '' COMMENT '处理人',
  handle_time   datetime,
  create_by     varchar(64)   default '',
  create_time   datetime,
  PRIMARY KEY (payment_id),
  KEY idx_payment_match (match_id, create_time),
  KEY idx_payment_status (status, create_time)
) ENGINE=InnoDB COMMENT='订单付款流水';

CREATE TABLE tutoring_followup (
  followup_id bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '回访ID',
  match_id    bigint(20)   NOT NULL COMMENT '订单ID',
  content     varchar(500) NOT NULL COMMENT '回访内容',
  next_action varchar(500) default '' COMMENT '后续动作',
  next_time   datetime COMMENT '下次跟进时间',
  status      char(1)      default '0' COMMENT '状态（0待跟进 1已完成）',
  create_by   varchar(64)  default '',
  create_time datetime,
  update_by   varchar(64)  default '',
  update_time datetime,
  PRIMARY KEY (followup_id),
  KEY idx_followup_match (match_id, create_time),
  KEY idx_followup_status (status, create_time)
) ENGINE=InnoDB COMMENT='订单回访记录';

CREATE TABLE tutoring_homework (
  homework_id bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  match_id    bigint(20)   NOT NULL COMMENT '订单ID',
  lesson_id   bigint(20)   default NULL COMMENT '课时ID',
  title       varchar(100) NOT NULL COMMENT '作业标题',
  content     varchar(1000) NOT NULL COMMENT '作业内容',
  submit_text varchar(1000) default '' COMMENT '提交内容',
  feedback    varchar(1000) default '' COMMENT '教员反馈',
  status      char(1)      default '0' COMMENT '状态（0待提交 1待反馈 2已反馈）',
  assign_by   varchar(64)  default '' COMMENT '布置人',
  submit_by   varchar(64)  default '' COMMENT '提交人',
  submit_time datetime,
  check_by    varchar(64)  default '' COMMENT '反馈人',
  check_time  datetime,
  create_by   varchar(64)  default '',
  create_time datetime,
  PRIMARY KEY (homework_id),
  KEY idx_homework_match (match_id, status, create_time)
) ENGINE=InnoDB COMMENT='课后作业';

CREATE TABLE tutoring_blacklist (
  blacklist_id bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
  user_id      bigint(20)   NOT NULL COMMENT '用户ID',
  reason       varchar(500) NOT NULL COMMENT '限制原因',
  status       char(1)      default '1' COMMENT '状态（1生效 0停用）',
  handle_by    varchar(64)  default '' COMMENT '停用人',
  handle_time  datetime,
  create_by    varchar(64)  default '',
  create_time  datetime,
  PRIMARY KEY (blacklist_id),
  UNIQUE KEY uk_blacklist_user (user_id),
  KEY idx_blacklist_status (status, create_time)
) ENGINE=InnoDB COMMENT='家教风控黑名单';

CREATE TABLE tutoring_finance_ledger (
  ledger_id  bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '财务流水ID',
  biz_type   varchar(30)   NOT NULL COMMENT '业务类型',
  biz_id     bigint(20)    NOT NULL COMMENT '业务ID',
  match_id   bigint(20)    default NULL COMMENT '订单ID',
  user_id    bigint(20)    default NULL COMMENT '相关用户ID',
  amount     decimal(10,2) default 0 COMMENT '金额',
  direction  varchar(10)   default 'none' COMMENT '方向（in/out/none）',
  status     varchar(30)   default '' COMMENT '状态',
  remark     varchar(500)  default '' COMMENT '备注',
  create_by  varchar(64)   default '',
  create_time datetime,
  PRIMARY KEY (ledger_id),
  KEY idx_finance_ledger_match (match_id, create_time),
  KEY idx_finance_ledger_biz (biz_type, biz_id)
) ENGINE=InnoDB COMMENT='财务业务流水';

INSERT INTO sys_menu VALUES
  (2011, '查找教员', 2001, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:tutor:list', '#', 'admin', sysdate(), '', null, ''),
  (2012, '业务监管', 2001, 11, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:business:monitor', '#', 'admin', sysdate(), '', null, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  `query` = VALUES(`query`),
  route_name = VALUES(route_name),
  is_frame = VALUES(is_frame),
  is_cache = VALUES(is_cache),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  remark = VALUES(remark);

DELETE FROM sys_role_menu WHERE role_id = 101 AND menu_id = 2004;
INSERT IGNORE INTO sys_role_menu VALUES (101, 2011);
