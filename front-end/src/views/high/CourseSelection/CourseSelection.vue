<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import HighNav from '@/components/HighNav.vue'
import MySelectionPanel from '@/views/high/CourseSelection/MySelectionPanel.vue'
import SelectionCenterPanel from '@/views/high/CourseSelection/SelectionCenterPanel.vue'
import MajorRecommendPanel from '@/views/high/CourseSelection/MajorRecommendPanel.vue'
import StatisticsPanel from '@/views/high/CourseSelection/StatisticsPanel.vue'
import HistoryPanel from '@/views/high/CourseSelection/HistoryPanel.vue'
import ApprovalPanel from '@/views/high/CourseSelection/ApprovalPanel.vue'
import EditSelectionForm from '@/views/high/CourseSelection/EditSelectionForm.vue'
import CourseSelectionIntentionPanel from '@/views/high/CourseSelection/CourseSelectionIntentionPanel.vue'
import CourseGuidancePanel from '@/views/high/CourseSelection/CourseGuidancePanel.vue'
import GradingScalePanel from '@/views/high/CourseSelection/GradingScalePanel.vue'
import MajorAdminPanel from '@/views/high/CourseSelection/MajorAdminPanel.vue'

const props = defineProps({
  isDark: {
    type: Boolean,
    default: false
  }
})

const activeTab = ref('mySelection')
const editDialogVisible = ref(false)
const editSelectionData = ref(null)
const currentStudentId = ref('') // 从登录用户获取（雪花ID用字符串）
const User = ref({}) // 从登录用户获取
const isAdmin = ref(false) // 从用户权限获取

const statistics = ref({
  totalStudents: 0,
  confirmedCount: 0,
  combinationCount: 12,
  hotCombination: '物化生'
})

//获取用户信息
const getUserInfo=()=>{
  return request.get('/user/getUserById',{},(message,data)=>{
    User.value=data || {}
    currentStudentId.value = data?.id != null ? String(data.id) : ''
  })
}

// 获取我的选课
const fetchMySelection = async () => {
  try {
    const res = await request.get(`/selection/student/${currentStudentId.value}`)
    if (res.code === 200) {
      // 更新数据
    }
  } catch (error) {
    console.error('获取选课失败', error)
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await request.get('/selection/statistics/grade', {
      grade: '高一',
      academicYear: '2024-2025'
    })
    if (res.code === 200) {
      statistics.value.totalStudents = res.data.totalCount || 0
      statistics.value.confirmedCount = res.data.totalCount || 0
    }

    // 获取热门组合
    const hotRes = await request.get('/selection/hot-combinations')
    if (hotRes.code === 200 && hotRes.data.length > 0) {
      statistics.value.hotCombination = hotRes.data[0].name || hotRes.data[0].combination_name
    }
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const handleSelectionSuccess = () => {
  activeTab.value = 'mySelection'
  fetchMySelection()
  fetchStatistics()
  ElMessage.success('选课成功')
}

const openEditDialog = (selection) => {
  editSelectionData.value = selection
  editDialogVisible.value = true
}

const handleEditSuccess = () => {
  editDialogVisible.value = false
  fetchMySelection()
  fetchStatistics()
  ElMessage.success('修改成功')
}

// 监听 activeTab 变化
watch(activeTab, (newValue, oldValue) => {
  console.log('activeTab changed:', oldValue, '->', newValue)
})

onMounted(async () => {
  await getUserInfo()
  if (currentStudentId.value) {
    fetchMySelection()
  }
  fetchStatistics()
})
</script>
<template>
  <div class="min-h-screen relative overflow-x-hidden" :class="isDark ? 'dark' : ''">
    <!-- 动态渐变背景 -->
    <div class="fixed inset-0 bg-gradient-to-br from-slate-50 via-white to-indigo-50/30 dark:from-slate-950 dark:via-slate-900 dark:to-indigo-950/40 -z-10"></div>
    
    <!-- 共用导航栏 -->
    <HighNav :isDark="isDark" />

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-28">
      <!-- 头部 -->
      <div class="mb-8">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/40 dark:bg-slate-800/40 backdrop-blur-sm mb-4">
          <span class="relative flex h-2 w-2">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span class="text-xs font-medium text-slate-600 dark:text-slate-300">3+1+2新高考选课系统</span>
        </div>
        <h1 class="text-4xl md:text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          新高考选课系统
        </h1>
        <p class="text-slate-500 dark:text-slate-400 mt-2">物理/历史二选一 · 化学/生物/政治/地理四选二</p>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <div class="rounded-xl p-4 bg-white/50 dark:bg-white/10 backdrop-blur-sm border border-white/20">
          <div class="text-2xl mb-1">📊</div>
          <div class="text-2xl font-bold">{{ statistics.totalStudents || 0 }}</div>
          <div class="text-xs text-slate-500">总选课人数</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-white/10 backdrop-blur-sm border border-white/20">
          <div class="text-2xl mb-1">🏆</div>
          <div class="text-2xl font-bold">{{ statistics.confirmedCount || 0 }}</div>
          <div class="text-xs text-slate-500">已确认人数</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-white/10 backdrop-blur-sm border border-white/20">
          <div class="text-2xl mb-1">📚</div>
          <div class="text-2xl font-bold">{{ statistics.combinationCount || 0 }}</div>
          <div class="text-xs text-slate-500">可选组合</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-white/10 backdrop-blur-sm border border-white/20">
          <div class="text-2xl mb-1">🎯</div>
          <div class="text-2xl font-bold">{{ statistics.hotCombination || '物化生' }}</div>
          <div class="text-xs text-slate-500">最热门组合</div>
        </div>
      </div>

      <!-- 左侧菜单 + 右侧内容 -->
      <div class="mb-4">
        <span class="text-sm text-slate-500">当前选中: {{ activeTab }}</span>
      </div>
      <el-container class="selection-container">
        <!-- 左侧菜单 -->
        <el-aside width="280px" class="selection-sidebar">
          <el-menu
            v-model="activeTab"
            class="selection-menu"
            :background-color="isDark ? '#1e293b' : '#ffffff'"
            :text-color="isDark ? '#94a3b8' : '#334155'"
            :active-text-color="isDark ? '#818cf8' : '#6366f1'"
            :unique-opened="true"
            mode="vertical"
          >
            <!-- 我的选课 -->
            <el-menu-item index="mySelection" class="menu-item">
              <span class="menu-icon">📝</span>
              <span>我的选课</span>
            </el-menu-item>
            
            <!-- 选课中心 -->
            <el-menu-item index="selectionCenter" class="menu-item">
              <span class="menu-icon">🎯</span>
              <span>选课中心</span>
            </el-menu-item>
            
            <!-- 专业推荐 -->
            <el-menu-item index="majorRecommend" class="menu-item">
              <span class="menu-icon">🎓</span>
              <span>专业推荐</span>
            </el-menu-item>
            
            <!-- 选课意向 -->
            <el-menu-item index="intention" class="menu-item">
              <span class="menu-icon">🧭</span>
              <span>选课意向</span>
            </el-menu-item>
            
            <!-- 选科指导 -->
            <el-menu-item index="guidance" class="menu-item">
              <span class="menu-icon">👨‍🏫</span>
              <span>选科指导</span>
            </el-menu-item>
            
            <!-- 统计分析 -->
            <el-menu-item index="statistics" class="menu-item">
              <span class="menu-icon">📈</span>
              <span>统计分析</span>
            </el-menu-item>
            
            <!-- 历史记录 -->
            <el-menu-item index="history" class="menu-item">
              <span class="menu-icon">📜</span>
              <span>历史记录</span>
            </el-menu-item>
            
            <!-- 审批管理（管理员） -->
            <el-menu-item v-if="isAdmin" index="approval" class="menu-item">
              <span class="menu-icon">✅</span>
              <span>审批管理</span>
            </el-menu-item>
            
            <!-- 赋分规则 -->
            <el-menu-item index="grading" class="menu-item">
              <span class="menu-icon">🧮</span>
              <span>赋分规则</span>
            </el-menu-item>
            
            <!-- 专业库管理 -->
            <el-menu-item index="majorAdmin" class="menu-item">
              <span class="menu-icon">🧩</span>
              <span>专业库管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 右侧内容 -->
        <el-main class="selection-content">
          <!-- 我的选课 -->
          <MySelectionPanel
            v-show="activeTab === 'mySelection'"
            :isDark="isDark"
            :studentId="currentStudentId"
            @refresh="fetchMySelection"
            @edit="openEditDialog"
          />
          
          <!-- 选课中心 -->
          <SelectionCenterPanel
            v-show="activeTab === 'selectionCenter'"
            :isDark="isDark"
            :studentId="currentStudentId"
            @success="handleSelectionSuccess"
          />
          
          <!-- 专业推荐 -->
          <MajorRecommendPanel
            v-show="activeTab === 'majorRecommend'"
            :isDark="isDark"
            :studentId="currentStudentId"
          />
          
          <!-- 选课意向 -->
          <CourseSelectionIntentionPanel
            v-show="activeTab === 'intention'"
            :isDark="isDark"
            :studentId="currentStudentId"
            :studentName="User?.username || User?.nickname || User?.name || ''"
          />
          
          <!-- 选科指导 -->
          <CourseGuidancePanel
            v-show="activeTab === 'guidance'"
            :isDark="isDark"
            :studentId="currentStudentId"
            :studentName="User?.username || User?.nickname || User?.name || ''"
          />
          
          <!-- 统计分析 -->
          <StatisticsPanel
            v-show="activeTab === 'statistics'"
            :isDark="isDark"
          />
          
          <!-- 历史记录 -->
          <HistoryPanel
            v-show="activeTab === 'history'"
            :isDark="isDark"
            :studentId="currentStudentId"
          />
          
          <!-- 审批管理（管理员） -->
          <ApprovalPanel
            v-show="activeTab === 'approval' && isAdmin"
            :isDark="isDark"
          />
          
          <!-- 赋分规则 -->
          <GradingScalePanel
            v-show="activeTab === 'grading'"
          />
          
          <!-- 专业库管理 -->
          <MajorAdminPanel
            v-show="activeTab === 'majorAdmin'"
          />
        </el-main>
      </el-container>
    </div>

    <!-- 编辑选课弹窗 -->
    <el-dialog v-model="editDialogVisible" title="修改选课" width="700px" :class="{ 'dark-dialog': isDark }">
      <EditSelectionForm
        v-if="editSelectionData"
        :selectionData="editSelectionData"
        :isDark="isDark"
        @success="handleEditSuccess"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<style scoped>
.selection-container {
  min-height: 600px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
}

.dark .selection-container {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.selection-sidebar {
  background: transparent;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  padding: 0;
}

.dark .selection-sidebar {
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.selection-menu {
  border: none !important;
  height: 100%;
  padding: 20px 0;
}

.menu-item {
  height: 60px;
  line-height: 60px;
  margin: 0 15px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.menu-item:hover {
  background: rgba(99, 102, 241, 0.1) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.15)) !important;
  border-left: 4px solid #6366f1;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.selection-content {
  padding: 30px;
  background: transparent;
  overflow-y: auto;
}

:deep(.el-dialog) {
  border-radius: 24px !important;
}

.dark :deep(.el-dialog) {
  background: rgba(15, 23, 42, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .selection-sidebar {
    width: 240px !important;
  }
  
  .selection-content {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .selection-container {
    flex-direction: column;
  }
  
  .selection-sidebar {
    width: 100% !important;
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
  
  .dark .selection-sidebar {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .selection-menu {
    display: flex;
    overflow-x: auto;
    padding: 10px 0;
  }
  
  .menu-item {
    white-space: nowrap;
    margin: 0 10px;
    height: 50px;
    line-height: 50px;
  }
}
</style>
