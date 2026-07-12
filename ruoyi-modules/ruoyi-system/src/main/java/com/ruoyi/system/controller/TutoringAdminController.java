package com.ruoyi.system.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.domain.TutorProfile;
import com.ruoyi.system.domain.TutoringAnnouncement;
import com.ruoyi.system.domain.TutoringBlacklist;
import com.ruoyi.system.domain.TutoringFinanceLedger;
import com.ruoyi.system.domain.TutoringFollowup;
import com.ruoyi.system.domain.TutoringInvitation;
import com.ruoyi.system.domain.TutoringLesson;
import com.ruoyi.system.domain.TutoringMatch;
import com.ruoyi.system.domain.TutoringPayment;
import com.ruoyi.system.domain.TutoringRequest;
import com.ruoyi.system.domain.TutoringSettlement;
import com.ruoyi.system.domain.TutoringTicket;
import com.ruoyi.system.service.TutoringService;

@RestController
@RequestMapping("/tutoring")
public class TutoringAdminController extends BaseController
{
    @Autowired
    private TutoringService service;

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/announcements")
    public TableDataInfo adminAnnouncements(TutoringAnnouncement query)
    {
        startPage();
        return getDataTable(service.getAdminAnnouncements(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PostMapping("/admin/announcements")
    public AjaxResult addAnnouncement(@RequestBody TutoringAnnouncement announcement)
    {
        return toAjax(service.saveAnnouncement(announcement, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/announcements/{announcementId}")
    public AjaxResult updateAnnouncement(@PathVariable Long announcementId,
        @RequestBody TutoringAnnouncement announcement)
    {
        announcement.setAnnouncementId(announcementId);
        return toAjax(service.saveAnnouncement(announcement, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @DeleteMapping("/admin/announcements/{announcementId}")
    public AjaxResult deleteAnnouncement(@PathVariable Long announcementId)
    {
        return toAjax(service.deleteAnnouncement(announcementId));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/requests")
    public TableDataInfo adminRequests(TutoringRequest query)
    {
        startPage();
        return getDataTable(service.getAdminRequests(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/clients")
    public TableDataInfo adminClients(SysUser query)
    {
        startPage();
        return getDataTable(service.getAdminClients(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/tutors")
    public TableDataInfo adminTutors(TutorProfile query)
    {
        startPage();
        return getDataTable(service.getAdminTutors(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/users/{userId}/status")
    public AjaxResult changeAdminUserStatus(@PathVariable Long userId, @RequestBody SysUser user)
    {
        return toAjax(service.changeAdminUserStatus(userId, user));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/matches")
    public TableDataInfo adminMatches(TutoringMatch query)
    {
        startPage();
        return getDataTable(service.getAdminMatches(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/matches/{matchId}/close")
    public AjaxResult closeAdminMatch(@PathVariable Long matchId)
    {
        return toAjax(service.closeAdminMatch(matchId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/invitations")
    public TableDataInfo adminInvitations(TutoringInvitation query)
    {
        startPage();
        return getDataTable(service.getAdminInvitations(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/lessons")
    public TableDataInfo adminLessons(TutoringLesson query)
    {
        startPage();
        return getDataTable(service.getAdminLessons(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/dashboard/todos")
    public AjaxResult dashboardTodos()
    {
        return success(service.getDashboardTodos());
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/crm")
    public AjaxResult crmDashboard()
    {
        return success(service.getCrmDashboard());
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/risk-alerts")
    public AjaxResult riskAlerts()
    {
        return success(service.getRiskAlerts());
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/blacklists")
    public TableDataInfo adminBlacklists(TutoringBlacklist query)
    {
        startPage();
        return getDataTable(service.getAdminBlacklists(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PostMapping("/admin/blacklists")
    public AjaxResult addBlacklist(@RequestBody TutoringBlacklist blacklist)
    {
        return toAjax(service.saveBlacklist(blacklist, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/blacklists/{blacklistId}/disable")
    public AjaxResult disableBlacklist(@PathVariable Long blacklistId)
    {
        return toAjax(service.disableBlacklist(blacklistId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/finance-ledgers")
    public TableDataInfo financeLedgers(TutoringFinanceLedger query)
    {
        startPage();
        return getDataTable(service.getFinanceLedgers(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/reports")
    public AjaxResult operationsReport()
    {
        return success(service.getOperationsReport());
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PostMapping("/admin/reminders/generate")
    public AjaxResult generateReminders()
    {
        return success(service.generateReminders());
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PostMapping("/admin/export/{type}")
    public void export(@PathVariable String type, HttpServletResponse response, SysUser user,
        TutorProfile profile, TutoringRequest request, TutoringMatch match,
        TutoringInvitation invitation, TutoringLesson lesson) throws IOException
    {
        List<String[]> rows = new ArrayList<>();
        if ("clients".equals(type))
        {
            rows.add(new String[] { "account", "name", "phone", "email", "status", "stats" });
            service.getAdminClients(user).forEach(item -> rows.add(new String[] {
                item.getUserName(), item.getNickName(), item.getPhonenumber(), item.getEmail(),
                item.getStatus(), item.getRemark()
            }));
        }
        else if ("tutors".equals(type))
        {
            rows.add(new String[] { "account", "name", "school", "major", "subjects", "rate", "verify" });
            service.getAdminTutors(profile).forEach(item -> rows.add(new String[] {
                item.getLoginName(), item.getUserName(), item.getUniversity(), item.getMajor(),
                item.getSubjects(), text(item.getHourlyRate()), item.getVerifyStatus()
            }));
        }
        else if ("requests".equals(type))
        {
            rows.add(new String[] { "publisher", "subject", "grade", "area", "schedule", "budget", "status" });
            service.getAdminRequests(request).forEach(item -> rows.add(new String[] {
                item.getPublisherName(), item.getSubject(), item.getLearnerGrade(), item.getArea(),
                item.getScheduleText(), text(item.getHourlyBudget()), item.getStatus()
            }));
        }
        else if ("matches".equals(type))
        {
            rows.add(new String[] { "client", "tutor", "subject", "rate", "status", "rating" });
            service.getAdminMatches(match).forEach(item -> rows.add(new String[] {
                item.getPublisherName(), item.getTutorName(), item.getSubject(),
                text(item.getQuotedRate()), item.getStatus(), text(item.getRating())
            }));
        }
        else if ("invitations".equals(type))
        {
            rows.add(new String[] { "client", "tutor", "subject", "grade", "area", "rate", "status" });
            service.getAdminInvitations(invitation).forEach(item -> rows.add(new String[] {
                item.getPublisherName(), item.getTutorName(), item.getSubject(), item.getLearnerGrade(),
                item.getArea(), text(item.getOfferedRate()), item.getStatus()
            }));
        }
        else if ("lessons".equals(type))
        {
            rows.add(new String[] { "match", "subject", "client", "tutor", "date", "start", "end", "hours", "amount", "confirmed" });
            service.getAdminLessons(lesson).forEach(item -> rows.add(new String[] {
                text(item.getMatchId()), item.getSubject(), item.getPublisherName(), item.getTutorName(),
                text(item.getLessonDate()), text(item.getStartTime()), text(item.getEndTime()),
                text(item.getHours()), text(item.getAmount()), item.getConfirmStatus()
            }));
        }
        else
        {
            rows.add(new String[] { "error" });
            rows.add(new String[] { "unsupported export type" });
        }
        writeCsv(response, "tutoring-" + type + ".csv", rows);
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/settlements")
    public TableDataInfo adminSettlements(TutoringSettlement query)
    {
        startPage();
        return getDataTable(service.getAdminSettlements(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/settlements/{settlementId}/settle")
    public AjaxResult settleSettlement(@PathVariable Long settlementId)
    {
        return toAjax(service.settleSettlement(settlementId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/settlements/batch/settle")
    public AjaxResult batchSettleSettlements(@RequestBody List<Long> settlementIds)
    {
        return toAjax(service.batchSettleSettlements(settlementIds, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/payments")
    public TableDataInfo adminPayments(TutoringPayment query)
    {
        startPage();
        return getDataTable(service.getAdminPayments(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/payments/{paymentId}/handle")
    public AjaxResult handlePayment(@PathVariable Long paymentId, @RequestBody TutoringPayment handling)
    {
        return toAjax(service.handlePayment(paymentId, handling, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/payments/{paymentId}/refund")
    public AjaxResult refundPayment(@PathVariable Long paymentId, @RequestBody TutoringPayment refund)
    {
        return toAjax(service.refundPayment(paymentId, refund, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/payments/{paymentId}/reconcile")
    public AjaxResult reconcilePayment(@PathVariable Long paymentId, @RequestBody TutoringPayment reconciliation)
    {
        return toAjax(service.reconcilePayment(paymentId, reconciliation, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/payments/{paymentId}/invoice")
    public AjaxResult issueInvoice(@PathVariable Long paymentId, @RequestBody TutoringPayment invoice)
    {
        return toAjax(service.issueInvoice(paymentId, invoice, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/followups")
    public TableDataInfo adminFollowups(TutoringFollowup query)
    {
        startPage();
        return getDataTable(service.getAdminFollowups(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PostMapping("/admin/matches/{matchId}/followups")
    public AjaxResult addFollowup(@PathVariable Long matchId, @RequestBody TutoringFollowup followup)
    {
        return toAjax(service.addFollowup(matchId, followup, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/followups/{followupId}/done")
    public AjaxResult completeFollowup(@PathVariable Long followupId)
    {
        return toAjax(service.completeFollowup(followupId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @GetMapping("/admin/tickets")
    public TableDataInfo adminTickets(TutoringTicket query)
    {
        startPage();
        return getDataTable(service.getAdminTickets(query));
    }

    @RequiresPermissions("tutoring:business:monitor")
    @PutMapping("/admin/tickets/{ticketId}/handle")
    public AjaxResult handleTicket(@PathVariable Long ticketId, @RequestBody TutoringTicket handling)
    {
        return toAjax(service.handleTicket(ticketId, handling, SecurityUtils.getUsername()));
    }

    private void writeCsv(HttpServletResponse response, String filename, List<String[]> rows)
        throws IOException
    {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        for (String[] row : rows)
        {
            for (int i = 0; i < row.length; i++)
            {
                if (i > 0)
                {
                    writer.write(',');
                }
                writer.write(csv(row[i]));
            }
            writer.println();
        }
    }

    private String csv(String value)
    {
        return "\"" + text(value).replace("\"", "\"\"") + "\"";
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }
}
