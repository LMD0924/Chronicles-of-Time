<template>
  <div :class="isDark ? 'dark bg-black' : ''">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900',
      'min-h-screen transition-colors duration-300'
    ]">

      <Nav :isDark="isDark" :menuItems="menuItems"/>

    <div class="max-w-[min(92vw,1420px)] mx-auto px-4 sm:px-6 lg:px-10 py-20 md:py-24" :class="isDark ? 'bg-black' : ''">
      <!-- 头部 -->
      <div class="mb-8 md:mb-10">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/40 dark:bg-black backdrop-blur-sm mb-4 ring-1 ring-slate-200/60 dark:ring-slate-600/40">
          <span class="relative flex h-2 w-2">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span class="text-xs font-medium text-slate-600 dark:text-slate-300">3+1+2新高考选课系统</span>
        </div>
        <h1 class="text-3xl sm:text-4xl md:text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent tracking-tight">
          新高考选课系统
        </h1>
        <p class="text-slate-500 dark:text-slate-400 mt-2 text-sm sm:text-base max-w-2xl">物理/历史二选一 · 化学/生物/政治/地理四选二</p>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4 mb-6 md:mb-8">
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black backdrop-blur-sm border border-white/20 dark:border-slate-800">
          <div class="text-2xl mb-1">📊</div>
          <div class="text-2xl font-bold">{{ statistics.totalStudents || 0 }}</div>
          <div class="text-xs text-slate-500 dark:text-slate-400">总选课人数</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black backdrop-blur-sm border border-white/20 dark:border-slate-800">
          <div class="text-2xl mb-1">🏆</div>
          <div class="text-2xl font-bold">{{ statistics.confirmedCount || 0 }}</div>
          <div class="text-xs text-slate-500 dark:text-slate-400">已确认人数</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black backdrop-blur-sm border border-white/20 dark:border-slate-800">
          <div class="text-2xl mb-1">📚</div>
          <div class="text-2xl font-bold">{{ statistics.combinationCount || 0 }}</div>
          <div class="text-xs text-slate-500 dark:text-slate-400">可选组合</div>
        </div>
        <div class="rounded-xl p-4 bg-white/50 dark:bg-black backdrop-blur-sm border border-white/20 dark:border-slate-800">
          <div class="text-2xl mb-1">🎯</div>
          <div class="text-2xl font-bold">{{ statistics.hotCombination || '物化生' }}</div>
          <div class="text-xs text-slate-500 dark:text-slate-400">最热门组合</div>
        </div>
      </div>

      <!-- 左侧菜单 + 右侧内容 -->
      <el-container class="selection-container">
        <el-aside width="280px" class="selection-sidebar">
          <!-- 方案1：使用 @select 事件 -->
          <el-menu
            :default-active="activeTab"
            class="selection-menu"
            :background-color="isDark ? '#000000' : '#ffffff'"
            :text-color="isDark ? '#94a3b8' : '#000000'"
            :active-text-color="isDark ? '#818cf8' : '#6366f1'"
            mode="vertical"
            @select="handleMenuSelect"
          >
            <el-menu-item index="mySelection" class="menu-item">
              <span class="menu-icon">📝</span>
              <span>我的选课</span>
            </el-menu-item>

            <el-menu-item index="selectionCenter" class="menu-item">
              <span class="menu-icon">🎯</span>
              <span>选课中心</span>
            </el-menu-item>

            <el-menu-item index="majorRecommend" class="menu-item">
              <span class="menu-icon">🎓</span>
              <span>专业推荐</span>
            </el-menu-item>

            <el-menu-item index="intention" class="menu-item">
              <span class="menu-icon">🧭</span>
              <span>选课意向</span>
            </el-menu-item>

            <el-menu-item index="guidance" class="menu-item">
              <span class="menu-icon">👨‍🏫</span>
              <span>选科指导</span>
            </el-menu-item>

            <el-menu-item index="statistics" class="menu-item">
              <span class="menu-icon">📈</span>
              <span>统计分析</span>
            </el-menu-item>

            <el-menu-item index="history" class="menu-item">
              <span class="menu-icon">📜</span>
              <span>历史记录</span>
            </el-menu-item>

            <el-menu-item v-if="isAdmin" index="approval" class="menu-item">
              <span class="menu-icon">✅</span>
              <span>审批管理</span>
            </el-menu-item>

            <el-menu-item index="grading" class="menu-item">
              <span class="menu-icon">🧮</span>
              <span>赋分规则</span>
            </el-menu-item>

            <el-menu-item index="majorAdmin" class="menu-item">
              <span class="menu-icon">🧩</span>
              <span>专业库管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="selection-content">
          <!-- 使用 v-if 而不是 v-show，确保完全重新渲染 -->
          <MySelectionPanel
            v-if="activeTab === 'mySelection'"
            :isDark="isDark"
            :studentId="currentStudentId"
            @refresh="fetchMySelection"
            @edit="openEditDialog"
          />

          <SelectionCenterPanel
            v-if="activeTab === 'selectionCenter'"
            :isDark="isDark"
            :studentId="currentStudentId"
            @success="handleSelectionSuccess"
          />

          <MajorRecommendPanel
            v-if="activeTab === 'majorRecommend'"
            :isDark="isDark"
            :studentId="currentStudentId"
          />

          <CourseSelectionIntentionPanel
            v-if="activeTab === 'intention'"
            :isDark="isDark"
            :studentId="currentStudentId"
            :studentName="User?.username || User?.nickname || User?.name || ''"
          />

          <CourseGuidancePanel
            v-if="activeTab === 'guidance'"
            :isDark="isDark"
            :studentId="currentStudentId"
            :studentName="User?.username || User?.nickname || User?.name || ''"
          />

          <StatisticsPanel
            v-if="activeTab === 'statistics'"
            :isDark="isDark"
          />

          <HistoryPanel
            v-if="activeTab === 'history'"
            :isDark="isDark"
            :studentId="currentStudentId"
          />

          <ApprovalPanel
            v-if="activeTab === 'approval' && isAdmin"
            :isDark="isDark"
          />

          <GradingScalePanel
            v-if="activeTab === 'grading'"
          />

          <MajorAdminPanel
            v-if="activeTab === 'majorAdmin'"
          />
        </el-main>
      </el-container>
    </div>

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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import Nav from '@/components/Nav.vue'
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
import { getStoredTheme, ThemeType } from '@/utils/theme'

// 主题
const isDark = ref(getStoredTheme() === ThemeType.DARK)
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

// 导航菜单配置
const menuItems = [
  {
    key: 'CourseSelection',
    label: '明确目标',
    icon: '🎯',
    path: '/CourseSelection'
  },
  {
    key: 'Volunteer',
    label: '规划未来',
    icon: '🎓',
    path: '/Volunteer'
  },
  {
    key: 'StudyDashboard',
    label: '温故而知新',
    icon: '📚',
    children: [
      { key: 'practice', label: '实战练习', icon: '⚡', path: '/StudyDashboard?tab=practice' },
      { key: 'mistake', label: '错题本', icon: '📖', path: '/StudyDashboard?tab=mistake' },
      { key: 'analysis', label: '成绩分析', icon: '📊', path: '/StudyDashboard?tab=analysis' },
      { key: 'questionBank', label: '题库管理', icon: '📚', path: '/StudyDashboard?tab=questionBank' },
      { key: 'answerRecords', label: '答题记录', icon: '✍️', path: '/StudyDashboard?tab=answerRecords' }
    ]
  },
  {
    key: '个人图谱分析',
    label: '图谱',
    icon: '👤',
    path: '/GraphView'
  }
]

// 处理菜单选择
const handleMenuSelect = (index) => {
  console.log('菜单被点击:', index)
  activeTab.value = index
}

const getUserInfo = () => {
  return request.get('/user/getUserById', {}, (message, data) => {
    User.value = data || {}
    currentStudentId.value = data?.id != null ? String(data.id) : ''
  })
}

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

<style scoped>
.selection-container {
  /* 大屏略增高工作区；矮屏随视口收缩，避免 min/max 冲突 */
  min-height: min(520px, calc(100vh - 12rem));
  max-height: min(780px, calc(100vh - 10rem));
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 4px 6px -1px rgba(15, 23, 42, 0.06),
    0 20px 50px -12px rgba(99, 102, 241, 0.12);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: stretch;
}

.dark .selection-container {
  background: black;
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
  max-height: min(780px, calc(100vh - 10rem));
  padding: 20px 0;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: thin;
  scrollbar-color: rgba(99, 102, 241, 0.35) transparent;
}

.menu-item {
  height: 60px;
  line-height: 60px;
  margin: 0 15px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
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
  flex: 1;
  min-width: 0;
  padding: 24px 28px 28px;
  background: transparent;
  overflow-x: hidden;
  overflow-y: auto;
  scroll-behavior: smooth;
  scrollbar-width: thin;
  scrollbar-color: rgba(99, 102, 241, 0.4) transparent;
}

:deep(.el-dialog) {
  border-radius: 24px !important;
}

.dark :deep(.el-dialog) {
  background: rgba(15, 23, 42, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

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
    min-height: auto;
    max-height: none;
  }

  .selection-menu {
    max-height: none;
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

.selection-content::-webkit-scrollbar,
.selection-menu::-webkit-scrollbar {
  width: 7px;
}
.selection-content::-webkit-scrollbar-thumb,
.selection-menu::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.35);
  border-radius: 999px;
}
.selection-content::-webkit-scrollbar-track,
.selection-menu::-webkit-scrollbar-track {
  background: transparent;
}
</style>

<!-- 子面板根节点统一类名，保证在滚动区内宽度与排版一致 -->
<style>
.cs-panel {
  width: 100%;
  max-width: 100%;
  min-height: 0;
  box-sizing: border-box;
}
</style>
