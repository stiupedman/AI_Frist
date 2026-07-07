package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.TutorProfile;
import com.ruoyi.system.domain.TutoringComplaint;
import com.ruoyi.system.domain.TutoringInvitation;
import com.ruoyi.system.domain.TutoringLesson;
import com.ruoyi.system.domain.TutoringMatch;
import com.ruoyi.system.domain.TutoringNotification;
import com.ruoyi.system.domain.TutoringRequest;

public interface TutoringMapper
{
    TutorProfile selectProfileByUserId(Long userId);

    TutorProfile selectProfileById(Long profileId);

    List<TutorProfile> selectPendingProfiles();

    List<TutorProfile> selectVerifiedTutors(TutorProfile query);

    int insertProfile(TutorProfile profile);

    int updateProfile(TutorProfile profile);

    int verifyProfile(TutorProfile profile);

    List<TutoringRequest> selectOpenRequests(TutoringRequest query);

    List<TutoringRequest> selectAllRequests(TutoringRequest query);

    List<TutoringRequest> selectRequestsByPublisherId(Long publisherId);

    TutoringRequest selectRequestById(Long requestId);

    int insertRequest(TutoringRequest request);

    int updateRequestStatusIfCurrent(@Param("requestId") Long requestId,
        @Param("current") String current, @Param("target") String target,
        @Param("updateBy") String updateBy);

    int cancelOpenMatches(@Param("requestId") Long requestId, @Param("updateBy") String updateBy);

    TutoringMatch selectMatchById(Long matchId);

    List<TutoringMatch> selectMatchesByUserId(Long userId);

    List<TutoringMatch> selectAllMatches(TutoringMatch query);

    int countMatch(@Param("requestId") Long requestId, @Param("tutorId") Long tutorId);

    int insertMatch(TutoringMatch match);

    int updateMatchStatusIfCurrent(@Param("matchId") Long matchId,
        @Param("current") String current, @Param("target") String target,
        @Param("updateBy") String updateBy);

    int rejectOtherMatches(@Param("requestId") Long requestId,
        @Param("acceptedMatchId") Long acceptedMatchId, @Param("updateBy") String updateBy);

    int reviewMatch(TutoringMatch match);

    Map<String, Object> selectDashboardStats();

    List<Map<String, Object>> selectTopSubjects();

    List<TutoringLesson> selectLessonsByMatchId(Long matchId);

    int insertLesson(TutoringLesson lesson);

    int insertNotification(@Param("userId") Long userId, @Param("title") String title,
        @Param("content") String content);

    List<TutoringNotification> selectNotificationsByUserId(Long userId);

    int readNotification(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    List<Long> selectOpenMatchTutorIds(Long requestId);

    int insertComplaint(TutoringComplaint complaint);

    List<TutoringComplaint> selectComplaintsByUserId(Long userId);

    List<TutoringComplaint> selectComplaints();

    TutoringComplaint selectComplaintById(Long complaintId);

    int handleComplaint(TutoringComplaint complaint);

    List<TutoringRequest> selectRecommendedRequests(Long tutorId);

    int insertFavorite(@Param("userId") Long userId, @Param("tutorId") Long tutorId);

    int deleteFavorite(@Param("userId") Long userId, @Param("tutorId") Long tutorId);

    List<TutorProfile> selectFavoriteTutors(Long userId);

    int insertInvitation(TutoringInvitation invitation);

    List<TutoringInvitation> selectInvitationsByUserId(Long userId);

    TutoringInvitation selectInvitationById(Long invitationId);

    int updateInvitationStatusIfCurrent(@Param("invitationId") Long invitationId,
        @Param("target") String target, @Param("updateBy") String updateBy);
}
