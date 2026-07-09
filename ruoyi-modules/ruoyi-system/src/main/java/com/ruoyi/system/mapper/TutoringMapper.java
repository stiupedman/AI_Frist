package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
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

public interface TutoringMapper
{
    TutorProfile selectProfileByUserId(Long userId);

    TutorProfile selectProfileById(Long profileId);

    List<TutorProfile> selectPendingProfiles();

    List<TutorProfile> selectVerifiedTutors(TutorProfile query);

    int insertProfile(TutorProfile profile);

    int updateProfile(TutorProfile profile);

    int verifyProfile(TutorProfile profile);

    List<TutoringLearner> selectLearnersByUserId(Long userId);

    int insertLearner(TutoringLearner learner);

    int updateLearner(TutoringLearner learner);

    int deleteLearner(@Param("learnerId") Long learnerId, @Param("userId") Long userId);

    List<TutorAvailability> selectAvailabilityByUserId(Long userId);

    int insertAvailability(TutorAvailability availability);

    int deleteAvailability(@Param("availabilityId") Long availabilityId, @Param("userId") Long userId);

    List<TutoringAnnouncement> selectActiveAnnouncements();

    List<TutoringAnnouncement> selectAllAnnouncements(TutoringAnnouncement query);

    int insertAnnouncement(TutoringAnnouncement announcement);

    int updateAnnouncement(TutoringAnnouncement announcement);

    int deleteAnnouncement(Long announcementId);

    List<TutoringRequest> selectOpenRequests(TutoringRequest query);

    List<TutoringRequest> selectAllRequests(TutoringRequest query);

    List<SysUser> selectAdminClients(SysUser query);

    List<TutorProfile> selectAdminTutors(TutorProfile query);

    int countTutoringUser(Long userId);

    List<TutoringRequest> selectRequestsByPublisherId(Long publisherId);

    TutoringRequest selectRequestById(Long requestId);

    int insertRequest(TutoringRequest request);

    int updateRequestStatusIfCurrent(@Param("requestId") Long requestId,
        @Param("current") String current, @Param("target") String target,
        @Param("updateBy") String updateBy);

    int updateRequestSchedule(@Param("requestId") Long requestId,
        @Param("scheduleText") String scheduleText, @Param("updateBy") String updateBy);

    int cancelOpenMatches(@Param("requestId") Long requestId, @Param("updateBy") String updateBy);

    TutoringMatch selectMatchById(Long matchId);

    List<TutoringMatch> selectMatchesByUserId(Long userId);

    List<TutoringMatch> selectAllMatches(TutoringMatch query);

    List<TutoringInvitation> selectAllInvitations(TutoringInvitation query);

    List<TutoringLesson> selectAllLessons(TutoringLesson query);

    int countMatch(@Param("requestId") Long requestId, @Param("tutorId") Long tutorId);

    int insertMatch(TutoringMatch match);

    int updateMatchStatusIfCurrent(@Param("matchId") Long matchId,
        @Param("current") String current, @Param("target") String target,
        @Param("updateBy") String updateBy);

    int cancelMatchIfCurrent(@Param("matchId") Long matchId,
        @Param("current") String current, @Param("cancelReason") String cancelReason,
        @Param("updateBy") String updateBy);

    int updateMatchRescheduleText(@Param("matchId") Long matchId,
        @Param("rescheduleText") String rescheduleText, @Param("updateBy") String updateBy);

    int scheduleTrial(TutoringMatch match);

    int completeTrial(@Param("matchId") Long matchId, @Param("updateBy") String updateBy);

    int rejectOtherMatches(@Param("requestId") Long requestId,
        @Param("acceptedMatchId") Long acceptedMatchId, @Param("updateBy") String updateBy);

    int reviewMatch(TutoringMatch match);

    Map<String, Object> selectDashboardStats();

    List<Map<String, Object>> selectTopSubjects();

    List<TutoringLesson> selectLessonsByMatchId(Long matchId);

    int insertLesson(TutoringLesson lesson);

    int confirmLesson(@Param("matchId") Long matchId, @Param("lessonId") Long lessonId,
        @Param("confirmBy") String confirmBy);

    int insertSettlementForLesson(Long lessonId);

    List<TutoringSettlement> selectSettlementsByTutorId(Long tutorId);

    List<TutoringSettlement> selectAllSettlements(TutoringSettlement query);

    TutoringSettlement selectSettlementById(Long settlementId);

    int settleSettlement(@Param("settlementId") Long settlementId, @Param("settleBy") String settleBy);

    int countConfirmedLessons(Long matchId);

    List<TutoringMaterial> selectMaterialsByMatchId(Long matchId);

    int insertMaterial(TutoringMaterial material);

    List<TutoringMessage> selectMessagesByMatchId(Long matchId);

    int insertMessage(TutoringMessage message);

    List<TutoringPayment> selectPaymentsByMatchId(Long matchId);

    List<TutoringPayment> selectAllPayments(TutoringPayment query);

    TutoringPayment selectPaymentById(Long paymentId);

    int insertPayment(TutoringPayment payment);

    int handlePayment(TutoringPayment payment);

    List<TutoringFollowup> selectAllFollowups(TutoringFollowup query);

    int insertFollowup(TutoringFollowup followup);

    int insertNotification(@Param("userId") Long userId, @Param("title") String title,
        @Param("content") String content);

    List<TutoringNotification> selectNotificationsByUserId(@Param("userId") Long userId,
        @Param("readStatus") String readStatus, @Param("title") String title);

    int readNotification(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    int readAllNotifications(Long userId);

    int countUnreadNotifications(Long userId);

    List<Long> selectOpenMatchTutorIds(Long requestId);

    int insertComplaint(TutoringComplaint complaint);

    int insertComplaintLog(@Param("complaintId") Long complaintId,
        @Param("actionText") String actionText, @Param("createBy") String createBy);

    List<TutoringComplaint> selectComplaintsByUserId(Long userId);

    List<TutoringComplaint> selectComplaints();

    TutoringComplaint selectComplaintById(Long complaintId);

    int handleComplaint(TutoringComplaint complaint);

    int insertTicket(TutoringTicket ticket);

    List<TutoringTicket> selectTicketsByUserId(Long userId);

    List<TutoringTicket> selectAllTickets(TutoringTicket query);

    TutoringTicket selectTicketById(Long ticketId);

    int handleTicket(TutoringTicket ticket);

    List<TutoringRequest> selectRecommendedRequests(Long tutorId);

    Map<String, Object> selectAdminTodos();

    int insertFavorite(@Param("userId") Long userId, @Param("tutorId") Long tutorId);

    int deleteFavorite(@Param("userId") Long userId, @Param("tutorId") Long tutorId);

    List<TutorProfile> selectFavoriteTutors(Long userId);

    int insertInvitation(TutoringInvitation invitation);

    List<TutoringInvitation> selectInvitationsByUserId(Long userId);

    TutoringInvitation selectInvitationById(Long invitationId);

    int updateInvitationStatusIfCurrent(@Param("invitationId") Long invitationId,
        @Param("target") String target, @Param("updateBy") String updateBy);
}
