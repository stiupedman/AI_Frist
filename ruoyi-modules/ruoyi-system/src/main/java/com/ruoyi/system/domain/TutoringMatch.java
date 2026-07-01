package com.ruoyi.system.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TutoringMatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long matchId;
    private Long requestId;
    private Long tutorId;
    private String tutorName;
    private Long publisherId;
    private String subject;
    private BigDecimal quotedRate;
    private String applicationText;
    private String status;
    private Integer rating;
    private String reviewText;

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    @NotNull(message = "报价不能为空")
    @DecimalMin(value = "0.01", message = "报价必须大于0")
    public BigDecimal getQuotedRate() { return quotedRate; }
    public void setQuotedRate(BigDecimal quotedRate) { this.quotedRate = quotedRate; }

    @Size(max = 500, message = "申请说明长度不能超过500个字符")
    public String getApplicationText() { return applicationText; }
    public void setApplicationText(String applicationText) { this.applicationText = applicationText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Min(value = 1, message = "评分不能低于1")
    @Max(value = 5, message = "评分不能高于5")
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    @Size(max = 500, message = "评价长度不能超过500个字符")
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}
