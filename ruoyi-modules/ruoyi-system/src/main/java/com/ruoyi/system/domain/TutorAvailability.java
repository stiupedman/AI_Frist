package com.ruoyi.system.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;

public class TutorAvailability extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long availabilityId;
    private Long userId;
    private String weekDay;
    private String startTime;
    private String endTime;
    private String availableStatus;
    private String remark;

    public Long getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(Long availabilityId) { this.availabilityId = availabilityId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getWeekDay() { return weekDay; }
    public void setWeekDay(String weekDay) { this.weekDay = weekDay; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getAvailableStatus() { return availableStatus; }
    public void setAvailableStatus(String availableStatus) { this.availableStatus = availableStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
