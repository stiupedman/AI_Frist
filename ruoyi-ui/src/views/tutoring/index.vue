<template>
  <div class="app-container tutoring-page">
    <el-alert v-if="!workbenchRole" title="当前账号没有可用的家教工作台角色" type="warning" :closable="false" />

    <div v-else class="workbench-shell">
      <div class="workbench-hero">
        <div class="hero-copy">
          <div class="role-chip"><i :class="roleMeta.icon" />{{ roleMeta.role }}</div>
          <h2>{{ roleMeta.title }}</h2>
          <p>{{ roleMeta.subtitle }}</p>
        </div>
        <div class="hero-actions">
          <el-button v-if="toolbarVisible('publish')" type="primary" icon="el-icon-plus" size="small"
            v-hasPermi="['tutoring:request:add']" @click="requestDialog = true">发布需求</el-button>
          <el-button v-if="toolbarVisible('profile')" type="success" icon="el-icon-user" size="small"
            v-hasPermi="['tutoring:profile:edit']" @click="openProfile">维护资料</el-button>
          <el-button v-if="toolbarVisible('refresh')" icon="el-icon-refresh" size="small" @click="loadAll">刷新数据</el-button>
          <el-dropdown class="workbench-account" trigger="click">
            <button type="button" class="account-button">
              <img :src="userAvatar" alt="头像" />
              <span>{{ userNickName }}</span>
              <i class="el-icon-arrow-down" />
            </button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="goProfile">个人中心</el-dropdown-item>
              <el-dropdown-item @click.native="lockScreen">锁定屏幕</el-dropdown-item>
              <el-dropdown-item divided @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>

      <el-row :gutter="12" class="metric-row">
        <el-col v-for="item in roleMetricCards" :key="item.label" :xs="12" :sm="8" :lg="4">
          <div class="metric-card" :class="{ 'is-warning': item.warning }">
            <i :class="item.icon" />
            <div>
              <div class="metric-value">{{ item.value }}</div>
              <div class="metric-label">{{ item.label }}</div>
              <div class="metric-note">{{ item.note }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="module-switcher">
        <button v-for="item in workbenchNavItems" :key="item.name" type="button" class="module-tab"
          :class="{ active: activeTab === item.name }" @click="switchTab(item.name)">
          <i :class="item.icon" />
          <span>{{ item.label }}</span>
          <em v-if="item.badge">{{ item.badge }}</em>
        </button>
      </div>

      <section class="workbench-content">
        <div class="content-header">
          <div>
            <h3>{{ activeNav.label }}</h3>
            <p>{{ activeNav.desc }}</p>
          </div>
          <el-button icon="el-icon-refresh" size="mini" @click="loadCurrentTab">刷新当前</el-button>
        </div>

        <el-tabs v-model="activeTab" class="workbench-tabs" @tab-click="loadCurrentTab">
      <el-tab-pane v-if="tabVisible('profile')" label="教员资料" name="profile">
        <el-alert :closable="false" :title="profileVerifyText" :type="profileForm.verifyStatus === '1' ? 'success' : 'warning'" />
        <el-alert class="mt12" :closable="false" :title="'资料完整度 ' + profileCompleteness + '%'" type="info" />
        <el-descriptions :column="2" border class="mt12">
          <el-descriptions-item label="学校">{{ profileForm.university || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ profileForm.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ profileForm.collegeYear || '-' }}</el-descriptions-item>
          <el-descriptions-item label="擅长科目">{{ profileForm.subjects || '-' }}</el-descriptions-item>
          <el-descriptions-item label="可授课时间">{{ profileForm.availabilityText || '-' }}</el-descriptions-item>
          <el-descriptions-item label="期望课时费">{{ profileForm.hourlyRate ? '￥' + profileForm.hourlyRate + '/小时' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ profileForm.verifyRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-button type="primary" size="mini" class="mt12" @click="openProfile">维护资料并提交审核</el-button>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('availability')" label="授课日历" name="availability">
        <el-form :inline="true" :model="availabilityForm" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="星期">
            <el-select v-model="availabilityForm.weekDay" class="status-select">
              <el-option v-for="day in ['1','2','3','4','5','6','7']" :key="day" :label="weekDayText(day)" :value="day" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始"><el-time-picker v-model="availabilityForm.startTime" value-format="HH:mm" format="HH:mm" placeholder="开始时间" /></el-form-item>
          <el-form-item label="结束"><el-time-picker v-model="availabilityForm.endTime" value-format="HH:mm" format="HH:mm" placeholder="结束时间" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="availabilityForm.remark" clearable placeholder="如：仅线上" /></el-form-item>
          <el-form-item><el-button type="primary" icon="el-icon-plus" @click="addAvailabilityRow">添加时段</el-button></el-form-item>
        </el-form>
        <el-table :data="availabilityRows">
          <el-table-column label="星期" width="100"><template slot-scope="scope">{{ weekDayText(scope.row.weekDay) }}</template></el-table-column>
          <el-table-column label="开始" prop="startTime" width="100" />
          <el-table-column label="结束" prop="endTime" width="100" />
          <el-table-column label="备注" prop="remark" />
          <el-table-column label="操作" width="90">
            <template slot-scope="scope"><el-button type="text" class="text-danger" @click="removeAvailabilityRow(scope.row)">删除</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('learners')" label="学员档案" name="learners">
        <div class="table-toolbar"><el-button type="primary" size="small" icon="el-icon-plus" @click="openLearner()">新增学员</el-button></div>
        <el-table :data="learnerRows">
          <el-table-column label="姓名" prop="learnerName" width="100" />
          <el-table-column label="年级" prop="grade" width="100" />
          <el-table-column label="学校" prop="school" min-width="120" />
          <el-table-column label="薄弱科目" prop="weakSubjects" min-width="140" />
          <el-table-column label="目标" prop="targetScore" min-width="120" />
          <el-table-column label="可上课时间" prop="availableTime" min-width="160" show-overflow-tooltip />
          <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="130">
            <template slot-scope="scope">
              <el-button type="text" @click="openLearner(scope.row)">编辑</el-button>
              <el-button type="text" class="text-danger" @click="removeLearner(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('tutors')" label="找教员" name="tutors">
        <el-form :inline="true" :model="tutorQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="科目"><el-input v-model="tutorQuery.subjects" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item label="学校"><el-input v-model="tutorQuery.university" clearable placeholder="学校名称" /></el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchTutors">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetTutorSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="tutors">
          <el-table-column label="姓名" prop="userName" width="100" />
          <el-table-column label="学校" prop="university" />
          <el-table-column label="专业" prop="major" />
          <el-table-column label="科目" prop="subjects" />
          <el-table-column label="课时费" width="100"><template slot-scope="scope">￥{{ scope.row.hourlyRate }}</template></el-table-column>
          <el-table-column label="评分" width="90"><template slot-scope="scope">{{ scope.row.averageRating || '暂无' }}</template></el-table-column>
          <el-table-column label="操作" width="160">
            <template slot-scope="scope">
              <el-button v-if="actionVisible('tutors', 'view')" type="text" @click="viewTutor(scope.row.userId)">查看</el-button>
              <el-button v-if="actionVisible('tutors', 'favorite')" type="text" @click="addFavorite(scope.row)">收藏</el-button>
              <el-button v-if="actionVisible('tutors', 'invite')" type="text" @click="openInvite(scope.row)">预约</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('open')" label="开放需求" name="open">
        <el-form :inline="true" :model="queryForm" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="科目"><el-input v-model="queryForm.subject" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item label="年级"><el-input v-model="queryForm.learnerGrade" clearable placeholder="如：初二" /></el-form-item>
          <el-form-item label="区域"><el-input v-model="queryForm.area" clearable placeholder="授课区域" /></el-form-item>
          <el-form-item label="预算">
            <el-input-number v-model="queryForm.minBudget" :min="0" :controls="false" placeholder="最低" class="budget-input" />
            <span class="budget-separator">—</span>
            <el-input-number v-model="queryForm.maxBudget" :min="0" :controls="false" placeholder="最高" class="budget-input" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchRequests">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="openRequests">
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="年级" prop="learnerGrade" width="90" />
          <el-table-column label="区域" prop="area" min-width="120" />
          <el-table-column label="期望时间" prop="scheduleText" min-width="140" />
          <el-table-column label="预算/小时" width="110">
            <template slot-scope="scope">￥{{ scope.row.hourlyBudget }}</template>
          </el-table-column>
          <el-table-column label="发布人" prop="publisherName" width="100" />
          <el-table-column label="要求" prop="requirementText" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="90" align="center">
            <template slot-scope="scope">
              <el-button v-if="actionVisible('open', 'apply')" size="mini" type="text" v-hasPermi="['tutoring:match:apply']"
                @click="openApply(scope.row)">申请</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('recommended')" label="智能推荐" name="recommended">
        <el-table :data="recommendedRequests">
          <el-table-column label="匹配度" width="110">
            <template slot-scope="scope"><el-progress :percentage="scope.row.matchScore" :stroke-width="8" /></template>
          </el-table-column>
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="年级" prop="learnerGrade" width="90" />
          <el-table-column label="区域" prop="area" />
          <el-table-column label="预算/小时" width="110">
            <template slot-scope="scope">￥{{ scope.row.hourlyBudget }}</template>
          </el-table-column>
          <el-table-column label="期望时间" prop="scheduleText" />
          <el-table-column label="操作" width="80">
            <template slot-scope="scope"><el-button v-if="actionVisible('recommended', 'apply')" type="text" @click="openApply(scope.row)">申请</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('requests')" label="我的需求" name="requests">
        <el-table :data="myRequests">
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="年级" prop="learnerGrade" width="90" />
          <el-table-column label="区域" prop="area" />
          <el-table-column label="期望时间" prop="scheduleText" />
          <el-table-column label="预算/小时" width="110">
            <template slot-scope="scope">￥{{ scope.row.hourlyBudget }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template slot-scope="scope">{{ requestStatus(scope.row.status) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0' && actionVisible('requests', 'cancel')" size="mini" type="text" class="text-danger"
                @click="cancelOwnRequest(scope.row)">取消</el-button>
              <el-button v-if="actionVisible('requests', 'copy')" size="mini" type="text"
                @click="copyOwnRequest(scope.row)">复制</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('matches')" label="我的订单" name="matches">
        <el-table :data="matches">
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="教员" width="110">
            <template slot-scope="scope">
              <el-button type="text" @click="viewTutor(scope.row.tutorId)">{{ scope.row.tutorName }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="报价/小时" width="110">
            <template slot-scope="scope">￥{{ scope.row.quotedRate }}</template>
          </el-table-column>
          <el-table-column label="申请说明" prop="applicationText" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template slot-scope="scope">{{ matchStatus(scope.row.status) }}</template>
          </el-table-column>
          <el-table-column label="试听" width="150">
            <template slot-scope="scope">
              <span>{{ trialStatus(scope.row.trialStatus) }}</span>
              <span v-if="scope.row.trialTime"> {{ scope.row.trialTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="评价" min-width="140">
            <template slot-scope="scope">
              <span v-if="scope.row.rating">{{ scope.row.rating }}星 {{ scope.row.reviewText }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="540" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0' && actionVisible('matches', 'accept')" size="mini" type="text"
                v-hasPermi="['tutoring:match:accept']" @click="accept(scope.row)">接受</el-button>
              <el-button v-if="scope.row.status === '0' && actionVisible('matches', 'withdraw')" size="mini" type="text" class="text-danger"
                v-hasPermi="['tutoring:match:apply']" @click="withdraw(scope.row)">撤回</el-button>
              <el-button v-if="scope.row.status === '1' && actionVisible('matches', 'complete')" size="mini" type="text"
                v-hasPermi="['tutoring:match:complete']" @click="complete(scope.row)">完成授课</el-button>
              <el-button v-if="scope.row.status === '2' && !scope.row.rating && actionVisible('matches', 'review')" size="mini" type="text"
                v-hasPermi="['tutoring:match:review']" @click="openReview(scope.row)">评价</el-button>
              <el-button v-if="(scope.row.status === '1' || scope.row.status === '2') && actionVisible('matches', 'lessons')" size="mini" type="text"
                @click="openLessons(scope.row)">课程记录</el-button>
              <el-button v-if="(scope.row.status === '1' || scope.row.status === '2') && actionVisible('matches', 'materials')" size="mini" type="text"
                @click="openMaterials(scope.row)">资料</el-button>
              <el-button v-if="(scope.row.status === '1' || scope.row.status === '2') && actionVisible('matches', 'messages')" size="mini" type="text"
                @click="openMessages(scope.row)">沟通</el-button>
              <el-button v-if="(scope.row.status === '1' || scope.row.status === '2') && actionVisible('matches', 'homeworks')" size="mini" type="text"
                @click="openHomeworks(scope.row)">作业</el-button>
              <el-button v-if="(scope.row.status === '1' || scope.row.status === '2') && actionVisible('matches', 'payments')" size="mini" type="text"
                @click="openPayments(scope.row)">付款</el-button>
              <el-button v-if="scope.row.status === '1' && actionVisible('matches', 'trial')" size="mini" type="text"
                @click="openTrial(scope.row)">安排试听</el-button>
              <el-button v-if="scope.row.status === '1' && scope.row.trialStatus === '1' && actionVisible('matches', 'trial')" size="mini" type="text"
                @click="completeTrialRow(scope.row)">完成试听</el-button>
              <el-button v-if="scope.row.status === '1' && actionVisible('matches', 'reschedule')" size="mini" type="text"
                @click="rescheduleAcceptedMatch(scope.row)">改期</el-button>
              <el-button v-if="scope.row.status === '1' && actionVisible('matches', 'cancel')" size="mini" type="text" class="text-danger"
                @click="cancelAcceptedMatch(scope.row)">取消订单</el-button>
              <el-button v-if="(scope.row.status === '1' || scope.row.status === '2') && actionVisible('matches', 'complaint')" size="mini" type="text" class="text-danger"
                @click="openComplaint(scope.row)">投诉</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('calendar')" label="排课日历" name="calendar">
        <el-table :data="calendarRows">
          <el-table-column label="日期" prop="lessonDate" width="110" />
          <el-table-column label="开始" prop="startTime" width="90" />
          <el-table-column label="结束" prop="endTime" width="90" />
          <el-table-column label="科目" prop="subject" width="100" />
          <el-table-column label="家长" prop="publisherName" width="110" />
          <el-table-column label="教员" prop="tutorName" width="110" />
          <el-table-column label="课时" prop="hours" width="80" />
          <el-table-column label="确认" width="90">
            <template slot-scope="scope">{{ scope.row.confirmStatus === '1' ? '已确认' : '待确认' }}</template>
          </el-table-column>
          <el-table-column label="内容" prop="content" min-width="180" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('learning')" label="学习档案" name="learning">
        <el-table :data="learningRows">
          <el-table-column label="日期" prop="lessonDate" width="110" />
          <el-table-column label="科目" prop="subject" width="100" />
          <el-table-column label="家长" prop="publisherName" width="110" />
          <el-table-column label="教员" prop="tutorName" width="110" />
          <el-table-column label="出勤" width="90">
            <template slot-scope="scope">{{ attendanceStatus(scope.row.attendanceStatus) }}</template>
          </el-table-column>
          <el-table-column label="课后报告" prop="studentPerformance" min-width="180" show-overflow-tooltip />
          <el-table-column label="作业" prop="homework" min-width="160" show-overflow-tooltip />
          <el-table-column label="阶段反馈" prop="phaseFeedback" min-width="180" show-overflow-tooltip />
          <el-table-column label="下节计划" prop="nextPlan" min-width="160" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('announcements')" label="平台公告" name="announcements">
        <el-table :data="announcementRows">
          <el-table-column label="标题" prop="title" width="180" />
          <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
          <el-table-column label="发布时间" prop="publishTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('notifications')" label="消息通知" name="notifications">
        <el-form :inline="true" :model="notificationQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="状态">
            <el-select v-model="notificationQuery.readStatus" clearable placeholder="全部" class="status-select">
              <el-option label="未读" value="0" />
              <el-option label="已读" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型"><el-input v-model="notificationQuery.title" clearable placeholder="标题关键词" /></el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="loadNotifications">查询</el-button>
            <el-button icon="el-icon-check" @click="markAllRead">全部已读 {{ unreadNotifications }}</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="notifications">
          <el-table-column label="状态" width="80">
            <template slot-scope="scope"><el-tag :type="scope.row.readStatus === '1' ? 'info' : 'danger'" size="mini">{{ scope.row.readStatus === '1' ? '已读' : '未读' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="标题" prop="title" width="180" />
          <el-table-column label="渠道" prop="channel" width="90" />
          <el-table-column label="模板" prop="templateCode" width="130" />
          <el-table-column label="发送" width="90">
            <template slot-scope="scope">{{ scope.row.sendStatus === '1' ? '已发送' : '待发送' }}</template>
          </el-table-column>
          <el-table-column label="内容" prop="content" />
          <el-table-column label="时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="80">
            <template slot-scope="scope"><el-button v-if="scope.row.readStatus === '0'" type="text" @click="markRead(scope.row)">标为已读</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('settlements')" label="收入结算" name="settlements">
        <el-table :data="settlementRows">
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="100" />
          <el-table-column label="课时" prop="lessonId" width="90" />
          <el-table-column label="毛额" width="90"><template slot-scope="scope">￥{{ scope.row.amount }}</template></el-table-column>
          <el-table-column label="抽成" width="90"><template slot-scope="scope">￥{{ scope.row.platformFee }}</template></el-table-column>
          <el-table-column label="净额" width="90"><template slot-scope="scope">￥{{ scope.row.netAmount || scope.row.amount }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ settlementStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="结算人" prop="settleBy" width="100" />
          <el-table-column label="结算时间" prop="settleTime" width="170" />
          <el-table-column label="生成时间" prop="createTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('tickets')" label="客服工单" name="tickets">
        <div class="table-toolbar"><el-button type="primary" size="small" icon="el-icon-plus" @click="openTicket">提交工单</el-button></div>
        <el-table :data="ticketRows">
          <el-table-column label="标题" prop="title" width="180" />
          <el-table-column label="问题描述" prop="content" min-width="220" show-overflow-tooltip />
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ ticketStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="处理意见" prop="handleRemark" min-width="180" show-overflow-tooltip />
          <el-table-column label="处理人" prop="handleBy" width="100" />
          <el-table-column label="提交时间" prop="createTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('complaints')" :label="complaintsLabel" name="complaints">
        <el-table :data="complaints">
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="100" />
          <el-table-column v-if="workbenchRole === 'admin'" label="投诉人" prop="complainantName" width="100" />
          <el-table-column label="投诉原因" prop="reason" />
          <el-table-column label="处理状态" width="90">
            <template slot-scope="scope">{{ complaintStatus(scope.row.status) }}</template>
          </el-table-column>
          <el-table-column label="处理意见" prop="handleRemark" />
          <el-table-column label="处理记录" prop="handleTimeline" min-width="220" show-overflow-tooltip />
          <el-table-column v-if="actionVisible('complaints', 'handle')" label="操作" width="130">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" type="text" @click="handleComplaintRow(scope.row, '1')">解决</el-button>
              <el-button v-if="scope.row.status === '0'" type="text" class="text-danger" @click="handleComplaintRow(scope.row, '2')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('favorites')" label="收藏教员" name="favorites">
        <el-table :data="favoriteTutors">
          <el-table-column label="姓名" prop="userName" width="100" />
          <el-table-column label="学校" prop="university" />
          <el-table-column label="专业" prop="major" />
          <el-table-column label="科目" prop="subjects" />
          <el-table-column label="评分" width="90">
            <template slot-scope="scope">{{ scope.row.averageRating || '暂无' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template slot-scope="scope">
              <el-button v-if="actionVisible('favorites', 'view')" type="text" @click="viewTutor(scope.row.userId)">查看</el-button>
              <el-button v-if="actionVisible('favorites', 'invite')" type="text" @click="openInvite(scope.row)">预约</el-button>
              <el-button v-if="actionVisible('favorites', 'unfavorite')" type="text" class="text-danger" @click="removeFavorite(scope.row)">取消收藏</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('invitations')" label="预约邀请" name="invitations">
        <el-table :data="invitations">
          <el-table-column label="家长" prop="publisherName" width="100" />
          <el-table-column label="教员" prop="tutorName" width="100" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="年级" prop="learnerGrade" width="90" />
          <el-table-column label="区域" prop="area" />
          <el-table-column label="期望时间" prop="scheduleText" />
          <el-table-column label="课时费" width="90"><template slot-scope="scope">￥{{ scope.row.offeredRate }}</template></el-table-column>
          <el-table-column label="状态" width="80"><template slot-scope="scope">{{ invitationStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0' && actionVisible('invitations', 'respond')" v-hasPermi="['tutoring:match:apply']" type="text" @click="respondInvite(scope.row, true)">接受</el-button>
              <el-button v-if="scope.row.status === '0' && actionVisible('invitations', 'respond')" v-hasPermi="['tutoring:match:apply']" type="text" class="text-danger" @click="respondInvite(scope.row, false)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessCrm')" label="招生 CRM" name="businessCrm">
        <el-row :gutter="16">
          <el-col v-for="item in crmCards" :key="item.label" :xs="12" :sm="8" :lg="4">
            <div class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
        <el-table :data="crmDashboard.sourceStats || []" size="small">
          <el-table-column label="来源" prop="sourceChannel" />
          <el-table-column label="线索数" prop="leadCount" width="120" />
          <el-table-column label="成单数" prop="convertedCount" width="120" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessRisk')" label="风控质检" name="businessRisk">
        <el-table :data="riskAlerts">
          <el-table-column label="级别" width="90">
            <template slot-scope="scope"><el-tag :type="riskSeverity(scope.row.severity)" size="mini">{{ scope.row.severity }}</el-tag></template>
          </el-table-column>
          <el-table-column label="类型" prop="title" width="120" />
          <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
          <el-table-column label="时间" prop="createTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessReports')" label="运营报表" name="businessReports">
        <el-row :gutter="16">
          <el-col v-for="item in reportCards" :key="item.label" :xs="12" :sm="8" :lg="4">
            <div class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
        <el-table :data="operationsReport.subjectHeat || []" size="small">
          <el-table-column label="科目" prop="subject" />
          <el-table-column label="需求数" prop="requestCount" width="110" />
          <el-table-column label="订单数" prop="matchCount" width="110" />
          <el-table-column label="GMV" width="120"><template slot-scope="scope">¥{{ scope.row.gmv || 0 }}</template></el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessReminders')" label="提醒中心" name="businessReminders">
        <div class="table-toolbar">
          <el-button type="primary" icon="el-icon-bell" @click="generateReminderRows">生成提醒</el-button>
        </div>
        <el-row :gutter="16">
          <el-col v-for="item in reminderCards" :key="item.label" :xs="12" :sm="8">
            <div class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessBlacklists')" label="黑名单" name="businessBlacklists">
        <el-form :inline="true" :model="blacklistQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="用户ID"><el-input-number v-model="blacklistQuery.userId" :min="1" :controls="false" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="blacklistQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="生效" value="1" />
              <el-option label="停用" value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="loadCurrentTab">查询</el-button>
            <el-button icon="el-icon-plus" @click="submitBlacklist">加入黑名单</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminBlacklists">
          <el-table-column label="用户ID" prop="userId" width="90" />
          <el-table-column label="昵称" prop="userName" width="120" />
          <el-table-column label="原因" prop="reason" min-width="240" show-overflow-tooltip />
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ scope.row.status === '1' ? '生效' : '停用' }}</template></el-table-column>
          <el-table-column label="创建人" prop="createBy" width="100" />
          <el-table-column label="时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="90">
            <template slot-scope="scope"><el-button v-if="scope.row.status === '1'" type="text" @click="disableBlacklistRow(scope.row)">停用</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessLedgers')" label="财务流水" name="businessLedgers">
        <el-form :inline="true" :model="ledgerQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="业务"><el-input v-model="ledgerQuery.bizType" clearable placeholder="payment/refund/settlement" /></el-form-item>
          <el-form-item label="订单"><el-input-number v-model="ledgerQuery.matchId" :min="1" :controls="false" /></el-form-item>
          <el-form-item label="状态"><el-input v-model="ledgerQuery.status" clearable placeholder="confirmed/settled" /></el-form-item>
          <el-form-item><el-button type="primary" icon="el-icon-search" @click="loadCurrentTab">查询</el-button></el-form-item>
        </el-form>
        <el-table :data="financeLedgers">
          <el-table-column label="流水" prop="ledgerId" width="80" />
          <el-table-column label="业务" prop="bizType" width="100" />
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="用户" prop="userName" width="110" />
          <el-table-column label="金额" width="100"><template slot-scope="scope">￥{{ scope.row.amount || 0 }}</template></el-table-column>
          <el-table-column label="方向" prop="direction" width="80" />
          <el-table-column label="状态" prop="status" width="110" />
          <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
          <el-table-column label="时间" prop="createTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessClients')" label="家长管理" name="businessClients">
        <el-form :inline="true" :model="adminClientQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="账号/昵称"><el-input v-model="adminClientQuery.userName" clearable placeholder="账号或昵称" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="adminClientQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminClients">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminClients">重置</el-button>
            <el-button icon="el-icon-download" @click="handleAdminExport('clients', adminClientQuery)">导出</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminClients">
          <el-table-column label="账号" prop="userName" width="120" />
          <el-table-column label="昵称" prop="nickName" width="120" />
          <el-table-column label="手机" prop="phonenumber" width="130" />
          <el-table-column label="邮箱" prop="email" min-width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="80"><template slot-scope="scope">{{ userStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="业务统计" prop="remark" min-width="180" />
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="90">
            <template slot-scope="scope">
              <el-button type="text" @click="changeBusinessUserStatus(scope.row.userId, scope.row.status)">
                {{ scope.row.status === '0' ? '停用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessTutors')" label="教员管理" name="businessTutors">
        <el-form :inline="true" :model="adminTutorQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="姓名"><el-input v-model="adminTutorQuery.userName" clearable placeholder="账号或昵称" /></el-form-item>
          <el-form-item label="学校"><el-input v-model="adminTutorQuery.university" clearable placeholder="学校名称" /></el-form-item>
          <el-form-item label="科目"><el-input v-model="adminTutorQuery.subjects" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item label="审核">
            <el-select v-model="adminTutorQuery.verifyStatus" clearable placeholder="全部" class="status-select">
              <el-option label="待审核" value="0" />
              <el-option label="已通过" value="1" />
              <el-option label="已驳回" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminTutors">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminTutors">重置</el-button>
            <el-button icon="el-icon-download" @click="handleAdminExport('tutors', adminTutorQuery)">导出</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminTutors">
          <el-table-column label="账号" prop="loginName" width="120" />
          <el-table-column label="昵称" prop="userName" width="100" />
          <el-table-column label="学校" prop="university" min-width="120" />
          <el-table-column label="专业" prop="major" min-width="120" />
          <el-table-column label="科目" prop="subjects" min-width="120" />
          <el-table-column label="课时费" width="90"><template slot-scope="scope">{{ scope.row.hourlyRate ? '￥' + scope.row.hourlyRate : '-' }}</template></el-table-column>
          <el-table-column label="评分" width="80"><template slot-scope="scope">{{ scope.row.averageRating || '暂无' }}</template></el-table-column>
          <el-table-column label="完成单" prop="completedOrders" width="80" />
          <el-table-column label="审核" width="90"><template slot-scope="scope">{{ verifyStatus(scope.row.verifyStatus) }}</template></el-table-column>
          <el-table-column label="账号状态" width="90"><template slot-scope="scope">{{ userStatus(scope.row.userStatus) }}</template></el-table-column>
          <el-table-column label="操作" width="90">
            <template slot-scope="scope">
              <el-button type="text" @click="changeBusinessUserStatus(scope.row.userId, scope.row.userStatus)">
                {{ scope.row.userStatus === '0' ? '停用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessRequests')" label="业务监管-需求" name="businessRequests">
        <el-form :inline="true" :model="adminRequestQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="科目"><el-input v-model="adminRequestQuery.subject" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item label="来源"><el-input v-model="adminRequestQuery.sourceChannel" clearable placeholder="如：社群" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="adminRequestQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="招募中" value="0" />
              <el-option label="已匹配" value="1" />
              <el-option label="已完成" value="2" />
              <el-option label="已取消" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminRequests">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminRequests">重置</el-button>
            <el-button icon="el-icon-download" @click="handleAdminExport('requests', adminRequestQuery)">导出</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminRequests">
          <el-table-column label="发布人" prop="publisherName" width="100" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="年级" prop="learnerGrade" width="90" />
          <el-table-column label="区域" prop="area" />
          <el-table-column label="来源" prop="sourceChannel" width="100" />
          <el-table-column label="预算/小时" width="110"><template slot-scope="scope">￥{{ scope.row.hourlyBudget }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ requestStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="发布时间" prop="createTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessMatches')" label="业务监管-订单" name="businessMatches">
        <el-form :inline="true" :model="adminMatchQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="科目"><el-input v-model="adminMatchQuery.subject" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="adminMatchQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="申请中" value="0" />
              <el-option label="已接单" value="1" />
              <el-option label="已完成" value="2" />
              <el-option label="未选中" value="3" />
              <el-option label="已取消" value="4" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminMatches">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminMatches">重置</el-button>
            <el-button icon="el-icon-download" @click="handleAdminExport('matches', adminMatchQuery)">导出</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminMatches">
          <el-table-column label="家长" prop="publisherName" width="100" />
          <el-table-column label="教员" prop="tutorName" width="100" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="报价/小时" width="110"><template slot-scope="scope">￥{{ scope.row.quotedRate }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ matchStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="评价" width="90"><template slot-scope="scope">{{ scope.row.rating || '-' }}</template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="90">
            <template slot-scope="scope">
              <el-button v-if="['0', '1'].includes(scope.row.status)" type="text" class="text-danger"
                @click="closeBusinessMatch(scope.row)">关闭</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessInvitations')" label="业务监管-预约" name="businessInvitations">
        <el-form :inline="true" :model="adminInvitationQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="科目"><el-input v-model="adminInvitationQuery.subject" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="adminInvitationQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="待处理" value="0" />
              <el-option label="已接受" value="1" />
              <el-option label="已拒绝" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminInvitations">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminInvitations">重置</el-button>
            <el-button icon="el-icon-download" @click="handleAdminExport('invitations', adminInvitationQuery)">导出</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminInvitations">
          <el-table-column label="家长" prop="publisherName" width="100" />
          <el-table-column label="教员" prop="tutorName" width="100" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="年级" prop="learnerGrade" width="90" />
          <el-table-column label="区域" prop="area" min-width="120" />
          <el-table-column label="课时费" width="90"><template slot-scope="scope">￥{{ scope.row.offeredRate }}</template></el-table-column>
          <el-table-column label="状态" width="80"><template slot-scope="scope">{{ invitationStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessLessons')" label="业务监管-课时" name="businessLessons">
        <el-form :inline="true" :model="adminLessonQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="科目"><el-input v-model="adminLessonQuery.subject" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminLessons">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminLessons">重置</el-button>
            <el-button icon="el-icon-download" @click="handleAdminExport('lessons', adminLessonQuery)">导出</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminLessons">
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="家长" prop="publisherName" width="100" />
          <el-table-column label="教员" prop="tutorName" width="100" />
          <el-table-column label="日期" prop="lessonDate" width="110" />
          <el-table-column label="开始" prop="startTime" width="80" />
          <el-table-column label="结束" prop="endTime" width="80" />
          <el-table-column label="课时" prop="hours" width="80" />
          <el-table-column label="课时费" width="100"><template slot-scope="scope">￥{{ scope.row.amount }}</template></el-table-column>
          <el-table-column label="出勤" width="80"><template slot-scope="scope">{{ attendanceStatus(scope.row.attendanceStatus) }}</template></el-table-column>
          <el-table-column label="确认" width="90"><template slot-scope="scope">{{ scope.row.confirmStatus === '1' ? '已确认' : '待确认' }}</template></el-table-column>
          <el-table-column label="内容" prop="content" min-width="180" show-overflow-tooltip />
          <el-table-column label="阶段反馈" prop="phaseFeedback" min-width="160" show-overflow-tooltip />
          <el-table-column label="记录时间" prop="createTime" width="170" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessSettlements')" label="结算管理" name="businessSettlements">
        <el-form :inline="true" :model="adminSettlementQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="科目"><el-input v-model="adminSettlementQuery.subject" clearable placeholder="如：数学" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="adminSettlementQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="待结算" value="0" />
              <el-option label="已结算" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminSettlements">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminSettlements">重置</el-button>
            <el-button icon="el-icon-check" @click="batchSettleSelected">批量结算</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminSettlements" @selection-change="selectedSettlements = $event">
          <el-table-column type="selection" width="45" />
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="家长" prop="publisherName" width="100" />
          <el-table-column label="教员" prop="tutorName" width="100" />
          <el-table-column label="课时" prop="lessonId" width="90" />
          <el-table-column label="毛额" width="90"><template slot-scope="scope">￥{{ scope.row.amount }}</template></el-table-column>
          <el-table-column label="抽成" width="90"><template slot-scope="scope">￥{{ scope.row.platformFee }}</template></el-table-column>
          <el-table-column label="净额" width="90"><template slot-scope="scope">￥{{ scope.row.netAmount || scope.row.amount }}</template></el-table-column>
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ settlementStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="生成时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="90">
            <template slot-scope="scope"><el-button v-if="scope.row.status === '0'" type="text" @click="settleSettlementRow(scope.row)">结算</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessTickets')" label="工单处理" name="businessTickets">
        <el-form :inline="true" :model="adminTicketQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="标题"><el-input v-model="adminTicketQuery.title" clearable placeholder="标题关键字" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="adminTicketQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="待处理" value="0" />
              <el-option label="已解决" value="1" />
              <el-option label="已关闭" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchAdminTickets">查询</el-button>
            <el-button icon="el-icon-refresh" @click="resetAdminTickets">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminTickets">
          <el-table-column label="提交人" prop="userName" width="100" />
          <el-table-column label="标题" prop="title" width="180" />
          <el-table-column label="问题描述" prop="content" min-width="220" show-overflow-tooltip />
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ ticketStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="处理意见" prop="handleRemark" min-width="180" show-overflow-tooltip />
          <el-table-column label="提交时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="130">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" type="text" @click="handleTicketRow(scope.row, '1')">解决</el-button>
              <el-button v-if="scope.row.status === '0'" type="text" class="text-danger" @click="handleTicketRow(scope.row, '2')">关闭</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessAnnouncements')" label="公告管理" name="businessAnnouncements">
        <el-form :inline="true" :model="adminAnnouncementQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="标题"><el-input v-model="adminAnnouncementQuery.title" clearable placeholder="标题关键字" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="adminAnnouncementQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="草稿" value="0" />
              <el-option label="发布" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="loadCurrentTab">查询</el-button>
            <el-button icon="el-icon-plus" @click="openAnnouncement()">新增公告</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminAnnouncements">
          <el-table-column label="标题" prop="title" width="180" />
          <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ announcementStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="发布时间" prop="publishTime" width="170" />
          <el-table-column label="操作" width="130">
            <template slot-scope="scope">
              <el-button type="text" @click="openAnnouncement(scope.row)">编辑</el-button>
              <el-button type="text" class="text-danger" @click="removeAnnouncement(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessFollowups')" label="回访记录" name="businessFollowups">
        <el-form :inline="true" :model="followupForm" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="订单ID"><el-input-number v-model="followupForm.matchId" :min="1" :controls="false" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="followupForm.status" clearable placeholder="全部" class="status-select">
              <el-option label="待跟进" value="0" />
              <el-option label="已完成" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="内容"><el-input v-model="followupForm.content" clearable placeholder="回访内容" /></el-form-item>
          <el-form-item label="后续"><el-input v-model="followupForm.nextAction" clearable placeholder="后续动作" /></el-form-item>
          <el-form-item label="提醒"><el-date-picker v-model="followupForm.nextTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="下次跟进" /></el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-plus" @click="submitFollowup">新增回访</el-button>
            <el-button icon="el-icon-search" @click="loadCurrentTab">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="followupRows">
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="家长" prop="publisherName" width="100" />
          <el-table-column label="教员" prop="tutorName" width="100" />
          <el-table-column label="回访内容" prop="content" min-width="220" show-overflow-tooltip />
          <el-table-column label="后续动作" prop="nextAction" min-width="180" show-overflow-tooltip />
          <el-table-column label="提醒时间" prop="nextTime" width="170" />
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ followupStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="记录人" prop="createBy" width="100" />
          <el-table-column label="时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="90">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" type="text" @click="completeFollowupRow(scope.row)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('businessPayments')" label="支付流水" name="businessPayments">
        <el-form :inline="true" :model="adminPaymentQuery" size="small" class="filter-form" @submit.native.prevent>
          <el-form-item label="状态">
            <el-select v-model="adminPaymentQuery.status" clearable placeholder="全部" class="status-select">
              <el-option label="待确认" value="0" />
              <el-option label="已确认" value="1" />
              <el-option label="已驳回" value="2" />
              <el-option label="已退款" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="loadCurrentTab">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="adminPayments">
          <el-table-column label="流水ID" prop="paymentId" width="90" />
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="家长" prop="payerName" width="100" />
          <el-table-column label="教员" prop="tutorName" width="100" />
          <el-table-column label="金额" width="90"><template slot-scope="scope">￥{{ scope.row.amount }}</template></el-table-column>
          <el-table-column label="抽成" width="90"><template slot-scope="scope">￥{{ scope.row.platformFee || 0 }}</template></el-table-column>
          <el-table-column label="退款" width="90"><template slot-scope="scope">￥{{ scope.row.refundAmount || 0 }}</template></el-table-column>
          <el-table-column label="方式" prop="payMethod" width="90" />
          <el-table-column label="交易号" prop="tradeNo" min-width="140" show-overflow-tooltip />
          <el-table-column label="发票" prop="invoiceNo" min-width="120" show-overflow-tooltip />
          <el-table-column label="收据" prop="receiptNo" min-width="120" show-overflow-tooltip />
          <el-table-column label="凭证" min-width="180" show-overflow-tooltip>
            <template slot-scope="scope"><a v-if="scope.row.proofUrl" :href="scope.row.proofUrl" target="_blank">查看凭证</a></template>
          </el-table-column>
          <el-table-column label="状态" width="90"><template slot-scope="scope">{{ paymentStatus(scope.row.status) }}</template></el-table-column>
          <el-table-column label="对账" width="90"><template slot-scope="scope">{{ scope.row.reconciledStatus === '1' ? '已对账' : '未对账' }}</template></el-table-column>
          <el-table-column label="处理意见" prop="handleRemark" min-width="180" show-overflow-tooltip />
          <el-table-column label="提交时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="190" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" type="text" @click="handlePaymentRow(scope.row, '1')">确认</el-button>
              <el-button v-if="scope.row.status === '0'" type="text" class="text-danger" @click="handlePaymentRow(scope.row, '2')">驳回</el-button>
              <el-button v-if="scope.row.status === '1'" type="text" class="text-danger" @click="refundPaymentRow(scope.row)">退款</el-button>
              <el-button v-if="scope.row.status === '1' || scope.row.status === '3'" type="text" @click="reconcilePaymentRow(scope.row)">对账</el-button>
              <el-button v-if="scope.row.status === '1' || scope.row.status === '3'" type="text" @click="issueInvoiceRow(scope.row)">开票</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('verify')" label="教员审核" name="verify">
        <el-table :data="pendingProfiles">
          <el-table-column label="姓名" prop="userName" width="100" />
          <el-table-column label="学校" prop="university" />
          <el-table-column label="专业" prop="major" />
          <el-table-column label="年级" prop="collegeYear" width="80" />
          <el-table-column label="科目" prop="subjects" />
          <el-table-column label="期望课时费" prop="hourlyRate" width="110" />
          <el-table-column label="审核材料" width="130">
            <template slot-scope="scope">
              <image-preview v-if="scope.row.studentCardUrl" :src="scope.row.studentCardUrl" :width="42" :height="42" />
              <image-preview v-if="scope.row.qualificationUrl" :src="scope.row.qualificationUrl" :width="42" :height="42" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="verify(scope.row, '1')">通过</el-button>
              <el-button size="mini" type="text" class="text-danger" @click="verify(scope.row, '2')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="tabVisible('dashboard')" label="数据看板" name="dashboard">
        <el-row :gutter="16">
          <el-col v-for="item in dashboardCards" :key="item.label" :xs="12" :sm="8" :lg="4">
            <div class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
        <h4>待办中心</h4>
        <el-row :gutter="16">
          <el-col v-for="item in adminTodoCards" :key="item.label" :xs="12" :sm="6">
            <div class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
        <h4>热门科目</h4>
        <el-table :data="dashboard.topSubjects || []" size="small">
          <el-table-column label="排名" type="index" width="80" />
          <el-table-column label="科目" prop="subject" />
          <el-table-column label="需求数" prop="requestCount" width="120" />
        </el-table>
      </el-tab-pane>
        </el-tabs>
      </section>
    </div>

    <el-dialog title="教员资料" :visible.sync="profileDialog" width="620px" append-to-body>
      <el-form ref="profileForm" :model="profileForm" :rules="profileRules" label-width="100px">
        <el-alert class="mb8" :closable="false" :title="'资料完整度 ' + profileCompleteness + '%'" type="info" />
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学校" prop="university"><el-input v-model="profileForm.university" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="专业" prop="major"><el-input v-model="profileForm.major" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年级" prop="collegeYear"><el-input v-model="profileForm.collegeYear" placeholder="例如：大三" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="期望课时费" prop="hourlyRate"><el-input-number v-model="profileForm.hourlyRate" :min="1" :precision="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="擅长科目" prop="subjects"><el-input v-model="profileForm.subjects" placeholder="例如：数学,物理" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="可授课时间"><el-input v-model="profileForm.availabilityText" placeholder="例如：周三晚、周末下午" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="个人简介" prop="introduction"><el-input v-model="profileForm.introduction" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学生证"><image-upload v-model="profileForm.studentCardUrl" :limit="1" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资格证"><image-upload v-model="profileForm.qualificationUrl" :limit="1" /></el-form-item></el-col>
          <el-col v-if="profileForm.verifyStatus" :span="24"><el-alert :closable="false" :title="profileVerifyText" type="info" /></el-col>
        </el-row>
      </el-form>
      <div slot="footer"><el-button @click="profileDialog = false">取消</el-button><el-button type="primary" @click="submitProfile">保存并提交审核</el-button></div>
    </el-dialog>

    <el-dialog title="发布家教需求" :visible.sync="requestDialog" width="620px" append-to-body>
      <el-form ref="requestForm" :model="requestForm" :rules="requestRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学员年级" prop="learnerGrade"><el-input v-model="requestForm.learnerGrade" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="辅导科目" prop="subject"><el-input v-model="requestForm.subject" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="授课区域" prop="area"><el-input v-model="requestForm.area" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="课时预算" prop="hourlyBudget"><el-input-number v-model="requestForm.hourlyBudget" :min="1" :precision="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源"><el-input v-model="requestForm.sourceChannel" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="期望时间" prop="scheduleText"><el-input v-model="requestForm.scheduleText" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="补充要求" prop="requirementText"><el-input v-model="requestForm.requirementText" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <div slot="footer"><el-button @click="requestDialog = false">取消</el-button><el-button type="primary" @click="submitRequest">发布</el-button></div>
    </el-dialog>

    <el-dialog title="申请家教需求" :visible.sync="applyDialog" width="520px" append-to-body>
      <el-form ref="applyForm" :model="applyForm" :rules="applyRules" label-width="90px">
        <el-form-item label="报价/小时" prop="quotedRate"><el-input-number v-model="applyForm.quotedRate" :min="1" :precision="2" /></el-form-item>
        <el-form-item label="申请说明" prop="applicationText"><el-input v-model="applyForm.applicationText" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="applyDialog = false">取消</el-button><el-button type="primary" @click="submitApply">提交申请</el-button></div>
    </el-dialog>

    <el-dialog title="评价家教订单" :visible.sync="reviewDialog" width="520px" append-to-body>
      <el-form ref="reviewForm" :model="reviewForm" label-width="80px">
        <el-form-item label="评分"><el-rate v-model="reviewForm.rating" /></el-form-item>
        <el-form-item label="评价"><el-input v-model="reviewForm.reviewText" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="reviewDialog = false">取消</el-button><el-button type="primary" @click="submitReview">提交评价</el-button></div>
    </el-dialog>

    <el-dialog title="课程记录" :visible.sync="lessonDialog" width="720px" append-to-body>
      <el-table :data="lessons" size="small">
        <el-table-column label="上课日期" prop="lessonDate" width="110" />
        <el-table-column label="开始" prop="startTime" width="80" />
        <el-table-column label="结束" prop="endTime" width="80" />
        <el-table-column label="课时" prop="hours" width="80" />
        <el-table-column label="授课内容" prop="content" />
        <el-table-column label="出勤" width="80"><template slot-scope="scope">{{ attendanceStatus(scope.row.attendanceStatus) }}</template></el-table-column>
        <el-table-column label="课堂表现" prop="studentPerformance" min-width="120" show-overflow-tooltip />
        <el-table-column label="课后作业" prop="homework" min-width="120" show-overflow-tooltip />
        <el-table-column label="阶段反馈" prop="phaseFeedback" min-width="120" show-overflow-tooltip />
        <el-table-column label="下节计划" prop="nextPlan" min-width="120" show-overflow-tooltip />
        <el-table-column label="课时费" width="100"><template slot-scope="scope">￥{{ scope.row.amount }}</template></el-table-column>
        <el-table-column label="确认" width="90">
          <template slot-scope="scope">{{ scope.row.confirmStatus === '1' ? '已确认' : '待确认' }}</template>
        </el-table-column>
        <el-table-column v-if="actionVisible('matches', 'confirmLesson')" label="操作" width="90">
          <template slot-scope="scope">
            <el-button v-if="scope.row.confirmStatus !== '1'" type="text" @click="confirmLessonRow(scope.row)">确认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider v-if="currentMatch.status === '1' && actionVisible('matches', 'complete')" />
      <el-form v-if="currentMatch.status === '1' && actionVisible('matches', 'complete')" :model="lessonForm" label-width="90px">
        <el-form-item label="上课日期"><el-date-picker v-model="lessonForm.lessonDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" /></el-form-item>
        <el-form-item label="开始时间"><el-time-picker v-model="lessonForm.startTime" value-format="HH:mm" format="HH:mm" placeholder="开始时间" /></el-form-item>
        <el-form-item label="结束时间"><el-time-picker v-model="lessonForm.endTime" value-format="HH:mm" format="HH:mm" placeholder="结束时间" /></el-form-item>
        <el-form-item label="课时数"><el-input-number v-model="lessonForm.hours" :min="0.5" :step="0.5" :precision="1" /></el-form-item>
        <el-form-item label="授课内容"><el-input v-model="lessonForm.content" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="出勤">
          <el-select v-model="lessonForm.attendanceStatus" class="status-select">
            <el-option label="到课" value="1" />
            <el-option label="请假" value="2" />
            <el-option label="缺勤" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="课堂表现"><el-input v-model="lessonForm.studentPerformance" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="课后作业"><el-input v-model="lessonForm.homework" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="阶段反馈"><el-input v-model="lessonForm.phaseFeedback" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="下节计划"><el-input v-model="lessonForm.nextPlan" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="lessonDialog = false">关闭</el-button>
        <el-button v-if="currentMatch.status === '1' && actionVisible('matches', 'complete')" type="primary" @click="submitLesson">添加记录</el-button>
      </div>
    </el-dialog>

    <el-dialog title="提交订单投诉" :visible.sync="complaintDialog" width="520px" append-to-body>
      <el-input v-model="complaintForm.reason" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="请说明投诉原因" />
      <div slot="footer"><el-button @click="complaintDialog = false">取消</el-button><el-button type="primary" @click="submitComplaintForm">提交</el-button></div>
    </el-dialog>

    <el-dialog title="预约教员" :visible.sync="inviteDialog" width="620px" append-to-body>
      <el-form ref="inviteForm" :model="inviteForm" :rules="requestRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学员年级" prop="learnerGrade"><el-input v-model="inviteForm.learnerGrade" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="辅导科目" prop="subject"><el-input v-model="inviteForm.subject" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="授课区域" prop="area"><el-input v-model="inviteForm.area" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预约课时费" prop="offeredRate"><el-input-number v-model="inviteForm.offeredRate" :min="1" :precision="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="期望时间" prop="scheduleText"><el-input v-model="inviteForm.scheduleText" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="预约说明"><el-input v-model="inviteForm.message" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <div slot="footer"><el-button @click="inviteDialog = false">取消</el-button><el-button type="primary" @click="submitInvite">发送预约</el-button></div>
    </el-dialog>

    <el-dialog title="学员档案" :visible.sync="learnerDialog" width="620px" append-to-body>
      <el-form :model="learnerForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学员姓名"><el-input v-model="learnerForm.learnerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年级"><el-input v-model="learnerForm.grade" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学校"><el-input v-model="learnerForm.school" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="目标"><el-input v-model="learnerForm.targetScore" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="薄弱科目"><el-input v-model="learnerForm.weakSubjects" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="可上课时间"><el-input v-model="learnerForm.availableTime" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="learnerForm.remark" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <div slot="footer"><el-button @click="learnerDialog = false">取消</el-button><el-button type="primary" @click="submitLearner">保存</el-button></div>
    </el-dialog>

    <el-dialog title="安排试听课" :visible.sync="trialDialog" width="520px" append-to-body>
      <el-form :model="trialForm" label-width="90px">
        <el-form-item label="试听时间"><el-date-picker v-model="trialForm.trialTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="选择时间" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="trialForm.trialRemark" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="trialDialog = false">取消</el-button><el-button type="primary" @click="submitTrial">保存</el-button></div>
    </el-dialog>

    <el-dialog title="客服工单" :visible.sync="ticketDialog" width="560px" append-to-body>
      <el-form :model="ticketForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="ticketForm.title" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="问题描述"><el-input v-model="ticketForm.content" type="textarea" :rows="5" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="ticketDialog = false">取消</el-button><el-button type="primary" @click="submitTicketForm">提交</el-button></div>
    </el-dialog>

    <el-dialog title="课程资料" :visible.sync="materialDialog" width="640px" append-to-body>
      <el-table :data="materialRows" size="small">
        <el-table-column label="标题" prop="title" width="160" />
        <el-table-column label="链接" min-width="220">
          <template slot-scope="scope"><a :href="scope.row.fileUrl" target="_blank">{{ scope.row.fileUrl }}</a></template>
        </el-table-column>
        <el-table-column label="上传人" prop="uploaderName" width="100" />
        <el-table-column label="时间" prop="createTime" width="160" />
      </el-table>
      <el-divider />
      <el-form :model="materialForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="materialForm.title" /></el-form-item>
        <el-form-item label="资料文件"><file-upload v-model="materialForm.fileUrl" :limit="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="materialForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="materialDialog = false">关闭</el-button><el-button type="primary" @click="submitMaterial">添加资料</el-button></div>
    </el-dialog>

    <el-dialog title="订单沟通" :visible.sync="messageDialog" width="640px" append-to-body @close="closeChatSocket">
      <div class="chat-status"><el-tag size="mini" :type="chatConnected ? 'success' : 'info'">{{ chatConnected ? '实时连接' : '普通留言' }}</el-tag></div>
      <el-table :data="messageRows" size="small" max-height="300">
        <el-table-column label="发送人" prop="senderName" width="110" />
        <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
        <el-table-column label="时间" prop="createTime" width="160" />
      </el-table>
      <el-divider />
      <el-form :model="messageForm" label-width="90px">
        <el-form-item label="留言"><el-input v-model="messageForm.content" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="messageDialog = false">关闭</el-button><el-button type="primary" @click="submitMessage">发送</el-button></div>
    </el-dialog>

    <el-dialog title="订单付款" :visible.sync="paymentDialog" width="680px" append-to-body>
      <el-table :data="paymentRows" size="small">
        <el-table-column label="金额" width="100"><template slot-scope="scope">￥{{ scope.row.amount }}</template></el-table-column>
        <el-table-column label="方式" prop="payMethod" width="90" />
        <el-table-column label="交易号" prop="tradeNo" min-width="140" show-overflow-tooltip />
        <el-table-column label="收据" prop="receiptNo" min-width="120" show-overflow-tooltip />
        <el-table-column label="退款" width="90"><template slot-scope="scope">￥{{ scope.row.refundAmount || 0 }}</template></el-table-column>
        <el-table-column label="凭证" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope"><a v-if="scope.row.proofUrl" :href="scope.row.proofUrl" target="_blank">查看凭证</a></template>
        </el-table-column>
        <el-table-column label="状态" width="90"><template slot-scope="scope">{{ paymentStatus(scope.row.status) }}</template></el-table-column>
        <el-table-column label="备注" prop="remark" min-width="140" show-overflow-tooltip />
        <el-table-column label="时间" prop="createTime" width="160" />
      </el-table>
      <el-divider />
      <el-form :model="paymentForm" label-width="90px">
        <el-form-item label="付款金额"><el-input-number v-model="paymentForm.amount" :min="1" :precision="2" :controls="false" /></el-form-item>
        <el-form-item label="支付方式"><el-input v-model="paymentForm.payMethod" placeholder="如：微信/支付宝/转账" /></el-form-item>
        <el-form-item label="交易号"><el-input v-model="paymentForm.tradeNo" placeholder="可选" /></el-form-item>
        <el-form-item label="付款凭证"><file-upload v-model="paymentForm.proofUrl" :limit="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="paymentForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="paymentDialog = false">关闭</el-button>
        <el-button @click="submitMockPayment">模拟支付</el-button>
        <el-button type="primary" @click="submitPayment">提交凭证</el-button>
      </div>
    </el-dialog>

    <el-dialog title="课后作业" :visible.sync="homeworkDialog" width="760px" append-to-body>
      <el-table :data="homeworkRows" size="small">
        <el-table-column label="标题" prop="title" width="140" />
        <el-table-column label="内容" prop="content" min-width="180" show-overflow-tooltip />
        <el-table-column label="提交" prop="submitText" min-width="160" show-overflow-tooltip />
        <el-table-column label="反馈" prop="feedback" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90"><template slot-scope="scope">{{ homeworkStatus(scope.row.status) }}</template></el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button v-if="workbenchRole === 'client' && scope.row.status === '0'" type="text" @click="submitHomeworkRow(scope.row)">提交</el-button>
            <el-button v-if="workbenchRole === 'tutor' && scope.row.status === '1'" type="text" @click="feedbackHomeworkRow(scope.row)">反馈</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider v-if="workbenchRole === 'tutor'" />
      <el-form v-if="workbenchRole === 'tutor'" :model="homeworkForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="homeworkForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="homeworkForm.content" type="textarea" :rows="3" maxlength="1000" show-word-limit /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="homeworkDialog = false">关闭</el-button>
        <el-button v-if="workbenchRole === 'tutor'" type="primary" @click="submitHomeworkForm">布置作业</el-button>
      </div>
    </el-dialog>

    <el-dialog title="平台公告" :visible.sync="announcementDialog" width="620px" append-to-body>
      <el-form :model="announcementForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="announcementForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="announcementForm.content" type="textarea" :rows="5" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="announcementForm.status">
            <el-radio label="1">发布</el-radio>
            <el-radio label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="announcementDialog = false">取消</el-button><el-button type="primary" @click="submitAnnouncement">保存</el-button></div>
    </el-dialog>

    <el-dialog title="教员详情" :visible.sync="tutorDialog" width="560px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ tutorDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ tutorDetail.university }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ tutorDetail.major }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ tutorDetail.collegeYear }}</el-descriptions-item>
        <el-descriptions-item label="擅长科目">{{ tutorDetail.subjects }}</el-descriptions-item>
        <el-descriptions-item label="可授课时间">{{ tutorDetail.availabilityText || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="期望课时费">￥{{ tutorDetail.hourlyRate }}/小时</el-descriptions-item>
        <el-descriptions-item label="已完成订单">{{ tutorDetail.completedOrders || 0 }}</el-descriptions-item>
        <el-descriptions-item label="平均评分">{{ tutorDetail.averageRating ? tutorDetail.averageRating + ' 分' : '暂无评价' }}</el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">{{ tutorDetail.introduction || '暂无' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="workbenchRole === 'client'" slot="footer">
        <el-button @click="addFavorite(tutorDetail)">收藏教员</el-button>
        <el-button type="primary" @click="openInvite(tutorDetail)">发起预约</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyProfile, getTutorProfile, saveMyProfile, listPendingProfiles, verifyProfile,
  listLearners, saveLearner, deleteLearner, listAvailability, addAvailability, deleteAvailability,
  listAnnouncements, listAdminAnnouncements, saveAnnouncement, deleteAnnouncement,
  listVerifiedTutors, listOpenRequests, listAdminRequests, listMyRequests, publishRequest, copyRequest,
  cancelRequest, listMyMatches, listAdminClients, listAdminTutors, listAdminMatches,
  listAdminInvitations, listAdminLessons, listAdminSettlements, listAdminTickets, changeAdminUserStatus, closeAdminMatch,
  applyRequest, withdrawMatch, cancelMatch, rescheduleMatch, scheduleTrial, completeTrial, acceptMatch, completeMatch, reviewMatch, getDashboard, getDashboardTodos,
  listCalendarLessons, listLearningRecords, getCrmDashboard, listRiskAlerts, getOperationsReport, generateReminders,
  listLessons, addLesson, confirmLesson, listMaterials, addMaterial, listMessages, addMessage, listPayments, addPayment, mockPayment,
  listHomeworks, addHomework, submitHomework, feedbackHomework,
  listNotifications, readNotification, readAllNotifications, getUnreadNotificationCount, submitComplaint,
  listMySettlements, settleSettlement, batchSettleSettlements, listAdminPayments, handlePayment, refundPayment, reconcilePayment, issueInvoice, listFinanceLedgers, listAdminBlacklists, saveBlacklist, disableBlacklist, listAdminFollowups, addFollowup, completeFollowup, listTickets, submitTicket, handleTicket,
  listMyComplaints, listComplaints, handleComplaint, listRecommendedRequests,
  listFavoriteTutors, favoriteTutor as favoriteTutorApi, unfavoriteTutor, inviteTutor,
  listInvitations, acceptInvitation, rejectInvitation
} from '@/api/tutoring'
import { getToken } from '@/utils/auth'
const { resolveWorkbenchRole, getWorkbenchConfig } = require('./roleConfig.cjs')

const emptyProfile = () => ({ university: '', major: '', collegeYear: '', subjects: '', hourlyRate: 60, availabilityText: '', introduction: '', studentCardUrl: '', qualificationUrl: '' })
const emptyRequest = () => ({ learnerGrade: '', subject: '', area: '', scheduleText: '', hourlyBudget: 80, requirementText: '', sourceChannel: '平台发布' })
const emptyInvite = () => ({ tutorId: null, learnerGrade: '', subject: '', area: '', scheduleText: '', offeredRate: 80, message: '' })
const emptyLearner = () => ({ learnerId: null, learnerName: '', grade: '', school: '', weakSubjects: '', targetScore: '', availableTime: '', remark: '' })
const emptyAvailability = () => ({ weekDay: '1', startTime: '18:00', endTime: '20:00', remark: '' })
const emptyTicket = () => ({ title: '', content: '' })
const emptyAnnouncement = () => ({ announcementId: null, title: '', content: '', status: '1' })
const emptyMaterial = () => ({ title: '', fileUrl: '', remark: '' })
const emptyFollowup = () => ({ matchId: null, content: '', nextAction: '', nextTime: '', status: '0' })
const emptyMessage = () => ({ content: '' })
const emptyPayment = () => ({ amount: 0, proofUrl: '', payMethod: 'voucher', tradeNo: '', remark: '' })
const emptyHomework = () => ({ lessonId: null, title: '', content: '', submitText: '', feedback: '' })
const emptyBlacklist = () => ({ userId: null, reason: '' })
const countRows = response => response && response.total != null
  ? response.total
  : ((response && (response.rows || response.data)) || []).length
const ROLE_META = {
  admin: { role: '管理员', title: '家教运营管理台', subtitle: '集中处理审核、用户、需求、订单、课时与投诉，适合日常运营巡检。', icon: 'el-icon-s-tools' },
  tutor: { role: '大学生教员', title: '教员接单工作台', subtitle: '维护认证资料，查看推荐需求，管理订单、课时记录和消息通知。', icon: 'el-icon-reading' },
  client: { role: '家长学员', title: '家教服务工作台', subtitle: '筛选教员、发布需求、处理预约、确认课时并跟进服务评价。', icon: 'el-icon-s-custom' }
}
const NAV_META = {
  dashboard: { label: '数据看板', icon: 'el-icon-s-data', desc: '查看平台核心指标、待办事项和热门科目。' },
  businessClients: { label: '家长管理', icon: 'el-icon-user', desc: '管理家长账号状态、联系方式和业务统计。' },
  businessTutors: { label: '教员管理', icon: 'el-icon-reading', desc: '查看教员资料、认证状态、评分和账号状态。' },
  businessRequests: { label: '需求监管', icon: 'el-icon-document', desc: '按科目和状态巡检家教需求发布情况。' },
  businessMatches: { label: '订单监管', icon: 'el-icon-s-order', desc: '跟进申请、接单、完成和异常关闭订单。' },
  businessInvitations: { label: '预约监管', icon: 'el-icon-message', desc: '查看家长向教员发起的预约与响应状态。' },
  businessLessons: { label: '课时监管', icon: 'el-icon-date', desc: '查看课时记录、费用和家长确认状态。' },
  verify: { label: '教员审核', icon: 'el-icon-s-check', desc: '审核大学生教员提交的认证材料。' },
  complaints: { label: '投诉处理', icon: 'el-icon-warning-outline', desc: '跟踪投诉原因、处理意见和处理记录。' },
  profile: { label: '认证资料', icon: 'el-icon-user', desc: '维护学校、专业、擅长科目、课时费和授课时间。' },
  open: { label: '开放需求', icon: 'el-icon-document', desc: '浏览可申请的家教需求并提交报价说明。' },
  recommended: { label: '智能推荐', icon: 'el-icon-magic-stick', desc: '按资料匹配度查看更适合申请的需求。' },
  matches: { label: '订单管理', icon: 'el-icon-s-order', desc: '处理申请、接单、课程记录、评价、改期和投诉。' },
  invitations: { label: '预约邀请', icon: 'el-icon-message', desc: '处理家长预约邀请和预约流转状态。' },
  notifications: { label: '消息通知', icon: 'el-icon-bell', desc: '查看系统通知并处理未读消息。' },
  tutors: { label: '教员库', icon: 'el-icon-s-custom', desc: '筛选认证教员，查看详情、收藏或发起预约。' },
  requests: { label: '我的需求', icon: 'el-icon-tickets', desc: '管理已发布需求，支持取消和复制再发布。' },
  favorites: { label: '收藏教员', icon: 'el-icon-star-off', desc: '维护常用教员名单并快速发起预约。' }
}
Object.assign(NAV_META, {
  businessSettlements: { label: '结算管理', icon: 'el-icon-money', desc: '核对已确认课时生成的待结算课时费。' },
  businessTickets: { label: '工单处理', icon: 'el-icon-service', desc: '处理家长和教员提交的平台问题。' },
  businessCrm: { label: '招生 CRM', icon: 'el-icon-connection', desc: '查看线索、试听、成单和来源转化。' },
  businessRisk: { label: '风控质检', icon: 'el-icon-warning-outline', desc: '跟踪投诉 SLA、异常报价、取消和教员低分风险。' },
  businessReports: { label: '运营报表', icon: 'el-icon-data-analysis', desc: '查看转化、复购、GMV、抽成和服务效率。' },
  businessReminders: { label: '提醒中心', icon: 'el-icon-bell', desc: '生成课前、付款和工单提醒。' },
  businessBlacklists: { label: '黑名单', icon: 'el-icon-remove-outline', desc: '限制异常家长或教员继续发布、申请和预约。' },
  businessLedgers: { label: '财务流水', icon: 'el-icon-notebook-2', desc: '追踪付款、退款、对账、开票和结算动作。' },
  availability: { label: '授课日历', icon: 'el-icon-time', desc: '维护可预约的星期和时间段。' },
  settlements: { label: '收入结算', icon: 'el-icon-money', desc: '查看已确认课时对应的结算状态。' },
  calendar: { label: '排课日历', icon: 'el-icon-date', desc: '查看即将上课的日期、时间和确认状态。' },
  learning: { label: '学习档案', icon: 'el-icon-notebook-1', desc: '查看课后报告、作业、出勤和阶段反馈。' },
  tickets: { label: '客服工单', icon: 'el-icon-service', desc: '提交和跟踪平台服务问题。' },
  learners: { label: '学员档案', icon: 'el-icon-notebook-2', desc: '维护学员年级、薄弱科目、目标和可上课时间。' },
  announcements: { label: '平台公告', icon: 'el-icon-date', desc: '查看平台通知、服务规则和运营提醒。' },
  businessAnnouncements: { label: '公告管理', icon: 'el-icon-date', desc: '发布和维护平台公告。' },
  businessFollowups: { label: '回访记录', icon: 'el-icon-phone-outline', desc: '记录订单服务回访和后续动作。' },
  businessPayments: { label: '支付流水', icon: 'el-icon-bank-card', desc: '核验家长提交的付款凭证和订单收款状态。' }
})

export default {
  name: 'TutoringWorkbench',
  data() {
    return {
      activeTab: '', loading: false,
      tutors: [], openRequests: [], recommendedRequests: [], myRequests: [], matches: [], pendingProfiles: [],
      notifications: [], complaints: [], favoriteTutors: [], invitations: [], lessons: [], calendarRows: [], learningRows: [],
      learnerRows: [], availabilityRows: [], settlementRows: [], ticketRows: [],
      announcementRows: [], materialRows: [], followupRows: [], messageRows: [], paymentRows: [], homeworkRows: [],
      adminClients: [], adminTutors: [], adminRequests: [], adminMatches: [], adminInvitations: [], adminLessons: [],
      adminSettlements: [], selectedSettlements: [], adminTickets: [], adminAnnouncements: [], adminPayments: [],
      adminBlacklists: [], financeLedgers: [],
      crmDashboard: { sourceStats: [] }, riskAlerts: [], operationsReport: { subjectHeat: [] }, reminderResult: {},
      notificationQuery: { readStatus: '', title: '' }, unreadNotifications: 0,
      profileDialog: false, requestDialog: false, applyDialog: false, reviewDialog: false, tutorDialog: false,
      lessonDialog: false, complaintDialog: false, inviteDialog: false, learnerDialog: false, ticketDialog: false, trialDialog: false,
      materialDialog: false, announcementDialog: false, messageDialog: false, paymentDialog: false, homeworkDialog: false,
      tutorQuery: { subjects: '', university: '' },
      queryForm: { subject: '', learnerGrade: '', area: '', minBudget: undefined, maxBudget: undefined },
      adminClientQuery: { userName: '', status: '' },
      adminTutorQuery: { userName: '', university: '', subjects: '', verifyStatus: '' },
      adminRequestQuery: { subject: '', status: '', sourceChannel: '' },
      adminMatchQuery: { subject: '', status: '' },
      adminInvitationQuery: { subject: '', status: '' },
      adminLessonQuery: { subject: '' },
      adminSettlementQuery: { subject: '', status: '' },
      adminTicketQuery: { title: '', status: '' },
      adminAnnouncementQuery: { title: '', status: '' },
      adminPaymentQuery: { status: '' },
      blacklistQuery: { userId: undefined, status: '' },
      ledgerQuery: { bizType: '', matchId: undefined, status: '' },
      chatSocket: null, chatConnected: false,
      tutorDetail: {}, dashboard: { topSubjects: [] }, adminTodos: {},
      overview: { tutors: 0, openRequests: 0, recommended: 0, requests: 0, matches: 0, invitations: 0, unread: 0 },
      currentMatch: {},
      profileForm: emptyProfile(), requestForm: emptyRequest(),
      applyForm: { requestId: null, quotedRate: 60, applicationText: '' },
      reviewForm: { matchId: null, rating: 5, reviewText: '' },
      lessonForm: { lessonDate: '', startTime: '18:00', endTime: '20:00', hours: 1, content: '', studentPerformance: '', homework: '', nextPlan: '', attendanceStatus: '1', phaseFeedback: '' },
      learnerForm: emptyLearner(),
      availabilityForm: emptyAvailability(),
      ticketForm: emptyTicket(),
      announcementForm: emptyAnnouncement(),
      materialForm: emptyMaterial(),
      followupForm: emptyFollowup(),
      messageForm: emptyMessage(),
      paymentForm: emptyPayment(),
      homeworkForm: emptyHomework(),
      blacklistForm: emptyBlacklist(),
      trialForm: { matchId: null, trialTime: '', trialRemark: '' },
      complaintForm: { matchId: null, reason: '' },
      inviteForm: emptyInvite(),
      profileRules: {
        university: [{ required: true, message: '请输入学校', trigger: 'blur' }],
        major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
        collegeYear: [{ required: true, message: '请输入年级', trigger: 'blur' }],
        subjects: [{ required: true, message: '请输入擅长科目', trigger: 'blur' }]
      },
      requestRules: {
        learnerGrade: [{ required: true, message: '请输入学员年级', trigger: 'blur' }],
        subject: [{ required: true, message: '请输入辅导科目', trigger: 'blur' }],
        area: [{ required: true, message: '请输入授课区域', trigger: 'blur' }],
        scheduleText: [{ required: true, message: '请输入期望时间', trigger: 'blur' }]
      },
      applyRules: { quotedRate: [{ required: true, message: '请输入报价', trigger: 'change' }] }
    }
  },
  computed: {
    workbenchRole() {
      return resolveWorkbenchRole(this.$store.getters.roles || [])
    },
    workbenchConfig() {
      return getWorkbenchConfig(this.workbenchRole, { tutorVerified: this.profileForm.verifyStatus === '1' })
    },
    complaintsLabel() {
      return this.workbenchRole === 'admin' ? '投诉处理' : '我的投诉'
    },
    roleMeta() {
      return ROLE_META[this.workbenchRole] || { role: '工作台', title: '家教工作台', subtitle: '', icon: 'el-icon-s-platform' }
    },
    userAvatar() {
      return this.$store.getters.avatar
    },
    userNickName() {
      return this.$store.getters.nickName || this.$store.getters.name || '个人中心'
    },
    workbenchNavItems() {
      return this.workbenchConfig.tabs.map(name => {
        const item = NAV_META[name] || { label: name, icon: 'el-icon-menu', desc: '' }
        return { ...item, name, label: name === 'complaints' ? this.complaintsLabel : item.label, badge: this.navBadge(name) }
      })
    },
    activeNav() {
      return this.workbenchNavItems.find(item => item.name === this.activeTab) || { label: '工作台', desc: '' }
    },
    roleMetricCards() {
      if (this.workbenchRole === 'admin') {
        return [
          { label: '需求总数', value: this.dashboard.totalRequests || 0, note: '全平台需求', icon: 'el-icon-document' },
          { label: '开放需求', value: this.dashboard.openRequests || 0, note: '正在招募', icon: 'el-icon-tickets' },
          { label: '完成订单', value: this.dashboard.completedRequests || 0, note: '已闭环服务', icon: 'el-icon-circle-check' },
          { label: '认证教员', value: this.dashboard.verifiedTutors || 0, note: '可接单教员', icon: 'el-icon-user' },
          { label: '待办事项', value: this.adminTodoTotal, note: '需运营处理', icon: 'el-icon-warning-outline', warning: this.adminTodoTotal > 0 },
          { label: '匹配率', value: `${this.dashboard.matchRate || 0}%`, note: '需求转化', icon: 'el-icon-data-line' }
        ]
      }
      if (this.workbenchRole === 'tutor') {
        return [
          { label: '认证状态', value: this.verifyStatus(this.profileForm.verifyStatus), note: '通过后开放接单', icon: 'el-icon-s-check', warning: this.profileForm.verifyStatus !== '1' },
          { label: '开放需求', value: this.overview.openRequests || 0, note: '可申请需求', icon: 'el-icon-document' },
          { label: '推荐需求', value: this.overview.recommended || 0, note: '匹配资料', icon: 'el-icon-magic-stick' },
          { label: '我的订单', value: this.overview.matches || 0, note: '申请与接单', icon: 'el-icon-s-order' },
          { label: '预约邀请', value: this.overview.invitations || 0, note: '待响应邀约', icon: 'el-icon-message' },
          { label: '未读消息', value: this.overview.unread || 0, note: '系统通知', icon: 'el-icon-bell', warning: this.overview.unread > 0 }
        ]
      }
      return [
        { label: '认证教员', value: this.overview.tutors || 0, note: '可预约名单', icon: 'el-icon-s-custom' },
        { label: '我的需求', value: this.overview.requests || 0, note: '已发布需求', icon: 'el-icon-tickets' },
        { label: '我的订单', value: this.overview.matches || 0, note: '服务进度', icon: 'el-icon-s-order' },
        { label: '预约邀请', value: this.overview.invitations || 0, note: '预约流转', icon: 'el-icon-message' },
        { label: '未读消息', value: this.overview.unread || 0, note: '系统通知', icon: 'el-icon-bell', warning: this.overview.unread > 0 }
      ]
    },
    adminTodoTotal() {
      return ['pendingProfiles', 'pendingComplaints', 'unconfirmedLessons', 'staleMatches', 'pendingSettlements', 'pendingTickets', 'dueFollowups']
        .reduce((sum, key) => sum + Number(this.adminTodos[key] || 0), 0)
    },
    dashboardCards() {
      return [
        { label: '需求总数', value: this.dashboard.totalRequests || 0 },
        { label: '开放需求', value: this.dashboard.openRequests || 0 },
        { label: '匹配率', value: `${this.dashboard.matchRate || 0}%` },
        { label: '完成订单', value: this.dashboard.completedRequests || 0 },
        { label: '认证教员', value: this.dashboard.verifiedTutors || 0 },
        { label: '平均评分', value: this.dashboard.averageRating || 0 }
      ]
    },
    crmCards() {
      return [
        { label: '线索数', value: this.crmDashboard.leadCount || 0 },
        { label: '开放线索', value: this.crmDashboard.openLeads || 0 },
        { label: '申请数', value: this.crmDashboard.applyCount || 0 },
        { label: '试听数', value: this.crmDashboard.trialCount || 0 },
        { label: '成单数', value: this.crmDashboard.acceptedCount || 0 },
        { label: '转化率', value: `${this.crmDashboard.conversionRate || 0}%` }
      ]
    },
    reportCards() {
      return [
        { label: '线索数', value: this.operationsReport.leadCount || 0 },
        { label: '成单数', value: this.operationsReport.dealCount || 0 },
        { label: '复购客户', value: this.operationsReport.repeatClients || 0 },
        { label: 'GMV', value: `¥${this.operationsReport.gmv || 0}` },
        { label: '抽成收入', value: `¥${this.operationsReport.platformRevenue || 0}` },
        { label: '客诉率', value: `${this.operationsReport.complaintRate || 0}%` }
      ]
    },
    reminderCards() {
      return [
        { label: '课前提醒', value: this.reminderResult.lessonReminders || 0 },
        { label: '付款提醒', value: this.reminderResult.paymentReminders || 0 },
        { label: '工单提醒', value: this.reminderResult.ticketReminders || 0 }
      ]
    },
    adminTodoCards() {
      return [
        { label: '待审核教员', value: this.adminTodos.pendingProfiles || 0 },
        { label: '待处理投诉', value: this.adminTodos.pendingComplaints || 0 },
        { label: '待确认课时', value: this.adminTodos.unconfirmedLessons || 0 },
        { label: '待跟进回访', value: this.adminTodos.dueFollowups || 0 }
      ]
    },
    profileCompleteness() {
      const fields = ['university', 'major', 'collegeYear', 'subjects', 'hourlyRate', 'availabilityText', 'introduction', 'studentCardUrl']
      const filled = fields.filter(key => String(this.profileForm[key] || '').trim()).length
      return Math.round(filled * 100 / fields.length)
    },
    profileVerifyText() {
      const text = { '0': '资料待审核', '1': '资料已通过审核', '2': '资料被驳回' }[this.profileForm.verifyStatus]
      return `${text || '尚未提交教员资料'}${this.profileForm.verifyRemark ? '：' + this.profileForm.verifyRemark : ''}`
    }
  },
  created() { this.initWorkbench() },
  beforeDestroy() { this.closeChatSocket() },
  methods: {
    has(permission) {
      const permissions = this.$store.getters.permissions || []
      return permissions.includes('*:*:*') || permissions.includes(permission)
    },
    toolbarVisible(name) {
      return this.workbenchConfig.toolbar.includes(name)
    },
    tabVisible(name) {
      return this.workbenchConfig.tabs.includes(name)
    },
    actionVisible(tab, action) {
      return (this.workbenchConfig.actions[tab] || []).includes(action)
    },
    navBadge(name) {
      if (name === 'notifications') return this.overview.unread || 0
      if (name === 'invitations') return this.overview.invitations || 0
      if (name === 'verify') return this.adminTodos.pendingProfiles || 0
      if (name === 'complaints') return this.workbenchRole === 'admin' ? (this.adminTodos.pendingComplaints || 0) : 0
      if (name === 'businessLessons') return this.adminTodos.unconfirmedLessons || 0
      if (name === 'businessSettlements') return this.adminTodos.pendingSettlements || 0
      if (name === 'businessTickets') return this.adminTodos.pendingTickets || 0
      if (name === 'businessMatches') return this.adminTodos.staleMatches || 0
      if (name === 'businessFollowups') return this.adminTodos.dueFollowups || 0
      if (name === 'businessRisk') return this.riskAlerts.length || 0
      return 0
    },
    switchTab(name) {
      if (this.activeTab === name) return this.loadCurrentTab()
      this.activeTab = name
      return this.loadCurrentTab()
    },
    goProfile() {
      this.$router.push('/user/profile')
    },
    lockScreen() {
      this.$store.dispatch('lock/lockScreen', this.$route.fullPath).then(() => {
        this.$router.push('/lock')
      })
    },
    logout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/index'
        })
      }).catch(() => {})
    },
    ensureActiveTab() {
      if (!this.tabVisible(this.activeTab)) this.activeTab = this.workbenchConfig.defaultTab
    },
    initWorkbench() {
      const ready = this.workbenchRole === 'tutor' ? this.loadProfile(false) : Promise.resolve()
      ready.finally(() => {
        this.ensureActiveTab()
        this.loadAll()
      })
    },
    loadProfile(showDialog) {
      return getMyProfile().then(r => {
        this.profileForm = r.data || emptyProfile()
        this.profileDialog = !!showDialog
      }).catch(() => {
        this.profileForm = emptyProfile()
        this.profileDialog = !!showDialog
      })
    },
    loadAll() {
      return Promise.all([this.loadRoleOverview().catch(() => {}), this.loadCurrentTab()])
    },
    loadRoleOverview() {
      if (!this.workbenchRole) return Promise.resolve()
      if (this.workbenchRole === 'admin') {
        return getDashboard()
          .then(r => { this.dashboard = r.data || { topSubjects: [] } })
          .then(() => this.loadAdminTodos())
      }
      if (this.workbenchRole === 'tutor') {
        if (this.profileForm.verifyStatus !== '1') {
          this.overview = { ...this.overview, openRequests: 0, recommended: 0, matches: 0, invitations: 0 }
          return Promise.resolve()
        }
        return Promise.all([
          listOpenRequests({}),
          listRecommendedRequests(),
          listMyMatches(),
          listInvitations(),
          getUnreadNotificationCount()
        ]).then(([open, recommended, matches, invitations, unread]) => {
          this.overview = {
            ...this.overview,
            openRequests: countRows(open),
            recommended: countRows(recommended),
            matches: countRows(matches),
            invitations: countRows(invitations),
            unread: unread.data || 0
          }
        })
      }
      return Promise.all([
        listVerifiedTutors({}),
        listMyRequests(),
        listMyMatches(),
        listInvitations(),
        getUnreadNotificationCount()
      ]).then(([tutors, requests, matches, invitations, unread]) => {
        this.overview = {
          ...this.overview,
          tutors: countRows(tutors),
          requests: countRows(requests),
          matches: countRows(matches),
          invitations: countRows(invitations),
          unread: unread.data || 0
        }
      })
    },
    loadCurrentTab() {
      if (!this.workbenchRole) return Promise.resolve()
      this.ensureActiveTab()
      const loaders = {
        profile: () => this.loadProfile(false),
        learners: () => listLearners().then(r => { this.learnerRows = r.rows || [] }),
        availability: () => listAvailability().then(r => { this.availabilityRows = r.data || [] }),
        tutors: () => listVerifiedTutors(this.tutorQuery).then(r => { this.tutors = r.rows || [] }),
        open: () => listOpenRequests(this.queryForm).then(r => { this.openRequests = r.rows || [] }),
        recommended: () => listRecommendedRequests().then(r => { this.recommendedRequests = r.data || [] }),
        requests: () => listMyRequests().then(r => { this.myRequests = r.rows || [] }),
        matches: () => listMyMatches().then(r => { this.matches = r.rows || [] }),
        calendar: () => listCalendarLessons().then(r => { this.calendarRows = r.data || [] }),
        learning: () => listLearningRecords().then(r => { this.learningRows = r.data || [] }),
        notifications: () => this.loadNotifications(),
        invitations: () => listInvitations().then(r => { this.invitations = r.data || [] }),
        announcements: () => listAnnouncements().then(r => { this.announcementRows = r.data || [] }),
        settlements: () => listMySettlements().then(r => { this.settlementRows = r.data || [] }),
        tickets: () => listTickets().then(r => { this.ticketRows = r.data || [] }),
        complaints: () => (this.workbenchRole === 'admin' ? listComplaints() : listMyComplaints()).then(r => { this.complaints = r.data || [] }),
        favorites: () => listFavoriteTutors().then(r => { this.favoriteTutors = r.data || [] }),
        verify: () => listPendingProfiles().then(r => { this.pendingProfiles = r.rows || [] }),
        dashboard: () => getDashboard().then(r => { this.dashboard = r.data || { topSubjects: [] } }).then(() => this.loadAdminTodos()),
        businessCrm: () => getCrmDashboard().then(r => { this.crmDashboard = r.data || { sourceStats: [] } }),
        businessRisk: () => listRiskAlerts().then(r => { this.riskAlerts = r.data || [] }),
        businessReports: () => getOperationsReport().then(r => { this.operationsReport = r.data || { subjectHeat: [] } }),
        businessReminders: () => Promise.resolve(),
        businessBlacklists: () => listAdminBlacklists(this.blacklistQuery).then(r => { this.adminBlacklists = r.rows || [] }),
        businessLedgers: () => listFinanceLedgers(this.ledgerQuery).then(r => { this.financeLedgers = r.rows || [] }),
        businessClients: () => listAdminClients(this.adminClientQuery).then(r => { this.adminClients = r.rows || [] }),
        businessTutors: () => listAdminTutors(this.adminTutorQuery).then(r => { this.adminTutors = r.rows || [] }),
        businessRequests: () => listAdminRequests(this.adminRequestQuery).then(r => { this.adminRequests = r.rows || [] }),
        businessMatches: () => listAdminMatches(this.adminMatchQuery).then(r => { this.adminMatches = r.rows || [] }),
        businessInvitations: () => listAdminInvitations(this.adminInvitationQuery).then(r => { this.adminInvitations = r.rows || [] }),
        businessLessons: () => listAdminLessons(this.adminLessonQuery).then(r => { this.adminLessons = r.rows || [] }),
        businessSettlements: () => listAdminSettlements(this.adminSettlementQuery).then(r => { this.adminSettlements = r.rows || [] }),
        businessTickets: () => listAdminTickets(this.adminTicketQuery).then(r => { this.adminTickets = r.rows || [] }),
        businessAnnouncements: () => listAdminAnnouncements(this.adminAnnouncementQuery).then(r => { this.adminAnnouncements = r.rows || [] }),
        businessFollowups: () => listAdminFollowups(this.followupForm).then(r => { this.followupRows = r.rows || [] }),
        businessPayments: () => listAdminPayments(this.adminPaymentQuery).then(r => { this.adminPayments = r.rows || [] })
      }
      const loader = loaders[this.activeTab]
      if (!loader) return Promise.resolve()
      this.loading = true
      return loader().finally(() => { this.loading = false })
    },
    loadNotifications() {
      return listNotifications(this.notificationQuery).then(r => {
        this.notifications = r.data || []
        return getUnreadNotificationCount()
      }).then(r => {
        this.unreadNotifications = r.data || 0
        this.overview = { ...this.overview, unread: this.unreadNotifications }
      })
    },
    loadAdminTodos() {
      if (this.workbenchRole !== 'admin') return Promise.resolve()
      return getDashboardTodos().then(r => { this.adminTodos = r.data || {} })
    },
    handleAdminExport(type, query) {
      this.download(`system/tutoring/admin/export/${type}`, query, `tutoring-${type}-${new Date().getTime()}.csv`)
    },
    requestStatus(status) { return { '0': '招募中', '1': '已匹配', '2': '已完成', '3': '已取消' }[status] || status },
    matchStatus(status) { return { '0': '申请中', '1': '已接单', '2': '已完成', '3': '未选中', '4': '已取消' }[status] || status },
    complaintStatus(status) { return { '0': '待处理', '1': '已解决', '2': '已驳回' }[status] || status },
    invitationStatus(status) { return { '0': '待处理', '1': '已接受', '2': '已拒绝' }[status] || status },
    userStatus(status) { return { '0': '正常', '1': '停用' }[status] || status || '-' },
    verifyStatus(status) { return { '0': '待审核', '1': '已通过', '2': '已驳回' }[status] || '未提交' },
    searchRequests() {
      if (this.queryForm.minBudget != null && this.queryForm.maxBudget != null && this.queryForm.minBudget > this.queryForm.maxBudget) {
        return this.$modal.msgError('最低预算不能高于最高预算')
      }
      this.loadCurrentTab()
    },
    resetSearch() {
      this.queryForm = { subject: '', learnerGrade: '', area: '', minBudget: undefined, maxBudget: undefined }
      this.searchRequests()
    },
    searchTutors() {
      this.loadCurrentTab()
    },
    resetTutorSearch() {
      this.tutorQuery = { subjects: '', university: '' }
      this.searchTutors()
    },
    searchAdminClients() {
      this.loadCurrentTab()
    },
    resetAdminClients() {
      this.adminClientQuery = { userName: '', status: '' }
      this.searchAdminClients()
    },
    searchAdminTutors() {
      this.loadCurrentTab()
    },
    resetAdminTutors() {
      this.adminTutorQuery = { userName: '', university: '', subjects: '', verifyStatus: '' }
      this.searchAdminTutors()
    },
    searchAdminRequests() {
      this.loadCurrentTab()
    },
    resetAdminRequests() {
      this.adminRequestQuery = { subject: '', status: '', sourceChannel: '' }
      this.searchAdminRequests()
    },
    searchAdminMatches() {
      this.loadCurrentTab()
    },
    resetAdminMatches() {
      this.adminMatchQuery = { subject: '', status: '' }
      this.searchAdminMatches()
    },
    changeBusinessUserStatus(userId, status) {
      const target = status === '0' ? '1' : '0'
      const action = target === '1' ? '停用' : '启用'
      this.$modal.confirm(`确认${action}该账号吗？`).then(() => changeAdminUserStatus(userId, target)).then(() => {
        this.$modal.msgSuccess(`${action}成功`)
        this.loadCurrentTab()
      }).catch(() => {})
    },
    closeBusinessMatch(row) {
      this.$modal.confirm('确认关闭该异常订单吗？').then(() => closeAdminMatch(row.matchId)).then(() => {
        this.$modal.msgSuccess('订单已关闭')
        this.loadCurrentTab()
      }).catch(() => {})
    },
    searchAdminInvitations() {
      this.loadCurrentTab()
    },
    resetAdminInvitations() {
      this.adminInvitationQuery = { subject: '', status: '' }
      this.searchAdminInvitations()
    },
    searchAdminLessons() {
      this.loadCurrentTab()
    },
    resetAdminLessons() {
      this.adminLessonQuery = { subject: '' }
      this.searchAdminLessons()
    },
    searchAdminSettlements() {
      this.loadCurrentTab()
    },
    resetAdminSettlements() {
      this.adminSettlementQuery = { subject: '', status: '' }
      this.searchAdminSettlements()
    },
    searchAdminPayments() {
      this.loadCurrentTab()
    },
    resetAdminPayments() {
      this.adminPaymentQuery = { status: '' }
      this.searchAdminPayments()
    },
    searchAdminTickets() {
      this.loadCurrentTab()
    },
    resetAdminTickets() {
      this.adminTicketQuery = { title: '', status: '' }
      this.searchAdminTickets()
    },
    weekDayText(day) {
      return { '1': '周一', '2': '周二', '3': '周三', '4': '周四', '5': '周五', '6': '周六', '7': '周日' }[day] || day
    },
    trialStatus(status) {
      return { '0': '未安排', '1': '待试听', '2': '已完成' }[status] || '未安排'
    },
    settlementStatus(status) {
      return { '0': '待结算', '1': '已结算' }[status] || status
    },
    ticketStatus(status) {
      return { '0': '待处理', '1': '已解决', '2': '已关闭' }[status] || status
    },
    announcementStatus(status) {
      return { '0': '草稿', '1': '发布' }[status] || status
    },
    followupStatus(status) {
      return { '0': '待跟进', '1': '已完成' }[status] || status
    },
    attendanceStatus(status) {
      return { '0': '待确认', '1': '到课', '2': '请假', '3': '缺勤' }[status] || status || '-'
    },
    riskSeverity(severity) {
      return { high: 'danger', medium: 'warning', low: 'info' }[severity] || 'info'
    },
    paymentStatus(status) {
      return { '0': '待确认', '1': '已确认', '2': '已驳回', '3': '已退款' }[status] || status
    },
    homeworkStatus(status) {
      return { '0': '待提交', '1': '待反馈', '2': '已反馈' }[status] || status
    },
    openAnnouncement(row) {
      this.announcementForm = row ? { ...row } : emptyAnnouncement()
      this.announcementDialog = true
    },
    submitAnnouncement() {
      if (!this.announcementForm.title || !this.announcementForm.content) return this.$modal.msgError('请填写公告标题和内容')
      saveAnnouncement(this.announcementForm).then(() => {
        this.$modal.msgSuccess('公告已保存')
        this.announcementDialog = false
        this.loadCurrentTab()
      })
    },
    removeAnnouncement(row) {
      this.$modal.confirm('确认删除该公告吗？').then(() => deleteAnnouncement(row.announcementId)).then(() => {
        this.$modal.msgSuccess('公告已删除')
        this.loadCurrentTab()
      }).catch(() => {})
    },
    openMaterials(row) {
      this.currentMatch = row
      this.materialForm = emptyMaterial()
      listMaterials(row.matchId).then(r => {
        this.materialRows = r.data || []
        this.materialDialog = true
      })
    },
    submitMaterial() {
      if (!this.materialForm.title || !this.materialForm.fileUrl) return this.$modal.msgError('请填写资料标题和链接')
      addMaterial(this.currentMatch.matchId, this.materialForm).then(() => {
        this.$modal.msgSuccess('课程资料已添加')
        this.openMaterials(this.currentMatch)
      })
    },
    openMessages(row) {
      this.currentMatch = row
      this.messageForm = emptyMessage()
      listMessages(row.matchId).then(r => {
        this.messageRows = r.data || []
        this.messageDialog = true
        this.connectChatSocket(row.matchId)
      })
    },
    submitMessage() {
      if (!this.messageForm.content) return this.$modal.msgError('请输入留言内容')
      if (this.chatSocket && this.chatSocket.readyState === WebSocket.OPEN) {
        this.chatSocket.send(this.messageForm.content)
        this.messageForm = emptyMessage()
        return
      }
      addMessage(this.currentMatch.matchId, this.messageForm).then(() => {
        this.$modal.msgSuccess('留言已发送')
        this.openMessages(this.currentMatch)
      })
    },
    refreshMessages() {
      if (!this.currentMatch.matchId) return
      return listMessages(this.currentMatch.matchId).then(r => { this.messageRows = r.data || [] })
    },
    connectChatSocket(matchId) {
      this.closeChatSocket()
      const token = getToken()
      if (!token || typeof WebSocket === 'undefined') return
      const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
      const baseApi = process.env.VUE_APP_BASE_API || ''
      const wsUrl = `${protocol}://${window.location.host}${baseApi}/system/tutoring/ws/messages/${matchId}/${encodeURIComponent(token)}`
      const socket = new WebSocket(wsUrl)
      this.chatSocket = socket
      socket.onopen = () => { this.chatConnected = true }
      socket.onmessage = () => { this.refreshMessages() }
      socket.onerror = () => { this.chatConnected = false }
      socket.onclose = () => {
        if (this.chatSocket === socket) this.chatSocket = null
        this.chatConnected = false
      }
    },
    closeChatSocket() {
      if (this.chatSocket) {
        this.chatSocket.close()
        this.chatSocket = null
      }
      this.chatConnected = false
    },
    openPayments(row) {
      this.currentMatch = row
      this.paymentForm = { ...emptyPayment(), amount: Number(row.quotedRate) || 0 }
      listPayments(row.matchId).then(r => {
        this.paymentRows = r.data || []
        this.paymentDialog = true
      })
    },
    submitPayment() {
      if (!this.paymentForm.amount || !this.paymentForm.proofUrl) return this.$modal.msgError('请填写付款金额并上传凭证')
      addPayment(this.currentMatch.matchId, this.paymentForm).then(() => {
        this.$modal.msgSuccess('付款凭证已提交')
        this.openPayments(this.currentMatch)
      })
    },
    submitMockPayment() {
      if (!this.paymentForm.amount) return this.$modal.msgError('请填写付款金额')
      mockPayment(this.currentMatch.matchId, { amount: this.paymentForm.amount, remark: this.paymentForm.remark }).then(() => {
        this.$modal.msgSuccess('模拟支付成功')
        this.openPayments(this.currentMatch)
      })
    },
    handlePaymentRow(row, status) {
      const action = status === '1' ? '确认' : '驳回'
      this.$prompt(`请输入${action}意见`, `付款${action}`, { inputValidator: value => !!value || '请输入处理意见' })
        .then(({ value }) => handlePayment(row.paymentId, { status, handleRemark: value }))
        .then(() => { this.$modal.msgSuccess(`付款已${action}`); this.loadAll() })
        .catch(() => {})
    },
    refundPaymentRow(row) {
      this.$prompt('请输入退款金额', '付款退款', {
        inputValue: row.amount,
        inputValidator: value => Number(value) > 0 && Number(value) <= Number(row.amount) || '退款金额不正确'
      })
        .then(({ value }) => refundPayment(row.paymentId, { refundAmount: Number(value), handleRemark: '退款' }))
        .then(() => { this.$modal.msgSuccess('退款已记录'); this.loadAll() })
        .catch(() => {})
    },
    reconcilePaymentRow(row) {
      this.$prompt('请输入交易号或票据备注', '付款对账', { inputValue: row.tradeNo || row.receiptNo || '' })
        .then(({ value }) => reconcilePayment(row.paymentId, {
          tradeNo: value || row.tradeNo,
          invoiceNo: row.invoiceNo,
          receiptNo: row.receiptNo,
          handleRemark: '对账完成'
        }))
        .then(() => { this.$modal.msgSuccess('对账已完成'); this.loadAll() })
        .catch(() => {})
    },
    issueInvoiceRow(row) {
      this.$prompt('请输入发票号', '付款开票', { inputValue: row.invoiceNo || `INV-${row.paymentId}` })
        .then(({ value }) => issueInvoice(row.paymentId, { invoiceNo: value }))
        .then(() => { this.$modal.msgSuccess('发票号已记录'); this.loadAll() })
        .catch(() => {})
    },
    openHomeworks(row) {
      this.currentMatch = row
      this.homeworkForm = emptyHomework()
      listHomeworks(row.matchId).then(r => {
        this.homeworkRows = r.data || []
        this.homeworkDialog = true
      })
    },
    submitHomeworkForm() {
      if (!this.homeworkForm.title || !this.homeworkForm.content) return this.$modal.msgError('请填写作业标题和内容')
      addHomework(this.currentMatch.matchId, this.homeworkForm).then(() => {
        this.$modal.msgSuccess('作业已布置')
        this.openHomeworks(this.currentMatch)
      })
    },
    submitHomeworkRow(row) {
      this.$prompt('请输入作业提交内容', '提交作业', { inputValidator: value => !!value || '请填写提交内容' })
        .then(({ value }) => submitHomework(row.homeworkId, { submitText: value }))
        .then(() => { this.$modal.msgSuccess('作业已提交'); this.openHomeworks(this.currentMatch) })
        .catch(() => {})
    },
    feedbackHomeworkRow(row) {
      this.$prompt('请输入作业反馈', '反馈作业', { inputValidator: value => !!value || '请填写反馈内容' })
        .then(({ value }) => feedbackHomework(row.homeworkId, { feedback: value }))
        .then(() => { this.$modal.msgSuccess('作业反馈已保存'); this.openHomeworks(this.currentMatch) })
        .catch(() => {})
    },
    submitBlacklist() {
      this.$prompt('请输入用户ID', '加入黑名单', { inputValidator: value => Number(value) > 0 || '请输入用户ID' })
        .then(({ value }) => this.$prompt('请输入限制原因', '加入黑名单', { inputValidator: reason => !!reason || '请输入原因' })
          .then(({ value: reason }) => saveBlacklist({ userId: Number(value), reason })))
        .then(() => { this.$modal.msgSuccess('黑名单已生效'); this.loadCurrentTab() })
        .catch(() => {})
    },
    disableBlacklistRow(row) {
      this.$modal.confirm('确认停用该黑名单记录吗？')
        .then(() => disableBlacklist(row.blacklistId))
        .then(() => { this.$modal.msgSuccess('黑名单已停用'); this.loadCurrentTab() })
        .catch(() => {})
    },
    submitFollowup() {
      if (!this.followupForm.matchId || !this.followupForm.content) return this.$modal.msgError('请填写订单ID和回访内容')
      addFollowup(this.followupForm.matchId, this.followupForm).then(() => {
        this.$modal.msgSuccess('回访记录已添加')
        this.followupForm = emptyFollowup()
        this.loadCurrentTab()
      })
    },
    completeFollowupRow(row) {
      completeFollowup(row.followupId).then(() => {
        this.$modal.msgSuccess('回访已完成')
        this.loadCurrentTab()
        this.loadAdminTodos()
      })
    },
    generateReminderRows() {
      generateReminders().then(r => {
        this.reminderResult = r.data || {}
        this.$modal.msgSuccess('提醒已生成')
      })
    },
    openLearner(row) {
      this.learnerForm = row ? { ...row } : emptyLearner()
      this.learnerDialog = true
    },
    submitLearner() {
      if (!this.learnerForm.learnerName || !this.learnerForm.grade) return this.$modal.msgError('请填写学员姓名和年级')
      saveLearner(this.learnerForm).then(() => {
        this.$modal.msgSuccess('学员档案已保存')
        this.learnerDialog = false
        this.loadCurrentTab()
      })
    },
    removeLearner(row) {
      this.$modal.confirm('确认删除该学员档案吗？').then(() => deleteLearner(row.learnerId)).then(() => {
        this.$modal.msgSuccess('学员档案已删除')
        this.loadCurrentTab()
      }).catch(() => {})
    },
    addAvailabilityRow() {
      if (!this.availabilityForm.weekDay || !this.availabilityForm.startTime || !this.availabilityForm.endTime) return this.$modal.msgError('请填写星期和时间段')
      addAvailability(this.availabilityForm).then(() => {
        this.$modal.msgSuccess('授课时间已添加')
        this.availabilityForm = emptyAvailability()
        this.loadCurrentTab()
      })
    },
    removeAvailabilityRow(row) {
      deleteAvailability(row.availabilityId).then(() => {
        this.$modal.msgSuccess('授课时间已删除')
        this.loadCurrentTab()
      })
    },
    openTrial(row) {
      this.trialForm = { matchId: row.matchId, trialTime: row.trialTime || '', trialRemark: row.trialRemark || '' }
      this.trialDialog = true
    },
    submitTrial() {
      if (!this.trialForm.trialTime) return this.$modal.msgError('请选择试听课时间')
      scheduleTrial(this.trialForm.matchId, this.trialForm).then(() => {
        this.$modal.msgSuccess('试听课已安排')
        this.trialDialog = false
        this.loadAll()
      })
    },
    completeTrialRow(row) {
      this.$modal.confirm('确认试听课已完成吗？').then(() => completeTrial(row.matchId)).then(() => {
        this.$modal.msgSuccess('试听课已完成')
        this.loadAll()
      }).catch(() => {})
    },
    settleSettlementRow(row) {
      this.$modal.confirm('确认将该课时费标记为已结算吗？').then(() => settleSettlement(row.settlementId)).then(() => {
        this.$modal.msgSuccess('结算状态已更新')
        this.loadAll()
      }).catch(() => {})
    },
    batchSettleSelected() {
      const ids = this.selectedSettlements.filter(row => row.status === '0').map(row => row.settlementId)
      if (!ids.length) return this.$modal.msgError('请选择待结算记录')
      this.$modal.confirm(`确认批量结算 ${ids.length} 条记录吗？`)
        .then(() => batchSettleSettlements(ids))
        .then(() => { this.$modal.msgSuccess('批量结算已完成'); this.loadAll() })
        .catch(() => {})
    },
    openTicket() {
      this.ticketForm = emptyTicket()
      this.ticketDialog = true
    },
    submitTicketForm() {
      if (!this.ticketForm.title || !this.ticketForm.content) return this.$modal.msgError('请填写工单标题和问题描述')
      submitTicket(this.ticketForm).then(() => {
        this.$modal.msgSuccess('工单已提交')
        this.ticketDialog = false
        this.loadCurrentTab()
      })
    },
    handleTicketRow(row, status) {
      const action = status === '1' ? '解决' : '关闭'
      this.$prompt(`请输入${action}意见`, `工单${action}`, { inputValidator: value => !!value || '请输入处理意见' })
        .then(({ value }) => handleTicket(row.ticketId, { status, handleRemark: value }))
        .then(() => { this.$modal.msgSuccess(`工单已${action}`); this.loadAll() })
        .catch(() => {})
    },
    openProfile() {
      this.loadProfile(true)
    },
    submitProfile() {
      this.$refs.profileForm.validate(valid => {
        if (!valid) return
        saveMyProfile(this.profileForm).then(() => {
          this.$modal.msgSuccess('资料已提交审核')
          this.profileDialog = false
          this.loadCurrentTab()
        })
      })
    },
    submitRequest() {
      this.$refs.requestForm.validate(valid => {
        if (!valid) return
        publishRequest(this.requestForm).then(() => {
          this.$modal.msgSuccess('需求发布成功')
          this.requestDialog = false
          this.requestForm = emptyRequest()
          this.activeTab = 'requests'
          this.loadCurrentTab()
        })
      })
    },
    cancelOwnRequest(row) {
      this.$modal.confirm('确认取消该家教需求吗？相关申请也会一并取消。').then(() => cancelRequest(row.requestId)).then(() => {
        this.$modal.msgSuccess('需求已取消')
        this.loadAll()
      }).catch(() => {})
    },
    copyOwnRequest(row) {
      this.$modal.confirm('确认复制并重新发布该需求吗？').then(() => copyRequest(row.requestId)).then(() => {
        this.$modal.msgSuccess('需求已复制')
        this.loadAll()
      }).catch(() => {})
    },
    openApply(row) {
      this.applyForm = { requestId: row.requestId, quotedRate: row.hourlyBudget, applicationText: '' }
      this.applyDialog = true
    },
    submitApply() {
      this.$refs.applyForm.validate(valid => {
        if (!valid) return
        applyRequest(this.applyForm.requestId, this.applyForm).then(() => {
          this.$modal.msgSuccess('申请已提交')
          this.applyDialog = false
          this.loadAll()
        })
      })
    },
    withdraw(row) {
      this.$modal.confirm('确认撤回该申请吗？').then(() => withdrawMatch(row.matchId)).then(() => {
        this.$modal.msgSuccess('申请已撤回')
        this.loadAll()
      }).catch(() => {})
    },
    cancelAcceptedMatch(row) {
      this.$prompt('请输入取消原因', '取消订单', { inputValidator: value => !!value || '请填写取消原因' })
        .then(({ value }) => cancelMatch(row.matchId, { cancelReason: value }))
        .then(() => { this.$modal.msgSuccess('订单已取消'); this.loadAll() })
        .catch(() => {})
    },
    rescheduleAcceptedMatch(row) {
      this.$prompt('请输入调整后的上课时间', '订单改期', { inputValue: row.rescheduleText || '', inputValidator: value => !!value || '请填写新时间' })
        .then(({ value }) => rescheduleMatch(row.matchId, { rescheduleText: value }))
        .then(() => { this.$modal.msgSuccess('时间已调整'); this.loadAll() })
        .catch(() => {})
    },
    viewTutor(userId) {
      getTutorProfile(userId).then(r => {
        this.tutorDetail = r.data || {}
        this.tutorDialog = true
      })
    },
    openLessons(row) {
      this.currentMatch = row
      this.lessonForm = { lessonDate: '', startTime: '18:00', endTime: '20:00', hours: 1, content: '', studentPerformance: '', homework: '', nextPlan: '', attendanceStatus: '1', phaseFeedback: '' }
      listLessons(row.matchId).then(r => {
        this.lessons = r.data || []
        this.lessonDialog = true
      })
    },
    submitLesson() {
      if (!this.lessonForm.lessonDate || !this.lessonForm.startTime || !this.lessonForm.endTime || !this.lessonForm.content) return this.$modal.msgError('请填写上课日期、时间和授课内容')
      addLesson(this.currentMatch.matchId, this.lessonForm).then(() => {
        this.$modal.msgSuccess('上课记录已添加')
        this.openLessons(this.currentMatch)
      })
    },
    confirmLessonRow(row) {
      confirmLesson(this.currentMatch.matchId, row.lessonId).then(() => {
        this.$modal.msgSuccess('课时已确认')
        this.openLessons(this.currentMatch)
      })
    },
    markRead(row) {
      readNotification(row.notificationId).then(() => {
        row.readStatus = '1'
        this.loadNotifications()
      })
    },
    markAllRead() {
      readAllNotifications().then(() => {
        this.$modal.msgSuccess('已全部标记为已读')
        this.loadNotifications()
      })
    },
    openComplaint(row) {
      this.complaintForm = { matchId: row.matchId, reason: '' }
      this.complaintDialog = true
    },
    submitComplaintForm() {
      if (!this.complaintForm.reason.trim()) return this.$modal.msgError('请填写投诉原因')
      submitComplaint(this.complaintForm.matchId, { reason: this.complaintForm.reason }).then(() => {
        this.$modal.msgSuccess('投诉已提交')
        this.complaintDialog = false
        this.loadAll()
      })
    },
    handleComplaintRow(row, status) {
      const action = status === '1' ? '解决' : '驳回'
      this.$prompt(`请输入${action}意见`, `投诉${action}`, { inputValidator: value => !!value || '请输入处理意见' })
        .then(({ value }) => handleComplaint(row.complaintId, { status, handleRemark: value }))
        .then(() => { this.$modal.msgSuccess(`投诉已${action}`); this.loadAll() })
        .catch(() => {})
    },
    addFavorite(tutor) {
      favoriteTutorApi(tutor.userId).then(() => {
        this.$modal.msgSuccess('已收藏教员')
        this.loadAll()
      })
    },
    removeFavorite(tutor) {
      unfavoriteTutor(tutor.userId).then(() => {
        this.$modal.msgSuccess('已取消收藏')
        this.loadAll()
      })
    },
    openInvite(tutor) {
      this.inviteForm = { ...emptyInvite(), tutorId: tutor.userId, offeredRate: tutor.hourlyRate || 80 }
      this.inviteDialog = true
    },
    submitInvite() {
      this.$refs.inviteForm.validate(valid => {
        if (!valid) return
        inviteTutor(this.inviteForm.tutorId, this.inviteForm).then(() => {
          this.$modal.msgSuccess('预约已发送')
          this.inviteDialog = false
          this.tutorDialog = false
          this.loadAll()
        })
      })
    },
    respondInvite(row, accepted) {
      const request = accepted ? acceptInvitation : rejectInvitation
      this.$modal.confirm(`确认${accepted ? '接受' : '拒绝'}该预约吗？`).then(() => request(row.invitationId)).then(() => {
        this.$modal.msgSuccess(`预约已${accepted ? '接受' : '拒绝'}`)
        this.loadAll()
      }).catch(() => {})
    },
    accept(row) {
      this.$modal.confirm('确认接受该教员申请吗？').then(() => acceptMatch(row.matchId)).then(() => {
        this.$modal.msgSuccess('已接受申请')
        this.loadAll()
      }).catch(() => {})
    },
    complete(row) {
      this.$modal.confirm('确认本次家教服务已完成吗？').then(() => completeMatch(row.matchId)).then(() => {
        this.$modal.msgSuccess('订单已完成')
        this.loadAll()
      }).catch(() => {})
    },
    openReview(row) {
      this.reviewForm = { matchId: row.matchId, rating: 5, reviewText: '' }
      this.reviewDialog = true
    },
    submitReview() {
      if (!this.reviewForm.rating) return this.$modal.msgError('请选择评分')
      reviewMatch(this.reviewForm.matchId, this.reviewForm).then(() => {
        this.$modal.msgSuccess('评价成功')
        this.reviewDialog = false
        this.loadAll()
      })
    },
    verify(row, status) {
      const action = status === '1' ? '通过' : '驳回'
      this.$prompt(`请输入${action}意见`, `审核${action}`, { inputValue: action === '1' ? '审核通过' : '' })
        .then(({ value }) => verifyProfile(row.profileId, { verifyStatus: status, verifyRemark: value || action }))
        .then(() => { this.$modal.msgSuccess(`已${action}`); this.loadAll() })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.tutoring-page { min-height: calc(100vh - 84px); padding: 16px; background: #f5f7fb; }
.text-danger { color: #f56c6c; }
.table-toolbar { margin-bottom: 10px; }
.workbench-shell { max-width: 1440px; margin: 0 auto; }
.workbench-hero { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 12px; padding: 18px 20px; border: 1px solid #e4e9f2; border-radius: 8px; background: #fff; box-shadow: 0 6px 18px rgba(31, 45, 61, 0.05); }
.hero-copy h2 { margin: 8px 0 6px; color: #1f2d3d; font-size: 22px; line-height: 1.3; font-weight: 600; }
.hero-copy p { margin: 0; color: #606b7b; line-height: 1.6; }
.role-chip { display: inline-flex; align-items: center; gap: 6px; color: #2f6fdd; font-size: 13px; font-weight: 600; }
.hero-actions { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 8px; }
.hero-actions .el-button + .el-button { margin-left: 0; }
.workbench-account { display: inline-flex; }
.account-button { display: inline-flex; align-items: center; gap: 8px; height: 32px; padding: 0 10px; border: 1px solid #dcdfe6; border-radius: 4px; color: #303133; background: #fff; cursor: pointer; }
.account-button:hover { border-color: #c6d8f6; color: #2f6fdd; background: #f7fbff; }
.account-button img { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
.account-button span { max-width: 96px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 600; }
.metric-row { margin-bottom: 12px; }
.metric-card { display: flex; align-items: center; gap: 12px; min-height: 92px; margin-bottom: 12px; padding: 14px; border: 1px solid #e4e9f2; border-radius: 8px; background: #fff; }
.metric-card > i { flex: 0 0 36px; height: 36px; border-radius: 8px; color: #2f6fdd; background: #edf4ff; font-size: 18px; line-height: 36px; text-align: center; }
.metric-card.is-warning > i { color: #b56a00; background: #fff4df; }
.metric-value { color: #1f2d3d; font-size: 24px; line-height: 1.2; font-weight: 700; }
.metric-label { margin-top: 2px; color: #303133; font-size: 13px; font-weight: 600; }
.metric-note { margin-top: 4px; color: #8a96a8; font-size: 12px; line-height: 1.3; }
.module-switcher { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; padding: 10px; border: 1px solid #e4e9f2; border-radius: 8px; background: #fff; }
.module-tab { display: inline-flex; align-items: center; gap: 6px; height: 34px; padding: 0 12px; border: 1px solid transparent; border-radius: 6px; color: #4d5b6c; background: transparent; font-size: 14px; line-height: 34px; cursor: pointer; }
.module-tab:hover { color: #2f6fdd; background: #f4f8ff; }
.module-tab.active { color: #2f6fdd; border-color: #cfe0ff; background: #edf4ff; font-weight: 600; }
.module-tab em { min-width: 18px; height: 18px; padding: 0 5px; border-radius: 9px; color: #fff; background: #f56c6c; font-style: normal; font-size: 12px; line-height: 18px; text-align: center; }
.workbench-content { padding: 14px; border: 1px solid #e4e9f2; border-radius: 8px; background: #fff; }
.content-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #eef1f6; }
.content-header h3 { margin: 0; color: #1f2d3d; font-size: 18px; line-height: 1.4; font-weight: 600; }
.content-header p { margin: 4px 0 0; color: #7a8698; line-height: 1.5; }
.workbench-tabs >>> .el-tabs__header { display: none; }
.workbench-tabs >>> .el-tabs__content { padding: 0; overflow: visible; }
.filter-form { margin-bottom: 12px; padding: 12px 12px 0; border: 1px solid #eef1f6; border-radius: 6px; background: #fafbfc; }
.budget-input { width: 110px; }
.budget-separator { margin: 0 4px; color: #909399; }
.stat-card { margin-bottom: 16px; padding: 18px 12px; text-align: center; border: 1px solid #e4e9f2; border-radius: 8px; background: #fff; }
.stat-value { color: #2f6fdd; font-size: 28px; font-weight: 600; }
.stat-label { margin-top: 8px; color: #606266; }
@media (max-width: 992px) {
  .module-switcher { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .module-tab { justify-content: center; }
}
@media (max-width: 640px) {
  .tutoring-page { padding: 10px; }
  .workbench-content { overflow-x: auto; }
  .filter-form >>> .el-form-item { display: block; margin-right: 0; }
  .filter-form >>> .el-input,
  .filter-form >>> .el-select,
  .filter-form >>> .el-date-editor { width: 100%; }
  .workbench-tabs >>> .el-table { min-width: 760px; }
  .workbench-hero, .content-header { display: block; }
  .hero-actions { justify-content: flex-start; margin-top: 12px; }
  .module-switcher { grid-template-columns: 1fr; }
  .metric-card { min-height: 86px; padding: 12px; }
  .metric-value { font-size: 20px; }
}
</style>
