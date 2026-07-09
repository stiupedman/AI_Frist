package com.ruoyi.system.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.domain.TutorAvailability;
import com.ruoyi.system.domain.TutorProfile;
import com.ruoyi.system.domain.TutoringAnnouncement;
import com.ruoyi.system.domain.TutoringComplaint;
import com.ruoyi.system.domain.TutoringFollowup;
import com.ruoyi.system.domain.TutoringInvitation;
import com.ruoyi.system.domain.TutoringLesson;
import com.ruoyi.system.domain.TutoringLearner;
import com.ruoyi.system.domain.TutoringMaterial;
import com.ruoyi.system.domain.TutoringMatch;
import com.ruoyi.system.domain.TutoringMessage;
import com.ruoyi.system.domain.TutoringNotification;
import com.ruoyi.system.domain.TutoringPayment;
import com.ruoyi.system.domain.TutoringRequest;
import com.ruoyi.system.domain.TutoringSettlement;
import com.ruoyi.system.domain.TutoringTicket;
import com.ruoyi.system.service.TutoringService;

@RestController
@RequestMapping("/tutoring")
public class TutoringController extends BaseController
{
    @Autowired
    private TutoringService service;

    @RequiresPermissions("tutoring:profile:edit")
    @GetMapping("/profile/me")
    public AjaxResult myProfile()
    {
        return success(service.getProfile(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/profiles/{userId}")
    public AjaxResult tutorProfile(@PathVariable Long userId)
    {
        return success(service.getVerifiedProfile(userId));
    }

    @RequiresPermissions("tutoring:profile:edit")
    @Log(title = "教员资料", businessType = BusinessType.UPDATE)
    @PutMapping("/profile/me")
    public AjaxResult saveProfile(@Validated @RequestBody TutorProfile profile)
    {
        return toAjax(service.saveProfile(profile, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:request:add")
    @GetMapping("/learners/mine")
    public TableDataInfo learners()
    {
        startPage();
        return getDataTable(service.getLearners(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:request:add")
    @PostMapping("/learners")
    public AjaxResult addLearner(@RequestBody TutoringLearner learner)
    {
        return toAjax(service.saveLearner(learner, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:request:add")
    @PutMapping("/learners/{learnerId}")
    public AjaxResult updateLearner(@PathVariable Long learnerId, @RequestBody TutoringLearner learner)
    {
        learner.setLearnerId(learnerId);
        return toAjax(service.saveLearner(learner, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:request:add")
    @DeleteMapping("/learners/{learnerId}")
    public AjaxResult deleteLearner(@PathVariable Long learnerId)
    {
        return toAjax(service.deleteLearner(learnerId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:profile:edit")
    @GetMapping("/availability/mine")
    public AjaxResult availability()
    {
        return success(service.getAvailability(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:profile:edit")
    @PostMapping("/availability")
    public AjaxResult addAvailability(@RequestBody TutorAvailability availability)
    {
        return toAjax(service.addAvailability(availability, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:profile:edit")
    @DeleteMapping("/availability/{availabilityId}")
    public AjaxResult deleteAvailability(@PathVariable Long availabilityId)
    {
        return toAjax(service.deleteAvailability(availabilityId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:workbench:view")
    @GetMapping("/announcements")
    public AjaxResult announcements()
    {
        return success(service.getAnnouncements());
    }

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

    @RequiresPermissions("tutoring:profile:verify")
    @GetMapping("/profiles/pending")
    public TableDataInfo pendingProfiles()
    {
        startPage();
        return getDataTable(service.getPendingProfiles());
    }

    @RequiresPermissions("tutoring:workbench:view")
    @GetMapping("/tutors")
    public TableDataInfo tutors(TutorProfile query)
    {
        startPage();
        return getDataTable(service.getVerifiedTutors(query));
    }

    @RequiresPermissions("tutoring:profile:verify")
    @Log(title = "教员审核", businessType = BusinessType.UPDATE)
    @PutMapping("/profiles/{profileId}/verify")
    public AjaxResult verifyProfile(@PathVariable Long profileId, @RequestBody TutorProfile profile)
    {
        profile.setProfileId(profileId);
        return toAjax(service.verifyProfile(profile, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:request:list")
    @GetMapping("/requests/open")
    public TableDataInfo openRequests(TutoringRequest query)
    {
        startPage();
        List<TutoringRequest> list = service.getOpenRequests(query);
        return getDataTable(list);
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

    @RequiresPermissions("tutoring:request:add")
    @GetMapping("/requests/mine")
    public TableDataInfo myRequests()
    {
        startPage();
        return getDataTable(service.getMyRequests(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:request:add")
    @Log(title = "家教需求", businessType = BusinessType.INSERT)
    @PostMapping("/requests")
    public AjaxResult publishRequest(@Validated @RequestBody TutoringRequest request)
    {
        return toAjax(service.publishRequest(request, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:request:add")
    @Log(title = "复制家教需求", businessType = BusinessType.INSERT)
    @PostMapping("/requests/{requestId}/copy")
    public AjaxResult copyRequest(@PathVariable Long requestId)
    {
        return toAjax(service.copyRequest(requestId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:request:add")
    @Log(title = "取消家教需求", businessType = BusinessType.UPDATE)
    @PutMapping("/requests/{requestId}/cancel")
    public AjaxResult cancelRequest(@PathVariable Long requestId)
    {
        return toAjax(service.cancelRequest(requestId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/matches/mine")
    public TableDataInfo myMatches()
    {
        startPage();
        return getDataTable(service.getMyMatches(SecurityUtils.getUserId()));
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
            rows.add(new String[] { "match", "subject", "client", "tutor", "date", "hours", "amount", "confirmed" });
            service.getAdminLessons(lesson).forEach(item -> rows.add(new String[] {
                text(item.getMatchId()), item.getSubject(), item.getPublisherName(), item.getTutorName(),
                text(item.getLessonDate()), text(item.getHours()), text(item.getAmount()), item.getConfirmStatus()
            }));
        }
        else
        {
            rows.add(new String[] { "error" });
            rows.add(new String[] { "unsupported export type" });
        }
        writeCsv(response, "tutoring-" + type + ".csv", rows);
    }

    @RequiresPermissions("tutoring:match:apply")
    @Log(title = "家教申请", businessType = BusinessType.INSERT)
    @PostMapping("/requests/{requestId}/apply")
    public AjaxResult apply(@PathVariable Long requestId, @Validated @RequestBody TutoringMatch match)
    {
        return toAjax(service.apply(requestId, match, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:apply")
    @Log(title = "撤回家教申请", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/withdraw")
    public AjaxResult withdraw(@PathVariable Long matchId)
    {
        return toAjax(service.withdraw(matchId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @Log(title = "取消家教订单", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/cancel")
    public AjaxResult cancelMatch(@PathVariable Long matchId, @RequestBody TutoringMatch cancel)
    {
        return toAjax(service.cancelMatch(matchId, cancel, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @Log(title = "调整家教时间", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/reschedule")
    public AjaxResult rescheduleMatch(@PathVariable Long matchId, @RequestBody TutoringMatch change)
    {
        return toAjax(service.rescheduleMatch(matchId, change, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PutMapping("/matches/{matchId}/trial")
    public AjaxResult scheduleTrial(@PathVariable Long matchId, @RequestBody TutoringMatch trial)
    {
        return toAjax(service.scheduleTrial(matchId, trial, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PutMapping("/matches/{matchId}/trial/complete")
    public AjaxResult completeTrial(@PathVariable Long matchId)
    {
        return toAjax(service.completeTrial(matchId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:profile:verify")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(service.getDashboard());
    }

    @RequiresPermissions("tutoring:match:accept")
    @Log(title = "接受教员申请", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/accept")
    public AjaxResult accept(@PathVariable Long matchId)
    {
        return toAjax(service.accept(matchId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:complete")
    @Log(title = "完成家教订单", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/complete")
    public AjaxResult complete(@PathVariable Long matchId)
    {
        return toAjax(service.complete(matchId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:review")
    @Log(title = "评价家教订单", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/review")
    public AjaxResult review(@PathVariable Long matchId, @RequestBody TutoringMatch review)
    {
        return toAjax(service.review(matchId, review, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/matches/{matchId}/lessons")
    public AjaxResult lessons(@PathVariable Long matchId)
    {
        return success(service.getLessons(matchId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/matches/{matchId}/materials")
    public AjaxResult materials(@PathVariable Long matchId)
    {
        return success(service.getMaterials(matchId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PostMapping("/matches/{matchId}/materials")
    public AjaxResult addMaterial(@PathVariable Long matchId, @RequestBody TutoringMaterial material)
    {
        return toAjax(service.addMaterial(matchId, material, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/matches/{matchId}/messages")
    public AjaxResult messages(@PathVariable Long matchId)
    {
        return success(service.getMessages(matchId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PostMapping("/matches/{matchId}/messages")
    public AjaxResult addMessage(@PathVariable Long matchId, @RequestBody TutoringMessage message)
    {
        return toAjax(service.addMessage(matchId, message, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/matches/{matchId}/payments")
    public AjaxResult payments(@PathVariable Long matchId)
    {
        return success(service.getPayments(matchId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PostMapping("/matches/{matchId}/payments")
    public AjaxResult addPayment(@PathVariable Long matchId, @RequestBody TutoringPayment payment)
    {
        return toAjax(service.addPayment(matchId, payment, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PostMapping("/matches/{matchId}/payments/mock")
    public AjaxResult mockPayment(@PathVariable Long matchId, @RequestBody TutoringPayment payment)
    {
        return toAjax(service.mockPayment(matchId, payment, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:complete")
    @Log(title = "上课记录", businessType = BusinessType.INSERT)
    @PostMapping("/matches/{matchId}/lessons")
    public AjaxResult addLesson(@PathVariable Long matchId, @Validated @RequestBody TutoringLesson lesson)
    {
        return toAjax(service.addLesson(matchId, lesson, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @Log(title = "确认课时记录", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/lessons/{lessonId}/confirm")
    public AjaxResult confirmLesson(@PathVariable Long matchId, @PathVariable Long lessonId)
    {
        return toAjax(service.confirmLesson(matchId, lessonId, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/settlements/mine")
    public AjaxResult mySettlements()
    {
        return success(service.getMySettlements(SecurityUtils.getUserId()));
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

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/notifications/mine")
    public AjaxResult notifications(TutoringNotification query)
    {
        return success(service.getNotifications(SecurityUtils.getUserId(), query));
    }

    @RequiresPermissions("tutoring:match:list")
    @PutMapping("/notifications/{notificationId}/read")
    public AjaxResult readNotification(@PathVariable Long notificationId)
    {
        return toAjax(service.readNotification(notificationId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PutMapping("/notifications/read-all")
    public AjaxResult readAllNotifications()
    {
        return toAjax(service.readAllNotifications(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/notifications/unread-count")
    public AjaxResult unreadNotificationCount()
    {
        return success(service.countUnreadNotifications(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @Log(title = "订单投诉", businessType = BusinessType.INSERT)
    @PostMapping("/matches/{matchId}/complaints")
    public AjaxResult complain(@PathVariable Long matchId, @Validated @RequestBody TutoringComplaint complaint)
    {
        return toAjax(service.complain(matchId, complaint, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/complaints/mine")
    public AjaxResult myComplaints()
    {
        return success(service.getMyComplaints(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:profile:verify")
    @GetMapping("/complaints")
    public AjaxResult complaints()
    {
        return success(service.getComplaints());
    }

    @RequiresPermissions("tutoring:profile:verify")
    @Log(title = "处理订单投诉", businessType = BusinessType.UPDATE)
    @PutMapping("/complaints/{complaintId}/handle")
    public AjaxResult handleComplaint(@PathVariable Long complaintId, @RequestBody TutoringComplaint handling)
    {
        return toAjax(service.handleComplaint(complaintId, handling, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/tickets/mine")
    public AjaxResult myTickets()
    {
        return success(service.getMyTickets(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:list")
    @PostMapping("/tickets")
    public AjaxResult submitTicket(@RequestBody TutoringTicket ticket)
    {
        return toAjax(service.submitTicket(ticket, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
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

    @RequiresPermissions("tutoring:match:apply")
    @GetMapping("/requests/recommended")
    public AjaxResult recommendedRequests()
    {
        return success(service.getRecommendedRequests(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:request:add")
    @GetMapping("/favorites")
    public AjaxResult favoriteTutors()
    {
        return success(service.getFavoriteTutors(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:request:add")
    @PostMapping("/favorites/{tutorId}")
    public AjaxResult favoriteTutor(@PathVariable Long tutorId)
    {
        return toAjax(service.favoriteTutor(tutorId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:request:add")
    @DeleteMapping("/favorites/{tutorId}")
    public AjaxResult unfavoriteTutor(@PathVariable Long tutorId)
    {
        return toAjax(service.unfavoriteTutor(tutorId, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:request:add")
    @Log(title = "预约教员", businessType = BusinessType.INSERT)
    @PostMapping("/invitations/{tutorId}")
    public AjaxResult inviteTutor(@PathVariable Long tutorId,
        @Validated @RequestBody TutoringInvitation invitation)
    {
        return toAjax(service.inviteTutor(tutorId, invitation, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/invitations/mine")
    public AjaxResult invitations()
    {
        return success(service.getInvitations(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:apply")
    @PutMapping("/invitations/{invitationId}/accept")
    public AjaxResult acceptInvitation(@PathVariable Long invitationId)
    {
        return toAjax(service.respondInvitation(invitationId, true, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:apply")
    @PutMapping("/invitations/{invitationId}/reject")
    public AjaxResult rejectInvitation(@PathVariable Long invitationId)
    {
        return toAjax(service.respondInvitation(invitationId, false, SecurityUtils.getUserId(),
            SecurityUtils.getUsername()));
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
