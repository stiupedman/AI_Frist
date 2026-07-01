package com.ruoyi.system.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TutorProfile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long profileId;
    private Long userId;
    private String userName;
    private String university;
    private String major;
    private String collegeYear;
    private String subjects;
    private BigDecimal hourlyRate;
    private String introduction;
    private String verifyStatus;
    private String verifyRemark;

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    @NotBlank(message = "学校不能为空")
    @Size(max = 100, message = "学校长度不能超过100个字符")
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    @NotBlank(message = "专业不能为空")
    @Size(max = 100, message = "专业长度不能超过100个字符")
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    @NotBlank(message = "年级不能为空")
    @Size(max = 30, message = "年级长度不能超过30个字符")
    public String getCollegeYear() { return collegeYear; }
    public void setCollegeYear(String collegeYear) { this.collegeYear = collegeYear; }

    @NotBlank(message = "擅长科目不能为空")
    @Size(max = 200, message = "擅长科目长度不能超过200个字符")
    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    @NotNull(message = "课时费不能为空")
    @DecimalMin(value = "0.01", message = "课时费必须大于0")
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(String verifyStatus) { this.verifyStatus = verifyStatus; }
    public String getVerifyRemark() { return verifyRemark; }
    public void setVerifyRemark(String verifyRemark) { this.verifyRemark = verifyRemark; }
}
