<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request.js'
import { ThemeType, getStoredTheme, onThemeChange } from '@/utils/theme.js'

const router = useRouter()
const [messageApi, contextHolder] = message.useMessage()
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const loading = ref(false)
const articles = ref([])
const selectedYear = ref('all')

const menuItems = [
  { key: 'journal', label: '云边小札', icon: '📖', path: '/List' },
  { key: 'publish', label: '分享小札', icon: '✍️', path: '/Publish' },
  { key: 'archive', label: '文章归档', icon: '🗓️', path: '/Archive' },
  { key: 'knowledge', label: '知识图谱', icon: '🔗', path: '/ContentKnowledgeGraph' }
]

const articleDate = (article) => article.publishTime || article.createTime || article.updateTime
const parseDate = (value) => {
  if (!value) return null
  const date = new Date(String(value).replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? null : date
}

const years = computed(() => [...new Set(articles.value
  .map(article => parseDate(articleDate(article))?.getFullYear())
  .filter(Boolean))].sort((first, second) => second - first))

const timelineGroups = computed(() => {
  const grouped = new Map()
  const filtered = articles.value
    .filter(article => selectedYear.value === 'all' || parseDate(articleDate(article))?.getFullYear() === selectedYear.value)
    .sort((first, second) => (parseDate(articleDate(second))?.getTime() || 0) - (parseDate(articleDate(first))?.getTime() || 0))

  for (const article of filtered) {
    const date = parseDate(articleDate(article))
    if (!date) continue
    const year = date.getFullYear()
    const month = date.getMonth() + 1
    const key = `${year}-${String(month).padStart(2, '0')}`
    if (!grouped.has(key)) grouped.set(key, { key, year, month, articles: [] })
    grouped.get(key).articles.push({ article, date })
  }
  return [...grouped.values()]
})

const earliestYear = computed(() => years.value.at(-1) || '-')

const loadArchive = async () => {
  loading.value = true
  try {
    const res = await request.get('/content/archive')
    if (res.code === 200) {
      articles.value = res.data || []
    } else {
      messageApi.error(res.message || '获取文章归档失败')
    }
  } catch (error) {
    console.error('获取文章归档失败', error)
    messageApi.error('获取文章归档失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const formatDay = (date) => String(date.getDate()).padStart(2, '0')
const formatWeekday = (date) => ['日', '一', '二', '三', '四', '五', '六'][date.getDay()]
const openArticle = (article) => router.push(`/View/${article.id}`)
const handleMenuClick = (item) => item.path && router.push(item.path)
const handleLogoClick = () => router.push('/home')
const removeThemeListener = onThemeChange((theme) => { isDark.value = theme === ThemeType.DARK })

onMounted(loadArchive)
onUnmounted(() => removeThemeListener?.())
</script>

<template>
  <a-config-provider>
    <div :class="[isDark ? 'dark' : '', 'min-h-screen']">
      <div :class="[isDark ? 'bg-dark-bg text-gray-100' : 'app-page-bg text-gray-900', 'min-h-screen transition-colors duration-300']">
        <Nav :isDark="isDark" logoIcon="📚" logoText="拾光记" logoSubtext="文章归档" :menuItems="menuItems" :showBackHome="true" logoPath="/home" @menuClick="handleMenuClick" @logoClick="handleLogoClick" />

        <main class="mx-auto max-w-5xl px-4 pb-16 pt-28 sm:px-6">
          <section class="mb-8 flex flex-col gap-5 border-b pb-6 sm:flex-row sm:items-end sm:justify-between" :class="isDark ? 'border-gray-800' : 'border-gray-200'">
            <div>
              <h1 class="text-2xl font-semibold">文章归档</h1>
              <p class="mt-2 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">按文章发布时间整理</p>
            </div>
            <div class="flex gap-6 text-sm">
              <div><div class="text-xl font-semibold">{{ articles.length }}</div><div :class="isDark ? 'text-gray-500' : 'text-gray-400'">已归档文章</div></div>
              <div><div class="text-xl font-semibold">{{ earliestYear }}</div><div :class="isDark ? 'text-gray-500' : 'text-gray-400'">最早记录</div></div>
            </div>
          </section>

          <div v-if="years.length" class="mb-8 flex flex-wrap gap-2">
            <button type="button" class="h-8 border px-3 text-sm transition-colors" :class="selectedYear === 'all' ? 'border-brand-600 bg-brand-600 text-white' : (isDark ? 'border-gray-700 text-gray-300 hover:border-gray-500' : 'border-gray-300 text-gray-600 hover:border-gray-500')" @click="selectedYear = 'all'">全部</button>
            <button v-for="year in years" :key="year" type="button" class="h-8 border px-3 text-sm transition-colors" :class="selectedYear === year ? 'border-brand-600 bg-brand-600 text-white' : (isDark ? 'border-gray-700 text-gray-300 hover:border-gray-500' : 'border-gray-300 text-gray-600 hover:border-gray-500')" @click="selectedYear = year">{{ year }}</button>
          </div>

          <div v-if="loading" class="flex justify-center py-20"><div class="h-7 w-7 animate-spin rounded-full border-2 border-brand-600 border-t-transparent"></div></div>

          <div v-else-if="timelineGroups.length" class="relative ml-3 border-l" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
            <section v-for="group in timelineGroups" :key="group.key" class="relative pb-9 pl-7 last:pb-0">
              <span class="absolute -left-2 top-1 h-4 w-4 rounded-full border-4 border-brand-600" :class="isDark ? 'bg-dark-bg' : 'bg-white'"></span>
              <h2 class="mb-3 text-base font-semibold">{{ group.year }} 年 {{ group.month }} 月</h2>
              <div class="space-y-1">
                <button v-for="entry in group.articles" :key="entry.article.id" type="button" class="grid w-full grid-cols-[3rem_minmax(0,1fr)] items-center gap-3 border-b px-2 py-3 text-left transition-colors" :class="isDark ? 'border-gray-800 hover:bg-gray-900' : 'border-gray-100 hover:bg-white'" @click="openArticle(entry.article)">
                  <span class="text-center"><span class="block text-lg font-semibold leading-none">{{ formatDay(entry.date) }}</span><span class="mt-1 block text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">周{{ formatWeekday(entry.date) }}</span></span>
                  <span class="min-w-0"><span class="block truncate text-sm font-medium">{{ entry.article.title || '未命名文章' }}</span><span class="mt-1 flex items-center gap-2 text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'"><span>{{ entry.article.category || '随笔' }}</span><span>{{ entry.article.views || 0 }} 阅读</span><span>{{ entry.article.commentsCount || 0 }} 评论</span></span></span>
                </button>
              </div>
            </section>
          </div>

          <div v-else class="py-20 text-center text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-400'">暂无已发布文章</div>
        </main>
      </div>
    </div>
    <component :is="contextHolder" />
  </a-config-provider>
</template>