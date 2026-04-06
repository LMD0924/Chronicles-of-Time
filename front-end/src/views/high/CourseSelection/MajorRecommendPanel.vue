<script setup>
import { ref, watch } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean,
  studentId: [String, Number]
})

const loading = ref(false)
const currentSelection = ref(null)
const recommendedMajors = ref([])
const searchKeyword = ref('')
const searchResults = ref([])
const hotMajors = ref([])

// 获取当前选课
const fetchCurrentSelection = async () => {
  try {
    const res = await request.get(`/selection/student/${props.studentId}`)
    if (res.code === 200 && res.data.length > 0) {
      currentSelection.value = res.data[0]
      fetchRecommendations()
    }
  } catch (error) {
    console.error('获取选课失败', error)
  }
}

// 获取推荐专业
const fetchRecommendations = async () => {
  if (!currentSelection.value || !props.studentId) return

  loading.value = true
  try {
    const res = await request.get('/major/match', {
      firstSubject: currentSelection.value.firstSubjectName,
      subject1Id: currentSelection.value.firstSubjectId,
      subject2Id: currentSelection.value.secondSubject1Id,
      subject3Id: currentSelection.value.secondSubject2Id,
      limit: 8
    })

    if (res.code === 200) {
      recommendedMajors.value = res.data || []
    }
  } catch (error) {
    console.error('获取推荐失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索专业
const searchMajors = async () => {
  if (!searchKeyword.value) return

  try {
    const res = await request.get('/major/search', { keyword: searchKeyword.value })
    if (res.code === 200) {
      searchResults.value = res.data || []
    }
  } catch (error) {
    console.error('搜索失败', error)
  }
}

// 获取热门专业
const fetchHotMajors = async () => {
  try {
    const res = await request.get('/major/hot', { limit: 10 })
    if (res.code === 200) {
      hotMajors.value = res.data || []
    }
  } catch (error) {
    console.error('获取热门专业失败', error)
  }
}

const getUniversityLevelClass = (level) => {
  const map = {
    '985': 'bg-amber-100 text-amber-700 dark:bg-black dark:text-amber-400',
    '211': 'bg-emerald-100 text-emerald-700 dark:bg-black dark:text-emerald-400',
    '双一流': 'bg-blue-100 text-blue-700 dark:bg-black dark:text-blue-400',
    '普通': 'bg-gray-100 text-gray-600 dark:bg-black dark:text-gray-400'
  }
  return map[level] || 'bg-gray-100 text-gray-600 dark:bg-black dark:text-gray-400'
}

const getMatchingScoreColor = (score) => {
  if (score >= 80) return 'from-green-500 to-emerald-500'
  if (score >= 60) return 'from-blue-500 to-cyan-500'
  if (score >= 40) return 'from-yellow-500 to-amber-500'
  return 'from-gray-500 to-gray-600'
}

watch(() => props.studentId, (val) => {
  if (val) {
    fetchCurrentSelection()
    fetchHotMajors()
  }
}, { immediate: true })
</script>

<template>
  <div class="cs-panel space-y-6">
    <!-- 当前选科组合 -->
    <div v-if="currentSelection" class="bg-gradient-to-r from-indigo-500/10 to-purple-500/10 dark:from-indigo-500/5 dark:to-purple-500/5 rounded-2xl p-5 border border-indigo-200/30 dark:border-indigo-500/20">
      <div class="flex items-center gap-3 mb-3">
        <span class="text-2xl">🎯</span>
        <span class="font-semibold text-gray-800 dark:text-gray-200">当前选科组合</span>
        <span class="px-2 py-0.5 rounded-full text-xs bg-indigo-100 dark:bg-black text-indigo-600 dark:text-indigo-400">已选</span>
      </div>
      <div class="flex flex-wrap gap-3 mt-3">
        <span class="px-4 py-2 rounded-full bg-gradient-to-r from-indigo-500 to-indigo-600 text-white font-medium shadow-md">
          {{ currentSelection.firstSubjectName }}
        </span>
        <span class="px-4 py-2 rounded-full bg-gradient-to-r from-purple-500 to-purple-600 text-white font-medium shadow-md">
          {{ currentSelection.secondSubject1Name }}
        </span>
        <span class="px-4 py-2 rounded-full bg-gradient-to-r from-pink-500 to-pink-600 text-white font-medium shadow-md">
          {{ currentSelection.secondSubject2Name }}
        </span>
      </div>
      <p class="text-sm text-gray-600 dark:text-gray-400 mt-3 flex items-center gap-2">
        <span class="text-base">📋</span>
        组合名称：{{ currentSelection.combinationName }}
      </p>
    </div>

    <!-- 推荐专业 -->
    <div>
      <div class="flex items-center gap-3 mb-4">
        <span class="text-2xl">🎓</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-200">推荐专业</h3>
        <span class="px-2 py-0.5 rounded-full text-xs bg-green-100 dark:bg-black text-green-600 dark:text-green-400">智能匹配</span>
      </div>

      <div v-if="loading" class="flex justify-center py-12">
        <div class="flex flex-col items-center gap-3">
          <div class="w-10 h-10 border-3 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
          <p class="text-sm text-gray-500 dark:text-gray-400">正在分析匹配专业...</p>
        </div>
      </div>

      <div v-else-if="recommendedMajors.length === 0" class="text-center py-12 bg-gray-50/50 dark:bg-black rounded-2xl">
        <span class="text-5xl opacity-50">📭</span>
        <p class="mt-3 text-gray-500 dark:text-gray-400">暂无推荐专业，请先完成选课</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="major in recommendedMajors" :key="major.id"
             class="group bg-white/60 dark:bg-black backdrop-blur-sm rounded-xl border border-gray-200/50 dark:border-gray-700/50 p-4 hover:shadow-xl hover:-translate-y-1 transition-all duration-300">
          <div class="flex items-start justify-between mb-3">
            <div class="flex-1">
              <div class="font-bold text-lg text-gray-800 dark:text-gray-200 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                {{ major.majorName }}
              </div>
              <div class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{{ major.universityName }}</div>
            </div>
            <span class="px-2.5 py-1 rounded-full text-xs font-medium" :class="getUniversityLevelClass(major.universityLevel)">
              {{ major.universityLevel }}
            </span>
          </div>

          <div class="space-y-2 text-sm">
            <div class="flex items-center gap-2 text-gray-600 dark:text-gray-300">
              <span>📚</span>
              <span>专业类别：{{ major.category }}</span>
            </div>
            <div class="flex items-center gap-2 text-gray-600 dark:text-gray-300">
              <span>📖</span>
              <span>选科要求：{{ major.firstSubjectRequired }} + {{ major.secondSubjectRequired }}</span>
            </div>
            <div v-if="major.matchedSubjects" class="flex items-center gap-2 text-green-600 dark:text-green-400">
              <span>✅</span>
              <span>匹配科目：{{ major.matchedSubjects }}</span>
            </div>
            <div v-if="major.avgMatchingScore" class="mt-2">
              <div class="flex justify-between items-center mb-1">
                <span class="text-xs text-gray-500 dark:text-gray-400">匹配度</span>
                <span class="text-sm font-semibold" :class="major.avgMatchingScore >= 60 ? 'text-green-600 dark:text-green-400' : 'text-yellow-600'">
                  {{ major.avgMatchingScore }}%
                </span>
              </div>
              <div class="h-2 bg-gray-200 dark:bg-black rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r rounded-full transition-all duration-500"
                     :class="getMatchingScoreColor(major.avgMatchingScore)"
                     :style="{ width: major.avgMatchingScore + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 按专业搜索 -->
    <div class="bg-white/60 dark:bg-black backdrop-blur-sm rounded-2xl border border-gray-200/50 dark:border-gray-700/50 overflow-hidden">
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-blue-50/50 to-cyan-50/50 dark:from-blue-950/20 dark:to-cyan-950/20 border-b border-gray-200/50 dark:border-gray-700/50">
        <span class="text-2xl">🔍</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-200">搜索专业</h3>
        <span class="ml-auto text-xs px-2 py-1 rounded-full bg-blue-100 dark:bg-black text-blue-600 dark:text-blue-400">全国高校</span>
      </div>

      <div class="p-5">
        <div class="flex gap-3">
          <input v-model="searchKeyword" type="text"
                 class="flex-1 px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-black text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
                 placeholder="输入专业名称、专业代码或类别..." @keyup.enter="searchMajors">
          <button @click="searchMajors"
                  class="px-6 py-3 rounded-xl bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 text-white font-medium shadow-md hover:shadow-lg transition-all duration-200">
            🔍 搜索
          </button>
        </div>

        <div v-if="searchResults.length > 0" class="mt-5 space-y-3">
          <div v-for="major in searchResults" :key="major.id"
               class="p-4 rounded-xl bg-gray-50/50 dark:bg-black border border-gray-200/50 dark:border-gray-700/50 hover:border-indigo-300 dark:hover:border-indigo-700 transition-all">
            <div class="flex items-start justify-between flex-wrap gap-2">
              <div class="flex-1">
                <div class="font-semibold text-gray-800 dark:text-gray-200">{{ major.majorName }}</div>
                <div class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{{ major.universityName }}</div>
              </div>
              <span class="text-xs font-mono text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-black px-2 py-1 rounded">{{ major.majorCode }}</span>
            </div>
            <div class="text-sm mt-2 flex items-center gap-2 text-gray-600 dark:text-gray-300">
              <span>📖</span>
              <span>选科要求：{{ major.firstSubjectRequired }} + {{ major.secondSubjectRequired }}</span>
            </div>
          </div>
        </div>

        <div v-else-if="searchKeyword && searchResults.length === 0" class="mt-5 text-center py-8 text-gray-500 dark:text-gray-400">
          <span class="text-4xl opacity-50">🔍</span>
          <p class="mt-2">未找到相关专业，请尝试其他关键词</p>
        </div>
      </div>
    </div>

    <!-- 热门专业排行 -->
    <div>
      <div class="flex items-center gap-3 mb-4">
        <span class="text-2xl">🔥</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-200">热门专业排行</h3>
        <span class="px-2 py-0.5 rounded-full text-xs bg-orange-100 dark:bg-black text-orange-600 dark:text-orange-400">实时热度</span>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div v-for="(major, index) in hotMajors" :key="index"
             class="group flex items-center gap-4 p-4 rounded-xl bg-white/60 dark:bg-black backdrop-blur-sm border border-gray-200/50 dark:border-gray-700/50 hover:shadow-lg hover:-translate-y-0.5 transition-all duration-200">
          <div class="flex-shrink-0">
            <div v-if="index === 0" class="w-10 h-10 rounded-full bg-gradient-to-r from-yellow-400 to-amber-500 text-white flex items-center justify-center text-lg shadow-lg">🥇</div>
            <div v-else-if="index === 1" class="w-10 h-10 rounded-full bg-gradient-to-r from-gray-400 to-gray-500 text-white flex items-center justify-center text-lg shadow-lg">🥈</div>
            <div v-else-if="index === 2" class="w-10 h-10 rounded-full bg-gradient-to-r from-amber-600 to-amber-700 text-white flex items-center justify-center text-lg shadow-lg">🥉</div>
            <div v-else class="w-10 h-10 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white flex items-center justify-center font-bold shadow-md">{{ index + 1 }}</div>
          </div>
          <div class="flex-1">
            <div class="font-semibold text-gray-800 dark:text-gray-200 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
              {{ major.major_name }}
            </div>
            <div class="text-sm text-gray-500 dark:text-gray-400">{{ major.category }}</div>
          </div>
          <div class="text-right">
            <div class="text-sm font-semibold text-indigo-600 dark:text-indigo-400">{{ major.count }}所院校</div>
            <div class="text-xs text-gray-400 dark:text-gray-500">开设院校</div>
          </div>
        </div>
      </div>

      <div v-if="hotMajors.length === 0" class="text-center py-8 bg-gray-50/50 dark:bg-black rounded-2xl">
        <span class="text-4xl opacity-50">🔥</span>
        <p class="mt-2 text-gray-500 dark:text-gray-400">暂无热门专业数据</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes spin {
  to { transform: rotate(360deg); }
}

.animate-spin {
  animation: spin 0.8s linear infinite;
}

.border-3 {
  border-width: 3px;
}
</style>
