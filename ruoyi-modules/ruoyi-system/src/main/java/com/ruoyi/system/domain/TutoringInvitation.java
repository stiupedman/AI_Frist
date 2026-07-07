package com.ruoyi.system.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TutoringInvitation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long invitationId;
    private Long publisherId;
    private String publisherName;
    private Long tutorId;
    private String tutorName;
    private String learnerGrade;
    private String subject;
    private String area;
    private String scheduleText;
    private BigDecimal offeredRate;
    private String message;
    private String status;

    public Long getInvitationId() { return invitationId; }
    public void setInvitationId(Long invitationId) { this.invitationId = invitationId; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    @NotBlank(message = "学员年级不能为空")
    @Size(max = 50, message = "学员年级长度不能超过50个字符")
    public String getLearnerGrade() { return learnerGrade; }
    public void setLearnerGrade(String learnerGrade) { this.learnerGrade = learnerGrade; }
    @NotBlank(message = "辅导科目不能为空")
    @Size(max = 50, message = "辅导科目长度不能超过50个字符")
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    @NotBlank(message = "授课区域不能为空")
    @Size(max = 100, message = "授课区域长度不能超过100个字符")
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    @NotBlank(message = "期望时间不能为空")
    @Size(max = 200, message = "期望时间长度不能超过200个字符")
    public String getScheduleText() { return scheduleText; }
    public void setScheduleText(String scheduleText) { this.scheduleText = scheduleText; }
    @NotNull(message = "预约课时费不能为空")
    @DecimalMin(value = "0.01", message = "预约课时费必须大于0")
    public BigDecimal getOfferedRate() { return offeredRate; }
    public void setOfferedRate(BigDecimal offeredRate) { this.offeredRate = offeredRate; }
    @Size(max = 500, message = "预约说明长度不能超过500个字符")
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
