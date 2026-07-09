package com.ruoyi.system.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;

public class TutoringFollowup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long followupId;
    private Long matchId;
    private String subject;
    private String publisherName;
    private String tutorName;
    private String content;
    private String nextAction;
    private String status;

    public Long getFollowupId() { return followupId; }
    public void setFollowupId(Long followupId) { this.followupId = followupId; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
