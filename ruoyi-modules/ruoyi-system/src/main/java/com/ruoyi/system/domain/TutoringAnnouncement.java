package com.ruoyi.system.domain;

import java.util.Date;
import com.ruoyi.common.core.web.domain.BaseEntity;

public class TutoringAnnouncement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long announcementId;
    private String title;
    private String content;
    private String status;
    private Date publishTime;

    public Long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Long announcementId) { this.announcementId = announcementId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
}
