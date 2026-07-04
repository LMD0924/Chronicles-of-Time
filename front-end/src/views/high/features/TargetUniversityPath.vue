<!--
  文件说明：拾光记前台应用高中阶段页面组件，承载高中阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

defineProps({ isDark: Boolean })
const router = useRouter()
const userId = ref(null)
const plans = ref([])
const scores = ref([])
const targetGap = ref([])
const loadError = ref('')

const load = async () => {
  loadError.value = ''
  try {
    const u = await request.get('/user/getUserById')
    if (u.code === 200) userId.value = u.data?.id
    if (!userId.value) {
      loadError.value = '未获取到用户信息'
      return
    }

    const planRes = await request.get(`/volunteer/plan/list/${userId.value}`).catch(() => ({ code: 0 }))
    if (planRes.code === 200) plans.value = planRes.data || []

    let scoreList = []
    try {
      const scoreRes = await request.get(`/score/list/${userId.value}`)
      if (scoreRes.code === 200) scoreList = scoreRes.data || []
    } catch {
      /* 无成绩记录时静默 */
    }

    scores.value = scoreList.slice().sort((a, b) => new Date(a.examDate || 0) - new Date(b.examDate || 0))
    const target = plans.value[0]?.targetScore ?? plans.value[0]?.minScore
    if (target != null && scores.value.length) {
      targetGap.value = scores.value.map((s) => {
        const sc = Number(s.totalScore ?? s.score ?? 0)
        return {
          label: s.examName || s.examDate || '考试',
          score: sc,
          gap: Number(target) - sc,
        }
      })
    }
  } catch (e) {
    console.error(e)
    loadError.value = '数据加载失败'
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <p v-if="loadError" class="text-sm text-amber-600">{{ loadError }}</p>
    <div class="flex gap-3">
      <button type="button" class="app-btn-primary text-sm" @click="router.push('/Volunteer')">打开志愿模块</button>
      <button type="button" class="app-btn-secondary text-sm" @click="router.push('/StudyDashboard?tab=analysis')">查看成绩分析</button>
    </div>
    <div class="grid md:grid-cols-2 gap-4">
      <div class="app-card-surface p-5">
        <h3 class="font-semibold mb-3">志愿方案</h3>
        <div v-for="p in plans" :key="p.id" class="py-3 border-b last:border-0 text-sm">
          <p class="font-medium">{{ p.planName || p.name || '方案' }}</p>
          <p class="text-zinc-500">目标分：{{ p.targetScore ?? p.minScore ?? '未设置' }}</p>
        </div>
        <p v-if="!plans.length" class="text-zinc-500 text-sm">暂无志愿方案，请先在志愿填报中创建</p>
      </div>
      <div class="app-card-surface p-5">
        <h3 class="font-semibold mb-3">模考与目标校分差</h3>
        <ul class="space-y-2">
          <li v-for="(item, i) in targetGap" :key="i" class="flex justify-between text-sm">
            <span>{{ item.label }}</span>
            <span>
              {{ item.score }} 分
              <span :class="item.gap <= 0 ? 'text-green-600' : 'text-red-500'" class="ml-2">
                {{ item.gap <= 0 ? '已达标' : `差 ${item.gap} 分` }}
              </span>
            </span>
          </li>
        </ul>
        <p v-if="!targetGap.length" class="text-zinc-500 text-sm">在学习仪表盘录入模考成绩，并在志愿方案中设置目标分后显示</p>
      </div>
    </div>
  </div>
</template>
