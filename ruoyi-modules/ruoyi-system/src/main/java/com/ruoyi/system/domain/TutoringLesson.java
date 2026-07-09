package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TutoringLesson extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long lessonId;
    private Long matchId;
    private String subject;
    private String publisherName;
    private String tutorName;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal hours;
    private String content;
    private BigDecimal amount;
    private String studentPerformance;
    private String homework;
    private String nextPlan;
    private String attendanceStatus;
    private String phaseFeedback;
    private String confirmStatus;
    private String confirmBy;
    private Date confirmTime;

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    @NotNull(message = "上课日期不能为空")
    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    @NotNull(message = "课时数不能为空")
    @DecimalMin(value = "0.5", message = "课时数不能少于0.5")
    @DecimalMax(value = "24", message = "单次课时数不能超过24")
    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }

    @NotBlank(message = "授课内容不能为空")
    @Size(max = 500, message = "授课内容长度不能超过500个字符")
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStudentPerformance() { return studentPerformance; }
    public void setStudentPerformance(String studentPerformance) { this.studentPerformance = studentPerformance; }
    public String getHomework() { return homework; }
    public void setHomework(String homework) { this.homework = homework; }
    public String getNextPlan() { return nextPlan; }
    public void setNextPlan(String nextPlan) { this.nextPlan = nextPlan; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public String getPhaseFeedback() { return phaseFeedback; }
    public void setPhaseFeedback(String phaseFeedback) { this.phaseFeedback = phaseFeedback; }
    public String getConfirmStatus() { return confirmStatus; }
    public void setConfirmStatus(String confirmStatus) { this.confirmStatus = confirmStatus; }
    public String getConfirmBy() { return confirmBy; }
    public void setConfirmBy(String confirmBy) { this.confirmBy = confirmBy; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
}
