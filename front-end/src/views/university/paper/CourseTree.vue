<!--
  文件说明：拾光记前台应用大学阶段页面组件，承载大学阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import * as echarts from 'echarts'
import request from "@/utils/request.js"
import { useTheme } from '@/composables/useTheme'
import Nav from "@/components/Nav.vue";

/** 粉紫主题 + 课程状态语义色 */
const BRAND = {
  primary: '#d946ef',
  secondary: '#a855f7',
  accent: '#ec4899',
  light: '#f0abfc',
  muted: '#e4e4e7',
}
const STATUS_COLORS = {
  passed: '#22c55e',
  failed: '#ef4444',
  studying: '#f59e0b',
  retake: '#e879f9',
  pending: '#a1a1aa',
}
const CATEGORY_PALETTE = ['#d946ef', '#a855f7', '#ec4899', '#c026d3', '#e879f9', '#f472b6']

const getCategoryColor = (category, index = 0) =>
  category?.color && category.color !== '#10b981' ? category.color : CATEGORY_PALETTE[index % CATEGORY_PALETTE.length]

const router = useRouter()
const [messageApi, contextHolder] = message.useMessage();
const { isDark } = useTheme()
const isScrolled = ref(false)

const normalizeId = (value) => value == null ? value : String(value)

const normalizeMajor = (major) => ({
  ...major,
  id: normalizeId(major.id)
})

const normalizeCategory = (category) => ({
  ...category,
  id: normalizeId(category.id),
  majorId: normalizeId(category.majorId),
  parentId: normalizeId(category.parentId)
})

const normalizeCourse = (course) => ({
  ...course,
  id: normalizeId(course.id),
  majorId: normalizeId(course.majorId),
  categoryId: normalizeId(course.categoryId)
})

const normalizeStudentCourse = (studentCourse) => ({
  ...studentCourse,
  id: normalizeId(studentCourse.id),
  courseId: normalizeId(studentCourse.courseId),
  majorId: normalizeId(studentCourse.majorId)
})

// 数据
const majors = ref([])
const currentMajorId = ref(null)
const categories = ref([])
const courses = ref([])
const studentCourses = ref([])
const progress = ref({
  majorName: '',
  totalCreditsRequired: 160,
  totalCreditsEarned: 0,
  compulsoryCreditsRequired: 120,
  compulsoryCreditsEarned: 0,
  electiveCreditsRequired: 40,
  electiveCreditsEarned: 0,
  gpa: 0,
  progressPercent: 0
})

// 搜索
const searchKeyword = ref('')
const searchResults = ref([])
const isSearching = ref(false)

// 图表
const chartRef = ref(null)
let chart = null

// 弹窗相关
const showCourseModal = ref(false)
const showCategoryFormModal = ref(false)
const showCourseFormModal = ref(false)
const loading = ref(false)
const isCategoryMode = ref(false)
const currentEditId = ref(null)

// 专业详情弹窗
const showMajorModal = ref(false)
const majorDetail = ref(null)

//导航栏
const menuItems = [
  { key: 'hub', label: '大学中心', icon: '🎓', path: '/UniversityHub' },
  { key: 'tree', label: '课程树', icon: '🌳', path: '/CourseTree' },
  { key: 'paper', label: '写论文', icon: '📄', path: '/Paper' },
]


// 表单数据
const formData = ref({
  id: null,
  courseCode: '',
  name: '',
  credit: 2,
  term: 1,
  courseType: 'compulsory',
  examType: 'exam',
  totalHours: 32,
  description: '',
  prerequisite: ''
})

const categoryForm = ref({
  id: null,
  name: '',
  icon: '📚',
  color: BRAND.primary,
  sortOrder: 0
})

const courseForm = ref({
  id: null,
  categoryId: null,
  courseCode: '',
  name: '',
  credit: 2,
  term: 1,
  courseType: 'compulsory',
  examType: 'exam',
  totalHours: 32,
  description: '',
  prerequisite: ''
})

const scoreInput = ref('')
const studentCourseInfo = ref(null)

// 计算属性
const modalTitle = computed(() => isCategoryMode.value ? '分类详情' : '课程详情')
const categoryFormTitle = computed(() => categoryForm.value.id ? '编辑分类' : '添加分类')
const courseFormTitle = computed(() => courseForm.value.id ? '编辑课程' : '添加课程')

// 获取专业列表
const getMajors = async () => {
  try {
    const res = await request.get('/university/major/list')
    console.log('专业列表:', res)
    if (res.code === 200 && res.data && res.data.length > 0) {
      majors.value = res.data.map(normalizeMajor)
      currentMajorId.value = majors.value[0].id
      await loadAllData()
    }
  } catch (error) {
    console.error('获取专业列表失败', error)
  }
}

// 加载所有数据
const loadAllData = async () => {
  if (!currentMajorId.value) {
    console.warn('currentMajorId is null')
    return
  }
  console.log('开始加载数据, majorId:', currentMajorId.value)
  await Promise.all([
    loadCategories(),
    loadCourses(),
    loadStudentCourses(),
    loadProgress(),
    loadMajorInfo()
  ])
  console.log('数据加载完成, categories:', categories.value.length, 'courses:', courses.value.length)
  await nextTick()
  renderTree()
}

// 加载分类
const loadCategories = async () => {
  if (!currentMajorId.value) return
  try {
    const res = await request.get('/course-category/list', { majorId: currentMajorId.value })
    console.log('分类接口返回:', res)
    if (res.code === 200) {
      categories.value = (res.data || []).map(normalizeCategory)
    }
  } catch (error) {
    console.error('获取分类失败', error)
    categories.value = []
  }
}

// 加载课程
const loadCourses = async () => {
  if (!currentMajorId.value) return
  try {
    const res = await request.get('/course/list', { majorId: currentMajorId.value })
    console.log('课程接口返回:', res)
    if (res.code === 200) {
      courses.value = (res.data || []).map(normalizeCourse)
    }
  } catch (error) {
    console.error('获取课程失败', error)
    courses.value = []
  }
}

// 加载学生选课
const loadStudentCourses = async () => {
  try {
    const res = await request.get('/student-course/list')
    console.log('选课接口返回:', res)
    if (res.code === 200) {
      studentCourses.value = (res.data || []).map(normalizeStudentCourse)
    }
  } catch (error) {
    console.error('获取选课信息失败', error)
    studentCourses.value = []
  }
}

// 加载毕业进度
const loadProgress = async () => {
  if (!currentMajorId.value) return
  try {
    const res = await request.get('/student-course/progress', { majorId: currentMajorId.value })
    console.log('进度接口返回:', res)
    if (res.code === 200 && res.data) {
      progress.value = res.data
    }
  } catch (error) {
    console.error('获取进度失败', error)
  }
}

// 加载专业信息
const loadMajorInfo = async () => {
  if (!currentMajorId.value) return
  try {
    const res = await request.get(`/university/major/${currentMajorId.value}`)
    console.log('专业接口返回:', res)
    if (res.code === 200 && res.data) {
      progress.value.majorName = res.data.name
    }
  } catch (error) {
    console.error('获取专业信息失败', error)
  }
}

// 获取课程修读状态
const getCourseStatus = (courseId) => {
  const sc = studentCourses.value.find(s => s.courseId === courseId)
  if (!sc) return { status: 'planned', score: null, gradePoint: null, isPassed: false, isRetake: false }
  return {
    status: sc.status,
    score: sc.score,
    gradePoint: sc.gradePoint,
    isPassed: sc.isPassed === 1,
    isRetake: sc.isRetake === 1
  }
}

// 搜索
const onSearch = async () => {
  if (!currentMajorId.value) return
  if (!searchKeyword.value.trim()) {
    isSearching.value = false
    renderTree()
    return
  }
  isSearching.value = true
  try {
    const res = await request.get('/course/search', { majorId: currentMajorId.value, keyword: searchKeyword.value })
    if (res.code === 200) {
      searchResults.value = res.data || []
      renderTree()
    }
  } catch (error) {
    console.error('搜索失败', error)
  }
}

// 构建树数据
const buildTreeData = () => {
  // 搜索模式
  if (isSearching.value && searchKeyword.value && searchResults.value.length > 0) {
    return searchResults.value.map(course => {
      const status = getCourseStatus(course.id)
      return {
        name: `${course.name} (${course.credit}学分)`,
        value: course.credit,
        id: course.id,
        type: 'course',
        categoryId: course.categoryId,
        itemStyle: getItemStyleByStatus(status),
        label: { show: true, position: 'right', offset: [5, 0], formatter: `{b}` },
        symbol: 'circle',
        symbolSize: 12
      }
    })
  }

  // 正常模式 - 构建树形结构
  if (!categories.value || categories.value.length === 0) {
    console.log('没有分类数据')
    return []
  }

  // 创建专业根节点
  const majorNode = {
    name: `🎓 ${progress.value.majorName || '专业课程体系'}`,
    id: `major-${currentMajorId.value}`,
    type: 'major',
    itemStyle: { color: '#d946ef' },
    label: { color: '#d946ef', fontWeight: 'bold', fontSize: 16, show: true, position: 'top' },
    symbol: 'rect',
    symbolSize: 25,
    children: []
  }

  categories.value.forEach((category, index) => {
    const categoryCourses = courses.value.filter(c => c.categoryId === category.id)
    const catColor = getCategoryColor(category, index)

    const children = categoryCourses.map(course => {
      const status = getCourseStatus(course.id)
      return {
        name: `${course.name} (${course.credit}学分)`,
        value: course.credit,
        id: course.id,
        type: 'course',
        categoryId: course.categoryId,
        itemStyle: getItemStyleByStatus(status),
        label: { show: true, position: 'right', offset: [5, 0], formatter: `{b}` },
        symbol: 'circle',
        symbolSize: 12
      }
    })

    majorNode.children.push({
      name: `${category.icon || '📚'} ${category.name}`,
      id: category.id,
      type: 'category',
      itemStyle: { color: catColor },
      label: { color: catColor, fontWeight: 'bold', fontSize: 14, show: true, position: 'top' },
      symbol: 'rect',
      symbolSize: 20,
      children: children
    })
  })

  console.log('构建的树数据:', [majorNode])
  return [majorNode]
}

// 根据状态获取样式
const getItemStyleByStatus = (status) => {
  if (status.isPassed) return { color: STATUS_COLORS.passed, borderColor: STATUS_COLORS.passed, borderWidth: 2 }
  if (status.status === 'failed') return { color: STATUS_COLORS.failed, borderColor: STATUS_COLORS.failed, borderWidth: 2 }
  if (status.status === 'studying') return { color: STATUS_COLORS.studying, borderColor: STATUS_COLORS.studying, borderWidth: 2 }
  if (status.isRetake) return { color: STATUS_COLORS.retake, borderColor: STATUS_COLORS.retake, borderWidth: 2 }
  return { color: STATUS_COLORS.pending, borderColor: STATUS_COLORS.pending, borderWidth: 1 }
}

const courseStatusLegend = [
  { key: 'passed', label: '已通过', color: STATUS_COLORS.passed },
  { key: 'studying', label: '修读中', color: STATUS_COLORS.studying },
  { key: 'failed', label: '未通过', color: STATUS_COLORS.failed },
  { key: 'retake', label: '重修', color: STATUS_COLORS.retake },
  { key: 'pending', label: '未修读', color: STATUS_COLORS.pending },
]

const getChartTheme = () => ({
  line: isDark.value ? '#3f3f46' : '#e4e4e7',
  lineEmphasis: BRAND.primary,
  tooltipBg: isDark.value ? 'rgba(24,24,27,0.95)' : 'rgba(255,255,255,0.98)',
  tooltipBorder: BRAND.primary,
  tooltipText: isDark.value ? '#fafafa' : '#18181b',
  labelColor: isDark.value ? '#d4d4d8' : '#52525b',
})

// 打开专业详情（从后端获取）
const openMajorDetail = async (majorIdStr) => {
  // 从majorIdStr中提取实际的majorId（格式为'major-1'）
  const majorId = String(majorIdStr).replace(/^major-/, '')
  console.log('点击专业节点，majorId:', majorId)

  try {
    // 从后端获取专业详细信息
    const res = await request.get(`/university/major/${majorId}`)
    console.log('专业详情接口返回:', res)

    if (res.code === 200 && res.data) {
      majorDetail.value = res.data
      showMajorModal.value = true
    } else {
      messageApi.error('未找到专业信息')
    }
  } catch (error) {
    console.error('获取专业信息失败', error)
    messageApi.error('获取专业信息失败，请稍后重试')
  }
}

// 渲染树图
const renderTree = () => {
  if (!chartRef.value) {
    console.log('chartRef 不存在')
    return
  }

  const treeData = buildTreeData()
  console.log('渲染树图, 数据长度:', treeData.length)

  if (treeData.length === 0) {
    // 显示空状态
    if (chart) chart.dispose()
    chart = echarts.init(chartRef.value)
    chart.setOption({
      title: {
        text: '暂无课程数据',
        left: 'center',
        top: 'center',
        textStyle: { color: isDark.value ? '#9ca3af' : '#6b7280', fontSize: 14 }
      }
    })
    return
  }

  if (chart) chart.dispose()

  chart = echarts.init(chartRef.value, null, { renderer: 'canvas' })
  const theme = getChartTheme()
  const option = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      backgroundColor: theme.tooltipBg,
      borderColor: theme.tooltipBorder,
      borderWidth: 1,
      textStyle: { color: theme.tooltipText, fontSize: 13 },
      extraCssText: 'border-radius: 12px; box-shadow: 0 8px 24px rgba(217,70,239,0.15);'
    },
    series: [{
      type: 'tree',
      data: treeData,
      layout: 'vertical',
      symbol: 'emptyCircle',
      symbolSize: 14,
      roam: true,
      expandAndCollapse: true,
      initialTreeDepth: -1,
      animation: true,
      animationDuration: 400,
      lineStyle: { color: theme.line, width: 1.5, curveness: 0.5 },
      edgeShape: 'curve',
      focusNodeAdjacency: false,
      label: {
        position: 'right',
        verticalAlign: 'middle',
        align: 'left',
        fontSize: 12,
        color: theme.labelColor,
        offset: [10, 0],
        show: true
      },
      leaves: { label: { position: 'right', offset: [10, 0], show: true, color: theme.labelColor } },
      emphasis: { focus: 'descendant', lineStyle: { color: theme.lineEmphasis, width: 2 } }
    }]
  }
  chart.setOption(option)

  chart.off('click')
  chart.on('click', (params) => {
    if (params.data && params.data.id) {
      if (params.data.type === 'major') {
        openMajorDetail(params.data.id)
      } else if (params.data.type === 'category') {
        openCategoryDetail(params.data.id)
      } else if (params.data.type === 'course') {
        openCourseDetail(params.data.id)
      }
    }
  })
}

// 打开分类详情
const openCategoryDetail = async (categoryId) => {
  isCategoryMode.value = true
  currentEditId.value = categoryId
  const category = categories.value.find(c => c.id === categoryId)
  if (category) {
    categoryForm.value = { ...category }
  }
  showCourseModal.value = true
}

// 打开课程详情
const openCourseDetail = async (courseId) => {
  isCategoryMode.value = false
  currentEditId.value = courseId
  try {
    const res = await request.get(`/course/${courseId}`)
    if (res.code === 200) {
      formData.value = normalizeCourse(res.data)
      courseForm.value = { ...normalizeCourse(res.data) }
      const sc = studentCourses.value.find(s => s.courseId === courseId)
      studentCourseInfo.value = sc || null
      scoreInput.value = sc?.score || ''
    }
  } catch (error) {
    console.error('获取课程详情失败', error)
  }
  showCourseModal.value = true
}

// 保存成绩
const saveScore = async () => {
  if (!currentEditId.value || isCategoryMode.value) return
  try {
    const res = await request.post('/student-course/score', null, {
      courseId: currentEditId.value,
      score: scoreInput.value,
      majorId: currentMajorId.value
    })
    if (res.code === 200) {
      messageApi.success('成绩保存成功')
      await loadStudentCourses()
      await loadProgress()
      renderTree()
      showCourseModal.value = false
    } else {
      messageApi.error(res.message || '保存失败')
    }
  } catch (error) {
    messageApi.error('保存失败')
  }
}

// 打开添加分类弹窗
const openAddCategoryModal = () => {
  categoryForm.value = { id: null, name: '', icon: '📚', color: BRAND.primary, sortOrder: 0 }
  showCategoryFormModal.value = true
}

// 打开编辑分类弹窗
const openEditCategoryModal = () => {
  if (currentEditId.value) {
    const category = categories.value.find(c => c.id === currentEditId.value)
    if (category) {
      categoryForm.value = { ...category }
      showCategoryFormModal.value = true
    }
  }
}

// 保存分类
const saveCategory = async () => {
  if (!categoryForm.value.name) {
    messageApi.warning('请输入分类名称')
    return
  }
  loading.value = true
  try {
    const url = categoryForm.value.id ? '/course-category/update' : '/course-category/create'
    const data = { ...categoryForm.value, majorId: currentMajorId.value }
    const res = await request[categoryForm.value.id ? 'put' : 'post'](url, data)
    if (res.code === 200) {
      messageApi.success(categoryForm.value.id ? '更新成功' : '创建成功')
      showCategoryFormModal.value = false
      await loadCategories()
      renderTree()
    } else {
      messageApi.error(res.message || '操作失败')
    }
  } catch (error) {
    messageApi.error('操作失败')
  } finally {
    loading.value = false
  }
}

// 删除分类
const deleteCategory = () => {
  Modal.confirm({
    title: '确认删除',
    content: '删除分类将同时删除该分类下的所有课程，确定删除吗？',
    okType: 'danger',
    async onOk() {
      try {
        const res = await request.delete(`/course-category/${currentEditId.value}`)
        if (res.code === 200) {
          messageApi.success('删除成功')
          showCourseModal.value = false
          await loadCategories()
          await loadCourses()
          renderTree()
        } else {
          messageApi.error(res.message || '删除失败')
        }
      } catch (error) {
        messageApi.error('删除失败')
      }
    }
  })
}

// 打开添加课程弹窗
const openAddCourseModal = () => {
  courseForm.value = {
    id: null,
    categoryId: currentEditId.value,
    courseCode: '',
    name: '',
    credit: 2,
    term: 1,
    courseType: 'compulsory',
    examType: 'exam',
    totalHours: 32,
    description: '',
    prerequisite: ''
  }
  showCourseFormModal.value = true
}

// 打开编辑课程弹窗
const openEditCourseModal = () => {
  if (currentEditId.value) {
    const course = courses.value.find(c => c.id === currentEditId.value)
    if (course) {
      courseForm.value = { ...course }
      showCourseFormModal.value = true
    }
  }
}

// 保存课程
const saveCourse = async () => {
  if (!courseForm.value.name) {
    messageApi.warning('请输入课程名称')
    return
  }
  loading.value = true
  try {
    const url = courseForm.value.id ? '/course/update' : '/course/create'
    const data = { ...courseForm.value, majorId: currentMajorId.value }
    const res = await request[courseForm.value.id ? 'put' : 'post'](url, data)
    if (res.code === 200) {
      messageApi.success(courseForm.value.id ? '更新成功' : '创建成功')
      showCourseFormModal.value = false
      await loadCourses()
      renderTree()
    } else {
      messageApi.error(res.message || '操作失败')
    }
  } catch (error) {
    messageApi.error('操作失败')
  } finally {
    loading.value = false
  }
}

// 删除课程
const deleteCourse = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定删除这门课程吗？',
    okType: 'danger',
    async onOk() {
      try {
        const res = await request.delete(`/course/${currentEditId.value}`)
        if (res.code === 200) {
          messageApi.success('删除成功')
          showCourseModal.value = false
          await loadCourses()
          renderTree()
        } else {
          messageApi.error(res.message || '删除失败')
        }
      } catch (error) {
        messageApi.error('删除失败')
      }
    }
  })
}

// 专业切换
const onMajorChange = async () => {
  await loadAllData()
}

// 滚动监听
const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

watch(isDark, () => {
  setTimeout(() => renderTree(), 80)
})

// 添加滚动动画
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

watch([categories, courses], () => {
  renderTree()
})

onMounted(() => {
  getMajors()
  window.addEventListener('scroll', handleScroll)
  setTimeout(initScrollAnimation, 100)
  window.scrollTo(0, 0)
})

onUnmounted(() => {
  if (chart) chart.dispose()
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <contextHolder />
  <div class="app-shell overflow-x-hidden" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
      <Nav :isDark="isDark" :menuItems="menuItems" :showBackHome="true" />

      <main class="app-main">
        <div class="app-container space-y-6">
          <!-- 页头 -->
          <div class="flex flex-col lg:flex-row lg:items-end lg:justify-between gap-4 scroll-animate">
            <div>
              <p class="app-section-label mb-2">大学 · 培养方案</p>
              <h1 class="app-page-title">课程树</h1>
              <p class="app-page-desc">可视化课程体系，跟踪毕业学分与修读状态</p>
            </div>
            <div class="flex flex-wrap items-center gap-3">
              <div class="relative">
                <select
                  v-model="currentMajorId"
                  @change="onMajorChange"
                  class="course-tree-select pl-4 pr-9 py-2.5 rounded-xl text-sm border focus:ring-2 focus:ring-brand-500/50 appearance-none cursor-pointer min-w-[160px]"
                  :class="isDark ? 'bg-dark-surface border-dark-border text-zinc-100' : 'bg-white border-zinc-200 text-zinc-800'"
                >
                  <option v-for="major in majors" :key="major.id" :value="major.id">{{ major.name }}</option>
                </select>
                <svg class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                </svg>
              </div>
              <div class="relative">
                <input
                  v-model="searchKeyword"
                  @input="onSearch"
                  type="text"
                  placeholder="搜索课程..."
                  class="pl-10 pr-4 py-2.5 rounded-xl text-sm border focus:ring-2 focus:ring-brand-500/50 w-52 lg:w-64"
                  :class="isDark ? 'bg-dark-surface border-dark-border text-zinc-100 placeholder-zinc-500' : 'bg-white border-zinc-200 text-zinc-800'"
                />
                <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <button type="button" class="app-btn-primary text-sm" @click="openAddCategoryModal">+ 添加分类</button>
            </div>
          </div>

          <!-- 毕业进度 -->
          <div class="app-card-surface p-6 scroll-animate">
            <div class="flex flex-wrap justify-between items-start gap-4 mb-5">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-xl app-gradient-bar flex items-center justify-center text-2xl shadow-glow">
                  🎓
                </div>
                <div>
                  <h3 class="font-semibold text-lg text-zinc-900 dark:text-zinc-50">毕业进度</h3>
                  <p class="text-xs text-zinc-500 dark:text-zinc-400">{{ progress.majorName || '请选择专业' }}</p>
                </div>
              </div>
              <div class="flex flex-wrap gap-x-6 gap-y-2 text-sm">
                <div>
                  <span class="text-zinc-500">已修学分</span>
                  <span class="font-semibold text-brand-600 dark:text-brand-400 text-lg ml-1">{{ progress.totalCreditsEarned || 0 }}</span>
                  <span class="text-zinc-400">/ {{ progress.totalCreditsRequired || 160 }}</span>
                </div>
                <div>
                  <span class="text-zinc-500">必修</span>
                  <span class="font-medium ml-1">{{ progress.compulsoryCreditsEarned || 0 }}</span>
                  <span class="text-zinc-400">/ {{ progress.compulsoryCreditsRequired || 120 }}</span>
                </div>
                <div>
                  <span class="text-zinc-500">选修</span>
                  <span class="font-medium ml-1">{{ progress.electiveCreditsEarned || 0 }}</span>
                  <span class="text-zinc-400">/ {{ progress.electiveCreditsRequired || 40 }}</span>
                </div>
                <div>
                  <span class="text-zinc-500">绩点</span>
                  <span class="font-semibold text-amber-500 ml-1">{{ progress.gpa || 0 }}</span>
                </div>
              </div>
            </div>
            <div class="relative h-2.5 rounded-full overflow-hidden" :class="isDark ? 'bg-zinc-800' : 'bg-zinc-100'">
              <div
                class="absolute inset-y-0 left-0 app-gradient-bar rounded-full transition-all duration-500"
                :style="{ width: (progress.progressPercent || 0) + '%' }"
              />
            </div>
            <p class="text-right text-xs text-brand-600 dark:text-brand-400 mt-1.5 font-medium">{{ progress.progressPercent || 0 }}% 完成</p>
          </div>

          <!-- 图例 + 课程树 -->
          <div class="app-card-surface p-4 lg:p-6 scroll-animate">
            <div class="flex flex-wrap items-center justify-between gap-3 mb-4 pb-4 border-b" :class="isDark ? 'border-dark-border' : 'border-zinc-100'">
              <span class="text-sm font-medium text-zinc-700 dark:text-zinc-300">课程状态</span>
              <div class="flex flex-wrap gap-3">
                <span
                  v-for="item in courseStatusLegend"
                  :key="item.key"
                  class="inline-flex items-center gap-1.5 text-xs text-zinc-600 dark:text-zinc-400"
                >
                  <span class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: item.color }" />
                  {{ item.label }}
                </span>
              </div>
            </div>
            <div
              ref="chartRef"
              class="tree-chart rounded-xl"
              :class="isDark ? 'bg-zinc-950/50' : 'bg-zinc-50/80'"
            />
          </div>
        </div>
      </main>

      <footer class="py-10 border-t" :class="isDark ? 'border-dark-border bg-dark-bg' : 'border-zinc-200 bg-zinc-50/80'">
        <div class="app-container text-center">
          <p class="text-sm text-zinc-500">© 2026 拾光记 · 让成长有迹可循</p>
        </div>
      </footer>
  </div>

    <!-- 专业详情弹窗 -->
    <Modal v-model:open="showMajorModal" title="专业详情" width="600px" centered :footer="null" :class="isDark ? 'dark-modal' : ''">
      <div v-if="majorDetail" class="major-detail-content">
        <h3 class="major-detail-title">{{ majorDetail.name }}</h3>
        <div class="major-detail-grid">
          <div class="major-detail-item">
            <p class="major-detail-label">专业代码</p>
            <p class="major-detail-value">{{ majorDetail.code || '暂无' }}</p>
          </div>
          <div class="major-detail-item">
            <p class="major-detail-label">学科门类</p>
            <p class="major-detail-value">{{ majorDetail.category || '暂无' }}</p>
          </div>
          <div class="major-detail-item">
            <p class="major-detail-label">专业类</p>
            <p class="major-detail-value">{{ majorDetail.subCategory || '暂无' }}</p>
          </div>
          <div class="major-detail-item">
            <p class="major-detail-label">学制</p>
            <p class="major-detail-value">{{ majorDetail.duration || 4 }}年</p>
          </div>
          <div class="major-detail-item">
            <p class="major-detail-label">平均学费</p>
            <p class="major-detail-value">{{ majorDetail.tuitionFee || 0 }}元/年</p>
          </div>
          <div class="major-detail-item">
            <p class="major-detail-label">学位类型</p>
            <p class="major-detail-value">{{ majorDetail.degree || '学士' }}</p>
          </div>
        </div>
        <div class="major-detail-intro">
          <p class="major-detail-label">专业介绍</p>
          <p class="major-detail-text">{{ majorDetail.description || '暂无介绍' }}</p>
        </div>
      </div>
    </Modal>

    <!-- 课程详情弹窗 -->
    <Modal v-model:open="showCourseModal" :title="modalTitle" :footer="null" width="520px" centered :class="isDark ? 'dark-modal' : ''">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-3 text-sm">
          <div><span class="text-gray-500">课程代码:</span> <span class="font-mono">{{ formData.courseCode || '-' }}</span></div>
          <div><span class="text-gray-500">学分:</span> <span class="font-semibold text-brand-600 dark:text-brand-400">{{ formData.credit }}</span></div>
          <div><span class="text-gray-500">学期:</span> 第{{ formData.term }}学期</div>
          <div><span class="text-gray-500">课程类型:</span> <span :class="formData.courseType === 'compulsory' ? 'text-red-500' : 'text-blue-500'">{{ formData.courseType === 'compulsory' ? '必修' : '选修' }}</span></div>
          <div><span class="text-gray-500">考核方式:</span> {{ formData.examType === 'exam' ? '考试' : '考查' }}</div>
          <div><span class="text-gray-500">总学时:</span> {{ formData.totalHours }}学时</div>
        </div>
        <div v-if="formData.description" class="text-sm"><span class="text-gray-500">简介:</span> {{ formData.description }}</div>
        <div v-if="formData.prerequisite" class="text-sm"><span class="text-gray-500">先修课程:</span> {{ formData.prerequisite }}</div>

        <!-- 成绩信息 -->
        <div v-if="!isCategoryMode" class="border-t pt-3 mt-2" :class="isDark ? 'border-gray-700' : 'border-gray-100'">
          <h4 class="text-sm font-semibold mb-2">📊 修读情况</h4>
          <div class="flex items-center gap-3">
            <div class="flex-1">
              <input v-model.number="scoreInput" type="number" step="0.5" min="0" max="100" placeholder="输入成绩" class="w-full px-3 py-2 rounded-lg text-sm border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-gray-50 border-gray-200'">
            </div>
            <button type="button" class="app-btn-primary text-sm !py-2" @click="saveScore">保存成绩</button>
          </div>
          <div v-if="studentCourseInfo" class="mt-2 text-xs">
            <span class="text-gray-500">当前状态:</span>
            <span :class="studentCourseInfo.isPassed ? 'text-green-500' : 'text-red-500'">
              {{ studentCourseInfo.isPassed ? '✅ 已通过' : (studentCourseInfo.score ? '❌ 未通过' : '📝 未修读') }}
            </span>
            <span v-if="studentCourseInfo.score" class="ml-3">成绩: {{ studentCourseInfo.score }}分</span>
            <span v-if="studentCourseInfo.gradePoint" class="ml-3">绩点: {{ studentCourseInfo.gradePoint }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-3 pt-3 border-t" :class="isDark ? 'border-gray-700' : 'border-gray-100'">
          <button v-if="isCategoryMode" type="button" class="flex-1 app-btn-primary text-sm !py-2" @click="openAddCourseModal">+ 添加课程</button>
          <button v-if="isCategoryMode" type="button" class="flex-1 app-btn-secondary text-sm !py-2" @click="openEditCategoryModal">✏️ 编辑分类</button>
          <button v-if="isCategoryMode" @click="deleteCategory" class="flex-1 py-2 bg-red-500 text-white rounded-lg text-sm font-medium hover:bg-red-600 transition-all">🗑️ 删除分类</button>
          <button v-if="!isCategoryMode" type="button" class="flex-1 app-btn-secondary text-sm !py-2" @click="openEditCourseModal">✏️ 编辑课程</button>
          <button v-if="!isCategoryMode" @click="deleteCourse" class="flex-1 py-2 bg-red-500 text-white rounded-lg text-sm font-medium hover:bg-red-600 transition-all">🗑️ 删除课程</button>
        </div>
      </div>
    </Modal>

    <!-- 添加/编辑分类弹窗 -->
    <Modal v-model:open="showCategoryFormModal" :title="categoryFormTitle" @ok="saveCategory" :confirmLoading="loading" centered :class="isDark ? 'dark-modal' : ''">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-1">分类名称</label>
          <input v-model="categoryForm.name" type="text" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">图标</label>
          <input v-model="categoryForm.icon" type="text" placeholder="🌳" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">分类颜色</label>
          <div class="flex flex-wrap gap-2 mb-2">
            <button
              v-for="c in CATEGORY_PALETTE"
              :key="c"
              type="button"
              class="w-8 h-8 rounded-lg border-2 transition-all"
              :class="categoryForm.color === c ? 'border-brand-500 scale-110' : 'border-transparent'"
              :style="{ backgroundColor: c }"
              @click="categoryForm.color = c"
            />
          </div>
          <input v-model="categoryForm.color" type="color" class="w-full h-10 rounded-lg border cursor-pointer" :class="isDark ? 'border-dark-border' : 'border-zinc-200'">
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">排序</label>
          <input v-model.number="categoryForm.sortOrder" type="number" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
      </div>
    </Modal>

    <!-- 添加/编辑课程弹窗 -->
    <Modal v-model:open="showCourseFormModal" :title="courseFormTitle" @ok="saveCourse" :confirmLoading="loading" width="600px" centered :class="isDark ? 'dark-modal' : ''">
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">课程代码</label>
          <input v-model="courseForm.courseCode" type="text" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">课程名称</label>
          <input v-model="courseForm.name" type="text" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">学分</label>
          <input v-model.number="courseForm.credit" type="number" step="0.5" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">学期</label>
          <select v-model.number="courseForm.term" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
            <option v-for="i in 8" :key="i" :value="i">第{{ i }}学期</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">课程类型</label>
          <select v-model="courseForm.courseType" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
            <option value="compulsory">必修</option>
            <option value="elective">选修</option>
            <option value="limited_elective">限选</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">考核方式</label>
          <select v-model="courseForm.examType" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
            <option value="exam">考试</option>
            <option value="assessment">考查</option>
            <option value="practical">实践</option>
          </select>
        </div>
        <div class="col-span-2">
          <label class="block text-sm font-medium mb-1">总学时</label>
          <input v-model.number="courseForm.totalHours" type="number" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
        <div class="col-span-2">
          <label class="block text-sm font-medium mb-1">课程简介</label>
          <textarea v-model="courseForm.description" rows="3" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'"></textarea>
        </div>
        <div class="col-span-2">
          <label class="block text-sm font-medium mb-1">先修课程</label>
          <input v-model="courseForm.prerequisite" type="text" class="w-full px-3 py-2 rounded-lg border focus:ring-2 focus:ring-brand-500" :class="isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-200'">
        </div>
      </div>
    </Modal>
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

.tree-chart {
  width: 100%;
  height: min(65vh, 720px);
  min-height: 420px;
}

/* 暗色模式弹窗 */
:deep(.dark-modal .ant-modal-content) {
  background-color: #18181b !important;
  color: #fafafa !important;
  border: 1px solid #3f3f46;
  border-radius: 16px;
}
:deep(.dark-modal .ant-modal-header) {
  background-color: #18181b !important;
  border-bottom-color: #3f3f46 !important;
}
:deep(.dark-modal .ant-modal-title) {
  color: #f3f4f6 !important;
}
:deep(.dark-modal .ant-modal-close) {
  color: #9ca3af !important;
}
:deep(.dark-modal .ant-modal-footer) {
  border-top-color: #374151 !important;
}

/* 专业详情弹窗样式 */
.major-detail-content {
  padding: 8px 0;
}

.major-detail-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #d946ef;
  border-left: 3px solid #d946ef;
  padding-left: 12px;
}

.major-detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.major-detail-item {
  background: #fafafa;
  border: 1px solid #e4e4e7;
  border-radius: 10px;
  padding: 10px 12px;
}

.dark .major-detail-item {
  background: #27272a;
  border-color: #3f3f46;
}

.major-detail-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.dark .major-detail-label {
  color: #9ca3af;
}

.major-detail-value {
  font-weight: 500;
  color: #1f2937;
}

.dark .major-detail-value {
  color: #f3f4f6;
}

.major-detail-intro {
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
  margin-top: 4px;
}

.dark .major-detail-intro {
  border-top-color: #374151;
}

.major-detail-text {
  font-size: 13px;
  line-height: 1.6;
  margin-top: 8px;
  color: #4b5563;
}

.dark .major-detail-text {
  color: #d1d5db;
}
</style>
