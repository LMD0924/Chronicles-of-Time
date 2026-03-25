<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isScrolled = ref(false)
const showBackTop = ref(false)
const activeNav = ref('home')
const activeStage = ref(0)
const showUserMenu = ref(false)

// 动画统计数据
const animatedStats = ref({
  users: 0,
  moments: 0,
  days: 0
})

// 滚动动画观察器
const observedElements = ref([])

// 时光轴节点预览
const timelineNodes = [
  { year: '2019', event: '高中入学' },
  { year: '2022', event: '高考冲刺' },
  { year: '2023', event: '大学新生' },
  { year: '2025', event: '实习入职' }
]

// 人生阶段
const lifeStages = [
  { icon: '📚', name: '高中时代', years: '15-18岁' },
  { icon: '🎓', name: '大学时光', years: '18-22岁' },
  { icon: '💼', name: '职场新人', years: '22-25岁' },
  { icon: '🚀', name: '进阶之路', years: '25岁+' }
]

// 时光轴数据
const timelineData = [
  {
    date: '2022年6月',
    stage: '高中',
    stageClass: 'highschool',
    title: '高考 · 最后一课',
    description: '黑板上写着"高考必胜"，同学们互相在衣服上签名。三年时光，在这个夏天画上句号。',
    tags: ['高考', '毕业', '青春']
  },
  {
    date: '2022年9月',
    stage: '大学',
    stageClass: 'university',
    title: '大学开学第一课',
    description: '拖着行李箱走进校门，对一切都充满好奇。第一次离开家，既紧张又兴奋。',
    tags: ['新生', '开学', '大学']
  },
  {
    date: '2023年12月',
    stage: '大学',
    stageClass: 'university',
    title: '第一次获奖',
    description: '参加校园设计大赛获得一等奖，站在领奖台上的那一刻，觉得所有努力都值得。',
    tags: ['成长', '获奖', '突破']
  },
  {
    date: '2024年7月',
    stage: '职场',
    stageClass: 'work',
    title: '第一份实习offer',
    description: '经过三轮面试，终于拿到心仪的实习offer。职场第一步，从这里开始。',
    tags: ['实习', '职场', '新起点']
  },
  {
    date: '2025年3月',
    stage: '职场',
    stageClass: 'work',
    title: '独立完成项目',
    description: '第一次独立负责项目，从策划到执行全程主导。项目上线那天，成就感满满。',
    tags: ['成长', '独立', '项目']
  }
]

// 里程碑数据
const milestones = [
  { icon: '📖', year: '2019', title: '考上重点高中', desc: '中考全市前100名', progress: 100, status: '已达成' },
  { icon: '🎓', year: '2022', title: '高考上岸', desc: '考入理想大学', progress: 100, status: '已达成' },
  { icon: '🏅', year: '2024', title: '英语六级', desc: '目标600分', progress: 85, status: '进行中' },
  { icon: '💼', year: '2025', title: '秋招offer', desc: '拿到大厂offer', progress: 60, status: '进行中' },
  { icon: '🚀', year: '2026', title: '职场晋升', desc: '成为项目负责人', progress: 30, status: '规划中' }
]

// 相册数据
const galleryPhotos = [
  { icon: '🏫', title: '高中毕业照', date: '2022年6月' },
  { icon: '🎓', title: '大学迎新晚会', date: '2022年9月' },
  { icon: '🏆', title: '比赛获奖', date: '2023年12月' },
  { icon: '💼', title: '实习第一天', date: '2024年7月' },
  { icon: '✈️', title: '第一次出差', date: '2025年3月' },
  { icon: '🎉', title: '团队聚餐', date: '2025年8月' }
]

// 日记数据
const journalEntries = [
  { day: '15', month: '3月', title: '今天学会了新技能', excerpt: '花了整整一天时间，终于把数据分析的基础搞懂了...', likes: 23, comments: 5 },
  { day: '20', month: '3月', title: '和老朋友的重逢', excerpt: '高中同学来出差，一起吃了顿饭，聊了很多过去的事...', likes: 45, comments: 12 },
  { day: '25', month: '3月', title: '第一次主持会议', excerpt: '紧张到手心出汗，但最终还是顺利完成了。原来我也可以...', likes: 67, comments: 23 }
]

// 每日寄语
const quotes = [
  { text: '种一棵树最好的时间是十年前，其次是现在。', author: '佚名' },
  { text: '那些你熬夜努力的时光，终会化作照亮前路的光。', author: '拾光记' },
  { text: '成长不是变得复杂，而是学会在复杂中保持简单。', author: '佚名' },
  { text: '每一个优秀的人都有一段沉默的时光，那段时间是付出了很多努力，却得不到结果的日子，我们把它叫做扎根。', author: '佚名' }
]

const dailyQuote = ref(quotes[0])

// 刷新寄语
const refreshQuote = () => {
  const randomIndex = Math.floor(Math.random() * quotes.length)
  dailyQuote.value = quotes[randomIndex]
}

// 数字滚动动画
const animateNumbers = () => {
  const targets = [50000, 120000, 365]
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

  // 更新导航高亮 - 根据滚动位置
  const sections = ['home', 'timeline', 'milestone', 'gallery', 'journal']
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
const scrollToSection = (sectionId) => {
  const element = document.getElementById(sectionId)
  if (element) {
    const offset = 80
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset
    window.scrollTo({ top: offsetPosition, behavior: 'smooth' })
  }
  activeNav.value = sectionId
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 查看更多
const viewMore = () => {
  router.push('/gallery')
}

// 切换用户菜单
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

// 关闭用户菜单
const closeUserMenu = () => {
  showUserMenu.value = false
}

// 处理菜单项点击
const handleMenuItemClick = (action) => {
  closeUserMenu()
  switch (action) {
    case 'profile':
      router.push('/PersonalProfile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      // 这里可以添加退出登录的逻辑
      console.log('退出登录')
      break
  }
}

// 点击页面其他地方关闭菜单
const handleClickOutside = (event) => {
  const userMenu = document.querySelector('.user-menu-container')
  if (userMenu && !userMenu.contains(event.target)) {
    closeUserMenu()
  }
}

// 初始化滚动动画观察器
const initScrollAnimation = () => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animated')
        // 可以取消观察，避免重复动画
        observer.unobserve(entry.target)
      }
    })
  }, {
    threshold: 0.2,  // 元素 20% 可见时触发
    rootMargin: '0px 0px -50px 0px'
  })

  // 观察所有需要动画的元素
  const animatedElements = document.querySelectorAll('.scroll-animate')
  animatedElements.forEach(el => observer.observe(el))
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  window.addEventListener('click', handleClickOutside)
  animateNumbers()
  // 延迟初始化滚动动画，确保 DOM 已渲染
  setTimeout(initScrollAnimation, 100)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 min-h-screen overflow-x-hidden">
    <!-- 高级导航栏 - 移除登录按钮 -->
    <nav class="fixed top-0 left-0 right-0 z-50 transition-all duration-500" :class="[
      isScrolled
        ? 'bg-white/95 backdrop-blur-xl shadow-lg border-b border-gray-100'
        : 'bg-transparent'
    ]">
      <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
        <div class="flex items-center justify-between h-16 lg:h-20">
          <!-- Logo -->
          <div class="flex items-center gap-3 cursor-pointer group" @click="scrollToSection('home')">
            <div class="relative">
              <div class="absolute inset-0 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl blur-lg opacity-0 group-hover:opacity-50 transition-opacity duration-500"></div>
              <div class="relative w-9 h-9 lg:w-10 lg:h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
                <span class="text-xl lg:text-2xl">⏰</span>
              </div>
            </div>
            <div class="flex items-baseline gap-1">
              <span class="text-xl lg:text-2xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">拾光记</span>
              <span class="hidden lg:inline text-xs font-medium text-gray-400 tracking-wider">Chronicles</span>
            </div>
          </div>

          <!-- 导航菜单 - 桌面端（居中） -->
          <div class="flex-1 flex justify-center">
            <div class="hidden md:flex items-center gap-1 bg-gray-50/80 backdrop-blur-sm rounded-full p-1 shadow-sm">
              <button
                v-for="item in [
                  { id: 'home', name: '首页', icon: '🏠' },
                  { id: 'timeline', name: '时光轴', icon: '⏳' },
                  { id: 'milestone', name: '里程碑', icon: '🏆' },
                  { id: 'gallery', name: '回忆相册', icon: '📸' },
                  { id: 'journal', name: '成长日记', icon: '📖' }
                ]"
                :key="item.id"
                @click="scrollToSection(item.id)"
                class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group"
                :class="[
                  activeNav === item.id
                    ? 'text-black shadow-lg'
                    : 'text-gray-600 hover:text-indigo-600'
                ]"
              >
                <span v-if="activeNav === item.id" class="absolute inset-0 bg-gradient-to-r rounded-full shadow-md"></span>
                <span class="relative flex items-center gap-2 z-10">
                  <span class="text-base">{{ item.icon }}</span>
                  <span>{{ item.name }}</span>
                </span>
              </button>
            </div>
          </div>

          <!-- 用户信息（右侧） -->
          <div class="flex items-center gap-4 user-menu-container">
            <!-- 用户名和头像 -->
            <div class="hidden md:flex items-center gap-2 cursor-pointer group" @click="toggleUserMenu">
              <div class="relative w-9 h-9 rounded-full overflow-hidden border-2 border-indigo-200 group-hover:border-indigo-400 transition-colors">
                <div class="w-full h-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-white font-medium">
                  用
                </div>
              </div>
              <span class="text-sm font-medium text-gray-700 group-hover:text-indigo-600 transition-colors">用户名</span>
            </div>

            <!-- 下拉菜单 -->
            <div v-if="showUserMenu" class="absolute top-full right-6 mt-2 w-48 bg-white rounded-lg shadow-xl border border-gray-100 overflow-hidden z-50">
              <div class="py-2">
                <button @click="handleMenuItemClick('profile')" class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2">
                  <span>👤</span>
                  <span>个人档案</span>
                </button>
                <button @click="handleMenuItemClick('settings')" class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2">
                  <span>⚙️</span>
                  <span>设置</span>
                </button>
                <div class="border-t border-gray-100 my-1"></div>
                <button @click="handleMenuItemClick('logout')" class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 transition-colors flex items-center gap-2">
                  <span>🚪</span>
                  <span>退出登录</span>
                </button>
              </div>
            </div>

            <!-- 移动端菜单按钮（仅移动端显示） -->
            <button class="md:hidden w-9 h-9 rounded-lg bg-gray-100 text-gray-600 flex items-center justify-center">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Hero 区域 -->
    <section id="home" class="relative min-h-screen flex items-center pt-20 overflow-hidden">
      <!-- 背景光晕 -->
      <div class="absolute inset-0 z-0">
        <div class="absolute w-[400px] h-[400px] bg-indigo-400/20 rounded-full blur-[80px] -top-20 -left-20 animate-float"></div>
        <div class="absolute w-[500px] h-[500px] bg-purple-400/15 rounded-full blur-[80px] -bottom-32 -right-32 animate-float animation-delay-2000"></div>
        <div class="absolute w-[300px] h-[300px] bg-pink-400/10 rounded-full blur-[80px] top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 animate-float animation-delay-5000"></div>
      </div>

      <div class="relative z-10 max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="grid lg:grid-cols-2 gap-12 items-center">
          <!-- 左侧内容 -->
          <div class="scroll-animate">
            <div class="inline-flex items-center gap-2 px-4 py-2 bg-indigo-50 rounded-full text-indigo-600 text-sm mb-6">
              <span>✨</span>
              <span>从高中到职场，记录每一步成长</span>
            </div>
            <h1 class="text-4xl lg:text-6xl font-bold text-gray-900 leading-tight mb-5">
              拾光记 · 成长时光机
            </h1>
            <p class="text-gray-600 text-base lg:text-lg leading-relaxed mb-8">
              记录高中奋斗的日夜，珍藏大学青春的瞬间，见证职场蜕变的每一步<br>
              让时间有迹可循，让成长值得回味
            </p>
            <div class="flex gap-8 mb-8">
              <div>
                <div class="text-3xl font-bold text-gray-900">{{ animatedStats.users }}+</div>
                <div class="text-sm text-gray-500">成长记录者</div>
              </div>
              <div>
                <div class="text-3xl font-bold text-gray-900">{{ animatedStats.moments }}+</div>
                <div class="text-sm text-gray-500">珍藏瞬间</div>
              </div>
              <div>
                <div class="text-3xl font-bold text-gray-900">{{ animatedStats.days }}+</div>
                <div class="text-sm text-gray-500">陪伴天数</div>
              </div>
            </div>
            <div class="flex gap-4">
              <button @click="$router.push('/register')" class="px-8 py-3 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-full font-medium shadow-lg hover:shadow-xl transition-all hover:-translate-y-0.5">
                开始记录时光
              </button>
              <button @click="scrollToSection('timeline')" class="px-8 py-3 border border-gray-300 text-gray-700 rounded-full font-medium hover:border-indigo-400 hover:text-indigo-600 transition-all">
                浏览时光故事
              </button>
            </div>
          </div>

          <!-- 右侧预览 -->
          <div class="bg-white/60 backdrop-blur-xl rounded-2xl p-8 border border-white/80 shadow-xl scroll-animate">
            <div class="relative pl-8 min-h-[280px]">
              <div class="absolute left-2 top-0 bottom-0 w-0.5 bg-gradient-to-b from-indigo-500 to-purple-500"></div>
              <div v-for="(node, idx) in timelineNodes" :key="idx" class="absolute flex items-center gap-3" :style="{ top: `${idx * 25}%` }">
                <div class="w-3 h-3 bg-indigo-500 rounded-full border-2 border-white shadow-[0_0_0_3px_rgba(59,130,246,0.2)]"></div>
                <div class="text-sm font-medium text-gray-700">{{ node.year }} · {{ node.event }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 人生阶段导航 -->
    <section class="py-16 bg-white scroll-animate">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="flex justify-center gap-5 flex-wrap">
          <div v-for="(stage, idx) in lifeStages" :key="idx"
               class="px-8 py-5 bg-gray-50 rounded-2xl text-center cursor-pointer transition-all border border-gray-200 min-w-[140px] hover:border-indigo-400 hover:shadow-lg hover:-translate-y-1"
               :class="{ 'border-indigo-400 shadow-lg -translate-y-1 bg-white': activeStage === idx }"
               @click="activeStage = idx">
            <div class="text-3xl mb-2">{{ stage.icon }}</div>
            <div class="font-semibold text-gray-800 mb-1">{{ stage.name }}</div>
            <div class="text-xs text-gray-500">{{ stage.years }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 时光轴区域 -->
    <section id="timeline" class="py-20 bg-gray-50">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl lg:text-4xl font-bold text-gray-900 mb-3">时光轴 · 成长轨迹</h2>
          <p class="text-gray-500">记录每一个值得铭记的瞬间</p>
        </div>
        <div class="relative max-w-3xl mx-auto">
          <div v-for="(item, idx) in timelineData" :key="idx"
               class="relative mb-12 flex justify-start scroll-animate"
               :class="{ 'justify-end': idx % 2 === 1 }"
               :style="{ transitionDelay: `${idx * 0.1}s` }">
            <div class="absolute left-1/2 -translate-x-1/2 w-10 flex flex-col items-center">
              <div class="w-4 h-4 bg-indigo-500 rounded-full border-2 border-white shadow-[0_0_0_4px_rgba(59,130,246,0.2)] z-10"></div>
              <div class="w-0.5 h-16 bg-gray-300 mt-2" v-if="idx !== timelineData.length - 1"></div>
            </div>
            <div class="w-[calc(50%-50px)] bg-white rounded-2xl p-5 shadow-md hover:shadow-xl transition-all hover:-translate-y-1" :class="{ 'ml-auto': idx % 2 === 1 }">
              <div class="text-xs text-gray-500 mb-2">{{ item.date }}</div>
              <div class="inline-block px-2 py-1 rounded-full text-xs font-medium mb-3" :class="{
                'bg-amber-100 text-amber-700': item.stage === '高中',
                'bg-blue-100 text-blue-700': item.stage === '大学',
                'bg-green-100 text-green-700': item.stage === '职场'
              }">{{ item.stage }}</div>
              <h3 class="text-lg font-semibold text-gray-800 mb-2">{{ item.title }}</h3>
              <p class="text-sm text-gray-600 leading-relaxed mb-3">{{ item.description }}</p>
              <div class="flex gap-2 flex-wrap">
                <span v-for="tag in item.tags" :key="tag" class="px-2 py-1 bg-gray-100 rounded-full text-xs text-gray-600">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 里程碑区域 -->
    <section id="milestone" class="py-20 bg-white">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl lg:text-4xl font-bold text-gray-900 mb-3">🏆 人生里程碑</h2>
          <p class="text-gray-500">每一个里程碑，都是成长的勋章</p>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
          <div v-for="(item, idx) in milestones" :key="idx"
               class="bg-gray-50 rounded-2xl p-6 text-center transition-all hover:-translate-y-2 hover:shadow-xl scroll-animate"
               :style="{ transitionDelay: `${idx * 0.05}s` }">
            <div class="text-4xl mb-3">{{ item.icon }}</div>
            <div class="text-xs text-indigo-500 font-medium mb-2">{{ item.year }}</div>
            <h3 class="font-semibold text-gray-800 mb-2">{{ item.title }}</h3>
            <p class="text-xs text-gray-500 mb-4">{{ item.desc }}</p>
            <div class="h-1 bg-gray-200 rounded-full overflow-hidden mb-2">
              <div class="h-full bg-indigo-500 rounded-full transition-all" :style="{ width: item.progress + '%' }"></div>
            </div>
            <span class="text-xs text-gray-400">{{ item.status }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 成长数据统计 -->
    <section class="py-16 bg-gray-50">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
          <div v-for="(stat, idx) in [
            { icon: '📚', number: '2,847', label: '学习时长（小时）', trend: '↑ 较上周 +12%', trendColor: 'text-green-500' },
            { icon: '✍️', number: '156', label: '时光笔记', trend: '累计 3.2万字', trendColor: 'text-gray-400' },
            { icon: '📸', number: '342', label: '回忆照片', trend: '新增 12张本周', trendColor: 'text-green-500' },
            { icon: '🎯', number: '23', label: '目标达成', trend: '完成率 85%', trendColor: 'text-green-500' }
          ]" :key="idx"
               class="bg-white rounded-2xl p-7 text-center hover:-translate-y-1 transition-all shadow-sm hover:shadow-lg scroll-animate"
               :style="{ transitionDelay: `${idx * 0.1}s` }">
            <div class="text-4xl mb-3">{{ stat.icon }}</div>
            <div class="text-3xl font-bold text-gray-800 mb-2">{{ stat.number }}</div>
            <div class="text-sm text-gray-500 mb-2">{{ stat.label }}</div>
            <div class="text-xs" :class="stat.trendColor">{{ stat.trend }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 回忆相册 -->
    <section id="gallery" class="py-20 bg-white">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl lg:text-4xl font-bold text-gray-900 mb-3">📸 回忆相册</h2>
          <p class="text-gray-500">用影像定格时光，让回忆触手可及</p>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          <div v-for="(photo, idx) in galleryPhotos" :key="idx"
               class="bg-gray-50 rounded-xl overflow-hidden transition-all hover:-translate-y-2 hover:shadow-xl cursor-pointer scroll-animate"
               :style="{ transitionDelay: `${idx * 0.05}s` }">
            <div class="h-40 bg-gradient-to-br from-gray-200 to-gray-300 flex items-center justify-center text-5xl">{{ photo.icon }}</div>
            <div class="p-4">
              <h4 class="font-semibold text-gray-800 mb-1">{{ photo.title }}</h4>
              <p class="text-xs text-gray-500">{{ photo.date }}</p>
            </div>
          </div>
        </div>
        <div class="text-center mt-10 scroll-animate">
          <button @click="viewMore" class="px-6 py-2 border border-gray-300 text-gray-600 rounded-full hover:border-indigo-400 hover:text-indigo-500 transition-all">查看全部相册 →</button>
        </div>
      </div>
    </section>

    <!-- 成长日记 -->
    <section id="journal" class="py-20 bg-gray-50">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="text-center mb-12 scroll-animate">
          <h2 class="text-3xl lg:text-4xl font-bold text-gray-900 mb-3">📖 成长日记</h2>
          <p class="text-gray-500">记录日常感悟，见证点滴成长</p>
        </div>
        <div class="max-w-3xl mx-auto space-y-5">
          <div v-for="(entry, idx) in journalEntries" :key="idx"
               class="bg-white rounded-2xl p-6 flex gap-5 transition-all hover:translate-x-1 hover:shadow-lg scroll-animate"
               :style="{ transitionDelay: `${idx * 0.1}s` }">
            <div class="text-center min-w-[60px]">
              <div class="text-3xl font-bold text-indigo-500 leading-none">{{ entry.day }}</div>
              <div class="text-xs text-gray-400 mt-1">{{ entry.month }}</div>
            </div>
            <div class="flex-1">
              <h3 class="font-semibold text-gray-800 mb-2">{{ entry.title }}</h3>
              <p class="text-sm text-gray-600 leading-relaxed mb-3">{{ entry.excerpt }}</p>
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
    <section class="py-20 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="bg-white/95 rounded-3xl p-12 text-center backdrop-blur-sm scroll-animate">
          <div class="text-5xl text-purple-400 opacity-50 mb-4">“</div>
          <p class="text-xl lg:text-2xl text-gray-800 leading-relaxed max-w-2xl mx-auto mb-5 italic">{{ dailyQuote.text }}</p>
          <p class="text-gray-500 mb-5">—— {{ dailyQuote.author }}</p>
          <button @click="refreshQuote" class="inline-flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-full text-sm text-gray-600 hover:bg-gray-200 transition-all">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
            </svg>
            换一句
          </button>
        </div>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="bg-gray-900 text-gray-400 py-16">
      <div class="max-w-[1200px] mx-auto px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10 mb-10">
          <div>
            <div class="flex items-center gap-2 mb-3">
              <span class="text-2xl">⏰</span>
              <span class="text-xl font-semibold text-white">拾光记</span>
            </div>
            <p class="text-sm mb-4">记录每一段值得珍藏的时光</p>
            <div class="flex gap-4 text-xl">
              <span>📧</span><span>💬</span><span>📱</span>
            </div>
          </div>
          <div>
            <h4 class="text-white font-semibold mb-4">关于我们</h4>
            <ul class="space-y-2 text-sm">
              <li class="hover:text-white cursor-pointer">品牌故事</li>
              <li class="hover:text-white cursor-pointer">加入我们</li>
              <li class="hover:text-white cursor-pointer">联系我们</li>
            </ul>
          </div>
          <div>
            <h4 class="text-white font-semibold mb-4">帮助中心</h4>
            <ul class="space-y-2 text-sm">
              <li class="hover:text-white cursor-pointer">使用指南</li>
              <li class="hover:text-white cursor-pointer">隐私政策</li>
              <li class="hover:text-white cursor-pointer">服务条款</li>
            </ul>
          </div>
          <div>
            <h4 class="text-white font-semibold mb-4">关注我们</h4>
            <ul class="space-y-2 text-sm">
              <li class="hover:text-white cursor-pointer">微信公众号</li>
              <li class="hover:text-white cursor-pointer">小红书</li>
              <li class="hover:text-white cursor-pointer">微博</li>
            </ul>
          </div>
        </div>
        <div class="text-center pt-8 border-t border-gray-800 text-xs">
          <p>© 2026 拾光记 Chronicles of Time · 让成长有迹可循</p>
        </div>
      </div>
    </footer>

    <!-- 返回顶部 -->
    <button v-show="showBackTop" @click="scrollToTop" class="fixed bottom-10 right-10 w-11 h-11 bg-white rounded-full shadow-lg flex items-center justify-center hover:-translate-y-1 transition-all z-50">
      <svg class="w-5 h-5 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19V5M5 12l7-7 7 7"></path>
      </svg>
    </button>
  </div>
</template>

<style scoped>
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.1); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

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

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-float {
  animation: float 20s ease-in-out infinite;
}

.animation-delay-200 {
  animation-delay: 0.2s;
}

.animation-delay-400 {
  animation-delay: 0.4s;
}

.animation-delay-600 {
  animation-delay: 0.6s;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-5000 {
  animation-delay: 5s;
}

/* 滚动动画样式 */
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
