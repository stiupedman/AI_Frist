package com.ruoyi.system.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.TutorProfile;
import com.ruoyi.system.domain.TutoringMatch;
import com.ruoyi.system.domain.TutoringRequest;
import com.ruoyi.system.service.TutoringService;

@RestController
@RequestMapping("/tutoring")
public class TutoringController extends BaseController
{
    @Autowired
    private TutoringService service;

    @RequiresPermissions("tutoring:profile:edit")
    @GetMapping("/profile/me")
    public AjaxResult myProfile()
    {
        return success(service.getProfile(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:profile:edit")
    @Log(title = "教员资料", businessType = BusinessType.UPDATE)
    @PutMapping("/profile/me")
    public AjaxResult saveProfile(@Validated @RequestBody TutorProfile profile)
    {
        return toAjax(service.saveProfile(profile, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:profile:verify")
    @GetMapping("/profiles/pending")
    public TableDataInfo pendingProfiles()
    {
        startPage();
        return getDataTable(service.getPendingProfiles());
    }

    @RequiresPermissions("tutoring:profile:verify")
    @Log(title = "教员审核", businessType = BusinessType.UPDATE)
    @PutMapping("/profiles/{profileId}/verify")
    public AjaxResult verifyProfile(@PathVariable Long profileId, @RequestBody TutorProfile profile)
    {
        profile.setProfileId(profileId);
        return toAjax(service.verifyProfile(profile, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:request:list")
    @GetMapping("/requests/open")
    public TableDataInfo openRequests()
    {
        startPage();
        List<TutoringRequest> list = service.getOpenRequests();
        return getDataTable(list);
    }

    @RequiresPermissions("tutoring:request:add")
    @GetMapping("/requests/mine")
    public TableDataInfo myRequests()
    {
        startPage();
        return getDataTable(service.getMyRequests(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:request:add")
    @Log(title = "家教需求", businessType = BusinessType.INSERT)
    @PostMapping("/requests")
    public AjaxResult publishRequest(@Validated @RequestBody TutoringRequest request)
    {
        return toAjax(service.publishRequest(request, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:list")
    @GetMapping("/matches/mine")
    public TableDataInfo myMatches()
    {
        startPage();
        return getDataTable(service.getMyMatches(SecurityUtils.getUserId()));
    }

    @RequiresPermissions("tutoring:match:apply")
    @Log(title = "家教申请", businessType = BusinessType.INSERT)
    @PostMapping("/requests/{requestId}/apply")
    public AjaxResult apply(@PathVariable Long requestId, @Validated @RequestBody TutoringMatch match)
    {
        return toAjax(service.apply(requestId, match, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:accept")
    @Log(title = "接受教员申请", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/accept")
    public AjaxResult accept(@PathVariable Long matchId)
    {
        return toAjax(service.accept(matchId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:complete")
    @Log(title = "完成家教订单", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/complete")
    public AjaxResult complete(@PathVariable Long matchId)
    {
        return toAjax(service.complete(matchId, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    @RequiresPermissions("tutoring:match:review")
    @Log(title = "评价家教订单", businessType = BusinessType.UPDATE)
    @PutMapping("/matches/{matchId}/review")
    public AjaxResult review(@PathVariable Long matchId, @RequestBody TutoringMatch review)
    {
        return toAjax(service.review(matchId, review, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }
}
