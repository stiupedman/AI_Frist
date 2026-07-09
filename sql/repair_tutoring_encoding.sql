SET NAMES utf8mb4;
USE `ry-cloud`;

START TRANSACTION;

UPDATE sys_role SET role_name = '大学生教员', role_key = 'tutor', remark = '家教教员' WHERE role_id = 100;
UPDATE sys_role SET role_name = '家长学员', role_key = 'client', remark = '家教需求方' WHERE role_id = 101;

UPDATE sys_menu SET menu_name = '家教管理', remark = '家教业务目录' WHERE menu_id = 2000;
UPDATE sys_menu SET menu_name = '家教工作台', remark = '家教业务工作台' WHERE menu_id = 2001;
UPDATE sys_menu SET menu_name = '维护教员资料' WHERE menu_id = 2002;
UPDATE sys_menu SET menu_name = '审核教员资料' WHERE menu_id = 2003;
UPDATE sys_menu SET menu_name = '查看家教需求' WHERE menu_id = 2004;
UPDATE sys_menu SET menu_name = '发布家教需求' WHERE menu_id = 2005;
UPDATE sys_menu SET menu_name = '查看家教订单' WHERE menu_id = 2006;
UPDATE sys_menu SET menu_name = '申请家教需求' WHERE menu_id = 2007;
UPDATE sys_menu SET menu_name = '接受教员申请' WHERE menu_id = 2008;
UPDATE sys_menu SET menu_name = '完成家教订单' WHERE menu_id = 2009;
UPDATE sys_menu SET menu_name = '评价家教订单' WHERE menu_id = 2010;

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

UPDATE sys_user SET nick_name = '张同学', remark = '演示教员' WHERE user_id = 100;
UPDATE sys_user SET nick_name = '李家长', remark = '演示家长' WHERE user_id = 101;

UPDATE tutor_profile
SET university = '示例大学', major = '数学与应用数学', college_year = '大三',
    subjects = '数学,物理', introduction = '耐心负责，可辅导初高中数学。',
    verify_remark = '演示资料自动通过'
WHERE profile_id = 1 AND user_id = 100;

UPDATE tutoring_request
SET learner_grade = '初二', subject = '数学', area = '大学城附近',
    schedule_text = '周六下午', requirement_text = '希望重点辅导几何和函数。'
WHERE request_id = 1 AND publisher_id = 101 AND create_by = 'client_demo';

COMMIT;
