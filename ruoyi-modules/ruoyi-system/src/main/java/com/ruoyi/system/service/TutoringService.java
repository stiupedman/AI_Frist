package com.ruoyi.system.service;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.TutorProfile;
import com.ruoyi.system.domain.TutoringComplaint;
import com.ruoyi.system.domain.TutoringInvitation;
import com.ruoyi.system.domain.TutoringLesson;
import com.ruoyi.system.domain.TutoringMatch;
import com.ruoyi.system.domain.TutoringNotification;
import com.ruoyi.system.domain.TutoringRequest;
import com.ruoyi.system.mapper.TutoringMapper;

@Service
public class TutoringService
{
    @Autowired
    private TutoringMapper mapper;

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

    public List<TutoringNotification> getNotifications(Long userId)
    {
        return mapper.selectNotificationsByUserId(userId);
    }

    public int readNotification(Long notificationId, Long userId)
    {
        return mapper.readNotification(notificationId, userId);
    }

    public List<TutoringComplaint> getMyComplaints(Long userId)
    {
        return mapper.selectComplaintsByUserId(userId);
    }

    public List<TutoringComplaint> getComplaints()
    {
        return mapper.selectComplaints();
    }

    public int complain(Long matchId, TutoringComplaint complaint, Long userId)
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
        if (mapper.insertComplaint(complaint) == 0)
        {
            throw new ServiceException("你已投诉过该订单");
        }
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
        notify(complaint.getComplainantId(), "投诉处理完成", "你的订单投诉已处理，请查看处理意见");
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
