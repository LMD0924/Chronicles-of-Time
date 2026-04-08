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
const isEditing = ref(false)
const editForm = ref({
  name: '',
  email: '',
  phone: '',
  bio: ''
})
const isDark = ref(getStoredTheme() === ThemeType.DARK)

// 当前激活的右侧标签
const activeRightTab = ref('articles') // articles, likes, favorites, comments

// 统计数据
const stats = ref({
  totalArticles: 0,
  totalLikes: 0,
  totalFavorites: 0,
  totalComments: 0,
  totalViews: 0
})

// 发表的文章列表
const myArticles = ref([])
const articlesLoading = ref(false)
const articlesPage = ref(1)
const articlesTotal = ref(0)

// 点赞的文章列表
const likedArticles = ref([])
const likesLoading = ref(false)

// 收藏的文章列表
const favoritedArticles = ref([])
const favoritesLoading = ref(false)

// 我的评论列表
const myComments = ref([])
const commentsLoading = ref(false)

// 成就徽章
const badges = ref([
  { name: '初露锋芒', icon: '🌱', desc: '发表第一篇文章', earned: false },
  { name: '小有名气', icon: '🌟', desc: '获得100个点赞', earned: false },
  { name: '收藏家', icon: '📚', desc: '收藏50篇文章', earned: false },
  { name: '评论达人', icon: '💬', desc: '发表50条评论', earned: false },
  { name: '创作之星', icon: '⭐', desc: '发表10篇文章', earned: false },
  { name: '人气作者', icon: '🔥', desc: '单篇文章获赞100+', earned: false }
])

// 热门推荐
const hotArticles = ref([
  { id: 1, title: 'Vue3 组合式 API 完全指南', views: 1234, likes: 89 },
  { id: 2, title: 'Spring Boot 实战经验分享', views: 987, likes: 56 },
  { id: 3, title: '前端工程化最佳实践', views: 876, likes: 67 },
  { id: 4, title: 'MySQL 性能优化技巧', views: 654, likes: 43 },
  { id: 5, title: 'TypeScript 入门到精通', views: 543, likes: 38 }
])

// 友情链接
const friendLinks = ref([
  { name: 'Vue.js 官方文档', url: 'https://vuejs.org' },
  { name: 'Ant Design Vue', url: 'https://next.antdv.com' },
  { name: 'Element Plus', url: 'https://element-plus.org' },
  { name: 'Tailwind CSS', url: 'https://tailwindcss.com' }
])

// 计算加入天数
const joinDays = computed(() => {
  if (!userInfo.value?.createTime) return 0
  const join = new Date(userInfo.value.createTime)
  const now = new Date()
  const diffTime = Math.abs(now - join)
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
})

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    await request.get('/user/getUserById', {}, (msg, data) => {
      userInfo.value = data
      console.log("用户信息：", userInfo.value)
    })
  } catch (error) {
    messageApi.error('获取用户信息失败')
  } finally {
    loading.value = false
    setTimeout(() => {
      document.querySelectorAll('.fade-in-up').forEach(el => {
        el.classList.add('show')
      })
    }, 100)
  }
}

// 获取用户发表的文章
const fetchMyArticles = async () => {
  articlesLoading.value = true
  try {
    const res = await request.get('/api/content/user/' + userInfo.value.id, {
      pageNum: articlesPage.value,
      pageSize: 10
    })
    if (res.code === 200 && res.data) {
      myArticles.value = res.data.records || []
      articlesTotal.value = res.data.total || 0
      stats.value.totalArticles = articlesTotal.value
    }
  } catch (error) {
    console.error('获取文章列表失败', error)
  } finally {
    articlesLoading.value = false
  }
}

// 获取用户点赞的文章
const fetchLikedArticles = async () => {
  likesLoading.value = true
  try {
    const res = await request.get('/api/content/user/liked', {
      userId: userInfo.value.id,
      pageNum: 1,
      pageSize: 20
    })
    if (res.code === 200) {
      likedArticles.value = res.data || []
      stats.value.totalLikes = likedArticles.value.length
    }
  } catch (error) {
    console.error('获取点赞列表失败', error)
  } finally {
    likesLoading.value = false
  }
}

// 获取用户收藏的文章
const fetchFavoritedArticles = async () => {
  favoritesLoading.value = true
  try {
    const res = await request.get('/api/content/user/favorited', {
      userId: userInfo.value.id,
      pageNum: 1,
      pageSize: 20
    })
    if (res.code === 200) {
      favoritedArticles.value = res.data || []
      stats.value.totalFavorites = favoritedArticles.value.length
    }
  } catch (error) {
    console.error('获取收藏列表失败', error)
  } finally {
    favoritesLoading.value = false
  }
}

// 获取用户评论
const fetchMyComments = async () => {
  commentsLoading.value = true
  try {
    const res = await request.get('/api/content/comment/user/' + userInfo.value.id, {
      pageNum: 1,
      pageSize: 20
    })
    if (res.code === 200) {
      myComments.value = res.data || []
      stats.value.totalComments = myComments.value.length
    }
  } catch (error) {
    console.error('获取评论列表失败', error)
  } finally {
    commentsLoading.value = false
  }
}

// 加载所有数据
const loadUserData = async () => {
  if (!userInfo.value?.id) return
  await Promise.all([
    fetchMyArticles(),
    fetchLikedArticles(),
    fetchFavoritedArticles(),
    fetchMyComments()
  ])

  // 更新成就进度
  updateBadgesProgress()
}

// 更新成就进度
const updateBadgesProgress = () => {
  badges.value[0].earned = stats.value.totalArticles >= 1
  badges.value[1].earned = stats.value.totalLikes >= 100
  badges.value[2].earned = stats.value.totalFavorites >= 50
  badges.value[3].earned = stats.value.totalComments >= 50
  badges.value[4].earned = stats.value.totalArticles >= 10
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

// 上传头像
const uploadLoading = ref(false)
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

const uploadAvatar = async (file) => {
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
    const uploadRes = await request.upload('/upload/image', file, (percent) => {
      console.log(`上传进度: ${percent}%`)
    })

    if (uploadRes.code !== 200) {
      throw new Error(uploadRes.message || '上传图片失败')
    }

    const avatarUrl = uploadRes.data.url

    const updateRes = await request.post('/user/uploadAvatar', {
      avatarUrl: avatarUrl
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

// 跳转到文章详情
const goToArticle = (id) => {
  router.push(`/content/detail/${id}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 切换右侧标签
const switchTab = (tab) => {
  activeRightTab.value = tab
}

onMounted(async () => {
  await fetchUserInfo()
  await loadUserData()

  const stopListen = onThemeChange((theme) => {
    isDark.value = theme === ThemeType.DARK
  })

  onUnmounted(() => {
    stopListen()
  })
})
</script>

<template>
  <contextHolder />
  <div :class="[isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30', 'min-h-screen']">

    <!-- 保持原有导航栏不变 -->
    <div class="flex justify-between items-center pt-4 px-6">
      <div class="w-32"></div>
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

      <div class="mt-4 mr-56" :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'flex items-center gap-1 rounded-full p-1 shadow-sm']">
        <button
          @click="$router.push('/home')"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600']"
        >
          <span class="relative flex items-center gap-2 z-10">
            <span class="text-base">🏠</span>
            <span>返回首页</span>
          </span>
        </button>
        <button
          @click="handleLogout"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[isDark ? 'text-gray-300 hover:text-red-400' : 'text-gray-600 hover:text-red-600']"
        >
          <span class="relative flex items-center gap-2 z-10">
            <span class="text-base">🚪</span>
            <span>退出登录</span>
          </span>
        </button>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">

        <!-- 左侧：用户信息卡片 -->
        <div class="lg:col-span-4 space-y-6">
          <!-- 个人信息卡片 -->
          <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white border border-gray-100', 'rounded-2xl shadow-sm overflow-hidden fade-in-up']">
            <div class="relative h-24 bg-gradient-to-r from-indigo-500 to-purple-600">
              <div class="absolute inset-0 bg-black/20"></div>
            </div>
            <div class="relative px-6 pb-6">
              <div class="flex flex-col items-center -mt-12 mb-4">
                <div class="relative group avatar-container cursor-pointer" @click="handleAvatarClick">
                  <div class="w-24 h-24 bg-gradient-to-br from-indigo-100 to-purple-100 rounded-2xl flex items-center justify-center text-5xl shadow-lg border-4 border-white transition-all group-hover:scale-105">
                    <img v-if="userInfo?.avatar" :src="userInfo.avatar" class="w-full h-full object-cover rounded-2xl" />
                    <span v-else>👤</span>
                  </div>
                  <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full border-2 border-white"></div>
                  <div class="absolute inset-0 bg-black/30 rounded-2xl flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <span class="text-white text-sm font-medium">更换头像</span>
                  </div>
                  <div v-if="uploadLoading" class="absolute inset-0 bg-black/50 rounded-2xl flex items-center justify-center">
                    <div class="w-10 h-10 border-4 border-white/50 border-t-white rounded-full animate-spin"></div>
                  </div>
                </div>

                <h2 class="text-xl font-bold mt-3" :class="isDark ? 'text-white' : 'text-gray-800'">{{ userInfo?.name || userInfo?.username }}</h2>
                <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">@{{ userInfo?.username }}</p>

                <div class="flex items-center gap-1 mt-2 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                  </svg>
                  <span>加入 {{ joinDays }} 天</span>
                </div>
              </div>

              <div v-if="!isEditing" class="text-center">
                <p :class="isDark ? 'text-gray-300' : 'text-gray-600'" class="text-sm">{{ userInfo?.introduction || userInfo?.bio || '这个人很懒，还没写介绍~' }}</p>
                <button @click="startEdit" :class="[isDark ? 'border-white/20 text-gray-300 hover:border-indigo-400 hover:text-indigo-400' : 'border-gray-300 text-gray-600 hover:border-indigo-400 hover:text-indigo-500', 'mt-4 px-5 py-2 border rounded-full text-sm transition-all hover:scale-105']">
                  编辑资料
                </button>
              </div>

              <div v-else class="space-y-3">
                <div>
                  <input v-model="editForm.name" type="text" placeholder="昵称" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-gray-50 border-gray-200', 'w-full px-3 py-2 border rounded-lg text-sm outline-none focus:border-indigo-400']">
                </div>
                <div>
                  <input v-model="editForm.email" type="email" placeholder="邮箱" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-gray-50 border-gray-200', 'w-full px-3 py-2 border rounded-lg text-sm outline-none focus:border-indigo-400']">
                </div>
                <div>
                  <input v-model="editForm.phone" type="tel" placeholder="手机号" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-gray-50 border-gray-200', 'w-full px-3 py-2 border rounded-lg text-sm outline-none focus:border-indigo-400']">
                </div>
                <div>
                  <textarea v-model="editForm.introduction" rows="2" placeholder="个人简介" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-gray-50 border-gray-200', 'w-full px-3 py-2 border rounded-lg text-sm outline-none focus:border-indigo-400 resize-none']"></textarea>
                </div>
                <div class="flex gap-2">
                  <button @click="cancelEdit" :class="[isDark ? 'border-white/20 text-gray-300' : 'border-gray-300 text-gray-600', 'flex-1 px-3 py-2 border rounded-lg text-sm transition-all hover:scale-105']">取消</button>
                  <button @click="saveEdit" class="flex-1 px-3 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-lg text-sm hover:shadow-lg transition-all hover:scale-105">保存</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 统计数据卡片 -->
          <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white shadow-sm', 'rounded-2xl p-5']">
            <h3 class="font-semibold mb-4 flex items-center gap-2" :class="isDark ? 'text-white' : 'text-gray-800'">
              📊 数据统计
            </h3>
            <div class="grid grid-cols-2 gap-4">
              <div class="text-center">
                <div class="text-2xl font-bold text-indigo-500">{{ stats.totalArticles }}</div>
                <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">发表文章</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-indigo-500">{{ stats.totalLikes }}</div>
                <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">获得点赞</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-indigo-500">{{ stats.totalFavorites }}</div>
                <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">收藏数量</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-indigo-500">{{ stats.totalComments }}</div>
                <div :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs">评论数量</div>
              </div>
            </div>
          </div>

          <!-- 成就徽章 -->
          <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white shadow-sm', 'rounded-2xl p-5']">
            <h3 class="font-semibold mb-4 flex items-center gap-2" :class="isDark ? 'text-white' : 'text-gray-800'">
              🏅 成就徽章
            </h3>
            <div class="flex flex-wrap gap-3">
              <div v-for="badge in badges" :key="badge.name" class="text-center group cursor-pointer">
                <div :class="[
                  badge.earned
                    ? 'bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg'
                    : (isDark ? 'bg-gray-800' : 'bg-gray-100'),
                  'w-14 h-14 rounded-2xl flex items-center justify-center text-2xl transition-all group-hover:scale-110'
                ]">
                  <span :class="badge.earned ? '' : 'opacity-40'">{{ badge.icon }}</span>
                </div>
                <div :class="[isDark ? 'text-gray-400' : 'text-gray-500', 'text-xs mt-1']">{{ badge.name }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：内容区域 -->
        <div class="lg:col-span-8">
          <!-- 标签页切换 -->
          <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white shadow-sm', 'rounded-2xl overflow-hidden fade-in-up']">
            <div :class="[isDark ? 'border-white/20' : 'border-gray-100', 'flex border-b']">
              <button
                v-for="tab in [
                  { id: 'articles', name: '我的发表', icon: '📝', count: stats.totalArticles },
                  { id: 'likes', name: '我的点赞', icon: '❤️', count: stats.totalLikes },
                  { id: 'favorites', name: '我的收藏', icon: '⭐', count: stats.totalFavorites },
                  { id: 'comments', name: '我的评论', icon: '💬', count: stats.totalComments }
                ]"
                :key="tab.id"
                @click="switchTab(tab.id)"
                class="flex-1 px-4 py-3 text-center transition-all duration-300 relative group"
                :class="activeRightTab === tab.id
                  ? 'text-indigo-600 font-medium'
                  : (isDark ? 'text-gray-400 hover:text-gray-300' : 'text-gray-500 hover:text-gray-700')"
              >
                <span class="text-lg mr-1">{{ tab.icon }}</span>
                <span>{{ tab.name }}</span>
                <span class="ml-1 text-xs" :class="activeRightTab === tab.id ? 'text-indigo-500' : 'text-gray-400'">({{ tab.count }})</span>
                <div v-if="activeRightTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-indigo-500 to-purple-600"></div>
              </button>
            </div>

            <!-- 内容区域 - 设置固定高度和滚动 -->
            <div class="content-scroll-area">

              <!-- 我的发表 -->
              <div v-if="activeRightTab === 'articles'">
                <div v-if="articlesLoading" class="text-center py-8">
                  <div class="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
                </div>
                <div v-else-if="myArticles.length === 0" class="text-center py-12">
                  <div class="text-5xl mb-3">📝</div>
                  <p :class="isDark ? 'text-gray-400' : 'text-gray-500'">还没有发表过文章</p>
                  <button @click="$router.push('/content/create')" class="mt-4 px-4 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-lg text-sm">去写文章</button>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="article in myArticles" :key="article.id"
                       @click="goToArticle(article.id)"
                       class="p-4 rounded-xl cursor-pointer transition-all hover:translate-x-1"
                       :class="isDark ? 'hover:bg-white/5' : 'hover:bg-gray-50'">
                    <div class="flex justify-between items-start">
                      <div class="flex-1">
                        <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold mb-1">{{ article.title }}</h4>
                        <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm line-clamp-2">{{ article.summary || article.content?.substring(0, 100) }}</p>
                        <div class="flex items-center gap-4 mt-2 text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span>❤️ {{ article.likeCount || 0 }}</span>
                          <span>💬 {{ article.commentCount || 0 }}</span>
                          <span>👁️ {{ article.viewCount || 0 }}</span>
                          <span>{{ formatDate(article.createdAt) }}</span>
                        </div>
                      </div>
                      <div class="flex items-center gap-1">
                        <span class="px-2 py-1 rounded text-xs" :class="article.status === 1 ? 'bg-green-100 text-green-600' : 'bg-yellow-100 text-yellow-600'">
                          {{ article.status === 1 ? '已发布' : '草稿' }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 我的点赞 -->
              <div v-if="activeRightTab === 'likes'">
                <div v-if="likesLoading" class="text-center py-8">
                  <div class="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
                </div>
                <div v-else-if="likedArticles.length === 0" class="text-center py-12">
                  <div class="text-5xl mb-3">❤️</div>
                  <p :class="isDark ? 'text-gray-400' : 'text-gray-500'">还没有点赞过任何文章</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="article in likedArticles" :key="article.id"
                       @click="goToArticle(article.id)"
                       class="p-4 rounded-xl cursor-pointer transition-all hover:translate-x-1"
                       :class="isDark ? 'hover:bg-white/5' : 'hover:bg-gray-50'">
                    <div class="flex items-start gap-3">
                      <div class="text-2xl">❤️</div>
                      <div class="flex-1">
                        <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold mb-1">{{ article.title }}</h4>
                        <div class="flex items-center gap-3 text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span>👤 {{ article.authorName || '匿名' }}</span>
                          <span>{{ formatDate(article.createdAt) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 我的收藏 -->
              <div v-if="activeRightTab === 'favorites'">
                <div v-if="favoritesLoading" class="text-center py-8">
                  <div class="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
                </div>
                <div v-else-if="favoritedArticles.length === 0" class="text-center py-12">
                  <div class="text-5xl mb-3">⭐</div>
                  <p :class="isDark ? 'text-gray-400' : 'text-gray-500'">还没有收藏过任何文章</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="article in favoritedArticles" :key="article.id"
                       @click="goToArticle(article.id)"
                       class="p-4 rounded-xl cursor-pointer transition-all hover:translate-x-1"
                       :class="isDark ? 'hover:bg-white/5' : 'hover:bg-gray-50'">
                    <div class="flex items-start gap-3">
                      <div class="text-2xl">⭐</div>
                      <div class="flex-1">
                        <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold mb-1">{{ article.title }}</h4>
                        <div class="flex items-center gap-3 text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span>👤 {{ article.authorName || '匿名' }}</span>
                          <span>❤️ {{ article.likeCount || 0 }}</span>
                          <span>{{ formatDate(article.createdAt) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 我的评论 -->
              <div v-if="activeRightTab === 'comments'">
                <div v-if="commentsLoading" class="text-center py-8">
                  <div class="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
                </div>
                <div v-else-if="myComments.length === 0" class="text-center py-12">
                  <div class="text-5xl mb-3">💬</div>
                  <p :class="isDark ? 'text-gray-400' : 'text-gray-500'">还没有发表过评论</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="comment in myComments" :key="comment.id"
                       @click="goToArticle(comment.contentId)"
                       class="p-4 rounded-xl cursor-pointer transition-all hover:translate-x-1"
                       :class="isDark ? 'hover:bg-white/5' : 'hover:bg-gray-50'">
                    <div class="flex items-start gap-3">
                      <div class="text-2xl">💬</div>
                      <div class="flex-1">
                        <p :class="isDark ? 'text-gray-300' : 'text-gray-700'" class="text-sm mb-2">{{ comment.content }}</p>
                        <div class="flex items-center gap-3 text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span>📝 回复于: {{ comment.articleTitle || '文章' }}</span>
                          <span>{{ formatDate(comment.createdAt) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>

          <!-- 下方其他信息区域 - 热门推荐和友情链接 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">

            <!-- 热门推荐 -->
            <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white shadow-sm', 'rounded-2xl p-5 fade-in-up']">
              <h3 class="font-semibold mb-4 flex items-center gap-2" :class="isDark ? 'text-white' : 'text-gray-800'">
                🔥 热门推荐
              </h3>
              <div class="space-y-3">
                <div v-for="(article, idx) in hotArticles" :key="article.id"
                     @click="goToArticle(article.id)"
                     class="flex items-center justify-between p-2 rounded-lg cursor-pointer transition-all hover:translate-x-1"
                     :class="isDark ? 'hover:bg-white/5' : 'hover:bg-gray-50'">
                  <div class="flex items-center gap-3">
                    <span class="text-sm font-bold text-indigo-500 w-6">{{ idx + 1 }}</span>
                    <span :class="isDark ? 'text-gray-300' : 'text-gray-700'" class="text-sm">{{ article.title }}</span>
                  </div>
                  <div class="flex items-center gap-2 text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                    <span>👁️ {{ article.views }}</span>
                    <span>❤️ {{ article.likes }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 友情链接 -->
            <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white shadow-sm', 'rounded-2xl p-5 fade-in-up']">
              <h3 class="font-semibold mb-4 flex items-center gap-2" :class="isDark ? 'text-white' : 'text-gray-800'">
                🔗 友情链接
              </h3>
              <div class="flex flex-wrap gap-3">
                <a v-for="link in friendLinks" :key="link.name"
                   :href="link.url" target="_blank"
                   class="px-3 py-2 rounded-lg text-sm transition-all hover:scale-105"
                   :class="isDark ? 'bg-white/5 text-gray-300 hover:bg-indigo-500/20 hover:text-indigo-400' : 'bg-gray-50 text-gray-600 hover:bg-indigo-50 hover:text-indigo-600'">
                  {{ link.name }}
                </a>
              </div>

              <!-- 站点信息 -->
              <div class="mt-4 pt-4 border-t" :class="isDark ? 'border-white/10' : 'border-gray-100'">
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs text-center">
                  © 2026 拾光记 · 记录美好时光
                </p>
              </div>
            </div>

          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-in-up {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.fade-in-up.show {
  opacity: 1;
  transform: translateY(0);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.avatar-container {
  position: relative;
  transition: all 0.3s ease;
}

/* 内容区域固定高度和滚动 */
.content-scroll-area {
  height: 500px;
  overflow-y: auto;
  padding: 20px;
}

/* 自定义滚动条 */
.content-scroll-area::-webkit-scrollbar {
  width: 6px;
}

.content-scroll-area::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
}

.content-scroll-area::-webkit-scrollbar-thumb {
  background: linear-gradient(to bottom, #6366f1, #a855f7);
  border-radius: 10px;
}

.content-scroll-area::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(to bottom, #4f46e5, #9333ea);
}

/* 暗色模式滚动条 */
.dark .content-scroll-area::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

/* 全局滚动条美化 */
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

.dark ::-webkit-scrollbar-track {
  background: #1f2937;
}
</style>
