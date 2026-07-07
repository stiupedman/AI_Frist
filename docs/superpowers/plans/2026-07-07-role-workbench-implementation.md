# 三角色家教工作台 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按管理员、大学生教员、家长学员三种角色精确显示家教工作台按钮和页签，并补齐管理员监管、家长找教员、未认证教员锁定。

**Architecture:** 前端增加一个纯角色配置文件，`index.vue` 只按配置渲染按钮、页签、行内操作并按当前页签加载数据。后端只增加三个只读查询接口：家长查已认证教员、管理员查全平台需求、管理员查全平台订单。

**Tech Stack:** Vue 2 + Element UI + 若依 request；Spring Boot + MyBatis XML；MySQL 初始化 SQL；Node assert 自检。

## Global Constraints

- 不新增依赖。
- 管理员不能因 `*:*:*` 看到家长/教员按钮。
- 未认证教员只能维护资料和查看审核状态。
- 家长不显示“开放需求”，新增“找教员”。
- 后端仍做权限与数据归属校验。

---

### Task 1: 角色配置和自检

**Files:**
- Create: `ruoyi-ui/src/views/tutoring/roleConfig.cjs`
- Create: `ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

**Interfaces:**
- Produces: `resolveWorkbenchRole(roles: string[]): 'admin'|'tutor'|'client'|''`
- Produces: `getWorkbenchConfig(role: string, options?: { tutorVerified?: boolean }): { defaultTab: string, toolbar: string[], tabs: string[], actions: Record<string,string[]> }`

- [ ] **Step 1: Write failing test**

```js
const assert = require('assert')
const { resolveWorkbenchRole, getWorkbenchConfig } = require('./roleConfig.cjs')

assert.strictEqual(resolveWorkbenchRole(['client', 'admin']), 'admin')
assert.deepStrictEqual(getWorkbenchConfig('admin').toolbar, ['refresh'])
assert.ok(getWorkbenchConfig('admin').tabs.includes('dashboard'))
assert.ok(!getWorkbenchConfig('admin').tabs.includes('open'))
assert.ok(getWorkbenchConfig('client').tabs.includes('tutors'))
assert.ok(!getWorkbenchConfig('client').tabs.includes('open'))
assert.ok(getWorkbenchConfig('tutor', { tutorVerified: true }).tabs.includes('open'))
assert.deepStrictEqual(getWorkbenchConfig('tutor', { tutorVerified: false }).tabs, ['profile'])
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`
Expected: FAIL with module not found.

- [ ] **Step 3: Write minimal implementation**

Create role config with the three role tab/button/action lists and tutor locked override.

- [ ] **Step 4: Run test to verify it passes**

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`
Expected: PASS.

### Task 2: 后端只读查询接口

**Files:**
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringController.java`
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringService.java`
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/mapper/TutoringMapper.java`
- Modify: `ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/TutoringMapper.xml`
- Modify: `ruoyi-ui/src/api/tutoring.js`

**Interfaces:**
- Produces: `GET /system/tutoring/tutors`
- Produces: `GET /system/tutoring/admin/requests`
- Produces: `GET /system/tutoring/admin/matches`

- [ ] **Step 1: Add mapper/service/controller declarations**

Add `getVerifiedTutors(TutorProfile query)`, `getAdminRequests(TutoringRequest query)`, `getAdminMatches(TutoringMatch query)` and matching MyBatis selects.

- [ ] **Step 2: Add frontend API wrappers**

Add `listVerifiedTutors(params)`, `listAdminRequests(params)`, `listAdminMatches(params)`.

- [ ] **Step 3: Verify compile surface**

Run: `mvn -pl ruoyi-modules/ruoyi-system -am -DskipTests compile`
Expected: BUILD SUCCESS or report dependency/environment blocker.

### Task 3: 前端工作台按配置渲染

**Files:**
- Modify: `ruoyi-ui/src/views/tutoring/index.vue`

**Interfaces:**
- Consumes: `resolveWorkbenchRole`, `getWorkbenchConfig`
- Consumes: new API wrappers from Task 2

- [ ] **Step 1: Import role config and APIs**

Use Vuex `roles` to resolve role; keep permission checks only as backend button guard.

- [ ] **Step 2: Replace broad permission-only visibility**

Tabs/buttons use `tabVisible(name)` and `actionVisible(name, action)`; admin wildcard no longer exposes unrelated UI.

- [ ] **Step 3: Load active tab only**

Replace `loadAll()` broad preload with `loadCurrentTab()` and `tabLoaders`.

- [ ] **Step 4: Add missing UI tables**

Add `找教员` table for parents and read-only `业务监管` tables for admins.

- [ ] **Step 5: Re-run role config check**

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`
Expected: PASS.

### Task 4: SQL permission update and final verification

**Files:**
- Modify: `sql/tutoring.sql`
- Modify: `sql/tutoring_features.sql`

**Interfaces:**
- Adds permission: `tutoring:tutor:list`
- Adds permission: `tutoring:business:monitor`

- [ ] **Step 1: Add menu permissions**

Add “查找教员” and “业务监管”；client role gets `tutoring:tutor:list` and no longer needs `tutoring:request:list`.

- [ ] **Step 2: Run final checks**

Run:
`node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`
`mvn -pl ruoyi-modules/ruoyi-system -am -DskipTests compile`
`npm --prefix ruoyi-ui run build:prod`

Expected: role check PASS; backend/frontend builds PASS or report exact environment blocker.
