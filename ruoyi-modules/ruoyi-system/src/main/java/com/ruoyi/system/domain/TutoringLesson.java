package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private LocalDate lessonDate;
    private BigDecimal hours;
    private String content;
    private BigDecimal amount;

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    @NotNull(message = "上课日期不能为空")
    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }

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
}
