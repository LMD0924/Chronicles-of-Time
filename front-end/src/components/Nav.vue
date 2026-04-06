<template>
  <nav class="fixed top-0 left-0 right-0 z-50 transition-all duration-500" :class="[
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
            <div class="absolute inset-0 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl blur-lg opacity-0 group-hover:opacity-50 transition-opacity duration-500"></div>
            <div class="relative w-9 h-9 lg:w-10 lg:h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
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
        <div class="flex items-center justify-center gap-2 rounded-full">
          <template v-for="item in menuItems" :key="item.key">
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
                      ? 'text-gray-300 hover:text-indigo-600 hover:bg-gray-800'
                      : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-100'
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
              <div class="absolute top-full left-0 mt-2 w-48 rounded-xl shadow-xl overflow-hidden z-50"
                   :class="isDark ? 'bg-gray-900 border border-gray-800' : 'bg-white border border-gray-100'"
                   v-show="openSubmenuKey === item.key">
                <div class="py-2">
                  <button
                    v-for="child in item.children"
                    :key="child.key"
                    @click="handleMenuClick(child)"
                    class="w-full px-4 py-2.5 text-left text-sm transition-colors flex items-center gap-3"
                    :class="isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-50'"
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
                    ? 'text-gray-300 hover:text-indigo-600 hover:bg-gray-800'
                    : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-100'
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
        <div class="flex items-center gap-4 user-menu-container relative">
          <!-- 用户信息 -->
          <div class="hidden md:flex items-center gap-2 cursor-pointer group" @click="toggleUserMenu">
            <div class="relative w-9 h-9 rounded-full overflow-hidden border-2 border-indigo-200 group-hover:border-indigo-400 transition-colors">
              <img :src="UserInfo.avatar" alt="User Avatar">
            </div>
            <span :class="[isDark ? 'text-gray-300 group-hover:text-indigo-400' : 'text-gray-700 group-hover:text-indigo-600', 'text-sm font-medium transition-colors']">{{UserInfo.name}}</span>
          </div>

          <!-- 下拉菜单 -->
          <div v-if="showUserMenu" :class="[isDark ? 'bg-gray-900 border-gray-800' : 'bg-white border-gray-100', 'absolute top-full right-0 mt-2 w-48 rounded-lg shadow-xl border overflow-hidden z-50']">
            <div class="py-2">
              <button @click="navigateWithTransition('PersonalProfile')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                <span>👤</span>
                <span>个人档案</span>
              </button>
              <button @click="navigateWithTransition('Resume')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
                <span>👤</span>
                <span>个人简历</span>
              </button>
              <button @click="navigateWithTransition('settings')" :class="[isDark ? 'text-gray-300 hover:bg-gray-800' : 'text-gray-700 hover:bg-gray-100', 'w-full text-left px-4 py-2 text-sm transition-colors flex items-center gap-2']">
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
                : 'text-gray-500 hover:text-indigo-600 hover:bg-gray-100'
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
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ThemeToggleButton from '@/components/ThemeToggleButton.vue'
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

const emit = defineEmits(['menuClick', 'logoClick', 'userClick'])

const route = useRoute()
const router = useRouter()
const isScrolled = ref(false)
const openSubmenuKey = ref(null)
const activeNav = ref('')
const UserInfo = ref({})
const showUserMenu = ref(false)

//获取用户信息
const getUserInfo = () => {
  request.get('/user/getUserById', {}, (message, data) => {
    UserInfo.value = data
  })
}

// 带过渡效果的导航
const navigateWithTransition = (path) => {
  router.push(path)
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
  return item.children.some(child => {
    if (!child.path) return false
    const childPath = child.path.split('?')[0]
    const currentPath = route.path
    return currentPath === childPath || (childPath && currentPath.includes(childPath))
  })
}

// 更新当前激活的菜单项
const updateActiveNav = () => {
  const currentPath = route.path
  const currentQuery = route.query

  for (const item of props.menuItems) {
    // 检查普通菜单项
    if (item.path) {
      const itemPath = item.path.split('?')[0]
      if (currentPath === itemPath) {
        activeNav.value = item.key
        return
      }
    }

    // 检查子菜单项
    if (item.children) {
      for (const child of item.children) {
        if (child.path) {
          const childPath = child.path.split('?')[0]
          if (currentPath === childPath) {
            activeNav.value = item.key
            openSubmenuKey.value = item.key
            return
          }
        }
      }
    }
  }
  activeNav.value = ''
}

// 处理菜单点击
const handleMenuClick = (item) => {
  activeNav.value = item.key
  openSubmenuKey.value = null
  emit('menuClick', item)

  if (item.path) {
    router.push(item.path)
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
  router.push(props.logoPath)
}

// 处理返回首页
const handleBackHome = () => {
  router.push('/home')
}

// 滚动监听
const handleScroll = () => {
  isScrolled.value = window.scrollY > 10
}

onMounted(() => {
  getUserInfo()
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
/* 保持原有样式不变 */
</style>
