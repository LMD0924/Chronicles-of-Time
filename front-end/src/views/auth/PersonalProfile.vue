<script setup>
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(true)
const userInfo = ref(null)
const activeTab = ref('overview')
const isEditing = ref(false)
const editForm = ref({
  name: '',
  email: '',
  phone: '',
  bio: ''
})
const isNavScrolled = ref(false)

// 统计数据
const stats = ref({
  totalDays: 365,
  totalMoments: 128,
  totalJournals: 86,
  totalPhotos: 234,
  totalLikes: 1245,
  totalComments: 389
})

// 成就徽章
const badges = ref([
  { name: '时光记录者', icon: '📝', desc: '记录第一篇日记', earned: true, date: '2024-01-15' },
  { name: '摄影达人', icon: '📸', desc: '上传10张照片', earned: true, date: '2024-02-20' },
  { name: '连续打卡', icon: '🔥', desc: '连续7天登录', earned: true, date: '2024-03-10' },
  { name: '成长见证者', icon: '🌟', desc: '完成第一个里程碑', earned: true, date: '2024-04-05' },
  { name: '故事收藏家', icon: '📖', desc: '收藏50篇故事', earned: false, progress: 68 },
  { name: '社交达人', icon: '💬', desc: '获得100个点赞', earned: false, progress: 45 },
  { name: '百日记', icon: '📅', desc: '连续记录100天', earned: false, progress: 32 },
  { name: '时光旅行者', icon: '✈️', desc: '探索10个城市', earned: false, progress: 20 }
])

// 成长里程碑
const milestones = ref([
  { title: '完成新手任务', date: '2024-01-10', icon: '🎯', type: 'achievement' },
  { title: '发布第一篇日记', date: '2024-01-15', icon: '📝', type: 'achievement' },
  { title: '上传第一张照片', date: '2024-01-20', icon: '📸', type: 'achievement' },
  { title: '获得第一个粉丝', date: '2024-02-01', icon: '❤️', type: 'social' },
  { title: '完成第一个里程碑', date: '2024-02-15', icon: '🏆', type: 'achievement' },
  { title: '连续登录30天', date: '2024-03-01', icon: '🔥', type: 'streak' },
  { title: '收获100个点赞', date: '2024-03-20', icon: '👍', type: 'social' },
  { title: '成为VIP会员', date: '2024-04-01', icon: '💎', type: 'premium' }
])

// 最近动态
const activities = ref([
  { type: 'journal', title: '发表了新日记', content: '今天学会了新技能...', time: '2小时前', icon: '📝' },
  { type: 'photo', title: '上传了新照片', content: '实习第一天的纪念照', time: '昨天', icon: '📸' },
  { type: 'milestone', title: '达成了新成就', content: '连续打卡7天', time: '3天前', icon: '🏆' },
  { type: 'comment', title: '收到了评论', content: '用户"小明"评论了你的日记', time: '5天前', icon: '💬' },
  { type: 'like', title: '获得了点赞', content: '你的日记获得了10个点赞', time: '1周前', icon: '❤️' }
])

// 年度数据
const yearlyData = ref({
  year: 2026,
  months: [
    { name: '1月', journals: 12, photos: 8, likes: 45 },
    { name: '2月', journals: 15, photos: 12, likes: 67 },
    { name: '3月', journals: 18, photos: 15, likes: 89 },
    { name: '4月', journals: 10, photos: 6, likes: 34 },
    { name: '5月', journals: 0, photos: 0, likes: 0 },
    { name: '6月', journals: 0, photos: 0, likes: 0 },
    { name: '7月', journals: 0, photos: 0, likes: 0 },
    { name: '8月', journals: 0, photos: 0, likes: 0 },
    { name: '9月', journals: 0, photos: 0, likes: 0 },
    { name: '10月', journals: 0, photos: 0, likes: 0 },
    { name: '11月', journals: 0, photos: 0, likes: 0 },
    { name: '12月', journals: 0, photos: 0, likes: 0 }
  ]
})

// 计算总成就进度
const totalProgress = computed(() => {
  const earned = badges.value.filter(b => b.earned).length
  const total = badges.value.length
  return Math.round((earned / total) * 100)
})

// 计算加入天数
const joinDays = computed(() => {
  if (!userInfo.value?.joinDate) return 0
  const join = new Date(userInfo.value.joinDate)
  const now = new Date()
  const diffTime = Math.abs(now - join)
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
})

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    await request.get('/user/getUserById',{},(msg,data)=>{
      userInfo.value = data
      console.log("用户信息：", userInfo.value)
    })
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
    // 延迟执行入场动画
    setTimeout(() => {
      document.querySelectorAll('.fade-in-up').forEach(el => {
        el.classList.add('show')
      })
    }, 100)
  }
}

// 编辑资料
const startEdit = () => {
  editForm.value = {
    name: userInfo.value.name || '',
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || '',
    bio: userInfo.value.bio || ''
  }
  isEditing.value = true
}

// 保存编辑
const saveEdit = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    userInfo.value = { ...userInfo.value, ...editForm.value }
    localStorage.setItem('user_info', JSON.stringify(userInfo.value))
    ElMessage.success('资料更新成功')
    isEditing.value = false
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
}

// 登出
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    sessionStorage.removeItem('token')
    localStorage.removeItem('token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_info')
    ElMessage.success('已退出登录')
    router.push('/')
  }).catch(() => {})
}

// 导航栏滚动效果
const handleScroll = () => {
  isNavScrolled.value = window.scrollY > 10
}

onMounted(() => {
  fetchUserInfo()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 via-white to-indigo-50/30">
    <!-- 顶部导航 - 移除fade-in-up类，添加自定义动画 -->
    <nav
      class="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-gray-100 transition-all duration-300 nav-animation"
      :class="{ 'nav-scrolled': isNavScrolled }"
    >
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center gap-3 cursor-pointer group" @click="$router.push('/home')">
            <div class="w-8 h-8 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center shadow-md group-hover:shadow-lg transition-all group-hover:scale-110 group-hover:rotate-12">
              <span class="text-xl">⏰</span>
            </div>
            <span class="text-xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">拾光记</span>
          </div>
          <div class="flex items-center gap-3">
            <button @click="$router.push('/home')" class="px-4 py-2 text-gray-600 hover:text-indigo-600 hover:shadow-lg transition-all hover:bg-indigo-50 rounded-lg relative overflow-hidden group">
              <span class="relative z-10">返回首页</span>
              <div class="absolute inset-0 bg-indigo-50 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 origin-left"></div>
            </button>
            <button @click="handleLogout" class="px-4 py-2 text-red-600 hover:bg-red-50 rounded-lg transition-all relative overflow-hidden group hover:shadow-lg">
              <span class="relative z-10">退出登录</span>
              <div class="absolute inset-0 bg-red-50 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 origin-left"></div>
            </button>
          </div>
        </div>
      </div>
    </nav>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center items-center h-96">
        <div class="text-center">
          <div class="relative">
            <div class="w-16 h-16 border-4 border-indigo-200 border-t-indigo-500 rounded-full animate-spin mx-auto mb-4"></div>
            <div class="absolute inset-0 flex items-center justify-center">
              <div class="w-8 h-8 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-full animate-ping opacity-75"></div>
            </div>
          </div>
          <p class="text-gray-500 mt-4 animate-pulse">加载中...</p>
        </div>
      </div>

      <div v-else>
        <!-- 个人资料卡片 -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden mb-8 fade-in-up">
          <div class="relative h-32 bg-gradient-to-r from-indigo-500 to-purple-600">
            <div class="absolute inset-0 bg-black/20"></div>
            <!-- 装饰粒子 -->
            <div class="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-white to-transparent"></div>
          </div>
          <div class="relative px-6 pb-6">
            <div class="flex flex-col md:flex-row items-start md:items-end -mt-12 mb-6">
              <div class="relative group avatar-container">
                <div class="w-24 h-24 bg-gradient-to-br from-indigo-100 to-purple-100 rounded-2xl flex items-center justify-center text-5xl shadow-lg border-4 border-white transition-all group-hover:scale-105 group-hover:rotate-6">
                  {{ userInfo?.avatar || '👤' }}
                </div>
                <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full border-2 border-white animate-pulse"></div>
              </div>
              <div class="flex-1 md:ml-6 mt-4 md:mt-0">
                <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <h1 class="text-2xl font-bold text-gray-800">{{ userInfo?.name || userInfo?.username }}</h1>
                    <p class="text-gray-500 mt-1">@{{ userInfo?.username }}</p>
                    <p class="text-sm text-gray-400 mt-2 flex items-center gap-1">
                      <svg class="w-4 h-4 animate-bounce" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                      加入拾光记 {{ joinDays }} 天
                    </p>
                  </div>
                  <button v-if="!isEditing" @click="startEdit" class="px-5 py-2 border border-gray-300 rounded-full text-gray-600 hover:border-indigo-400 hover:text-indigo-500 hover:shadow-md transition-all duration-300 hover:scale-105">
                    编辑资料
                  </button>
                  <div v-else class="flex gap-2">
                    <button @click="cancelEdit" class="px-5 py-2 border border-gray-300 rounded-full text-gray-600 hover:bg-gray-50 transition-all hover:scale-105">取消</button>
                    <button @click="saveEdit" class="px-5 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-full hover:shadow-lg transition-all hover:scale-105 relative overflow-hidden group">
                      <span class="relative z-10">保存</span>
                      <div class="absolute inset-0 bg-gradient-to-r from-indigo-600 to-purple-700 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 origin-left"></div>
                    </button>
                  </div>
                </div>

                <!-- 编辑模式 -->
                <div v-if="isEditing" class="mt-4 space-y-3 edit-form">
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">昵称</label>
                      <input v-model="editForm.name" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all">
                    </div>
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">邮箱</label>
                      <input v-model="editForm.email" type="email" class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all">
                    </div>
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">手机号</label>
                      <input v-model="editForm.phone" type="tel" class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all">
                    </div>
                    <div class="md:col-span-2">
                      <label class="block text-xs text-gray-500 mb-1">个人简介</label>
                      <textarea v-model="editForm.bio" rows="3" class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all resize-none"></textarea>
                    </div>
                  </div>
                </div>

                <!-- 显示模式 -->
                <div v-else>
                  <p class="text-gray-600 mt-3">{{ userInfo?.bio || '这个人很懒，还没写介绍~' }}</p>
                  <div class="flex flex-wrap gap-4 mt-4 text-sm text-gray-500">
                    <span v-if="userInfo?.email" class="flex items-center gap-1 hover:text-indigo-500 transition-colors duration-300">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                      {{ userInfo.email }}
                    </span>
                    <span v-if="userInfo?.phone" class="flex items-center gap-1 hover:text-indigo-500 transition-colors duration-300">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path></svg>
                      {{ userInfo.phone }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计数据卡片 -->
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
          <div v-for="(stat, idx) in [
            { icon: '📅', value: stats.totalDays, label: '陪伴天数' },
            { icon: '✨', value: stats.totalMoments, label: '珍藏瞬间' },
            { icon: '📖', value: stats.totalJournals, label: '时光日记' },
            { icon: '📸', value: stats.totalPhotos, label: '回忆照片' },
            { icon: '❤️', value: stats.totalLikes, label: '获得点赞' },
            { icon: '💬', value: stats.totalComments, label: '收到评论' }
          ]" :key="idx"
               class="bg-white rounded-xl p-4 text-center shadow-sm card-hover fade-in-up"
               :style="{ transitionDelay: `${idx * 0.05}s` }">
            <div class="text-3xl mb-2 group-hover:scale-110 transition-transform duration-300">{{ stat.icon }}</div>
            <div class="text-2xl font-bold text-gray-800 stat-value">{{ stat.value }}</div>
            <div class="text-xs text-gray-500 mt-1">{{ stat.label }}</div>
          </div>
        </div>

        <!-- 标签页切换 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden mb-8 fade-in-up">
          <div class="flex border-b border-gray-100">
            <button
              v-for="tab in [
                { id: 'overview', name: '概览', icon: '📊' },
                { id: 'badges', name: '成就徽章', icon: '🏅' },
                { id: 'timeline', name: '成长轨迹', icon: '⏳' },
                { id: 'stats', name: '年度数据', icon: '📈' }
              ]"
              :key="tab.id"
              @click="activeTab = tab.id"
              class="flex-1 px-4 py-3 text-center transition-all duration-300 relative group overflow-hidden"
              :class="activeTab === tab.id ? 'text-indigo-600 font-medium' : 'text-gray-500 hover:text-gray-700'"
            >
              <span class="text-lg mr-1 inline-block group-hover:scale-110 transition-transform">{{ tab.icon }}</span>
              <span>{{ tab.name }}</span>
              <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-indigo-500 to-purple-600"></div>
              <div v-else class="absolute bottom-0 left-0 right-0 h-0.5 bg-gray-200 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300"></div>
            </button>
          </div>

          <div class="p-6">
            <!-- 概览标签页 -->
            <div v-if="activeTab === 'overview'" class="tab-content">
              <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
                <span class="w-1 h-5 bg-gradient-to-b from-indigo-500 to-purple-600 rounded-full animate-pulse"></span>
                最近动态
              </h3>
              <div class="space-y-3">
                <div v-for="(activity, idx) in activities.slice(0, 5)" :key="idx"
                     class="flex items-start gap-3 p-3 rounded-lg hover:bg-gradient-to-r hover:from-indigo-50 hover:to-transparent transition-all duration-300 hover:translate-x-2 cursor-pointer"
                     :style="{ transitionDelay: `${idx * 0.05}s` }">
                  <div class="w-8 h-8 bg-indigo-50 rounded-lg flex items-center justify-center text-lg group-hover:scale-110 transition-transform duration-300">{{ activity.icon }}</div>
                  <div class="flex-1">
                    <p class="text-sm font-medium text-gray-800">{{ activity.title }}</p>
                    <p class="text-xs text-gray-500">{{ activity.content }}</p>
                  </div>
                  <div class="text-xs text-gray-400">{{ activity.time }}</div>
                </div>
              </div>

              <div class="mt-6">
                <div class="flex justify-between items-center mb-3">
                  <h3 class="text-lg font-semibold text-gray-800">成就进度</h3>
                  <span class="text-sm text-indigo-600 font-medium animate-pulse">{{ totalProgress }}%</span>
                </div>
                <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-600 rounded-full transition-all duration-1000 relative" :style="{ width: totalProgress + '%' }">
                    <div class="absolute inset-0 bg-white/30 animate-shimmer"></div>
                  </div>
                </div>
                <p class="text-xs text-gray-500 mt-2">已获得 {{ badges.filter(b => b.earned).length }} / {{ badges.length }} 个徽章</p>
              </div>
            </div>

            <!-- 成就徽章标签页 -->
            <div v-else-if="activeTab === 'badges'" class="tab-content">
              <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                <div v-for="(badge, idx) in badges" :key="idx"
                     class="p-4 rounded-xl text-center transition-all duration-500 hover:-translate-y-2 hover:scale-105 cursor-pointer badge-card"
                     :class="badge.earned
                       ? 'bg-gradient-to-br from-indigo-50 to-purple-50 border border-indigo-100 shadow-sm earned'
                       : 'bg-gray-50 border border-gray-100'"
                     :style="{ transitionDelay: `${idx * 0.03}s` }">
                  <div class="text-4xl mb-2 group-hover:scale-125 transition-transform duration-300">{{ badge.icon }}</div>
                  <div class="font-semibold text-gray-800 text-sm">{{ badge.name }}</div>
                  <div class="text-xs text-gray-500 mt-1">{{ badge.desc }}</div>
                  <div v-if="badge.earned" class="mt-2">
                    <span class="text-xs text-indigo-500 bg-indigo-50 px-2 py-0.5 rounded-full inline-flex items-center gap-1">
                      <span>✓</span> 已获得
                    </span>
                  </div>
                  <div v-else class="mt-2">
                    <div class="h-1 bg-gray-200 rounded-full overflow-hidden">
                      <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-600 rounded-full transition-all duration-1000" :style="{ width: badge.progress + '%' }"></div>
                    </div>
                    <div class="text-xs text-gray-400 mt-1">{{ badge.progress }}%</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 成长轨迹标签页 -->
            <div v-else-if="activeTab === 'timeline'" class="tab-content">
              <div class="relative pl-8">
                <div class="absolute left-2 top-0 bottom-0 w-0.5 bg-gradient-to-b from-indigo-500 via-purple-500 to-pink-500 animate-growHeight"></div>
                <div v-for="(milestone, idx) in milestones" :key="idx"
                     class="relative mb-6 timeline-item"
                     :style="{ transitionDelay: `${idx * 0.05}s` }">
                  <div class="absolute -left-8 top-0 w-4 h-4 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-full border-2 border-white shadow-lg animate-ping-slow"></div>
                  <div class="bg-white rounded-xl p-4 border border-gray-100 hover:shadow-lg hover:-translate-y-1 transition-all duration-300 hover:scale-102 cursor-pointer">
                    <div class="flex items-center gap-3 mb-2">
                      <span class="text-2xl group-hover:scale-125 transition-transform duration-300">{{ milestone.icon }}</span>
                      <div>
                        <h4 class="font-semibold text-gray-800">{{ milestone.title }}</h4>
                        <p class="text-xs text-gray-500">{{ milestone.date }}</p>
                      </div>
                    </div>
                    <div class="inline-block px-2 py-0.5 rounded-full text-xs transition-all duration-300 hover:scale-105"
                         :class="{
                           'bg-green-100 text-green-600': milestone.type === 'achievement',
                           'bg-blue-100 text-blue-600': milestone.type === 'social',
                           'bg-orange-100 text-orange-600': milestone.type === 'streak',
                           'bg-purple-100 text-purple-600': milestone.type === 'premium'
                         }">
                      {{ milestone.type === 'achievement' ? '🏆 成就' : milestone.type === 'social' ? '💬 社交' : milestone.type === 'streak' ? '🔥 连续' : '💎 会员' }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 年度数据标签页 -->
            <div v-else-if="activeTab === 'stats'" class="tab-content">
              <div class="mb-6">
                <div class="flex justify-between items-center mb-4">
                  <h3 class="font-semibold text-gray-800">{{ yearlyData.year }}年 数据概览</h3>
                  <div class="flex gap-2">
                    <button class="px-3 py-1 text-sm border border-gray-300 rounded-full hover:border-indigo-400 hover:text-indigo-500 transition-all hover:scale-105">2025</button>
                    <button class="px-3 py-1 text-sm bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-full shadow-sm hover:shadow-lg transition-all hover:scale-105">2026</button>
                  </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                  <div v-for="(stat, idx) in [
                    { label: '总日记数', value: yearlyData.months.reduce((sum, m) => sum + m.journals, 0), icon: '📝' },
                    { label: '总照片数', value: yearlyData.months.reduce((sum, m) => sum + m.photos, 0), icon: '📸' },
                    { label: '总获赞数', value: yearlyData.months.reduce((sum, m) => sum + m.likes, 0), icon: '❤️' }
                  ]" :key="idx"
                       class="bg-gradient-to-br from-gray-50 to-white rounded-xl p-4 text-center hover:shadow-lg transition-all duration-300 hover:-translate-y-1 cursor-pointer"
                       :style="{ transitionDelay: `${idx * 0.1}s` }">
                    <div class="text-2xl mb-2 group-hover:scale-125 transition-transform duration-300">{{ stat.icon }}</div>
                    <div class="text-2xl font-bold text-indigo-600">{{ stat.value }}</div>
                    <div class="text-sm text-gray-500">{{ stat.label }}</div>
                  </div>
                </div>

                <div class="space-y-4">
                  <div v-for="(month, idx) in yearlyData.months.slice(0, 3)" :key="idx"
                       class="bg-gray-50 rounded-xl p-4 hover:shadow-md transition-all duration-300 hover:-translate-y-1 cursor-pointer"
                       :style="{ transitionDelay: `${idx * 0.05}s` }">
                    <div class="flex justify-between items-center mb-2">
                      <span class="font-medium text-gray-700">{{ month.name }}</span>
                      <span class="text-sm text-gray-500">日记 {{ month.journals }} · 照片 {{ month.photos }}</span>
                    </div>
                    <div class="flex gap-2">
                      <div class="flex-1">
                        <div class="flex justify-between text-xs text-gray-400 mb-1">
                          <span>日记</span>
                          <span>{{ month.journals }}</span>
                        </div>
                        <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
                          <div class="h-full bg-gradient-to-r from-indigo-500 to-indigo-600 rounded-full chart-bar" :style="{ width: (month.journals / 20 * 100) + '%' }"></div>
                        </div>
                      </div>
                      <div class="flex-1">
                        <div class="flex justify-between text-xs text-gray-400 mb-1">
                          <span>照片</span>
                          <span>{{ month.photos }}</span>
                        </div>
                        <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
                          <div class="h-full bg-gradient-to-r from-purple-500 to-purple-600 rounded-full chart-bar" :style="{ width: (month.photos / 20 * 100) + '%' }"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <p class="text-center text-sm text-gray-400 pt-4 animate-pulse">✨ 更多数据正在收集中... ✨</p>
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
/* 导航栏动画 - 直接显示，不需要延迟 */
.nav-animation {
  animation: slideDown 0.5s ease-out forwards;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 基础动画类 */
.fade-in-up {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.fade-in-up.show {
  opacity: 1;
  transform: translateY(0);
}

/* 悬浮效果 */
.hover-scale {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.hover-scale:hover {
  transform: scale(1.05);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}

/* 标签页切换动画 */
.tab-content {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 数据增长动画 */
@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-value {
  animation: countUp 1s ease-out forwards;
}

/* 徽章动画 */
.badge-card {
  position: relative;
  overflow: hidden;
}

.badge-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(139, 92, 246, 0.1),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 3s infinite;
  opacity: 0;
}

.badge-card.earned:hover::before {
  opacity: 1;
}

@keyframes shine {
  0% {
    transform: translateX(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) rotate(45deg);
  }
}

/* 按钮交互动效 */
.btn-primary {
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: left 0.6s ease;
  z-index: -1;
}

.btn-primary:hover::before {
  left: 100%;
}

/* 时间线动画 */
.timeline-item {
  opacity: 0;
  transform: translateX(-20px);
  animation: slideInLeft 0.6s ease-out forwards;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 编辑模式切换动画 */
.edit-form {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 加载动画优化 */
.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 图表动画 */
.chart-bar {
  animation: growUp 1s ease-out forwards;
  transform-origin: bottom;
}

@keyframes growUp {
  from {
    transform: scaleY(0);
  }
  to {
    transform: scaleY(1);
  }
}

/* 卡片悬浮效果增强 */
.card-hover {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card-hover:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* 导航栏滚动效果 */
.nav-scrolled {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.95);
}

/* 头像动画 */
.avatar-container {
  position: relative;
  transition: all 0.3s ease;
}

.avatar-container:hover {
  transform: scale(1.1);
}

.avatar-container::after {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 1rem;
  background: linear-gradient(45deg, #6366f1, #a855f7, #ec489a);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.avatar-container:hover::after {
  opacity: 1;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 新增动画 */
@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes growHeight {
  from {
    transform: scaleY(0);
  }
  to {
    transform: scaleY(1);
  }
}

@keyframes ping-slow {
  75%, 100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.animate-shimmer {
  animation: shimmer 2s infinite;
}

.animate-growHeight {
  animation: growHeight 1s ease-out forwards;
}

.animate-ping-slow {
  animation: ping-slow 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}

.hover\:scale-102:hover {
  transform: scale(1.02);
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(to bottom, #6366f1, #a855f7);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(to bottom, #4f46e5, #9333ea);
}
</style>
