<template>
  <div :class="[isDark ? 'dark' : '', 'min-h-screen overflow-x-hidden']">
    <div :class="[
      isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900',
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
            <!-- Logo -->
            <div class="flex items-center gap-3 cursor-pointer group" @click="goBack">
              <div class="relative">
                <div class="absolute inset-0 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl blur-lg opacity-0 group-hover:opacity-50 transition-opacity duration-500"></div>
                <div class="relative w-9 h-9 lg:w-10 lg:h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
                  <span class="text-xl lg:text-2xl">🎓</span>
                </div>
              </div>
              <div class="flex items-baseline gap-1">
                <span class="text-xl lg:text-2xl font-bold" :class="isDark ? 'text-white' : 'bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent'">
                  大学规划
                </span>
                <span class="hidden lg:inline text-xs font-medium text-gray-400 tracking-wider">University Plan</span>
              </div>
            </div>

            <!-- 右侧用户区域 -->
            <div class="flex items-center gap-4">
              <ThemeToggleButton />
              <div class="flex items-center gap-2 cursor-pointer group" @click="goToProfile">
                <div class="relative w-9 h-9 rounded-full overflow-hidden border-2 border-indigo-200 group-hover:border-indigo-400 transition-colors">
                  <img :src="UserInfo.avatar || 'https://via.placeholder.com/36'" alt="User Avatar" class="w-full h-full object-cover">
                </div>
                <span class="hidden md:inline text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ UserInfo.name || '用户' }}</span>
              </div>
            </div>
          </div>
        </div>
      </nav>

      <!-- 主内容区域 -->
      <div class="pt-24 pb-16 px-6 lg:px-8">
        <div class="max-w-4xl mx-auto">
          <!-- 页面标题 -->
          <div class="text-center mb-10 scroll-animate">
            <div class="inline-flex items-center gap-2 px-4 py-2 bg-indigo-50 rounded-full text-indigo-600 text-sm mb-4">
              <span>📝</span>
              <span>填写信息 · 规划未来</span>
            </div>
            <h1 class="text-3xl lg:text-4xl font-bold mb-3" :class="isDark ? 'text-white' : 'text-gray-900'">
              填写大学与专业信息
            </h1>
            <p class="text-gray-500">告诉我们你的大学和专业，让我们一起规划精彩的大学生活</p>
          </div>

          <!-- 表单卡片 -->
          <div class="rounded-2xl shadow-xl overflow-hidden scroll-animate" :class="isDark ? 'bg-white/10' : 'bg-white'">
            <div class="p-6 lg:p-8">
              <!-- 表单 -->
              <form @submit.prevent="handleSubmit" class="space-y-6">
                <!-- 大学选择 -->
                <div>
                  <label class="block text-sm font-medium mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-700'">
                    大学名称 <span class="text-red-500">*</span>
                  </label>
                  <div class="relative">
                    <input
                      v-model="formData.university"
                      type="text"
                      placeholder="请输入或选择你的大学"
                      class="w-full px-4 py-3 rounded-xl border transition-all focus:outline-none focus:ring-2 focus:ring-indigo-400"
                      :class="isDark ? 'bg-gray-800 border-gray-700 text-white focus:border-indigo-400' : 'bg-gray-50 border-gray-200 focus:border-indigo-400'"
                      @focus="showUniversitySuggestions = true"
                      @blur="handleUniversityBlur"
                    />
                    <!-- 大学建议列表 -->
                    <div v-if="showUniversitySuggestions && filteredUniversities.length > 0"
                         class="absolute top-full left-0 right-0 mt-1 rounded-xl shadow-lg z-10 max-h-48 overflow-y-auto"
                         :class="isDark ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'">
                      <div v-for="uni in filteredUniversities" :key="uni"
                           @mousedown.prevent="selectUniversity(uni)"
                           class="px-4 py-2 cursor-pointer hover:bg-indigo-50 transition-colors"
                           :class="isDark ? 'hover:bg-gray-700 text-gray-300' : 'hover:bg-indigo-50 text-gray-700'">
                        {{ uni }}
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 专业选择 -->
                <div>
                  <label class="block text-sm font-medium mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-700'">
                    专业名称 <span class="text-red-500">*</span>
                  </label>
                  <div class="relative">
                    <input
                      v-model="formData.major"
                      type="text"
                      placeholder="请输入或选择你的专业"
                      class="w-full px-4 py-3 rounded-xl border transition-all focus:outline-none focus:ring-2 focus:ring-indigo-400"
                      :class="isDark ? 'bg-gray-800 border-gray-700 text-white focus:border-indigo-400' : 'bg-gray-50 border-gray-200 focus:border-indigo-400'"
                      @focus="showMajorSuggestions = true"
                      @blur="handleMajorBlur"
                    />
                    <!-- 专业建议列表 -->
                    <div v-if="showMajorSuggestions && filteredMajors.length > 0"
                         class="absolute top-full left-0 right-0 mt-1 rounded-xl shadow-lg z-10 max-h-48 overflow-y-auto"
                         :class="isDark ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'">
                      <div v-for="major in filteredMajors" :key="major"
                           @mousedown.prevent="selectMajor(major)"
                           class="px-4 py-2 cursor-pointer hover:bg-indigo-50 transition-colors"
                           :class="isDark ? 'hover:bg-gray-700 text-gray-300' : 'hover:bg-indigo-50 text-gray-700'">
                        {{ major }}
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 发展规划选择 -->
                <div>
                  <label class="block text-sm font-medium mb-3" :class="isDark ? 'text-gray-300' : 'text-gray-700'">
                    发展规划 <span class="text-red-500">*</span>
                  </label>
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <div
                      v-for="option in careerOptions"
                      :key="option.value"
                      @click="selectCareer(option.value)"
                      class="cursor-pointer rounded-xl p-4 text-center transition-all hover:-translate-y-1"
                      :class="[
                        formData.choose === option.value
                          ? option.activeClass
                          : isDark
                            ? 'bg-gray-800 border border-gray-700 hover:border-indigo-400'
                            : 'bg-gray-50 border border-gray-200 hover:border-indigo-400'
                      ]"
                    >
                      <div class="text-2xl mb-2">{{ option.icon }}</div>
                      <div class="font-medium text-sm" :class="formData.choose === option.value ? 'text-white' : (isDark ? 'text-gray-300' : 'text-gray-700')">
                        {{ option.label }}
                      </div>
                      <div class="text-xs mt-1" :class="formData.choose === option.value ? 'text-white/70' : 'text-gray-400'">
                        {{ option.desc }}
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 额外信息（根据选择动态显示） -->
                <div v-if="formData.choose" class="rounded-xl p-4" :class="isDark ? 'bg-indigo-900/30 border border-indigo-800' : 'bg-indigo-50 border border-indigo-100'">
                  <div class="flex items-start gap-3">
                    <div class="text-xl">{{ getCareerIcon(formData.choose) }}</div>
                    <div>
                      <p class="text-sm font-medium mb-1" :class="isDark ? 'text-indigo-300' : 'text-indigo-700'">
                        {{ getCareerTip(formData.choose) }}
                      </p>
                      <p class="text-xs" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
                        {{ getCareerDescription(formData.choose) }}
                      </p>
                    </div>
                  </div>
                </div>

                <!-- 提交按钮 -->
                <div class="flex gap-4 pt-4">
                  <button
                    type="button"
                    @click="goBack"
                    class="flex-1 px-6 py-3 rounded-xl font-medium transition-all border"
                    :class="isDark ? 'border-gray-700 text-gray-300 hover:bg-gray-800' : 'border-gray-200 text-gray-600 hover:bg-gray-50'"
                  >
                    返回
                  </button>
                  <button
                    type="submit"
                    :disabled="loading"
                    class="flex-1 px-6 py-3 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-xl font-medium shadow-lg hover:shadow-xl transition-all hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span v-if="loading" class="flex items-center justify-center gap-2">
                      <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      提交中...
                    </span>
                    <span v-else>保存规划信息</span>
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- 已填写信息展示 -->
          <div v-if="existingInfo" class="mt-8 rounded-xl p-5 scroll-animate" :class="isDark ? 'bg-white/5 border border-gray-800' : 'bg-indigo-50 border border-indigo-100'">
            <div class="flex items-center justify-between mb-3">
              <h3 class="font-semibold flex items-center gap-2" :class="isDark ? 'text-white' : 'text-gray-800'">
                <span>📋</span> 已填写的规划信息
              </h3>
              <button @click="editInfo" class="text-sm text-indigo-500 hover:text-indigo-600">编辑</button>
            </div>
            <div class="flex flex-wrap gap-4 text-sm">
              <div><span class="text-gray-500">大学：</span><span class="font-medium">{{ existingInfo.university }}</span></div>
              <div><span class="text-gray-500">专业：</span><span class="font-medium">{{ existingInfo.major }}</span></div>
              <div><span class="text-gray-500">规划方向：</span>
                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs" :class="getCareerBadgeClass(existingInfo.choose)">
                  {{ getCareerLabel(existingInfo.choose) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 返回顶部按钮 -->
      <button v-show="showBackTop" @click="scrollToTop" class="fixed bottom-10 right-10 w-11 h-11 bg-white rounded-full shadow-lg flex items-center justify-center hover:-translate-y-1 transition-all z-50">
        <svg class="w-5 h-5 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggleButton from '@/components/ThemeToggleButton.vue'
import { getStoredTheme, ThemeType, onThemeChange } from "@/utils/theme.js"
import request from "@/utils/request.js"
import { message } from "ant-design-vue"

const router = useRouter()
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const isScrolled = ref(false)
const showBackTop = ref(false)
const loading = ref(false)
const UserInfo = ref({})

// 表单数据
const formData = ref({
  university: '',
  major: '',
  choose: ''
})

// 已存在的信息
const existingInfo = ref(null)

// 建议列表
const showUniversitySuggestions = ref(false)
const showMajorSuggestions = ref(false)

// 大学数据库（常见大学）
const commonUniversities = [
  '清华大学', '北京大学', '复旦大学', '上海交通大学', '浙江大学', '中国科学技术大学',
  '南京大学', '西安交通大学', '哈尔滨工业大学', '华中科技大学', '武汉大学', '中山大学',
  '四川大学', '南开大学', '天津大学', '山东大学', '东南大学', '吉林大学', '厦门大学',
  '同济大学', '北京师范大学', '国防科技大学', '中国人民大学', '兰州大学', '西北工业大学',
  '电子科技大学', '中国农业大学', '湖南大学', '中南大学', '华东师范大学', '华南理工大学'
]

// 专业数据库
const commonMajors = [
  '计算机科学与技术', '软件工程', '人工智能', '数据科学与大数据技术', '电子信息工程',
  '通信工程', '自动化', '机械工程', '电气工程及其自动化', '土木工程', '建筑学',
  '化学工程与工艺', '材料科学与工程', '环境工程', '生物工程', '药学', '临床医学',
  '口腔医学', '护理学', '经济学', '金融学', '国际经济与贸易', '工商管理', '会计学',
  '市场营销', '人力资源管理', '行政管理', '法学', '汉语言文学', '英语', '日语',
  '新闻学', '广告学', '广播电视学', '视觉传达设计', '环境设计', '产品设计', '动画'
]

// 发展规划选项
const careerOptions = [
  {
    value: 'employment',
    label: '就业',
    icon: '💼',
    desc: '直接就业，进入职场',
    activeClass: 'bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-lg'
  },
  {
    value: 'postgraduate',
    label: '考研',
    icon: '📚',
    desc: '继续深造，攻读硕士',
    activeClass: 'bg-gradient-to-r from-green-500 to-green-600 text-white shadow-lg'
  },
  {
    value: 'civil_servant',
    label: '考公',
    icon: '🏛️',
    desc: '公务员/事业单位',
    activeClass: 'bg-gradient-to-r from-amber-500 to-amber-600 text-white shadow-lg'
  },
  {
    value: 'internship',
    label: '实习',
    icon: '🎯',
    desc: '积累实习经验',
    activeClass: 'bg-gradient-to-r from-purple-500 to-purple-600 text-white shadow-lg'
  },
  {
    value: 'abroad',
    label: '留学',
    icon: '🌍',
    desc: '出国留学深造',
    activeClass: 'bg-gradient-to-r from-pink-500 to-pink-600 text-white shadow-lg'
  },
  {
    value: 'entrepreneurship',
    label: '创业',
    icon: '🚀',
    desc: '自主创业',
    activeClass: 'bg-gradient-to-r from-red-500 to-red-600 text-white shadow-lg'
  }
]

// 过滤后的大学列表
const filteredUniversities = computed(() => {
  if (!formData.value.university) return commonUniversities
  return commonUniversities.filter(uni =>
    uni.toLowerCase().includes(formData.value.university.toLowerCase())
  )
})

// 过滤后的专业列表
const filteredMajors = computed(() => {
  if (!formData.value.major) return commonMajors
  return commonMajors.filter(major =>
    major.toLowerCase().includes(formData.value.major.toLowerCase())
  )
})

// 选择大学
const selectUniversity = (uni) => {
  formData.value.university = uni
  showUniversitySuggestions.value = false
}

// 选择专业
const selectMajor = (major) => {
  formData.value.major = major
  showMajorSuggestions.value = false
}

// 处理失焦
const handleUniversityBlur = () => {
  setTimeout(() => {
    showUniversitySuggestions.value = false
  }, 200)
}

const handleMajorBlur = () => {
  setTimeout(() => {
    showMajorSuggestions.value = false
  }, 200)
}

// 选择发展规划
const selectCareer = (value) => {
  formData.value.choose = value
}

// 获取职业图标
const getCareerIcon = (value) => {
  const option = careerOptions.find(opt => opt.value === value)
  return option ? option.icon : '🎯'
}

// 获取职业提示
const getCareerTip = (value) => {
  const tips = {
    employment: '💡 小贴士：建议从大二开始准备实习，积累项目经验，完善简历。',
    postgraduate: '💡 小贴士：提前了解目标院校，制定复习计划，大三开始全力备考。',
    civil_servant: '💡 小贴士：关注国考省考时间，提前准备行测和申论。',
    internship: '💡 小贴士：寒暑假是积累实习的好时机，建议至少完成2-3份高质量实习。',
    abroad: '💡 小贴士：提前准备语言考试（雅思/托福），保持高GPA，丰富课外经历。',
    entrepreneurship: '💡 小贴士：参加创业比赛积累经验，寻找志同道合的伙伴。'
  }
  return tips[value] || '💡 提前规划，让你的大学生活更有方向。'
}

// 获取职业描述
const getCareerDescription = (value) => {
  const descs = {
    employment: '毕业后直接进入企业工作，建议提前积累实习经验和专业技能。',
    postgraduate: '继续攻读硕士研究生，提升学术水平和专业深度。',
    civil_servant: '参加公务员或事业单位考试，进入体制内工作。',
    internship: '通过实习积累工作经验，为正式求职打下基础。',
    abroad: '申请海外院校，拓展国际视野和学术背景。',
    entrepreneurship: '自主创业，实现自我价值和社会价值。'
  }
  return descs[value] || ''
}

// 获取职业标签
const getCareerLabel = (value) => {
  const option = careerOptions.find(opt => opt.value === value)
  return option ? option.label : value
}

// 获取职业徽章样式
const getCareerBadgeClass = (value) => {
  const classes = {
    employment: 'bg-blue-100 text-blue-700',
    postgraduate: 'bg-green-100 text-green-700',
    civil_servant: 'bg-amber-100 text-amber-700',
    internship: 'bg-purple-100 text-purple-700',
    abroad: 'bg-pink-100 text-pink-700',
    entrepreneurship: 'bg-red-100 text-red-700'
  }
  return classes[value] || 'bg-gray-100 text-gray-700'
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const res = await request.get('/user/getUserById')
    if (res && res.data) {
      UserInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 获取已填写的用户信息
const getUserInfoData = async () => {
  try {
    const res = await request.get('/api/userInfo/getCurrent')
    if (res && res.data && res.data.code === 200 && res.data.data) {
      existingInfo.value = res.data.data
      // 如果已有信息，回填到表单
      if (res.data.data.university) formData.value.university = res.data.data.university
      if (res.data.data.major) formData.value.major = res.data.data.major
      if (res.data.data.choose) formData.value.choose = res.data.data.choose
    }
  } catch (error) {
    // 没有记录是正常的
    console.log('暂无用户规划信息')
  }
}

// 提交表单
const handleSubmit = async () => {
  // 验证
  if (!formData.value.university) {
    message.warning('请输入大学名称')
    return
  }
  if (!formData.value.major) {
    message.warning('请输入专业名称')
    return
  }
  if (!formData.value.choose) {
    message.warning('请选择发展规划')
    return
  }

  loading.value = true
  try {
    let res
    if (existingInfo.value && existingInfo.value.id) {
      // 更新
      res = await request.put('/api/userInfo/update', {
        id: existingInfo.value.id,
        university: formData.value.university,
        major: formData.value.major,
        choose: formData.value.choose
      })
    } else {
      // 新增
      res = await request.post('/api/userInfo/add', {
        university: formData.value.university,
        major: formData.value.major,
        choose: formData.value.choose
      })
    }

    if (res && res.data && res.data.code === 200) {
      message.success(res.data.message || '保存成功')
      // 刷新数据
      await getUserInfoData()
    } else {
      message.error(res?.data?.message || '保存失败')
    }
  } catch (error) {
    console.error('提交失败', error)
    message.error('提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 编辑信息
const editInfo = () => {
  // 清空表单，让用户重新填写
  // 但保留已有值作为默认
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到个人资料
const goToProfile = () => {
  router.push('/PersonalProfile')
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 滚动监听
const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
  showBackTop.value = window.scrollY > 500
}

// 主题变化监听
onMounted(() => {
  getUserInfo()
  getUserInfoData()
  window.addEventListener('scroll', handleScroll)
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  }

  const stopListen = onThemeChange((theme) => {
    isDark.value = theme === ThemeType.DARK
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  })

  onUnmounted(() => {
    stopListen()
    window.removeEventListener('scroll', handleScroll)
  })
})
</script>

<style scoped>
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

.scroll-animate {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.scroll-animate.animated {
  opacity: 1;
  transform: translateY(0);
}

/* 滚动动画自动触发 */
.scroll-animate {
  animation: fadeInUp 0.6s ease-out forwards;
}
</style>
