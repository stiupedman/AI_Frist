package com.ruoyi.system.domain;

import java.util.Date;
import com.ruoyi.common.core.web.domain.BaseEntity;

public class TutoringHomework extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long homeworkId;
    private Long matchId;
    private Long lessonId;
    private String subject;
    private String publisherName;
    private String tutorName;
    private String title;
    private String content;
    private String submitText;
    private String feedback;
    private String status;
    private String assignBy;
    private String submitBy;
    private Date submitTime;
    private String checkBy;
    private Date checkTime;

    public Long getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSubmitText() { return submitText; }
    public void setSubmitText(String submitText) { this.submitText = submitText; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignBy() { return assignBy; }
    public void setAssignBy(String assignBy) { this.assignBy = assignBy; }
    public String getSubmitBy() { return submitBy; }
    public void setSubmitBy(String submitBy) { this.submitBy = submitBy; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public String getCheckBy() { return checkBy; }
    public void setCheckBy(String checkBy) { this.checkBy = checkBy; }
    public Date getCheckTime() { return checkTime; }
    public void setCheckTime(Date checkTime) { this.checkTime = checkTime; }
}
