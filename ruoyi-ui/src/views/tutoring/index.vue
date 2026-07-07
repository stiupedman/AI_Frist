<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini"
          v-hasPermi="['tutoring:request:add']" @click="requestDialog = true">发布家教需求</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-user" size="mini"
          v-hasPermi="['tutoring:profile:edit']" @click="openProfile">维护教员资料</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button icon="el-icon-refresh" size="mini" @click="loadAll">刷新</el-button>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane v-if="has('tutoring:request:list')" label="开放需求" name="open">
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
              <el-button size="mini" type="text" v-hasPermi="['tutoring:match:apply']"
                @click="openApply(scope.row)">申请</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:match:apply') && !has('tutoring:profile:verify')" label="智能推荐" name="recommended">
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
            <template slot-scope="scope"><el-button type="text" @click="openApply(scope.row)">申请</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:request:add')" label="我的需求" name="requests">
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
          <el-table-column label="操作" width="90" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" size="mini" type="text" class="text-danger"
                @click="cancelOwnRequest(scope.row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:match:list')" label="我的订单" name="matches">
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
          <el-table-column label="评价" min-width="140">
            <template slot-scope="scope">
              <span v-if="scope.row.rating">{{ scope.row.rating }}星 {{ scope.row.reviewText }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" size="mini" type="text"
                v-hasPermi="['tutoring:match:accept']" @click="accept(scope.row)">接受</el-button>
              <el-button v-if="scope.row.status === '0'" size="mini" type="text" class="text-danger"
                v-hasPermi="['tutoring:match:apply']" @click="withdraw(scope.row)">撤回</el-button>
              <el-button v-if="scope.row.status === '1'" size="mini" type="text"
                v-hasPermi="['tutoring:match:complete']" @click="complete(scope.row)">完成授课</el-button>
              <el-button v-if="scope.row.status === '2' && !scope.row.rating" size="mini" type="text"
                v-hasPermi="['tutoring:match:review']" @click="openReview(scope.row)">评价</el-button>
              <el-button v-if="scope.row.status === '1' || scope.row.status === '2'" size="mini" type="text"
                @click="openLessons(scope.row)">课程记录</el-button>
              <el-button v-if="scope.row.status === '1' || scope.row.status === '2'" size="mini" type="text" class="text-danger"
                @click="openComplaint(scope.row)">投诉</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:match:list')" label="消息通知" name="notifications">
        <el-table :data="notifications">
          <el-table-column label="状态" width="80">
            <template slot-scope="scope"><el-tag :type="scope.row.readStatus === '1' ? 'info' : 'danger'" size="mini">{{ scope.row.readStatus === '1' ? '已读' : '未读' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="标题" prop="title" width="180" />
          <el-table-column label="内容" prop="content" />
          <el-table-column label="时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="80">
            <template slot-scope="scope"><el-button v-if="scope.row.readStatus === '0'" type="text" @click="markRead(scope.row)">标为已读</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:match:list')" :label="has('tutoring:profile:verify') ? '投诉处理' : '我的投诉'" name="complaints">
        <el-table :data="complaints">
          <el-table-column label="订单" prop="matchId" width="80" />
          <el-table-column label="科目" prop="subject" width="100" />
          <el-table-column v-if="has('tutoring:profile:verify')" label="投诉人" prop="complainantName" width="100" />
          <el-table-column label="投诉原因" prop="reason" />
          <el-table-column label="处理状态" width="90">
            <template slot-scope="scope">{{ complaintStatus(scope.row.status) }}</template>
          </el-table-column>
          <el-table-column label="处理意见" prop="handleRemark" />
          <el-table-column v-if="has('tutoring:profile:verify')" label="操作" width="130">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" type="text" @click="handleComplaintRow(scope.row, '1')">解决</el-button>
              <el-button v-if="scope.row.status === '0'" type="text" class="text-danger" @click="handleComplaintRow(scope.row, '2')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:request:add') && !has('tutoring:profile:verify')" label="收藏教员" name="favorites">
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
              <el-button type="text" @click="viewTutor(scope.row.userId)">查看</el-button>
              <el-button type="text" @click="openInvite(scope.row)">预约</el-button>
              <el-button type="text" class="text-danger" @click="removeFavorite(scope.row)">取消收藏</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:match:list')" label="预约邀请" name="invitations">
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
              <el-button v-if="scope.row.status === '0'" v-hasPermi="['tutoring:match:apply']" type="text" @click="respondInvite(scope.row, true)">接受</el-button>
              <el-button v-if="scope.row.status === '0'" v-hasPermi="['tutoring:match:apply']" type="text" class="text-danger" @click="respondInvite(scope.row, false)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:profile:verify')" label="教员审核" name="verify">
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

      <el-tab-pane v-if="has('tutoring:profile:verify')" label="数据看板" name="dashboard">
        <el-row :gutter="16">
          <el-col v-for="item in dashboardCards" :key="item.label" :xs="12" :sm="8" :lg="4">
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

    <el-dialog title="教员资料" :visible.sync="profileDialog" width="620px" append-to-body>
      <el-form ref="profileForm" :model="profileForm" :rules="profileRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学校" prop="university"><el-input v-model="profileForm.university" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="专业" prop="major"><el-input v-model="profileForm.major" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年级" prop="collegeYear"><el-input v-model="profileForm.collegeYear" placeholder="例如：大三" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="期望课时费" prop="hourlyRate"><el-input-number v-model="profileForm.hourlyRate" :min="1" :precision="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="擅长科目" prop="subjects"><el-input v-model="profileForm.subjects" placeholder="例如：数学,物理" /></el-form-item></el-col>
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
        <el-table-column label="课时" prop="hours" width="80" />
        <el-table-column label="授课内容" prop="content" />
        <el-table-column label="课时费" width="100"><template slot-scope="scope">￥{{ scope.row.amount }}</template></el-table-column>
      </el-table>
      <el-divider v-if="currentMatch.status === '1' && has('tutoring:match:complete')" />
      <el-form v-if="currentMatch.status === '1' && has('tutoring:match:complete')" :model="lessonForm" label-width="90px">
        <el-form-item label="上课日期"><el-date-picker v-model="lessonForm.lessonDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" /></el-form-item>
        <el-form-item label="课时数"><el-input-number v-model="lessonForm.hours" :min="0.5" :step="0.5" :precision="1" /></el-form-item>
        <el-form-item label="授课内容"><el-input v-model="lessonForm.content" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="lessonDialog = false">关闭</el-button>
        <el-button v-if="currentMatch.status === '1' && has('tutoring:match:complete')" type="primary" @click="submitLesson">添加记录</el-button>
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

    <el-dialog title="教员详情" :visible.sync="tutorDialog" width="560px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ tutorDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ tutorDetail.university }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ tutorDetail.major }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ tutorDetail.collegeYear }}</el-descriptions-item>
        <el-descriptions-item label="擅长科目">{{ tutorDetail.subjects }}</el-descriptions-item>
        <el-descriptions-item label="期望课时费">￥{{ tutorDetail.hourlyRate }}/小时</el-descriptions-item>
        <el-descriptions-item label="已完成订单">{{ tutorDetail.completedOrders || 0 }}</el-descriptions-item>
        <el-descriptions-item label="平均评分">{{ tutorDetail.averageRating ? tutorDetail.averageRating + ' 分' : '暂无评价' }}</el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">{{ tutorDetail.introduction || '暂无' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="has('tutoring:request:add') && !has('tutoring:profile:verify')" slot="footer">
        <el-button @click="addFavorite(tutorDetail)">收藏教员</el-button>
        <el-button type="primary" @click="openInvite(tutorDetail)">发起预约</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyProfile, getTutorProfile, saveMyProfile, listPendingProfiles, verifyProfile,
  listOpenRequests, listMyRequests, publishRequest, cancelRequest, listMyMatches,
  applyRequest, withdrawMatch, acceptMatch, completeMatch, reviewMatch, getDashboard,
  listLessons, addLesson, listNotifications, readNotification, submitComplaint,
  listMyComplaints, listComplaints, handleComplaint, listRecommendedRequests,
  listFavoriteTutors, favoriteTutor as favoriteTutorApi, unfavoriteTutor, inviteTutor,
  listInvitations, acceptInvitation, rejectInvitation
} from '@/api/tutoring'

const emptyProfile = () => ({ university: '', major: '', collegeYear: '', subjects: '', hourlyRate: 60, introduction: '', studentCardUrl: '', qualificationUrl: '' })
const emptyRequest = () => ({ learnerGrade: '', subject: '', area: '', scheduleText: '', hourlyBudget: 80, requirementText: '' })
const emptyInvite = () => ({ tutorId: null, learnerGrade: '', subject: '', area: '', scheduleText: '', offeredRate: 80, message: '' })

export default {
  name: 'TutoringWorkbench',
  data() {
    return {
      activeTab: 'open', loading: false,
      openRequests: [], recommendedRequests: [], myRequests: [], matches: [], pendingProfiles: [],
      notifications: [], complaints: [], favoriteTutors: [], invitations: [], lessons: [],
      profileDialog: false, requestDialog: false, applyDialog: false, reviewDialog: false, tutorDialog: false,
      lessonDialog: false, complaintDialog: false, inviteDialog: false,
      queryForm: { subject: '', learnerGrade: '', area: '', minBudget: undefined, maxBudget: undefined },
      tutorDetail: {}, dashboard: { topSubjects: [] },
      currentMatch: {},
      profileForm: emptyProfile(), requestForm: emptyRequest(),
      applyForm: { requestId: null, quotedRate: 60, applicationText: '' },
      reviewForm: { matchId: null, rating: 5, reviewText: '' },
      lessonForm: { lessonDate: '', hours: 1, content: '' },
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
    profileVerifyText() {
      const text = { '0': '资料待审核', '1': '资料已通过审核', '2': '资料被驳回' }[this.profileForm.verifyStatus]
      return `${text || ''}${this.profileForm.verifyRemark ? '：' + this.profileForm.verifyRemark : ''}`
    }
  },
  created() { this.loadAll() },
  methods: {
    has(permission) {
      const permissions = this.$store.getters.permissions || []
      return permissions.includes('*:*:*') || permissions.includes(permission)
    },
    loadAll() {
      this.loading = true
      const jobs = []
      if (this.has('tutoring:request:list')) jobs.push(listOpenRequests(this.queryForm).then(r => { this.openRequests = r.rows }))
      if (this.has('tutoring:match:apply') && !this.has('tutoring:profile:verify')) jobs.push(listRecommendedRequests().then(r => { this.recommendedRequests = r.data || [] }))
      if (this.has('tutoring:request:add')) jobs.push(listMyRequests().then(r => { this.myRequests = r.rows }))
      if (this.has('tutoring:match:list')) jobs.push(listMyMatches().then(r => { this.matches = r.rows }))
      if (this.has('tutoring:match:list')) jobs.push(listNotifications().then(r => { this.notifications = r.data || [] }))
      if (this.has('tutoring:match:list')) jobs.push(listInvitations().then(r => { this.invitations = r.data || [] }))
      if (this.has('tutoring:match:list')) jobs.push((this.has('tutoring:profile:verify') ? listComplaints() : listMyComplaints()).then(r => { this.complaints = r.data || [] }))
      if (this.has('tutoring:request:add') && !this.has('tutoring:profile:verify')) jobs.push(listFavoriteTutors().then(r => { this.favoriteTutors = r.data || [] }))
      if (this.has('tutoring:profile:verify')) jobs.push(listPendingProfiles().then(r => { this.pendingProfiles = r.rows }))
      if (this.has('tutoring:profile:verify')) jobs.push(getDashboard().then(r => { this.dashboard = r.data || { topSubjects: [] } }))
      Promise.all(jobs).finally(() => { this.loading = false })
    },
    requestStatus(status) { return { '0': '招募中', '1': '已匹配', '2': '已完成', '3': '已取消' }[status] || status },
    matchStatus(status) { return { '0': '申请中', '1': '已接单', '2': '已完成', '3': '未选中', '4': '已取消' }[status] || status },
    complaintStatus(status) { return { '0': '待处理', '1': '已解决', '2': '已驳回' }[status] || status },
    invitationStatus(status) { return { '0': '待处理', '1': '已接受', '2': '已拒绝' }[status] || status },
    searchRequests() {
      if (this.queryForm.minBudget != null && this.queryForm.maxBudget != null && this.queryForm.minBudget > this.queryForm.maxBudget) {
        return this.$modal.msgError('最低预算不能高于最高预算')
      }
      this.loading = true
      listOpenRequests(this.queryForm).then(r => { this.openRequests = r.rows }).finally(() => { this.loading = false })
    },
    resetSearch() {
      this.queryForm = { subject: '', learnerGrade: '', area: '', minBudget: undefined, maxBudget: undefined }
      this.searchRequests()
    },
    openProfile() {
      getMyProfile().then(r => {
        this.profileForm = r.data || emptyProfile()
        this.profileDialog = true
      })
    },
    submitProfile() {
      this.$refs.profileForm.validate(valid => {
        if (!valid) return
        saveMyProfile(this.profileForm).then(() => {
          this.$modal.msgSuccess('资料已提交审核')
          this.profileDialog = false
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
          this.loadAll()
        })
      })
    },
    cancelOwnRequest(row) {
      this.$modal.confirm('确认取消该家教需求吗？相关申请也会一并取消。').then(() => cancelRequest(row.requestId)).then(() => {
        this.$modal.msgSuccess('需求已取消')
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
    viewTutor(userId) {
      getTutorProfile(userId).then(r => {
        this.tutorDetail = r.data || {}
        this.tutorDialog = true
      })
    },
    openLessons(row) {
      this.currentMatch = row
      this.lessonForm = { lessonDate: '', hours: 1, content: '' }
      listLessons(row.matchId).then(r => {
        this.lessons = r.data || []
        this.lessonDialog = true
      })
    },
    submitLesson() {
      if (!this.lessonForm.lessonDate || !this.lessonForm.content) return this.$modal.msgError('请填写上课日期和授课内容')
      addLesson(this.currentMatch.matchId, this.lessonForm).then(() => {
        this.$modal.msgSuccess('上课记录已添加')
        this.openLessons(this.currentMatch)
      })
    },
    markRead(row) {
      readNotification(row.notificationId).then(() => {
        row.readStatus = '1'
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
.text-danger { color: #f56c6c; }
.filter-form { margin-bottom: 4px; }
.budget-input { width: 110px; }
.budget-separator { margin: 0 4px; color: #909399; }
.stat-card { margin-bottom: 16px; padding: 20px 12px; text-align: center; border: 1px solid #ebeef5; border-radius: 4px; background: #fff; }
.stat-value { color: #409eff; font-size: 28px; font-weight: 600; }
.stat-label { margin-top: 8px; color: #606266; }
</style>
