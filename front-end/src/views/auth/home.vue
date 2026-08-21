<!--
  文件说明：拾光记前台应用认证与登录页面组件，承载认证与登录场景的界面展示、交互操作和数据承接。
-->
<script setup>
defineOptions({ name: 'HomeView' })

import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggleButton from '@/components/ThemeToggleButton.vue';
import AdvancedTypewriter from '@/components/Typewriter.vue'
import {
  ThemeType,
  getStoredTheme,
  onThemeChange
} from "@/utils/theme.js";
import AdvancedPageTransition from '@/components/AdvancedPageTransition.vue';
import request from "@/utils/request.js";
import messageApi from '@/utils/messageApi'
import { ChatDotRound, Collection, EditPen, RefreshRight, Search, Trophy } from '@element-plus/icons-vue'

// 页面过渡组件引用
const transitionRef = ref(null);
const router = useRouter()
const isScrolled = ref(false)
const showBackTop = ref(false)
const activeNav = ref('home')
const activeStage = ref(0)
const preferredStage = ref(localStorage.getItem('preferred_stage') || 'all')
const showUserMenu = ref(false)
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const texts = ref(['拾光记 · 弥补当时那个迷茫的自己'])
const UserInfo = ref({})

const homeNavItems = computed(() => [
  { id: 'home', name: '首页', icon: '🏠', stages: ['all', 'high_school', 'university', 'workplace'] },
  { id: 'timeline', name: '时光轴', icon: '⏳', stages: ['all', 'high_school', 'university', 'workplace'] },
  { id: 'milestone', name: '图谱总览', icon: '📜', stages: ['all', 'university', 'workplace'] },
  { id: 'exam', name: '在线考试', icon: '📝', link: '/StudyDashboard?tab=practice', stages: ['all', 'high_school', 'university'] },
  { id: 'journal', name: '云边小札', icon: '📖', stages: ['all', 'university', 'workplace'] }
].filter((item) => item.stages.includes(preferredStage.value)))

// 浮动窗口控制
const activePopup = ref(null)

// 各区域的内容配置
const sectionContents = {
  timeline: {
    title: '时光轴 · 成长轨迹',
    description: '记录从高中到职场的每一个重要时刻，让时间有迹可循。',
    menuList: [
      { icon: '🎓', name: '高中时代', description: '2019-2022 · 奋斗的青春', link: '/CourseSelection' },
      { icon: '📚', name: '大学时光', description: '2022-2026 · 成长的蜕变', link: '/PrePare' },
      { icon: '💼', name: '职场生涯', description: '2026-至今 · 职业的启航', link: '/WorkRecords' },
      { icon: '🗓️', name: '成长时间线', description: '目标、任务、面试和复盘一览', link: '/CareerTimeline' },
      { icon: '🧰', name: '入职工具箱', description: '90天计划与常用工作模板', link: '/CareerToolkit' },
      { icon: '🏆', name: '里程碑事件', description: '查看所有重要时刻', link: '/milestones' }
    ],
    stats: [
      { label: '记录时刻', value: '156+' },
      { label: '成长阶段', value: '3个' },
      { label: '里程碑', value: '23个' }
    ]
  },
  milestone: {
    title: '🧠 知识图谱总览',
    description: '构建个人知识网络，可视化掌握情况，让学习可量化、可成长。',
    menuList: [
      { icon: '🔗', name: '知识图谱', description: '知识点关联关系可视化', link: '/GraphView' },
      { icon: '🔥', name: '掌握热力图', description: '薄弱知识点智能分析', link: '/heatmap' },
      { icon: '📈', name: '文章图谱', description: '了解文章的结构', link: '/ContentKnowledgeGraph' },
      { icon: '🎯', name: '学习路径', description: '个性化提升规划', link: '/learning-path' }
    ],
    stats: [
      { label: '知识点总数', value: '128个' },
      { label: '已掌握', value: '76个' },
      { label: '整体掌握率', value: '68.7%' }
    ]
  },
  journal: {
    title: '📖 云边小札',
    description: '记录日常感悟，见证点滴成长。每一篇日记都是心灵的印记。',
    menuList: [
      { icon: '✍️', name: '心情随笔', description: '日常感悟', link: '/Publish' },
      { icon: '📝', name: '取其精华', description: '进步与反思', link: '/List' },
      { icon: '💡', name: '灵感笔记', description: '创意与想法', link: '/Publish' },
      { icon: '✅', name: '每日打卡', description: '记录登录天数和在线时长', link: '/DailyCheckin' },
      { icon: '💬', name: '在线聊天', description: '好友私聊与群聊', link: '/Chat' },
    ],
    stats: [
      { label: '日记总数', value: '156篇' },
      { label: '总字数', value: '3.2万字' },
      { label: '本周更新', value: '5篇' }
    ]
  }
}

// 显示浮动窗口
const showPopup = (sectionId, event) => {
  event.stopPropagation()
  if (activePopup.value === sectionId) {
    activePopup.value = null
    return
  }
  activePopup.value = sectionId
}

// 关闭浮动窗口
const closePopup = () => {
  activePopup.value = null
}

// 点击外部关闭弹窗
const handleClickOutsidePopup = (event) => {
  if (activePopup.value) {
    const popupElement = document.querySelector('.floating-popup')
    const titleElement = document.querySelector(`[data-section="${activePopup.value}"]`)
    if (popupElement && !popupElement.contains(event.target) &&
      titleElement && !titleElement.contains(event.target)) {
      closePopup()
    }
  }
}

// 处理菜单项点击
const handleMenuItemClick = (link) => {
  closePopup()
  if (link) {
    navigateWithTransition(link)
  }
}

// 动画统计数据
const animatedStats = ref({
  users: 0,
  moments: 0,
  days: 0
})
const statTargets = ref({ users: 0, moments: 0, days: 0 })

//获取用户信息
const getUserInfo =()=>{
  request.get('/user/getUserById',{},(message,data)=>{
    UserInfo.value=data
    const createdAt = data?.createTime || data?.createdAt
    statTargets.value.days = createdAt
      ? Math.max(1, Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000) + 1)
      : 0
    loadTimeline(data?.id)
  })
}

const loadPlatformStats = async () => {
  try {
    const response = await request.get('/user/public/stats')
    statTargets.value.users = Number(response.data?.totalUsers || 0)
  } catch {
    statTargets.value.users = 0
  }
}

// 时光轴节点预览
const motivationalTimelineNodes = [
  { year: '现在', event: '每一次记录，都是在为未来积累答案。' },
  { year: '下一步', event: '从完成一个小目标开始，让成长有迹可循。' },
  { year: '坚持', event: '今天的努力，会成为明天回头时的光。' },
  { year: '未来', event: '你会感谢现在没有放弃的自己。' }
]

// 人生阶段
const lifeStages = computed(() => [
  { icon: '📚', name: '高中时代', years: '15-18岁', path: '/CourseSelection', stage: 'high_school' },
  { icon: '🎓', name: '大学时光', years: '18-22岁', path: '/PrePare', stage: 'university' },
  { icon: '💼', name: '职场工作台', years: '22-25岁', path: '/WorkRecords', stage: 'workplace' },
  { icon: '🚀', name: '进阶之路', years: '25岁+', path: '/AdvanceRecords', stage: 'workplace' }
].filter((item) => preferredStage.value === 'all' || item.stage === preferredStage.value))

// 带过渡效果的导航
const navigateWithTransition = (path) => {
  const target = typeof path === 'string' && !path.startsWith('/') ? `/${path}` : path
  closeUserMenu()

  if (transitionRef.value) {
    transitionRef.value.show?.()
    setTimeout(() => {
      router.push(target)
    }, 650)
  } else {
    router.push(target)
  }
}

const openGlobalSearch = () => {
  window.dispatchEvent(new CustomEvent('app:open-search'))
}

// 处理阶段点击
const handleStageClick = (stage, idx) => {
  activeStage.value = idx
  if (stage.path) {
    navigateWithTransition(stage.path)
  }
}

// 时光轴数据
const timelineData = ref([])
const timelinePreviewRef = ref(null)
const timelinePreviewPaused = ref(false)
let timelinePreviewFrame = null
let timelinePreviewLastFrame = 0
let stopThemeListen = null

const fallbackTimelineItems = motivationalTimelineNodes.map((node) => ({
  source: 'fallback',
  date: node.year,
  stage: '成长',
  stageClass: 'growth',
  title: node.event,
  description: '记录一个小目标，成长轨迹就会继续向前。',
  tags: ['成长'],
  sortDate: ''
}))
const timelineItems = computed(() => {
  const source = timelineData.value.length ? timelineData.value : fallbackTimelineItems
  return source.slice(0, 10)
})

const timelineDate = value => value ? String(value).slice(0, 10) : ''
const localToday = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
const listFromResult = (result) => {
  if (result.status !== 'fulfilled') return []
  if (Array.isArray(result.value)) return result.value
  const data = result.value?.data
  if (Array.isArray(data)) return data
  return data?.records || data?.list || []
}
const allDatedItems = (items, dateOf) => items
  .filter((item) => dateOf(item))
  .sort((a, b) => String(dateOf(b)).localeCompare(String(dateOf(a))))

const fetchAllTimelinePages = async (url, method = 'get') => {
  const pageSize = 100
  const records = []
  let pageNum = 1
  let pageCount

  do {
    const params = { pageNum, pageSize }
    const response = method === 'post'
      ? await request.post(url, params)
      : await request.get(url, params)
    const data = response?.data
    const batch = Array.isArray(data) ? data : (data?.records || data?.list || [])
    records.push(...batch)

    if (Array.isArray(data)) break
    const declaredPages = Number(data?.pages)
    pageCount = Number.isFinite(declaredPages) && declaredPages > 0
      ? declaredPages
      : Math.max(1, Math.ceil(Number(data?.total || batch.length) / pageSize))
    pageNum += 1
  } while (pageNum <= pageCount)

  return records
}

const addTimelineItem = (items, item) => {
  const date = timelineDate(item.sortDate)
  if (!date || !item.title) return
  items.push({ ...item, date })
}

const loadTimeline = async (userId) => {
  if (!userId) return
  const [
    scoreResult,
    growthResult,
    articleResult,
    interviewResult,
    goalResult,
    taskResult,
    reviewResult,
    examResult,
    milestoneResult,
    activityResult,
  ] = await Promise.allSettled([
    request.get(`/score/list/${userId}`),
    fetchAllTimelinePages('/growth/list', 'post'),
    fetchAllTimelinePages('/content/my/list'),
    request.get('/workplace/interviews'),
    request.get('/workplace/goals'),
    request.get('/workplace/tasks'),
    request.get('/workplace/reviews'),
    request.get(`/question/exam/history/${userId}`),
    request.get('/advanced/milestones'),
    request.get('/activity/summary'),
  ])
  const items = []
  if (UserInfo.value?.createTime) {
    addTimelineItem(items, { source: 'account', id: UserInfo.value.id, stage: '成长', stageClass: 'growth', title: '加入拾光记', description: '开始建立自己的学习与成长记录。', tags: ['成长'], sortDate: UserInfo.value.createTime })
  }

  allDatedItems(listFromResult(scoreResult), (item) => item.examDate)
    .forEach((item) => addTimelineItem(items, { source: 'score', id: item.id, stage: '成绩', stageClass: 'highschool', title: item.examName || '模考记录', description: `${item.subjectName || '学科'} ${item.score ?? '-'} 分，已纳入模考诊断。`, tags: ['模考', '成绩'], sortDate: item.examDate }))

  allDatedItems(listFromResult(growthResult), (item) => item.recordDate)
    .forEach((item) => addTimelineItem(items, { source: 'growth', id: item.id, stage: item.stage || '成长', stageClass: 'growth', title: item.examName || item.activityName || item.companyName || item.milestoneName || '成长记录', description: item.achievementThisPeriod || item.jobContent || (item.studyHours ? `日均学习 ${item.studyHours} 小时。` : '记录了一段真实的成长经历。'), tags: ['成长记录'], sortDate: item.recordDate }))

  allDatedItems(listFromResult(articleResult), (item) => item.publishTime || item.createTime || item.createdAt)
    .forEach((item) => {
      const isQuote = item.contentType === 'quote'
      addTimelineItem(items, { source: isQuote ? 'quote' : 'article', id: item.id, stage: isQuote ? '寄语' : '文章', stageClass: 'article', title: isQuote ? '发表每日寄语' : (item.title || '发布了一篇文章'), description: item.summary || item.content || '把思考写下来，让成长留下可以回看的证据。', tags: isQuote ? ['寄语'] : ['文章', '表达'], sortDate: item.publishTime || item.createTime || item.createdAt })
    })

  allDatedItems(listFromResult(interviewResult), (item) => item.interviewDate || item.createdAt)
    .forEach((item) => addTimelineItem(items, { source: 'interview', id: item.id, stage: '面试', stageClass: 'work', title: `${item.companyName || '目标公司'} · ${item.positionName || '面试准备'}`, description: `${item.interviewRound || '面试'} · ${item.status || '准备中'}`, tags: ['面试', '职场'], sortDate: item.interviewDate || item.createdAt }))

  allDatedItems(listFromResult(goalResult), (item) => item.createdAt || item.startDate || item.targetDate)
    .forEach((item) => addTimelineItem(items, { source: 'goal', id: item.id, stage: '目标', stageClass: 'work', title: item.goalName || '职业目标', description: item.metric || item.notes || '职业目标正在推进中。', tags: ['目标', '职业'], sortDate: item.createdAt || item.startDate || item.targetDate }))

  allDatedItems(listFromResult(taskResult), (item) => item.completedAt || item.createdAt || item.startDate || item.dueDate)
    .forEach((item) => addTimelineItem(items, { source: 'task', id: item.id, stage: '任务', stageClass: 'study', title: `${item.status === 'DONE' ? '完成' : '创建'}任务 · ${item.taskName || '未命名任务'}`, description: item.outcome || item.notes || '任务已加入个人成长计划。', tags: ['任务'], sortDate: item.completedAt || item.createdAt || item.startDate || item.dueDate }))

  allDatedItems(listFromResult(reviewResult), (item) => item.reviewDate || item.createdAt)
    .forEach((item) => addTimelineItem(items, { source: 'review', id: item.id, stage: '复盘', stageClass: 'work', title: item.reviewType ? `${item.reviewType}复盘` : '完成工作复盘', description: item.learnings || item.wins || '沉淀了一次工作复盘。', tags: ['复盘', '职场'], sortDate: item.reviewDate || item.createdAt }))

  allDatedItems(listFromResult(examResult), (item) => item.finishedAt || item.startedAt)
    .forEach((item) => addTimelineItem(items, { source: 'exam', id: item.sessionId, stage: '考试', stageClass: 'study', title: item.title || `${item.subjectName || '在线'}考试`, description: `得分 ${item.scoreObtained ?? '-'} / ${item.scoreTotal ?? '-'}，正确率 ${item.scorePercent ?? '-'}%。`, tags: ['考试', '学习'], sortDate: item.finishedAt || item.startedAt }))

  allDatedItems(listFromResult(milestoneResult), (item) => item.completedDate || item.createdAt || item.dueDate)
    .forEach((item) => addTimelineItem(items, { source: 'milestone', id: item.id, stage: '里程碑', stageClass: 'growth', title: item.milestoneName || '达成一个里程碑', description: item.notes || `状态：${item.status || '进行中'}`, tags: ['里程碑'], sortDate: item.completedDate || item.createdAt || item.dueDate }))

  if (activityResult.status === 'fulfilled') {
    const activity = activityResult.value?.data || {}
    if (activity.lastCheckinDate) {
      addTimelineItem(items, { source: 'checkin', id: activity.lastCheckinDate, stage: '打卡', stageClass: 'growth', title: '完成每日打卡', description: `已累计打卡 ${activity.totalLoginDays || 1} 天。`, tags: ['打卡'], sortDate: activity.lastCheckinDate })
    }
    allDatedItems(activity.medals || [], (item) => item.awardedAt)
      .forEach((item) => addTimelineItem(items, { source: 'medal', id: item.id || item.ruleId, stage: '成就', stageClass: 'growth', title: `获得成就 · ${item.medalName || item.name || '成长勋章'}`, description: item.description || '新的成长成就已解锁。', tags: ['成就'], sortDate: item.awardedAt }))
  }

  timelineData.value = items.sort((a, b) => String(b.sortDate || '').localeCompare(String(a.sortDate || '')))
  statTargets.value.moments = timelineData.value.length
  animateNumbers()
  await nextTick()
  if (timelinePreviewRef.value) timelinePreviewRef.value.scrollTop = 0
  initScrollAnimation()
}
const hasTimelineActivity = computed(() => timelineData.value.some((item) => item.source !== 'account'))
const timelineNodes = computed(() => {
  if (!timelineData.value.length) {
    return [{ key: 'empty', date: localToday(), event: '还没有成长节点，今天就从第一条记录开始。', type: '成长', tone: 'growth' }]
  }
  return timelineData.value.map((item, index) => ({
    key: `${item.source}-${item.id || index}-${item.date}`,
    date: item.date,
    event: item.title,
    type: item.stage,
    tone: item.stageClass || 'growth',
  }))
})
const timelinePreviewCanLoop = computed(() => timelineNodes.value.length > 4)
const timelinePreviewItems = computed(() => {
  const nodes = timelineNodes.value.map((node) => ({ ...node, loopKey: `${node.key}-0`, duplicate: false }))
  if (!timelinePreviewCanLoop.value) return nodes
  return [
    ...nodes,
    ...timelineNodes.value.map((node) => ({ ...node, loopKey: `${node.key}-1`, duplicate: true })),
  ]
})

const runTimelinePreview = (timestamp) => {
  const viewport = timelinePreviewRef.value
  if (viewport && timelinePreviewCanLoop.value && !timelinePreviewPaused.value) {
    const elapsed = timelinePreviewLastFrame ? Math.min(timestamp - timelinePreviewLastFrame, 64) : 0
    viewport.scrollTop += elapsed * 0.04
    const loopHeight = viewport.scrollHeight / 2
    if (loopHeight > 0 && viewport.scrollTop >= loopHeight) viewport.scrollTop -= loopHeight
  }
  timelinePreviewLastFrame = timestamp
  timelinePreviewFrame = window.requestAnimationFrame(runTimelinePreview)
}

const pauseTimelinePreview = () => { timelinePreviewPaused.value = true }
const resumeTimelinePreview = () => { timelinePreviewPaused.value = false }
// 图谱总览数据
const milestones = [
  { icon: '✒️', year: '2019', title: '初入文海', desc: '开始记录读书笔记', progress: 100, status: '已完成' },
  { icon: '📜', year: '2022', title: '笔墨渐丰', desc: '积累笔记百余篇', progress: 100, status: '已完成' },
  { icon: '🔖', year: '2024', title: '文思积淀', desc: '形成个人知识体系', progress: 80, status: '进行中' },
  { icon: '🖋️', year: '2025', title: '落笔生花', desc: '输出完整知识框架', progress: 50, status: '进行中' },
  { icon: '📚', year: '2026', title: '墨海成章', desc: '个人知识库成型', progress: 25, status: '规划中' }
]

// 相册数据
const examCards = [
  { icon: '⚡', title: '随机组卷', desc: '按分类、知识点、题型和难度生成试卷', link: '/StudyDashboard?tab=practice' },
  { icon: '⏱️', title: '限时考试', desc: '支持考试计时与防作弊提醒', link: '/StudyDashboard?tab=practice' },
  { icon: '🧩', title: '错题练习', desc: '从错题本中抽题，针对薄弱点复习', link: '/StudyDashboard?tab=mistake' },
  { icon: '📈', title: '成绩分析', desc: '查看考试历史、得分和知识掌握趋势', link: '/StudyDashboard?tab=analysis' },
  { icon: '📚', title: '题库管理', desc: '新增题目并等待审核进入考试题库', link: '/StudyDashboard?tab=questionBank' },
  { icon: '✅', title: '答题记录', desc: '追踪每次作答、解析和正确率', link: '/StudyDashboard?tab=answerRecords' }
]
// 日记数据
const journalEntries = [
  { day: '15', month: '3月', title: '今天学会了新技能', excerpt: '花了整整一天时间，终于把数据分析的基础搞懂了...', likes: 23, comments: 5 },
  { day: '20', month: '3月', title: '和老朋友的重逢', excerpt: '高中同学来出差，一起吃了顿饭，聊了很多过去的事...', likes: 45, comments: 12 },
  { day: '25', month: '3月', title: '第一次主持会议', excerpt: '紧张到手心出汗，但最终还是顺利完成了。原来我也可以...', likes: 67, comments: 23 }
]

// 每日寄语
const fallbackQuotes = [
  { text: '种一棵树最好的时间是十年前，其次是现在。', author: '佚名' },
  { text: '那些你熬夜努力的时光，终会化作照亮前路的光。', author: '拾光记' },
  { text: '成长不是变得复杂，而是学会在复杂中保持简单。', author: '佚名' },
  { text: '每一个优秀的人都有一段沉默的时光，那段时间是付出了很多努力，却得不到结果的日子，我们把它叫做扎根。', author: '佚名' }
]

const publishedQuotes = ref([])
const dailyQuote = ref(fallbackQuotes[0])
const quoteTypewriterKey = ref(0)
let quoteRotationTimer = null
const quoteDraft = ref('')
const quotePublishing = ref(false)

const setDailyQuote = (quote) => {
  dailyQuote.value = quote
  quoteTypewriterKey.value += 1
}

const normalizeQuote = (item) => ({
  id: item.id,
  text: item.content,
  author: item.author?.name || item.author?.username || item.authorName || '拾光用户',
  createdAt: item.publishTime || item.createTime,
})

const loadQuotes = async (selectLatest = false) => {
  try {
    const res = await request.get('/content/public/list', {
      pageNum: 1,
      pageSize: 50,
      contentType: 'quote',
    })
    publishedQuotes.value = (res.data?.records || [])
      .filter((item) => item.content?.trim())
      .map(normalizeQuote)
    if (publishedQuotes.value.length) {
      setDailyQuote(selectLatest
        ? publishedQuotes.value[0]
        : publishedQuotes.value[Math.floor(Math.random() * publishedQuotes.value.length)])
    }
  } catch (error) {
    console.error('获取每日寄语失败', error)
  }
}

const publishQuote = async () => {
  const content = quoteDraft.value.trim()
  if (!content) {
    messageApi.warning('请先写下想分享的寄语')
    return
  }
  if (content.length > 200) {
    messageApi.warning('寄语不能超过 200 个字')
    return
  }

  quotePublishing.value = true
  try {
    const res = await request.post('/content/save', {
      title: '每日寄语',
      summary: content,
      content,
      contentType: 'quote',
      isPublic: 2,
      status: 1,
    })
    if (res.code !== 200) throw new Error(res.message || '发表失败')
    quoteDraft.value = ''
    await loadQuotes(true)
    messageApi.success('寄语已发表')
  } catch (error) {
    messageApi.error(error.message || '寄语发表失败')
  } finally {
    quotePublishing.value = false
  }
}

const refreshQuote = () => {
  const source = publishedQuotes.value.length ? publishedQuotes.value : fallbackQuotes
  if (source.length <= 1) return
  let nextQuote = dailyQuote.value
  let attempts = 0
  const sameQuote = (left, right) =>
    left?.id && right?.id
      ? String(left.id) === String(right.id)
      : left?.text === right?.text && left?.author === right?.author
  while (sameQuote(nextQuote, dailyQuote.value) && attempts < 10) {
    nextQuote = source[Math.floor(Math.random() * source.length)]
    attempts += 1
  }
  setDailyQuote(nextQuote)
}

// 数字滚动动画
const animateNumbers = () => {
  const targets = [statTargets.value.users, statTargets.value.moments, statTargets.value.days]
  const intervals = targets.map((target, i) => {
    let current = 0
    const increment = target / 50
    return setInterval(() => {
      current += increment
      if (current >= target) {
        current = target
        clearInterval(intervals[i])
      }
      if (i === 0) animatedStats.value.users = Math.floor(current)
      if (i === 1) animatedStats.value.moments = Math.floor(current)
      if (i === 2) animatedStats.value.days = Math.floor(current)
    }, 30)
  })
}

// 滚动监听 - 更新导航高亮
const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
  showBackTop.value = window.scrollY > 500

  const sections = ['home', 'timeline', 'milestone', 'exam', 'journal']
  for (const section of sections) {
    const element = document.getElementById(section)
    if (element) {
      const rect = element.getBoundingClientRect()
      if (rect.top <= 150 && rect.bottom >= 150) {
        activeNav.value = section
        break
      }
    }
  }
}

// 滚动到指定区域
const handleHomeNavClick = (item) => {
  if (item.link) {
    navigateWithTransition(item.link)
    return
  }
  scrollToSection(item.id)
}

const scrollToSection = (sectionId) => {
  const element = document.getElementById(sectionId)
  if (element) {
    const offset = 80
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset
    window.scrollTo({ top: offsetPosition, behavior: 'smooth' })
  }
  activeNav.value = sectionId
  closePopup()
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 查看更多
const viewMore = () => {
  navigateWithTransition('/StudyDashboard?tab=practice')
}

// 切换用户菜单
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

// 关闭用户菜单
const closeUserMenu = () => {
  showUserMenu.value = false
}

// 点击页面其他地方关闭菜单
const handleClickOutside = (event) => {
  const userMenu = document.querySelector('.user-menu-container')
  if (userMenu && !userMenu.contains(event.target)) {
    closeUserMenu()
  }
}

// 初始化滚动动画观察器
let scrollAnimationObserver = null

const initScrollAnimation = () => {
  if (typeof window === 'undefined') return

  if (!scrollAnimationObserver && 'IntersectionObserver' in window) {
    scrollAnimationObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('animated')
          scrollAnimationObserver.unobserve(entry.target)
        }
      })
    }, {
      threshold: 0.2,
      rootMargin: '0px 0px -50px 0px'
    })
  }

  const animatedElements = document.querySelectorAll('.scroll-animate:not(.animated)')
  if (!scrollAnimationObserver) {
    animatedElements.forEach((element) => element.classList.add('animated'))
    return
  }
  animatedElements.forEach(element => scrollAnimationObserver.observe(element))
}

// 处理主题切换
const handleThemeChange = (theme) => {
  const newTheme = theme === ThemeType.DARK
  isDark.value = newTheme

  if (newTheme) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }

  nextTick(() => {
    const animatedElements = document.querySelectorAll('.scroll-animate')
    animatedElements.forEach(el => {
      el.classList.remove('animated')
    })
    initScrollAnimation()
  })
}

const handleTransitionComplete = () => {
}

const handleStageChange = (event) => { preferredStage.value = event.detail || localStorage.getItem('preferred_stage') || 'all' }
const stageVisible = (stages) => preferredStage.value === 'all' || stages.includes(preferredStage.value)

onMounted(() => {
  getUserInfo()
  loadPlatformStats().then(animateNumbers)
  loadQuotes()
  quoteRotationTimer = window.setInterval(refreshQuote, 5000)
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  }

  window.addEventListener('scroll', handleScroll)
  window.addEventListener('click', handleClickOutside)
  window.addEventListener('click', handleClickOutsidePopup)
  window.addEventListener('app:stage-change', handleStageChange)
  timelinePreviewFrame = window.requestAnimationFrame(runTimelinePreview)
  animateNumbers()
  setTimeout(initScrollAnimation, 100)
  if (window.location.hash === '#daily-quote') {
    nextTick(() => document.getElementById('daily-quote')?.scrollIntoView({ behavior: 'smooth' }))
  }


  stopThemeListen = onThemeChange((theme) => {
    handleThemeChange(theme)
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('click', handleClickOutside)
  window.removeEventListener('click', handleClickOutsidePopup)
  window.removeEventListener('app:stage-change', handleStageChange)
  if (quoteRotationTimer) {
    window.clearInterval(quoteRotationTimer)
    quoteRotationTimer = null
  }
  if (timelinePreviewFrame) {
    window.cancelAnimationFrame(timelinePreviewFrame)
    timelinePreviewFrame = null
  }
  stopThemeListen?.()
  stopThemeListen = null
  scrollAnimationObserver?.disconnect()
  scrollAnimationObserver = null
})
</script>

<template>
  <AdvancedPageTransition ref="transitionRef" :duration="10000" @complete="handleTransitionComplete" />

  <!-- 浮动窗口 - 已修复 -->
  <div
    v-if="activePopup"
    class="floating-popup fixed z-[9999] animate-fadeIn"
    @click.stop
    :style="{
      left: '50%',
      top: '100px',
      transform: 'translateX(-50%)'
    }"
  >
    <div class="relative">
      <div class="absolute -top-2 left-1/2 -translate-x-1/2 w-4 h-4 rotate-45" :class="isDark ? 'bg-gray-800' : 'bg-white'"></div>
      <div :class="[
        isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200',
        'rounded-2xl shadow-2xl border overflow-hidden min-w-[320px] max-w-[400px]'
      ]">
        <div :class="[
          isDark ? 'bg-gray-900/50 border-gray-700' : 'bg-gradient-to-r from-brand-50 to-accent-50 border-gray-100',
          'p-4 border-b'
        ]">
          <h3 class="font-bold text-lg" :class="isDark ? 'text-white' : 'text-gray-800'">
            {{ sectionContents[activePopup]?.title }}
          </h3>
          <p class="text-sm mt-1" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
            {{ sectionContents[activePopup]?.description }}
          </p>
        </div>

        <div class="p-4 border-b" :class="isDark ? 'border-gray-700' : 'border-gray-100'">
          <div class="flex justify-around">
            <div v-for="(stat, idx) in sectionContents[activePopup]?.stats" :key="idx" class="text-center">
              <div class="text-2xl font-bold text-brand-500">{{ stat.value }}</div>
              <div class="text-xs" :class="isDark ? 'text-gray-400' : 'text-gray-500'">{{ stat.label }}</div>
            </div>
          </div>
        </div>

        <div class="p-2 max-h-[400px] overflow-y-auto">
          <div
            v-for="(item, idx) in sectionContents[activePopup]?.menuList"
            :key="idx"
            @click="handleMenuItemClick(item.link)"
            class="flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-all hover:bg-brand-50 dark:hover:bg-brand-950/30 group"
          >
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-100 to-accent-100 dark:from-brand-950/50 dark:to-accent-950/50 flex items-center justify-center text-xl">
              {{ item.icon }}
            </div>
            <div class="flex-1">
              <div class="font-medium" :class="isDark ? 'text-white group-hover:text-brand-400' : 'text-gray-800 group-hover:text-brand-600'">
                {{ item.name }}
              </div>
              <div class="text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                {{ item.description }}
              </div>
            </div>
            <svg class="w-4 h-4 opacity-0 group-hover:opacity-100 transition-opacity" :class="isDark ? 'text-brand-400' : 'text-brand-600'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </div>
        </div>

        <div class="p-3 border-t" :class="isDark ? 'border-gray-700' : 'border-gray-100'">
          <button
            @click="closePopup"
            class="w-full py-2 text-center text-sm rounded-lg transition-all"
            :class="isDark ? 'text-gray-400 hover:bg-gray-700' : 'text-gray-500 hover:bg-gray-100'"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>

  <div :class="[isDark ? 'dark' : '', 'min-h-screen overflow-x-hidden']">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-brand-50/30 text-gray-900',
      'min-h-screen transition-colors duration-300'
    ]">

      <!-- 导航栏 -->
      <nav class="fixed top-0 left-0 right-0 z-50 transition-all duration-500" :class="[
        isScrolled
          ? isDark
            ? 'bg-black/95 backdrop-blur-xl border-b border-gray-800'
            : 'bg-white/95 backdrop-blur-xl shadow-lg border-b border-gray-100'
          : 'bg-transparent'
      ]">
        <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
          <div class="flex items-center justify-between h-16 lg:h-20">
            <div class="flex items-center gap-3 cursor-pointer group" @click="scrollToSection('home')">
              <div class="relative">
                <div class="absolute inset-0 bg-gradient-to-r from-brand-500 to-accent-500 rounded-xl blur-lg opacity-0 group-hover:opacity-50 transition-opacity duration-500"></div>
                <div class="relative w-9 h-9 lg:w-10 lg:h-10 bg-gradient-to-br from-brand-500 to-accent-500 rounded-xl flex items-center justify-center shadow-lg">
                  <span class="text-xl lg:text-2xl">⏰</span>
                </div>
              </div>
              <div class="flex items-baseline gap-1">
                <span class="text-xl lg:text-2xl font-bold" :class="isDark ? 'text-white' : 'bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent'">
                  拾光记
                </span>
                <span class="hidden lg:inline text-xs font-medium text-gray-400 tracking-wider">弥补当时那个迷茫的自己</span>
              </div>
            </div>

            <div class="flex-1 flex justify-center">
              <div :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'hidden md:flex items-center gap-1 rounded-full p-1 shadow-sm']">
                <button
                  v-for="item in homeNavItems"
                  :key="item.id"
                  @click="handleHomeNavClick(item)"
                  class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group"
                  :class="[
                    activeNav === item.id
                      ? 'text-white shadow-lg'
                      : isDark ? 'text-gray-400 hover:text-brand-400' : 'text-gray-600 hover:text-brand-600'
                  ]"
                >
                  <span v-if="activeNav === item.id" class="absolute inset-0 bg-gradient-to-r from-brand-500 to-accent-500 rounded-full shadow-md shadow-brand-500/20"></span>
                  <span class="relative flex items-center gap-2 z-10">
                    <span class="text-base">{{ item.icon }}</span>
                    <span>{{ item.name }}</span>
                  </span>
                </button>
              </div>
            </div>

            <div class="flex items-center gap-2.5 user-menu-container relative">
              <div class="hidden md:flex items-center gap-2">
                <button
                  type="button"
                  class="home-quick-link"
                  title="全局搜索（Ctrl+K）"
                  aria-label="打开全局搜索"
                  @click="openGlobalSearch"
                >
                  <Search />
                  <span>搜索</span>
                </button>
                <button
                  type="button"
                  class="home-quick-link"
                  title="成长等级与每日任务"
                  @click="navigateWithTransition('/DailyCheckin')"
                >
                  <Trophy />
                  <span>成长</span>
                </button>
                <button
                  type="button"
                  class="home-quick-link"
                  title="在线聊天"
                  @click="navigateWithTransition('/Chat')"
                >
                  <ChatDotRound />
                  <span>聊天</span>
                </button>
              </div>
              <div class="hidden md:flex items-center gap-2 cursor-pointer group" @click="toggleUserMenu">
                <div class="relative w-9 h-9 rounded-full overflow-hidden border-2 border-brand-200 group-hover:border-brand-400 transition-colors">
                  <img :src="UserInfo.avatar" alt="User Avatar">
                </div>
                <span :class="[isDark ? 'text-gray-300 group-hover:text-brand-400' : 'text-gray-700 group-hover:text-brand-600', 'text-sm font-medium transition-colors']">{{UserInfo.name}}</span>
              </div>

              <div v-if="showUserMenu" :class="[isDark ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100', 'absolute top-full right-0 mt-2 w-48 rounded-lg shadow-xl border overflow-hidden z-50']">
                <div class="py-2">
                  <button @click="navigateWithTransition('/PersonalProfile')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>👤</span>
                    <span>个人档案</span>
                  </button>
                  <button @click="navigateWithTransition('/Resume')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>👤</span>
                    <span>个人简历</span>
                  </button>
                  <button @click="navigateWithTransition('/Settings')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>⚙️</span>
                    <span>设置</span>
                  </button>
                  <div :class="[isDark ? 'border-gray-800' : 'border-gray-100', 'border-t my-1']"></div>
                  <button @click="navigateWithTransition('/')" :class="[isDark ? 'text-red-400 hover:bg-gray-800' : 'text-red-600 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                    <span>🚪</span>
                    <span>退出登录</span>
                  </button>
                </div>
              </div>

              <ThemeToggleButton />
            </div>
          </div>
        </div>
      </nav>

      <!-- Hero 区域 -->
      <section id="home" class="relative min-h-screen flex items-center pt-20 overflow-hidden">
        <div class="absolute inset-0 z-0">
          <div class="absolute w-[400px] h-[400px] bg-brand-400/20 rounded-full blur-[80px] -top-20 -left-20 animate-float"></div>
          <div class="absolute w-[500px] h-[500px] bg-accent-400/15 rounded-full blur-[80px] -bottom-32 -right-32 animate-float"></div>
          <div class="absolute w-[300px] h-[300px] bg-brand-300/10 rounded-full blur-[80px] top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 animate-float animation-delay-5000"></div>
        </div>

        <div class="relative z-10 max-w-[1200px] mx-auto px-6 lg:px-8">
          <div class="grid lg:grid-cols-2 gap-12 items-center">
            <div class="scroll-animate">
              <div class="inline-flex items-center gap-2 px-4 py-2 bg-brand-50 rounded-full text-brand-600 text-sm mb-6">
                <span>✨</span>
                <span>从高中到职场，记录每一步成长</span>
              </div>
              <h1 class="text-4xl lg:text-6xl font-bold leading-tight mb-5">
                <AdvancedTypewriter :texts="texts"></AdvancedTypewriter>
              </h1>
              <p class="text-base lg:text-lg leading-relaxed mb-8" :class="isDark? 'text-gray-400' : 'text-gray-600'">
                记录高中奋斗的日夜，珍藏大学青春的瞬间，见证职场蜕变的每一步<br>
                让时间有迹可循，让成长值得回味
              </p>
              <div class="flex gap-8 mb-8">
                <div>
                  <div class="text-3xl font-bold">{{ animatedStats.users }}</div>
                  <div class="text-sm ">成长记录者</div>
                </div>
                <div>
                  <div class="text-3xl font-bold">{{ animatedStats.moments }}</div>
                  <div class="text-sm">珍藏瞬间</div>
                </div>
                <div>
                  <div class="text-3xl font-bold">{{ animatedStats.days }}</div>
                  <div class="text-sm">陪伴天数</div>
                </div>
              </div>
              <div class="flex flex-wrap gap-4">
                <button @click="navigateWithTransition('/Today')" class="px-8 py-3 bg-gradient-to-r from-brand-500 to-accent-500 text-white rounded-full font-medium shadow-lg hover:shadow-xl transition-all hover:-translate-y-0.5">
                  🧭 打开今日工作台
                </button>
                <button @click="navigateWithTransition('/Records')" class="px-8 py-3 border border-gray-300 rounded-full font-medium hover:border-brand-400 hover:text-brand-600 transition-all">
                  开始记录时光
                </button>
                <button @click="scrollToSection('timeline')" class="px-8 py-3 border border-gray-300 rounded-full font-medium hover:border-brand-400 hover:text-brand-600 transition-all">
                  浏览时光故事
                </button>
              </div>
            </div>

            <div class="timeline-preview-shell bg-white/10 backdrop-blur-xl rounded-2xl border border-white/80 shadow-xl scroll-animate">
              <div class="timeline-preview-heading">
                <span>我的成长节点</span>
                <strong>{{ timelineNodes.length }}</strong>
              </div>
              <div
                ref="timelinePreviewRef"
                class="timeline-preview-viewport"
                tabindex="0"
                role="region"
                aria-label="我的全部成长节点"
                @mouseenter="pauseTimelinePreview"
                @mouseleave="resumeTimelinePreview"
                @focusin="pauseTimelinePreview"
                @focusout="resumeTimelinePreview"
              >
                <div class="timeline-preview-track">
                  <article
                    v-for="node in timelinePreviewItems"
                    :key="node.loopKey"
                    class="timeline-preview-node"
                    :class="`timeline-preview-node--${node.tone}`"
                    :aria-hidden="node.duplicate ? 'true' : undefined"
                  >
                    <span class="timeline-preview-dot" aria-hidden="true"></span>
                    <div class="timeline-preview-copy">
                      <div class="timeline-preview-meta">
                        <time :datetime="node.date">{{ node.date }}</time>
                        <span>{{ node.type }}</span>
                      </div>
                      <p :title="node.event">{{ node.event }}</p>
                    </div>
                  </article>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 人生阶段导航 -->
      <section class="py-16" :class="isDark ? 'bg-black' : 'bg-white'">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div class="flex justify-center gap-5 flex-wrap">
            <div
              v-for="(stage, idx) in lifeStages"
              :key="idx"
              class="px-8 py-5 rounded-2xl text-center cursor-pointer transition-all min-w-[140px]"
              :class="[
                isDark
                  ? activeStage === idx
                    ? 'bg-black border-brand-400 shadow-lg -translate-y-1'
                    : 'bg-gray-900 border-gray-700 hover:border-brand-500'
                  : activeStage === idx
                    ? 'bg-white/10 border-white/50 shadow-lg -translate-y-1'
                    : 'bg-gray-50 border-gray-200 hover:border-brand-400',
                'border hover:shadow-lg hover:-translate-y-1'
              ]"
              @click="handleStageClick(stage, idx)">
              <div class="text-3xl mb-2">{{ stage.icon }}</div>
              <div class="font-semibold mb-1" :class="isDark ? 'text-white' : 'text-gray-800'">{{ stage.name }}</div>
              <div class="text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-500'">{{ stage.years }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 时光轴区域 - 可点击标题 -->
      <section v-if="stageVisible(['all', 'high_school', 'university', 'workplace'])" id="timeline" class="py-20" :class="isDark ? 'bg-black' : 'bg-white'">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div
            class="text-center mb-12 scroll-animate cursor-pointer group"
            @click="showPopup('timeline', $event)"
            data-section="timeline"
          >
            <h2 class="text-3xl lg:text-4xl font-bold mb-3 inline-flex items-center gap-2 group-hover:text-brand-500 transition-colors" :class="isDark ? 'text-white group-hover:text-brand-400' : 'text-gray-900 group-hover:text-brand-600'">
              时光轴 · 成长轨迹
              <svg class="w-5 h-5 opacity-0 group-hover:opacity-100 transition-opacity" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0121 0z"></path>
              </svg>
            </h2>
            <p class="text-gray-500">记录每一个值得铭记的瞬间</p>
          </div>
          <p v-if="!hasTimelineActivity" class="text-center text-sm text-gray-500">{{ timelineData.length ? '你已经迈出了第一步，记录一个目标或完成一次学习后，这条时光轴会继续生长。' : '今天记录一件小事，明天就多一份可以回看的成长。' }}</p>
          <div class="relative max-w-3xl mx-auto">
            <div v-for="(item, idx) in timelineItems" :key="idx"
                 class="relative mb-12 flex justify-start scroll-animate"
                 :class="{ 'justify-end': idx % 2 === 1 }"
                 :style="{ transitionDelay: `${idx * 0.1}s` }">
              <div class="absolute left-1/2 -translate-x-1/2 w-10 flex flex-col items-center">
                <div class="w-4 h-4 bg-brand-500 rounded-full border-2 border-white shadow-[0_0_0_4px_rgba(var(--theme-primary-rgb),0.2)] z-10"></div>
                <div class="w-0.5 h-16 bg-gray-300 mt-2" v-if="idx !== timelineItems.length - 1"></div>
              </div>
              <div class="w-[calc(50%-50px)] rounded-2xl p-5 shadow-md hover:shadow-xl transition-all hover:-translate-y-1"
                   :class="[isDark ? 'bg-white/10' : 'bg-white', { 'ml-auto': idx % 2 === 1 }]">
                <div class="text-xs text-gray-500 mb-2">{{ item.date }}</div>
                <div class="inline-block px-2 py-1 rounded-full text-xs font-medium mb-3" :class="{
                  'bg-amber-100 text-amber-700': item.stage === '高中',
                  'bg-blue-100 text-blue-700': item.stage === '大学',
                  'bg-green-100 text-green-700': item.stage === '职场',
                  'bg-purple-100 text-purple-700': item.stage === '文章',
                  'bg-rose-100 text-rose-700': item.stage === '成长'
                }">{{ item.stage }}</div>
                <h3 class="text-lg font-semibold mb-2" :class="isDark ? 'text-white' : 'text-gray-800'">{{ item.title }}</h3>
                <p class="text-sm leading-relaxed mb-3" :class="isDark ? 'text-gray-400' : 'text-gray-600'">{{ item.description }}</p>
                <div class="flex gap-2 flex-wrap">
                  <span v-for="tag in item.tags" :key="tag" class="px-2 py-1 bg-gray-100 rounded-full text-xs text-gray-600">{{ tag }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 图谱总览区域 - 可点击标题 -->
      <section v-if="stageVisible(['all', 'high_school', 'university', 'workplace'])" id="milestone" class="py-20" :class="isDark ? 'bg-black' : 'bg-white'">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div
            class="text-center mb-12 scroll-animate cursor-pointer group"
            @click="showPopup('milestone', $event)"
            data-section="milestone"
          >
            <h2 class="text-3xl lg:text-4xl font-bold mb-3 inline-flex items-center gap-2 group-hover:text-brand-500 transition-colors" :class="isDark ? 'text-white group-hover:text-brand-400' : 'text-gray-900 group-hover:text-brand-600'">
              📜 图谱总览
              <svg class="w-5 h-5 opacity-0 group-hover:opacity-100 transition-opacity" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0121 0z"></path>
              </svg>
            </h2>
            <p class="text-gray-500">笔墨留香，拾字成金，记录学习路上的点滴收获</p>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
            <div v-for="(item, idx) in milestones" :key="idx"
                 class="rounded-2xl p-6 text-center transition-all hover:-translate-y-2 hover:shadow-xl scroll-animate"
                 :class="isDark ? 'bg-white/10' : 'bg-gray-50'"
                 :style="{ transitionDelay: `${idx * 0.05}s` }">
              <div class="text-4xl mb-3">{{ item.icon }}</div>
              <div class="text-xs text-brand-500 font-medium mb-2">{{ item.year }}</div>
              <h3 class="font-semibold mb-2" :class="isDark ? 'text-white' : 'text-gray-800'">{{ item.title }}</h3>
              <p class="text-xs text-gray-500 mb-4">{{ item.desc }}</p>
              <div class="h-1 bg-gray-200 rounded-full overflow-hidden mb-2">
                <div class="h-full bg-gradient-to-r from-brand-500 to-accent-500 rounded-full transition-all" :style="{ width: item.progress + '%' }"></div>
              </div>
              <span class="text-xs text-gray-400">{{ item.status }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 成长数据统计 -->
      <section class="py-16" :class="isDark ? 'bg-black' : 'bg-gray-50'">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div v-for="(stat, idx) in [
              { icon: '📚', number: '2,847', label: '学习时长（小时）', trend: '↑ 较上周 +12%', trendColor: 'text-green-500' },
              { icon: '✍️', number: '156', label: '时光笔记', trend: '累计 3.2万字', trendColor: 'text-gray-400' },
              { icon: '📝', number: '12', label: '在线考试', trend: '进入组卷与复盘', trendColor: 'text-green-500' },
              { icon: '🎯', number: '23', label: '目标达成', trend: '完成率 85%', trendColor: 'text-green-500' }
            ]" :key="idx"
                 class="rounded-2xl p-7 text-center hover:-translate-y-1 transition-all shadow-sm hover:shadow-lg scroll-animate"
                 :class="isDark ? 'bg-white/10' : 'bg-white'"
                 :style="{ transitionDelay: `${idx * 0.1}s` }">
              <div class="text-4xl mb-3">{{ stat.icon }}</div>
              <div class="text-3xl font-bold mb-2" :class="isDark ? 'text-white' : 'text-gray-800'">{{ stat.number }}</div>
              <div class="text-sm text-gray-500 mb-2">{{ stat.label }}</div>
              <div class="text-xs" :class="stat.trendColor">{{ stat.trend }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 在线考试 -->
      <section v-if="stageVisible(['all', 'high_school', 'university', 'workplace'])" id="exam" class="py-20" :class="isDark ? 'bg-black' : 'bg-white'">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div
            class="text-center mb-12 scroll-animate"
          >
            <h2 class="text-3xl lg:text-4xl font-bold mb-3 inline-flex items-center gap-2" :class="isDark ? 'text-white' : 'text-gray-900'">
              📝 在线考试
            </h2>
            <p class="text-gray-500">快速进入组卷、考试、错题复盘和成绩分析</p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <button
              v-for="(card, idx) in examCards"
              :key="card.title"
              @click="navigateWithTransition(card.link)"
              class="group text-left rounded-xl p-6 transition-all hover:-translate-y-2 hover:shadow-xl scroll-animate border"
              :class="isDark ? 'bg-white/10 border-white/10 hover:border-brand-400/40' : 'bg-gray-50 border-gray-100 hover:border-brand-200'"
              :style="{ transitionDelay: `${idx * 0.05}s` }"
            >
              <div class="flex items-start justify-between gap-4 mb-5">
                <div class="w-12 h-12 rounded-xl bg-brand-500/10 text-3xl flex items-center justify-center">{{ card.icon }}</div>
                <svg class="w-5 h-5 mt-1 text-gray-400 group-hover:text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </div>
              <h4 class="font-semibold mb-2" :class="isDark ? 'text-white' : 'text-gray-800'">{{ card.title }}</h4>
              <p class="text-sm leading-relaxed" :class="isDark ? 'text-gray-400' : 'text-gray-600'">{{ card.desc }}</p>
            </button>
          </div>

          <div class="text-center mt-10 scroll-animate">
            <button @click="viewMore" class="px-6 py-2 border border-gray-300 text-gray-600 rounded-full hover:border-brand-400 hover:text-brand-500 transition-all">进入考试中心 →</button>
          </div>
        </div>
      </section>

      <!-- 云边小札 - 可点击标题 -->
      <section v-if="stageVisible(['all', 'high_school', 'university', 'workplace'])" id="journal" class="py-20" :class="isDark ? 'bg-black' : 'bg-gray-50'">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div
            class="text-center mb-12 scroll-animate cursor-pointer group"
            @click="showPopup('journal', $event)"
            data-section="journal"
          >
            <h2 class="text-3xl lg:text-4xl font-bold mb-3 inline-flex items-center gap-2 group-hover:text-brand-500 transition-colors" :class="isDark ? 'text-white group-hover:text-brand-400' : 'text-gray-900 group-hover:text-brand-600'">
              📖 云边小札
              <svg class="w-5 h-5 opacity-0 group-hover:opacity-100 transition-opacity" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0121 0z"></path>
              </svg>
            </h2>
            <p class="text-gray-500">记录日常感悟，见证点滴成长</p>
          </div>
          <div class="max-w-3xl mx-auto space-y-5">
            <div v-for="(entry, idx) in journalEntries" :key="idx"
                 class="rounded-2xl p-6 flex gap-5 transition-all hover:translate-x-1 hover:shadow-lg scroll-animate"
                 :class="isDark ? 'bg-white/10' : 'bg-white'"
                 :style="{ transitionDelay: `${idx * 0.1}s` }">
              <div class="text-center min-w-[60px]">
                <div class="text-3xl font-bold text-brand-500 leading-none">{{ entry.day }}</div>
                <div class="text-xs text-gray-400 mt-1">{{ entry.month }}</div>
              </div>
              <div class="flex-1">
                <h3 class="font-semibold mb-2" :class="isDark ? 'text-white' : 'text-gray-800'">{{ entry.title }}</h3>
                <p class="text-sm leading-relaxed mb-3" :class="isDark ? 'text-gray-400' : 'text-gray-600'">{{ entry.excerpt }}</p>
                <div class="flex gap-4 text-xs text-gray-400">
                  <span>❤️ {{ entry.likes }} 喜欢</span>
                  <span>💬 {{ entry.comments }} 评论</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 每日寄语 -->
      <section id="daily-quote" class="py-20 bg-gradient-to-r">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div class="rounded-3xl p-8 lg:p-12 text-center backdrop-blur-sm scroll-animate" :class="isDark ? 'bg-white/10 text-gray-300' : 'bg-white'">
            <div class="text-5xl text-accent-400 opacity-50 mb-4">“</div>
            <div class="text-xl lg:text-2xl leading-relaxed max-w-2xl mx-auto mb-5 italic min-h-[4.5rem] flex items-center justify-center">
              <AdvancedTypewriter
                :key="quoteTypewriterKey"
                :texts="[dailyQuote.text]"
                :type-speed="45"
                :delete-speed="25"
                :pause-time="5000"
                :loop="false"
                :keep-last-text="true"
                :show-cursor="true"
                text-class="text-center"
              />
            </div>
            <p class="text-gray-500 mb-5">—— {{ dailyQuote.author }}</p>
            <div class="max-w-2xl mx-auto text-left">
              <label class="block text-sm font-medium mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-700'">写下今天想分享的一句话</label>
              <textarea
                v-model="quoteDraft"
                maxlength="200"
                rows="3"
                class="w-full rounded-xl border px-4 py-3 text-sm outline-none resize-none transition-colors"
                :class="isDark ? 'bg-black/20 border-white/20 text-white placeholder:text-gray-500 focus:border-brand-400' : 'bg-gray-50 border-gray-200 text-gray-800 placeholder:text-gray-400 focus:border-brand-400'"
                placeholder="此刻想留给大家的话..."
              ></textarea>
              <div class="mt-2 flex flex-wrap items-center justify-between gap-3">
                <span class="text-xs text-gray-400">{{ quoteDraft.length }}/200</span>
                <div class="flex flex-wrap gap-2">
                  <button type="button" @click="publishQuote" :disabled="quotePublishing" class="inline-flex items-center gap-1.5 px-4 py-2 bg-brand-500 text-white rounded-lg text-sm hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                    <EditPen class="w-4 h-4" /> {{ quotePublishing ? '发表中...' : '发表寄语' }}
                  </button>
                  <button type="button" @click="navigateWithTransition('/PersonalProfile?tab=quotes')" class="inline-flex items-center gap-1.5 px-4 py-2 border rounded-lg text-sm transition-colors" :class="isDark ? 'border-white/20 text-gray-300 hover:border-brand-400 hover:text-brand-400' : 'border-gray-200 text-gray-600 hover:border-brand-400 hover:text-brand-600'">
                    <Collection class="w-4 h-4" /> 我的寄语
                  </button>
                  <button type="button" @click="refreshQuote" title="换一句寄语" aria-label="换一句寄语" class="inline-flex items-center gap-1.5 px-4 py-2 border rounded-lg text-sm transition-colors" :class="isDark ? 'border-white/20 text-gray-300 hover:border-brand-400 hover:text-brand-400' : 'border-gray-200 text-gray-600 hover:border-brand-400 hover:text-brand-600'">
                    <RefreshRight class="w-4 h-4" /> 换一句
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部 -->
      <footer class="py-16" :class="isDark ? 'bg-black' : 'bg-gray-100'">
        <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10 mb-10">
            <div>
              <div class="flex items-center gap-2 mb-3">
                <span class="text-2xl">⏰</span>
                <span class="text-xl font-semibold" :class="isDark ? 'text-white' : 'text-gray-900'">拾光记</span>
              </div>
              <p class="text-sm mb-4" :class="isDark ? 'text-gray-400' : 'text-gray-600'">记录每一段值得珍藏的时光</p>
              <div class="flex gap-4 text-xl" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                <span>📧</span><span>💬</span><span>📱</span>
              </div>
            </div>
            <div>
              <h4 class="font-semibold mb-4" :class="isDark ? 'text-white' : 'text-gray-900'">关于我们</h4>
              <ul class="space-y-2 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                <li class="hover:text-white cursor-pointer">品牌故事</li>
                <li class="hover:text-white cursor-pointer">加入我们</li>
                <li class="hover:text-white cursor-pointer">联系我们</li>
              </ul>
            </div>
            <div>
              <h4 class="font-semibold mb-4" :class="isDark ? 'text-white' : 'text-gray-900'">帮助中心</h4>
              <ul class="space-y-2 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                <li class="hover:text-white cursor-pointer">使用指南</li>
                <li class="hover:text-white cursor-pointer">隐私政策</li>
                <li class="hover:text-white cursor-pointer">服务条款</li>
              </ul>
            </div>
            <div>
              <h4 class="font-semibold mb-4" :class="isDark ? 'text-white' : 'text-gray-900'">关注我们</h4>
              <ul class="space-y-2 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                <li class="hover:text-white cursor-pointer">微信公众号</li>
                <li class="hover:text-white cursor-pointer">小红书</li>
                <li class="hover:text-white cursor-pointer">微博</li>
              </ul>
            </div>
          </div>
          <div class="text-center pt-8 border-t text-xs" :class="[isDark ? 'border-gray-800 text-gray-500' : 'border-gray-200 text-gray-400']">
            <p>© 2026 拾光记 Chronicles of Time · 让成长有迹可循</p>
          </div>
        </div>
      </footer>

      <!-- 返回顶部 -->
      <button v-show="showBackTop" @click="scrollToTop" class="fixed bottom-10 right-10 w-11 h-11 bg-white rounded-full shadow-lg flex items-center justify-center hover:-translate-y-1 transition-all z-50">
        <svg class="w-5 h-5 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19V5M5 12l7-7 7 7"></path>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.1); }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.animate-float {
  animation: float 20s ease-in-out infinite;
}

.animate-fadeIn {
  animation: fadeIn 0.2s ease-out;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-5000 {
  animation-delay: 5s;
}

.timeline-preview-shell {
  height: 372px;
  overflow: hidden;
  padding: 22px 24px;
}

.timeline-preview-heading {
  display: flex;
  height: 28px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  color: inherit;
  font-size: 13px;
  font-weight: 700;
}

.timeline-preview-heading strong {
  display: grid;
  min-width: 28px;
  height: 24px;
  place-items: center;
  border: 1px solid rgb(var(--theme-primary-rgb) / 0.28);
  border-radius: 6px;
  background: rgb(var(--theme-primary-rgb) / 0.1);
  color: var(--theme-primary);
  font-size: 11px;
}

.timeline-preview-viewport {
  height: 288px;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-color: transparent transparent;
  scrollbar-width: thin;
}

.timeline-preview-viewport:hover,
.timeline-preview-viewport:focus-visible {
  scrollbar-color: rgb(var(--theme-primary-rgb) / 0.55) transparent;
}

.timeline-preview-viewport:focus-visible {
  outline: 2px solid rgb(var(--theme-primary-rgb) / 0.45);
  outline-offset: -2px;
}

.timeline-preview-viewport::-webkit-scrollbar {
  width: 6px;
}

.timeline-preview-viewport::-webkit-scrollbar-track {
  background: transparent;
}

.timeline-preview-viewport::-webkit-scrollbar-thumb {
  border-radius: 6px;
  background: transparent;
}

.timeline-preview-viewport:hover::-webkit-scrollbar-thumb,
.timeline-preview-viewport:focus-visible::-webkit-scrollbar-thumb {
  background: rgb(var(--theme-primary-rgb) / 0.55);
}

.timeline-preview-track {
  position: relative;
  padding-left: 30px;
}

.timeline-preview-track::before {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 7px;
  width: 1px;
  background: rgb(148 163 184 / 0.55);
  content: '';
}

.timeline-preview-node {
  position: relative;
  display: flex;
  height: 72px;
  align-items: center;
  color: inherit;
}

.timeline-preview-dot {
  position: absolute;
  top: 29px;
  left: -29px;
  width: 13px;
  height: 13px;
  border: 2px solid white;
  border-radius: 50%;
  background: var(--theme-primary);
  box-shadow: 0 0 0 3px rgb(var(--theme-primary-rgb) / 0.2);
}

.timeline-preview-copy {
  min-width: 0;
  width: 100%;
  padding-right: 10px;
}

.timeline-preview-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgb(148 163 184);
  font-size: 11px;
  line-height: 1;
}

.timeline-preview-meta time {
  color: inherit;
  font-variant-numeric: tabular-nums;
  font-weight: 700;
}

.timeline-preview-meta span {
  overflow: hidden;
  max-width: 88px;
  border-radius: 4px;
  padding: 3px 5px;
  background: rgb(var(--theme-primary-rgb) / 0.12);
  color: var(--theme-primary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.timeline-preview-copy p {
  overflow: hidden;
  margin-top: 7px;
  color: inherit;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.timeline-preview-node--highschool .timeline-preview-dot { background: #f59e0b; box-shadow: 0 0 0 3px rgb(245 158 11 / 0.2); }
.timeline-preview-node--highschool .timeline-preview-meta span { background: rgb(245 158 11 / 0.13); color: #d97706; }
.timeline-preview-node--article .timeline-preview-dot { background: #a855f7; box-shadow: 0 0 0 3px rgb(168 85 247 / 0.2); }
.timeline-preview-node--article .timeline-preview-meta span { background: rgb(168 85 247 / 0.13); color: #9333ea; }
.timeline-preview-node--work .timeline-preview-dot { background: #10b981; box-shadow: 0 0 0 3px rgb(16 185 129 / 0.2); }
.timeline-preview-node--work .timeline-preview-meta span { background: rgb(16 185 129 / 0.13); color: #059669; }
.timeline-preview-node--study .timeline-preview-dot { background: #06b6d4; box-shadow: 0 0 0 3px rgb(6 182 212 / 0.2); }
.timeline-preview-node--study .timeline-preview-meta span { background: rgb(6 182 212 / 0.13); color: #0891b2; }

.dark .timeline-preview-dot {
  border-color: #111827;
}

@media (max-width: 640px) {
  .timeline-preview-shell {
    height: 340px;
    padding: 18px;
  }

  .timeline-preview-viewport {
    height: 256px;
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

/* 自定义滚动条 */
.floating-popup ::-webkit-scrollbar {
  width: 4px;
}

.floating-popup ::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.floating-popup ::-webkit-scrollbar-thumb {
  background: #c7d2fe;
  border-radius: 10px;
}

.dark .floating-popup ::-webkit-scrollbar-track {
  background: #1e293b;
}

.dark .floating-popup ::-webkit-scrollbar-thumb {
  background: #334155;
}
.home-quick-link,
.home-mobile-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--app-card-solid) 90%, var(--theme-primary) 10%);
  color: var(--app-text-secondary);
  font-size: 12px;
  font-weight: 700;
  transition: transform 260ms cubic-bezier(0.16, 1, 0.3, 1), border-color 200ms ease, box-shadow 260ms ease;
}

.home-quick-link {
  min-height: 38px;
  padding: 0 10px;
}

.home-mobile-link {
  width: 38px;
  height: 38px;
}

.home-quick-link svg,
.home-mobile-link svg {
  width: 17px;
  height: 17px;
  color: var(--theme-primary);
}

.home-quick-link:hover,
.home-mobile-link:hover {
  border-color: var(--theme-primary);
  box-shadow: 0 12px 22px -18px rgb(var(--theme-primary-rgb) / 0.9);
  transform: translateY(-2px);
}
</style>
