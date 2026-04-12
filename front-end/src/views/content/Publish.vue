<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import Nav from '@/components/Nav.vue';
import MarkdownEditor from '@/components/MarkdownEditor.vue';
import {
  ThemeType,
  getStoredTheme,
  onThemeChange
} from "@/utils/theme.js";
import request from "@/utils/request.js";

const router = useRouter()
const route = useRoute()
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const [messageApi, contextHolder] = message.useMessage();

const isEdit = ref(false)
const contentId = ref(null)

const formData = ref({
  title: '',
  summary: '',
  content: '',
  category: '随笔',
  contentType: 'markdown',
  tags: [],
  coverImage: '',
  status: 1
})

const tagInput = ref('')
const tagSuggestions = ref([
  '生活感悟', '技术分享', '读书笔记', '旅行日记', '成长记录', '职场经验'
])

const categories = [
  { value: '随笔', label: '随笔', icon: '✍️' },
  { value: '技术', label: '技术', icon: '💻' },
  { value: '生活', label: '生活', icon: '🌿' },
  { value: '旅行', label: '旅行', icon: '✈️' },
  { value: '读书', label: '读书', icon: '📚' },
  { value: '日记', label: '日记', icon: '📖' }
]

const menuItems = [
  { key: 'journal', label: '云边小札', icon: '📖', path: '/List' },
  { key: 'publish', label: '分享小札', icon: '✍️', path: '/Publish' },
  { key: 'knowledge', label: '知识图谱', icon: '🔗', path: '/ContentKnowledgeGraph' },
]

const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !formData.value.tags.includes(tag) && formData.value.tags.length < 5) {
    formData.value.tags.push(tag)
    tagInput.value = ''
  }
}

const removeTag = (tag) => {
  formData.value.tags = formData.value.tags.filter(t => t !== tag)
}

const saveContent = async (status = 1) => {
  if (!formData.value.title.trim()) {
    messageApi.warning('请输入标题')
    return
  }
  if (!formData.value.content.trim()) {
    messageApi.warning('请输入内容')
    return
  }

  formData.value.status = status

  const dto = {
    id: isEdit.value ? contentId.value : null,
    title: formData.value.title,
    summary: formData.value.summary,
    content: formData.value.content,
    category: formData.value.category,
    contentType: formData.value.contentType,
    tags: formData.value.tags,
    coverImage: formData.value.coverImage,
    status: formData.value.status
  }

  try {
    const res = await request.post('/content/save', dto)
    if (res.code === 200) {
      messageApi.success(status === 1 ? '发布成功' : '保存草稿成功')
      setTimeout(() => {
        router.push('/View/' + (res.data || contentId.value))
      }, 1500)
    } else {
      messageApi.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败', error)
    messageApi.error('保存失败，请稍后重试')
  }
}

const getContentDetail = async () => {
  if (!contentId.value) return

  try {
    const res = await request.get(`/content/detail/${contentId.value}`)
    if (res.code === 200 && res.data) {
      const data = res.data
      formData.value = {
        title: data.title,
        summary: data.summary || '',
        content: data.content,
        category: data.category || '随笔',
        contentType: data.contentType || 'markdown',
        tags: data.tags ? (Array.isArray(data.tags) ? data.tags : data.tags.split(',')) : [],
        coverImage: data.coverImage || '',
        status: data.status
      }
    }
  } catch (error) {
    console.error('获取内容失败', error)
    messageApi.error('获取内容失败')
  }
}

const handleMenuClick = (item) => {
  if (item.path) {
    router.push(item.path)
  }
}

const handleLogoClick = () => {
  router.push('/home')
}

const handleCoverClick = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      messageApi.error('请选择图片文件（JPG、PNG、GIF、WEBP）')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      messageApi.error('图片大小不能超过5MB')
      return
    }

    try {
      messageApi.loading('上传中...', 0)
      const uploadRes = await request.upload('/upload/file', file, (percent) => {
        console.log('上传进度:', percent)
      })

      if (uploadRes.code !== 200) {
        throw new Error(uploadRes.message || '上传失败')
      }

      formData.value.coverImage = uploadRes.data.url
      messageApi.success('上传成功！')
    } catch (err) {
      console.error('上传失败', err)
      messageApi.error('上传失败')
    }
  }

  input.click()
}

const handleSave = () => {
  saveContent(formData.value.status)
}

const initScrollAnimation = () => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animated')
        observer.unobserve(entry.target)
      }
    })
  }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' })

  const animatedElements = document.querySelectorAll('.scroll-animate')
  animatedElements.forEach(el => observer.observe(el))
}

const handleThemeChange = (theme) => {
  const newTheme = theme === ThemeType.DARK
  isDark.value = newTheme
  if (newTheme) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  nextTick(() => {
    initScrollAnimation()
  })
}

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    contentId.value = parseInt(route.params.id)
    getContentDetail()
  }

  if (isDark.value) {
    document.documentElement.classList.add('dark')
  }

  setTimeout(initScrollAnimation, 100)

  const stopListen = onThemeChange((theme) => {
    handleThemeChange(theme)
  })

  onUnmounted(() => {
    stopListen()
  })
})
</script>

<template>
  <contextHolder />
  <AdvancedPageTransition ref="transitionRef" :duration="3000" />

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

      <div class="pt-24 pb-16">
        <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
          <div class="grid lg:grid-cols-3 gap-8">
            <div class="lg:col-span-2 space-y-6">
              <!-- 标题 -->
              <div class="scroll-animate">
                <input
                  v-model="formData.title"
                  type="text"
                  placeholder="输入标题..."
                  class="w-full text-3xl lg:text-4xl font-bold bg-transparent border-none outline-none placeholder-gray-400"
                  :class="isDark ? 'text-white placeholder-gray-500' : 'text-gray-900 placeholder-gray-400'"
                >
                <div class="h-px bg-gradient-to-r from-transparent via-gray-300 to-transparent mt-2"></div>
              </div>

              <!-- 摘要 -->
              <div class="scroll-animate">
                <textarea
                  v-model="formData.summary"
                  rows="2"
                  placeholder="写一段摘要，让读者快速了解文章内容..."
                  class="w-full text-base bg-transparent border-none outline-none resize-none placeholder-gray-400"
                  :class="isDark ? 'text-gray-300 placeholder-gray-500' : 'text-gray-600 placeholder-gray-400'"
                ></textarea>
                <div class="h-px bg-gradient-to-r from-transparent via-gray-200 to-transparent mt-2"></div>
              </div>

              <!-- Markdown 编辑器组件 -->
              <div class="scroll-animate">
                <MarkdownEditor
                  v-model:content="formData.content"
                  :isDark="isDark"
                  :showToolbar="true"
                  :showWordCount="true"
                  @save="handleSave"
                />
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-4 justify-end scroll-animate">
                <button
                  @click="saveContent(0)"
                  class="px-6 py-2 rounded-full text-sm font-medium transition-all border"
                  :class="isDark ? 'border-gray-700 text-gray-300 hover:border-gray-600' : 'border-gray-300 text-gray-600 hover:border-indigo-400 hover:text-indigo-600'"
                >
                  保存草稿
                </button>
                <button
                  @click="saveContent(1)"
                  class="px-6 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-full text-sm font-medium shadow-lg hover:shadow-xl transition-all hover:-translate-y-0.5"
                >
                  {{ isEdit ? '更新发布' : '发布文章' }}
                </button>
              </div>
            </div>

            <!-- 右侧面板（保持不变） -->
            <div class="space-y-6">
              <!-- 封面图片 -->
              <div class="rounded-2xl p-5 scroll-animate" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-3 flex items-center gap-2">
                  <span>🖼️</span> 封面图片
                </h3>
                <div
                  v-if="!formData.coverImage"
                  @click="handleCoverClick"
                  class="h-32 rounded-xl border-2 border-dashed flex flex-col items-center justify-center cursor-pointer transition-all hover:border-indigo-400"
                  :class="isDark ? 'border-gray-700 text-gray-500' : 'border-gray-300 text-gray-400'"
                >
                  <svg class="w-8 h-8 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                  </svg>
                  <span class="text-sm">点击上传封面</span>
                </div>
                <div v-else class="relative group">
                  <img :src="formData.coverImage" alt="封面" class="w-full h-32 object-cover rounded-xl">
                  <button
                    @click="formData.coverImage = ''"
                    class="absolute top-2 right-2 w-6 h-6 bg-black/50 rounded-full flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    ✕
                  </button>
                </div>
                <input type="text" v-model="formData.coverImage" placeholder="或输入图片URL" class="mt-3 w-full px-3 py-2 text-sm rounded-lg outline-none" :class="isDark ? 'bg-gray-800 text-gray-300' : 'bg-gray-100 text-gray-600'">
              </div>

              <!-- 分类 -->
              <div class="rounded-2xl p-5 scroll-animate" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-3 flex items-center gap-2">
                  <span>📂</span> 分类
                </h3>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="cat in categories"
                    :key="cat.value"
                    @click="formData.category = cat.value"
                    class="px-3 py-1.5 rounded-full text-sm transition-all flex items-center gap-1"
                    :class="formData.category === cat.value
                      ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white shadow-md'
                      : (isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200')"
                  >
                    <span>{{ cat.icon }}</span>
                    <span>{{ cat.label }}</span>
                  </button>
                </div>
              </div>

              <!-- 标签 -->
              <div class="rounded-2xl p-5 scroll-animate" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-3 flex items-center gap-2">
                  <span>🏷️</span> 标签 (最多5个)
                </h3>
                <div class="flex flex-wrap gap-2 mb-3">
                  <span
                    v-for="tag in formData.tags"
                    :key="tag"
                    class="inline-flex items-center gap-1 px-2 py-1 bg-indigo-100 dark:bg-indigo-900/50 text-indigo-600 dark:text-indigo-400 rounded-full text-xs"
                  >
                    {{ tag }}
                    <button @click="removeTag(tag)" class="hover:text-indigo-800">×</button>
                  </span>
                </div>
                <div class="flex gap-2">
                  <input
                    v-model="tagInput"
                    @keyup.enter="addTag"
                    type="text"
                    placeholder="添加标签"
                    class="flex-1 px-3 py-2 text-sm rounded-lg outline-none"
                    :class="isDark ? 'bg-gray-800 text-gray-300' : 'bg-gray-100 text-gray-600'"
                  >
                  <button @click="addTag" class="px-3 py-2 rounded-lg text-sm bg-indigo-500 text-white hover:bg-indigo-600 transition-colors">
                    添加
                  </button>
                </div>
                <div class="mt-3 flex flex-wrap gap-1">
                  <span class="text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">推荐:</span>
                  <button
                    v-for="sug in tagSuggestions"
                    :key="sug"
                    @click="() => { if(formData.tags.length < 5 && !formData.tags.includes(sug)) formData.tags.push(sug) }"
                    class="text-xs px-2 py-0.5 rounded-full transition-all"
                    :class="isDark ? 'text-gray-500 hover:text-indigo-400' : 'text-gray-400 hover:text-indigo-600'"
                  >
                    {{ sug }}
                  </button>
                </div>
              </div>

              <!-- 发布设置 -->
              <div class="rounded-2xl p-5 scroll-animate" :class="isDark ? 'bg-gray-900/50' : 'bg-white shadow-sm'">
                <h3 class="font-semibold mb-3 flex items-center gap-2">
                  <span>⚙️</span> 发布设置
                </h3>
                <div class="space-y-3">
                  <label class="flex items-center justify-between cursor-pointer">
                    <span class="text-sm">可见性</span>
                    <select v-model="formData.status" class="px-3 py-1.5 text-sm rounded-lg outline-none" :class="isDark ? 'bg-gray-800 text-gray-300' : 'bg-gray-100 text-gray-600'">
                      <option :value="1">公开</option>
                      <option :value="0">仅自己可见</option>
                    </select>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.scroll-animate {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.scroll-animate.animated {
  opacity: 1;
  transform: translateY(0);
}
</style>
