-- 大学生家教管理系统最小业务模型
-- 先执行 ry_20260417.sql，再执行本文件。演示账号密码均为 admin123。

SET NAMES utf8mb4;

use `ry-cloud`;

drop table if exists tutoring_match;
drop table if exists tutoring_request;
drop table if exists tutor_profile;

create table tutor_profile (
  profile_id        bigint(20)      not null auto_increment comment '教员资料ID',
  user_id           bigint(20)      not null                comment '系统用户ID',
  university        varchar(100)    not null                comment '学校',
  major             varchar(100)    not null                comment '专业',
  college_year      varchar(30)     not null                comment '年级',
  subjects          varchar(200)    not null                comment '擅长科目',
  hourly_rate       decimal(10,2)   not null                comment '期望课时费',
  introduction      varchar(500)    default ''              comment '个人简介',
  verify_status     char(1)         default '0'             comment '审核状态（0待审核 1通过 2驳回）',
  verify_remark     varchar(255)    default ''              comment '审核意见',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  primary key (profile_id),
  unique key uk_tutor_profile_user (user_id)
) engine=innodb comment='大学生教员资料';

create table tutoring_request (
  request_id        bigint(20)      not null auto_increment comment '需求ID',
  publisher_id      bigint(20)      not null                comment '发布人用户ID',
  learner_grade     varchar(50)     not null                comment '学员年级',
  subject           varchar(50)     not null                comment '辅导科目',
  area              varchar(100)    not null                comment '授课区域',
  schedule_text     varchar(200)    not null                comment '期望时间',
  hourly_budget     decimal(10,2)   not null                comment '课时预算',
  requirement_text  varchar(500)    default ''              comment '补充要求',
  status            char(1)         default '0'             comment '状态（0招募中 1已匹配 2已完成 3已取消）',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  primary key (request_id),
  key idx_tutoring_request_status (status, create_time),
  key idx_tutoring_request_publisher (publisher_id)
) engine=innodb comment='家教需求';

create table tutoring_match (
  match_id          bigint(20)      not null auto_increment comment '匹配ID',
  request_id        bigint(20)      not null                comment '需求ID',
  tutor_id          bigint(20)      not null                comment '教员用户ID',
  quoted_rate       decimal(10,2)   not null                comment '教员报价',
  application_text  varchar(500)    default ''              comment '申请说明',
  status            char(1)         default '0'             comment '状态（0申请中 1已接单 2已完成 3未选中 4已取消）',
  rating            tinyint(1)      default null            comment '评分（1-5）',
  review_text       varchar(500)    default ''              comment '评价',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  primary key (match_id),
  unique key uk_tutoring_match_request_tutor (request_id, tutor_id),
  key idx_tutoring_match_tutor (tutor_id, status)
) engine=innodb comment='家教申请与订单';

-- 角色
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly,
  dept_check_strictly, status, del_flag, create_by, create_time, remark)
values
  (100, '大学生教员', 'tutor', 3, '1', 1, 1, '0', '0', 'admin', sysdate(), '家教教员'),
  (101, '家长学员', 'client', 4, '1', 1, 1, '0', '0', 'admin', sysdate(), '家教需求方');

-- 家教工作台及按钮权限
insert into sys_menu values
  (2000, '家教管理', 0, 1, 'tutoring', null, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', null, '家教业务目录'),
  (2001, '家教工作台', 2000, 1, 'workbench', 'tutoring/index', '', '', 1, 0, 'C', '0', '0', 'tutoring:workbench:view', 'dashboard', 'admin', sysdate(), '', null, '家教业务工作台'),
  (2002, '维护教员资料', 2001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:profile:edit', '#', 'admin', sysdate(), '', null, ''),
  (2003, '审核教员资料', 2001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:profile:verify', '#', 'admin', sysdate(), '', null, ''),
  (2004, '查看家教需求', 2001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:request:list', '#', 'admin', sysdate(), '', null, ''),
  (2005, '发布家教需求', 2001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:request:add', '#', 'admin', sysdate(), '', null, ''),
  (2006, '查看家教订单', 2001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:match:list', '#', 'admin', sysdate(), '', null, ''),
  (2007, '申请家教需求', 2001, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:match:apply', '#', 'admin', sysdate(), '', null, ''),
  (2008, '接受教员申请', 2001, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:match:accept', '#', 'admin', sysdate(), '', null, ''),
  (2009, '完成家教订单', 2001, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:match:complete', '#', 'admin', sysdate(), '', null, ''),
  (2010, '评价家教订单', 2001, 9, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:match:review', '#', 'admin', sysdate(), '', null, ''),
  (2011, '查找教员', 2001, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:tutor:list', '#', 'admin', sysdate(), '', null, ''),
  (2012, '业务监管', 2001, 11, '', '', '', '', 1, 0, 'F', '0', '0', 'tutoring:business:monitor', '#', 'admin', sysdate(), '', null, '');

-- 教员：目录、工作台、资料、需求列表、订单列表、申请、完成
insert into sys_role_menu values
  (100, 2000), (100, 2001), (100, 2002), (100, 2004),
  (100, 2006), (100, 2007), (100, 2009);

-- 家长：目录、工作台、找教员、发布、订单列表、接受、评价
insert into sys_role_menu values
  (101, 2000), (101, 2001), (101, 2011), (101, 2005),
  (101, 2006), (101, 2008), (101, 2010);

-- 演示账号
insert into sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber,
  sex, avatar, password, status, del_flag, create_by, create_time, remark)
values
  (100, 105, 'tutor_demo', '张同学', '00', 'tutor@example.com', '13800000001', '0', '',
   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', sysdate(), '演示教员'),
  (101, 105, 'client_demo', '李家长', '00', 'client@example.com', '13800000002', '1', '',
   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', sysdate(), '演示家长');

insert into sys_user_role values (100, 100), (101, 101);

insert into tutor_profile (user_id, university, major, college_year, subjects, hourly_rate,
  introduction, verify_status, verify_remark, create_by, create_time)
values (100, '示例大学', '数学与应用数学', '大三', '数学,物理', 80.00,
  '耐心负责，可辅导初高中数学。', '1', '演示资料自动通过', 'admin', sysdate());

insert into tutoring_request (publisher_id, learner_grade, subject, area, schedule_text,
  hourly_budget, requirement_text, status, create_by, create_time)
values (101, '初二', '数学', '大学城附近', '周六下午', 100.00,
  '希望重点辅导几何和函数。', '0', 'client_demo', sysdate());

-- 本机已有 MySQL 占用 3306，Java 服务通过宿主机 3307 访问容器 MySQL。
use `ry-config`;
update config_info
set content = replace(content, 'localhost:3306/ry-cloud', 'localhost:3307/ry-cloud')
where data_id in ('ruoyi-system-dev.yml', 'ruoyi-gen-dev.yml', 'ruoyi-job-dev.yml');
