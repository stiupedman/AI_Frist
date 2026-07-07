# 家教业务链与管理员监管设计

## 目标

补齐大学生教员和家长学员之间的一套完整业务闭环，并在家教工作台内补齐管理员对两类用户信息和业务信息的集中监管能力。

现有系统已经具备主链路：教员维护资料、管理员审核、家长发布需求、教员申请、家长接受、教员记录课时并完成、家长评价、双方投诉、家长查找/收藏/预约教员、消息通知。本次不重写这些动作，只补齐管理员能看全量人员与业务明细的缺口，并修正权限初始化脚本。

## 范围

采用方案 A：在现有 `tutoring/index.vue` 工作台中扩展管理员页签，后端新增家教域只读查询接口。系统用户的新增、编辑、停用、授权继续复用若依系统管理，不在家教工作台重做一套账号 CRUD。

## 角色业务链

### 大学生教员

- 维护教员资料并提交审核。
- 审核通过后查看开放需求和智能推荐需求。
- 对需求发起申请，可撤回未处理申请。
- 接收家长直接预约，可接受或拒绝。
- 接单后添加课时记录，完成授课订单。
- 查看消息通知、订单、投诉处理结果。
- 对进行中或已完成订单提交投诉。

### 家长学员

- 发布家教需求并查看自己的需求。
- 浏览已审核教员，按科目、学校筛选。
- 查看教员公开资料、收藏教员、发起预约。
- 查看申请或预约形成的订单，接受教员申请。
- 查看课时记录，订单完成后评价。
- 查看消息通知、投诉处理结果。
- 对进行中或已完成订单提交投诉。

### 管理员

- 审核大学生教员资料。
- 查看家教数据看板。
- 查看家长学员信息：账号、昵称、联系方式、状态、需求数、订单数、投诉数。
- 查看大学生教员信息：账号、昵称、联系方式、状态、资料审核状态、学校、专业、科目、评分、完成订单数。
- 查看全平台需求、订单、预约邀请、课时记录。
- 查看和处理投诉。
- 管理视图只读业务数据，不直接改需求、订单、预约和课时状态。

## 前端设计

继续使用 `ruoyi-ui/src/views/tutoring/roleConfig.cjs` 作为唯一角色配置入口。

管理员配置新增页签：

- `businessClients`：家长管理。
- `businessTutors`：教员管理。
- `businessRequests`：需求监管，已存在。
- `businessMatches`：订单监管，已存在。
- `businessInvitations`：预约监管。
- `businessLessons`：课时监管。
- `verify`：教员审核，已存在。
- `complaints`：投诉处理，已存在。
- `dashboard`：数据看板，已存在。

`ruoyi-ui/src/views/tutoring/index.vue` 继续按 `tabVisible` 和 `actionVisible` 渲染。新增管理员表格复用现有 Element UI 表格、筛选表单和 `loadCurrentTab` 加载器，不拆独立页面，不新增依赖。

## 后端设计

在 `TutoringController` 下新增只读接口，全部使用 `tutoring:business:monitor`：

- `GET /tutoring/admin/clients`
- `GET /tutoring/admin/tutors`
- `GET /tutoring/admin/invitations`
- `GET /tutoring/admin/lessons`

已有接口继续保留：

- `GET /tutoring/admin/requests`
- `GET /tutoring/admin/matches`
- `GET /tutoring/dashboard`
- `GET /tutoring/complaints`
- `GET /tutoring/profiles/pending`

Service 只做查询转发和必要脱敏。家长/教员管理返回账号和业务统计，不返回密码、学生证、资格证等敏感字段。教员公开信息仍按现有 `getVerifiedTutors` 脱敏。

Mapper 新增四个查询：

- `selectAdminClients(SysUser query)`：按家长角色 `client` 查用户，附需求数、订单数、投诉数。
- `selectAdminTutors(TutorProfile query)`：按教员角色 `tutor` 查用户和资料，附评分、完成订单数。
- `selectAllInvitations(TutoringInvitation query)`：全量预约邀请，可按科目、状态筛选。
- `selectAllLessons(TutoringLesson query)`：全量课时记录，可按科目、教员、家长筛选。

为了避免新增 DTO 文件，优先复用已有 domain：`SysUser` 使用 `remark` 承载简短统计文本；`TutorProfile` 复用已有统计字段；邀请和课时使用现有 domain 补充已有映射字段。若现有 domain 缺少必要展示字段，只在同一 domain 中加最少字段。

## SQL 与权限

初始化和修复脚本都需要确保：

- 家长角色 `role_key = 'client'`。
- 教员角色 `role_key = 'tutor'`。
- 菜单权限 `tutoring:tutor:list` 可用并授予家长。
- 菜单权限 `tutoring:business:monitor` 可用。管理员通过超级管理员权限访问；如有非超级管理员角色，需要显式授予。

## 验证标准

- 家长能完成“找教员 -> 收藏或预约 -> 查看预约/订单 -> 评价/投诉”的链路。
- 教员能完成“资料审核通过 -> 找需求/收预约 -> 申请或接受 -> 记录课时 -> 完成订单 -> 查看评价/投诉”的链路。
- 管理员能看到家长、教员、需求、订单、预约、课时、投诉和看板。
- 管理员页签没有业务写按钮；投诉处理和教员审核保留现有操作。
- 三种角色的页签自检通过。
- SQL 权限自检通过。
- 后端编译通过；前端生产构建尽量验证，若环境缺依赖则报告具体阻塞。

## 不做

- 不重做系统用户新增、编辑、停用、授权。
- 不增加支付、聊天、排课日历、自动结算。
- 不拆分三个工作台页面。
- 不新增依赖。
