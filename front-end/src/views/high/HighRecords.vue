<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getStoredTheme, ThemeType } from '@/utils/theme'
import AdvancedTypewriter from '@/components/Typewriter.vue'
import HighNav from '@/components/HighNav.vue'

// 主题
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const loading = ref(true)
const saving = ref(false)
const viewMode = ref('grid')
const activeTab = ref('academic')
const texts = ref(['高中拾光记，助力你成长'])

// 年级
const selectedGrade = ref('高一')
const grades = ['高一', '高二', '高三']

// 列表
const records = ref([])
const selectedRecords = ref([])

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const detailDialogVisible = ref(false)
const currentDetailRecord = ref(null)

// 完整表单数据 - 匹配所有数据库字段
const formData = ref({
  id: null,
  userId: null,
  grade: '高一',
  semester: '上学期',
  recordDate: new Date().toISOString().split('T')[0],
  examName: '',
  examRank: '',
  examScore: null,
  bestSubject: '',
  weakestSubject: '',
  studyHours: null,
  studyNotes: '',
  competitionName: '',
  competitionAward: '',
  activityName: '',
  activityRole: '',
  interestTested: '',
  interestContinued: '',
  skillLearned: '',
  stressLevel: null,
  happinessLevel: null,
  moodNotes: '',
  challengeText: '',
  helpNeeded: '',
  closeFriendsCount: null,
  newFriends: null,
  conflictExperience: '',
  leadershipExp: '',
  selfAwareness: '',
  careerInterest: '',
  dreamCollege: '',
  dreamMajor: '',
  sleepHours: null,
  exerciseMinutes: null,
  screenTimeHours: null,
  familyCommunicationQuality: null,
  familySupport: '',
  isMilestone: false,
  milestoneName: '',
  achievementThisPeriod: '',
  improvementNeeded: '',
  nextGoal: ''
})

// 6维度统计
const stats = ref({
  totalRecords: 0,
  milestoneCount: 0,
  avgStudyHours: 0,
  avgHappinessLevel: 0,
  avgSleepHours: 0,
  avgExerciseMinutes: 0
})

const dimensionCards = computed(() => [
  { label: '总记录', value: stats.value.totalRecords || 0, icon: '📘' },
  { label: '里程碑', value: stats.value.milestoneCount || 0, icon: '🏆' },
  { label: '学习时长', value: (stats.value.avgStudyHours || 0).toFixed(1) + 'h', icon: '⏳' },
  { label: '幸福指数', value: (stats.value.avgHappinessLevel || 0).toFixed(1), icon: '😊' },
  { label: '睡眠时长', value: (stats.value.avgSleepHours || 0).toFixed(1) + 'h', icon: '😴' },
  { label: '运动时长', value: (stats.value.avgExerciseMinutes || 0).toFixed(0) + 'min', icon: '🏃' }
])

const queryParams = ref({
  grade: '高一',
  pageNum: 1,
  pageSize: 100
})

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await request.post('/highschool/growth/list', queryParams.value)
    if (res.code === 200) {
      records.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  const res = await request.get('/highschool/growth/stats')
  if (res.code === 200) stats.value = res.data
}

const deleteRecord = async (id) => {
  ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' }).then(async () => {
    const res = await request.post('/highschool/growth/delete', null, null, { params: { id } })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchRecords()
      fetchStats()
    }
  })
}

const batchDelete = async () => {
  if (selectedRecords.value.length === 0) return ElMessage.warning('请选择数据')
  ElMessageBox.confirm('确定删除选中项？').then(async () => {
    const res = await request.post('/highschool/growth/batchDelete', selectedRecords.value)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      selectedRecords.value = []
      fetchRecords()
      fetchStats()
    }
  })
}

const saveRecord = async () => {
  if (!formData.value.recordDate) return ElMessage.warning('请选择日期')
  saving.value = true
  try {
    const url = isEdit.value ? '/highschool/growth/update' : '/highschool/growth/add'
    const res = await request.post(url, formData.value)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchRecords()
      fetchStats()
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const editRecord = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑成长记录'
  formData.value = JSON.parse(JSON.stringify(row))
  dialogVisible.value = true
}

const addRecord = () => {
  isEdit.value = false
  dialogTitle.value = '添加成长记录'
  formData.value = {
    id: null,
    userId: null,
    grade: selectedGrade.value,
    semester: '上学期',
    recordDate: new Date().toISOString().split('T')[0],
    examName: '',
    examRank: '',
    examScore: null,
    bestSubject: '',
    weakestSubject: '',
    studyHours: null,
    studyNotes: '',
    competitionName: '',
    competitionAward: '',
    activityName: '',
    activityRole: '',
    interestTested: '',
    interestContinued: '',
    skillLearned: '',
    stressLevel: null,
    happinessLevel: null,
    moodNotes: '',
    challengeText: '',
    helpNeeded: '',
    closeFriendsCount: null,
    newFriends: null,
    conflictExperience: '',
    leadershipExp: '',
    selfAwareness: '',
    careerInterest: '',
    dreamCollege: '',
    dreamMajor: '',
    sleepHours: null,
    exerciseMinutes: null,
    screenTimeHours: null,
    familyCommunicationQuality: null,
    familySupport: '',
    isMilestone: false,
    milestoneName: '',
    achievementThisPeriod: '',
    improvementNeeded: '',
    nextGoal: ''
  }
  dialogVisible.value = true
}

const viewDetail = (row) => {
  currentDetailRecord.value = row
  detailDialogVisible.value = true
}

const changeGrade = (grade) => {
  selectedGrade.value = grade
  queryParams.value.grade = grade
  fetchRecords()
}

const formatDate = (d) => d ? d.split('T')[0] : ''
const getLevelColor = (v) => {
  if (!v) return 'text-slate-400'
  if (v <= 3) return 'text-emerald-500 font-medium'
  if (v <= 7) return 'text-amber-500'
  return 'text-rose-500'
}

const getStressLevelText = (level) => {
  if (!level) return '未记录'
  if (level <= 3) return '轻松'
  if (level <= 6) return '适中'
  if (level <= 8) return '较大'
  return '极大'
}

const getHappinessLevelText = (level) => {
  if (!level) return '未记录'
  if (level <= 3) return '低落'
  if (level <= 6) return '一般'
  if (level <= 8) return '开心'
  return '非常快乐'
}

onMounted(() => {
  fetchRecords()
  fetchStats()
})
</script>

<template>
  <div class="min-h-screen relative overflow-x-hidden" :class="isDark ? 'dark bg-black' : ''">
    <!-- 共用导航栏 -->
    <HighNav :isDark="isDark" />
    
    <div class="max-w-[90rem] mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-28">
      <!-- 头部区域 -->
      <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-6 mb-12">
        <div class="space-y-2">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/40 dark:bg-slate-800/40 backdrop-blur-sm border border-white/20 dark:border-slate-700/50">
            <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            <span class="text-xs font-medium text-slate-600 dark:text-slate-300">全维度成长追踪 · 实时更新</span>
          </div>
          <h1 class="text-5xl md:text-6xl font-bold tracking-tight bg-gradient-to-r from-slate-900 via-indigo-800 to-slate-900 dark:from-white dark:via-indigo-300 dark:to-white bg-clip-text text-transparent">
            <AdvancedTypewriter :texts="texts"></AdvancedTypewriter>
          </h1>
          <p class="text-slate-500 dark:text-slate-400 text-base flex items-center gap-2">
            <span class="inline-block w-6 h-[2px] bg-indigo-400 rounded-full"></span>
            学业 · 心理 · 社交 · 生涯 · 习惯 · 五维成长体系
          </p>
        </div>
        <div class="flex gap-3">
          <button @click="addRecord"
                  class="group relative px-6 py-3 bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-600 text-white rounded-2xl shadow-lg shadow-indigo-500/25 hover:shadow-indigo-500/40 transition-all duration-300 transform hover:-translate-y-0.5 flex items-center gap-2 font-medium">
            <svg class="w-5 h-5 group-hover:rotate-90 transition-transform duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
            </svg>
            记录成长
          </button>
          <button @click="batchDelete" v-if="selectedRecords.length"
                  class="px-6 py-3 bg-white/80 dark:bg-slate-800/80 backdrop-blur-sm border border-red-200 dark:border-red-900/50 text-red-600 dark:text-red-400 rounded-2xl hover:bg-red-50 dark:hover:bg-red-950/30 transition-all flex items-center gap-2 font-medium">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
            </svg>
            删除 ({{ selectedRecords.length }})
          </button>
        </div>
      </div>

      <!-- 6维度统计卡片 -->
      <div class="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4 mb-12">
        <div v-for="card in dimensionCards" :key="card.label"
             class="group relative overflow-hidden rounded-2xl bg-white/50 dark:bg-white/10 border border-white/20 dark:border-white/30 shadow-lg hover:shadow-2xl transition-all duration-500 hover:-translate-y-1">
          <div class="absolute inset-0 bg-gradient-to-br from-white/40 to-transparent dark:from-white/5 pointer-events-none"></div>
          <div class="p-4 text-center relative">
            <div class="text-3xl mb-2">{{ card.icon }}</div>
            <p class="text-2xl font-bold text-slate-800 dark:text-white">{{ card.value }}</p>
            <p class="text-xs text-slate-500 dark:text-slate-400 mt-1">{{ card.label }}</p>
          </div>
        </div>
      </div>

      <!-- 年级切换 + 视图模式 -->
      <div class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-8">
        <div class="inline-flex p-1 bg-white/60 dark:bg-slate-800/60 backdrop-blur-md rounded-full border border-white/30 dark:border-slate-700/50">
          <button v-for="g in grades" :key="g"
                  @click="changeGrade(g)"
                  class="px-8 py-2.5 rounded-full font-medium transition-all duration-300 text-sm"
                  :class="selectedGrade === g
                    ? 'bg-gradient-to-r from-indigo-600 to-indigo-500 text-white shadow-lg'
                    : 'text-slate-600 dark:text-slate-300 hover:text-indigo-600'">
            {{ g }}
          </button>
        </div>
        <div class="flex gap-2">
          <button @click="viewMode = 'grid'"
                  :class="viewMode === 'grid' ? 'bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600' : 'text-slate-500'"
                  class="p-2 rounded-lg transition-all">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg>
          </button>
          <button @click="viewMode = 'list'"
                  :class="viewMode === 'list' ? 'bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600' : 'text-slate-500'"
                  class="p-2 rounded-lg transition-all">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"></path></svg>
          </button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center py-32">
        <div class="relative">
          <div class="w-12 h-12 border-2 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
          <div class="absolute inset-0 flex items-center justify-center text-xs text-indigo-500 animate-pulse">载入</div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="records.length === 0" class="text-center py-32">
        <div class="inline-flex flex-col items-center gap-4">
          <div class="w-24 h-24 rounded-full bg-gradient-to-tr from-indigo-100 to-purple-100 dark:from-indigo-950/40 dark:to-purple-950/40 flex items-center justify-center text-5xl backdrop-blur-sm">📖</div>
          <p class="text-slate-500 dark:text-slate-400 text-lg">还没有成长记录，点击上方按钮开始记录</p>
        </div>
      </div>

      <!-- 记录列表 - 网格视图 -->
      <div v-else-if="viewMode === 'grid'" class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div v-for="item in records" :key="item.id"
             class="group relative bg-white/70 dark:bg-white/10 backdrop-blur-sm rounded-2xl border border-white/30 dark:border-white/20 shadow-lg hover:shadow-2xl transition-all duration-400 hover:-translate-y-1 overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 scale-x-0 group-hover:scale-x-100 transition-transform duration-500 origin-left"></div>

          <div class="p-5">
            <div class="flex items-start gap-3">
              <input v-model="selectedRecords" :value="item.id" type="checkbox"
                     class="mt-1 w-5 h-5 rounded-full border-2 border-slate-300 dark:border-white/30 text-indigo-600 focus:ring-indigo-500">
              <div class="flex-1 cursor-pointer" @click="viewDetail(item)">
                <!-- 头部 -->
                <div class="flex flex-wrap items-center gap-2 mb-3">
                  <span class="text-lg font-semibold bg-gradient-to-r from-slate-800 to-slate-600 dark:from-white dark:to-slate-300 bg-clip-text text-transparent">
                    {{ formatDate(item.recordDate) }}
                  </span>
                  <span class="px-2 py-0.5 rounded-full text-[11px] font-medium bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600">
                    {{ item.semester }}
                  </span>
                  <span v-if="item.isMilestone" class="px-2 py-0.5 rounded-full text-[11px] font-medium bg-amber-50 dark:bg-amber-950/40 text-amber-600 flex items-center gap-1">
                    🏅 {{ item.milestoneName || '里程碑' }}
                  </span>
                </div>

                <!-- 学业模块 -->
                <div v-if="item.examName || item.studyHours" class="mb-3 p-3 rounded-xl bg-slate-50/80 dark:bg-slate-800/40">
                  <div class="flex items-center gap-2 text-xs font-medium text-indigo-600 dark:text-indigo-400 mb-2">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
                    学业追踪
                  </div>
                  <div class="grid grid-cols-2 gap-2 text-xs">
                    <div v-if="item.examName" class="text-slate-600 dark:text-slate-300">📚 {{ item.examName }} · {{ item.examRank || '未排名' }}</div>
                    <div v-if="item.studyHours" class="text-slate-600 dark:text-slate-300">⏰ 日均 {{ item.studyHours }}h</div>
                    <div v-if="item.bestSubject" class="text-emerald-600 dark:text-emerald-400">⭐ 优势: {{ item.bestSubject }}</div>
                    <div v-if="item.weakestSubject" class="text-amber-600 dark:text-amber-400">⚠️ 薄弱: {{ item.weakestSubject }}</div>
                  </div>
                </div>

                <!-- 心理情绪 -->
                <div class="flex items-center gap-4 mb-3 text-xs dark:text-white">
                  <div class="flex items-center gap-1.5">
                    <span>😊 快乐</span>
                    <div class="w-16 h-1.5 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                      <div class="h-full bg-gradient-to-r from-emerald-400 to-green-500 rounded-full" :style="{ width: ((item.happinessLevel || 0) / 10 * 100) + '%' }"></div>
                    </div>
                    <span :class="getLevelColor(item.happinessLevel)">{{ item.happinessLevel || '-' }}</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <span>😫 压力</span>
                    <div class="w-16 h-1.5 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                      <div class="h-full bg-gradient-to-r from-rose-400 to-red-500 rounded-full" :style="{ width: ((item.stressLevel || 0) / 10 * 100) + '%' }"></div>
                    </div>
                    <span :class="getLevelColor(item.stressLevel)">{{ item.stressLevel || '-' }}</span>
                  </div>
                </div>

                <!-- 成就摘要 -->
                <div v-if="item.achievementThisPeriod" class="mt-2 p-2 rounded-lg bg-gradient-to-r from-indigo-50/50 to-purple-50/50 dark:from-indigo-950/30 dark:to-purple-950/30 text-sm italic text-slate-600 dark:text-slate-300">
                  ✨ {{ item.achievementThisPeriod }}
                </div>
              </div>

              <div class="flex flex-col gap-2 opacity-0 group-hover:opacity-100 transition-all duration-300">
                <button @click="viewDetail(item)" class="p-2 rounded-xl hover:bg-blue-50 dark:hover:bg-blue-950/40 text-blue-500" title="查看详情">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>
                </button>
                <button @click="editRecord(item)" class="p-2 rounded-xl hover:bg-indigo-50 dark:hover:bg-indigo-950/40 text-indigo-500">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                </button>
                <button @click="deleteRecord(item.id)" class="p-2 rounded-xl hover:bg-red-50 dark:hover:bg-red-950/40 text-red-400">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 - 紧凑模式 -->
      <div v-else class="space-y-3">
        <div v-for="item in records" :key="item.id"
             class="group bg-white/70 dark:bg-slate-800/50 backdrop-blur-sm rounded-xl border border-white/30 dark:border-slate-700/50 p-4 hover:shadow-lg transition-all dark:text-white">
          <div class="flex items-start gap-3">
            <input v-model="selectedRecords" :value="item.id" type="checkbox" class="mt-1 w-4 h-4 rounded">
            <div class="flex-1 flex flex-wrap items-center gap-4 cursor-pointer" @click="viewDetail(item)">
              <span class="font-mono text-sm">{{ formatDate(item.recordDate) }}</span>
              <span class="px-2 py-0.5 rounded-full text-xs bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600">{{ item.examName || '日常记录' }}</span>
              <span v-if="item.studyHours" class="text-sm">📚 {{ item.studyHours }}h/天</span>
              <span class="text-sm">😊 {{ item.happinessLevel || '-' }} / 😫 {{ item.stressLevel || '-' }}</span>
              <span v-if="item.achievementThisPeriod" class="text-sm text-indigo-600 dark:text-indigo-400 truncate max-w-md">{{ item.achievementThisPeriod }}</span>
            </div>
            <div class="flex gap-2 opacity-0 group-hover:opacity-100">
              <button @click="viewDetail(item)" class="text-blue-500 text-sm">详情</button>
              <button @click="editRecord(item)" class="text-indigo-500 text-sm">编辑</button>
              <button @click="deleteRecord(item.id)" class="text-red-500 text-sm">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑/添加弹窗 - 暗色主题适配 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" class="modern-dialog" :class="{ 'dark-dialog': isDark }" destroy-on-close>
      <el-tabs v-model="activeTab" class="growth-tabs" :class="{ 'dark-tabs': isDark }">
        <!-- 学业与考试 -->
        <el-tab-pane label="📚 学业" name="academic">
          <div class="grid grid-cols-2 gap-4 p-2">
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">考试名称</label>
              <input v-model="formData.examName" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">年级排名</label>
              <input v-model="formData.examRank" type="text" placeholder="如：前10%/第50名" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">总分/平均分</label>
              <input v-model.number="formData.examScore" type="number" step="0.5" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">优势科目</label>
              <input v-model="formData.bestSubject" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">薄弱科目</label>
              <input v-model="formData.weakestSubject" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">日均学习时长(小时)</label>
              <input v-model.number="formData.studyHours" type="number" step="0.5" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">学习心得/方法总结</label>
              <textarea v-model="formData.studyNotes" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white"></textarea>
            </div>
          </div>
        </el-tab-pane>

        <!-- 竞赛与活动 -->
        <el-tab-pane label="🏆 竞赛活动" name="activities">
          <div class="grid grid-cols-2 gap-4 p-2">
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">竞赛名称</label>
              <input v-model="formData.competitionName" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">获奖等级</label>
              <input v-model="formData.competitionAward" type="text" placeholder="省一/国二/校一等奖" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">社团/活动名称</label>
              <input v-model="formData.activityName" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">担任角色</label>
              <input v-model="formData.activityRole" type="text" placeholder="成员/部长/社长" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
          </div>
        </el-tab-pane>

        <!-- 心理与情绪 -->
        <el-tab-pane label="💭 心理" name="mental">
          <div class="grid grid-cols-2 gap-4 p-2">
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">压力指数 (1-10)</label>
              <input v-model.number="formData.stressLevel" type="number" min="1" max="10" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">快乐指数 (1-10)</label>
              <input v-model.number="formData.happinessLevel" type="number" min="1" max="10" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">心情记录</label>
              <textarea v-model="formData.moodNotes" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white" placeholder="记录此刻的心情..."></textarea>
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">遇到的困难/挑战</label>
              <textarea v-model="formData.challengeText" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white"></textarea>
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">需要的帮助</label>
              <input v-model="formData.helpNeeded" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
          </div>
        </el-tab-pane>

        <!-- 社交与人际 -->
        <el-tab-pane label="👥 社交" name="social">
          <div class="grid grid-cols-2 gap-4 p-2">
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">知心朋友数量</label>
              <input v-model.number="formData.closeFriendsCount" type="number" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">新认识的朋友</label>
              <input v-model.number="formData.newFriends" type="number" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">人际冲突/解决经验</label>
              <textarea v-model="formData.conflictExperience" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white"></textarea>
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">领导力锻炼经历</label>
              <textarea v-model="formData.leadershipExp" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white"></textarea>
            </div>
          </div>
        </el-tab-pane>

        <!-- 兴趣与技能 -->
        <el-tab-pane label="🎨 兴趣" name="interests">
          <div class="grid grid-cols-2 gap-4 p-2">
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">尝试的新兴趣/技能</label>
              <input v-model="formData.interestTested" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">持续坚持的兴趣</label>
              <input v-model="formData.interestContinued" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">学到的新技能</label>
              <input v-model="formData.skillLearned" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
          </div>
        </el-tab-pane>

        <!-- 生涯规划 -->
        <el-tab-pane label="🎯 生涯" name="career">
          <div class="grid grid-cols-2 gap-4 p-2">
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">自我认识</label>
              <textarea v-model="formData.selfAwareness" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white" placeholder="优点/缺点/兴趣方向"></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">职业兴趣方向</label>
              <input v-model="formData.careerInterest" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">理想大学</label>
              <input v-model="formData.dreamCollege" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">理想专业</label>
              <input v-model="formData.dreamMajor" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
          </div>
        </el-tab-pane>

        <!-- 生活习惯 -->
        <el-tab-pane label="💪 习惯" name="habits">
          <div class="grid grid-cols-2 gap-4 p-2">
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">日均睡眠(小时)</label>
              <input v-model.number="formData.sleepHours" type="number" step="0.5" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">日均运动(分钟)</label>
              <input v-model.number="formData.exerciseMinutes" type="number" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">日均屏幕时间(小时)</label>
              <input v-model.number="formData.screenTimeHours" type="number" step="0.5" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">家庭沟通质量(1-10)</label>
              <input v-model.number="formData.familyCommunicationQuality" type="number" min="1" max="10" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white">
            </div>
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">家人支持情况</label>
              <textarea v-model="formData.familySupport" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white"></textarea>
            </div>
          </div>
        </el-tab-pane>

        <!-- 成长总结 -->
        <el-tab-pane label="✨ 总结" name="summary">
          <div class="grid grid-cols-1 gap-4 p-2">
            <div class="flex items-center gap-3">
              <input type="checkbox" v-model="formData.isMilestone" id="milestone" class="w-5 h-5">
              <label for="milestone" class="text-sm font-medium dark:text-slate-300">🏅 标记为重要成长节点（里程碑）</label>
            </div>
            <div v-if="formData.isMilestone">
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">里程碑名称</label>
              <input v-model="formData.milestoneName" type="text" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white" placeholder="如：第一次获得竞赛奖项">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">本阶段成就</label>
              <textarea v-model="formData.achievementThisPeriod" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white" placeholder="这个阶段最值得骄傲的进步..."></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">需要改进的地方</label>
              <textarea v-model="formData.improvementNeeded" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white"></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 dark:text-slate-300">下个目标</label>
              <textarea v-model="formData.nextGoal" rows="2" class="w-full px-3 py-2 rounded-lg border dark:border-slate-600 dark:bg-slate-800 dark:text-white"></textarea>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="flex justify-end gap-3 pt-2">
          <button @click="dialogVisible = false" class="px-5 py-2.5 rounded-xl border dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-700 transition-all">取消</button>
          <button @click="saveRecord" :disabled="saving" class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-500 text-white shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/50 transition-all">
            {{ saving ? '保存中...' : '保存记录' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="成长记录详情" width="800px" class="detail-dialog" :class="{ 'dark-dialog': isDark }" destroy-on-close>
      <div v-if="currentDetailRecord" class="space-y-6">
        <!-- 基本信息 -->
        <div class="flex items-center justify-between pb-4 border-b dark:border-slate-700">
          <div>
            <div class="text-2xl font-bold text-slate-800 dark:text-white">{{ formatDate(currentDetailRecord.recordDate) }}</div>
            <div class="flex gap-2 mt-2">
              <span class="px-3 py-1 rounded-full text-sm bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-400">{{ currentDetailRecord.grade }} · {{ currentDetailRecord.semester }}</span>
              <span v-if="currentDetailRecord.isMilestone" class="px-3 py-1 rounded-full text-sm bg-amber-100 dark:bg-amber-900/40 text-amber-600 dark:text-amber-400 flex items-center gap-1">
                🏅 {{ currentDetailRecord.milestoneName || '里程碑' }}
              </span>
            </div>
          </div>
          <div class="flex gap-2">
            <button @click="editRecord(currentDetailRecord); detailDialogVisible = false" class="px-4 py-2 rounded-lg bg-indigo-50 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-100 transition-all">编辑</button>
          </div>
        </div>

        <!-- 学业信息 -->
        <div v-if="currentDetailRecord.examName || currentDetailRecord.studyHours" class="bg-gradient-to-r from-indigo-50/50 to-purple-50/50 dark:from-indigo-950/30 dark:to-purple-950/30 rounded-xl p-5">
          <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">📚 学业追踪</h3>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div v-if="currentDetailRecord.examName">
              <p class="text-slate-500 dark:text-slate-400">考试名称</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.examName }}</p>
            </div>
            <div v-if="currentDetailRecord.examRank">
              <p class="text-slate-500 dark:text-slate-400">年级排名</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.examRank }}</p>
            </div>
            <div v-if="currentDetailRecord.examScore">
              <p class="text-slate-500 dark:text-slate-400">总分/平均分</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.examScore }}</p>
            </div>
            <div v-if="currentDetailRecord.studyHours">
              <p class="text-slate-500 dark:text-slate-400">日均学习</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.studyHours }}小时</p>
            </div>
            <div v-if="currentDetailRecord.bestSubject">
              <p class="text-slate-500 dark:text-slate-400">优势科目</p>
              <p class="font-medium text-emerald-600 dark:text-emerald-400">{{ currentDetailRecord.bestSubject }}</p>
            </div>
            <div v-if="currentDetailRecord.weakestSubject">
              <p class="text-slate-500 dark:text-slate-400">薄弱科目</p>
              <p class="font-medium text-amber-600 dark:text-amber-400">{{ currentDetailRecord.weakestSubject }}</p>
            </div>
          </div>
          <div v-if="currentDetailRecord.studyNotes" class="mt-3 pt-3 border-t border-indigo-200 dark:border-indigo-800">
            <p class="text-slate-500 dark:text-slate-400 text-sm">📝 学习心得</p>
            <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.studyNotes }}</p>
          </div>
        </div>

        <!-- 竞赛活动 -->
        <div v-if="currentDetailRecord.competitionName || currentDetailRecord.activityName" class="bg-gradient-to-r from-amber-50/50 to-orange-50/50 dark:from-amber-950/30 dark:to-orange-950/30 rounded-xl p-5">
          <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">🏆 竞赛与活动</h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div v-if="currentDetailRecord.competitionName">
              <p class="text-slate-500 dark:text-slate-400">竞赛名称</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.competitionName }}</p>
            </div>
            <div v-if="currentDetailRecord.competitionAward">
              <p class="text-slate-500 dark:text-slate-400">获奖等级</p>
              <p class="font-medium text-amber-600 dark:text-amber-400">{{ currentDetailRecord.competitionAward }}</p>
            </div>
            <div v-if="currentDetailRecord.activityName">
              <p class="text-slate-500 dark:text-slate-400">社团/活动</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.activityName }}</p>
            </div>
            <div v-if="currentDetailRecord.activityRole">
              <p class="text-slate-500 dark:text-slate-400">担任角色</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.activityRole }}</p>
            </div>
          </div>
        </div>

        <!-- 心理情绪 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="bg-gradient-to-r from-emerald-50/50 to-teal-50/50 dark:from-emerald-950/30 dark:to-teal-950/30 rounded-xl p-5">
            <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">😊 心理状态</h3>
            <div class="space-y-3">
              <div>
                <p class="text-slate-500 dark:text-slate-400 text-sm">快乐指数</p>
                <div class="flex items-center gap-3 mt-1">
                  <div class="flex-1 h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                    <div class="h-full bg-gradient-to-r from-emerald-400 to-green-500 rounded-full" :style="{ width: ((currentDetailRecord.happinessLevel || 0) / 10 * 100) + '%' }"></div>
                  </div>
                  <span class="font-bold text-lg" :class="getLevelColor(currentDetailRecord.happinessLevel)">{{ currentDetailRecord.happinessLevel || '-' }}/10</span>
                </div>
                <p class="text-sm text-slate-600 dark:text-slate-400 mt-1">{{ getHappinessLevelText(currentDetailRecord.happinessLevel) }}</p>
              </div>
              <div>
                <p class="text-slate-500 dark:text-slate-400 text-sm">压力指数</p>
                <div class="flex items-center gap-3 mt-1">
                  <div class="flex-1 h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                    <div class="h-full bg-gradient-to-r from-rose-400 to-red-500 rounded-full" :style="{ width: ((currentDetailRecord.stressLevel || 0) / 10 * 100) + '%' }"></div>
                  </div>
                  <span class="font-bold text-lg" :class="getLevelColor(currentDetailRecord.stressLevel)">{{ currentDetailRecord.stressLevel || '-' }}/10</span>
                </div>
                <p class="text-sm text-slate-600 dark:text-slate-400 mt-1">{{ getStressLevelText(currentDetailRecord.stressLevel) }}</p>
              </div>
              <div v-if="currentDetailRecord.moodNotes">
                <p class="text-slate-500 dark:text-slate-400 text-sm">💭 心情记录</p>
                <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.moodNotes }}</p>
              </div>
              <div v-if="currentDetailRecord.challengeText">
                <p class="text-slate-500 dark:text-slate-400 text-sm">⚠️ 遇到的挑战</p>
                <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.challengeText }}</p>
              </div>
            </div>
          </div>

          <div class="bg-gradient-to-r from-blue-50/50 to-cyan-50/50 dark:from-blue-950/30 dark:to-cyan-950/30 rounded-xl p-5">
            <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">👥 社交与人际</h3>
            <div class="space-y-2 text-sm">
              <div v-if="currentDetailRecord.closeFriendsCount" class="flex justify-between">
                <span class="text-slate-500 dark:text-slate-400">知心朋友</span>
                <span class="font-medium dark:text-white">{{ currentDetailRecord.closeFriendsCount }}人</span>
              </div>
              <div v-if="currentDetailRecord.newFriends" class="flex justify-between">
                <span class="text-slate-500 dark:text-slate-400">新朋友</span>
                <span class="font-medium text-emerald-600 dark:text-emerald-400">+{{ currentDetailRecord.newFriends }}</span>
              </div>
              <div v-if="currentDetailRecord.conflictExperience">
                <p class="text-slate-500 dark:text-slate-400">人际冲突经验</p>
                <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.conflictExperience }}</p>
              </div>
              <div v-if="currentDetailRecord.leadershipExp">
                <p class="text-slate-500 dark:text-slate-400">领导力锻炼</p>
                <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.leadershipExp }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 兴趣与技能 -->
        <div v-if="currentDetailRecord.interestTested || currentDetailRecord.skillLearned" class="bg-gradient-to-r from-purple-50/50 to-pink-50/50 dark:from-purple-950/30 dark:to-pink-950/30 rounded-xl p-5">
          <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">🎨 兴趣与技能</h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div v-if="currentDetailRecord.interestTested">
              <p class="text-slate-500 dark:text-slate-400">新尝试的兴趣</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.interestTested }}</p>
            </div>
            <div v-if="currentDetailRecord.interestContinued">
              <p class="text-slate-500 dark:text-slate-400">持续坚持的兴趣</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.interestContinued }}</p>
            </div>
            <div v-if="currentDetailRecord.skillLearned">
              <p class="text-slate-500 dark:text-slate-400">学到的新技能</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.skillLearned }}</p>
            </div>
          </div>
        </div>

        <!-- 生涯规划 -->
        <div v-if="currentDetailRecord.dreamCollege || currentDetailRecord.careerInterest" class="bg-gradient-to-r from-indigo-50/50 to-violet-50/50 dark:from-indigo-950/30 dark:to-violet-950/30 rounded-xl p-5">
          <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">🎯 生涯规划</h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div v-if="currentDetailRecord.careerInterest">
              <p class="text-slate-500 dark:text-slate-400">职业兴趣</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.careerInterest }}</p>
            </div>
            <div v-if="currentDetailRecord.dreamCollege">
              <p class="text-slate-500 dark:text-slate-400">理想大学</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.dreamCollege }}</p>
            </div>
            <div v-if="currentDetailRecord.dreamMajor">
              <p class="text-slate-500 dark:text-slate-400">理想专业</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.dreamMajor }}</p>
            </div>
            <div v-if="currentDetailRecord.selfAwareness" class="col-span-2">
              <p class="text-slate-500 dark:text-slate-400">自我认识</p>
              <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.selfAwareness }}</p>
            </div>
          </div>
        </div>

        <!-- 生活习惯 -->
        <div v-if="currentDetailRecord.sleepHours || currentDetailRecord.exerciseMinutes" class="bg-gradient-to-r from-teal-50/50 to-emerald-50/50 dark:from-teal-950/30 dark:to-emerald-950/30 rounded-xl p-5">
          <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">💪 生活习惯</h3>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div v-if="currentDetailRecord.sleepHours">
              <p class="text-slate-500 dark:text-slate-400">日均睡眠</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.sleepHours }}小时</p>
            </div>
            <div v-if="currentDetailRecord.exerciseMinutes">
              <p class="text-slate-500 dark:text-slate-400">日均运动</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.exerciseMinutes }}分钟</p>
            </div>
            <div v-if="currentDetailRecord.screenTimeHours">
              <p class="text-slate-500 dark:text-slate-400">屏幕时间</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.screenTimeHours }}小时</p>
            </div>
            <div v-if="currentDetailRecord.familyCommunicationQuality">
              <p class="text-slate-500 dark:text-slate-400">家庭沟通质量</p>
              <p class="font-medium dark:text-white">{{ currentDetailRecord.familyCommunicationQuality }}/10</p>
            </div>
          </div>
          <div v-if="currentDetailRecord.familySupport" class="mt-3">
            <p class="text-slate-500 dark:text-slate-400 text-sm">👨‍👩‍👧 家人支持</p>
            <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.familySupport }}</p>
          </div>
        </div>

        <!-- 总结部分 -->
        <div class="bg-gradient-to-r from-slate-100 to-slate-50 dark:from-slate-800/50 dark:to-slate-800/30 rounded-xl p-5">
          <h3 class="font-semibold text-lg mb-3 flex items-center gap-2 dark:text-white">✨ 成长总结</h3>
          <div class="space-y-3">
            <div v-if="currentDetailRecord.achievementThisPeriod">
              <p class="text-slate-500 dark:text-slate-400 text-sm">🏅 本阶段成就</p>
              <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.achievementThisPeriod }}</p>
            </div>
            <div v-if="currentDetailRecord.improvementNeeded">
              <p class="text-slate-500 dark:text-slate-400 text-sm">📈 需要改进</p>
              <p class="mt-1 dark:text-slate-300">{{ currentDetailRecord.improvementNeeded }}</p>
            </div>
            <div v-if="currentDetailRecord.nextGoal">
              <p class="text-slate-500 dark:text-slate-400 text-sm">🎯 下个目标</p>
              <p class="mt-1 dark:text-slate-300 font-medium">{{ currentDetailRecord.nextGoal }}</p>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="detailDialogVisible = false" class="px-5 py-2.5 rounded-xl border dark:border-slate-600 dark:text-slate-300">关闭</button>
          <button @click="editRecord(currentDetailRecord); detailDialogVisible = false" class="px-5 py-2.5 rounded-xl bg-indigo-600 text-white hover:bg-indigo-700 transition-all">编辑记录</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
:deep(.modern-dialog),
:deep(.detail-dialog) {
  --el-dialog-border-radius: 28px;
}
:deep(.el-dialog) {
  border-radius: 28px !important;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px);
}
.dark :deep(.el-dialog) {
  background: rgba(15, 23, 42, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1);
}
:deep(.el-tabs__header) {
  margin-bottom: 20px;
}
:deep(.el-tabs__item) {
  font-weight: 500;
}
:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}
.dark :deep(.el-tabs__item) {
  color: #94a3b8;
}
.dark :deep(.el-tabs__item.is-active) {
  color: #818cf8;
}
.dark :deep(.el-tabs__nav-wrap::after) {
  background-color: #334155;
}
</style>

<style>
/* 全局滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #555;
}
.dark ::-webkit-scrollbar-track {
  background: #1e293b;
}
.dark ::-webkit-scrollbar-thumb {
  background: #475569;
}
</style>
