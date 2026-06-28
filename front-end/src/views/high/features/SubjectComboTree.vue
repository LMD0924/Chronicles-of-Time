<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import request from '@/utils/request'

defineProps({ isDark: Boolean })
const [messageApi] = message.useMessage()

const combinations = ref([])
const subjects = ref({ first: [], second: [] })
const selected = ref({ first: null, second: [] })
const matchMajors = ref([])
const matchLoading = ref(false)

const subjectLabel = (s) => s?.name || s?.subjectName || ''

const load = async () => {
  try {
    const [hot, s1, s2] = await Promise.all([
      request.get('/selection/hot-combinations'),
      request.get('/subject/first'),
      request.get('/subject/second'),
    ])
    if (hot.code === 200) combinations.value = hot.data || []
    if (s1.code === 200) subjects.value.first = s1.data || []
    if (s2.code === 200) subjects.value.second = s2.data || []
  } catch (e) {
    console.error(e)
    messageApi.warning('选科数据加载失败')
  }
}

const toggleFirst = (s) => {
  selected.value.first = s
}

const toggleSecond = (s) => {
  const id = s.id
  const idx = selected.value.second.findIndex((x) => x.id === id)
  if (idx >= 0) {
    selected.value.second.splice(idx, 1)
  } else if (selected.value.second.length < 2) {
    selected.value.second.push(s)
  }
}

const isSecondSelected = (s) => selected.value.second.some((x) => x.id === s.id)

const checkMatch = async () => {
  if (!selected.value.first?.id) {
    messageApi.warning('请先选择首选科目（物理/历史）')
    return
  }
  if (selected.value.second.length !== 2) {
    messageApi.warning('请再选择 2 门再选科目')
    return
  }
  matchLoading.value = true
  matchMajors.value = []
  try {
    const res = await request.get('/major/match', {
      firstSubject: subjectLabel(selected.value.first),
      subject1Id: selected.value.first.id,
      subject2Id: selected.value.second[0].id,
      subject3Id: selected.value.second[1].id,
      limit: 20,
    })
    if (res.code === 200) {
      matchMajors.value = res.data || []
    } else {
      messageApi.error(res.message || '匹配失败')
    }
  } catch (e) {
    console.error(e)
    messageApi.error('专业匹配请求失败，请确认已登录且选科完整')
  } finally {
    matchLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="grid lg:grid-cols-2 gap-6">
    <div class="app-card-surface p-5">
      <h3 class="font-semibold mb-4">3+1+2 选科组合树</h3>
      <div class="mb-4">
        <p class="text-xs text-zinc-500 mb-2">首选科目（1）</p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="s in subjects.first"
            :key="s.id"
            type="button"
            class="px-3 py-1.5 rounded-full text-sm border transition-all"
            :class="selected.first?.id === s.id ? 'bg-brand-500 text-white border-brand-500' : 'border-zinc-200'"
            @click="toggleFirst(s)"
          >
            {{ subjectLabel(s) }}
          </button>
        </div>
      </div>
      <div class="mb-4">
        <p class="text-xs text-zinc-500 mb-2">再选科目（2）</p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="s in subjects.second"
            :key="s.id"
            type="button"
            class="px-3 py-1.5 rounded-full text-sm border transition-all"
            :class="isSecondSelected(s) ? 'bg-brand-500 text-white border-brand-500' : 'border-zinc-200'"
            @click="toggleSecond(s)"
          >
            {{ subjectLabel(s) }}
          </button>
        </div>
      </div>
      <button type="button" class="app-btn-primary w-full" :disabled="matchLoading" @click="checkMatch">
        {{ matchLoading ? '匹配中...' : '查看可报专业范围' }}
      </button>
    </div>
    <div class="space-y-4">
      <div class="app-card-surface p-5">
        <h3 class="font-semibold mb-3">热门组合</h3>
        <ul class="space-y-2 text-sm">
          <li v-for="(c, i) in combinations.slice(0, 8)" :key="i" class="flex justify-between py-2 border-b last:border-0">
            <span>{{ c.combinationName || c.combination || c.name || '组合' }}</span>
            <span class="text-brand-600">{{ c.studentCount || c.count || '-' }} 人</span>
          </li>
        </ul>
      </div>
      <div v-if="matchMajors.length" class="app-card-surface p-5 max-h-64 overflow-y-auto">
        <h3 class="font-semibold mb-3">匹配专业 ({{ matchMajors.length }})</h3>
        <p v-for="m in matchMajors.slice(0, 20)" :key="m.id || m.majorCode" class="text-sm py-1">
          {{ m.majorName || m.name }} <span class="text-zinc-400 text-xs">{{ m.universityLevel || '' }}</span>
        </p>
      </div>
    </div>
  </div>
</template>
