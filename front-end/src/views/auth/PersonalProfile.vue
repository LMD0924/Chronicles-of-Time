<script setup>
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getStoredTheme, onThemeChange, ThemeType } from '@/utils/theme';
import {message} from "ant-design-vue";

const [messageApi, contextHolder] = message.useMessage();
const router = useRouter()
const route = useRoute()
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
const isDark = ref(getStoredTheme() === ThemeType.DARK)

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
    messageApi.error('获取用户信息失败')
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
    introduction: userInfo.value.introduction || userInfo.value.bio || ''
  }
  isEditing.value = true
}

// 保存编辑
const saveEdit = async () => {
  try {
    const response = await request.put('/user/updateUserInfo', editForm.value, (msg, data) => {
      return data
    })

    if (response.code === 200) {
      // 更新本地用户信息
      userInfo.value = { ...userInfo.value, ...editForm.value }
      localStorage.setItem('user_info', JSON.stringify(userInfo.value))
      messageApi.success('资料更新成功')
      isEditing.value = false
    } else {
      messageApi.error('保存失败，请重试')
    }
  } catch (error) {
    messageApi.error('保存失败，请重试')
    console.error('更新资料失败', error)
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
}

// 上传头像相关
const uploadLoading = ref(false)

// 选择图片
const handleAvatarClick = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      uploadAvatar(file)
    }
  }
  input.click()
}

// 上传头像 - 使用 request.upload 方法
const uploadAvatar = async (file) => {
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    messageApi.error('请选择图片文件（JPG、PNG、GIF、WEBP）')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    messageApi.error('图片大小不能超过5MB')
    return
  }

  uploadLoading.value = true

  try {
    // ✅ 使用 request.upload 方法
    const uploadRes = await request.upload('/upload/image', file, (percent) => {
      console.log(`上传进度: ${percent}%`)
    })

    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.message || '上传图片失败')
    }

    const avatarUrl = uploadRes.data.url

    // 更新用户头像
    const updateRes = await request.post('/user/uploadAvatar', {
      avatarUrl: avatarUrl
    }, (msg,data)=>{
    })

    if (updateRes.code === 200) {
      userInfo.value.avatar = updateRes.data

      const storedUser = localStorage.getItem('user_info')
      if (storedUser) {
        const userInfoData = JSON.parse(storedUser)
        userInfoData.avatar = updateRes.data
        localStorage.setItem('user_info', JSON.stringify(userInfoData))
      }

      messageApi.success('头像更新成功')
    } else {
      messageApi.error(updateRes.message || '更新头像失败')
    }
  } catch (error) {
    messageApi.error(error.message || '上传头像失败')
    console.error('上传头像失败', error)
  } finally {
    uploadLoading.value = false
  }
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

  // 监听主题变化
  const stopListen = onThemeChange((theme) => {
    isDark.value = theme === ThemeType.DARK
  })

  onUnmounted(() => {
    stopListen()
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <contextHolder />
  <div :class="[isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900', 'min-h-screen']">
    <div class="flex justify-between items-center">
      <!-- 左侧占位，保持平衡 -->
      <div class="w-32"></div>

      <!-- 中间：个人简历按钮 -->
      <div class="mt-4 shadow-lg" :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'flex items-center gap-1 rounded-full p-1 shadow-sm']">
        <button
          @click="$router.push('/Resume')"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        route.path === '/Resume'
          ? (isDark ? 'bg-indigo-900/50 text-indigo-400' : 'bg-indigo-100 text-indigo-600')
          : (isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600')
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">👤</span>
        <span>个人简历</span>
      </span>
        </button>
        <button
          @click="$router.push('/PersonalProfile')"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        route.path === '/PersonalProfile'
          ? (isDark ? 'bg-indigo-900/50 text-indigo-400' : 'bg-indigo-100 text-indigo-600 shadow-lg')
          : (isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600')
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">👤</span>
        <span>个人档案</span>
      </span>
        </button>
      </div>

      <!-- 右侧按钮组 -->
      <div class="mt-4 mr-56" :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'flex items-center gap-1 rounded-full p-1 shadow-sm']">
        <button
          @click="$router.push('/home')"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600'
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">🏠</span>
        <span>返回首页</span>
      </span>
        </button>
        <button
          @click="handleLogout"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        isDark ? 'text-gray-300 hover:text-red-400' : 'text-gray-600 hover:text-red-600'
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">🚪</span>
        <span>退出登录</span>
      </span>
        </button>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div>
        <!-- 个人资料卡片 - 暗色模式使用 bg-white/10 -->
        <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white border border-gray-100', 'rounded-2xl shadow-sm overflow-hidden mb-8 fade-in-up']">
          <div class="relative h-32 bg-gradient-to-r from-indigo-500 to-purple-600">
            <div class="absolute inset-0 bg-black/20"></div>
            <div class="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-white to-transparent"></div>
          </div>
          <div class="relative px-6 pb-6">
            <div class="flex flex-col md:flex-row items-start md:items-end -mt-12 mb-6">
              <div class="relative group avatar-container cursor-pointer" @click="handleAvatarClick">
                <div class="w-24 h-24 bg-gradient-to-br from-indigo-100 to-purple-100 rounded-2xl flex items-center justify-center text-5xl shadow-lg border-4 border-white transition-all group-hover:scale-105 group-hover:rotate-6">
                  <img v-if="userInfo?.avatar" :src="userInfo.avatar" class="w-full h-full object-cover rounded-2xl" />
                  <span v-else>👤</span>
                </div>
                <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full border-2 border-white animate-pulse"></div>
                <div class="absolute inset-0 bg-black/30 rounded-2xl flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  <span class="text-white text-sm font-medium">更换头像</span>
                </div>
                <div v-if="uploadLoading" class="absolute inset-0 bg-black/50 rounded-2xl flex items-center justify-center">
                  <div class="w-10 h-10 border-4 border-white/50 border-t-white rounded-full animate-spin"></div>
                </div>
              </div>
              <div class="flex-1 md:ml-6 mt-4 md:mt-0">
                <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <h1 class="text-2xl font-bold" :class="isDark ? 'text-white' : 'text-gray-800'">{{ userInfo?.name }}</h1>
                    <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="mt-1">@{{ userInfo?.username }}</p>
                    <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-sm mt-2 flex items-center gap-1">
                      <svg class="w-4 h-4 animate-bounce" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                      加入拾光记 {{ joinDays }} 天
                    </p>
                  </div>
                  <button v-if="!isEditing" @click="startEdit" :class="[isDark ? 'border-white/20 text-gray-300 hover:border-indigo-400 hover:text-indigo-400' : 'border-gray-300 text-gray-600 hover:border-indigo-400 hover:text-indigo-500', 'px-5 py-2 border rounded-full hover:shadow-md transition-all duration-300 hover:scale-105']">
                    编辑资料
                  </button>
                  <div v-else class="flex gap-2">
                    <button @click="cancelEdit" :class="[isDark ? 'border-white/20 text-gray-300 hover:bg-white/10' : 'border-gray-300 text-gray-600 hover:bg-gray-50', 'px-5 py-2 border rounded-full transition-all hover:scale-105']">取消</button>
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
                      <label :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="block text-xs mb-1">昵称</label>
                      <input v-model="editForm.name" type="text" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200 text-gray-900', 'w-full px-3 py-2 border rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all']">
                    </div>
                    <div>
                      <label :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="block text-xs mb-1">邮箱</label>
                      <input v-model="editForm.email" type="email" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200 text-gray-900', 'w-full px-3 py-2 border rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all']">
                    </div>
                    <div>
                      <label :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="block text-xs mb-1">手机号</label>
                      <input v-model="editForm.phone" type="tel" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200 text-gray-900', 'w-full px-3 py-2 border rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all']">
                    </div>
                    <div class="md:col-span-2">
                      <label :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="block text-xs mb-1">个人简介</label>
                      <textarea v-model="editForm.introduction" rows="3" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200 text-gray-900', 'w-full px-3 py-2 border rounded-lg focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all resize-none']"></textarea>
                    </div>
                  </div>
                </div>

                <!-- 显示模式 -->
                <div v-else>
                  <p :class="isDark ? 'text-gray-300' : 'text-gray-600'" class="mt-3">{{ userInfo?.introduction || userInfo?.bio || '这个人很懒，还没写介绍~' }}</p>
                  <div class="flex flex-wrap gap-4 mt-4 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                    <span v-if="userInfo?.email" :class="isDark ? 'hover:text-indigo-400' : 'hover:text-indigo-500'" class="flex items-center gap-1 transition-colors duration-300">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                      {{ userInfo.email }}
                    </span>
                    <span v-if="userInfo?.phone" :class="isDark ? 'hover:text-indigo-400' : 'hover:text-indigo-500'" class="flex items-center gap-1 transition-colors duration-300">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path></svg>
                      {{ userInfo.phone }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计数据卡片 - 暗色模式使用 bg-white/10 -->
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
          <div v-for="(stat, idx) in [
            { icon: '📅', value: stats.totalDays, label: '陪伴天数' },
            { icon: '✨', value: stats.totalMoments, label: '珍藏瞬间' },
            { icon: '📖', value: stats.totalJournals, label: '时光日记' },
            { icon: '📸', value: stats.totalPhotos, label: '回忆照片' },
            { icon: '❤️', value: stats.totalLikes, label: '获得点赞' },
            { icon: '💬', value: stats.totalComments, label: '收到评论' }
          ]" :key="idx"
               :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white shadow-sm', 'rounded-xl p-4 text-center card-hover fade-in-up']"
               :style="{ transitionDelay: `${idx * 0.05}s` }">
            <div class="text-3xl mb-2 group-hover:scale-110 transition-transform duration-300">{{ stat.icon }}</div>
            <div class="text-2xl font-bold stat-value" :class="isDark ? 'text-white' : 'text-gray-800'">{{ stat.value }}</div>
            <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs mt-1">{{ stat.label }}</div>
          </div>
        </div>

        <!-- 标签页切换 - 暗色模式使用 bg-white/10 -->
        <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white border border-gray-100', 'rounded-xl shadow-sm overflow-hidden mb-8 fade-in-up']">
          <div :class="[isDark ? 'border-white/20' : 'border-gray-100', 'flex border-b']">
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
              :class="activeTab === tab.id ? 'text-indigo-600 font-medium' : (isDark ? 'text-gray-400 hover:text-gray-300' : 'text-gray-500 hover:text-gray-700')"
            >
              <span class="text-lg mr-1 inline-block group-hover:scale-110 transition-transform">{{ tab.icon }}</span>
              <span>{{ tab.name }}</span>
              <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-indigo-500 to-purple-600"></div>
              <div v-else :class="[isDark ? 'bg-white/20' : 'bg-gray-200', 'absolute bottom-0 left-0 right-0 h-0.5 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300']"></div>
            </button>
          </div>

          <div class="p-6">
            <!-- 概览标签页 -->
            <div v-if="activeTab === 'overview'" class="tab-content">
              <h3 class="text-lg font-semibold mb-4 flex items-center gap-2" :class="isDark ? 'text-white' : 'text-gray-800'">
                <span class="w-1 h-5 bg-gradient-to-b from-indigo-500 to-purple-600 rounded-full animate-pulse"></span>
                最近动态
              </h3>
              <div class="space-y-3">
                <div v-for="(activity, idx) in activities.slice(0, 5)" :key="idx"
                     :class="[isDark ? 'hover:bg-white/5' : 'hover:bg-gradient-to-r hover:from-indigo-50 hover:to-transparent', 'flex items-start gap-3 p-3 rounded-lg transition-all duration-300 hover:translate-x-2 cursor-pointer']"
                     :style="{ transitionDelay: `${idx * 0.05}s` }">
                  <div :class="[isDark ? 'bg-indigo-500/20' : 'bg-indigo-50', 'w-8 h-8 rounded-lg flex items-center justify-center text-lg group-hover:scale-110 transition-transform duration-300']">{{ activity.icon }}</div>
                  <div class="flex-1">
                    <p :class="isDark ? 'text-gray-300' : 'text-gray-800'" class="text-sm font-medium">{{ activity.title }}</p>
                    <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">{{ activity.content }}</p>
                  </div>
                  <div :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">{{ activity.time }}</div>
                </div>
              </div>

              <div class="mt-6">
                <div class="flex justify-between items-center mb-3">
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="text-lg font-semibold">成就进度</h3>
                  <span class="text-sm text-indigo-600 font-medium animate-pulse">{{ totalProgress }}%</span>
                </div>
                <div :class="[isDark ? 'bg-white/10' : 'bg-gray-100', 'h-2 rounded-full overflow-hidden']">
                  <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-600 rounded-full transition-all duration-1000 relative" :style="{ width: totalProgress + '%' }">
                    <div class="absolute inset-0 bg-white/30 animate-shimmer"></div>
                  </div>
                </div>
                <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs mt-2">已获得 {{ badges.filter(b => b.earned).length }} / {{ badges.length }} 个徽章</p>
              </div>
            </div>

            <!-- 成就徽章标签页 -->
            <div v-else-if="activeTab === 'badges'" class="tab-content">
              <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                <div v-for="(badge, idx) in badges" :key="idx"
                     class="p-4 rounded-xl text-center transition-all duration-500 hover:-translate-y-2 hover:scale-105 cursor-pointer badge-card"
                     :class="badge.earned
                       ? (isDark ? 'bg-indigo-500/20 border border-indigo-400/30' : 'bg-gradient-to-br from-indigo-50 to-purple-50 border border-indigo-100 shadow-sm')
                       : (isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50 border border-gray-100')"
                     :style="{ transitionDelay: `${idx * 0.03}s` }">
                  <div class="text-4xl mb-2 group-hover:scale-125 transition-transform duration-300">{{ badge.icon }}</div>
                  <div :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold text-sm">{{ badge.name }}</div>
                  <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs mt-1">{{ badge.desc }}</div>
                  <div v-if="badge.earned" class="mt-2">
                    <span :class="isDark ? 'text-indigo-400 bg-indigo-500/20' : 'text-indigo-500 bg-indigo-50'" class="text-xs px-2 py-0.5 rounded-full inline-flex items-center gap-1">
                      <span>✓</span> 已获得
                    </span>
                  </div>
                  <div v-else class="mt-2">
                    <div :class="[isDark ? 'bg-white/20' : 'bg-gray-200', 'h-1 rounded-full overflow-hidden']">
                      <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-600 rounded-full transition-all duration-1000" :style="{ width: badge.progress + '%' }"></div>
                    </div>
                    <div :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs mt-1">{{ badge.progress }}%</div>
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
                  <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white border border-gray-100', 'rounded-xl p-4 hover:shadow-lg hover:-translate-y-1 transition-all duration-300 hover:scale-102 cursor-pointer']">
                    <div class="flex items-center gap-3 mb-2">
                      <span class="text-2xl group-hover:scale-125 transition-transform duration-300">{{ milestone.icon }}</span>
                      <div>
                        <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold">{{ milestone.title }}</h4>
                        <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">{{ milestone.date }}</p>
                      </div>
                    </div>
                    <div class="inline-block px-2 py-0.5 rounded-full text-xs transition-all duration-300 hover:scale-105"
                         :class="{
                           'bg-green-100 text-green-600': !isDark && milestone.type === 'achievement',
                           'bg-blue-100 text-blue-600': !isDark && milestone.type === 'social',
                           'bg-orange-100 text-orange-600': !isDark && milestone.type === 'streak',
                           'bg-purple-100 text-purple-600': !isDark && milestone.type === 'premium',
                           'bg-green-500/20 text-green-400': isDark && milestone.type === 'achievement',
                           'bg-blue-500/20 text-blue-400': isDark && milestone.type === 'social',
                           'bg-orange-500/20 text-orange-400': isDark && milestone.type === 'streak',
                           'bg-purple-500/20 text-purple-400': isDark && milestone.type === 'premium'
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
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold">{{ yearlyData.year }}年 数据概览</h3>
                  <div class="flex gap-2">
                    <button :class="[isDark ? 'border-white/20 text-gray-300 hover:border-indigo-400 hover:text-indigo-400' : 'border-gray-300 text-gray-600 hover:border-indigo-400 hover:text-indigo-500', 'px-3 py-1 text-sm border rounded-full transition-all hover:scale-105']">2025</button>
                    <button class="px-3 py-1 text-sm bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-full shadow-sm hover:shadow-lg transition-all hover:scale-105">2026</button>
                  </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                  <div v-for="(stat, idx) in [
                    { label: '总日记数', value: yearlyData.months.reduce((sum, m) => sum + m.journals, 0), icon: '📝' },
                    { label: '总照片数', value: yearlyData.months.reduce((sum, m) => sum + m.photos, 0), icon: '📸' },
                    { label: '总获赞数', value: yearlyData.months.reduce((sum, m) => sum + m.likes, 0), icon: '❤️' }
                  ]" :key="idx"
                       :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-gradient-to-br from-gray-50 to-white', 'rounded-xl p-4 text-center hover:shadow-lg transition-all duration-300 hover:-translate-y-1 cursor-pointer']"
                       :style="{ transitionDelay: `${idx * 0.1}s` }">
                    <div class="text-2xl mb-2 group-hover:scale-125 transition-transform duration-300">{{ stat.icon }}</div>
                    <div class="text-2xl font-bold text-indigo-600">{{ stat.value }}</div>
                    <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ stat.label }}</div>
                  </div>
                </div>

                <div class="space-y-4">
                  <div v-for="(month, idx) in yearlyData.months.slice(0, 3)" :key="idx"
                       :class="[isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50', 'rounded-xl p-4 hover:shadow-md transition-all duration-300 hover:-translate-y-1 cursor-pointer']"
                       :style="{ transitionDelay: `${idx * 0.05}s` }">
                    <div class="flex justify-between items-center mb-2">
                      <span :class="isDark ? 'text-white' : 'text-gray-700'" class="font-medium">{{ month.name }}</span>
                      <span :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">日记 {{ month.journals }} · 照片 {{ month.photos }}</span>
                    </div>
                    <div class="flex gap-2">
                      <div class="flex-1">
                        <div class="flex justify-between text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span>日记</span>
                          <span>{{ month.journals }}</span>
                        </div>
                        <div :class="[isDark ? 'bg-white/10' : 'bg-gray-200', 'h-2 rounded-full overflow-hidden']">
                          <div class="h-full bg-gradient-to-r from-indigo-500 to-indigo-600 rounded-full chart-bar" :style="{ width: (month.journals / 20 * 100) + '%' }"></div>
                        </div>
                      </div>
                      <div class="flex-1">
                        <div class="flex justify-between text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span>照片</span>
                          <span>{{ month.photos }}</span>
                        </div>
                        <div :class="[isDark ? 'bg-white/10' : 'bg-gray-200', 'h-2 rounded-full overflow-hidden']">
                          <div class="h-full bg-gradient-to-r from-purple-500 to-purple-600 rounded-full chart-bar" :style="{ width: (month.photos / 20 * 100) + '%' }"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-center text-sm pt-4 animate-pulse">✨ 更多数据正在收集中... ✨</p>
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
/* 导航栏动画 */
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
