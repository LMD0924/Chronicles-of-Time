<script setup>
defineOptions({ name: 'GlobalSearch' })

import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Close, Search } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const router = useRouter()
const open = ref(false)
const query = ref('')
const loading = ref(false)
const loaded = ref(false)
const inputRef = ref(null)
const userId = ref(null)
const indexItems = ref([])

const openSearch = async () => {
  open.value = true
  if (!loaded.value) await loadIndex()
  await nextTick()
  inputRef.value?.focus()
}
const closeSearch = () => {
  open.value = false
  query.value = ''
}
const listOf = (result) => {
  const data = result?.value?.data
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.records)) return data.records
  if (Array.isArray(data?.list)) return data.list
  return []
}
const addItems = (items, type, path, titleOf, metaOf, keywordsOf = () => '') => {
  items.forEach((item) => {
    const title = String(titleOf(item) || '').trim()
    if (!title) return
    indexItems.value.push({
      id: `${type}-${item.id}`,
      type,
      label: type,
      title,
      meta: String(metaOf(item) || '').trim(),
      keywords: `${title} ${metaOf(item) || ''} ${keywordsOf(item) || ''}`.toLowerCase(),
      path: typeof path === 'function' ? path(item) : path,
    })
  })
}
const loadIndex = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const userResult = await request.get('/user/getUserById').catch(() => null)
    userId.value = userResult?.data?.id || null
    const results = await Promise.allSettled([
      request.get('/content/my/list', { pageNum: 1, pageSize: 100 }),
      request.post('/growth/list', { pageNum: 1, pageSize: 100 }),
      request.get('/workplace/tasks'),
      request.get('/workplace/goals'),
      request.get('/workplace/interviews'),
      userId.value ? request.get(`/mistake/list/${userId.value}`) : Promise.resolve({ data: [] }),
      userId.value ? request.get(`/score/list/${userId.value}`) : Promise.resolve({ data: [] }),
    ])
    indexItems.value = []
    addItems(listOf(results[0]), '文章', (item) => `/View/${item.id}`, (item) => item.title, (item) => item.summary || item.content, (item) => item.tags)
    addItems(listOf(results[1]), '成长记录', '/Records', (item) => item.title || item.activityName || item.examName, (item) => item.achievementThisPeriod || item.summary || item.description, (item) => item.stage)
    addItems(listOf(results[2]), '任务', '/WorkRecords', (item) => item.taskName, (item) => `${item.status || '待办'} · ${item.dueDate || '未定日期'}`, (item) => item.notes)
    addItems(listOf(results[3]), '目标', '/WorkRecords', (item) => item.goalName, (item) => `${item.status || '进行中'} · ${item.targetDate || '未定日期'}`, (item) => item.metric || item.notes)
    addItems(listOf(results[4]), '面试', '/InterviewLab', (item) => `${item.companyName || '目标公司'} · ${item.positionName || '面试'}`, (item) => `${item.interviewRound || ''} · ${item.interviewDate || '未定日期'}`, (item) => item.status)
    addItems(listOf(results[5]), '错题', '/StudyDashboard?tab=mistake', (item) => item.mistakeName || item.questionTitle || item.subjectName, (item) => item.knowledgePoint || item.subjectName || '待复习', (item) => item.analysis)
    addItems(listOf(results[6]), '考试', '/StudyDashboard?tab=analysis', (item) => item.examName || item.subjectName || '考试成绩', (item) => `${item.score ?? '-'} 分 · ${item.examDate || '未定日期'}`, (item) => item.remark)
    loaded.value = true
  } finally {
    loading.value = false
  }
}
const normalizedQuery = computed(() => query.value.trim().toLowerCase())
const filteredItems = computed(() => {
  if (!normalizedQuery.value) return indexItems.value.slice(0, 8)
  return indexItems.value.filter((item) => item.keywords.includes(normalizedQuery.value)).slice(0, 30)
})
const groupedItems = computed(() => filteredItems.value.reduce((groups, item) => {
  if (!groups[item.label]) groups[item.label] = []
  groups[item.label].push(item)
  return groups
}, {}))
const navigateTo = (item) => {
  closeSearch()
  router.push(item.path)
}
const handleKeydown = (event) => {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    openSearch()
  } else if (event.key === 'Escape' && open.value) {
    closeSearch()
  }
}
onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('app:open-search', openSearch)
})
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('app:open-search', openSearch)
})
watch(open, (value) => { if (!value) query.value = '' })
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="global-search-root" @click.self="closeSearch">
      <section class="global-search-panel" role="dialog" aria-modal="true" aria-label="全局搜索">
        <header class="global-search-header">
          <div class="global-search-input-wrap">
            <Search class="global-search-input-icon" />
            <input ref="inputRef" v-model="query" type="search" placeholder="搜索文章、记录、错题、任务和目标..." autocomplete="off" />
            <kbd>Ctrl K</kbd>
          </div>
          <button type="button" class="global-search-close" aria-label="关闭搜索" title="关闭" @click="closeSearch"><Close /></button>
        </header>
        <div class="global-search-body">
          <div v-if="loading" class="global-search-state">正在整理你的内容...</div>
          <div v-else-if="!filteredItems.length" class="global-search-state">没有找到匹配内容</div>
          <template v-else>
            <div v-for="(items, label) in groupedItems" :key="label" class="global-search-group">
              <h3>{{ label }}</h3>
              <button v-for="item in items" :key="item.id" type="button" class="global-search-result" @click="navigateTo(item)">
                <span class="global-search-result-copy"><strong>{{ item.title }}</strong><small>{{ item.meta || '打开查看详情' }}</small></span>
                <ArrowRight />
              </button>
            </div>
          </template>
        </div>
        <footer class="global-search-footer"><span>输入关键词筛选你的成长资料</span><button type="button" @click="closeSearch">按 Esc 关闭</button></footer>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.global-search-root { position:fixed; inset:0; z-index:2000; display:flex; align-items:flex-start; justify-content:center; background:rgb(15 23 42 / .42); padding:10vh 16px 24px; backdrop-filter:blur(4px); }
.global-search-panel { width:min(720px, 100%); overflow:hidden; border:1px solid var(--app-border); border-radius:12px; background:var(--app-card-solid); box-shadow:0 28px 90px rgb(15 23 42 / .28); }
.global-search-header { display:flex; align-items:center; gap:8px; border-bottom:1px solid var(--app-border); padding:12px; }
.global-search-input-wrap { display:flex; min-width:0; flex:1; align-items:center; gap:9px; border:1px solid var(--app-border); border-radius:8px; padding:0 10px; }
.global-search-input-wrap:focus-within { border-color:rgb(var(--theme-primary-rgb) / .6); box-shadow:0 0 0 3px rgb(var(--theme-primary-rgb) / .1); }
.global-search-input-icon { width:18px; color:var(--app-text-muted); }
.global-search-input-wrap input { min-width:0; flex:1; border:0; background:transparent; padding:11px 0; color:var(--app-text); outline:0; font-size:14px; }
.global-search-input-wrap input::-webkit-search-cancel-button { display:none; }
.global-search-input-wrap kbd { display:inline-flex; align-items:center; gap:2px; border:1px solid var(--app-border); border-radius:5px; padding:3px 5px; color:var(--app-text-muted); font-size:10px; }
.global-search-input-wrap kbd svg { width:11px; }
.global-search-close { display:grid; width:36px; height:36px; flex:0 0 36px; place-items:center; border-radius:7px; color:var(--app-text-muted); }.global-search-close:hover { background:rgb(var(--theme-primary-rgb) / .1); color:var(--theme-primary); }.global-search-close svg { width:18px; }
.global-search-body { max-height:min(62vh, 560px); overflow-y:auto; padding:10px; }
.global-search-group + .global-search-group { margin-top:12px; }.global-search-group h3 { padding:6px 10px; color:var(--app-text-muted); font-size:11px; font-weight:800; letter-spacing:.04em; }
.global-search-result { display:flex; width:100%; align-items:center; gap:10px; border-radius:8px; padding:10px; text-align:left; }.global-search-result:hover { background:rgb(var(--theme-primary-rgb) / .09); }.global-search-result svg { width:16px; flex:0 0 auto; color:var(--app-text-muted); }.global-search-result-copy { min-width:0; flex:1; }.global-search-result-copy strong,.global-search-result-copy small { display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }.global-search-result-copy strong { color:var(--app-text); font-size:13px; }.global-search-result-copy small { margin-top:3px; color:var(--app-text-muted); font-size:11px; }
.global-search-state { padding:52px 16px; color:var(--app-text-muted); text-align:center; font-size:13px; }.global-search-footer { display:flex; align-items:center; justify-content:space-between; border-top:1px solid var(--app-border); padding:9px 14px; color:var(--app-text-muted); font-size:11px; }.global-search-footer button { color:var(--theme-primary); font-weight:700; }
@media (max-width:640px) { .global-search-root { padding-top:6vh; }.global-search-footer span { display:none; }.global-search-footer { justify-content:flex-end; } }
</style>
