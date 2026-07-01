<template>
  <div class="home">
    <el-card shadow="never" class="welcome-card">
      <div class="title">大学生家教管理系统</div>
      <p class="description">
        为大学生教员、家长学员和平台管理员提供资料审核、需求发布、在线申请、订单跟踪与评价服务。
      </p>
      <el-button type="primary" icon="el-icon-s-platform" @click="openWorkbench">
        进入家教工作台
      </el-button>
    </el-card>

    <el-row :gutter="20" class="roles">
      <el-col :xs="24" :sm="8" v-for="role in roles" :key="role.title">
        <el-card shadow="hover" class="role-card">
          <i :class="role.icon" />
          <h3>{{ role.title }}</h3>
          <p>{{ role.description }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Index',
  data() {
    return {
      roles: [
        { title: '大学生教员', icon: 'el-icon-user', description: '维护资料、申请需求、完成订单' },
        { title: '家长学员', icon: 'el-icon-s-custom', description: '发布需求、选择教员、评价服务' },
        { title: '平台管理员', icon: 'el-icon-s-check', description: '审核资料、管理用户与业务数据' }
      ]
    }
  },
  methods: {
    openWorkbench() {
      this.$router.push('/tutoring/workbench')
    }
  }
}
</script>

<style lang="scss" scoped>
.home {
  padding: 24px;
  min-height: calc(100vh - 84px);
  background: #f5f7fa;
}

.welcome-card {
  padding: 24px;
  text-align: center;
}

.title {
  color: #303133;
  font-size: 30px;
  font-weight: 600;
}

.description {
  margin: 18px auto 24px;
  max-width: 720px;
  color: #606266;
  line-height: 1.8;
}

.roles {
  margin-top: 20px;
}

.role-card {
  min-height: 190px;
  margin-bottom: 20px;
  text-align: center;

  i {
    color: #409eff;
    font-size: 42px;
  }

  h3 {
    margin: 18px 0 10px;
    color: #303133;
  }

  p {
    color: #909399;
    line-height: 1.6;
  }
}
</style>
