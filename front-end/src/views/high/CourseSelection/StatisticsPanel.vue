<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  isDark: Boolean
})

const selectedGrade = ref('高一')
const selectedYear = ref('2024-2025')
const hotCombinations = ref([])
const firstSubjectStats = ref([])
const trendData = ref([])
const topStudents = ref([])
const maxCount = ref(1)

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const [hotRes, firstRes, trendRes, topRes] = await Promise.all([
      request.get('/selection/hot-combinations'),
      request.get('/selection/statistics/grade', { grade: selectedGrade.value, academicYear: selectedYear.value }),
      request.get('/selection/trend'),
      request.get('/selection/top-students', { grade: selectedGrade.value, limit: 10 })
    ])

    if (hotRes.code === 200) hotCombinations.value = hotRes.data
    if (firstRes.code === 200) firstSubjectStats.value = firstRes.data.firstSubjectStats || []
    if (trendRes.code === 200) {
      trendData.value = trendRes.data
      maxCount.value = Math.max(...trendData.value.map(t => t.selection_count), 1)
    }
    if (topRes.code === 200) topStudents.value = topRes.data
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const getRankColor = (index) => {
  const colors = {
    0: 'from-yellow-400 to-amber-500',
    1: 'from-gray-400 to-gray-500',
    2: 'from-amber-600 to-amber-700',
  }
  return colors[index] || 'from-indigo-500 to-purple-500'
}

onMounted(() => {
  fetchStatistics()
})
</script>

<template>
  <div class="cs-panel space-y-6">
    <!-- 筛选栏 -->
    <div class="bg-white/60 dark:bg-black backdrop-blur-sm rounded-2xl p-4 border border-white/30 dark:border-gray-700/50">
      <div class="flex flex-wrap gap-3 items-end">
        <div class="flex-1 min-w-[120px]">
          <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">年级</label>
          <select v-model="selectedGrade" class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-black text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all">
            <option value="高一">高一</option>
            <option value="高二">高二</option>
            <option value="高三">高三</option>
          </select>
        </div>
        <div class="flex-1 min-w-[160px]">
          <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">学年</label>
          <select v-model="selectedYear" class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-black text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all">
            <option value="2024-2025">2024-2025学年</option>
            <option value="2023-2024">2023-2024学年</option>
          </select>
        </div>
        <button @click="fetchStatistics" class="px-6 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 text-white font-medium shadow-md hover:shadow-lg transition-all duration-200">
          🔍 查询
        </button>
      </div>
    </div>

    <!-- 热门组合排名 -->
    <div class="bg-white/60 dark:bg-black backdrop-blur-sm rounded-2xl border border-white/30 dark:border-gray-700/50 overflow-hidden">
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-orange-50/50 to-amber-50/50 dark:from-orange-950/20 dark:to-amber-950/20 border-b border-gray-200/50 dark:border-gray-700/50">
        <span class="text-2xl">🔥</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100">热门组合排名</h3>
        <span class="ml-auto text-xs px-2 py-1 rounded-full bg-orange-100 dark:bg-black text-orange-600 dark:text-orange-400">实时热度</span>
      </div>
      <div class="divide-y divide-gray-200/50 dark:divide-gray-700/50">
        <div v-for="(item, index) in hotCombinations" :key="index"
             class="p-4 flex flex-wrap items-center gap-4 hover:bg-gray-50/50 dark:hover:bg-white/5 transition-all">
          <div class="flex-shrink-0">
            <div v-if="index === 0" class="w-10 h-10 rounded-full bg-gradient-to-r from-yellow-400 to-amber-500 text-white flex items-center justify-center font-bold shadow-lg">🥇</div>
            <div v-else-if="index === 1" class="w-10 h-10 rounded-full bg-gradient-to-r from-gray-400 to-gray-500 text-white flex items-center justify-center font-bold shadow-lg">🥈</div>
            <div v-else-if="index === 2" class="w-10 h-10 rounded-full bg-gradient-to-r from-amber-600 to-amber-700 text-white flex items-center justify-center font-bold shadow-lg">🥉</div>
            <div v-else class="w-10 h-10 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white flex items-center justify-center font-bold shadow-md">{{ index + 1 }}</div>
          </div>
          <div class="flex-1 min-w-[150px]">
            <div class="font-semibold text-gray-800 dark:text-gray-200">{{ item.name || item.combination_name }}</div>
            <div class="text-sm text-gray-500 dark:text-gray-400">选课人数: {{ item.selection_count }}人</div>
          </div>
          <div class="text-right">
            <div class="text-sm font-medium text-gray-600 dark:text-gray-300">平均分</div>
            <div class="text-xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">{{ item.avg_score || '-' }}</div>
          </div>
        </div>
        <div v-if="hotCombinations.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">
          <span class="text-4xl opacity-50">📊</span>
          <p class="mt-2">暂无数据</p>
        </div>
      </div>
    </div>

    <!-- 首选科目分布 -->
    <div class="bg-white/60 dark:bg-black backdrop-blur-sm rounded-2xl border border-white/30 dark:border-gray-700/50 overflow-hidden">
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-blue-50/50 to-cyan-50/50 dark:from-blue-950/20 dark:to-cyan-950/20 border-b border-gray-200/50 dark:border-gray-700/50">
        <span class="text-2xl">📊</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100">首选科目分布</h3>
        <span class="ml-auto text-xs px-2 py-1 rounded-full bg-blue-100 dark:bg-black text-blue-600 dark:text-blue-400">比例分析</span>
      </div>
      <div class="p-5 space-y-4">
        <div v-for="item in firstSubjectStats" :key="item.first_subject_name" class="space-y-2">
          <div class="flex justify-between items-center">
            <span class="font-medium text-gray-700 dark:text-gray-300">{{ item.first_subject_name }}</span>
            <div class="flex items-center gap-3">
              <span class="text-sm text-gray-500 dark:text-gray-400">{{ item.count }}人</span>
              <span class="text-sm font-semibold text-indigo-600 dark:text-indigo-400">{{ item.percentage }}%</span>
            </div>
          </div>
          <div class="relative h-3 bg-gray-200 dark:bg-black rounded-full overflow-hidden">
            <div class="absolute inset-y-0 left-0 bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full transition-all duration-500"
                 :style="{ width: item.percentage + '%' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 选课趋势图 -->
    <div class="bg-white/60 dark:bg-black backdrop-blur-sm rounded-2xl border border-white/30 dark:border-gray-700/50 overflow-hidden">
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-green-50/50 to-emerald-50/50 dark:from-green-950/20 dark:to-emerald-950/20 border-b border-gray-200/50 dark:border-gray-700/50">
        <span class="text-2xl">📈</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100">选课趋势</h3>
        <span class="ml-auto text-xs px-2 py-1 rounded-full bg-green-100 dark:bg-black text-green-600 dark:text-green-400">月度统计</span>
      </div>
      <div class="p-5">
        <div class="h-64 flex items-end gap-2">
          <div v-for="item in trendData" :key="item.month" class="flex-1 flex flex-col items-center gap-2 group">
            <div class="relative w-full">
              <div class="w-full bg-indigo-200/50 dark:bg-black rounded-t-lg transition-all duration-300"
                   :style="{ height: (item.selection_count / maxCount * 200) + 'px' }">
                <div class="w-full bg-gradient-to-t from-indigo-500 to-purple-500 rounded-t-lg transition-all duration-300 hover:opacity-80"
                     :style="{ height: (item.selection_count / maxCount * 100) + '%' }"></div>
              </div>
              <!-- 悬停提示 -->
              <div class="absolute -top-8 left-1/2 transform -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                <div class="bg-gray-800 text-white text-xs px-2 py-1 rounded whitespace-nowrap">{{ item.selection_count }}人</div>
              </div>
            </div>
            <div class="text-xs text-gray-500 dark:text-gray-400 font-medium">{{ item.month }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 年级排名前10 -->
    <div class="bg-white/60 dark:bg-black backdrop-blur-sm rounded-2xl border border-white/30 dark:border-gray-700/50 overflow-hidden">
      <div class="flex items-center gap-3 px-5 py-4 bg-gradient-to-r from-purple-50/50 to-pink-50/50 dark:from-purple-950/20 dark:to-pink-950/20 border-b border-gray-200/50 dark:border-gray-700/50">
        <span class="text-2xl">🎖️</span>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100">年级排名前10</h3>
        <span class="ml-auto text-xs px-2 py-1 rounded-full bg-purple-100 dark:bg-black text-purple-600 dark:text-purple-400">赋分总分</span>
      </div>
      <div class="divide-y divide-gray-200/50 dark:divide-gray-700/50">
        <div v-for="(student, index) in topStudents" :key="student.student_id"
             class="p-4 flex flex-wrap items-center gap-4 hover:bg-gray-50/50 dark:hover:bg-white/5 transition-all group">
          <div class="flex-shrink-0 w-12">
            <div v-if="index === 0" class="text-2xl">👑</div>
            <div v-else class="w-8 h-8 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white flex items-center justify-center text-sm font-bold shadow-md">{{ index + 1 }}</div>
          </div>
          <div class="flex-1 min-w-[150px]">
            <div class="font-semibold text-gray-800 dark:text-gray-200 flex items-center gap-2">
              {{ student.student_name }}
              <span v-if="index === 0" class="text-xs px-2 py-0.5 rounded-full bg-yellow-100 dark:bg-black text-yellow-700 dark:text-yellow-400">学霸</span>
            </div>
            <div class="text-sm text-gray-500 dark:text-gray-400">{{ student.combination_name }}</div>
          </div>
          <div class="text-right">
            <div class="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">{{ student.total_score_weighted }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400">赋分总分</div>
          </div>
        </div>
        <div v-if="topStudents.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">
          <span class="text-4xl opacity-50">🏆</span>
          <p class="mt-2">暂无排名数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 自定义过渡动画 */
.transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}

/* 悬停效果增强 */
.group:hover .group-hover\:opacity-100 {
  opacity: 1;
}

/* 进度条动画 */
@keyframes slideIn {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.absolute.inset-y-0 {
  animation: slideIn 0.6s ease-out;
}
</style>
