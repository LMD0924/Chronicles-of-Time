<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const props = defineProps({
  // 内容
  content: {
    type: String,
    default: ''
  },
  // 占位符
  placeholder: {
    type: String,
    default: `# 欢迎使用 Markdown 编辑器

## 一级标题示例
这是一段普通文本，**粗体文字**、*斜体文字*、~~删除线文字~~。

## 代码块示例

\`\`\`javascript
// 这是一段 JavaScript 代码
function helloWorld() {
  console.log('Hello, World!');
  return '欢迎使用 Markdown 编辑器';
}
\`\`\`

## 列表示例

- 无序列表项 1
- 无序列表项 2
  - 嵌套列表项

## 引用示例

> 这是一段引用文字

## 表格示例

| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 内容1 | 内容2 | 内容3 |
`
  },
  // 是否显示字数统计
  showWordCount: {
    type: Boolean,
    default: true
  },
  // 是否显示工具栏
  showToolbar: {
    type: Boolean,
    default: true
  },
  // 编辑器高度
  height: {
    type: String,
    default: 'auto'
  },
  // 主题
  isDark: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:content', 'save'])

// Markdown 配置
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

const localContent = ref(props.content)
const showPreview = ref(false)
const previewContent = ref('')
const editorRef = ref(null)

// 工具栏选项
const toolbarOptions = [
  { icon: 'H1', action: 'h1', title: '一级标题', prefix: '# ', suffix: '', needSelection: false },
  { icon: 'H2', action: 'h2', title: '二级标题', prefix: '## ', suffix: '', needSelection: false },
  { icon: 'H3', action: 'h3', title: '三级标题', prefix: '### ', suffix: '', needSelection: false },
  { icon: 'B', action: 'bold', title: '粗体', prefix: '**', suffix: '**', needSelection: true },
  { icon: 'I', action: 'italic', title: '斜体', prefix: '*', suffix: '*', needSelection: true },
  { icon: '~', action: 'strike', title: '删除线', prefix: '~~', suffix: '~~', needSelection: true },
  { icon: '•', action: 'ul', title: '无序列表', prefix: '- ', suffix: '', needSelection: false },
  { icon: '1.', action: 'ol', title: '有序列表', prefix: '1. ', suffix: '', needSelection: false },
  { icon: '"', action: 'quote', title: '引用', prefix: '> ', suffix: '', needSelection: false },
  { icon: '🔗', action: 'link', title: '链接', prefix: '[', suffix: '](url)', needSelection: true },
  { icon: '🖼️', action: 'image', title: '图片', prefix: '![', suffix: '](url)', needSelection: true },
  { icon: '```', action: 'code', title: '代码块', prefix: '```\n', suffix: '\n```', needSelection: false },
  { icon: '📏', action: 'hr', title: '分割线', prefix: '\n---\n', suffix: '', needSelection: false }
]

// 字数统计
const wordCount = computed(() => {
  if (!localContent.value) return 0
  const text = localContent.value.replace(/[#*`>\-\[\]()]/g, '')
  return text.length
})

// 阅读时间
const readTime = computed(() => {
  const wordsPerMinute = 300
  const minutes = Math.ceil(wordCount.value / wordsPerMinute)
  return minutes > 0 ? minutes : 1
})

// 监听外部内容变化
watch(() => props.content, (newVal) => {
  if (newVal !== localContent.value) {
    localContent.value = newVal
  }
})

// 监听本地内容变化
watch(localContent, (newVal) => {
  emit('update:content', newVal)
})

// 渲染 Markdown
const renderMarkdown = (content) => {
  if (!content) return ''
  try {
    return md.render(content)
  } catch (e) {
    console.error('Markdown 渲染失败', e)
    return content
  }
}

// 预览
const previewMarkdown = () => {
  if (!localContent.value.trim()) {
    message.warning('请先输入内容')
    return
  }
  showPreview.value = true
  previewContent.value = renderMarkdown(localContent.value)
}

// 插入格式
const insertFormat = (tool) => {
  const textarea = editorRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = localContent.value.substring(start, end)

  let formattedText = ''
  let cursorOffset = 0

  if (tool.action === 'hr') {
    formattedText = tool.prefix
    cursorOffset = 0
  } else if (selectedText && tool.needSelection) {
    formattedText = tool.prefix + selectedText + tool.suffix
    cursorOffset = tool.prefix.length
  } else {
    formattedText = tool.prefix + tool.suffix
    cursorOffset = tool.prefix.length
  }

  const newContent = localContent.value.substring(0, start) + formattedText + localContent.value.substring(end)
  localContent.value = newContent

  nextTick(() => {
    textarea.focus()
    const newCursorPos = start + tool.prefix.length + (selectedText ? selectedText.length : 0)
    textarea.setSelectionRange(newCursorPos, newCursorPos)
  })
}

// 快捷键保存
const handleKeydown = (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    emit('save')
  }
}

defineExpose({
  getContent: () => localContent.value,
  setContent: (content) => { localContent.value = content }
})
</script>

<template>
  <div class="markdown-editor">
    <!-- 工具栏 -->
    <div v-if="showToolbar" class="flex flex-wrap gap-1 p-2 rounded-t-lg" :class="isDark ? 'bg-gray-900/50' : 'bg-gray-50'">
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

    <!-- 编辑器主体 -->
    <textarea
      ref="editorRef"
      v-model="localContent"
      :placeholder="placeholder"
      :style="{ height: height === 'auto' ? '500px' : height }"
      class="w-full p-4 rounded-b-lg font-mono text-sm outline-none resize-y transition-colors"
      :class="[
    showToolbar ? '' : 'rounded-t-lg',
    isDark ? 'bg-gray-900/50 text-gray-200 border border-gray-700' : 'bg-gray-50 text-gray-800 border border-gray-200'
  ]"
      @keydown="handleKeydown"
    ></textarea>

    <!-- 底部统计 -->
    <div v-if="showWordCount" class="flex justify-between items-center text-sm mt-2 px-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
      <span>📝 字数: {{ wordCount }}</span>
      <span>⏱️ 阅读时间: 约 {{ readTime }} 分钟</span>
      <span class="text-xs">
        💡 提示: Ctrl+S 快速保存
      </span>
    </div>

    <!-- 预览弹窗 -->
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
</template>

<style scoped>
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
</style>
