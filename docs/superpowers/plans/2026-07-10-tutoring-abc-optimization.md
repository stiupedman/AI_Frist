# 家教平台 ABC 优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 A（上线稳定性）、B（使用体验）、C（代码结构）完成家教平台最小业务闭环的成品化加固。

**Architecture:** 保留现有 RuoYi Cloud、Spring、MyBatis、Vue 2 和 Element UI 技术栈。先以数据库条件更新、事务和后端金额规则加固业务，再复用现有 UI 做响应式与提交防重，最后只拆管理员 HTTP 适配层和纯视图逻辑；财务方法暂留原服务，避免复制订单归属、通知和流水实现。

**Tech Stack:** Java 17、Spring Boot、MyBatis、MySQL、Vue 2.6、Element UI 2.15、Node.js `assert`、Maven。

## Global Constraints

- 保留模拟支付，不接入第三方支付、短信、微信、邮件、原生 App 或小程序。
- 不新增 Maven、npm 或测试依赖。
- 不改变现有 `/system/tutoring/**` 前端请求路径、后端 `/tutoring/**` 路径和数据库字段含义。
- 后端是权限、对象归属、金额和状态的最终可信来源。
- 每个阶段只提交该阶段文件；不得暂存 `.superpowers/` 或现有未跟踪计划文件。
- 所有代码修改使用 UTF-8，提交前运行 `git diff --check`。

## File Map

- Create: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringMoney.java` — 金额上限、精度和平台抽成的纯规则模块。
- Create: `sql/tutoring_finance.test.cjs` — 财务事务、幂等 SQL 和金额规则的最小可运行检查。
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringService.java` — 事务、金额校验、付款幂等和批量结算。
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/mapper/TutoringMapper.java` — 加锁查询和已确认付款计数。
- Modify: `ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/TutoringMapper.xml` — 行锁、幂等条件和付款计数 SQL。
- Modify: `sql/tutoring_permissions.test.cjs` — 覆盖拆分后的控制器与所有映射权限。
- Modify: `ruoyi-ui/src/views/tutoring/index.vue` — 移动导航、窄屏弹窗、统一加载和提交防重。
- Create: `ruoyi-ui/src/views/tutoring/viewConfig.cjs` — 状态文案纯函数。
- Modify: `ruoyi-ui/src/views/tutoring/roleConfig.test.cjs` — 响应式结构和状态文案自检。
- Create: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringAdminController.java` — 管理员 HTTP 适配层。
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringController.java` — 移除已迁移的管理员路由。

## Architecture Audit Decisions

- `shrink:` 管理员 HTTP 方法从通用控制器移动到管理员控制器；外部 HTTP interface 不变，管理员维护获得更高 locality。
- `shrink:` 页面内重复状态对象改为一个纯函数 module；模板继续调用原方法名，调用 interface 不扩大。
- `yagni:` 本轮不提取财务服务。删除测试显示，提取后必须复制订单归属、黑名单、通知和流水 implementation，或新增一个只有两个调用方的浅 module；先用事务和金额 module 集中规则。
- `yagni:` 本轮不提取整个管理员 Vue 页面。其查询、指标、公共弹窗和角色导航共享同一状态，拆分会形成宽 props/events interface，不能增加 depth。
- `yagni:` Mapper interface 和 XML 暂不拆。按领域拆文件只会移动同一批 SQL，不会减少调用者必须理解的 interface。
- `native:` 移动端使用 Element UI 选择器和 CSS 媒体查询，不新增响应式依赖。

预计净新增两个小型纯逻辑/契约检查文件；控制器和状态文案以移动、删除重复为主，不引入新依赖。

---

### Task 1: 金额规则模块

**Files:**
- Create: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringMoney.java`
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringService.java`

**Interfaces:**
- Produces: `TutoringMoney.requireAmount(BigDecimal amount, String label)`；合法时返回原金额，非法时抛 `ServiceException`。
- Produces: `TutoringMoney.fee(BigDecimal amount)`；返回四舍五入到两位的 10% 平台抽成。

- [ ] **Step 1: 写金额规则及内置失败检查**

```java
package com.ruoyi.system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.ruoyi.common.core.exception.ServiceException;

final class TutoringMoney
{
    private static final BigDecimal MAX = new BigDecimal("99999999.99");

    private TutoringMoney() {}

    static BigDecimal requireAmount(BigDecimal amount, String label)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
            || amount.compareTo(MAX) > 0 || amount.scale() > 2)
        {
            throw new ServiceException(label + "必须大于0、最多两位小数且不超过99999999.99");
        }
        return amount;
    }

    static BigDecimal fee(BigDecimal amount)
    {
        return requireAmount(amount, "付款金额").multiply(new BigDecimal("0.10"))
            .setScale(2, RoundingMode.HALF_UP);
    }

    public static void main(String[] args)
    {
        assert fee(new BigDecimal("100.00")).compareTo(new BigDecimal("10.00")) == 0;
        assertInvalid(new BigDecimal("0"));
        assertInvalid(new BigDecimal("1.001"));
        assertInvalid(new BigDecimal("100000000"));
    }

    private static void assertInvalid(BigDecimal amount)
    {
        try { requireAmount(amount, "金额"); }
        catch (ServiceException expected) { return; }
        throw new AssertionError("expected invalid amount: " + amount);
    }
}
```

- [ ] **Step 2: 编译并运行规则检查**

Run: `mvn -pl ruoyi-modules/ruoyi-system -am -DskipTests compile`

Expected: `BUILD SUCCESS`

Run: `java -ea -cp "ruoyi-modules/ruoyi-system/target/classes;ruoyi-common/ruoyi-common-core/target/classes" com.ruoyi.system.service.TutoringMoney`

Expected: exit code `0`，无输出。

- [ ] **Step 3: 替换服务内手写金额判断**

在 `addPayment`、`mockPayment` 和 `refundPayment` 中调用 `TutoringMoney.requireAmount`；退款继续额外检查 `refundAmount <= payment.amount`。将原 `fee` 方法替换为 `TutoringMoney.fee` 并删除重复实现。

```java
BigDecimal amount = TutoringMoney.requireAmount(payment == null ? null : payment.getAmount(), "付款金额");
payment.setAmount(amount);
payment.setPlatformFee(TutoringMoney.fee(amount));
```

- [ ] **Step 4: 重新编译**

Run: `mvn -pl ruoyi-modules/ruoyi-system -am -DskipTests compile`

Expected: `BUILD SUCCESS`

- [ ] **Step 5: 提交 A1**

```bash
git add ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringMoney.java ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringService.java
git commit -m "fix: 加固家教金额规则"
```

### Task 2: 付款、发票和结算幂等

**Files:**
- Create: `sql/tutoring_finance.test.cjs`
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringService.java`
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/mapper/TutoringMapper.java`
- Modify: `ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/TutoringMapper.xml`

**Interfaces:**
- Produces: `TutoringMapper.selectMatchByIdForUpdate(Long matchId)`。
- Produces: `TutoringMapper.countConfirmedPaymentsExcluding(Long matchId, Long paymentId)`。
- Preserves: all existing controller method signatures and HTTP paths.

- [ ] **Step 1: 写失败的财务源码契约检查**

```js
const assert = require('assert')
const fs = require('fs')
const service = fs.readFileSync('ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringService.java', 'utf8')
const mapper = fs.readFileSync('ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/TutoringMapper.xml', 'utf8')

for (const method of ['addPayment', 'mockPayment', 'addHomework', 'submitHomework', 'feedbackHomework', 'batchSettleSettlements']) {
  assert.match(service, new RegExp('@Transactional\\(rollbackFor = Exception\\.class\\)[\\s\\S]{0,120}public int ' + method + '\\('))
}
assert.match(service, /mockPayment[\s\S]*MATCH_ACCEPTED[\s\S]*MATCH_COMPLETED/)
assert.match(service, /selectMatchByIdForUpdate/)
assert.match(service, /countConfirmedPaymentsExcluding/)
assert.match(mapper, /selectMatchByIdForUpdate[\s\S]*for update/)
assert.match(mapper, /countConfirmedPaymentsExcluding/)
assert.match(mapper, /issueInvoice[\s\S]*invoice_no is null/)
assert.match(mapper, /reconcilePayment[\s\S]*reconciled_status = '0'/)
```

- [ ] **Step 2: 运行并确认失败**

Run: `node sql/tutoring_finance.test.cjs`

Expected: `AssertionError`，因为事务或幂等 SQL 尚未补齐。

- [ ] **Step 3: 增加行锁和已确认付款计数**

```java
TutoringMatch selectMatchByIdForUpdate(Long matchId);

int countConfirmedPaymentsExcluding(@Param("matchId") Long matchId,
    @Param("paymentId") Long paymentId);
```

```xml
<select id="selectMatchByIdForUpdate" resultMap="TutoringMatchResult">
    select <include refid="matchColumns"/>
    from tutoring_match m
    join tutoring_request r on r.request_id = m.request_id
    left join sys_user tutor on tutor.user_id = m.tutor_id
    left join sys_user publisher on publisher.user_id = r.publisher_id
    where m.match_id = #{matchId}
    for update
</select>

<select id="countConfirmedPaymentsExcluding" resultType="int">
    select count(1) from tutoring_payment
    where match_id = #{matchId} and status = '1'
    <if test="paymentId != null">and payment_id != #{paymentId}</if>
</select>
```

- [ ] **Step 4: 加固服务事务与状态**

为 `addPayment`、`mockPayment`、`addHomework`、`submitHomework`、`feedbackHomework`、`addMaterial`、`addMessage` 和 `batchSettleSettlements` 添加 `@Transactional(rollbackFor = Exception.class)`。

`mockPayment` 先锁订单，只允许进行中或已完成订单，并拒绝已有确认付款的订单：

```java
TutoringMatch match = mapper.selectMatchByIdForUpdate(matchId);
if (match == null) throw new ServiceException("订单不存在");
if (!TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus())
    && !TutoringStatus.MATCH_COMPLETED.equals(match.getStatus()))
{
    throw new ServiceException("只有进行中或已完成的订单可以付款");
}
if (mapper.countConfirmedPaymentsExcluding(matchId, null) > 0)
{
    throw new ServiceException("该订单已完成付款");
}
```

`handlePayment` 在确认前锁订单，并用当前 `paymentId` 排除自身；发现其他确认付款时拒绝。`batchSettleSettlements` 在一个事务中逐个调用已有单笔结算逻辑，确保每笔都有流水和通知：

```java
int rows = 0;
for (Long settlementId : settlementIds)
{
    rows += settleSettlement(settlementId, username);
}
return rows;
```

- [ ] **Step 5: 收紧重复对账和开票 SQL**

```xml
where payment_id = #{paymentId} and status in ('1', '3') and reconciled_status = '0'
```

```xml
where payment_id = #{paymentId} and status in ('1', '3')
  and (invoice_no is null or invoice_no = '')
```

- [ ] **Step 6: 运行财务检查和编译**

Run: `node sql/tutoring_finance.test.cjs`

Expected: exit code `0`。

Run: `mvn -pl ruoyi-modules/ruoyi-system -am -DskipTests compile`

Expected: `BUILD SUCCESS`

- [ ] **Step 7: 提交 A2**

```bash
git add sql/tutoring_finance.test.cjs ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/TutoringService.java ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/mapper/TutoringMapper.java ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/TutoringMapper.xml
git commit -m "fix: 保证家教财务操作幂等"
```

### Task 3: 权限契约与标准安全扫描

**Files:**
- Modify: `sql/tutoring_permissions.test.cjs`

**Interfaces:**
- Consumes: every Spring mapping in `TutoringController.java` and later `TutoringAdminController.java`。
- Produces: a source-level assertion that each mapping has a nearby `@RequiresPermissions` annotation.

- [ ] **Step 1: 增加映射权限检查**

```js
const controllerFiles = [
  'ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringController.java',
  'ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringAdminController.java'
].filter(fs.existsSync)
const controllers = controllerFiles.map(file => fs.readFileSync(file, 'utf8'))

for (const source of controllers) {
  const lines = source.split(/\r?\n/)
  lines.forEach((line, index) => {
    if (!/@(Get|Post|Put|Delete)Mapping/.test(line)) return
    assert.ok(lines.slice(Math.max(0, index - 3), index).some(item => /@RequiresPermissions/.test(item)),
      `missing permission before ${line.trim()}`)
  })
}
```

- [ ] **Step 2: 运行权限检查**

Run: `node sql/tutoring_permissions.test.cjs`

Expected: exit code `0`。

- [ ] **Step 3: 执行 `security-scan` 标准扫描**

Scope: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system`、`ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/TutoringMapper.xml`、`ruoyi-ui/src/api/tutoring.js`。

Focus: authentication、object-level authorization、SQL injection、sensitive-data exposure、financial consistency。

Expected gate: no unresolved reportable Critical/High finding. 若扫描产生已验证的 Critical/High finding，停止后续任务，使用 `codex-security:fix-finding` 为该 finding 建立单独修复与回归检查；Medium/Low 记录在最终剩余风险中。

- [ ] **Step 4: 提交 A3**

```bash
git add sql/tutoring_permissions.test.cjs
git commit -m "test: 加固家教接口权限契约"
```

### Task 4: 移动端导航、弹窗与提交防重

**Files:**
- Modify: `ruoyi-ui/src/views/tutoring/index.vue`
- Modify: `ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

**Interfaces:**
- Produces: `submitOnce(action)`，同一时间只执行一个关键提交 Promise。
- Preserves: `switchTab(name)` and all existing dialog state names.

- [ ] **Step 1: 写失败的前端结构检查**

在 `roleConfig.test.cjs` 追加：

```js
assert.match(view, /class="module-select"/)
assert.match(view, /v-loading="loading"/)
assert.match(view, /submitting: false/)
assert.match(view, /submitOnce\(action\)/)
assert.match(view, /width: 94% !important/)
```

- [ ] **Step 2: 运行并确认失败**

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

Expected: `AssertionError` for `module-select`。

- [ ] **Step 3: 添加移动端模块选择器**

在现有 `.module-switcher` 后添加：

```vue
<el-select v-model="activeTab" class="module-select" size="small" @change="switchTab">
  <el-option v-for="item in workbenchNavItems" :key="item.name"
    :label="item.badge ? `${item.label} (${item.badge})` : item.label" :value="item.name" />
</el-select>
```

将内容区改为统一加载容器：

```vue
<section v-loading="loading" class="workbench-content">
```

- [ ] **Step 4: 添加单一提交锁**

在 `data()` 增加 `submitting: false`，在 methods 增加：

```js
submitOnce(action) {
  if (this.submitting) return Promise.resolve(false)
  this.submitting = true
  return Promise.resolve().then(action).finally(() => { this.submitting = false })
}
```

付款、模拟支付、布置作业、添加课时、发布需求和提交投诉的主按钮增加 `:loading="submitting" :disabled="submitting"`，对应方法只将实际请求包进 `submitOnce(() => request(...))`；表单校验和确认框仍在锁外执行。

- [ ] **Step 5: 添加窄屏 CSS**

```css
.module-select { display: none; width: 100%; margin-bottom: 12px; }

@media (max-width: 640px) {
  .module-switcher { display: none; }
  .module-select { display: block; }
  .tutoring-page >>> .el-dialog { width: 94% !important; margin-top: 5vh !important; }
  .tutoring-page >>> .el-dialog__footer .el-button { min-width: 88px; margin-bottom: 6px; }
}
```

- [ ] **Step 6: 运行前端检查与构建**

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

Expected: exit code `0`。

Run: `npm.cmd --prefix ruoyi-ui run build:prod`

Expected: exit code `0`；允许现有资源体积 warning，不允许编译 error。

- [ ] **Step 7: 提交 B**

```bash
git add ruoyi-ui/src/views/tutoring/index.vue ruoyi-ui/src/views/tutoring/roleConfig.test.cjs
git commit -m "feat: 优化家教工作台移动体验"
```

### Task 5: 提取纯状态文案模块

**Files:**
- Create: `ruoyi-ui/src/views/tutoring/viewConfig.cjs`
- Modify: `ruoyi-ui/src/views/tutoring/index.vue`
- Modify: `ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

**Interfaces:**
- Produces: `statusText(type, value, fallback)`。

- [ ] **Step 1: 写状态文案失败检查**

```js
const { statusText } = require('./viewConfig.cjs')
assert.strictEqual(statusText('request', '0'), '招募中')
assert.strictEqual(statusText('match', '4'), '已取消')
assert.strictEqual(statusText('payment', '3'), '已退款')
assert.strictEqual(statusText('unknown', 'x', '-'), '-')
```

- [ ] **Step 2: 运行并确认失败**

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

Expected: `MODULE_NOT_FOUND` for `viewConfig.cjs`。

- [ ] **Step 3: 创建纯状态模块**

```js
const STATUS = {
  request: { '0': '招募中', '1': '已匹配', '2': '已完成', '3': '已取消' },
  match: { '0': '申请中', '1': '已接单', '2': '已完成', '3': '未选中', '4': '已取消' },
  complaint: { '0': '待处理', '1': '已解决', '2': '已驳回' },
  invitation: { '0': '待处理', '1': '已接受', '2': '已拒绝' },
  payment: { '0': '待审核', '1': '已确认', '2': '已驳回', '3': '已退款' },
  homework: { '0': '待提交', '1': '待反馈', '2': '已反馈' },
  user: { '0': '正常', '1': '停用' },
  verify: { '0': '待审核', '1': '已通过', '2': '已驳回' }
}

function statusText(type, value, fallback) {
  return (STATUS[type] || {})[value] || fallback || value || '-'
}

module.exports = { statusText }
```

- [ ] **Step 4: 替换页面内重复状态对象**

导入 `statusText`，保留现有模板方法名，但改成单行转发，避免修改模板：

```js
requestStatus(status) { return statusText('request', status) },
matchStatus(status) { return statusText('match', status) },
paymentStatus(status) { return statusText('payment', status) },
homeworkStatus(status) { return statusText('homework', status) }
```

- [ ] **Step 5: 运行检查和构建**

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

Expected: exit code `0`。

Run: `npm.cmd --prefix ruoyi-ui run build:prod`

Expected: exit code `0`。

- [ ] **Step 6: 提交 C1**

```bash
git add ruoyi-ui/src/views/tutoring/viewConfig.cjs ruoyi-ui/src/views/tutoring/index.vue ruoyi-ui/src/views/tutoring/roleConfig.test.cjs
git commit -m "refactor: 集中家教状态文案"
```

### Task 6: 拆分管理员 HTTP 适配层

**Files:**
- Create: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringAdminController.java`
- Modify: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringController.java`
- Modify: `sql/tutoring_permissions.test.cjs`

**Interfaces:**
- Preserves: every existing path, HTTP method, permission string, request body and response type.
- Produces: `TutoringAdminController` containing routes guarded by `tutoring:business:monitor`。

- [ ] **Step 1: 先让权限测试同时读取两个控制器**

将原单个 `controller` 字符串改为两个源文件连接；原有路径断言继续作用于连接结果：

```js
const controller = controllerFiles.map(file => fs.readFileSync(file, 'utf8')).join('\n')
```

- [ ] **Step 2: 创建管理员控制器并移动方法**

```java
@RestController
@RequestMapping("/tutoring")
public class TutoringAdminController extends BaseController
{
    @Autowired
    private TutoringService service;

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/clients")
    public TableDataInfo clients(SysUser query)
    {
        startPage();
        return getDataTable(service.getAdminClients(query));
    }
}
```

按同样方式移动所有 `tutoring:business:monitor` 方法，包括公告、用户、需求、订单、邀请、课时、CRM、风险、黑名单、流水、报表、提醒、导出、结算、付款、回访和工单。移动而非复制；日志注解、分页调用、CSV 帮助方法和返回类型原样保留。

- [ ] **Step 3: 检查重复路径和权限**

Run: `node sql/tutoring_permissions.test.cjs`

Expected: exit code `0`。

Run: `rg -n '@(Get|Post|Put|Delete)Mapping' ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringController.java ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringAdminController.java`

Expected: every mapping appears once；管理员映射位于 `TutoringAdminController.java`。

- [ ] **Step 4: 后端编译**

Run: `mvn -pl ruoyi-modules/ruoyi-system -am -DskipTests compile`

Expected: `BUILD SUCCESS`

- [ ] **Step 5: 提交 C2**

```bash
git add ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringAdminController.java ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/TutoringController.java sql/tutoring_permissions.test.cjs
git commit -m "refactor: 拆分家教管理员接口"
```

### Task 7: 浏览器验证与最终回归

**Files:**
- No committed files unless a verified defect requires a focused fix.

**Interfaces:**
- Validates: admin、client、tutor workbench rendering and one primary interaction per available role.

- [ ] **Step 1: 运行全部静态与编译检查**

Run: `node sql/tutoring_permissions.test.cjs`

Run: `node sql/tutoring_finance.test.cjs`

Run: `node ruoyi-ui/src/views/tutoring/roleConfig.test.cjs`

Run: `mvn -pl ruoyi-modules/ruoyi-system -am -DskipTests compile`

Run: `npm.cmd --prefix ruoyi-ui run build:prod`

Run: `git diff --check`

Expected: all commands exit `0`；Maven reports `BUILD SUCCESS`。

- [ ] **Step 2: 启动可用的本地服务**

优先复用已运行的网关、系统服务和前端端口。若仅前端未运行，执行：

Run: `npm.cmd --prefix ruoyi-ui run dev`

Expected: Vue dev server prints a local URL and remains running.

- [ ] **Step 3: 使用 frontend-testing-debugging + Playwright 验证**

Flow: `登录 -> /tutoring/workbench -> 切换模块 -> 打开一个关键弹窗 -> 提交按钮进入 loading/disabled -> 返回结果`。

Desktop viewport: `1440x900`。Mobile viewport: `390x844`。

Checks: page identity、non-blank DOM、no framework overlay、console errors、screenshot、module selector、dialog width、one interaction proof。

- [ ] **Step 4: 核对最终差异**

Run: `git status --short`

Run: `git diff --stat 64f18b45..HEAD`

Expected: only planned source/test files and plan document changes；`.superpowers/` 与旧未跟踪计划文件仍未提交。

若浏览器验证发现缺陷，先写能复现该缺陷的最小检查，再建立单独修复任务；本计划不预先授权未知范围的提交。
