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
