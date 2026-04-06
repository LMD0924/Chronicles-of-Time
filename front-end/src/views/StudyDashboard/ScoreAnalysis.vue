<template>
  <div class="space-y-6 max-w-[1400px] mx-auto px-4 lg:px-6 mt-24">
    <!-- 页面头部 -->
    <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-2xl shadow-indigo-500/10">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-emerald-500 via-teal-500 to-cyan-500 flex items-center justify-center shadow-lg shadow-emerald-500/30">
          <span class="text-white text-2xl">📊</span>
        </div>
        <div>
          <h2 class="text-2xl font-bold bg-gradient-to-r from-emerald-600 to-teal-600 bg-clip-text text-transparent">成绩分析</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">✨ 数据驱动，精准提升</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-indigo-500/10">
        <p class="text-indigo-600 dark:text-indigo-400 text-sm font-medium">总平均分</p>
        <p class="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mt-2">{{ overallAvg || '--' }}</p>
      </div>
      <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-emerald-500/10">
        <p class="text-emerald-600 dark:text-emerald-400 text-sm font-medium">最高分</p>
        <p class="text-4xl font-bold bg-gradient-to-r from-emerald-600 to-teal-600 bg-clip-text text-transparent mt-2">{{ maxScore || '--' }}</p>
      </div>
      <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-amber-500/10">
        <p class="text-amber-600 dark:text-amber-400 text-sm font-medium">考试次数</p>
        <p class="text-4xl font-bold bg-gradient-to-r from-amber-600 to-orange-600 bg-clip-text text-transparent mt-2">{{ examCount || '--' }}</p>
      </div>
      <div class="bg-white/80 dark:bg-black backdrop-blur-xl rounded-3xl p-6 border border-white/20 dark:border-gray-700/30 shadow-xl shadow-rose-500/10">
        <p class="text-rose-600 dark:text-rose-400 text-sm font-medium">错题总数</p>
        <p class="text-4xl font-bold bg-gradient-to-r from-rose-600 to-red-600 bg-clip-text text-transparent mt-2">{{ totalMistakes || '--' }}</p>
      </div>
    </div>

    <!-- 成绩趋势图 -->
    <div class="bg-white dark:bg-black rounded-2xl p-6 border border-gray-200/50 dark:border-gray-700/50">
      <div class="flex flex-wrap gap-3 justify-between items-center mb-4">
        <h3 class="font-semibold text-gray-800 dark:text-gray-200">成绩趋势</h3>
        <div class="flex gap-2">
          <select v-model="selectedCategory" class="px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-black text-sm">
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <select v-model="selectedSubject" class="px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-black text-sm">
            <option v-for="subject in subjects" :key="subject" :value="subject">{{ subject }}</option>
          </select>
        </div>
      </div>
      <div ref="trendChartRef" style="height: 300px;"></div>
    </div>

    <!-- 薄弱科目分析 -->
    <div class="bg-white dark:bg-black rounded-2xl p-6 border border-gray-200/50 dark:border-gray-700/50">
      <h3 class="font-semibold text-gray-800 dark:text-gray-200 mb-4">薄弱科目分析</h3>
      <div class="space-y-4">
        <div v-for="subject in weakSubjects" :key="subject.subject_name">
          <div class="flex justify-between mb-1">
            <span class="text-gray-700 dark:text-gray-300">{{ subject.subject_name }}</span>
            <span :class="getScoreColor(subject.avg_score)">{{ subject.avg_score }}分</span>
          </div>
          <div class="h-2 bg-gray-200 dark:bg-black rounded-full overflow-hidden">
            <div class="h-full rounded-full transition-all" :class="getProgressClass(subject.avg_score)" :style="{ width: (subject.avg_score / 100 * 100) + '%' }"></div>
          </div>
          <p class="text-xs text-gray-500 mt-1">考试次数：{{ subject.exam_count }}次</p>
        </div>
      </div>
    </div>

    <!-- 成绩列表 -->
    <div class="bg-white dark:bg-black rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200/50 dark:border-gray-700/50 flex justify-between items-center">
        <h3 class="font-semibold text-gray-800 dark:text-gray-200">成绩记录</h3>
        <button @click="showAddModal = true" class="px-4 py-2 rounded-xl bg-gradient-to-r from-indigo-500 to-purple-500 text-white text-sm font-medium hover:shadow-lg transition">
          + 添加成绩
        </button>
      </div>
      <div class="divide-y divide-gray-100 dark:divide-gray-700">
        <div v-for="score in scoreList" :key="score.id" class="px-6 py-3 flex justify-between items-center">
          <div>
            <span class="font-medium text-gray-800 dark:text-gray-200">{{ score.subjectName }}</span>
            <span class="text-gray-500 text-sm ml-3">{{ score.examDate }}</span>
          </div>
          <div class="flex items-center gap-3">
            <span :class="getScoreColor(score.score)" class="font-bold">{{ score.score }}</span>
            <button @click="deleteScore(score.id)" class="text-red-500 hover:text-red-600 text-sm">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加成绩弹窗 -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showAddModal = false">
      <div class="bg-white dark:bg-black rounded-2xl w-full max-w-md">
        <div class="p-6">
          <h3 class="text-xl font-bold text-gray-800 dark:text-gray-200 mb-4">添加成绩</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">科目</label>
              <select v-model="newScore.subjectName" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-black">
                <option v-for="subject in subjects" :key="subject" :value="subject">{{ subject }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">分数</label>
              <input v-model.number="newScore.score" type="number" min="0" max="100" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-black">
            </div>
            <div>
              <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300">考试日期</label>
              <input v-model="newScore.examDate" type="date" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-black">
            </div>
          </div>
          <div class="flex gap-3 mt-6">
            <button @click="showAddModal = false" class="flex-1 px-4 py-2 rounded-xl border border-gray-200 text-gray-600">取消</button>
            <button @click="addScore" class="flex-1 px-4 py-2 rounded-xl bg-indigo-500 text-white">确认添加</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '@/utils/request.js'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const overallAvg = ref(0)
const maxScore = ref(0)
const examCount = ref(0)
const totalMistakes = ref(0)
const scoreList = ref([])
const weakSubjects = ref([])
const categories = ref(['高中', '大学', '考公', '考研', '考证'])
const subjects = ref(['数学', '语文', '英语', '物理', '化学', '生物'])
const selectedCategory = ref('高中')
const selectedSubject = ref('数学')
const trendChartRef = ref(null)
let trendChart = null

const showAddModal = ref(false)
const newScore = ref({
  subjectName: '数学',
  score: '',
  examDate: new Date().toISOString().split('T')[0]
})

const fetchData = async () => {
  if (!props.studentId) return
  try {
    const [avgRes, weakRes, listRes, mistakeRes] = await Promise.all([
      request.get(`/score/overall-avg/${props.studentId}`),
      request.get(`/score/weak-subject/${props.studentId}`),
      request.get(`/score/list/${props.studentId}`),
      request.get(`/mistake/list/${props.studentId}`)
    ])
    if (avgRes.code === 200) overallAvg.value = avgRes.data
    if (weakRes.code === 200) weakSubjects.value = weakRes.data || []
    if (listRes.code === 200) {
      scoreList.value = listRes.data || []
      examCount.value = scoreList.value.length
      const scores = scoreList.value.map(s => s.score)
      maxScore.value = scores.length ? Math.max(...scores) : 0
    }
    if (mistakeRes.code === 200) totalMistakes.value = (mistakeRes.data || []).length
  } catch (error) {
    console.error('获取数据失败', error)
  }
}

const fetchTrend = async () => {
  if (!props.studentId || !trendChart) return
  try {
    const res = await request.get(`/score/trend/${props.studentId}/${selectedSubject.value}`, {
      params: { categoryLevel: selectedCategory.value }
    })
    if (res.code === 200 && res.data) {
      trendChart.setOption({
        xAxis: { data: res.data.map(item => item.exam_date) },
        series: [{ data: res.data.map(item => item.score) }]
      })
    }
  } catch (error) {
    console.error('获取趋势失败', error)
  }
}

const getScoreColor = (score) => {
  if (score >= 85) return 'text-emerald-600'
  if (score >= 70) return 'text-blue-600'
  if (score >= 60) return 'text-amber-600'
  return 'text-red-600'
}

const getProgressClass = (score) => {
  if (score >= 85) return 'bg-emerald-500'
  if (score >= 70) return 'bg-blue-500'
  if (score >= 60) return 'bg-amber-500'
  return 'bg-red-500'
}

const initChart = () => {
  if (trendChartRef.value) {
    if (trendChart) trendChart.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value', name: '分数', min: 0, max: 100 },
      series: [{ type: 'line', data: [], smooth: true, lineStyle: { color: '#6366f1', width: 3 }, areaStyle: { opacity: 0.2 } }]
    })
    fetchTrend()
  }
}

watch(() => props.studentId, async (val) => {
  if (val) {
    await fetchData()
    await nextTick()
    initChart()
  }
}, { immediate: true })

watch([selectedSubject, selectedCategory], () => fetchTrend())

const addScore = async () => {
  try {

    const res = await request.post('/score/add', {
      userId: props.studentId,
      subjectName: newScore.value.subjectName,
      score: newScore.value.score,
      examDate: newScore.value.examDate
    })
    if (res.code === 200) {
      ElMessage.success('添加成功')
      showAddModal.value = false
      newScore.value = {
        subjectName: '数学',
        score: '',
        examDate: new Date().toISOString().split('T')[0]
      }
      fetchData()
      if (trendChart) fetchTrend()
    }
  } catch (error) {
    console.error('添加成绩失败', error)
    ElMessage.error('添加失败')
  }
}

const deleteScore = async (id) => {
  if (confirm('确定删除这条成绩记录吗？')) {
    try {
      const res = await request.delete(`/score/delete/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
        if (trendChart) fetchTrend()
      }
    } catch (error) {
      console.error('删除成绩失败', error)
      ElMessage.error('删除失败')
    }
  }
}
</script>
