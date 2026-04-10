<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import request from "@/utils/request.js"
import Nav from '@/components/Nav.vue';
import {
  ThemeType,
  getStoredTheme,
  onThemeChange
} from "@/utils/theme.js";

const router = useRouter()
const route = useRoute()
const [messageApi, contextHolder] = message.useMessage();

// 主题
const isDark = ref(getStoredTheme() === ThemeType.DARK)

// 文章列表数据
const articles = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 筛选条件
const filters = ref({
  category: '',
  contentType: 'markdown',
  keyword: '',
  sortBy: 'latest', // latest, hottest, mostLiked
  myArticles: false // 是否只显示我的文章
})

// 分类选项
const categories = [
  { value: '', label: '全部', icon: '📋' },
  { value: '随笔', label: '随笔', icon: '✍️' },
  { value: '技术', label: '技术', icon: '💻' },
  { value: '生活', label: '生活', icon: '🌿' },
  { value: '旅行', label: '旅行', icon: '✈️' },
  { value: '读书', label: '读书', icon: '📚' },
  { value: '日记', label: '日记', icon: '📖' }
]

// 排序选项
const sortOptions = [
  { value: 'latest', label: '最新发布', icon: '🕐' },
  { value: 'hottest', label: '最热排行', icon: '🔥' },
  { value: 'mostLiked', label: '最多点赞', icon: '❤️' }
]

// 热门标签（从文章数据中提取）
const hotTags = ref([
  '生活感悟', '技术分享', '读书笔记', '旅行日记', '成长记录', '职场经验', 'Vue.js', 'Spring Boot'
])

// 选中的标签
const selectedTags = ref([])

// 用户信息
const UserInfo = ref({})

// 菜单配置
const menuItems = [
  { key: 'journal', label: '云边小札', icon: '📖', path: '/List' },
  { key: 'publish', label: '分享小札', icon: '✍️', path: '/Publish' },
  { key: 'knowledge', label: '知识图谱', icon: '🔗', path: '/ContentKnowledgeGraph' },
]

// 获取文章列表
const getArticleList = async () => {
  loading.value = true

  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      category: filters.value.category || null,
      contentType: filters.value.contentType,
      keyword: filters.value.keyword || null
    }

    let apiUrl = '/content/public/list'
    if (filters.value.myArticles) {
      apiUrl = '/content/my/list'
    }

    const res = await request.get(apiUrl, params)
    if (res.code === 200 && res.data) {
      articles.value = res.data.records || []
      total.value = res.data.total || 0
      console.log("文章列表数据：", articles.value)
    }
  } catch (error) {
    console.error('获取文章列表失败', error)
    messageApi.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索文章
const searchArticles = async () => {
  if (!filters.value.keyword.trim()) {
    getArticleList()
    return
  }

  loading.value = true

  try {
    const params = {
      keyword: filters.value.keyword,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    const res = await request.get('/content/search', params)
    if (res.code === 200 && res.data) {
      articles.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      articles.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('搜索失败', error)
    messageApi.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 获取热门内容（用于右侧推荐）
const getHotContents = async () => {
  try {
    const res = await request.get('/content/hot', { limit: 5 })
    if (res.code === 200) {
      hotArticles.value = res.data || []
    }
  } catch (error) {
    console.error('获取热门内容失败', error)
  }
}

// 热门文章数据
const hotArticles = ref([])

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'numeric',
      day: 'numeric'
    })
  }
}

// 跳转到文章详情
const goToDetail = (id) => {
  router.push(`/View/${id}`)
}

// 切换分类
const changeCategory = (category) => {
  filters.value.category = category
  currentPage.value = 1
  getArticleList()
}

// 切换我的文章
const toggleMyArticles = () => {
  filters.value.myArticles = !filters.value.myArticles
  currentPage.value = 1
  getArticleList()
}

// 切换排序
const changeSort = (sort) => {
  filters.value.sortBy = sort
  sortArticles()
}

// 本地排序
const sortArticles = () => {
  const sorted = [...articles.value]
  switch (filters.value.sortBy) {
    case 'latest':
      sorted.sort((a, b) => new Date(b.updateTime) - new Date(a.updateTime))
      break
    case 'hottest':
      sorted.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
      break
    case 'mostLiked':
      sorted.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
      break
  }
  articles.value = sorted
}

// 添加标签筛选
const addTag = (tag) => {
  if (!selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
    applyTagFilter()
  }
}

// 移除标签
const removeTag = (tag) => {
  selectedTags.value = selectedTags.value.filter(t => t !== tag)
  applyTagFilter()
}

// 应用标签筛选
const applyTagFilter = () => {
  currentPage.value = 1
  if (selectedTags.value.length > 0) {
    filterArticlesByTags()
  } else {
    getArticleList()
  }
}

// 根据标签筛选文章
const filterArticlesByTags = () => {
  if (!articles.value.length) return
  const filtered = articles.value.filter(article => {
    const articleTags = article.tags ? article.tags.split(',') : []
    return selectedTags.value.some(tag => articleTags.includes(tag))
  })
  articles.value = filtered
  total.value = filtered.length
}

// 清除所有筛选
const clearFilters = () => {
  filters.value.category = ''
  filters.value.keyword = ''
  filters.value.myArticles = false
  selectedTags.value = []
  currentPage.value = 1
  getArticleList()
}

// 搜索提交
const handleSearch = () => {
  currentPage.value = 1
  if (filters.value.keyword.trim()) {
    searchArticles()
  } else {
    getArticleList()
  }
}

// 重置搜索
const resetSearch = () => {
  filters.value.keyword = ''
  selectedTags.value = []
  currentPage.value = 1
  getArticleList()
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
  if (filters.value.keyword.trim()) {
    searchArticles()
  } else {
    getArticleList()
  }
}

// 获取用户信息
const getUserInfo = () => {
  request.get('/user/getUserById', {}, (message, data) => {
    UserInfo.value = data
  })
}

// 处理菜单点击
const handleMenuClick = (item) => {
  if (item.path) {
    router.push(item.path)
  }
}

// 处理 Logo 点击
const handleLogoClick = () => {
  router.push('/home')
}

// 创建新文章
const createArticle = () => {
  router.push('/Publish')
}

// 编辑文章
const editArticle = (e, id) => {
  e.stopPropagation()
  router.push(`/Publish/${id}`)
}

// 删除文章
const deleteArticle = (e, article) => {
  e.stopPropagation()
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文章《${article.title}》吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await request.delete(`/content/delete/${article.id}`)
        if (res.code === 200) {
          messageApi.success('删除成功')
          getArticleList()
        } else {
          messageApi.error(res.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败', error)
        messageApi.error('删除失败，请稍后重试')
      }
    }
  })
}

// 检查是否是当前用户的文章
const isOwnArticle = (article) => {
  return UserInfo.value && article.userId && UserInfo.value.id === article.userId
}

// 主题切换
const handleThemeChange = (theme) => {
  const newTheme = theme === ThemeType.DARK
  isDark.value = newTheme
  if (newTheme) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// 获取分类图标
const getCategoryIcon = (category) => {
  const cat = categories.find(c => c.value === category)
  return cat ? cat.icon : '📄'
}

// 获取分类名称
const getCategoryName = (category) => {
  const cat = categories.find(c => c.value === category)
  return cat ? cat.label : '随笔'
}

// 截断文本
const truncateText = (text, maxLength = 120) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

onMounted(() => {
  getUserInfo()
  getArticleList()
  getHotContents()

  if (isDark.value) {
    document.documentElement.classList.add('dark')
  }

  if (route.query.category) {
    filters.value.category = route.query.category
  }

  window.addEventListener('scroll', handleScroll)

  const stopListen = onThemeChange((theme) => {
    handleThemeChange(theme)
  })

  onUnmounted(() => {
    stopListen()
  })
})

// 滚动监听
const isScrolled = ref(false)
const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <contextHolder />

  <div :class="[isDark ? 'dark' : '', 'min-h-screen overflow-x-hidden']">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900',
      'min-h-screen transition-colors duration-300'
    ]">

      <Nav
        :isDark="isDark"
        logoIcon="📚"
        logoText="拾光记"
        logoSubtext="文章列表"
        :menuItems="menuItems"
        :showBackHome="true"
        logoPath="/home"
        @menuClick="handleMenuClick"
        @logoClick="handleLogoClick"
      />

      <section class="relative pt-24 pb-12 overflow-hidden">
        <div class="absolute inset-0 z-0">
          <div class="absolute w-[400px] h-[400px] bg-indigo-400/20 rounded-full blur-[80px] -top-20 -left-20"></div>
          <div class="absolute w-[300px] h-[300px] bg-purple-400/15 rounded-full blur-[80px] -bottom-32 -right-32"></div>
        </div>

        <div class="relative z-10 max-w-[1400px] mx-auto px-6 lg:px-8">
          <div class="text-center max-w-3xl mx-auto">
            <h1 class="text-3xl lg:text-4xl font-bold mb-4 bg-gradient-to-r from-indigo-500 to-purple-600 bg-clip-text text-transparent">
              文章列表
            </h1>
            <p class="text-base lg:text-lg mb-6" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
              记录思考，分享感悟，见证成长
            </p>

            <div class="flex gap-2 max-w-lg mx-auto">
              <input
                v-model="filters.keyword"
                @keyup.enter="handleSearch"
                type="text"
                placeholder="搜索文章..."
                class="flex-1 px-4 py-2.5 rounded-full outline-none transition-all"
                :class="isDark ? 'bg-gray-900 text-gray-200 border-gray-700 focus:border-indigo-500' : 'bg-white text-gray-800 border-gray-200 focus:border-indigo-400'"
              >
              <button
                @click="handleSearch"
                class="px-5 py-2.5 rounded-full bg-gradient-to-r from-indigo-500 to-purple-600 text-white font-medium hover:opacity-90 transition-all"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </section>

      <div class="max-w-[1400px] mx-auto px-6 lg:px-8 pb-16">
        <div class="grid lg:grid-cols-12 gap-8">

          <div class="lg:col-span-3">
            <div class="sticky top-24 space-y-6">
              <div class="rounded-2xl p-5" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-4 flex items-center gap-2">分类</h3>
                <div class="space-y-2 mb-4">
                  <button
                    v-for="cat in categories"
                    :key="cat.value"
                    @click="changeCategory(cat.value)"
                    class="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all"
                    :class="[
                      filters.category === cat.value
                        ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white shadow-md'
                        : (isDark ? 'text-gray-400 hover:bg-gray-800' : 'text-gray-600 hover:bg-gray-100')
                    ]"
                  >
                    <span>{{ cat.icon }}</span>
                    <span class="flex-1 text-left">{{ cat.label }}</span>
                    <span v-if="filters.category === cat.value" class="text-xs">✓</span>
                  </button>
                </div>
                <div :class="isDark ? 'border-gray-700' : 'border-gray-200'" class="border-t pt-4">
                  <button
                    @click="toggleMyArticles"
                    class="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all"
                    :class="[
                      filters.myArticles
                        ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white shadow-md'
                        : (isDark ? 'text-gray-400 hover:bg-gray-800' : 'text-gray-600 hover:bg-gray-100')
                    ]"
                  >
                    <span>👤</span>
                    <span class="flex-1 text-left">我的文章</span>
                    <span v-if="filters.myArticles" class="text-xs">✓</span>
                  </button>
                </div>
              </div>

              <div class="rounded-2xl p-5" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-4 flex items-center gap-2">🏷️ 热门标签</h3>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="tag in hotTags"
                    :key="tag"
                    @click="addTag(tag)"
                    class="px-2.5 py-1 rounded-full text-xs transition-all"
                    :class="[
                      selectedTags.includes(tag)
                        ? 'bg-indigo-500 text-white'
                        : (isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200')
                    ]"
                  >#{{ tag }}</button>
                </div>
              </div>

              <div v-if="filters.category || selectedTags.length > 0" class="rounded-2xl p-5" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-3">🔍 当前筛选</h3>
                <div class="flex flex-wrap gap-2 mb-3">
                  <span v-if="filters.category" class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs bg-indigo-100 dark:bg-indigo-900/50 text-indigo-600 dark:text-indigo-400">
                    {{ getCategoryName(filters.category) }}
                    <button @click="changeCategory('')" class="hover:opacity-70">×</button>
                  </span>
                  <span v-for="tag in selectedTags" :key="tag" class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs bg-indigo-100 dark:bg-indigo-900/50 text-indigo-600 dark:text-indigo-400">
                    #{{ tag }}
                    <button @click="removeTag(tag)" class="hover:opacity-70">×</button>
                  </span>
                </div>
                <button @click="clearFilters" class="text-sm text-indigo-500">清除全部筛选</button>
              </div>

              <button
                @click="createArticle"
                class="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-500 to-purple-600 text-white font-medium shadow-lg hover:shadow-xl transition-all hover:-translate-y-0.5 flex items-center justify-center gap-2"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                </svg>
                写文章
              </button>
            </div>
          </div>

          <!-- ====================== 文章列表 ====================== -->
          <div class="lg:col-span-6">
            <div class="flex items-center justify-between mb-6 flex-wrap gap-3">
              <span class="text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">共 {{ total }} 篇文章</span>
              <div class="flex gap-2">
                <button
                  v-for="option in sortOptions"
                  :key="option.value"
                  @click="changeSort(option.value)"
                  class="flex items-center gap-1 px-3 py-1.5 rounded-full text-sm"
                  :class="[filters.sortBy === option.value ? 'bg-indigo-500 text-white' : isDark ? 'bg-gray-800 text-gray-400' : 'bg-gray-100 text-gray-600']"
                >
                  <span>{{ option.icon }}</span>
                  <span>{{ option.label }}</span>
                </button>
              </div>
            </div>

            <div v-if="loading" class="space-y-4">
              <div v-for="i in 3" :key="i" class="rounded-2xl p-6 animate-pulse" :class="isDark ? 'bg-gray-900/50' : 'bg-white'">
                <div class="h-4 bg-gray-300 dark:bg-gray-700 rounded w-1/4 mb-3"></div>
                <div class="h-6 bg-gray-300 dark:bg-gray-700 rounded w-3/4 mb-3"></div>
              </div>
            </div>

            <div v-else-if="articles.length === 0" class="text-center py-16">
              <p class="text-gray-400">暂无文章</p>
              <button @click="createArticle" class="mt-4 px-4 py-2 rounded-lg bg-indigo-500 text-white text-sm">写文章</button>
            </div>

            <div v-else class="space-y-5 max-h-[100vh] overflow-y-auto pr-2 no-scrollbar">
              <div
                v-for="article in articles"
                :key="article.id"
                @click="goToDetail(article.id)"
                class="rounded-2xl p-6 cursor-pointer transition-all hover:-translate-y-1 hover:shadow-xl group relative"
                :class="isDark ? 'bg-gray-900/50 hover:bg-gray-900/70' : 'bg-white shadow-sm hover:shadow-lg'"
              >
                <div v-if="isOwnArticle(article)" class="absolute top-4 right-4 flex gap-2 z-10">
                  <button @click="editArticle($event, article.id)" class="px-2 py-1 rounded-lg text-xs bg-indigo-100 text-indigo-600">✏️ 编辑</button>
                  <button @click="deleteArticle($event, article)" class="px-2 py-1 rounded-lg text-xs bg-red-100 text-red-600">🗑️ 删除</button>
                </div>

                <!-- ====================== ✅ 文章封面 ====================== -->
                <div v-if="article.coverImage" class="mb-4 rounded-xl overflow-hidden">
                  <img :src="article.coverImage" alt="封面" class="w-full h-48 object-cover transition-transform group-hover:scale-105">
                </div>

                <div class="flex items-center justify-between mb-3 flex-wrap gap-2">
                  <div class="flex items-center gap-2">
                    <span class="px-2 py-1 rounded-full text-xs font-medium bg-indigo-100 dark:bg-indigo-900/50 text-indigo-600">
                      {{ getCategoryIcon(article.category) }} {{ getCategoryName(article.category) }}
                    </span>
                    <span class="text-xs text-gray-400">{{ formatDate(article.updateTime) }}</span>
                  </div>
                  <div class="flex items-center gap-3 text-xs text-gray-400">
                    <span>👁️ {{ article.views || 0 }}</span>
                    <span>❤️ {{ article.likesCount || 0 }}</span>
                    <span>⭐ {{ article.favoritesCount || 0 }}</span>
                    <span>💬 {{ article.commentsCount || 0 }}</span>
                  </div>
                </div>

                <h2 class="text-xl font-bold mb-2 group-hover:text-indigo-500 transition-colors">
                  {{ article.title }}
                </h2>

                <p class="text-sm leading-relaxed mb-3 text-gray-500">
                  {{ truncateText(article.summary || article.content) }}
                </p>

                <div v-if="article.tags" class="flex flex-wrap gap-2">
                  <span
                    v-for="tag in article.tags.split(',').slice(0,3)"
                    :key="tag"
                    class="px-2 py-0.5 rounded-full text-xs bg-gray-100 dark:bg-gray-800 text-gray-500"
                  >#{{ tag.trim() }}</span>
                </div>
              </div>
            </div>

            <div v-if="total > pageSize" class="flex justify-center mt-8">
              <div class="flex gap-2">
                <button :disabled="currentPage === 1" @click="handlePageChange(currentPage - 1)" class="w-9 h-9 rounded-lg flex items-center justify-center">←</button>
                <button
                  v-for="page in Math.ceil(total/pageSize)"
                  :key="page"
                  @click="handlePageChange(page)"
                  class="w-9 h-9 rounded-lg"
                  :class="currentPage === page ? 'bg-indigo-500 text-white' : 'bg-gray-100'"
                >{{ page }}</button>
                <button :disabled="currentPage === Math.ceil(total/pageSize)" @click="handlePageChange(currentPage + 1)" class="w-9 h-9 rounded-lg flex items-center justify-center">→</button>
              </div>
            </div>
          </div>

          <div class="lg:col-span-3">
            <div class="sticky top-24 space-y-6">
              <div class="rounded-2xl p-5 text-center" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <img :src="UserInfo.avatar || '/default-avatar.png'" alt="头像" class="w-16 h-16 rounded-full mx-auto mb-3">
                <h3 class="font-semibold">{{ UserInfo.name || '游客' }}</h3>
                <button @click="createArticle" class="mt-4 w-full py-2 rounded-lg text-sm bg-gradient-to-r from-indigo-500 to-purple-600 text-white">写文章</button>
              </div>

              <div class="rounded-2xl p-5" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-4">🔥 热门推荐</h3>
                <div class="space-y-3">
                  <div v-for="(item, idx) in hotArticles.slice(0,5)" :key="item.id" @click="goToDetail(item.id)" class="flex gap-3 cursor-pointer">
                    <div class="text-lg font-bold text-indigo-500 w-6">{{ idx+1 }}</div>
                    <div>
                      <p class="text-sm font-medium line-clamp-2">{{ item.title }}</p>
                      <div class="text-xs text-gray-400 mt-1">👁️ {{ item.views || 0 }} ❤️ {{ item.likesCount || 0 }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <button
        v-show="isScrolled"
        @click="window.scrollTo({top:0,behavior:'smooth'})"
        class="fixed bottom-8 right-8 w-10 h-10 rounded-full bg-indigo-500 text-white shadow-lg flex items-center justify-center"
      >↑</button>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
/* 隐藏滚动条，但保留滚动功能 */
.no-scrollbar {
  /* Firefox */
  scrollbar-width: none;
  /* IE/Edge */
  -ms-overflow-style: none;
}

/* Chrome/Safari/Edge */
.no-scrollbar::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  background: transparent;
}
</style>
