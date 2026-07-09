package com.ruoyi.system.domain;

import java.util.Date;
import com.ruoyi.common.core.web.domain.BaseEntity;

public class TutoringBlacklist extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long blacklistId;
    private Long userId;
    private String userName;
    private String reason;
    private String status;
    private String handleBy;
    private Date handleTime;

    public Long getBlacklistId() { return blacklistId; }
    public void setBlacklistId(Long blacklistId) { this.blacklistId = blacklistId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHandleBy() { return handleBy; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }
    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }
}
