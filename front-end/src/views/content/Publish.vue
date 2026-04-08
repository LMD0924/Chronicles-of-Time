<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import Nav from '@/components/Nav.vue';
import {
  ThemeType,
  getStoredTheme,
  onThemeChange
} from "@/utils/theme.js";
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import request from "@/utils/request.js";

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight: function(str, lang) {
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

const transitionRef = ref(null);
const router = useRouter()
const route = useRoute()
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const [messageApi, contextHolder] = message.useMessage();

const isEdit = ref(false)
const contentId = ref(null)

const formData = ref({
  title: '',
  summary: '',
  content: `# 欢迎使用 Markdown 编辑器

## 一级标题示例
这是一段普通文本，**粗体文字**、*斜体文字*、~~删除线文字~~。

## 代码块示例

\`\`\`javascript
// 这是一段 JavaScript 代码
function helloWorld() {
  console.log('Hello, World!');
  return '欢迎来到拾光记';
}
\`\`\`

## 列表示例

- 无序列表项 1
- 无序列表项 2
  - 嵌套列表项
  - 另一个嵌套项

## 引用示例

> 这是一段引用文字
> 可以有多行内容

## 链接和图片

[点击访问拾光记](https://example.com)

![示例图片](https://picsum.photos/200/150)

## 表格示例

| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 内容1 | 内容2 | 内容3 |
| 数据A | 数据B | 数据C |

---
*最后更新于 2024年*`,
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

const showPreview = ref(false)
const previewContent = ref('')

const menuItems = [
  { key: 'journal', label: '云边小札', icon: '📖', path: '/List' },
  { key: 'publish', label: '分享小札', icon: '✍️', path: '/Publish' },
]

const toolbarOptions = [
  { icon: 'H1', action: 'h1', title: '一级标题', prefix: '# ', suffix: '' },
  { icon: 'H2', action: 'h2', title: '二级标题', prefix: '## ', suffix: '' },
  { icon: 'H3', action: 'h3', title: '三级标题', prefix: '### ', suffix: '' },
  { icon: 'B', action: 'bold', title: '粗体', prefix: '**', suffix: '**' },
  { icon: 'I', action: 'italic', title: '斜体', prefix: '*', suffix: '*' },
  { icon: '~', action: 'strike', title: '删除线', prefix: '~~', suffix: '~~' },
  { icon: '•', action: 'ul', title: '无序列表', prefix: '- ', suffix: '' },
  { icon: '1.', action: 'ol', title: '有序列表', prefix: '1. ', suffix: '' },
  { icon: '"', action: 'quote', title: '引用', prefix: '> ', suffix: '' },
  { icon: '🔗', action: 'link', title: '链接', prefix: '[', suffix: '](url)' },
  { icon: '🖼️', action: 'image', title: '图片', prefix: '![', suffix: '](url)' },
  { icon: '```', action: 'code', title: '代码块', prefix: '```\n', suffix: '\n```' },
  { icon: '📏', action: 'hr', title: '分割线', prefix: '\n---\n', suffix: '' }
]

const wordCount = computed(() => {
  const text = formData.value.content.replace(/[#*`>\-\[\]()]/g, '')
  return text.length
})

const readTime = computed(() => {
  const wordsPerMinute = 300
  const minutes = Math.ceil(wordCount.value / wordsPerMinute)
  return minutes > 0 ? minutes : 1
})

const renderMarkdown = (content) => {
  if (!content) return ''
  try {
    return md.render(content)
  } catch (e) {
    console.error('Markdown 渲染失败', e)
    return content
  }
}

const previewMarkdown = () => {
  if (!formData.value.content.trim()) {
    messageApi.warning('请先输入内容')
    return
  }
  showPreview.value = true
  previewContent.value = renderMarkdown(formData.value.content)
}

const insertFormat = (tool) => {
  const textarea = document.querySelector('#content-editor')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = formData.value.content.substring(start, end)

  let formattedText = ''
  let cursorOffset = 0

  if (tool.action === 'hr') {
    formattedText = tool.prefix
    cursorOffset = 0
  } else if (selectedText) {
    formattedText = tool.prefix + selectedText + tool.suffix
    cursorOffset = tool.prefix.length
  } else {
    formattedText = tool.prefix + tool.suffix
    cursorOffset = tool.prefix.length
  }

  const newContent = formData.value.content.substring(0, start) + formattedText + formData.value.content.substring(end)
  formData.value.content = newContent

  nextTick(() => {
    textarea.focus()
    const newCursorPos = start + tool.prefix.length + (selectedText ? selectedText.length : 0)
    textarea.setSelectionRange(newCursorPos, newCursorPos)
  })
}

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

const initScrollAnimation = () => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animated')
        observer.unobserve(entry.target)
      }
    })
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  })

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

// ======================
// ✅ 和你头像上传完全一样 · 封面上传（100% 成功）
// ======================
const handleCoverClick = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    // 完全和你头像一样的校验
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

      // ✅ 完全和你头像上传一模一样！！！
      const uploadRes = await request.upload('/upload/file', file, (percent) => {
        console.log('上传进度:', percent)
      })

      if (uploadRes.code !== 200) {
        throw new Error(uploadRes.message || '上传失败')
      }

      // ✅ 赋值给封面
      formData.value.coverImage = uploadRes.data.url
      messageApi.success('上传成功！')

    } catch (err) {
      console.error('上传失败', err)
      messageApi.error('上传失败')
    }
  }

  input.click()
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

              <div class="flex flex-wrap gap-1 p-2 rounded-lg scroll-animate" :class="isDark ? 'bg-gray-900/50' : 'bg-gray-50'">
                <button
                  v-for="tool in toolbarOptions"
                  :key="tool.action"
                  @click="insertFormat(tool)"
                  :title="tool.title"
                  class="w-8 h-8 rounded-md flex items-center justify-center text-sm font-mono transition-all hover:bg-indigo-100 dark:hover:bg-indigo-900/50"
                  :class="isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600'"
                >
                  {{ tool.icon }}
                </button>
                <div class="w-px h-6 mx-1 bg-gray-300 dark:bg-gray-700"></div>
                <button
                  @click="previewMarkdown"
                  class="px-3 h-8 rounded-md text-sm transition-all hover:bg-indigo-100 dark:hover:bg-indigo-900/50"
                  :class="isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600'"
                >
                  👁️ 预览
                </button>
              </div>

              <div class="scroll-animate">
                <textarea
                  id="content-editor"
                  v-model="formData.content"
                  rows="20"
                  class="w-full p-4 rounded-xl font-mono text-sm outline-none resize-y transition-colors"
                  :class="isDark ? 'bg-gray-900/50 text-gray-200 border border-gray-700' : 'bg-gray-50 text-gray-800 border border-gray-200'"
                ></textarea>
              </div>

              <div class="flex justify-between items-center text-sm scroll-animate" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                <span>📝 字数: {{ wordCount }}</span>
                <span>⏱️ 阅读时间: 约 {{ readTime }} 分钟</span>
              </div>

              <div class="flex gap-4 justify-end scroll-animate">
                <button
                  @click="saveContent(0)"
                  class="px-6 py-2 rounded-full text-sm font-medium transition-all border"
                  :class="isDark ? 'border-gray-700 text-gray-300 hover:border-gray-600' : 'border-gray-300 text-gray-600 hover:border-indigo-400 hover:text-indigo-600'"
                >
                  保存草稿
                </button>
                <button
                  @click="saveContent(formData.status)"
                  class="px-6 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-full text-sm font-medium shadow-lg hover:shadow-xl transition-all hover:-translate-y-0.5"
                >
                  {{ isEdit ? '更新发布' : '发布文章' }}
                </button>
              </div>
            </div>

            <div class="space-y-6">
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
                  <div class="text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                    公开内容将展示在个人主页和内容广场
                  </div>
                </div>
              </div>

              <div class="rounded-2xl p-5 scroll-animate" :class="isDark ? 'bg-gradient-to-r from-indigo-900/20 to-purple-900/20 border border-indigo-500/20' : 'bg-gradient-to-r from-indigo-50 to-purple-50'">
                <div class="flex items-start gap-3">
                  <span class="text-2xl">💡</span>
                  <div>
                    <h4 class="font-semibold text-sm mb-1">Markdown 小贴士</h4>
                    <ul class="text-xs space-y-1" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                      <li>使用 # 到 ### 创建标题</li>
                      <li>使用 **粗体** 和 *斜体* 强调文字</li>
                      <li>使用 - 或 1. 创建列表</li>
                      <li>使用 > 添加引用</li>
                      <li>使用 ``` 包裹代码块</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="showPreview" class="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm" @click.self="showPreview = false">
        <div class="relative max-w-4xl w-full max-h-[85vh] overflow-y-auto rounded-2xl shadow-2xl" :class="isDark ? 'bg-gray-900' : 'bg-white'">
          <div class="sticky top-0 flex justify-between items-center p-4 border-b z-10" :class="isDark ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-200'">
            <h3 class="font-semibold">预览</h3>
            <button @click="showPreview = false" class="w-8 h-8 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">✕</button>
          </div>
          <div class="p-6 markdown-preview" v-html="previewContent"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
@import 'highlight.js/styles/github-dark.css';

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.scroll-animate {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.scroll-animate.animated {
  opacity: 1;
  transform: translateY(0);
}

.markdown-preview {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
}

.dark .markdown-preview {
  color: #e5e7eb;
}

.markdown-preview h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 1.5rem 0 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.markdown-preview h2 {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 1.3rem 0 0.8rem;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid #e5e7eb;
}

.markdown-preview h3 {
  font-size: 1.35rem;
  font-weight: 600;
  margin: 1.1rem 0 0.6rem;
}

.markdown-preview h4 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 1rem 0 0.5rem;
}

.markdown-preview h5, .markdown-preview h6 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0.9rem 0 0.5rem;
}

.dark .markdown-preview h1,
.dark .markdown-preview h2 {
  border-bottom-color: #374151;
}

.markdown-preview p {
  margin: 0.8rem 0;
  line-height: 1.8;
}

.markdown-preview ul,
.markdown-preview ol {
  margin: 0.8rem 0;
  padding-left: 2rem;
}

.markdown-preview li {
  margin: 0.3rem 0;
  line-height: 1.7;
}

.markdown-preview ul {
  list-style-type: disc;
}

.markdown-preview ol {
  list-style-type: decimal;
}

.markdown-preview blockquote {
  margin: 1rem 0;
  padding: 0.5rem 1rem;
  border-left: 4px solid #6366f1;
  background-color: #f8f9fa;
  color: #6c757d;
  font-style: italic;
}

.dark .markdown-preview blockquote {
  background-color: #1f2937;
  color: #9ca3af;
}

.markdown-preview pre {
  background-color: #1e1e1e;
  border-radius: 8px;
  padding: 1rem;
  overflow-x: auto;
  margin: 1rem 0;
}

.markdown-preview pre code {
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #d4d4d4;
}

.markdown-preview code:not(pre code) {
  padding: 0.2rem 0.4rem;
  background-color: #f3f4f6;
  border-radius: 4px;
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 0.9em;
  color: #e11d48;
}

.dark .markdown-preview code:not(pre code) {
  background-color: #374151;
  color: #f87171;
}

.markdown-preview a {
  color: #6366f1;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.markdown-preview a:hover {
  border-bottom-color: #6366f1;
}

.markdown-preview img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1rem 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.markdown-preview table {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
  overflow-x: auto;
  display: block;
}

.markdown-preview th,
.markdown-preview td {
  border: 1px solid #e5e7eb;
  padding: 0.5rem 1rem;
  text-align: left;
}

.markdown-preview th {
  background-color: #f9fafb;
  font-weight: 600;
}

.dark .markdown-preview th,
.dark .markdown-preview td {
  border-color: #374151;
}

.dark .markdown-preview th {
  background-color: #1f2937;
}

.markdown-preview hr {
  margin: 2rem 0;
  border: none;
  height: 1px;
  background: linear-gradient(to right, transparent, #e5e7eb, transparent);
}

.dark .markdown-preview hr {
  background: linear-gradient(to right, transparent, #374151, transparent);
}

.markdown-preview strong {
  font-weight: 700;
  color: inherit;
}

.markdown-preview em {
  font-style: italic;
}

.markdown-preview del {
  text-decoration: line-through;
  opacity: 0.7;
}
</style>
