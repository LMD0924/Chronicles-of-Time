<!--
  文件说明：拾光记前台应用内容社区页面组件，承载内容社区场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import request from "@/utils/request.js"
import Nav from '@/components/Nav.vue';
import {
  ThemeType,
  getStoredTheme,
  onThemeChange
} from "@/utils/theme.js"
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const router = useRouter()
const route = useRoute()
const [messageApi, contextHolder] = message.useMessage();

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

const isDark = ref(getStoredTheme() === ThemeType.DARK)

const article = ref({
  id: null,
  authorId: null,
  title: '',
  summary: '',
  content: '',
  category: '',
  tags: [],
  coverImage: '',
  author: {
    id: null,
    name: '',
    avatar: '',
    introduction: ''
  },
  likeCount: 0,
  favoriteCount: 0,
  commentCount: 0,
  views: 0,
  createdAt: '',
  updatedAt: '',
  isLiked: false,
  isFavorited: false
})

const toc = ref([])
const activeHeadingId = ref('')
const comments = ref([])
const commentInput = ref('')
const replyTo = ref(null)
const loadingComments = ref(false)
const submittingComment = ref(false)
const UserInfo = ref({})
const loading = ref(true)

const menuItems = [
  { key: 'home', label: '首页', icon: '🏠', path: '/home' },
  { key: 'journal', label: '云边小札', icon: '📖', path: '/List' }
]

const extractToc = (content) => {
  const headings = []
  const lines = content.split('\n')
  let headingIndex = 0
  lines.forEach((line) => {
    const match = line.match(/^(#{1,3})\s+(.*)$/)
    if (match) {
      const level = match[1].length
      const title = match[2].trim()
      const id = `heading-${headingIndex++}`
      headings.push({ id, level, title })
    }
  })
  return headings
}

const renderMarkdownWithHeadingIds = (content) => {
  if (!content) return ''
  toc.value = extractToc(content)
  let html = md.render(content)
  let headingIndex = 0
  html = html.replace(/<(h[1-3])>(.*?)<\/\1>/g, (match, tag, text) => {
    const id = `heading-${headingIndex++}`
    return `<${tag} id="${id}">${text}</${tag}>`
  })
  return html
}

const renderedContent = computed(() => {
  return renderMarkdownWithHeadingIds(article.value.content)
})

const getArticleDetail = async () => {
  const id = route.params.id
  if (!id) {
    messageApi.error('文章不存在')
    router.back()
    return
  }
  loading.value = true
  try {
    const res = await request.get(`/content/detail/${id}`)
    if (res.code === 200 && res.data) {
      const data = res.data
      const isLiked = data.isLiked === true || data.isLiked === 1 || data.isLiked === 'true' || data.isLiked === '1'
      const isFavorited = data.isFavorited === true || data.isFavorited === 1 || data.isFavorited === 'true' || data.isFavorited === '1'
      const author = data.author || {}

      article.value = {
        id: data.id,
        title: data.title,
        authorId: data.userId == null ? null : String(data.userId),
        summary: data.summary || '',
        content: data.content,
        category: data.category,
        tags: data.tags ? data.tags.split(',') : [],
        coverImage: data.coverImage || '',
        author: {
          id: author.id ?? data.userId,
          name: author.name || author.username || data.authorName || '匿名用户',
          avatar: author.avatar || data.authorAvatar || '/default-avatar.png',
          introduction: author.introduction || data.authorIntroduction || ''
        },
        likeCount: data.likesCount || 0,
        favoriteCount: data.favoritesCount || 0,
        commentCount: data.commentsCount || 0,
        views: data.views || 0,
        createdAt: data.createdAt,
        updatedAt: data.updatedAt,
        isLiked: isLiked,
        isFavorited: isFavorited
      }
    } else {
      messageApi.error(res.message || '获取文章失败')
    }
  } catch (error) {
    console.error('获取文章失败', error)
    messageApi.error('获取文章失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getComments = async () => {
  const id = route.params.id
  if (!id) return
  loadingComments.value = true
  try {
    const res = await request.get(`/content/comment/list/${id}`)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (error) {
    console.error('获取评论失败', error)
  } finally {
    loadingComments.value = false
  }
}

const submitComment = async () => {
  if (!commentInput.value.trim()) {
    messageApi.warning('请输入评论内容')
    return
  }
  submittingComment.value = true
  try {
    const res = await request.post('/content/comment/add', {
      contentId: String(article.value.id),
      content: commentInput.value,
      parentId: replyTo.value?.id || null
    })
    if (res.code === 200) {
      messageApi.success('评论成功')
      commentInput.value = ''
      replyTo.value = null
      await getComments()
      article.value.commentCount++
    } else {
      messageApi.error(res.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败', error)
    messageApi.error('评论失败，请稍后重试')
  } finally {
    submittingComment.value = false
  }
}

const canReplyToComment = (comment) => {
  const currentUserId = UserInfo.value?.id
  return currentUserId != null && comment?.userId != null && String(currentUserId) !== String(comment.userId)
}

const isCommentLiked = (comment) => comment?.liked === true || comment?.liked === 1 || comment?.liked === 'true' || comment?.liked === '1'

const handleCommentLike = async (comment) => {
  if (!comment?.id) return
  const liked = isCommentLiked(comment)
  try {
    const res = liked
      ? await request.delete('/content/comment/unlike', { commentId: comment.id })
      : await request.post('/content/comment/like', {}, { commentId: comment.id })
    if (res.code === 200) {
      comment.liked = !liked
      const likesCount = Number(comment.likesCount || 0)
      comment.likesCount = Math.max(0, likesCount + (liked ? -1 : 1))
    } else {
      messageApi.error(res.message || '点赞操作失败')
    }
  } catch (error) {
    console.error('评论点赞失败', error)
    messageApi.error('点赞操作失败，请稍后重试')
  }
}

const replyToComment = (comment) => {
  if (!canReplyToComment(comment)) {
    messageApi.warning('不能回复自己的评论')
    return
  }
  replyTo.value = comment
  commentInput.value = `@${comment.userName || '用户'} `
  const inputEl = document.querySelector('.comment-input')
  inputEl?.focus()
}

const cancelReply = () => {
  replyTo.value = null
  commentInput.value = ''
}

const handleLike = async () => {
  try {
    const id = article.value.id;
    let res;
    if (article.value.isLiked) {
      res = await request.delete('/content/unlike', { contentId: id });
    } else {
      res = await request.post('/content/like', {}, { contentId: id });
    }
    if (res.code === 200) {
      article.value.isLiked = !article.value.isLiked;
      article.value.likeCount += article.value.isLiked ? 1 : -1;
      messageApi.success(article.value.isLiked ? "点赞成功" : "取消点赞");
    }
  } catch (error) {
    console.error("点赞失败", error);
  }
};

const handleFavorite = async () => {
  try {
    const id = article.value.id;
    let res;
    if (article.value.isFavorited) {
      res = await request.delete('/content/unfavorite', { contentId: id });
    } else {
      res = await request.post('/content/favorite', {}, { contentId: id });
    }
    if (res.code === 200) {
      article.value.isFavorited = !article.value.isFavorited;
      article.value.favoriteCount += article.value.isFavorited ? 1 : -1;
      messageApi.success(article.value.isFavorited ? "收藏成功" : "取消收藏");
    }
  } catch (error) {
    console.error("收藏失败", error);
  }
};

const scrollToHeading = (id) => {
  nextTick(() => {
    const element = document.getElementById(id)
    if (element) {
      const offset = 120
      const elementPosition = element.getBoundingClientRect().top
      const offsetPosition = elementPosition + window.pageYOffset - offset
      window.scrollTo({ top: offsetPosition, behavior: 'smooth' })
      activeHeadingId.value = id
    }
  })
}

const scrollToSection = (sectionId) => {
  const element = document.getElementById(sectionId)
  if (element) {
    const offset = 100
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset
    window.scrollTo({ top: offsetPosition, behavior: 'smooth' })
  }
}

const isOwnArticle = computed(() => {
  const userId = UserInfo.value?.id
  const authorId = article.value.authorId
  return userId != null && authorId != null && String(userId) === String(authorId)
})

// ==============================================
// 只改这里：编辑弹窗（和 Publish 完全一样）
// ==============================================
const editModal = ref(false)
const editForm = ref({
  id: null,
  title: '',
  summary: '',
  content: '',
  category: '',
  tags: [],
  coverImage: '',
  status: 1
})
const tagInput = ref('')
const editToolbar = ref([
  { icon: 'H1', prefix: '# ' },
  { icon: 'H2', prefix: '## ' },
  { icon: 'H3', prefix: '### ' },
  { icon: 'B', prefix: '**', suffix: '**' },
  { icon: 'I', prefix: '*', suffix: '*' },
  { icon: '~', prefix: '~~', suffix: '~~' },
  { icon: '•', prefix: '- ' },
  { icon: '1.', prefix: '1. ' },
  { icon: '"', prefix: '> ' },
  { icon: '🔗', prefix: '[', suffix: '](url)' },
  { icon: '🖼️', prefix: '![', suffix: '](url)' },
  { icon: '```', prefix: '```\n', suffix: '\n```' },
])

const editArticle = () => {
  editForm.value = {
    id: article.value.id,
    title: article.value.title,
    summary: article.value.summary || '',
    content: article.value.content,
    category: article.value.category || '随笔',
    tags: article.value.tags ? [...article.value.tags] : [],
    coverImage: article.value.coverImage || '',
    status: 1
  }
  editModal.value = true
}

const addEditTag = () => {
  const t = tagInput.value.trim()
  if (t && !editForm.value.tags.includes(t)) {
    editForm.value.tags.push(t)
    tagInput.value = ''
  }
}
const removeEditTag = (t) => {
  editForm.value.tags = editForm.value.tags.filter(x => x !== t)
}

const submitEdit = async () => {
  if (!editForm.value.title?.trim()) {
    messageApi.warning('请输入标题')
    return
  }
  try {
    // 强制保证 tags 一定是数组，不会出现空字符串
    const finalTags = Array.isArray(editForm.value.tags) ? editForm.value.tags : []

    const res = await request.post('/content/save', {
      id: editForm.value.id,
      title: editForm.value.title,
      summary: editForm.value.summary || '',
      content: editForm.value.content || '',
      category: editForm.value.category || '随笔',
      tags: finalTags,
      coverImage: editForm.value.coverImage || '',
      status: 1
    })

    if (res.code === 200) {
      messageApi.success('修改成功')
      editModal.value = false
      getArticleDetail()
    }
  } catch (e) {
    messageApi.error('保存失败')
  }
}

const deleteArticle = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这篇文章吗？此操作不可恢复。',
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await request.delete(`/content/delete/${article.value.id}`)
        if (res.code === 200) {
          messageApi.success('删除成功')
          router.push('/List')
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

const handleScroll = () => {
  const headings = toc.value.map(item => item.id)
  for (let i = headings.length - 1; i >= 0; i--) {
    const element = document.getElementById(headings[i])
    if (element) {
      const rect = element.getBoundingClientRect().top
      if (rect.top <= 100) {
        activeHeadingId.value = headings[i]
        break
      }
    }
  }
}

const getUserInfo = async () => {
  try {
    const res = await request.get('/user/getUserById')
    if (res.code === 200 && res.data) {
      UserInfo.value = res.data
    }
  } catch (err) {
    console.error('获取用户信息失败', err)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const handleMenuClick = (item) => {
  if (item.path) {
    router.push(item.path)
  }
}

const handleLogoClick = () => {
  router.push('/home')
}

const goBack = () => {
  router.back()
}

const handleThemeChange = (theme) => {
  const newTheme = theme === ThemeType.DARK
  isDark.value = newTheme
  if (newTheme) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

onMounted(async () => {
  await getUserInfo()
  await getArticleDetail()
  getComments()

  if (isDark.value) {
    document.documentElement.classList.add('dark')
  }

  window.addEventListener('scroll', handleScroll)
  const stopListen = onThemeChange((theme) => {
    handleThemeChange(theme)
  })
  onUnmounted(() => {
    stopListen()
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(() => article.value.content, () => {
  nextTick(() => {
    toc.value = extractToc(article.value.content)
  })
})

</script>

<template>
  <contextHolder />
  <div :class="[isDark ? 'dark' : '', 'min-h-screen overflow-x-hidden']">
    <div :class="[
      isDark ? 'bg-dark-bg text-white' : 'app-page-bg text-gray-900',
      'min-h-screen transition-colors duration-300'
    ]">

      <Nav
        :isDark="isDark"
        logoIcon="📖"
        logoText="拾光记"
        logoSubtext="阅读文章"
        :menuItems="menuItems"
        :showArchive="true"
        :showBackHome="true"
        logoPath="/home"
        @menuClick="handleMenuClick"
        @logoClick="handleLogoClick"
      />

      <div v-if="loading" class="flex items-center justify-center min-h-screen pt-20">
        <div class="text-center">
          <div class="w-12 h-12 border-4 border-brand-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p class="text-gray-500">加载中...</p>
        </div>
      </div>

      <div v-else class="pt-20 pb-16">
        <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">

            <div class="hidden lg:block lg:col-span-2">
              <div style="position:fixed; top:90px; width:200px;">
                <div class="rounded-xl p-4 bg-white dark:bg-gray-900/50 shadow-sm">
                  <div class="flex items-center gap-2 mb-4 pb-2 border-b border-gray-200 dark:border-gray-800">
                    <svg class="w-4 h-4 text-brand-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7"></path>
                    </svg>
                    <span class="text-sm font-semibold">目录</span>
                    <span class="text-xs text-gray-400">{{ toc.length }}节</span>
                  </div>

                  <div v-if="toc.length === 0" class="text-center text-sm text-gray-400 py-4">暂无目录</div>

                  <nav class="space-y-1 max-h-[calc(100vh-220px)] overflow-y-auto">
                    <div
                      v-for="heading in toc"
                      :key="heading.id"
                      @click="scrollToHeading(heading.id)"
                      class="cursor-pointer transition-all hover:text-brand-600 py-1.5 rounded"
                      :class="[
                        activeHeadingId === heading.id
                          ? 'text-brand-600 bg-brand-50 dark:bg-brand-950/30'
                          : (isDark ? 'text-gray-400 hover:text-gray-300' : 'text-gray-600 hover:text-gray-900')
                      ]"
                      :style="{ paddingLeft: `${(heading.level - 1) * 12 + 12}px` }"
                    >
                      <span class="text-xs">{{ heading.title }}</span>
                    </div>
                  </nav>
                </div>
              </div>
            </div>

            <div class="lg:col-span-7 xl:col-span-8">
              <div class="rounded-2xl overflow-hidden" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <div class="p-6 lg:p-8 border-b" :class="isDark ? 'border-gray-800' : 'border-gray-100'">
                  <button @click="goBack" class="lg:hidden flex items-center gap-1 text-sm mb-4 hover:text-brand-600">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
                    </svg>
                    返回
                  </button>

                  <div class="flex items-center gap-2 mb-4">
                    <span class="px-2 py-1 rounded-full text-xs font-medium bg-brand-100 dark:bg-brand-900/50 text-brand-600 dark:text-brand-400">
                      {{ article.category || '随笔' }}
                    </span>
                    <span class="text-xs text-gray-400">📅 {{ formatDate(article.createdAt) }}</span>
                    <span class="text-xs text-gray-400">👁️ {{ article.views }} 阅读</span>
                  </div>

                  <h1 class="text-2xl lg:text-3xl xl:text-4xl font-bold mb-4" :class="isDark ? 'text-white' : 'text-gray-900'">
                    {{ article.title }}
                  </h1>

                  <p v-if="article.summary" class="text-base leading-relaxed mb-5 italic" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                    {{ article.summary }}
                  </p>

                  <div class="flex items-center justify-between flex-wrap gap-4">
                    <div class="flex items-center gap-3">
                      <img :src="article.author.avatar || '/default-avatar.png'" alt="头像" class="w-10 h-10 rounded-full object-cover">
                      <div class="min-w-0">
                        <div class="text-xs text-gray-400 mb-0.5">作者</div>
                        <div class="font-medium text-sm truncate">{{ article.author.name }}</div>
                        <div v-if="article.author.introduction" class="text-xs text-gray-400 truncate">{{ article.author.introduction }}</div>
                      </div>
                    </div>

                    <div v-if="isOwnArticle" class="flex items-center gap-2">
                      <button @click="editArticle" class="px-3 py-1.5 rounded-full text-sm bg-brand-100 text-brand-600 dark:bg-brand-900/50 dark:text-brand-400 hover:opacity-80">
                        ✏️ 编辑
                      </button>
                      <button @click="deleteArticle" class="px-3 py-1.5 rounded-full text-sm bg-red-100 text-red-600 dark:bg-red-900/50 dark:text-red-400 hover:opacity-80">
                        🗑️ 删除
                      </button>
                    </div>

                    <div class="flex items-center gap-3">
                      <button @click="handleLike" :class="[
                        article.isLiked ? 'bg-red-50 text-red-500 dark:bg-red-950/30' : (isDark ? 'bg-gray-800 text-gray-400' : 'bg-gray-100 text-gray-600')
                      ]" class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm">
                        <svg class="w-4 h-4" :fill="article.isLiked ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                        </svg>
                        <span>{{ article.likeCount }}</span>
                      </button>

                      <button @click="handleFavorite" :class="[
                        article.isFavorited ? 'bg-yellow-50 text-yellow-500 dark:bg-yellow-950/30' : (isDark ? 'bg-gray-800 text-gray-400' : 'bg-gray-100 text-gray-600')
                      ]" class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm">
                        <svg class="w-4 h-4" :fill="article.isFavorited ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"></path>
                        </svg>
                        <span>{{ article.favoriteCount }}</span>
                      </button>

                      <button @click="scrollToSection('comments')" :class="isDark ? 'bg-gray-800 text-gray-400' : 'bg-gray-100 text-gray-600'" class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                        </svg>
                        <span>{{ article.commentCount }}</span>
                      </button>
                    </div>
                  </div>
                </div>

                <div class="p-6 lg:p-8">
                  <div class="article-content prose max-w-none" :class="isDark ? 'prose-invert' : ''" v-html="renderedContent"></div>

                  <div v-if="article.tags && article.tags.length" class="flex flex-wrap gap-2 mt-8 pt-6 border-t" :class="isDark ? 'border-gray-800' : 'border-gray-100'">
                    <span class="text-sm text-gray-500 mr-2">🏷️ 标签：</span>
                    <span v-for="tag in article.tags" :key="tag" class="px-2 py-1 rounded-full text-xs bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400">
                      #{{ tag }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div class="lg:col-span-3 xl:col-span-2">
              <div class="sticky top-24">
                <div id="comments" class="rounded-lg border p-4" :class="isDark ? 'bg-gray-900/50 border-gray-800' : 'bg-white border-gray-100 shadow-sm'">
                  <div class="flex items-center justify-between mb-4 pb-2 border-b" :class="isDark ? 'border-gray-800' : 'border-gray-200'">
                    <div class="flex items-center gap-2">
                      <svg class="w-4 h-4 text-brand-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                      </svg>
                      <span class="text-sm font-semibold">评论</span>
                      <span class="text-xs text-gray-400">({{ article.commentCount }})</span>
                    </div>
                  </div>

                  <div class="mb-6">
                    <div v-if="replyTo" class="flex items-center justify-between text-xs mb-2 p-2 rounded" :class="isDark ? 'bg-brand-950/30 text-brand-400' : 'bg-brand-50 text-brand-600'">
                      <span>回复 @{{ replyTo.userName }}</span>
                      <button @click="cancelReply" class="hover:opacity-70">✕</button>
                    </div>
                    <textarea
                      v-model="commentInput"
                      placeholder="写下你的想法..."
                      rows="3"
                      class="comment-input w-full p-3 rounded-lg border text-sm outline-none resize-none transition-colors"
                      :class="isDark ? 'bg-gray-800 text-gray-200 border-gray-700' : 'bg-gray-50 text-gray-800 border-gray-200'"
                    ></textarea>
                    <div class="flex justify-end mt-2">
                      <button
                        @click="submitComment"
                        :disabled="submittingComment"
                        class="px-4 py-1.5 rounded-lg text-sm font-medium bg-gradient-to-r from-brand-500 to-pink-500 text-white hover:opacity-90 disabled:opacity-50"
                      >
                        {{ submittingComment ? '发送中...' : '发表评论' }}
                      </button>
                    </div>
                  </div>

                  <div v-if="loadingComments" class="flex justify-center py-8">
                    <div class="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
                  </div>

                  <div v-else-if="comments.length === 0" class="text-center py-8 text-gray-400 text-sm">
                    <svg class="w-12 h-12 mx-auto mb-3 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                    </svg>
                    暂无评论，快来抢沙发吧~
                  </div>

                  <div v-else class="space-y-4 max-h-[500px] overflow-y-auto pr-1">
                    <div v-for="comment in comments" :key="comment.id" class="comment-item rounded-lg border p-3 transition-colors" :class="isDark ? 'border-gray-800 bg-gray-900/30 hover:bg-gray-800/60' : 'border-gray-100 bg-gray-50/70 hover:bg-white'">
                      <div class="flex gap-2">
                        <img :src="comment.userAvatar || '/default-avatar.png'" alt="头像" class="w-8 h-8 rounded-full ring-2 ring-brand-100 dark:ring-brand-950 flex-shrink-0">
                        <div class="flex-1">
                          <div class="flex items-center gap-2 flex-wrap mb-1">
                            <span class="text-sm font-medium">{{ comment.userName }}</span>
                            <span class="text-xs text-gray-400">{{ formatDate(comment.createTime) }}</span>
                          </div>
                          <p class="text-sm leading-6 mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-700'">
                            {{ comment.content }}
                          </p>
                          <div class="flex items-center gap-3">
                            <button v-if="canReplyToComment(comment)" @click="replyToComment(comment)" class="text-xs text-gray-400 transition-colors hover:text-brand-600">回复</button>
                            <button @click="handleCommentLike(comment)" class="text-xs transition-colors" :class="isCommentLiked(comment) ? 'text-red-500' : 'text-gray-400 hover:text-red-500'">赞 {{ comment.likesCount || 0 }}</button>
                          </div>

                          <div v-if="comment.children && comment.children.length" class="mt-3 ml-1 space-y-2 border-l-2 pl-3" :class="isDark ? 'border-gray-800' : 'border-gray-100'">
                            <div v-for="reply in comment.children" :key="reply.id" class="rounded-md px-2.5 py-2" :class="isDark ? 'bg-gray-800/60' : 'bg-white'">
                              <div class="flex gap-2">
                                <img :src="reply.userAvatar || '/default-avatar.png'" alt="头像" class="w-6 h-6 rounded-full ring-1 ring-brand-100 dark:ring-brand-950 flex-shrink-0">
                                <div class="flex-1">
                                  <div class="flex items-center gap-2 flex-wrap mb-1">
                                    <span class="text-xs font-semibold" :class="isDark ? 'text-gray-200' : 'text-gray-700'">{{ reply.userName }}</span>
                                    <span class="text-xs text-gray-400">{{ formatDate(reply.createTime) }}</span>
                                  </div>
                                  <p class="text-xs leading-relaxed" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                                    <span v-if="reply.replyToUserName" class="mr-1 font-medium text-brand-600 dark:text-brand-400">@{{ reply.replyToUserName }}</span>
                                    {{ reply.content }}
                                  </p>
                                  <div class="flex items-center gap-3 mt-2">
                                    <button v-if="canReplyToComment(reply)" @click="replyToComment(reply)" class="text-xs text-gray-400 transition-colors hover:text-brand-600">回复</button>
                                    <button @click="handleCommentLike(reply)" class="text-xs transition-colors" :class="isCommentLiked(reply) ? 'text-red-500' : 'text-gray-400 hover:text-red-500'">赞 {{ reply.likesCount || 0 }}</button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ================== 编辑弹窗（和 Publish 一模一样） ================== -->
      <Modal v-model:open="editModal" title="编辑文章" width="900px" :footer="null">
        <div class="p-2 space-y-3">
          <input v-model="editForm.title" class="w-full p-2 border rounded dark:bg-gray-800" placeholder="标题" />
          <textarea v-model="editForm.summary" class="w-full p-2 border rounded dark:bg-gray-800" rows="2" placeholder="摘要"></textarea>

          <div class="flex gap-1 flex-wrap">
            <button v-for="tool in editToolbar" :key="tool.icon" class="w-7 h-7 border rounded flex items-center justify-center">
              {{ tool.icon }}
            </button>
          </div>

          <textarea v-model="editForm.content" rows="10" class="w-full p-2 border rounded dark:bg-gray-800"></textarea>

          <div class="flex gap-2">
            <input v-model="tagInput" @keyup.enter="addEditTag" class="flex-1 p-2 border rounded dark:bg-gray-800" placeholder="标签" />
            <button @click="addEditTag" class="px-3 bg-brand-500 text-white rounded">添加</button>
          </div>

          <div class="flex gap-1 flex-wrap">
            <span v-for="t in editForm.tags" :key="t" class="px-2 py-1 bg-brand-100 text-brand-600 rounded-full text-xs">
              {{ t }} <button @click="removeEditTag(t)">×</button>
            </span>
          </div>

          <button @click="submitEdit" class="w-full py-2 bg-brand-600 text-white rounded">保存修改</button>
        </div>
      </Modal>

    </div>
  </div>
</template>

<style scoped>
@import 'highlight.js/styles/github-dark.css';

.article-content :deep(h1) {
  font-size: 1.875rem;
  font-weight: 700;
  margin-top: 2rem;
  margin-bottom: 1rem;
  scroll-margin-top: 100px;
}

.article-content :deep(h2) {
  font-size: 1.5rem;
  font-weight: 600;
  margin-top: 1.75rem;
  margin-bottom: 0.875rem;
  scroll-margin-top: 100px;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.dark .article-content :deep(h2) {
  border-color: #1f2937;
}

.article-content :deep(h3) {
  font-size: 1.25rem;
  font-weight: 600;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  scroll-margin-top: 100px;
}

.article-content :deep(p) {
  margin-bottom: 1rem;
  line-height: 1.75;
}

.article-content :deep(strong) {
  font-weight: 600;
  color: inherit;
}

.article-content :deep(em) {
  font-style: italic;
}

.article-content :deep(del) {
  text-decoration: line-through;
  opacity: 0.7;
}

.article-content :deep(a) {
  color: var(--theme-primary);
  text-decoration: underline;
  text-decoration-thickness: 2px;
  text-underline-offset: 4px;
}

.article-content :deep(a:hover) {
  color: var(--theme-secondary);
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0.75rem;
  margin: 1.5rem 0;
}

.article-content :deep(blockquote) {
  border-left: 4px solid var(--theme-primary);
  padding-left: 1rem;
  margin: 1rem 0;
  font-style: italic;
  color: #6b7280;
}

.dark .article-content :deep(blockquote) {
  color: #9ca3af;
}

.article-content :deep(ul), .article-content :deep(ol) {
  margin: 1rem 0;
  padding-left: 1.5rem;
}

.article-content :deep(li) {
  margin: 0.25rem 0;
  line-height: 1.6;
}

.article-content :deep(ul) {
  list-style-type: disc;
}

.article-content :deep(ol) {
  list-style-type: decimal;
}

.article-content :deep(code) {
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 0.875rem;
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  background-color: #f3f4f6;
}

.dark .article-content :deep(code) {
  background-color: #1f2937;
}

.article-content :deep(pre) {
  background-color: #1f2937;
  color: #f9fafb;
  padding: 1rem;
  border-radius: 0.75rem;
  overflow-x: auto;
  margin: 1.25rem 0;
}

.article-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: inherit;
}

.article-content :deep(hr) {
  margin: 2rem 0;
  border: none;
  height: 1px;
  background: linear-gradient(to right, transparent, #e5e7eb, transparent);
}

.dark .article-content :deep(hr) {
  background: linear-gradient(to right, transparent, #374151, transparent);
}

.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #c7d2fe;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #818cf8;
}

.dark .overflow-y-auto::-webkit-scrollbar-track {
  background: #1e293b;
}

.dark .overflow-y-auto::-webkit-scrollbar-thumb {
  background: #334155;
}
</style>
