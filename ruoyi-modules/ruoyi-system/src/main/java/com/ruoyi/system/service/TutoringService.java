package com.ruoyi.system.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.TutorProfile;
import com.ruoyi.system.domain.TutoringMatch;
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

    public int verifyProfile(TutorProfile profile, String username)
    {
        if (!"1".equals(profile.getVerifyStatus()) && !"2".equals(profile.getVerifyStatus()))
        {
            throw new ServiceException("审核状态只能是通过或驳回");
        }
        profile.setUpdateBy(username);
        int rows = mapper.verifyProfile(profile);
        if (rows == 0)
        {
            throw new ServiceException("资料不存在或已完成审核");
        }
        return rows;
    }

    public List<TutoringRequest> getOpenRequests()
    {
        return mapper.selectOpenRequests();
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
        return mapper.insertMatch(match);
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
        return 1;
    }

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
        return rows;
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
