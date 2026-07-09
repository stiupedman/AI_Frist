package com.ruoyi.system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
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
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.TutoringMapper;

@Service
public class TutoringService
{
    @Autowired
    private TutoringMapper mapper;

    @Autowired
    private SysUserMapper userMapper;

    public TutorProfile getProfile(Long userId)
    {
        return mapper.selectProfileByUserId(userId);
    }

    public TutorProfile getVerifiedProfile(Long userId)
    {
        TutorProfile profile = mapper.selectProfileByUserId(userId);
        if (profile == null || !"1".equals(profile.getVerifyStatus()))
        {
            throw new ServiceException("教员资料不存在或未通过审核");
        }
        profile.setStudentCardUrl(null);
        profile.setQualificationUrl(null);
        return profile;
    }

    public List<TutorProfile> getPendingProfiles()
    {
        return mapper.selectPendingProfiles();
    }

    public List<TutorProfile> getVerifiedTutors(TutorProfile query)
    {
        List<TutorProfile> profiles = mapper.selectVerifiedTutors(query);
        profiles.forEach(profile -> {
            profile.setStudentCardUrl(null);
            profile.setQualificationUrl(null);
        });
        return profiles;
    }

    public List<TutoringLearner> getLearners(Long userId)
    {
        return mapper.selectLearnersByUserId(userId);
    }

    public int saveLearner(TutoringLearner learner, Long userId, String username)
    {
        if (blank(learner.getLearnerName()) || blank(learner.getGrade()))
        {
            throw new ServiceException("请填写学员姓名和年级");
        }
        learner.setUserId(userId);
        if (learner.getLearnerId() == null)
        {
            learner.setCreateBy(username);
            return mapper.insertLearner(learner);
        }
        learner.setUpdateBy(username);
        int rows = mapper.updateLearner(learner);
        if (rows == 0)
        {
            throw new ServiceException("学员档案不存在");
        }
        return rows;
    }

    public int deleteLearner(Long learnerId, Long userId)
    {
        return mapper.deleteLearner(learnerId, userId);
    }

    public List<TutorAvailability> getAvailability(Long userId)
    {
        getVerifiedProfile(userId);
        return mapper.selectAvailabilityByUserId(userId);
    }

    public int addAvailability(TutorAvailability availability, Long userId, String username)
    {
        getVerifiedProfile(userId);
        if (blank(availability.getWeekDay()) || blank(availability.getStartTime()) || blank(availability.getEndTime()))
        {
            throw new ServiceException("请填写星期和时间段");
        }
        availability.setUserId(userId);
        availability.setAvailableStatus("1");
        availability.setCreateBy(username);
        return mapper.insertAvailability(availability);
    }

    public int deleteAvailability(Long availabilityId, Long userId)
    {
        return mapper.deleteAvailability(availabilityId, userId);
    }

    public List<TutoringAnnouncement> getAnnouncements()
    {
        return mapper.selectActiveAnnouncements();
    }

    public List<TutoringAnnouncement> getAdminAnnouncements(TutoringAnnouncement query)
    {
        return mapper.selectAllAnnouncements(query);
    }

    public int saveAnnouncement(TutoringAnnouncement announcement, String username)
    {
        if (announcement == null || blank(announcement.getTitle()) || blank(announcement.getContent()))
        {
            throw new ServiceException("请填写公告标题和内容");
        }
        if (!"1".equals(announcement.getStatus()))
        {
            announcement.setStatus("0");
        }
        if (announcement.getAnnouncementId() == null)
        {
            announcement.setCreateBy(username);
            return mapper.insertAnnouncement(announcement);
        }
        announcement.setUpdateBy(username);
        return mapper.updateAnnouncement(announcement);
    }

    public int deleteAnnouncement(Long announcementId)
    {
        return mapper.deleteAnnouncement(announcementId);
    }

    public int saveProfile(TutorProfile profile, Long userId, String username)
    {
        profile.setUserId(userId);
        if (mapper.selectProfileByUserId(userId) == null)
        {
            profile.setCreateBy(username);
            return mapper.insertProfile(profile);
        }
        profile.setUpdateBy(username);
        return mapper.updateProfile(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    public int verifyProfile(TutorProfile profile, String username)
    {
        if (!"1".equals(profile.getVerifyStatus()) && !"2".equals(profile.getVerifyStatus()))
        {
            throw new ServiceException("审核状态只能是通过或驳回");
        }
        TutorProfile current = mapper.selectProfileById(profile.getProfileId());
        if (current == null)
        {
            throw new ServiceException("教员资料不存在");
        }
        profile.setUpdateBy(username);
        int rows = mapper.verifyProfile(profile);
        if (rows == 0)
        {
            throw new ServiceException("资料不存在或已完成审核");
        }
        notify(current.getUserId(), "教员资料审核",
            "你的教员资料审核结果：" + ("1".equals(profile.getVerifyStatus()) ? "通过" : "驳回"));
        return rows;
    }

    public List<TutoringRequest> getOpenRequests(TutoringRequest query)
    {
        if (query.getMinBudget() != null && query.getMaxBudget() != null
            && query.getMinBudget().compareTo(query.getMaxBudget()) > 0)
        {
            throw new ServiceException("最低预算不能高于最高预算");
        }
        return mapper.selectOpenRequests(query);
    }

    public List<TutoringRequest> getAdminRequests(TutoringRequest query)
    {
        return mapper.selectAllRequests(query);
    }

    public List<SysUser> getAdminClients(SysUser query)
    {
        return mapper.selectAdminClients(query);
    }

    public List<TutorProfile> getAdminTutors(TutorProfile query)
    {
        List<TutorProfile> profiles = mapper.selectAdminTutors(query);
        profiles.forEach(profile -> {
            profile.setStudentCardUrl(null);
            profile.setQualificationUrl(null);
        });
        return profiles;
    }

    public int changeAdminUserStatus(Long userId, SysUser user)
    {
        String status = user.getStatus();
        if (!"0".equals(status) && !"1".equals(status))
        {
            throw new ServiceException("账号状态只能是正常或停用");
        }
        if (mapper.countTutoringUser(userId) == 0)
        {
            throw new ServiceException("只能管理家长或教员账号");
        }
        if (userMapper.updateUserStatus(userId, status) != 1)
        {
            throw new ServiceException("账号状态未变化");
        }
        return 1;
    }

    public List<TutoringRequest> getMyRequests(Long userId)
    {
        return mapper.selectRequestsByPublisherId(userId);
    }

    public int publishRequest(TutoringRequest request, Long userId, String username)
    {
        request.setPublisherId(userId);
        request.setCreateBy(username);
        return mapper.insertRequest(request);
    }

    public int copyRequest(Long requestId, Long userId, String username)
    {
        TutoringRequest current = requireRequest(requestId);
        if (!userId.equals(current.getPublisherId()))
        {
            throw new ServiceException("只能复制自己发布的需求");
        }
        TutoringRequest copy = new TutoringRequest();
        copy.setPublisherId(userId);
        copy.setLearnerGrade(current.getLearnerGrade());
        copy.setSubject(current.getSubject());
        copy.setArea(current.getArea());
        copy.setScheduleText(current.getScheduleText());
        copy.setHourlyBudget(current.getHourlyBudget());
        copy.setRequirementText(current.getRequirementText());
        copy.setCreateBy(username);
        return mapper.insertRequest(copy);
    }

    @Transactional(rollbackFor = Exception.class)
    public int cancelRequest(Long requestId, Long userId, String username)
    {
        TutoringRequest request = requireRequest(requestId);
        if (!userId.equals(request.getPublisherId()))
        {
            throw new ServiceException("只能取消自己发布的需求");
        }
        TutoringStatus.requireRequestTransition(request.getStatus(), TutoringStatus.REQUEST_CANCELLED);
        List<Long> tutorIds = mapper.selectOpenMatchTutorIds(requestId);
        if (mapper.updateRequestStatusIfCurrent(requestId, TutoringStatus.REQUEST_OPEN,
            TutoringStatus.REQUEST_CANCELLED, username) != 1)
        {
            throw new ServiceException("需求状态已变化，请刷新后重试");
        }
        mapper.cancelOpenMatches(requestId, username);
        tutorIds.forEach(tutorId -> notify(tutorId, "需求已取消", "你申请的“" + request.getSubject() + "”需求已取消"));
        return 1;
    }

    public List<TutoringMatch> getMyMatches(Long userId)
    {
        return mapper.selectMatchesByUserId(userId);
    }

    public List<TutoringMatch> getAdminMatches(TutoringMatch query)
    {
        return mapper.selectAllMatches(query);
    }

    public List<TutoringInvitation> getAdminInvitations(TutoringInvitation query)
    {
        return mapper.selectAllInvitations(query);
    }

    public List<TutoringLesson> getAdminLessons(TutoringLesson query)
    {
        return mapper.selectAllLessons(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public int closeAdminMatch(Long matchId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        if (TutoringStatus.MATCH_APPLIED.equals(match.getStatus()))
        {
            if (mapper.updateMatchStatusIfCurrent(matchId, TutoringStatus.MATCH_APPLIED,
                TutoringStatus.MATCH_CANCELLED, username) != 1)
            {
                throw new ServiceException("订单状态已变化，请刷新后重试");
            }
        }
        else if (TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus()))
        {
            if (mapper.updateMatchStatusIfCurrent(matchId, TutoringStatus.MATCH_ACCEPTED,
                TutoringStatus.MATCH_CANCELLED, username) != 1
                || mapper.updateRequestStatusIfCurrent(match.getRequestId(), TutoringStatus.REQUEST_MATCHED,
                    TutoringStatus.REQUEST_CANCELLED, username) != 1)
            {
                throw new ServiceException("订单状态已变化，请刷新后重试");
            }
        }
        else
        {
            throw new ServiceException("只能关闭申请中或已接单的订单");
        }
        notify(match.getPublisherId(), "订单已关闭", "管理员已关闭“" + match.getSubject() + "”家教订单");
        notify(match.getTutorId(), "订单已关闭", "管理员已关闭“" + match.getSubject() + "”家教订单");
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int apply(Long requestId, TutoringMatch match, Long userId, String username)
    {
        TutoringRequest request = requireRequest(requestId);
        if (!TutoringStatus.REQUEST_OPEN.equals(request.getStatus()))
        {
            throw new ServiceException("该需求已停止招募");
        }
        TutorProfile profile = mapper.selectProfileByUserId(userId);
        if (profile == null || !"1".equals(profile.getVerifyStatus()))
        {
            throw new ServiceException("教员资料审核通过后才能申请");
        }
        if (mapper.countMatch(requestId, userId) > 0)
        {
            throw new ServiceException("你已申请过该需求");
        }
        match.setRequestId(requestId);
        match.setTutorId(userId);
        match.setCreateBy(username);
        int rows = mapper.insertMatch(match);
        notify(request.getPublisherId(), "收到新的家教申请", "“" + request.getSubject() + "”需求收到一份教员申请");
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int withdraw(Long matchId, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        if (!userId.equals(match.getTutorId()))
        {
            throw new ServiceException("只能撤回自己的申请");
        }
        TutoringStatus.requireMatchTransition(match.getStatus(), TutoringStatus.MATCH_CANCELLED);
        if (mapper.updateMatchStatusIfCurrent(matchId, TutoringStatus.MATCH_APPLIED,
            TutoringStatus.MATCH_CANCELLED, username) != 1)
        {
            throw new ServiceException("申请状态已变化，请刷新后重试");
        }
        notify(match.getPublisherId(), "教员撤回申请", "“" + match.getSubject() + "”需求的一份申请已撤回");
        return 1;
    }

    public Map<String, Object> getDashboard()
    {
        Map<String, Object> dashboard = mapper.selectDashboardStats();
        dashboard.put("topSubjects", mapper.selectTopSubjects());
        return dashboard;
    }

    @Transactional(rollbackFor = Exception.class)
    public int cancelMatch(Long matchId, TutoringMatch cancel, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        TutoringStatus.requireMatchTransition(match.getStatus(), TutoringStatus.MATCH_CANCELLED);
        String reason = cancel == null ? "" : cancel.getCancelReason();
        if (reason == null || reason.trim().isEmpty())
        {
            throw new ServiceException("请填写取消原因");
        }
        if (mapper.cancelMatchIfCurrent(matchId, TutoringStatus.MATCH_ACCEPTED, reason.trim(), username) != 1
            || mapper.updateRequestStatusIfCurrent(match.getRequestId(), TutoringStatus.REQUEST_MATCHED,
                TutoringStatus.REQUEST_CANCELLED, username) != 1)
        {
            throw new ServiceException("订单状态已变化，请刷新后重试");
        }
        Long target = userId.equals(match.getTutorId()) ? match.getPublisherId() : match.getTutorId();
        notify(target, "订单已取消", "“" + match.getSubject() + "”家教订单已取消，原因：" + reason.trim());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int rescheduleMatch(Long matchId, TutoringMatch change, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (!TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus()))
        {
            throw new ServiceException("只有进行中的订单可以改期");
        }
        String schedule = change == null ? "" : change.getRescheduleText();
        if (schedule == null || schedule.trim().isEmpty())
        {
            throw new ServiceException("请填写调整后的时间");
        }
        mapper.updateRequestSchedule(match.getRequestId(), schedule.trim(), username);
        mapper.updateMatchRescheduleText(matchId, schedule.trim(), username);
        Long target = userId.equals(match.getTutorId()) ? match.getPublisherId() : match.getTutorId();
        notify(target, "订单时间已调整", "“" + match.getSubject() + "”家教订单时间调整为：" + schedule.trim());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int scheduleTrial(Long matchId, TutoringMatch trial, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (!TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus()))
        {
            throw new ServiceException("只有进行中的订单可以安排试听课");
        }
        if (trial == null || blank(trial.getTrialTime()))
        {
            throw new ServiceException("请填写试听课时间");
        }
        trial.setMatchId(matchId);
        trial.setUpdateBy(username);
        if (mapper.scheduleTrial(trial) != 1)
        {
            throw new ServiceException("试听课安排失败");
        }
        Long target = userId.equals(match.getTutorId()) ? match.getPublisherId() : match.getTutorId();
        notify(target, "试听课已安排", match.getSubject() + " 试听课时间：" + trial.getTrialTime());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int completeTrial(Long matchId, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (!TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus()))
        {
            throw new ServiceException("只有进行中的订单可以完成试听课");
        }
        if (mapper.completeTrial(matchId, username) != 1)
        {
            throw new ServiceException("请先安排试听课");
        }
        Long target = userId.equals(match.getTutorId()) ? match.getPublisherId() : match.getTutorId();
        notify(target, "试听课已完成", match.getSubject() + " 试听课已标记完成");
        return 1;
    }

    public Map<String, Object> getDashboardTodos()
    {
        return mapper.selectAdminTodos();
    }

    @Transactional(rollbackFor = Exception.class)
    public int accept(Long matchId, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        if (!userId.equals(match.getPublisherId()))
        {
            throw new ServiceException("只能接受自己发布需求下的申请");
        }
        TutoringRequest request = requireRequest(match.getRequestId());
        TutoringStatus.requireRequestTransition(request.getStatus(), TutoringStatus.REQUEST_MATCHED);
        TutoringStatus.requireMatchTransition(match.getStatus(), TutoringStatus.MATCH_ACCEPTED);
        if (mapper.updateRequestStatusIfCurrent(request.getRequestId(), TutoringStatus.REQUEST_OPEN,
            TutoringStatus.REQUEST_MATCHED, username) != 1)
        {
            throw new ServiceException("该需求已被处理，请刷新后重试");
        }
        if (mapper.updateMatchStatusIfCurrent(matchId, TutoringStatus.MATCH_APPLIED,
            TutoringStatus.MATCH_ACCEPTED, username) != 1)
        {
            throw new ServiceException("该申请已被处理，请刷新后重试");
        }
        mapper.rejectOtherMatches(request.getRequestId(), matchId, username);
        notify(match.getTutorId(), "申请已接受", "家长已接受你的“" + match.getSubject() + "”家教申请");
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int complete(Long matchId, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        if (!userId.equals(match.getTutorId()))
        {
            throw new ServiceException("只能完成自己的家教订单");
        }
        TutoringRequest request = requireRequest(match.getRequestId());
        TutoringStatus.requireMatchTransition(match.getStatus(), TutoringStatus.MATCH_COMPLETED);
        TutoringStatus.requireRequestTransition(request.getStatus(), TutoringStatus.REQUEST_COMPLETED);
        if (mapper.countConfirmedLessons(matchId) == 0)
        {
            throw new ServiceException("至少确认一条课时记录后才能完成订单");
        }
        if (mapper.updateMatchStatusIfCurrent(matchId, TutoringStatus.MATCH_ACCEPTED,
            TutoringStatus.MATCH_COMPLETED, username) != 1
            || mapper.updateRequestStatusIfCurrent(request.getRequestId(), TutoringStatus.REQUEST_MATCHED,
                TutoringStatus.REQUEST_COMPLETED, username) != 1)
        {
            throw new ServiceException("订单状态已变化，请刷新后重试");
        }
        notify(match.getPublisherId(), "订单已完成", "教员已完成“" + match.getSubject() + "”家教订单，请及时评价");
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int review(Long matchId, TutoringMatch review, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        if (!userId.equals(match.getPublisherId()))
        {
            throw new ServiceException("只能评价自己发布需求产生的订单");
        }
        if (!TutoringStatus.MATCH_COMPLETED.equals(match.getStatus()))
        {
            throw new ServiceException("订单完成后才能评价");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5)
        {
            throw new ServiceException("评分必须在1到5之间");
        }
        review.setMatchId(matchId);
        review.setUpdateBy(username);
        int rows = mapper.reviewMatch(review);
        if (rows == 0)
        {
            throw new ServiceException("该订单已经评价");
        }
        notify(match.getTutorId(), "收到家长评价", "“" + match.getSubject() + "”订单获得 " + review.getRating() + " 星评价");
        return rows;
    }

    public List<TutoringLesson> getLessons(Long matchId, Long userId)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        return mapper.selectLessonsByMatchId(matchId);
    }

    public List<TutoringMaterial> getMaterials(Long matchId, Long userId)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        return mapper.selectMaterialsByMatchId(matchId);
    }

    public int addMaterial(Long matchId, TutoringMaterial material, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (material == null || blank(material.getTitle()) || blank(material.getFileUrl()))
        {
            throw new ServiceException("请填写资料标题和链接");
        }
        material.setMatchId(matchId);
        material.setUploaderId(userId);
        material.setCreateBy(username);
        int rows = mapper.insertMaterial(material);
        Long target = userId.equals(match.getTutorId()) ? match.getPublisherId() : match.getTutorId();
        notify(target, "课程资料已更新", match.getSubject() + " 订单新增课程资料");
        return rows;
    }

    public List<TutoringMessage> getMessages(Long matchId, Long userId)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        return mapper.selectMessagesByMatchId(matchId);
    }

    public int addMessage(Long matchId, TutoringMessage message, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (message == null || blank(message.getContent()))
        {
            throw new ServiceException("请输入留言内容");
        }
        message.setMatchId(matchId);
        message.setSenderId(userId);
        message.setCreateBy(username);
        int rows = mapper.insertMessage(message);
        Long target = userId.equals(match.getTutorId()) ? match.getPublisherId() : match.getTutorId();
        notify(target, "订单收到新留言", "“" + match.getSubject() + "”订单收到一条新留言");
        return rows;
    }

    public List<TutoringPayment> getPayments(Long matchId, Long userId)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        return mapper.selectPaymentsByMatchId(matchId);
    }

    public int addPayment(Long matchId, TutoringPayment payment, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (!userId.equals(match.getPublisherId()))
        {
            throw new ServiceException("只能为自己发布需求产生的订单提交付款凭证");
        }
        if (!TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus())
            && !TutoringStatus.MATCH_COMPLETED.equals(match.getStatus()))
        {
            throw new ServiceException("只有进行中或已完成的订单可以提交付款凭证");
        }
        if (payment == null || payment.getAmount() == null
            || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0 || blank(payment.getProofUrl()))
        {
            throw new ServiceException("请填写付款金额并上传凭证");
        }
        payment.setMatchId(matchId);
        payment.setPayerId(userId);
        payment.setStatus("0");
        payment.setCreateBy(username);
        int rows = mapper.insertPayment(payment);
        notify(match.getTutorId(), "家长已提交付款凭证", "“" + match.getSubject() + "”订单有新的付款凭证待平台确认");
        return rows;
    }

    public int mockPayment(Long matchId, TutoringPayment payment, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (!userId.equals(match.getPublisherId()))
        {
            throw new ServiceException("只能为自己发布需求产生的订单付款");
        }
        if (payment == null || payment.getAmount() == null
            || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("请填写付款金额");
        }
        payment.setMatchId(matchId);
        payment.setPayerId(userId);
        payment.setProofUrl("平台模拟支付");
        payment.setRemark(blank(payment.getRemark()) ? "模拟支付成功" : payment.getRemark());
        payment.setStatus("1");
        payment.setCreateBy(username);
        int rows = mapper.insertPayment(payment);
        notify(match.getTutorId(), "订单付款已完成", "“" + match.getSubject() + "”订单收到平台模拟支付");
        return rows;
    }

    public List<TutoringPayment> getAdminPayments(TutoringPayment query)
    {
        return mapper.selectAllPayments(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public int handlePayment(Long paymentId, TutoringPayment handling, String username)
    {
        if (handling == null || (!"1".equals(handling.getStatus()) && !"2".equals(handling.getStatus())))
        {
            throw new ServiceException("付款处理结果不正确");
        }
        TutoringPayment payment = mapper.selectPaymentById(paymentId);
        if (payment == null)
        {
            throw new ServiceException("付款流水不存在");
        }
        handling.setPaymentId(paymentId);
        handling.setHandleBy(username);
        if (mapper.handlePayment(handling) != 1)
        {
            throw new ServiceException("该付款流水已处理");
        }
        TutoringMatch match = requireMatch(payment.getMatchId());
        notify(payment.getPayerId(), "付款凭证已处理", "你的“" + payment.getSubject() + "”订单付款凭证已处理");
        if ("1".equals(handling.getStatus()))
        {
            notify(match.getTutorId(), "订单付款已确认", "“" + payment.getSubject() + "”订单付款已由平台确认");
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int addLesson(Long matchId, TutoringLesson lesson, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        if (!userId.equals(match.getTutorId()))
        {
            throw new ServiceException("只能为自己的订单添加上课记录");
        }
        if (!TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus()))
        {
            throw new ServiceException("只有进行中的订单可以添加上课记录");
        }
        lesson.setMatchId(matchId);
        lesson.setAmount(match.getQuotedRate().multiply(lesson.getHours()).setScale(2, RoundingMode.HALF_UP));
        lesson.setCreateBy(username);
        int rows = mapper.insertLesson(lesson);
        notify(match.getPublisherId(), "新增上课记录", "教员记录了“" + match.getSubject() + "”课程：" + lesson.getHours() + "课时");
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int confirmLesson(Long matchId, Long lessonId, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        if (!userId.equals(match.getPublisherId()))
        {
            throw new ServiceException("只能确认自己订单下的课时");
        }
        if (mapper.confirmLesson(matchId, lessonId, username) != 1)
        {
            throw new ServiceException("课时不存在或已确认");
        }
        notify(match.getTutorId(), "课时已确认", "“" + match.getSubject() + "”订单有一条课时已被家长确认");
        mapper.insertSettlementForLesson(lessonId);
        return 1;
    }

    public List<TutoringSettlement> getMySettlements(Long userId)
    {
        return mapper.selectSettlementsByTutorId(userId);
    }

    public List<TutoringFollowup> getAdminFollowups(TutoringFollowup query)
    {
        return mapper.selectAllFollowups(query);
    }

    public int addFollowup(Long matchId, TutoringFollowup followup, String username)
    {
        requireMatch(matchId);
        if (followup == null || blank(followup.getContent()))
        {
            throw new ServiceException("请填写回访内容");
        }
        followup.setMatchId(matchId);
        followup.setStatus(blank(followup.getStatus()) ? "0" : followup.getStatus());
        followup.setCreateBy(username);
        return mapper.insertFollowup(followup);
    }

    public List<TutoringSettlement> getAdminSettlements(TutoringSettlement query)
    {
        return mapper.selectAllSettlements(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public int settleSettlement(Long settlementId, String username)
    {
        TutoringSettlement settlement = mapper.selectSettlementById(settlementId);
        if (settlement == null)
        {
            throw new ServiceException("结算记录不存在");
        }
        if (mapper.settleSettlement(settlementId, username) != 1)
        {
            throw new ServiceException("该结算已处理");
        }
        notify(settlement.getTutorId(), "课时费已结算", "订单 " + settlement.getMatchId() + " 课时费已结算");
        return 1;
    }

    public List<TutoringNotification> getNotifications(Long userId, TutoringNotification query)
    {
        String readStatus = query == null ? null : query.getReadStatus();
        String title = query == null ? null : query.getTitle();
        return mapper.selectNotificationsByUserId(userId, readStatus, title);
    }

    public int readNotification(Long notificationId, Long userId)
    {
        return mapper.readNotification(notificationId, userId);
    }

    public int readAllNotifications(Long userId)
    {
        return mapper.readAllNotifications(userId);
    }

    public int countUnreadNotifications(Long userId)
    {
        return mapper.countUnreadNotifications(userId);
    }

    public List<TutoringComplaint> getMyComplaints(Long userId)
    {
        return mapper.selectComplaintsByUserId(userId);
    }

    public List<TutoringComplaint> getComplaints()
    {
        return mapper.selectComplaints();
    }

    @Transactional(rollbackFor = Exception.class)
    public int complain(Long matchId, TutoringComplaint complaint, Long userId, String username)
    {
        TutoringMatch match = requireMatch(matchId);
        requireParticipant(match, userId);
        if (!TutoringStatus.MATCH_ACCEPTED.equals(match.getStatus())
            && !TutoringStatus.MATCH_COMPLETED.equals(match.getStatus()))
        {
            throw new ServiceException("只有进行中或已完成的订单可以投诉");
        }
        complaint.setMatchId(matchId);
        complaint.setComplainantId(userId);
        int rows = mapper.insertComplaint(complaint);
        if (rows == 0)
        {
            throw new ServiceException("你已投诉过该订单");
        }
        mapper.insertComplaintLog(complaint.getComplaintId(), "提交投诉：" + complaint.getReason(), username);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int handleComplaint(Long complaintId, TutoringComplaint handling, String username)
    {
        if (!"1".equals(handling.getStatus()) && !"2".equals(handling.getStatus()))
        {
            throw new ServiceException("处理结果只能是已解决或已驳回");
        }
        TutoringComplaint complaint = mapper.selectComplaintById(complaintId);
        if (complaint == null)
        {
            throw new ServiceException("投诉不存在");
        }
        handling.setComplaintId(complaintId);
        handling.setHandleBy(username);
        if (mapper.handleComplaint(handling) != 1)
        {
            throw new ServiceException("该投诉已经处理");
        }
        mapper.insertComplaintLog(complaintId, handling.getHandleRemark(), username);
        notify(complaint.getComplainantId(), "投诉处理完成", "你的订单投诉已处理，请查看处理意见");
        return 1;
    }

    public int submitTicket(TutoringTicket ticket, Long userId, String username)
    {
        if (ticket == null || blank(ticket.getTitle()) || blank(ticket.getContent()))
        {
            throw new ServiceException("请填写工单标题和问题描述");
        }
        ticket.setUserId(userId);
        ticket.setCreateBy(username);
        return mapper.insertTicket(ticket);
    }

    public List<TutoringTicket> getMyTickets(Long userId)
    {
        return mapper.selectTicketsByUserId(userId);
    }

    public List<TutoringTicket> getAdminTickets(TutoringTicket query)
    {
        return mapper.selectAllTickets(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public int handleTicket(Long ticketId, TutoringTicket handling, String username)
    {
        if (handling == null || (!"1".equals(handling.getStatus()) && !"2".equals(handling.getStatus())))
        {
            throw new ServiceException("工单处理结果不正确");
        }
        TutoringTicket ticket = mapper.selectTicketById(ticketId);
        if (ticket == null)
        {
            throw new ServiceException("工单不存在");
        }
        handling.setTicketId(ticketId);
        handling.setHandleBy(username);
        if (mapper.handleTicket(handling) != 1)
        {
            throw new ServiceException("该工单已处理");
        }
        notify(ticket.getUserId(), "客服工单已处理", "你的工单“" + ticket.getTitle() + "”已处理");
        return 1;
    }

    public List<TutoringRequest> getRecommendedRequests(Long userId)
    {
        getVerifiedProfile(userId);
        return mapper.selectRecommendedRequests(userId);
    }

    public List<TutorProfile> getFavoriteTutors(Long userId)
    {
        List<TutorProfile> profiles = mapper.selectFavoriteTutors(userId);
        profiles.forEach(profile -> {
            profile.setStudentCardUrl(null);
            profile.setQualificationUrl(null);
        });
        return profiles;
    }

    public int favoriteTutor(Long tutorId, Long userId)
    {
        if (userId.equals(tutorId))
        {
            throw new ServiceException("不能收藏自己");
        }
        getVerifiedProfile(tutorId);
        if (mapper.insertFavorite(userId, tutorId) == 0)
        {
            throw new ServiceException("该教员已收藏");
        }
        return 1;
    }

    public int unfavoriteTutor(Long tutorId, Long userId)
    {
        return mapper.deleteFavorite(userId, tutorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int inviteTutor(Long tutorId, TutoringInvitation invitation, Long userId, String username)
    {
        if (userId.equals(tutorId))
        {
            throw new ServiceException("不能预约自己");
        }
        getVerifiedProfile(tutorId);
        invitation.setPublisherId(userId);
        invitation.setTutorId(tutorId);
        invitation.setCreateBy(username);
        int rows = mapper.insertInvitation(invitation);
        notify(tutorId, "收到家教预约", "家长向你发起“" + invitation.getSubject() + "”家教预约");
        return rows;
    }

    public List<TutoringInvitation> getInvitations(Long userId)
    {
        return mapper.selectInvitationsByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int respondInvitation(Long invitationId, boolean accept, Long userId, String username)
    {
        TutoringInvitation invitation = mapper.selectInvitationById(invitationId);
        if (invitation == null)
        {
            throw new ServiceException("预约不存在");
        }
        if (!userId.equals(invitation.getTutorId()))
        {
            throw new ServiceException("只能处理发给自己的预约");
        }
        String target = accept ? "1" : "2";
        if (mapper.updateInvitationStatusIfCurrent(invitationId, target, username) != 1)
        {
            throw new ServiceException("该预约已经处理");
        }
        if (accept)
        {
            TutoringRequest request = new TutoringRequest();
            request.setPublisherId(invitation.getPublisherId());
            request.setLearnerGrade(invitation.getLearnerGrade());
            request.setSubject(invitation.getSubject());
            request.setArea(invitation.getArea());
            request.setScheduleText(invitation.getScheduleText());
            request.setHourlyBudget(invitation.getOfferedRate());
            request.setRequirementText(invitation.getMessage());
            request.setCreateBy(invitation.getCreateBy());
            mapper.insertRequest(request);
            if (mapper.updateRequestStatusIfCurrent(request.getRequestId(), TutoringStatus.REQUEST_OPEN,
                TutoringStatus.REQUEST_MATCHED, username) != 1)
            {
                throw new ServiceException("创建预约订单失败");
            }

            TutoringMatch match = new TutoringMatch();
            match.setRequestId(request.getRequestId());
            match.setTutorId(userId);
            match.setQuotedRate(invitation.getOfferedRate());
            match.setApplicationText("接受家长直接预约");
            match.setCreateBy(username);
            mapper.insertMatch(match);
            if (mapper.updateMatchStatusIfCurrent(match.getMatchId(), TutoringStatus.MATCH_APPLIED,
                TutoringStatus.MATCH_ACCEPTED, username) != 1)
            {
                throw new ServiceException("创建预约订单失败");
            }
        }
        notify(invitation.getPublisherId(), accept ? "预约已接受" : "预约已拒绝",
            "教员已" + (accept ? "接受" : "拒绝") + "你的“" + invitation.getSubject() + "”预约");
        return 1;
    }

    private boolean blank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private void notify(Long userId, String title, String content)
    {
        mapper.insertNotification(userId, title, content);
    }

    private void requireParticipant(TutoringMatch match, Long userId)
    {
        if (!userId.equals(match.getTutorId()) && !userId.equals(match.getPublisherId()))
        {
            throw new ServiceException("只能查看或操作自己的订单");
        }
    }

    private TutoringRequest requireRequest(Long requestId)
    {
        TutoringRequest request = mapper.selectRequestById(requestId);
        if (request == null)
        {
            throw new ServiceException("家教需求不存在");
        }
        return request;
    }

    private TutoringMatch requireMatch(Long matchId)
    {
        TutoringMatch match = mapper.selectMatchById(matchId);
        if (match == null)
        {
            throw new ServiceException("家教订单不存在");
        }
        return match;
    }
}
