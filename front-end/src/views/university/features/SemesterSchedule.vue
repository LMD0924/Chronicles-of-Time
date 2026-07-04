<!--
  文件说明：拾光记前台应用大学阶段页面组件，承载大学阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const props = defineProps({ majorId: Number, isDark: Boolean })
const router = useRouter()
const schedule = ref([])
const loading = ref(false)

const statusClass = (item) => {
  if (item.isPassed === 1) return 'bg-green-100 text-green-700 dark:bg-green-950/40 dark:text-green-400'
  if (item.studentStatus === 'studying') return 'bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400'
  if (item.studentStatus === 'failed') return 'bg-red-100 text-red-700 dark:bg-red-950/40 dark:text-red-400'
  return 'bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400'
}

const statusLabel = (item) => {
  if (item.isPassed === 1) return '已通过'
  if (item.studentStatus === 'studying') return '修读中'
  if (item.studentStatus === 'failed') return '未通过'
  return '未修读'
}

const load = async () => {
  if (!props.majorId) return
  loading.value = true
  try {
    const res = await request.get('/student-course/schedule', { majorId: props.majorId })
    if (res.code === 200) schedule.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openInTree = (courseId) => {
  router.push({ path: '/CourseTree', query: { highlight: courseId } })
}

watch(() => props.majorId, load, { immediate: true })
onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div v-if="loading" class="app-card-surface p-8 text-center text-zinc-500">加载中...</div>
    <div v-for="sem in schedule" :key="sem.term" class="app-card-surface overflow-hidden">
      <div class="px-5 py-3 border-b flex justify-between items-center" :class="isDark ? 'border-dark-border bg-zinc-900/50' : 'border-zinc-100 bg-brand-50/30'">
        <h3 class="font-semibold text-brand-700 dark:text-brand-300">{{ sem.termLabel }}</h3>
        <span class="text-xs text-zinc-500">共 {{ sem.totalCredits }} 学分 · {{ sem.courses?.length || 0 }} 门课</span>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-left text-zinc-500 border-b" :class="isDark ? 'border-dark-border' : 'border-zinc-100'">
              <th class="px-4 py-2">代码</th>
              <th class="px-4 py-2">课程</th>
              <th class="px-4 py-2">学分</th>
              <th class="px-4 py-2">类型</th>
              <th class="px-4 py-2">状态</th>
              <th class="px-4 py-2">成绩</th>
              <th class="px-4 py-2"></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="c in sem.courses"
              :key="c.courseId"
              class="border-b hover:bg-brand-50/30 dark:hover:bg-brand-950/20 transition-colors"
              :class="isDark ? 'border-dark-border' : 'border-zinc-50'"
            >
              <td class="px-4 py-2 font-mono text-xs">{{ c.courseCode || '-' }}</td>
              <td class="px-4 py-2 font-medium">{{ c.name }}</td>
              <td class="px-4 py-2">{{ c.credit }}</td>
              <td class="px-4 py-2">{{ c.courseType === 'compulsory' ? '必修' : '选修' }}</td>
              <td class="px-4 py-2">
                <span class="px-2 py-0.5 rounded-full text-xs" :class="statusClass(c)">{{ statusLabel(c) }}</span>
              </td>
              <td class="px-4 py-2">{{ c.score ?? '-' }}</td>
              <td class="px-4 py-2">
                <button type="button" class="text-brand-600 text-xs hover:underline" @click="openInTree(c.courseId)">在课程树查看</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div v-if="!loading && !schedule.length" class="app-card-surface p-8 text-center text-zinc-500">暂无课程，请先在课程树添加</div>
  </div>
</template>
