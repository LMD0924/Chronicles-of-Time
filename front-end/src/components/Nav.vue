<!--
  文件说明：拾光记前台应用通用组件页面组件，承载通用组件场景的界面展示、交互操作和数据承接。
-->
<template>
  <nav class="app-global-nav fixed top-0 left-0 right-0 z-50 transition-all duration-500" :class="[
    isScrolled
      ? isDark
        ? 'bg-black/95 backdrop-blur-xl border-b border-gray-800'
        : 'bg-white/95 backdrop-blur-xl shadow-lg border-b border-gray-100'
      : 'bg-transparent'
  ]">
    <div class="max-w-[1400px] mx-auto px-6 lg:px-8">
      <div class="flex items-center justify-between h-16 lg:h-20">
        <!-- Logo -->
        <div class="flex items-center gap-3 cursor-pointer group" @click="handleLogoClick">
          <div class="relative">
            <div class="absolute inset-0 bg-gradient-to-r from-brand-500 to-accent-500 rounded-xl blur-lg opacity-0 group-hover:opacity-50 transition-opacity duration-500"></div>
            <div class="relative w-9 h-9 lg:w-10 lg:h-10 bg-gradient-to-br from-brand-500 to-accent-500 rounded-xl flex items-center justify-center shadow-lg">
              <span class="text-xl lg:text-2xl">{{ logoIcon }}</span>
            </div>
          </div>
          <div class="flex items-baseline gap-1">
            <span class="text-xl lg:text-2xl font-bold" :class="isDark ? 'text-white' : 'bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent'">
              {{ logoText }}
            </span>
            <span class="hidden lg:inline text-xs font-medium text-gray-400 tracking-wider">{{ logoSubtext }}</span>
          </div>
        </div>

        <!-- 中间导航菜单 -->
        <div v-if="resolvedMenuItems.length" class="nav-center-menu flex items-center justify-center gap-2">
          <template v-for="item in resolvedMenuItems" :key="item.key">
            <!-- 有子菜单的项 -->
            <div v-if="item.children && item.children.length" class="relative submenu-container" @click.stop>
              <button
                @click="toggleSubmenu(item.key)"
                class="relative px-6 py-2 rounded-full text-base font-medium transition-all duration-300 overflow-hidden group"
                :class="[
                  activeNav === item.key || isChildActive(item)
                    ? isDark
                      ? 'bg-white/30 text-white shadow-lg'
                      : 'bg-white/30 text-gray-700 shadow-lg'
                    : isDark
                      ? 'text-gray-300 hover:text-brand-400 hover:bg-gray-800'
                      : 'text-gray-700 hover:text-brand-600 hover:bg-brand-50'
                ]"
              >
                <span class="relative flex items-center gap-2 z-10">
                  <span class="text-lg">{{ item.icon }}</span>
                  <span>{{ item.label }}</span>
                  <svg class="w-3 h-3 transition-transform" :class="{ 'rotate-180': openSubmenuKey === item.key }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                  </svg>
                </span>
              </button>

              <!-- 下拉子菜单 -->
              <div class="absolute top-full left-0 mt-3 w-52 rounded-2xl shadow-2xl overflow-hidden z-50 backdrop-blur-xl"
                   :class="isDark ? 'bg-gray-900/95 border border-white/10' : 'bg-white/95 border border-brand-100/80'"
                   v-show="openSubmenuKey === item.key">
                <div class="p-1.5">
                  <button
                    v-for="child in item.children"
                    :key="child.key"
                    @click="handleMenuClick(child)"
                    class="w-full px-3.5 py-2.5 text-left text-sm transition-all flex items-center gap-3 rounded-xl"
                    :class="isNavItemActive(child)
                      ? 'bg-gradient-to-r from-brand-500 to-accent-500 text-white shadow-md shadow-brand-500/20'
                      : isDark ? 'text-gray-300 hover:bg-white/10 hover:text-white' : 'text-gray-700 hover:bg-brand-50 hover:text-brand-700'"
                  >
                    <span class="text-base">{{ child.icon }}</span>
                    <span>{{ child.label }}</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- 无子菜单的普通项 -->
            <button
              v-else
              @click="handleMenuClick(item)"
              class="relative px-6 py-2 rounded-full text-base font-medium transition-all duration-300 overflow-hidden group"
              :class="[
                activeNav === item.key
                  ? isDark
                    ? 'bg-white/30 text-white shadow-lg'
                    : 'bg-white/30 text-gray-700 shadow-lg'
                  : isDark
                    ? 'text-gray-300 hover:text-brand-400 hover:bg-gray-800'
                    : 'text-gray-700 hover:text-brand-600 hover:bg-brand-50'
              ]"
            >
              <span class="relative flex items-center gap-2 z-10">
                <span class="text-lg">{{ item.icon }}</span>
                <span>{{ item.label }}</span>
              </span>
            </button>
          </template>
        </div>

        <!-- 右侧区域 -->
        <div class="flex items-center gap-2.5 user-menu-container relative">
          <button
            class="nav-growth-chip"
            type="button"
            title="查看成长等级与每日任务"
            @click="navigateWithTransition('/DailyCheckin')"
          >
            <span class="nav-growth-icon"><Trophy /></span>
            <span class="nav-growth-copy">
              <small>{{ growthSummary.levelName || '初见' }}</small>
              <strong>Lv.{{ growthSummary.level || 1 }}</strong>
            </span>
            <span class="nav-growth-track"><i :style="{ width: `${growthSummary.levelProgress || 0}%` }"></i></span>
          </button>

          <button
            class="nav-icon-action"
            type="button"
            title="在线聊天"
            aria-label="打开在线聊天"
            @click="navigateWithTransition('/Chat')"
          >
            <ChatDotRound />
          </button>

          <!-- 用户信息 -->
          <div class="hidden md:flex items-center gap-2 cursor-pointer group" @click="toggleUserMenu">
            <div class="relative w-9 h-9 rounded-full overflow-hidden border-2 border-brand-200 group-hover:border-brand-400 transition-colors">
              <img :src="UserInfo.avatar" alt="User Avatar">
            </div>
            <span :class="[isDark ? 'text-gray-300 group-hover:text-brand-400' : 'text-gray-700 group-hover:text-brand-600', 'text-sm font-medium transition-colors']">{{UserInfo.name}}</span>
          </div>

          <!-- 下拉菜单 -->
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
              <button @click="navigateWithTransition('/DailyCheckin')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                <span>🌱</span>
                <span>成长等级</span>
              </button>
              <button @click="navigateWithTransition('/Chat')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                <span>💬</span>
                <span>在线聊天</span>
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

          <!-- 返回首页按钮 -->
          <button
            v-if="showBackHome"
            @click="handleBackHome"
            class="shadow-lg h-12 group flex items-center gap-2 px-4 py-1 rounded-full text-sm font-medium transition-all duration-300"
            :class="[
              isDark
                ? 'text-gray-300 hover:text-white hover:bg-gray-500'
                : 'text-gray-500 hover:text-brand-600 hover:bg-brand-50'
            ]"
          >
            <svg class="w-4 h-4 transition-transform group-hover:-translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
            </svg>
            <span>返回首页</span>
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, Trophy } from '@element-plus/icons-vue'
import request from "@/utils/request.js";

const props = defineProps({
  // 主题（由父组件传入）
  isDark: {
    type: Boolean,
    default: false
  },
  // Logo 图标
  logoIcon: {
    type: String,
    default: '⏰'
  },
  // Logo 文字
  logoText: {
    type: String,
    default: '拾光记'
  },
  // Logo 副文字
  logoSubtext: {
    type: String,
    default: '弥补当时那个迷茫的自己'
  },
  // 菜单配置（核心）
  menuItems: {
    type: Array,
    default: () => []
    /*
      菜单配置格式：
      [
        { key: 'home', label: '首页', icon: '🏠', path: '/home' },
        {
          key: 'study',
          label: '学习中心',
          icon: '📚',
          children: [
            { key: 'practice', label: '实战练习', icon: '⚡', path: '/StudyDashboard?tab=practice' },
            { key: 'mistake', label: '错题本', icon: '📖', path: '/StudyDashboard?tab=mistake' }
          ]
        },
        { key: 'volunteer', label: '志愿填报', icon: '🎓', path: '/volunteer' }
      ]
    */
  },
  // 是否显示返回首页按钮
  showBackHome: {
    type: Boolean,
    default: true
  },
  // Logo 点击路径
  logoPath: {
    type: String,
    default: '/home'
  }
})

const archiveMenuItem = { key: 'archive', label: '文章归档', icon: '🗓️', path: '/Archive' }

const includesArchive = (items) => items.some((item) =>
  item.path === '/Archive' || item.route === '/Archive' || item.to === '/Archive' || (item.children && includesArchive(item.children))
)

const resolvedMenuItems = computed(() => includesArchive(props.menuItems)
  ? props.menuItems
  : [...props.menuItems, archiveMenuItem]
)
const emit = defineEmits(['menuClick', 'logoClick', 'userClick'])

const route = useRoute()
const router = useRouter()
const isScrolled = ref(false)
const openSubmenuKey = ref(null)
const activeNav = ref('')
const UserInfo = ref({})
const growthSummary = ref({ level: 1, levelName: '初见', levelProgress: 0 })
const showUserMenu = ref(false)

const normalizePath = (path) => {
  if (!path) return ''
  if (typeof path !== 'string') return path
  if (path.startsWith('/') || path.startsWith('#')) return path
  return `/${path}`
}

const resolveNavTarget = (itemOrPath) => {
  if (!itemOrPath) return null
  if (typeof itemOrPath === 'string') return normalizePath(itemOrPath)

  if (itemOrPath.route) return itemOrPath.route
  if (itemOrPath.to) return typeof itemOrPath.to === 'string' ? normalizePath(itemOrPath.to) : itemOrPath.to
  if (itemOrPath.path) return normalizePath(itemOrPath.path)
  if (itemOrPath.section) return { section: itemOrPath.section }
  return null
}

const routeLocationFromString = (target) => {
  const normalized = normalizePath(target)
  if (!normalized || normalized.startsWith('#')) return normalized

  const [pathWithQuery, hashPart] = normalized.split('#')
  const [path, queryString] = pathWithQuery.split('?')
  const location = { path: path || '/' }

  if (queryString) {
    location.query = Object.fromEntries(new URLSearchParams(queryString))
  }
  if (hashPart) {
    location.hash = `#${hashPart}`
  }

  return location
}

const sameQuery = (expected = {}, actual = {}) => {
  return Object.entries(expected).every(([key, value]) => String(actual[key] ?? '') === String(value))
}

const goToNavTarget = async (target) => {
  if (!target) return

  if (typeof target === 'object' && target.section) {
    emit('menuClick', { section: target.section })
    document.getElementById(target.section)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    return
  }

  const location = typeof target === 'string' ? routeLocationFromString(target) : target
  if (!location) return

  await router.push(location)
}

//获取用户信息
const getUserInfo = () => {
  request.get('/user/getUserById', {}, (message, data) => {
    UserInfo.value = data
  })
}

const getGrowthSummary = async () => {
  try {
    const response = await request.get('/activity/summary')
    growthSummary.value = { ...growthSummary.value, ...(response.data || {}) }
  } catch {
    // 导航仍可使用，成长服务不可用时展示默认等级。
  }
}

// 统一导航入口
const navigateWithTransition = (path) => {
  closeUserMenu()
  goToNavTarget(path)
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
  if (!event.target.closest('.submenu-container')) {
    openSubmenuKey.value = null
  }
}

// 判断子菜单是否激活
const isChildActive = (item) => {
  if (!item.children) return false
  return item.children.some(isNavItemActive)
}

const isNavItemActive = (item) => {
  const target = resolveNavTarget(item)
  if (!target || (typeof target === 'object' && target.section)) return false

  if (typeof target === 'string') {
    const location = routeLocationFromString(target)
    if (typeof location === 'string') return false
    return route.path === location.path && sameQuery(location.query, route.query)
  }

  if (target.name) {
    return route.name === target.name && sameQuery(target.query, route.query)
  }

  if (target.path) {
    return route.path === target.path && sameQuery(target.query, route.query)
  }

  return false
}

// 更新当前激活的菜单项
const updateActiveNav = () => {
  for (const item of resolvedMenuItems.value) {
    if (isNavItemActive(item)) {
      activeNav.value = item.key
      return
    }

    if (isChildActive(item)) {
      activeNav.value = item.key
      return
    }
  }
  activeNav.value = ''
}

// 处理菜单点击
const handleMenuClick = (item) => {
  activeNav.value = item.key
  openSubmenuKey.value = null
  emit('menuClick', item)

  const target = resolveNavTarget(item)
  if (target) {
    goToNavTarget(target)
  } else if (item.handler) {
    item.handler()
  }
}

// 切换子菜单
const toggleSubmenu = (key) => {
  openSubmenuKey.value = openSubmenuKey.value === key ? null : key
}

// 处理 Logo 点击
const handleLogoClick = () => {
  emit('logoClick')
  goToNavTarget(props.logoPath)
}

// 处理返回首页
const handleBackHome = () => {
  goToNavTarget('/home')
}

// 滚动监听
const handleScroll = () => {
  isScrolled.value = window.scrollY > 10
}

onMounted(() => {
  getUserInfo()
  getGrowthSummary()
  window.addEventListener('scroll', handleScroll)
  window.addEventListener('click', handleClickOutside)
  updateActiveNav()
})

watch(() => route.path, () => {
  updateActiveNav()
})

watch(() => route.query, () => {
  updateActiveNav()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.app-global-nav {
  animation: nav-arrive 560ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.nav-growth-chip,
.nav-icon-action {
  position: relative;
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--app-card-solid) 88%, var(--theme-primary) 12%);
  color: var(--app-text-secondary);
  box-shadow: 0 8px 22px -18px rgb(var(--theme-primary-rgb) / 0.72);
  transition: transform 320ms cubic-bezier(0.16, 1, 0.3, 1), border-color 220ms ease, box-shadow 320ms ease;
}

.nav-growth-chip {
  min-width: 118px;
  height: 42px;
  gap: 8px;
  padding: 5px 10px;
  overflow: hidden;
}

.nav-growth-chip:hover,
.nav-icon-action:hover {
  border-color: color-mix(in srgb, var(--theme-primary) 58%, transparent);
  box-shadow: 0 14px 28px -20px rgb(var(--theme-primary-rgb) / 0.9);
  transform: translateY(-2px);
}

.nav-growth-chip:active,
.nav-icon-action:active {
  transform: translateY(0) scale(0.96);
}

.nav-growth-icon {
  display: grid;
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  place-items: center;
  border-radius: 7px;
  background: var(--theme-primary);
  color: white;
}

.nav-growth-icon svg,
.nav-icon-action svg {
  width: 18px;
  height: 18px;
}

.nav-growth-copy {
  display: grid;
  min-width: 44px;
  text-align: left;
}

.nav-growth-copy small {
  color: var(--app-text-muted);
  font-size: 10px;
  line-height: 1.1;
}

.nav-growth-copy strong {
  color: var(--app-text);
  font-size: 12px;
  line-height: 1.3;
}

.nav-growth-track {
  position: absolute;
  right: 8px;
  bottom: 4px;
  left: 46px;
  height: 2px;
  overflow: hidden;
  border-radius: 2px;
  background: rgb(var(--theme-primary-rgb) / 0.14);
}

.nav-growth-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--theme-primary);
  transition: width 720ms cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-icon-action {
  width: 42px;
  height: 42px;
  justify-content: center;
}

.nav-icon-action:hover svg {
  animation: friendly-wiggle 520ms cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes nav-arrive {
  from { opacity: 0; transform: translateY(-12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes friendly-wiggle {
  0%, 100% { transform: rotate(0deg) scale(1); }
  35% { transform: rotate(-9deg) scale(1.08); }
  70% { transform: rotate(7deg) scale(1.04); }
}

@media (max-width: 1180px) {
  .nav-growth-copy,
  .nav-growth-track {
    display: none;
  }

  .nav-growth-chip {
    min-width: 42px;
    width: 42px;
    justify-content: center;
    padding: 5px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .app-global-nav,
  .nav-icon-action:hover svg {
    animation: none;
  }
}
</style>
