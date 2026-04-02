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

onMounted(() => {
  fetchStatistics()
})
</script>
<template>
  <div class="space-y-6">
    <!-- 年级选择 -->
    <div class="flex gap-2">
      <select v-model="selectedGrade" class="px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800">
        <option value="高一">高一</option>
        <option value="高二">高二</option>
        <option value="高三">高三</option>
      </select>
      <select v-model="selectedYear" class="px-4 py-2 rounded-xl border dark:border-slate-600 dark:bg-slate-800">
        <option value="2024-2025">2024-2025学年</option>
        <option value="2023-2024">2023-2024学年</option>
      </select>
      <button @click="fetchStatistics" class="px-4 py-2 rounded-xl bg-indigo-600 text-white">查询</button>
    </div>

    <!-- 热门组合排名 -->
    <div class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">🔥 热门组合排名</h3>
      <div class="space-y-3">
        <div v-for="(item, index) in hotCombinations" :key="index"
             class="flex items-center gap-3 p-2 hover:bg-slate-50 dark:hover:bg-slate-800/40 rounded-lg transition-all">
          <div class="w-8 h-8 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white flex items-center justify-center font-bold">
            {{ index + 1 }}
          </div>
          <div class="flex-1">
            <div class="font-medium">{{ item.name || item.combination_name }}</div>
            <div class="text-sm text-slate-500">选课人数: {{ item.selection_count }}人</div>
          </div>
          <div class="text-right">
            <div class="text-sm font-medium">平均分: {{ item.avg_score || '-' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 首选科目分布 -->
    <div class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">📊 首选科目分布</h3>
      <div class="space-y-3">
        <div v-for="item in firstSubjectStats" :key="item.first_subject_name" class="flex items-center gap-3">
          <div class="w-24 font-medium">{{ item.first_subject_name }}</div>
          <div class="flex-1 h-8 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
            <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full flex items-center justify-end pr-3 text-white text-xs"
                 :style="{ width: item.percentage + '%' }">
              {{ item.percentage }}%
            </div>
          </div>
          <div class="w-16 text-right">{{ item.count }}人</div>
        </div>
      </div>
    </div>



    <!-- 选课趋势图 -->
    <div class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">📈 选课趋势</h3>
      <div class="h-64 flex items-end gap-2">
        <div v-for="item in trendData" :key="item.month" class="flex-1 flex flex-col items-center gap-2">
          <div class="w-full bg-indigo-500/20 rounded-t-lg overflow-hidden"
               :style="{ height: (item.selection_count / maxCount * 200) + 'px' }">
            <div class="w-full h-full bg-gradient-to-t from-indigo-500 to-purple-500"
                 :style="{ height: (item.selection_count / maxCount * 100) + '%' }"></div>
          </div>
          <div class="text-xs text-slate-500">{{ item.month }}</div>
          <div class="text-xs font-medium">{{ item.selection_count }}</div>
        </div>
      </div>
    </div>

    <!-- 年级排名前10 -->
    <div class="rounded-xl bg-white/70 dark:bg-white/10 backdrop-blur-sm border border-white/30 p-4">
      <h3 class="text-lg font-semibold mb-3">🎖️ 年级排名前10</h3>
      <div class="space-y-2">
        <div v-for="(student, index) in topStudents" :key="student.student_id"
             class="flex items-center gap-3 p-2 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800/40">
          <div class="w-8 font-bold text-indigo-600">#{{ index + 1 }}</div>
          <div class="flex-1">
            <div class="font-medium">{{ student.student_name }}</div>
            <div class="text-xs text-slate-500">{{ student.combination_name }}</div>
          </div>
          <div class="text-right">
            <div class="font-bold text-lg">{{ student.total_score_weighted }}</div>
            <div class="text-xs text-slate-500">赋分总分</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
