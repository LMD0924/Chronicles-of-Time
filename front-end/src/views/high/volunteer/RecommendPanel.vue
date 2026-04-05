<template>
  <div class="space-y-6">
    <!-- 综合推荐 -->
    <div :class="[isDark ? 'bg-gray-800/50' : 'bg-white', 'backdrop-blur-xl rounded-2xl p-6 shadow-lg', isDark ? 'border border-gray-700' : 'border border-gray-200']">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 bg-gradient-to-r from-blue-500 to-indigo-500 rounded-xl flex items-center justify-center">
          <span class="text-xl">🎯</span>
        </div>
        <div>
          <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-lg font-semibold">综合智能推荐</h2>
          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">根据您的分数和选科，智能推荐适合的大学和专业</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">📅 年份</label>
          <el-select v-model="recommendParams.year" class="w-full">
            <el-option v-for="y in [2025,2024,2023,2022]" :key="y" :value="y" :label="`${y}年`" />
          </el-select>
        </div>
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">📍 省份</label>
          <el-select v-model="recommendParams.province" class="w-full">
            <el-option v-for="p in provinces" :key="p" :value="p" />
          </el-select>
        </div>
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">📊 高考分数</label>
          <el-input-number v-model="recommendParams.score" :min="0" :max="750" class="w-full" />
        </div>
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">🏆 全省排名</label>
          <el-input-number v-model="recommendParams.rank" :min="0" class="w-full" />
        </div>
      </div>

      <div class="mb-6">
        <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">📚 选考科目</label>
        <div class="flex flex-wrap gap-3">
          <button
            v-for="subject in subjects"
            :key="subject"
            @click="toggleSubject(subject)"
            :class="[
              'px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200',
              recommendParams.subjects.includes(subject)
                ? 'bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow-lg'
                : isDark
                  ? 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            {{ subject }}
          </button>
        </div>
      </div>

      <el-button
        type="primary"
        size="large"
        @click="getRecommendations"
        :loading="loading"
        class="!w-full !bg-gradient-to-r !from-blue-500 !to-indigo-500 !border-0 !h-12"
      >
        <el-icon><MagicStick /></el-icon>
        {{ loading ? '分析中...' : '开始智能推荐' }}
      </el-button>
    </div>

    <!-- 按专业推荐 -->
    <div :class="[isDark ? 'bg-gray-800/50' : 'bg-white', 'backdrop-blur-xl rounded-2xl p-6 shadow-lg', isDark ? 'border border-gray-700' : 'border border-gray-200']">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 bg-gradient-to-r from-green-500 to-teal-500 rounded-xl flex items-center justify-center">
          <span class="text-xl">💼</span>
        </div>
        <div>
          <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-lg font-semibold">按专业推荐</h2>
          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">根据专业代码，查找开设该专业的院校</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">🔖 专业代码</label>
          <el-input v-model="recommendByMajorParams.majorCode" placeholder="如: 080901" />
        </div>
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">📊 高考分数</label>
          <el-input-number v-model="recommendByMajorParams.score" :min="0" :max="750" class="w-full" />
        </div>
        <div>
          <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">📍 省份</label>
          <el-select v-model="recommendByMajorParams.province" class="w-full">
            <el-option value="北京" /><el-option value="上海" /><el-option value="浙江" /><el-option value="广东" />
          </el-select>
        </div>
      </div>

      <el-button type="success" size="large" @click="getRecommendByMajor" class="!w-full !bg-gradient-to-r !from-green-500 !to-teal-500 !border-0 !h-12">
        <el-icon><School /></el-icon>
        按专业推荐
      </el-button>

      <!-- 按专业推荐结果 -->
      <div v-if="recommendByMajorResults.length > 0" class="mt-6 space-y-3">
        <div :class="isDark ? 'text-gray-300' : 'text-gray-600'" class="text-sm mb-2">共找到 {{ recommendByMajorResults.length }} 所院校</div>
        <div v-for="item in recommendByMajorResults" :key="item.universityId" :class="[
          isDark ? 'bg-gray-700/50 border-gray-600 hover:border-gray-500' : 'bg-gray-50 border-gray-200 hover:border-gray-300',
          'rounded-xl p-4 border transition'
        ]">
          <div class="flex flex-wrap justify-between items-center gap-4">
            <div>
              <h4 :class="isDark ? 'text-white' : 'text-gray-900'" class="font-semibold">{{ item.universityName }}</h4>
              <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.majorName }} · 录取线: {{ item.minScore }}分</p>
            </div>
            <div class="text-right">
              <span :class="[
                'px-3 py-1 rounded-full text-xs font-medium',
                item.strategy === '冲刺' ? 'bg-orange-500/20 text-orange-500' :
                item.strategy === '稳妥' ? 'bg-green-500/20 text-green-500' : 'bg-blue-500/20 text-blue-500'
              ]">
                {{ item.strategy }}
              </span>
              <div :class="getProbabilityColor(item.probability)" class="text-2xl font-bold mt-1">
                {{ (item.probability * 100).toFixed(0) }}%
              </div>
              <el-button text size="small" @click="getChance(item.universityId, item.majorId)" class="!text-blue-500 mt-1">
                机会分析 →
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐结果 -->
    <div v-if="recommendations.length > 0" class="space-y-6">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 bg-gradient-to-r from-purple-500 to-pink-500 rounded-xl flex items-center justify-center">
          <span class="text-xl">✨</span>
        </div>
        <div>
          <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-lg font-semibold">智能推荐结果</h2>
          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">基于您的分数和选科，为您推荐以下院校</p>
        </div>
      </div>

      <!-- 冲刺志愿 -->
      <div v-if="groupedRecommendations.冲刺?.length" :class="[isDark ? 'bg-orange-500/10 border-orange-500/30' : 'bg-orange-50 border-orange-200', 'rounded-2xl p-5 border']">
        <h3 class="text-lg font-semibold text-orange-500 mb-4 flex items-center gap-2">
          <span>🚀</span> 冲刺志愿 <span :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">({{ groupedRecommendations.冲刺.length }}个)</span>
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="item in groupedRecommendations.冲刺" :key="item.universityId" :class="[
            isDark ? 'bg-gray-700/50 border-gray-600 hover:border-orange-500/30' : 'bg-white border-gray-200 hover:border-orange-300',
            'rounded-xl p-4 border transition'
          ]">
            <h4 :class="isDark ? 'text-white' : 'text-gray-900'" class="font-semibold">{{ item.universityName }}</h4>
            <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.majorName }}</p>
            <div class="flex justify-between items-center mt-3">
              <div>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">录取线: {{ item.minScore }}分</p>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">分差: {{ item.scoreDiff }}分</p>
              </div>
              <div class="text-right">
                <div class="text-xl font-bold text-orange-500">{{ (item.probability * 100).toFixed(0) }}%</div>
                <el-button size="small" @click="addToPlan(item)" class="!mt-2">
                  添加
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 稳妥志愿 -->
      <div v-if="groupedRecommendations.稳妥?.length" :class="[isDark ? 'bg-green-500/10 border-green-500/30' : 'bg-green-50 border-green-200', 'rounded-2xl p-5 border']">
        <h3 class="text-lg font-semibold text-green-500 mb-4 flex items-center gap-2">
          <span>✅</span> 稳妥志愿 <span :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">({{ groupedRecommendations.稳妥.length }}个)</span>
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="item in groupedRecommendations.稳妥" :key="item.universityId" :class="[
            isDark ? 'bg-gray-700/50 border-gray-600 hover:border-green-500/30' : 'bg-white border-gray-200 hover:border-green-300',
            'rounded-xl p-4 border transition'
          ]">
            <h4 :class="isDark ? 'text-white' : 'text-gray-900'" class="font-semibold">{{ item.universityName }}</h4>
            <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.majorName }}</p>
            <div class="flex justify-between items-center mt-3">
              <div>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">录取线: {{ item.minScore }}分</p>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">分差: {{ item.scoreDiff }}分</p>
              </div>
              <div class="text-right">
                <div class="text-xl font-bold text-green-500">{{ (item.probability * 100).toFixed(0) }}%</div>
                <el-button size="small" @click="addToPlan(item)" class="!mt-2">
                  添加
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 保底志愿 -->
      <div v-if="groupedRecommendations.保底?.length" :class="[isDark ? 'bg-blue-500/10 border-blue-500/30' : 'bg-blue-50 border-blue-200', 'rounded-2xl p-5 border']">
        <h3 class="text-lg font-semibold text-blue-500 mb-4 flex items-center gap-2">
          <span>🛡️</span> 保底志愿 <span :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">({{ groupedRecommendations.保底.length }}个)</span>
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="item in groupedRecommendations.保底" :key="item.universityId + '-' + item.majorId" :class="[
            isDark ? 'bg-gray-700/50 border-gray-600 hover:border-blue-500/30' : 'bg-white border-gray-200 hover:border-blue-300',
            'rounded-xl p-4 border transition'
          ]">
            <h4 :class="isDark ? 'text-white' : 'text-gray-900'" class="font-semibold">{{ item.universityName }}</h4>
            <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.majorName }}</p>
            <div class="flex justify-between items-center mt-3">
              <div>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">录取线: {{ item.minScore }}分</p>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">分差: {{ item.scoreDiff }}分</p>
              </div>
              <div class="text-right">
                <div class="text-xl font-bold text-blue-500">{{ (item.probability * 100).toFixed(0) }}%</div>
                <el-button size="small" @click="addToPlan(item)" class="!mt-2">
                  添加
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 梦想志愿（分差较大，仅供参考） -->
      <div v-if="groupedRecommendations.梦想?.length" :class="[isDark ? 'bg-violet-500/10 border-violet-500/30' : 'bg-violet-50 border-violet-200', 'rounded-2xl p-5 border']">
        <h3 class="text-lg font-semibold text-violet-500 mb-4 flex items-center gap-2">
          <span>✨</span> 梦想志愿 <span :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">({{ groupedRecommendations.梦想.length }}个)</span>
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="item in groupedRecommendations.梦想" :key="item.universityId + '-' + item.majorId" :class="[
            isDark ? 'bg-gray-700/50 border-gray-600 hover:border-violet-500/30' : 'bg-white border-gray-200 hover:border-violet-300',
            'rounded-xl p-4 border transition'
          ]">
            <h4 :class="isDark ? 'text-white' : 'text-gray-900'" class="font-semibold">{{ item.universityName }}</h4>
            <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.majorName }}</p>
            <div class="flex justify-between items-center mt-3">
              <div>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">录取线: {{ item.minScore }}分</p>
                <p :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-xs">分差: {{ item.scoreDiff }}分</p>
              </div>
              <div class="text-right">
                <div class="text-xl font-bold text-violet-500">{{ (item.probability * 100).toFixed(0) }}%</div>
                <el-button size="small" @click="addToPlan(item)" class="!mt-2">
                  添加
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, School } from '@element-plus/icons-vue'
import request from '@/utils/request'

const isDark = inject('isDark', ref(false))

const props = defineProps({
  userId: {
    type: Number,
    default: 1
  }
})

const subjects = ['物理', '化学', '生物', '历史', '政治', '地理']
const provinces = ref([])

const loading = ref(false)
const recommendations = ref([])
const recommendParams = ref({ userId: props.userId, year: 2025, province: '浙江', score: null, rank: null, subjects: [] })
const recommendByMajorParams = ref({ userId: props.userId, majorCode: '', score: null, province: '浙江' })
const recommendByMajorResults = ref([])

const groupedRecommendations = computed(() => ({
  '冲刺': recommendations.value.filter(r => r.strategy === '冲刺'),
  '稳妥': recommendations.value.filter(r => r.strategy === '稳妥'),
  '保底': recommendations.value.filter(r => r.strategy === '保底'),
  // 后端分差过大时标记为「梦想」，原先未展示导致「有数据却像空白」
  '梦想': recommendations.value.filter(r => r.strategy === '梦想')
}))

const getProbabilityColor = (prob) => {
  if (prob >= 0.7) return 'text-green-500'
  if (prob >= 0.4) return 'text-yellow-500'
  return 'text-red-500'
}

const toggleSubject = (subject) => {
  const index = recommendParams.value.subjects.indexOf(subject)
  if (index > -1) {
    recommendParams.value.subjects.splice(index, 1)
  } else {
    recommendParams.value.subjects.push(subject)
  }
}

const getRecommendations = async () => {
  if (!recommendParams.value.score) {
    ElMessage.warning('请填写高考分数')
    return
  }
  loading.value = true
  try {
    const queryParams = {
      userId: recommendParams.value.userId,
      year: recommendParams.value.year,
      province: recommendParams.value.province,
      score: recommendParams.value.score,
      rank: recommendParams.value.rank || 0
    }
    const res = await request.post(
      '/volunteer/recommend/universities',
      recommendParams.value.subjects,
      queryParams
    )
    if (res.code === 200) {
      recommendations.value = res.data || []
      if (recommendations.value.length === 0) ElMessage.warning('未找到匹配的推荐结果')
    }
  } catch (error) {
    console.error('获取推荐失败', error)
  } finally {
    loading.value = false
  }
}

const getRecommendByMajor = async () => {
  if (!recommendByMajorParams.value.majorCode || !recommendByMajorParams.value.score) {
    ElMessage.warning('请填写专业代码和分数')
    return
  }
  try {
    const res = await request.get('/volunteer/recommend/byMajor', recommendByMajorParams.value)
    if (res.code === 200) {
      recommendByMajorResults.value = res.data || []
      if (recommendByMajorResults.value.length === 0) ElMessage.warning('未找到相关专业推荐')
    }
  } catch (error) {
    console.error('按专业推荐失败', error)
  }
}

const getChance = async (universityId, majorId) => {
  try {
    const res = await request.get(`/volunteer/chance/${props.userId}`, { universityId, majorId })
    if (res.code === 200) {
      const chance = res.data
      ElMessageBox.alert(
        `<div class="space-y-2">
          <h4 class="font-bold text-lg">📈 录取机会分析</h4>
          <p>录取概率：<span class="font-bold text-blue-500">${(chance.probability * 100).toFixed(0)}%</span></p>
          <p>等级：<span class="font-bold">${chance.level || ''}</span></p>
          <p>你的分数：${chance.yourScore}分 | 目标分数：${chance.targetScore}分</p>
          <p>分差：${chance.scoreDiff}分</p>
          <p class="mt-2 text-sm">建议：${chance.suggestion || ''}</p>
        </div>`,
        '机会分析',
        { dangerouslyUseHTMLString: true }
      )
    }
  } catch (error) {
    console.error('获取机会分析失败', error)
  }
}

const addToPlan = async (item) => {
  ElMessage.info(`已将 ${item.universityName} - ${item.majorName} 添加到志愿，请在"志愿方案"页面完善`)
}

const loadFilters = async () => {
  try {
    const res = await request.get('/volunteer/filter/provinces')
    if (res.code === 200) {
      provinces.value = res.data || []
    }
  } catch (error) {
    console.error('加载筛选条件失败', error)
  }
}

onMounted(() => {
  loadFilters()
})

defineExpose({
  getRecommendations
})
</script>

<style scoped>
</style>
