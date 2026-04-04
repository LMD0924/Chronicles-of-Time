<template>
  <div v-if="visible" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="close">
    <div class="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-2xl p-6 max-h-[80vh] overflow-y-auto">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-bold text-gray-800 dark:text-gray-200">题目详情</h3>
        <button @click="close" class="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
      <div v-if="record" class="space-y-4">
        <div>
          <h4 class="font-semibold text-gray-800 dark:text-gray-200 mb-2">题目</h4>
          <p class="text-gray-600 dark:text-gray-300">{{ record.question_title }}</p>
        </div>
        <div>
          <h4 class="font-semibold text-gray-800 dark:text-gray-200 mb-2">用户答案</h4>
          <p class="text-gray-600 dark:text-gray-300">{{ record.user_answer }}</p>
        </div>
        <div>
          <h4 class="font-semibold text-gray-800 dark:text-gray-200 mb-2">正确答案</h4>
          <p class="text-green-600 dark:text-green-400">{{ record.correct_answer }}</p>
        </div>
        <div>
          <h4 class="font-semibold text-gray-800 dark:text-gray-200 mb-2">答题信息</h4>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-500">科目：</span>
              <span class="text-gray-700 dark:text-gray-300">{{ record.subject_name }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">题型：</span>
              <span class="text-gray-700 dark:text-gray-300">{{ record.question_type }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">答题结果：</span>
              <span :class="record.is_correct === 1 ? 'text-green-600' : 'text-red-600'">{{ record.is_correct === 1 ? '正确' : '错误' }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">得分：</span>
              <span class="text-gray-700 dark:text-gray-300">{{ record.score }}分</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">答题日期：</span>
              <span class="text-gray-700 dark:text-gray-300">{{ record.answer_date }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">知识点：</span>
              <span class="text-gray-700 dark:text-gray-300">{{ record.knowledge_point || '未分类' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  record: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const close = () => {
  emit('close')
}
</script>

<style scoped>
/* 组件样式 */
</style>
