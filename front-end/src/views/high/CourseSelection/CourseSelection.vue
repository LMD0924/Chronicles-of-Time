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
const currentStudentId = ref('')
const User = ref({})
const isAdmin = ref(false)

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
    <!-- 纯黑渐变背景 -->
    <div class="fixed inset-0 bg-gradient-to-br from-slate-50 via-white to-indigo-50/30 dark:from-black dark:via-black dark:to-black -z-10"></div>

    <HighNav :isDark="isDark" />

    <div class="max-w-[90rem] mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-28">
      <div class="mb-8">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/40 dark:bg-black/40 backdrop-blur-sm mb-4">
          <span class="relative flex h-2 w-2">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span class="text-xs font-medium text-slate-600 dark:text-white/80">3+1+2新高考选课系统</span>
        </div>
        <h1 class="text-4xl md:text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          新高考选课系统
        </h1>
        <p class="text-slate-500 dark:text-white/60 mt-2">物理/历史二选一 · 化学/生物/政治/地理四选二</p>
      </div>

      <!-- 统计卡片 - 纯黑模式 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black/50 backdrop-blur-sm border border-white/20 dark:border-white/10">
          <div class="text-2xl mb-1">📊</div>
          <div class="text-2xl font-bold text-black dark:text-white">{{ statistics.totalStudents || 0 }}</div>
          <div class="text-xs text-slate-500 dark:text-white/60">总选课人数</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black/50 backdrop-blur-sm border border-white/20 dark:border-white/10">
          <div class="text-2xl mb-1">🏆</div>
          <div class="text-2xl font-bold text-black dark:text-white">{{ statistics.confirmedCount || 0 }}</div>
          <div class="text-xs text-slate-500 dark:text-white/60">已确认人数</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black/50 backdrop-blur-sm border border-white/20 dark:border-white/10">
          <div class="text-2xl mb-1">📚</div>
          <div class="text-2xl font-bold text-black dark:text-white">{{ statistics.combinationCount || 0 }}</div>
          <div class="text-xs text-slate-500 dark:text-white/60">可选组合</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black/50 backdrop-blur-sm border border-white/20 dark:border-white/10">
          <div class="text-2xl mb-1">🎯</div>
          <div class="text-2xl font-bold text-black dark:text-white">{{ statistics.hotCombination || '物化生' }}</div>
          <div class="text-xs text-slate-500 dark:text-white/60">最热门组合</div>
        </div>
      </div>

      <el-container class="selection-container">
        <!-- 左侧纯原生菜单 -->
        <el-aside width="280px" class="selection-sidebar">
          <div class="selection-menu">
            <div class="menu-item" :class="{ active: activeTab === 'mySelection' }" @click="activeTab = 'mySelection'">
              <span class="menu-icon">📝</span>
              <span>我的选课</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'selectionCenter' }" @click="activeTab = 'selectionCenter'">
              <span class="menu-icon">🎯</span>
              <span>选课中心</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'majorRecommend' }" @click="activeTab = 'majorRecommend'">
              <span class="menu-icon">🎓</span>
              <span>专业推荐</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'intention' }" @click="activeTab = 'intention'">
              <span class="menu-icon">🧭</span>
              <span>选课意向</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'guidance' }" @click="activeTab = 'guidance'">
              <span class="menu-icon">👨‍🏫</span>
              <span>选科指导</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'statistics' }" @click="activeTab = 'statistics'">
              <span class="menu-icon">📈</span>
              <span>统计分析</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">
              <span class="menu-icon">📜</span>
              <span>历史记录</span>
            </div>
            <div v-if="isAdmin" class="menu-item" :class="{ active: activeTab === 'approval' }" @click="activeTab = 'approval'">
              <span class="menu-icon">✅</span>
              <span>审批管理</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'grading' }" @click="activeTab = 'grading'">
              <span class="menu-icon">🧮</span>
              <span>赋分规则</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'majorAdmin' }" @click="activeTab = 'majorAdmin'">
              <span class="menu-icon">🧩</span>
              <span>专业库管理</span>
            </div>
          </div>
        </el-aside>

        <!-- 右侧内容：限制高度 + 自动滚动 -->
        <el-main class="selection-content-wrapper">
          <div class="selection-content">
            <MySelectionPanel v-show="activeTab === 'mySelection'" :isDark="isDark" :studentId="currentStudentId" @refresh="fetchMySelection" @edit="openEditDialog" />
            <SelectionCenterPanel v-show="activeTab === 'selectionCenter'" :isDark="isDark" :studentId="currentStudentId" @success="handleSelectionSuccess" />
            <MajorRecommendPanel v-show="activeTab === 'majorRecommend'" :isDark="isDark" :studentId="currentStudentId" />
            <CourseSelectionIntentionPanel v-show="activeTab === 'intention'" :isDark="isDark" :studentId="currentStudentId" :studentName="User?.username || User?.nickname || User?.name || ''" />
            <CourseGuidancePanel v-show="activeTab === 'guidance'" :isDark="isDark" :studentId="currentStudentId" :studentName="User?.username || User?.nickname || User?.name || ''" />
            <StatisticsPanel v-show="activeTab === 'statistics'" :isDark="isDark" />
            <HistoryPanel v-show="activeTab === 'history'" :isDark="isDark" :studentId="currentStudentId" />
            <ApprovalPanel v-show="activeTab === 'approval' && isAdmin" :isDark="isDark" />
            <GradingScalePanel v-show="activeTab === 'grading'" />
            <MajorAdminPanel v-show="activeTab === 'majorAdmin'" />
          </div>
        </el-main>
      </el-container>
    </div>

    <!-- 编辑弹窗 - 纯黑 -->
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

/* 纯黑暗色主题 */
.dark .selection-container {
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
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
  display: flex;
  align-items: center;
  padding: 0 15px;
  cursor: pointer;
  color: #4f4d4e;
}

.dark .menu-item {
  color: rgba(255, 255, 255, 0.7);
}

.menu-item:hover {
  background: rgba(99, 102, 241, 0.1) !important;
}

.menu-item.active {
  background: rgba(99, 102, 241, 0.08) !important;
  border-left: 3px solid transparent;
  color: #6366f1;
  position: relative;
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.2), 0 2px 8px rgba(99, 102, 241, 0.1);
}

.dark .menu-item.active {
  color: #818cf8;
  background: rgba(99, 102, 241, 0.12) !important;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

/* 右侧限制高度 + 滚动 */
.selection-content-wrapper {
  background: transparent;
  height: 750px; /* 固定最大高度 */
  overflow: hidden;
}

.selection-content {
  height: 100%;
  padding: 30px;
  overflow-y: auto; /* 内容多了自动滚动 */
}

/* 滚动条美化 */
.selection-content::-webkit-scrollbar {
  width: 6px;
}
.selection-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}
.dark .selection-content::-webkit-scrollbar-thumb {
  background: #444;
}

/* 纯黑弹窗 */
:deep(.el-dialog) {
  border-radius: 24px !important;
}

.dark :deep(.el-dialog) {
  background: #000000 !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

@media (max-width: 1024px) {
  .selection-sidebar {
    width: 240px !important;
  }
  .selection-content-wrapper {
    height: 650px;
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
  .selection-content-wrapper {
    height: 600px;
  }
}
</style>
