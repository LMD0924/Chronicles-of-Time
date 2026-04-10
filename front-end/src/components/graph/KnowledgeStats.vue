<template>
  <div class="knowledge-stats">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
            <span class="w-2 h-2 bg-red-500 rounded-full"></span>
            待加强知识点
          </h3>
          <button @click="refreshWeak" class="text-gray-400 hover:text-gray-600">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
        </div>

        <div v-if="weakPointsLoading" class="flex justify-center py-8">
          <div class="w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div v-else-if="weakKnowledgePoints.length === 0" class="text-center py-8 text-gray-400">
          暂无数据，快去练习吧！
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="item in weakKnowledgePoints"
            :key="item.knowledgePoint"
            class="p-3 bg-gray-50 rounded-lg hover:shadow-md transition-shadow"
          >
            <div class="flex justify-between items-start mb-2">
              <div>
                <span class="font-medium text-gray-800">{{ item.knowledgePoint }}</span>
                <span class="text-xs text-gray-400 ml-2">{{ item.subjectName }}</span>
              </div>
              <span class="text-xs px-2 py-1 bg-red-100 text-red-600 rounded-full">
                错误 {{ item.wrongCount }} 次
              </span>
            </div>
            <div class="flex justify-between text-sm text-gray-500 mb-1">
              <span>正确率</span>
              <span :class="item.accuracyRate >= 60 ? 'text-green-600' : 'text-red-600'">
                {{ item.accuracyRate }}%
              </span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all"
                :class="item.accuracyRate >= 60 ? 'bg-green-500' : 'bg-red-500'"
                :style="{ width: `${item.accuracyRate}%` }"
              ></div>
            </div>
            <div class="flex justify-between mt-2 text-xs text-gray-400">
              <span>总答题: {{ item.totalCount }} 次</span>
              <span>正确: {{ item.correctCount }} 次</span>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-5">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
            <span class="w-2 h-2 bg-blue-500 rounded-full"></span>
            热门知识点
          </h3>
          <button @click="refreshHot" class="text-gray-400 hover:text-gray-600">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
        </div>

        <div v-if="hotPointsLoading" class="flex justify-center py-8">
          <div class="w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div v-else-if="hotKnowledgePoints.length === 0" class="text-center py-8 text-gray-400">
          暂无数据
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="(item, index) in hotKnowledgePoints"
            :key="item.knowledgePoint"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
          >
            <div class="flex items-center gap-3">
              <div class="w-6 h-6 rounded-full flex items-center justify-center text-sm font-medium"
                   :class="{
                     'bg-yellow-100 text-yellow-600': index === 0,
                     'bg-gray-200 text-gray-600': index === 1,
                     'bg-orange-100 text-orange-600': index === 2,
                     'bg-gray-100 text-gray-500': index >= 3
                   }">
                {{ index + 1 }}
              </div>
              <div>
                <div class="font-medium text-gray-800">{{ item.knowledgePoint }}</div>
                <div class="text-xs text-gray-400">{{ item.subjectName }}</div>
              </div>
            </div>
            <div class="text-right">
              <div class="text-sm font-medium text-blue-600">{{ item.totalCount }} 次</div>
              <div class="text-xs text-gray-400">{{ item.accuracyRate }}% 正确</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  userId: { type: Number, default: null }
})

const weakKnowledgePoints = ref([])
const hotKnowledgePoints = ref([])
const weakPointsLoading = ref(false)
const hotPointsLoading = ref(false)

const fetchWeakKnowledgePoints = async () => {
  weakPointsLoading.value = true
  try {
    let url = 'http://localhost:8500/api/graph/weak-knowledge?limit=5'
    if (props.userId) url += `&userId=${props.userId}`
    const res = await fetch(url)
    const data = await res.json()
    if (data.code === 200) weakKnowledgePoints.value = data.data
  } catch (e) {
  } finally {
    weakPointsLoading.value = false
  }
}

const fetchHotKnowledgePoints = async () => {
  hotPointsLoading.value = true
  try {
    const res = await fetch('http://localhost:8500/api/graph/hot-knowledge?limit=5')
    const data = await res.json()
    if (data.code === 200) hotKnowledgePoints.value = data.data
  } catch (e) {
  } finally {
    hotPointsLoading.value = false
  }
}

const refreshWeak = () => fetchWeakKnowledgePoints()
const refreshHot = () => fetchHotKnowledgePoints()

onMounted(() => {
  fetchWeakKnowledgePoints()
  fetchHotKnowledgePoints()
})

watch(() => props.userId, fetchWeakKnowledgePoints)
</script>
