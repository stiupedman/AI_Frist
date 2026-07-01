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
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="has('tutoring:match:list')" label="我的订单" name="matches">
        <el-table :data="matches">
          <el-table-column label="科目" prop="subject" width="90" />
          <el-table-column label="教员" prop="tutorName" width="100" />
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
          <el-table-column label="操作" width="180" align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.status === '0'" size="mini" type="text"
                v-hasPermi="['tutoring:match:accept']" @click="accept(scope.row)">接受</el-button>
              <el-button v-if="scope.row.status === '1'" size="mini" type="text"
                v-hasPermi="['tutoring:match:complete']" @click="complete(scope.row)">完成授课</el-button>
              <el-button v-if="scope.row.status === '2' && !scope.row.rating" size="mini" type="text"
                v-hasPermi="['tutoring:match:review']" @click="openReview(scope.row)">评价</el-button>
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
          <el-table-column label="操作" width="130" align="center">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="verify(scope.row, '1')">通过</el-button>
              <el-button size="mini" type="text" class="text-danger" @click="verify(scope.row, '2')">驳回</el-button>
            </template>
          </el-table-column>
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
  </div>
</template>

<script>
import {
  getMyProfile, saveMyProfile, listPendingProfiles, verifyProfile,
  listOpenRequests, listMyRequests, publishRequest, listMyMatches,
  applyRequest, acceptMatch, completeMatch, reviewMatch
} from '@/api/tutoring'

const emptyProfile = () => ({ university: '', major: '', collegeYear: '', subjects: '', hourlyRate: 60, introduction: '' })
const emptyRequest = () => ({ learnerGrade: '', subject: '', area: '', scheduleText: '', hourlyBudget: 80, requirementText: '' })

export default {
  name: 'TutoringWorkbench',
  data() {
    return {
      activeTab: 'open', loading: false,
      openRequests: [], myRequests: [], matches: [], pendingProfiles: [],
      profileDialog: false, requestDialog: false, applyDialog: false, reviewDialog: false,
      profileForm: emptyProfile(), requestForm: emptyRequest(),
      applyForm: { requestId: null, quotedRate: 60, applicationText: '' },
      reviewForm: { matchId: null, rating: 5, reviewText: '' },
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
      if (this.has('tutoring:request:list')) jobs.push(listOpenRequests().then(r => { this.openRequests = r.rows }))
      if (this.has('tutoring:request:add')) jobs.push(listMyRequests().then(r => { this.myRequests = r.rows }))
      if (this.has('tutoring:match:list')) jobs.push(listMyMatches().then(r => { this.matches = r.rows }))
      if (this.has('tutoring:profile:verify')) jobs.push(listPendingProfiles().then(r => { this.pendingProfiles = r.rows }))
      Promise.all(jobs).finally(() => { this.loading = false })
    },
    requestStatus(status) { return { '0': '招募中', '1': '已匹配', '2': '已完成', '3': '已取消' }[status] || status },
    matchStatus(status) { return { '0': '申请中', '1': '已接单', '2': '已完成', '3': '未选中', '4': '已取消' }[status] || status },
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
</style>
