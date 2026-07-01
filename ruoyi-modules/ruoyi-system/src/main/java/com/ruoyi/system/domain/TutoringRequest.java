package com.ruoyi.system.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TutoringRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long requestId;
    private Long publisherId;
    private String publisherName;
    private String learnerGrade;
    private String subject;
    private String area;
    private String scheduleText;
    private BigDecimal hourlyBudget;
    private String requirementText;
    private String status;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

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

    @NotNull(message = "课时预算不能为空")
    @DecimalMin(value = "0.01", message = "课时预算必须大于0")
    public BigDecimal getHourlyBudget() { return hourlyBudget; }
    public void setHourlyBudget(BigDecimal hourlyBudget) { this.hourlyBudget = hourlyBudget; }

    @Size(max = 500, message = "补充要求长度不能超过500个字符")
    public String getRequirementText() { return requirementText; }
    public void setRequirementText(String requirementText) { this.requirementText = requirementText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
