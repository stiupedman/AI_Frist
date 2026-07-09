package com.ruoyi.system.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;

public class TutoringLearner extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long learnerId;
    private Long userId;
    private String learnerName;
    private String grade;
    private String school;
    private String weakSubjects;
    private String targetScore;
    private String availableTime;
    private String remark;

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLearnerName() { return learnerName; }
    public void setLearnerName(String learnerName) { this.learnerName = learnerName; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getWeakSubjects() { return weakSubjects; }
    public void setWeakSubjects(String weakSubjects) { this.weakSubjects = weakSubjects; }
    public String getTargetScore() { return targetScore; }
    public void setTargetScore(String targetScore) { this.targetScore = targetScore; }
    public String getAvailableTime() { return availableTime; }
    public void setAvailableTime(String availableTime) { this.availableTime = availableTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
