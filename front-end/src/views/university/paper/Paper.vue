<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { message,Modal } from 'ant-design-vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue';
import request from "@/utils/request.js";
import Nav from "@/components/Nav.vue";

const router = useRouter()
const isDark = ref(document.documentElement.classList.contains('dark'))
const [messageApi, contextHolder] = message.useMessage();

// 当前论文ID
const currentPaperId = ref(null)
const currentPaper = ref(null)

// 论文数据
const paperTitle = ref('')
const paperContent = ref('')
const selectedDirections = ref([])
const supervisor = ref('')
const saveStatus = ref('已保存')
const autoSaveTimer = ref(null)
const lastSaveTime = ref(null)

// 论文分类
const selectedCategory = ref('毕业论文')
const paperCategories = [
  { value: '毕业论文', label: '🎓 毕业论文', color: 'purple' },
  { value: '课程论文', label: '📚 课程论文', color: 'blue' },
  { value: '期刊论文', label: '📰 期刊论文', color: 'green' },
  { value: '会议论文', label: '🎤 会议论文', color: 'orange' },
]

// AI 建议相关
const showSuggestions = ref(false)
const suggestions = ref([])
const suggestionsLoading = ref(false)
const suggestionsHistory = ref([])

// 研究方向选项
const directionOptions = [
  { id: 1, name: '人工智能', icon: '🤖', color: 'from-blue-500 to-cyan-500' },
  { id: 2, name: '机器学习', icon: '📊', color: 'from-green-500 to-teal-500' },
  { id: 3, name: '深度学习', icon: '🧠', color: 'from-purple-500 to-pink-500' },
  { id: 4, name: '计算机视觉', icon: '👁️', color: 'from-orange-500 to-red-500' },
  { id: 5, name: '自然语言处理', icon: '💬', color: 'from-indigo-500 to-blue-500' },
  { id: 6, name: '大数据', icon: '📈', color: 'from-yellow-500 to-orange-500' },
  { id: 7, name: '云计算', icon: '☁️', color: 'from-sky-500 to-blue-500' },
  { id: 8, name: '区块链', icon: '🔗', color: 'from-emerald-500 to-green-500' },
]

const menuItems = [
  { key: '论文', label: '写论文', icon: '📄', path:'/Paper' },
]

const handleMenuClick = (item) => {
  if (item.path) {
    router.push(item.path)
  }
}

const handleLogoClick = () => {
  router.push('/home')
}

// 论文列表
const paperList = ref([])
const showPaperList = ref(false)
const sidebarCollapsed = ref(false)
const paperSearchKeyword = ref('')

// 是否有未保存的内容
const hasUnsavedChanges = computed(() => {
  return saveStatus.value === '未保存'
})

// 过滤后的论文列表
const filteredPaperList = computed(() => {
  if (!paperSearchKeyword.value) return paperList.value
  return paperList.value.filter(p =>
    p.title.toLowerCase().includes(paperSearchKeyword.value.toLowerCase())
  )
})

// 论文字数统计
const paperWordCount = computed(() => {
  if (!paperContent.value) return 0
  const text = paperContent.value.replace(/[#*`>\-\[\]()]/g, '')
  return text.length
})

// 论文预计阅读时间
const paperReadTime = computed(() => {
  const wordsPerMinute = 300
  const minutes = Math.ceil(paperWordCount.value / wordsPerMinute)
  return minutes > 0 ? minutes : 1
})

// 监听内容变化，触发自动保存
watch([paperTitle, paperContent, selectedDirections, supervisor, selectedCategory], () => {
  if (currentPaperId.value) {
    updateSaveStatus('未保存')
    triggerAutoSave()
    debouncedGetSuggestions()
  }
})

// 防抖获取建议
let suggestionDebounceTimer = null
const debouncedGetSuggestions = () => {
  if (suggestionDebounceTimer) {
    clearTimeout(suggestionDebounceTimer)
  }
  suggestionDebounceTimer = setTimeout(() => {
    if (paperContent.value.length > 200) {
      getSuggestions()
    }
  }, 3000)
}

// 触发自动保存
const triggerAutoSave = () => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
  autoSaveTimer.value = setTimeout(() => {
    savePaper()
  }, 2000)
}

// 更新保存状态
const updateSaveStatus = (status) => {
  saveStatus.value = status
  if (status === '已保存') {
    lastSaveTime.value = new Date()
  }
}

// 保存论文
const savePaper = () => {
  if (!currentPaperId.value) return

  updateSaveStatus('保存中')

  const paperData = {
    id: currentPaperId.value,
    title: paperTitle.value,
    supervisor: supervisor.value,
    direction: selectedDirections.value.map(d => d.name).join(','),
    content: paperContent.value,
    category: selectedCategory.value
  }

  request.put('/paper/update', paperData, (msg, data) => {
    if (data) {
      updateSaveStatus('已保存')
      backupToLocal()
      const index = paperList.value.findIndex(p => p.id === currentPaperId.value)
      if (index !== -1) {
        paperList.value[index].title = paperTitle.value
        paperList.value[index].updatedAt = new Date().toISOString()
      }
    } else {
      updateSaveStatus('保存失败')
      setTimeout(() => {
        if (saveStatus.value === '保存失败') {
          savePaper()
        }
      }, 3000)
    }
  })
}

// 手动保存
const manualSave = () => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
  savePaper()
  messageApi.success('已保存')
}

// 备份到本地
const backupToLocal = () => {
  if (!currentPaperId.value) return
  const backup = {
    id: currentPaperId.value,
    title: paperTitle.value,
    content: paperContent.value,
    supervisor: supervisor.value,
    directions: selectedDirections.value,
    category: selectedCategory.value,
    timestamp: Date.now()
  }
  localStorage.setItem(`paper_backup_${currentPaperId.value}`, JSON.stringify(backup))
}

// 从备份恢复
const restoreFromBackup = () => {
  if (!currentPaperId.value) return
  const backupStr = localStorage.getItem(`paper_backup_${currentPaperId.value}`)
  if (backupStr) {
    const backup = JSON.parse(backupStr)
    const hoursDiff = (Date.now() - backup.timestamp) / (1000 * 60 * 60)
    if (hoursDiff < 24 && backup.content !== paperContent.value && backup.content) {
      if (confirm('检测到有未保存的本地备份，是否恢复？')) {
        paperTitle.value = backup.title
        paperContent.value = backup.content
        supervisor.value = backup.supervisor
        selectedDirections.value = backup.directions || []
        selectedCategory.value = backup.category || '毕业论文'
        messageApi.success('已从备份恢复')
      }
    }
  }
}

// 获取论文列表
const getPaperList = () => {
  request.get('/paper/list', {}, (msg, data) => {
    if (data && Array.isArray(data)) {
      paperList.value = data
      if (data.length > 0 && !currentPaperId.value) {
        loadPaper(data[0].id)
      }
    } else {
      console.error('获取论文列表失败:', msg)
      paperList.value = []
    }
  })
}

// 删除论文
const deletePaper = (id, event) => {
  if (event) {
    event.stopPropagation()
  }

  // 找到要删除的论文标题
  const paperToDelete = paperList.value.find(p => p.id === id)
  const paperTitle = paperToDelete?.title || '这篇论文'

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除《${paperTitle}》吗？删除后无法恢复。`,
    okText: '确认删除',
    cancelText: '取消',
    okType: 'danger',
    centered: true,
    onOk: () => {
      request.delete(`/paper/${id}`, {}, (msg, data) => {
        if (data) {
          messageApi.success('删除成功')

          // 从列表中移除
          const index = paperList.value.findIndex(p => p.id === id)
          if (index !== -1) {
            paperList.value.splice(index, 1)
          }

          // 清除本地备份
          localStorage.removeItem(`paper_backup_${id}`)
          localStorage.removeItem(`paper_suggestions_${id}`)

          // 如果删除的是当前论文，切换到第一篇或清空
          if (currentPaperId.value === id) {
            if (paperList.value.length > 0) {
              loadPaper(paperList.value[0].id)
            } else {
              currentPaperId.value = null
              currentPaper.value = null
              paperTitle.value = ''
              paperContent.value = ''
              supervisor.value = ''
              selectedDirections.value = []
              selectedCategory.value = '毕业论文'
              updateSaveStatus('已保存')
            }
          }
        } else {
          messageApi.error(msg || '删除失败')
        }
      })
    }
  })
}

// 创建新论文
const createNewPaper = () => {
  const newPaper = {
    title: '未命名论文',
    supervisor: '',
    direction: '',
    content: '',
    category: '毕业论文'
  }

  request.post('/paper/create', newPaper, (msg, data) => {
    if (data) {
      messageApi.success('创建成功')
      getPaperList()
      setTimeout(() => {
        const latestPaper = paperList.value[paperList.value.length - 1]
        if (latestPaper) {
          loadPaper(latestPaper.id)
        }
      }, 300)
    } else {
      messageApi.error(msg || '创建失败')
    }
  })
}

// 加载论文
const loadPaper = (id) => {
  request.get(`/paper/${id}`, {}, (msg, data) => {
    if (data) {
      currentPaperId.value = data.id
      currentPaper.value = data
      paperTitle.value = data.title || ''
      supervisor.value = data.supervisor || ''
      paperContent.value = data.content || ''
      selectedCategory.value = data.category || '毕业论文'

      if (data.direction) {
        const directionNames = data.direction.split(',')
        selectedDirections.value = directionOptions.filter(opt =>
          directionNames.includes(opt.name)
        )
      } else {
        selectedDirections.value = []
      }

      updateSaveStatus('已保存')
      showPaperList.value = false

      loadSuggestionsHistory(id)

      setTimeout(() => {
        restoreFromBackup()
      }, 500)
    } else {
      messageApi.error(msg || '加载论文失败')
    }
  })
}

// 加载建议历史
const loadSuggestionsHistory = (paperId) => {
  const historyKey = `paper_suggestions_${paperId}`
  const saved = localStorage.getItem(historyKey)
  if (saved) {
    try {
      suggestionsHistory.value = JSON.parse(saved)
    } catch (e) {
      suggestionsHistory.value = []
    }
  } else {
    suggestionsHistory.value = []
  }
}

// 保存建议到历史
const saveSuggestionToHistory = (suggestion) => {
  suggestionsHistory.value.unshift({
    ...suggestion,
    id: Date.now(),
    timestamp: new Date().toISOString(),
    paperId: currentPaperId.value
  })
  if (suggestionsHistory.value.length > 20) {
    suggestionsHistory.value = suggestionsHistory.value.slice(0, 20)
  }
  localStorage.setItem(`paper_suggestions_${currentPaperId.value}`, JSON.stringify(suggestionsHistory.value))
}

// 获取 AI 建议
const getSuggestions = () => {
  if (!paperContent.value || paperContent.value.length < 200) {
    return
  }

  suggestionsLoading.value = true

  setTimeout(() => {
    const analysis = analyzePaperContent()
    suggestions.value = analysis
    suggestionsLoading.value = false

    if (analysis.length > 0) {
      analysis.forEach(s => saveSuggestionToHistory(s))
    }
  }, 1500)
}

// 分析论文内容
const analyzePaperContent = () => {
  const content = paperContent.value
  const suggestionsList = []

  if (!content.includes('## 摘要') && !content.includes('摘要')) {
    suggestionsList.push({
      type: 'structure',
      title: '缺少摘要部分',
      content: '建议在论文开头添加摘要部分，概述研究背景、方法、结果和结论。',
      priority: 'high',
      icon: '📋'
    })
  }

  if (!content.includes('## 引言') && !content.includes('引言')) {
    suggestionsList.push({
      type: 'structure',
      title: '缺少引言部分',
      content: '引言应该阐述研究背景、问题陈述、研究意义和论文结构。',
      priority: 'high',
      icon: '📖'
    })
  }

  if (!content.includes('## 结论') && !content.includes('结论')) {
    suggestionsList.push({
      type: 'conclusion',
      title: '需要添加结论部分',
      content: '总结主要发现，强调贡献，展望未来工作。',
      priority: 'high',
      icon: '🎯'
    })
  }

  return suggestionsList
}

// 应用建议
const applySuggestion = (suggestion) => {
  let insertText = ''
  switch (suggestion.type) {
    case 'structure':
      if (suggestion.title.includes('摘要')) {
        insertText = '\n\n## 摘要\n\n在此填写摘要内容...\n'
      } else if (suggestion.title.includes('引言')) {
        insertText = '\n\n## 引言\n\n### 研究背景\n\n### 问题陈述\n\n### 研究意义\n\n### 论文结构\n'
      }
      break
    case 'conclusion':
      insertText = '\n\n## 结论\n\n### 主要贡献\n\n1. ...\n2. ...\n\n### 未来工作\n\n- ...\n'
      break
    default:
      insertText = `\n\n> **建议**：${suggestion.content}\n\n`
  }

  paperContent.value = paperContent.value + insertText
  messageApi.success(`已添加${suggestion.title}`)
  showSuggestions.value = false
}

// 获取建议的优先级颜色
const getPriorityColor = (priority) => {
  switch (priority) {
    case 'high': return 'text-red-500 bg-red-50 dark:bg-red-950/30'
    case 'medium': return 'text-yellow-500 bg-yellow-50 dark:bg-yellow-950/30'
    case 'low': return 'text-green-500 bg-green-50 dark:bg-green-950/30'
    default: return 'text-gray-500 bg-gray-50 dark:bg-gray-800'
  }
}

// 获取建议类型标签
const getTypeLabel = (type) => {
  const labels = {
    structure: '结构',
    literature: '文献',
    methodology: '方法',
    results: '结果',
    discussion: '讨论',
    conclusion: '结论',
    citation: '引用',
    figure: '图表',
    code: '代码'
  }
  return labels[type] || '建议'
}

// 导出论文
const exportPaper = () => {
  const content = `# ${paperTitle.value}\n\n## 基本信息\n\n- 类型：${selectedCategory.value}\n- 导师：${supervisor.value || '未填写'}\n- 研究方向：${selectedDirections.value.map(d => d.name).join('、') || '未选择'}\n- 字数：${paperWordCount.value}字\n- 预计阅读时间：${paperReadTime.value}分钟\n\n## 论文内容\n\n${paperContent.value}`

  const blob = new Blob([content], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${paperTitle.value || '论文'}.md`
  a.click()
  URL.revokeObjectURL(url)
  messageApi.success('导出成功')
}

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleSave = () => {
  manualSave()
}

// 切换研究方向
const toggleDirection = (direction) => {
  const index = selectedDirections.value.findIndex(d => d.id === direction.id)
  if (index === -1) {
    selectedDirections.value.push(direction)
  } else {
    selectedDirections.value.splice(index, 1)
  }
}

// 检查是否选中
const isDirectionSelected = (direction) => {
  return selectedDirections.value.some(d => d.id === direction.id)
}

// 路由离开前提示
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges.value) {
    const confirmLeave = confirm('有未保存的内容，确定要离开吗？')
    if (confirmLeave) {
      next()
    } else {
      next(false)
    }
  } else {
    next()
  }
})

onMounted(() => {
  getPaperList()

  window.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault()
      manualSave()
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'e') {
      e.preventDefault()
      exportPaper()
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
      e.preventDefault()
      createNewPaper()
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'h') {
      e.preventDefault()
      showSuggestions.value = !showSuggestions.value
    }
  })
})

onUnmounted(() => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
  if (suggestionDebounceTimer) {
    clearTimeout(suggestionDebounceTimer)
  }
})
</script>

<template>
  <contextHolder />

  <div :class="[isDark ? 'dark' : '', 'min-h-screen overflow-hidden']">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900',
      'min-h-screen transition-colors duration-300 flex flex-col'
    ]">

      <!-- 导航栏 -->
      <Nav
        :isDark="isDark"
        logoIcon="📚"
        logoText="拾光记"
        logoSubtext="弥补当时那个迷茫的自己"
        :menuItems="menuItems"
        :showBackHome="true"
        logoPath="/home"
        @menuClick="handleMenuClick"
        @logoClick="handleLogoClick"
      />
      <div class="max-w-[1600px] px-4 mt-24">
        <div class="flex items-center justify-between h-14 lg:h-16">
          <!-- 左侧：你可以留空，或者放其他内容 -->
          <div></div>

          <!-- 右侧：所有按钮全部靠右 -->
          <div class="flex items-center gap-3">
            <!-- 保存状态 -->
            <div class="flex items-center gap-2 text-sm">
              <div class="w-2 h-2 rounded-full" :class="{
          'bg-green-500': saveStatus === '已保存',
          'bg-yellow-500 animate-pulse': saveStatus === '保存中',
          'bg-red-500': saveStatus === '保存失败',
          'bg-gray-400': saveStatus === '未保存'
        }"></div>
              <span class="text-xs text-gray-400">{{ saveStatus }}</span>
              <span v-if="lastSaveTime && saveStatus === '已保存'" class="text-xs text-gray-500 hidden lg:inline">
          {{ lastSaveTime.toLocaleTimeString() }}
        </span>
            </div>

            <!-- AI 建议按钮 -->
            <button
              @click="showSuggestions = !showSuggestions"
              class="relative px-3 py-1.5 rounded-lg text-sm transition-all flex items-center gap-1"
              :class="showSuggestions
          ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white'
          : (isDark ? 'hover:bg-indigo-950/30' : 'hover:bg-indigo-50')"
            >
              <span>🤖</span>
              <span>论文建议</span>
              <span v-if="suggestions.length > 0" class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
          {{ suggestions.length }}
        </span>
            </button>

            <!-- 论文切换 -->
            <div class="relative">
              <button @click="showPaperList = !showPaperList" class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm transition-all hover:bg-indigo-50 dark:hover:bg-indigo-950/30">
                <span>📄</span>
                <span class="max-w-[150px] truncate">{{ currentPaper?.title || '选择论文' }}</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                </svg>
              </button>

              <!-- 论文下拉列表 -->
              <div v-if="showPaperList" class="absolute top-full right-0 mt-2 w-72 rounded-xl shadow-xl border overflow-hidden z-50" :class="isDark ? 'bg-gray-900 border-gray-700' : 'bg-white border-gray-100'">
                <div class="p-2 border-b" :class="isDark ? 'border-gray-700' : 'border-gray-100'">
                  <input
                    v-model="paperSearchKeyword"
                    type="text"
                    placeholder="🔍 搜索论文..."
                    class="w-full px-3 py-1.5 text-sm rounded-lg border outline-none focus:ring-2 focus:ring-indigo-500"
                    :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-50 border-gray-200'"
                    @click.stop
                  />
                </div>
                <div class="max-h-80 overflow-y-auto">
                  <div v-for="paper in filteredPaperList" :key="paper.id" @click="loadPaper(paper.id)" class="flex items-center justify-between px-4 py-3 cursor-pointer transition-colors hover:bg-indigo-50 dark:hover:bg-indigo-950/30 group">
                    <div class="flex-1 min-w-0">
                      <div class="text-sm font-medium truncate">{{ paper.title }}</div>
                      <div class="text-xs text-gray-400 mt-0.5">{{ paper.updatedAt?.slice(0, 10) }}</div>
                    </div>
                    <button @click="deletePaper(paper.id, $event)" class="opacity-0 group-hover:opacity-100 text-red-400 hover:text-red-500 transition-all">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 1 0 00-1 1v3M4 7h16"></path>
                      </svg>
                    </button>
                  </div>
                </div>
                <div class="border-t p-2" :class="isDark ? 'border-gray-700' : 'border-gray-100'">
                  <button @click="createNewPaper" class="w-full py-2 text-center text-sm text-indigo-500 hover:bg-indigo-50 dark:hover:bg-indigo-950/30 rounded-lg transition-all">
                    + 新建论文
                  </button>
                </div>
              </div>
            </div>

            <button @click="exportPaper" class="px-3 py-1.5 rounded-lg text-sm transition-all hover:bg-indigo-50 dark:hover:bg-indigo-950/30" title="导出 (Ctrl+E)">
              📎 导出
            </button>
            <button @click="router.push('/home')" class="hidden lg:flex items-center gap-1 px-3 py-1.5 rounded-lg text-sm transition-all hover:bg-indigo-50 dark:hover:bg-indigo-950/30">
              🏠 返回
            </button>
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="flex-1 flex pt-16 overflow-hidden">
        <!-- 左侧面板 -->
        <div :class="[sidebarCollapsed ? 'w-16' : 'w-80', 'transition-all duration-300 border-r overflow-y-auto', isDark ? 'border-gray-800 bg-black/50' : 'border-gray-100 bg-white/50']">
          <div class="p-4">
            <div class="flex justify-end mb-4">
              <button @click="toggleSidebar" class="p-2 rounded-lg hover:bg-indigo-50 dark:hover:bg-indigo-950/30 transition-all">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path v-if="!sidebarCollapsed" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7"></path>
                  <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7"></path>
                </svg>
              </button>
            </div>

            <div v-if="!sidebarCollapsed">
              <!-- 论文分类 -->
              <div class="mb-6">
                <label class="block text-sm font-medium mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-700'">📂 论文类型</label>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="cat in paperCategories"
                    :key="cat.value"
                    @click="selectedCategory = cat.value"
                    class="px-3 py-1.5 rounded-full text-xs font-medium transition-all"
                    :class="selectedCategory === cat.value
                      ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white shadow-md'
                      : (isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200')"
                  >
                    {{ cat.label }}
                  </button>
                </div>
              </div>

              <!-- 论文题目 -->
              <div class="mb-6">
                <label class="block text-sm font-medium mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-700'">📌 论文题目</label>
                <input v-model="paperTitle" type="text" placeholder="请输入论文题目" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-indigo-500 transition-all text-sm" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-50 border-gray-200'">
              </div>

              <!-- 导师姓名 -->
              <div class="mb-6">
                <label class="block text-sm font-medium mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-700'">👨‍🏫 导师姓名</label>
                <input v-model="supervisor" type="text" placeholder="请输入导师姓名" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-indigo-500 transition-all text-sm" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-50 border-gray-200'">
              </div>

              <!-- 研究方向 -->
              <div class="mb-6">
                <label class="block text-sm font-medium mb-3" :class="isDark ? 'text-gray-300' : 'text-gray-700'">🏷️ 研究方向（可多选）</label>
                <div class="flex flex-wrap gap-2">
                  <button v-for="dir in directionOptions" :key="dir.id" @click="toggleDirection(dir)" class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all" :class="isDirectionSelected(dir) ? 'bg-gradient-to-r ' + dir.color + ' text-white shadow-md' : (isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200')">
                    <span>{{ dir.icon }}</span>
                    <span>{{ dir.name }}</span>
                  </button>
                </div>
              </div>

              <!-- 统计信息 -->
              <div class="mb-6 p-3 rounded-lg" :class="isDark ? 'bg-gray-800/50' : 'bg-gray-100/50'">
                <div class="text-xs space-y-1" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                  <div class="flex justify-between">
                    <span>📝 字数</span>
                    <span class="font-medium">{{ paperWordCount }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span>⏱️ 阅读时间</span>
                    <span class="font-medium">约 {{ paperReadTime }} 分钟</span>
                  </div>
                  <div class="flex justify-between">
                    <span>🏷️ 研究方向</span>
                    <span class="font-medium">{{ selectedDirections.length }} 个</span>
                  </div>
                </div>
              </div>

              <!-- 保存按钮 -->
              <div class="pt-4 border-t" :class="isDark ? 'border-gray-800' : 'border-gray-100'">
                <button @click="manualSave" class="w-full py-2.5 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-lg font-medium shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-2">
                  💾 立即保存 (Ctrl+S)
                </button>
              </div>

              <!-- 快捷键提示 -->
              <div class="mt-4 text-xs text-center" :class="isDark ? 'text-gray-600' : 'text-gray-400'">
                <div>⌘/Ctrl + S: 保存</div>
                <div>⌘/Ctrl + E: 导出</div>
                <div>⌘/Ctrl + N: 新建</div>
                <div>⌘/Ctrl + H: 建议</div>
              </div>
            </div>

            <!-- 折叠状态 -->
            <div v-else class="flex flex-col items-center gap-4">
              <div class="relative group">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-950/50 flex items-center justify-center text-lg">
                  📌
                </div>
                <div class="absolute left-full ml-2 top-1/2 -translate-y-1/2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
                  {{ paperTitle || '未命名' }}
                </div>
              </div>
              <div class="relative group">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-950/50 flex items-center justify-center text-lg">
                  👨‍🏫
                </div>
                <div class="absolute left-full ml-2 top-1/2 -translate-y-1/2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
                  {{ supervisor || '未填写' }}
                </div>
              </div>
              <div class="relative group">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-lg cursor-pointer hover:scale-105 transition-transform" @click="manualSave">
                  💾
                </div>
                <div class="absolute left-full ml-2 top-1/2 -translate-y-1/2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none">
                  保存
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧编辑器 -->
        <div class="flex-1 overflow-hidden flex flex-col">
          <MarkdownEditor
            v-model:content="paperContent"
            :isDark="isDark"
            :showToolbar="true"
            :showWordCount="true"
            :height="'calc(100vh - 120px)'"
            @save="handleSave"
          />
        </div>
      </div>

      <!-- AI 建议侧边栏 -->
      <div
        v-if="showSuggestions"
        class="fixed right-0 top-16 bottom-0 w-96 z-30 transition-transform duration-300 transform"
        :class="showSuggestions ? 'translate-x-0' : 'translate-x-full'"
      >
        <div :class="[
          isDark ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-200',
          'h-full border-l shadow-2xl overflow-y-auto'
        ]">
          <!-- 头部 -->
          <div class="sticky top-0 p-4 border-b flex justify-between items-center" :class="isDark ? 'border-gray-800 bg-gray-900' : 'border-gray-200 bg-white'">
            <div class="flex items-center gap-2">
              <span class="text-2xl">🤖</span>
              <div>
                <h3 class="font-semibold">AI 论文建议</h3>
                <p class="text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">智能分析，助力完善论文</p>
              </div>
            </div>
            <button @click="showSuggestions = false" class="w-8 h-8 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
              ✕
            </button>
          </div>

          <!-- 内容区域 -->
          <div class="p-4">
            <!-- 加载状态 -->
            <div v-if="suggestionsLoading" class="text-center py-12">
              <div class="inline-block w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
              <p class="mt-2 text-sm text-gray-400">正在分析论文...</p>
            </div>

            <!-- 建议列表 -->
            <div v-else-if="suggestions.length > 0" class="space-y-4">
              <div class="mb-3 p-2 bg-indigo-50 dark:bg-indigo-950/30 rounded-lg text-xs text-center" :class="isDark ? 'text-indigo-300' : 'text-indigo-600'">
                发现 {{ suggestions.length }} 条优化建议
              </div>

              <div
                v-for="(suggestion, index) in suggestions"
                :key="index"
                class="p-4 rounded-xl border transition-all hover:shadow-md"
                :class="isDark ? 'border-gray-800 bg-gray-800/30' : 'border-gray-100 bg-gray-50'"
              >
                <div class="flex items-start gap-3">
                  <div class="text-2xl">{{ suggestion.icon }}</div>
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2 flex-wrap">
                      <span class="text-xs px-2 py-0.5 rounded-full" :class="getPriorityColor(suggestion.priority)">
                        {{ suggestion.priority === 'high' ? '高优先级' : suggestion.priority === 'medium' ? '中优先级' : '低优先级' }}
                      </span>
                      <span class="text-xs px-2 py-0.5 rounded-full bg-gray-200 dark:bg-gray-700">
                        {{ getTypeLabel(suggestion.type) }}
                      </span>
                    </div>
                    <h4 class="font-semibold text-sm mb-2">{{ suggestion.title }}</h4>
                    <p class="text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                      {{ suggestion.content }}
                    </p>
                    <button
                      @click="applySuggestion(suggestion)"
                      class="mt-3 text-sm text-indigo-500 hover:text-indigo-600 transition-colors flex items-center gap-1"
                    >
                      <span>+</span>
                      <span>应用建议</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 无建议 -->
            <div v-else class="text-center py-12">
              <div class="text-5xl mb-3">✅</div>
              <p class="text-sm text-gray-400">论文结构良好！</p>
              <p class="text-xs text-gray-500 mt-1">继续写作获取更多建议</p>
            </div>

            <!-- 历史记录 -->
            <div v-if="suggestionsHistory.length > 0" class="mt-6 pt-6 border-t" :class="isDark ? 'border-gray-800' : 'border-gray-200'">
              <h4 class="text-sm font-medium mb-3 flex items-center gap-2">
                <span>📜</span>
                <span>历史建议</span>
              </h4>
              <div class="space-y-2">
                <div
                  v-for="history in suggestionsHistory.slice(0, 5)"
                  :key="history.id"
                  class="p-2 rounded-lg text-xs cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                  @click="applySuggestion(history)"
                >
                  <div class="flex items-center gap-2">
                    <span>{{ history.icon }}</span>
                    <span class="font-medium">{{ history.title }}</span>
                  </div>
                  <p class="text-gray-400 mt-1 line-clamp-2">{{ history.content }}</p>
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
.animate-pulse {
  animation: pulse 1s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
