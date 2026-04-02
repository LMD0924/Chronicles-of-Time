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
    // 不依赖 advice 接口，直接用当前选科组合去匹配专业
    // MajorRequirementController: GET /api/major/match
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
    '985': 'bg-amber-100 text-amber-600',
    '211': 'bg-emerald-100 text-emerald-600',
    '双一流': 'bg-blue-100 text-blue-600',
    '普通': 'bg-gray-100 text-gray-600'
  }
  return map[level] || 'bg-gray-100 text-gray-600'
}

watch(() => props.studentId, (val) => {
  if (val) {
    fetchCurrentSelection()
    fetchHotMajors()
  }
}, { immediate: true })
</script>
<template>
  <div class="space-y-6">
    <!-- 当前选科组合 -->
    <div v-if="currentSelection" class="rounded-xl bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-950/30 dark:to-purple-950/30 p-4">
      <div class="flex items-center gap-2 mb-2">
        <span class="text-xl">🎯</span>
        <span class="font-semibold">当前选科组合</span>
      </div>
      <div class="flex flex-wrap gap-3 mt-2">
        <span class="px-3 py-1 rounded-full bg-indigo-100 dark:bg-indigo-900/40">{{ currentSelection.firstSubjectName }}</span>
        <span class="px-3 py-1 rounded-full bg-indigo-100 dark:bg-indigo-900/40">{{ currentSelection.secondSubject1Name }}</span>
        <span class="px-3 py-1 rounded-full bg-indigo-100 dark:bg-indigo-900/40">{{ currentSelection.secondSubject2Name }}</span>
      </div>
      <p class="text-sm text-slate-600 dark:text-slate-300 mt-2">组合名称：{{ currentSelection.combinationName }}</p>
    </div>

    <!-- 推荐专业 -->
    <div>
      <h3 class="text-lg font-semibold mb-3">🎓 推荐专业</h3>
      <div v-if="loading" class="flex justify-center py-8">
        <div class="w-8 h-8 border-2 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
      </div>
      <div v-else-if="recommendedMajors.length === 0" class="text-center py-8 text-slate-500">
        暂无推荐专业，请先完成选课
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="major in recommendedMajors" :key="major.id"
             class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4 hover:shadow-lg transition-all">
          <div class="flex items-start justify-between mb-2">
            <div>
              <div class="font-semibold text-lg">{{ major.majorName }}</div>
              <div class="text-sm text-slate-500">{{ major.universityName }}</div>
            </div>
            <span class="px-2 py-1 rounded-full text-xs" :class="getUniversityLevelClass(major.universityLevel)">
              {{ major.universityLevel }}
            </span>
          </div>
          <div class="text-sm text-slate-600 dark:text-slate-300 mt-2">
            <div>📚 专业类别：{{ major.category }}</div>
            <div>📖 选科要求：{{ major.firstSubjectRequired }} + {{ major.secondSubjectRequired }}</div>
            <div v-if="major.matchedSubjects" class="mt-1">
              🎯 匹配科目：{{ major.matchedSubjects }}
            </div>
            <div v-if="major.avgMatchingScore" class="mt-1">
              匹配度：{{ major.avgMatchingScore }}%
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 按专业搜索 -->
    <div class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">🔍 搜索专业</h3>
      <div class="flex gap-3">
        <input v-model="searchKeyword" type="text"
               class="flex-1 px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800"
               placeholder="输入专业名称、专业代码或类别" @keyup.enter="searchMajors">
        <button @click="searchMajors"
                class="px-6 py-2 rounded-xl bg-indigo-600 text-white hover:bg-indigo-700 transition-all">
          搜索
        </button>
      </div>

      <div v-if="searchResults.length > 0" class="mt-4 space-y-3">
        <div v-for="major in searchResults" :key="major.id"
             class="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/40">
          <div class="flex items-start justify-between">
            <div>
              <div class="font-medium">{{ major.majorName }}</div>
              <div class="text-sm text-slate-500">{{ major.universityName }}</div>
            </div>
            <span class="text-xs text-slate-500">{{ major.majorCode }}</span>
          </div>
          <div class="text-sm mt-1">
            <span class="text-slate-500">选科要求：</span>
            {{ major.firstSubjectRequired }} + {{ major.secondSubjectRequired }}
          </div>
        </div>
      </div>
    </div>

    <!-- 热门专业排行 -->
    <div>
      <h3 class="text-lg font-semibold mb-3">🔥 热门专业排行</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div v-for="(major, index) in hotMajors" :key="index"
             class="flex items-center gap-3 p-3 rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30">
          <div class="w-8 h-8 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white flex items-center justify-center font-bold">
            {{ index + 1 }}
          </div>
          <div class="flex-1">
            <div class="font-medium">{{ major.major_name }}</div>
            <div class="text-sm text-slate-500">{{ major.category }}</div>
          </div>
          <div class="text-sm text-indigo-600">{{ major.count }}所院校</div>
        </div>
      </div>
    </div>
  </div>
</template>
