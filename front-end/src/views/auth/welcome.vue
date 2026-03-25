<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import router from "@/router/index.js";
import {
  ThemeType,
  getStoredTheme,
  setTheme,
  toggleTheme as toggleGlobalTheme,
  initTheme,
  onThemeChange
} from "@/utils/theme.js";
import AdvancedPageTransition from '@/components/AdvancedPageTransition.vue';

// 页面过渡组件引用
const transitionRef = ref(null);

// 响应式主题状态
const isDark = ref(false)

// 导航项
const navItems = [
  { id: 'hero', label: '首页' },
  { id: 'features', label: '功能' },
  { id: 'solutions', label: '解决方案' },
  { id: 'showcase', label: '案例' },
  { id: 'pricing', label: '价格' }
]

// 品牌列表
const brands = ['Microsoft', 'Google', 'Amazon', 'Apple', 'Meta', 'Netflix']

// 额外功能
const extraFeatures = [
  { icon: '📊', title: '数据可视化', description: '直观的数据图表展示' },
  { icon: '🤖', title: 'AI 智能分析', description: '人工智能辅助决策' },
  { icon: '🔔', title: '实时通知', description: '重要消息即时推送' },
  { icon: '📱', title: '移动端适配', description: '随时随地访问' },
  { icon: '🔒', title: '数据加密', description: '银行级安全保护' },
  { icon: '⚡', title: '极速响应', description: '毫秒级加载速度' }
]

// 用户评价
const testimonials = [
  { name: '张伟', role: '产品经理', avatar: '👨‍💼', content: '拾光记让团队协作效率提升了50%以上，界面设计也非常优雅。' },
  { name: '李娜', role: '设计师', avatar: '👩‍🎨', content: '特别喜欢它的记录功能，帮我保存了很多灵感瞬间。' },
  { name: '王强', role: '创业者', avatar: '👨‍💻', content: '从0到1的成长记录，拾光记见证了我们团队的每一步。' }
]

// 常见问题
const faqs = ref([
  { question: '拾光记适合个人使用吗？', answer: '当然适合！拾光记既支持个人记录生活点滴，也支持团队协作。', open: false },
  { question: '数据安全如何保障？', answer: '我们采用银行级加密技术，所有数据都经过严格保护。', open: false },
  { question: '支持多端同步吗？', answer: '支持！Web、iOS、Android全平台同步，随时随地访问。', open: false },
  { question: '有免费试用吗？', answer: '新用户可享受14天免费试用，无需绑定信用卡。', open: false }
])

// 动画数字
const animatedValues = ref(['0', '0', '0', '0'])
let statsAnimated = false

// 滚动动画观察器
let observer = null

// 从全局主题工具同步主题状态
const syncThemeState = (theme) => {
  isDark.value = theme === ThemeType.DARK
}

// 切换主题
const toggleTheme = () => {
  toggleGlobalTheme()
}

// 平滑滚动到指定区域
const scrollToSection = (sectionId) => {
  const element = document.getElementById(sectionId)
  if (element) {
    const offset = 80 // 导航栏高度偏移
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }
}

// 过渡完成处理
const handleTransitionComplete = () => {
  // 过渡完成后可以执行后续操作
  console.log('过渡动画完成')
}

// 带过渡效果的导航 - 修改这个函数
const navigateWithTransition = (path) => {
  if (transitionRef.value) {
    // 显示过渡层
    transitionRef.value.show()
    // 延迟跳转，让过渡动画显示足够长时间
    setTimeout(() => {
      router.push(path)
    }, 3000)  // 1.5秒后跳转，配合组件的duration
  } else {
    // 如果没有过渡组件，直接跳转
    router.push(path)
  }
}

// 滚动到顶部按钮
const showScrollTop = ref(false)

const handleScroll = () => {
  showScrollTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 初始化滚动动画观察器
const initScrollObserver = () => {
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in')

        // 如果是统计数字区域，触发数字动画
        if (entry.target.classList.contains('stat-item') && !statsAnimated) {
          statsAnimated = true
          animateNumbers()
        }

        observer.unobserve(entry.target)
      }
    })
  }, { threshold: 0.2 })

  // 观察所有需要动画的元素
  document.querySelectorAll('.animate-on-scroll').forEach(el => {
    observer.observe(el)
  })
}

// 数字动画
const animateNumbers = () => {
  const stats = [
    { value: 50, unit: 'K+' },
    { value: 99.9, unit: '%' },
    { value: 24, unit: '/7' },
    { value: 200, unit: '+' }
  ]

  stats.forEach((stat, index) => {
    let current = 0
    const target = stat.value
    const increment = target / 50
    const interval = setInterval(() => {
      current += increment
      if (current >= target) {
        current = target
        clearInterval(interval)
      }
      if (stat.unit === '%') {
        animatedValues.value[index] = current.toFixed(1)
      } else {
        animatedValues.value[index] = Math.floor(current).toString()
      }
    }, 20)
  })
}

// 切换FAQ
const toggleFaq = (index) => {
  faqs.value[index].open = !faqs.value[index].open
}

// 打字机效果
const texts = [
  '拾光记・记录每一刻价值',
  '拾光记，让时间有迹可循',
  '记录每一刻努力，沉淀每一份成果',
  '开启未来之旅'
]

const displayText = ref('')
let currentIndex = 0
let currentCharIndex = 0
let isDeleting = false
let timer = null

const runTypewriter = () => {
  const currentText = texts[currentIndex]

  if (!isDeleting) {
    if (currentCharIndex < currentText.length) {
      displayText.value = currentText.substring(0, currentCharIndex + 1)
      currentCharIndex++
      timer = setTimeout(runTypewriter, 100)
    } else {
      timer = setTimeout(() => {
        isDeleting = true
        runTypewriter()
      }, 2000)
    }
  } else {
    if (currentCharIndex > 0) {
      displayText.value = currentText.substring(0, currentCharIndex - 1)
      currentCharIndex--
      timer = setTimeout(runTypewriter, 50)
    } else {
      isDeleting = false
      currentIndex = (currentIndex + 1) % texts.length
      timer = setTimeout(runTypewriter, 500)
    }
  }
}

const stopTypewriter = () => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

// 功能数据
const features = [
  {
    title: '高中记录',
    description: '拾光记，陪伴你走过伏案刷题的日夜，记录少年意气与成长蜕变，让青春时光不负奔赴，让每一份努力都值得被铭记。',
    iconPath: 'M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z'
  },
  {
    title: '大学时光',
    description: '从青涩入学到成熟毕业，从课堂学习到社会实践，拾光记全程记录你的努力、收获与蜕变，让青春不留白，让成长有迹可循。',
    iconPath: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z'
  },
  {
    title: '伴你工作',
    description: '从初入职场到独当一面，从日常执行到项目攻坚，拾光记陪你记录每一步成长，让时光不负努力，让付出皆有回响。',
    iconPath: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z'
  },
  {
    title: '跨平台同步',
    description: '多端数据同步，随时随地保持工作状态',
    iconPath: 'M10.5 1.5H8.25A2.25 2.25 0 006 3.75v16.5a2.25 2.25 0 002.25 2.25h10.5A2.25 2.25 0 0021 20.25V3.75a2.25 2.25 0 00-2.25-2.25H13.5m-3 0V3h3V1.5m-3 0h3m-3 18.75h3'
  }
]

// 解决方案
const solutions = [
  { title: '项目管理', description: '高效的项目追踪和任务分配系统' },
  { title: '团队协作', description: '实时沟通、文件共享、评论反馈' },
  { title: '数据分析', description: '智能数据可视化，洞察业务趋势' },
  { title: '自动化流程', description: '自定义工作流，减少重复性工作' }
]

// 统计数据
const stats = [
  { value: '50K', label: '活跃用户', unit: '+' },
  { value: '99.9', label: '服务可用性', unit: '%' },
  { value: '24', label: '技术支持', unit: '/7' },
  { value: '200', label: '全球客户', unit: '+' }
]

// 案例数据
const cases = [
  {
    industry: '科技 / SaaS',
    title: 'TechCorp 数字化转型',
    description: '通过 拾光记 平台，工作效率提升 200%，协作成本降低 40%'
  },
  {
    industry: '金融 / 银行',
    title: 'FinBank 智能办公',
    description: '实现全流程自动化，审批时间从 3 天缩短至 2 小时'
  },
  {
    industry: '教育 / 在线',
    title: 'EduPlus 远程教学',
    description: '服务 10,000+ 学生，课程管理效率提升 150%'
  }
]

// 价格方案
const pricing = [
  {
    name: '基础版',
    price: '¥99',
    description: '适合个人和小团队',
    features: ['5 个项目', '10GB 存储空间', '基础支持', 'API 访问']
  },
  {
    name: '专业版',
    price: '¥299',
    description: '适合成长型企业',
    features: ['无限项目', '100GB 存储空间', '优先支持', '高级分析', '团队协作'],
    popular: true
  },
  {
    name: '企业版',
    price: '定制',
    description: '适合大型企业',
    features: ['无限项目', '无限存储', '专属支持', '定制开发', 'SSO 集成']
  }
]

// 团队数据
const team = [
  { name: '张明', role: '创始人 & CEO', avatar: '👨‍💼' },
  { name: '李华', role: '技术总监', avatar: '👨‍💻' },
  { name: '王芳', role: '产品总监', avatar: '👩‍🎨' },
  { name: '陈晨', role: '市场总监', avatar: '👩‍💼' }
]

let stopThemeListening = null

onMounted(() => {
  // 初始化全局主题
  initTheme()

  // 监听主题变化
  stopThemeListening = onThemeChange(syncThemeState)

  // 启动打字机效果
  runTypewriter()

  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)

  // 初始化滚动动画观察器
  nextTick(() => {
    initScrollObserver()
  })

  // 初始触发一次滚动检测
  handleScroll()
})

onUnmounted(() => {
  stopTypewriter()
  window.removeEventListener('scroll', handleScroll)
  if (stopThemeListening) {
    stopThemeListening()
  }
  if (observer) {
    observer.disconnect()
  }
})
</script>

<template>
  <!-- 页面过渡组件 -->
  <AdvancedPageTransition ref="transitionRef"  :duration="10000" @complete="handleTransitionComplete" />

  <div
    :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-slate-50 via-white to-slate-100 text-slate-800',
      'min-h-screen transition-colors duration-500'
    ]"
  >
    <!-- 主题切换按钮 -->
    <button
      @click="toggleTheme"
      class="fixed top-6 right-6 z-50 w-10 h-10 rounded-full flex items-center justify-center shadow-lg transition-all duration-300 hover:scale-110"
      :class="isDark ? 'bg-black text-white' : 'bg-white text-slate-800 border border-slate-200'"
      aria-label="切换主题"
    >
      <svg v-if="!isDark" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
      </svg>
      <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    </button>

    <!-- 导航栏 -->
    <nav
      ref="navbarRef"
      class="fixed top-0 left-0 right-0 z-40 backdrop-blur-md border-b transition-all duration-500 shadow-md"
      :class="isDark ? 'bg-black/80 border-gray-600/50' : 'bg-white/80 border-slate-200/50'"
    >
      <div class="container mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <!-- Logo -->
          <div class="flex items-center space-x-3 cursor-pointer group" @click="scrollToSection('hero')">
            <div
              class="w-10 h-10 rounded-xl shadow-lg flex items-center justify-center transition-all duration-300 group-hover:scale-110"
              :class="isDark ? 'bg-gradient-to-br from-gray-700 to-gray-900' : 'bg-gradient-to-br from-slate-700 to-slate-900'"
            >
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z"/>
              </svg>
            </div>
            <span
              class="text-xl font-light tracking-wide transition-colors duration-300"
              :class="isDark ? 'text-gray-100' : 'text-slate-700'"
            >拾光记</span>
          </div>

          <!-- 导航链接 -->
          <div class="hidden md:flex items-center space-x-8">
            <a
              v-for="item in navItems"
              :key="item.id"
              :href="`#${item.id}`"
              @click.prevent="scrollToSection(item.id)"
              class="relative transition-colors duration-300 hover:font-medium group"
              :class="isDark ? 'text-gray-300 hover:text-white' : 'text-slate-600 hover:text-slate-900'"
            >
              {{ item.label }}
              <span class="absolute -bottom-1 left-0 w-0 h-0.5 bg-current transition-all duration-300 group-hover:w-full"></span>
            </a>
          </div>

          <!-- 按钮 -->
          <div class="flex items-center space-x-4">
            <button
            @click="navigateWithTransition('/login')"
            class="transition-colors duration-300"
            :class="isDark ? 'text-gray-300 hover:text-white' : 'text-slate-600 hover:text-slate-900'"
          >登录</button>
            <button
              class="px-5 py-2 rounded-full hover:scale-105 transition-all duration-300 shadow-lg"
              :class="isDark ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-slate-800 text-white hover:bg-slate-700'"
            >
              免费试用
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Hero 区域 -->
    <section id="hero" ref="heroRef" class="relative pt-32 pb-20 overflow-hidden">
      <!-- 背景装饰 -->
      <div class="absolute inset-0 overflow-hidden">
        <div
          class="absolute top-1/4 -left-48 w-96 h-96 rounded-full blur-3xl animate-float-slow transition-colors duration-500"
          :class="isDark ? 'bg-amber-900/20' : 'bg-amber-100/30'"
        ></div>
        <div
          class="absolute bottom-1/4 -right-48 w-96 h-96 rounded-full blur-3xl animate-float-slower transition-colors duration-500"
          :class="isDark ? 'bg-slate-800/40' : 'bg-slate-200/40'"
        ></div>
        <div
          class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] rounded-full blur-3xl animate-pulse-slow transition-colors duration-500"
          :class="isDark ? 'bg-indigo-900/20' : 'bg-indigo-50/20'"
        ></div>
      </div>

      <div class="relative container mx-auto px-6">
        <div class="max-w-5xl mx-auto text-center">
          <!-- 徽章 -->
          <div
            class="inline-flex items-center px-4 py-2 rounded-full mb-6 animate-fade-in-up transition-colors duration-500"
            :class="isDark ? 'bg-gray-800 border border-gray-700' : 'bg-slate-100'"
          >
            <span class="w-2 h-2 bg-emerald-500 rounded-full mr-2 animate-pulse"></span>
            <span
              class="text-sm transition-colors duration-300"
              :class="isDark ? 'text-gray-300' : 'text-slate-600'"
            >✨ 全新发布 v2.0</span>
          </div>

          <!-- 主标题 -->
          <h1 class="text-5xl md:text-7xl font-bold mb-6 animate-fade-in-up animation-delay-200">
            <span
              class="bg-clip-text text-transparent transition-all duration-500"
              :class="isDark ? 'bg-gradient-to-r from-gray-100 via-white to-gray-200' : 'bg-gradient-to-r from-slate-700 via-slate-800 to-slate-900'"
            >
              {{ displayText }}
            </span>
            <span
              class="inline-block w-0.5 h-10 md:h-12 ml-1 animate-blink"
              :class="isDark ? 'bg-gray-400' : 'bg-slate-600'"
            ></span>
          </h1>

          <p
            class="text-lg md:text-xl max-w-2xl mx-auto leading-relaxed mb-8 animate-fade-in-up animation-delay-400 transition-colors duration-300"
            :class="isDark ? 'text-gray-300' : 'text-slate-500'"
          >
            拾光记（Chronicles of Time），是一款以时光记录、成长沉淀、高效协作为核心的轻量化平台，取 "捡拾时光、记刻美好" 之意，帮用户留住生活与工作中每一段值得珍藏的瞬间。
          </p>

          <!-- CTA 按钮 -->
          <div class="flex flex-col sm:flex-row gap-4 justify-center animate-fade-in-up animation-delay-600">
            <button
              class="px-8 py-3 rounded-full font-medium hover:scale-105 transition-all duration-300 shadow-lg hover:shadow-xl flex items-center justify-center space-x-2 group"
              :class="isDark ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-slate-800 text-white hover:bg-slate-700'"
            >
              <span>开始免费试用</span>
              <svg class="w-5 h-5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6"/>
              </svg>
            </button>
            <button
              class="px-8 py-3 backdrop-blur-sm border rounded-full font-medium hover:scale-105 transition-all duration-300"
              :class="isDark ? 'bg-gray-800/50 border-gray-700 text-gray-200 hover:bg-gray-800/80' : 'bg-white/50 border-slate-200 text-slate-700 hover:bg-white/80'"
            >
              观看演示
            </button>
          </div>

          <!-- 信任品牌 -->
          <div
            class="mt-16 pt-8 border-t mb-8 animate-fade-in-up animation-delay-800 transition-colors duration-500"
            :class="isDark ? 'border-gray-700/50' : 'border-slate-200/50'"
          >
            <p
              class="text-sm mb-6 transition-colors duration-300"
              :class="isDark ? 'text-gray-400' : 'text-slate-400'"
            >已获得全球 10,000+ 企业的信赖</p>
            <div class="flex flex-wrap justify-center items-center gap-8 opacity-60">
              <div v-for="brand in brands" :key="brand" class="font-light text-xl transition-colors duration-300 hover:opacity-100 cursor-pointer"
                   :class="isDark ? 'text-gray-400 hover:text-gray-200' : 'text-slate-400 hover:text-slate-600'">
                {{ brand }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心功能区域 - 增加更多卡片 -->
    <section
      id="features"
      ref="featuresRef"
      class="py-20 transition-colors duration-500"
      :class="isDark ? 'bg-black' : 'bg-white/50'"
    >
      <div class="container mx-auto px-6">
        <div class="text-center mb-16">
          <h2
            class="text-3xl md:text-4xl font-bold mb-4 transition-colors duration-300"
            :class="isDark ? 'text-white' : 'text-slate-800'"
          >为什么选择 拾光记</h2>
          <p
            class="text-lg max-w-2xl mx-auto transition-colors duration-300"
            :class="isDark ? 'text-gray-300' : 'text-slate-500'"
          >时光匆匆，我们为你记刻</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div
            v-for="(feature, index) in features"
            :key="index"
            class="feature-card group rounded-2xl p-6 border shadow-sm transition-all duration-500 hover:-translate-y-2 hover:rotate-1"
            :class="[isDark ? 'bg-black border-gray-700' : 'bg-white border-slate-200', 'animate-on-scroll']"
            :style="{ transitionDelay: `${index * 100}ms` }"
          >
            <div
              class="w-14 h-14 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300"
              :class="isDark ? 'bg-gray-900' : 'bg-slate-100'"
            >
              <svg
                class="w-7 h-7 transition-colors duration-300"
                :class="isDark ? 'text-gray-300' : 'text-slate-600'"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  :d="feature.iconPath"
                />
              </svg>
            </div>
            <h3
              class="text-xl font-semibold mb-2 transition-colors duration-300"
              :class="isDark ? 'text-white' : 'text-slate-800'"
            >{{ feature.title }}</h3>
            <p
              class="leading-relaxed transition-colors duration-300"
              :class="isDark ? 'text-gray-400' : 'text-slate-500'"
            >{{ feature.description }}</p>
          </div>
        </div>

        <!-- 新增：更多功能卡片 -->
        <div class="mt-16">
          <h3 class="text-2xl font-semibold text-center mb-8" :class="isDark ? 'text-gray-200' : 'text-slate-700'">更多精彩功能</h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div v-for="(extra, index) in extraFeatures" :key="index" class="extra-card rounded-xl p-5 border transition-all duration-500 hover:-translate-y-1 animate-on-scroll"
                 :class="[isDark ? 'bg-black border-gray-700 hover:border-gray-500' : 'bg-white border-slate-200 hover:shadow-lg', 'cursor-pointer']"
                 :style="{ transitionDelay: `${index * 100}ms` }">
              <div class="flex items-start space-x-3">
                <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="isDark ? 'bg-gray-800' : 'bg-slate-100'">
                  <span class="text-xl">{{ extra.icon }}</span>
                </div>
                <div>
                  <h4 class="font-semibold mb-1" :class="isDark ? 'text-white' : 'text-slate-800'">{{ extra.title }}</h4>
                  <p class="text-sm" :class="isDark ? 'text-gray-400' : 'text-slate-500'">{{ extra.description }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 解决方案区域 - 增加卡片 -->
    <section
      id="solutions"
      ref="solutionsRef"
      class="py-20 transition-colors duration-500"
      :class="isDark ? 'bg-black' : 'bg-white'"
    >
      <div class="container mx-auto px-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div class="order-2 lg:order-1">
            <h2
              class="text-3xl md:text-4xl font-bold mb-4 transition-colors duration-300"
              :class="isDark ? 'text-white' : 'text-slate-800'"
            >一站式解决方案</h2>
            <p
              class="text-lg mb-6 transition-colors duration-300"
              :class="isDark ? 'text-gray-300' : 'text-slate-500'"
            >集成所有您需要的工具，让工作流程更加顺畅</p>
            <div class="space-y-4">
              <div
                v-for="(solution, index) in solutions"
                :key="index"
                class="solution-item flex items-start space-x-3 animate-on-scroll cursor-pointer group"
                :style="{ transitionDelay: `${index * 100}ms` }"
              >
                <div
                  class="w-6 h-6 rounded-full flex items-center justify-center mt-1 transition-all duration-300 group-hover:scale-110"
                  :class="isDark ? 'bg-emerald-900/20' : 'bg-emerald-100'"
                >
                  <svg class="w-4 h-4 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                </div>
                <div>
                  <h4
                    class="font-semibold transition-colors duration-300 group-hover:translate-x-1 inline-block"
                    :class="isDark ? 'text-white' : 'text-slate-800'"
                  >{{ solution.title }}</h4>
                  <p
                    class="text-sm transition-colors duration-300"
                    :class="isDark ? 'text-gray-400' : 'text-slate-500'"
                  >{{ solution.description }}</p>
                </div>
              </div>
            </div>
            <button
              class="mt-8 px-6 py-2 border rounded-full hover:scale-105 transition-all duration-300"
              :class="isDark ? 'border-gray-600 text-gray-300 hover:bg-gray-800' : 'border-slate-300 text-slate-700 hover:bg-slate-50'"
            >
              了解更多 →
            </button>
          </div>
          <div class="order-1 lg:order-2 animate-zoom-in">
            <div
              class="rounded-2xl p-8 shadow-lg transition-colors duration-500 hover:scale-105 transition-all duration-500"
              :class="isDark ? 'bg-gray-900' : 'bg-gradient-to-br from-slate-100 to-white'"
            >
              <div class="relative">
                <div
                  class="absolute -top-4 -right-4 w-20 h-20 rounded-full blur-2xl transition-colors duration-500"
                  :class="isDark ? 'bg-amber-900/30' : 'bg-amber-100'"
                ></div>
                <div class="rounded-xl shadow-lg w-full h-64 flex items-center justify-center" :class="isDark ? 'bg-gray-800' : 'bg-slate-200'">
                  <svg class="w-20 h-20" :class="isDark ? 'text-gray-600' : 'text-slate-400'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据统计区域 - 增加动画数字 -->
    <section
      class="py-20 transition-colors duration-500"
      :class="isDark ? 'bg-black border border-gray-700' : 'bg-white'"
    >
      <div class="container mx-auto px-6">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div
            v-for="(stat, index) in stats"
            :key="index"
            class="stat-item text-center group animate-on-scroll"
            :style="{ transitionDelay: `${index * 100}ms` }"
          >
            <div
              class="text-4xl md:text-5xl font-bold mb-2 group-hover:scale-110 transition-transform duration-300"
              :class="isDark ? 'text-white' : 'text-black'"
            >
              <span ref="statNumbers" :data-value="stat.value" :data-unit="stat.unit">{{ animatedValues[index] }}{{ stat.unit }}</span>
            </div>
            <div
              class="text-sm transition-colors duration-300"
              :class="isDark ? 'text-gray-300' : 'text-slate-300'"
            >{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 案例展示区域 - 增加更多案例卡片 -->
    <section
      id="showcase"
      ref="showcaseRef"
      class="py-20 transition-colors duration-500"
      :class="isDark ? 'bg-black' : 'bg-white'"
    >
      <div class="container mx-auto px-6">
        <div class="text-center mb-16">
          <h2
            class="text-3xl md:text-4xl font-bold mb-4 transition-colors duration-300"
            :class="isDark ? 'text-white' : 'text-slate-800'"
          >成功案例</h2>
          <p
            class="text-lg max-w-2xl mx-auto transition-colors duration-300"
            :class="isDark ? 'text-gray-300' : 'text-slate-500'"
          >看看我们的客户如何通过 拾光记 实现业务增长</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div
            v-for="(case_, index) in cases"
            :key="index"
            class="case-card group rounded-2xl overflow-hidden border shadow-sm transition-all duration-500 hover:-translate-y-2 hover:shadow-xl animate-on-scroll"
            :class="[isDark ? 'bg-black border-gray-700' : 'bg-white border-slate-200']"
            :style="{ transitionDelay: `${index * 100}ms` }"
          >
            <div
              class="h-48 relative overflow-hidden"
              :class="isDark ? 'bg-gray-700' : 'bg-slate-100'"
            >
              <div
                class="absolute inset-0 flex items-center justify-center transition-transform duration-500 group-hover:scale-110"
                :class="isDark ? 'bg-black' : 'bg-gradient-to-br from-slate-200 to-slate-300'"
              >
                <svg class="w-16 h-16" :class="isDark ? 'text-gray-600' : 'text-slate-400'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            </div>
            <div class="p-6">
              <div
                class="text-sm mb-2 transition-colors duration-300"
                :class="isDark ? 'text-gray-400' : 'text-slate-500'"
              >{{ case_.industry }}</div>
              <h3
                class="text-xl font-semibold mb-2 transition-colors duration-300"
                :class="isDark ? 'text-white' : 'text-slate-800'"
              >{{ case_.title }}</h3>
              <p
                class="mb-4 transition-colors duration-300"
                :class="isDark ? 'text-gray-400' : 'text-slate-500'"
              >{{ case_.description }}</p>
              <div class="flex items-center justify-between">
                <a
                  href="#"
                  class="font-medium inline-flex items-center transition-colors duration-300 group-hover:translate-x-1"
                  :class="isDark ? 'text-gray-300 hover:text-white' : 'text-slate-600 hover:text-slate-800'"
                >
                  查看案例
                  <svg class="w-4 h-4 ml-1 transition-transform group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                  </svg>
                </a>
                <div class="flex items-center space-x-1">
                  <svg class="w-4 h-4 text-emerald-500" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  <span class="text-sm" :class="isDark ? 'text-gray-400' : 'text-slate-500'">4.9</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 新增：更多案例卡片 -->
        <div class="mt-12 text-center">
          <button class="px-6 py-2 rounded-full border transition-all duration-300 hover:scale-105"
                  :class="isDark ? 'border-gray-600 text-gray-300 hover:bg-gray-800' : 'border-slate-300 text-slate-700 hover:bg-slate-50'">
            查看更多案例 →
          </button>
        </div>
      </div>
    </section>

    <!-- 价格方案区域 - 增加更多卡片 -->
    <section
      id="pricing"
      ref="pricingRef"
      class="py-20 transition-colors duration-500"
      :class="isDark ? 'bg-black' : 'bg-white/50'"
    >
      <div class="container mx-auto px-6">
        <div class="text-center mb-16">
          <h2
            class="text-3xl md:text-4xl font-bold mb-4 transition-colors duration-300"
            :class="isDark ? 'text-white' : 'text-slate-800'"
          >灵活的价格方案</h2>
          <p
            class="text-lg max-w-2xl mx-auto transition-colors duration-300"
            :class="isDark ? 'text-gray-300' : 'text-slate-500'"
          >选择最适合您的方案，随时升级或降级</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          <div
            v-for="(plan, index) in pricing"
            :key="index"
            class="pricing-card rounded-2xl p-8 border transition-all duration-500 hover:-translate-y-2 animate-on-scroll"
            :style="{ transitionDelay: `${index * 100}ms` }"
            :class="[
              isDark ? 'bg-black border-gray-700' : 'bg-white border-slate-200',
              plan.popular ? (isDark ? 'border-gray-500 shadow-xl' : 'border-slate-400 shadow-xl') : (isDark ? 'border-gray-700 shadow-sm' : 'border-slate-200 shadow-sm')
            ]"
          >
            <div v-if="plan.popular" class="absolute -top-3 left-1/2 transform -translate-x-1/2 px-3 py-1 rounded-full text-xs font-semibold"
                 :class="isDark ? 'bg-gray-700 text-white' : 'bg-slate-800 text-white'">
              最受欢迎
            </div>
            <h3
              class="text-2xl font-bold mb-2 transition-colors duration-300"
              :class="isDark ? 'text-white' : 'text-slate-800'"
            >{{ plan.name }}</h3>
            <div
              class="text-4xl font-bold mb-4 transition-colors duration-300"
              :class="isDark ? 'text-white' : 'text-slate-800'"
            >
              {{ plan.price }}
              <span
                class="text-base font-normal transition-colors duration-300"
                :class="isDark ? 'text-gray-400' : 'text-slate-500'"
              >/月</span>
            </div>
            <p
              class="mb-6 transition-colors duration-300"
              :class="isDark ? 'text-gray-400' : 'text-slate-500'"
            >{{ plan.description }}</p>
            <ul class="space-y-3 mb-8">
              <li
                v-for="feature in plan.features"
                :key="feature"
                class="flex items-center space-x-2 text-sm transition-colors duration-300"
                :class="isDark ? 'text-gray-300' : 'text-slate-600'"
              >
                <svg class="w-4 h-4 text-emerald-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
                <span>{{ feature }}</span>
              </li>
            </ul>
            <button
              class="w-full py-3 rounded-full font-medium hover:scale-105 transition-all duration-300"
              :class="[
               plan.popular
                 ? (isDark ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-slate-800 text-white hover:bg-slate-700')
                 : (isDark ? 'border border-gray-600 text-gray-300 hover:bg-gray-700' : 'border border-slate-300 text-slate-700 hover:bg-slate-50')
             ]"
            >
              {{ plan.popular ? '立即购买' : '选择方案' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 团队介绍区域 - 增加更多成员 -->
    <section
      class="py-20 transition-colors duration-500"
      :class="isDark ? 'bg-black' : 'bg-white'"
    >
      <div class="container mx-auto px-6">
        <div class="text-center mb-16">
          <h2
            class="text-3xl md:text-4xl font-bold mb-4 transition-colors duration-300"
            :class="isDark ? 'text-white' : 'text-slate-800'"
          >核心团队</h2>
          <p
            class="text-lg max-w-2xl mx-auto transition-colors duration-300"
            :class="isDark ? 'text-gray-300' : 'text-slate-500'"
          >一群充满激情的创造者，致力于打造极致产品</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-8 max-w-5xl mx-auto">
          <div
            v-for="(member, index) in team"
            :key="index"
            class="team-card text-center group animate-on-scroll cursor-pointer"
            :style="{ transitionDelay: `${index * 100}ms` }"
          >
            <div
              class="w-32 h-32 mx-auto mb-4 rounded-full flex items-center justify-center group-hover:scale-110 transition-all duration-300 group-hover:shadow-xl"
              :class="isDark ? 'bg-black border-2 border-gray-700' : 'bg-gradient-to-br from-slate-200 to-slate-300'"
            >
              <span
                class="text-4xl transition-colors duration-300 group-hover:scale-125 inline-block"
                :class="isDark ? 'text-gray-300' : 'text-slate-500'"
              >{{ member.avatar }}</span>
            </div>
            <h4
              class="text-lg font-semibold transition-colors duration-300"
              :class="isDark ? 'text-white' : 'text-slate-800'"
            >{{ member.name }}</h4>
            <p
              class="text-sm transition-colors duration-300"
              :class="isDark ? 'text-gray-400' : 'text-slate-500'"
            >{{ member.role }}</p>
            <div class="flex justify-center space-x-2 mt-3 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
              <span class="w-2 h-2 rounded-full" :class="isDark ? 'bg-gray-600' : 'bg-slate-300'"></span>
              <span class="w-2 h-2 rounded-full" :class="isDark ? 'bg-gray-600' : 'bg-slate-300'"></span>
              <span class="w-2 h-2 rounded-full" :class="isDark ? 'bg-gray-600' : 'bg-slate-300'"></span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 新增：用户评价区域 -->
    <section class="py-20 transition-colors duration-500" :class="isDark ? 'bg-black' : 'bg-slate-50'">
      <div class="container mx-auto px-6">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold mb-4" :class="isDark ? 'text-white' : 'text-slate-800'">用户评价</h2>
          <p class="text-lg max-w-2xl mx-auto" :class="isDark ? 'text-gray-300' : 'text-slate-500'">听听他们怎么说</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div v-for="(testimonial, index) in testimonials" :key="index" class="testimonial-card rounded-2xl p-6 border transition-all duration-500 hover:-translate-y-2 animate-on-scroll"
               :class="[isDark ? 'bg-black border-gray-700' : 'bg-white border-slate-200']"
               :style="{ transitionDelay: `${index * 100}ms` }">
            <div class="flex items-center space-x-1 mb-4">
              <svg v-for="i in 5" :key="i" class="w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>
            <p class="mb-4 italic" :class="isDark ? 'text-gray-300' : 'text-slate-600'">"{{ testimonial.content }}"</p>
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-full flex items-center justify-center" :class="isDark ? 'bg-gray-800' : 'bg-slate-200'">
                <span class="text-lg">{{ testimonial.avatar }}</span>
              </div>
              <div>
                <h4 class="font-semibold text-sm" :class="isDark ? 'text-white' : 'text-slate-800'">{{ testimonial.name }}</h4>
                <p class="text-xs" :class="isDark ? 'text-gray-500' : 'text-slate-400'">{{ testimonial.role }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 新增：常见问题区域 -->
    <section class="py-20 transition-colors duration-500" :class="isDark ? 'bg-black' : 'bg-white'">
      <div class="container mx-auto px-6">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold mb-4" :class="isDark ? 'text-white' : 'text-slate-800'">常见问题</h2>
          <p class="text-lg max-w-2xl mx-auto" :class="isDark ? 'text-gray-300' : 'text-slate-500'">还有疑问？我们为您解答</p>
        </div>

        <div class="max-w-3xl mx-auto space-y-4">
          <div v-for="(faq, index) in faqs" :key="index" class="faq-item border rounded-xl transition-all duration-300 animate-on-scroll cursor-pointer"
               :class="[isDark ? 'border-gray-700 hover:border-gray-500' : 'border-slate-200 hover:shadow-md']"
               :style="{ transitionDelay: `${index * 50}ms` }"
               @click="toggleFaq(index)">
            <div class="flex justify-between items-center p-5">
              <h3 class="font-semibold" :class="isDark ? 'text-white' : 'text-slate-800'">{{ faq.question }}</h3>
              <svg class="w-5 h-5 transition-transform duration-300" :class="[faq.open ? 'rotate-180' : '', isDark ? 'text-gray-400' : 'text-slate-400']" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
              </svg>
            </div>
            <div v-show="faq.open" class="px-5 pb-5">
              <p class="text-sm" :class="isDark ? 'text-gray-400' : 'text-slate-500'">{{ faq.answer }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA 区域 -->
    <section
      class="py-20 transition-colors duration-500"
      :class="isDark ? 'bg-black' : 'bg-slate-800'"
    >
      <div class="container mx-auto px-6 text-center">
        <h2
          class="text-3xl md:text-4xl font-bold mb-4 transition-colors duration-300"
          :class="isDark ? 'text-white' : 'text-white'"
        >准备好开始了吗？</h2>
        <p
          class="text-lg max-w-2xl mx-auto mb-8 transition-colors duration-300"
          :class="isDark ? 'text-gray-300' : 'text-slate-300'"
        >加入数千家创新企业，开启智能工作新体验</p>
        <button
          class="px-8 py-3 rounded-full font-medium hover:scale-105 transition-all duration-300 shadow-lg animate-pulse-hover"
          :class="isDark ? 'bg-gray-100 text-gray-900 hover:bg-white' : 'bg-white text-slate-800 hover:bg-slate-100'"
        >
          免费试用 14 天
        </button>
      </div>
    </section>

    <!-- 页脚 -->
    <footer
      class="py-12 transition-colors duration-500"
      :class="isDark ? 'bg-black text-gray-400' : 'bg-slate-900 text-slate-400'"
    >
      <div class="container mx-auto px-6">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8 mb-8">
          <div>
            <h4 class="font-semibold mb-4 transition-colors duration-300 text-white">产品</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white transition-colors">功能</a></li>
              <li><a href="#" class="hover:text-white transition-colors">定价</a></li>
              <li><a href="#" class="hover:text-white transition-colors">企业版</a></li>
            </ul>
          </div>
          <div>
            <h4 class="font-semibold mb-4 transition-colors duration-300 text-white">支持</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white transition-colors">帮助中心</a></li>
              <li><a href="#" class="hover:text-white transition-colors">文档</a></li>
              <li><a href="#" class="hover:text-white transition-colors">API</a></li>
            </ul>
          </div>
          <div>
            <h4 class="font-semibold mb-4 transition-colors duration-300 text-white">公司</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white transition-colors">关于我们</a></li>
              <li><a href="#" class="hover:text-white transition-colors">博客</a></li>
              <li><a href="#" class="hover:text-white transition-colors">招聘</a></li>
            </ul>
          </div>
          <div>
            <h4 class="font-semibold mb-4 transition-colors duration-300 text-white">法律</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="#" class="hover:text-white transition-colors">隐私政策</a></li>
              <li><a href="#" class="hover:text-white transition-colors">服务条款</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Cookie</a></li>
            </ul>
          </div>
        </div>
        <div
          class="pt-8 border-t mb-8 transition-colors duration-500"
          :class="isDark ? 'border-gray-800' : 'border-slate-800'"
        >
          <p class="text-center text-sm">&copy; 2024 拾光记. All rights reserved.</p>
        </div>
      </div>
    </footer>

    <!-- 返回顶部按钮 -->
    <button
      @click="scrollToTop"
      v-show="showScrollTop"
      class="fixed bottom-8 right-8 w-10 h-10 rounded-full shadow-lg hover:scale-110 transition-all duration-300 flex items-center justify-center z-50 animate-bounce-hover"
      :class="isDark ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-slate-800 text-white hover:bg-slate-700'"
      aria-label="返回顶部"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"/>
      </svg>
    </button>
  </div>
</template>

<style scoped>
/* 基础动画定义 */
@keyframes float-slow {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(30px, -30px) rotate(5deg); }
}

@keyframes float-slower {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-40px, 40px) rotate(-8deg); }
}

@keyframes pulse-slow {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.1); }
}

@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slide-in {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes zoom-in {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes counter-up {
  from { opacity: 0; transform: translateY(20px) scale(0.9); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes pulse-hover {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

@keyframes bounce-hover {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

/* 动画类 */
.animate-float-slow { animation: float-slow 20s ease-in-out infinite; }
.animate-float-slower { animation: float-slower 25s ease-in-out infinite; }
.animate-pulse-slow { animation: pulse-slow 8s ease-in-out infinite; }
.animate-fade-in-up { animation: fade-in-up 0.8s ease-out forwards; opacity: 0; }
.animate-blink { animation: blink 1s step-end infinite; }
.animate-fade-in { animation: fade-in 0.8s ease-out forwards; opacity: 0; }
.animate-slide-in { animation: slide-in 0.8s ease-out forwards; opacity: 0; }
.animate-zoom-in { animation: zoom-in 0.8s ease-out forwards; opacity: 0; }
.animate-slide-up { animation: slide-up 0.8s ease-out forwards; opacity: 0; }
.animate-counter-up { animation: counter-up 0.8s ease-out forwards; opacity: 0; }

/* 交互动画 */
.animate-pulse-hover:hover { animation: pulse-hover 1s ease-in-out infinite; }
.animate-bounce-hover:hover { animation: bounce-hover 0.6s ease-in-out infinite; }

/* 延迟类 */
.animation-delay-200 { animation-delay: 0.2s; }
.animation-delay-400 { animation-delay: 0.4s; }
.animation-delay-600 { animation-delay: 0.6s; }
.animation-delay-800 { animation-delay: 0.8s; }

/* 滚动动画 - 初始状态 */
.animate-on-scroll {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.animate-on-scroll.animate-in {
  opacity: 1;
  transform: translateY(0);
}

/* 卡片悬停效果 */
.feature-card, .case-card, .pricing-card, .testimonial-card {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* FAQ 过渡效果 */
.faq-item {
  transition: all 0.3s ease;
}

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
  scroll-padding-top: 80px;
}

/* 滚动条美化 */
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-track { background: #f1f1f1; }
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover { background: #a8a8a8; }

.dark ::-webkit-scrollbar-track { background: #1f2937; }
.dark ::-webkit-scrollbar-thumb { background: #4b5563; }
.dark ::-webkit-scrollbar-thumb:hover { background: #6b7280; }

/* 媒体查询 */
@media (max-width: 768px) {
  h1 { font-size: 3rem !important; }
}

@media (max-width: 640px) {
  h1 { font-size: 2.5rem !important; }
}
</style>
