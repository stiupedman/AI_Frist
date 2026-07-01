package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.TutorProfile;
import com.ruoyi.system.domain.TutoringMatch;
import com.ruoyi.system.domain.TutoringRequest;

public interface TutoringMapper
{
    TutorProfile selectProfileByUserId(Long userId);

    List<TutorProfile> selectPendingProfiles();

    int insertProfile(TutorProfile profile);

    int updateProfile(TutorProfile profile);

    int verifyProfile(TutorProfile profile);

    List<TutoringRequest> selectOpenRequests();

    List<TutoringRequest> selectRequestsByPublisherId(Long publisherId);

    TutoringRequest selectRequestById(Long requestId);

    int insertRequest(TutoringRequest request);

    int updateRequestStatusIfCurrent(@Param("requestId") Long requestId,
        @Param("current") String current, @Param("target") String target,
        @Param("updateBy") String updateBy);

    TutoringMatch selectMatchById(Long matchId);

    List<TutoringMatch> selectMatchesByUserId(Long userId);

    int countMatch(@Param("requestId") Long requestId, @Param("tutorId") Long tutorId);

    int insertMatch(TutoringMatch match);

    int updateMatchStatusIfCurrent(@Param("matchId") Long matchId,
        @Param("current") String current, @Param("target") String target,
        @Param("updateBy") String updateBy);

    int rejectOtherMatches(@Param("requestId") Long requestId,
        @Param("acceptedMatchId") Long acceptedMatchId, @Param("updateBy") String updateBy);

    int reviewMatch(TutoringMatch match);
}
