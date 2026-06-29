<script setup>
/** 选课参考（本地示例，正式版需后端与合规审核） */
import { ref } from 'vue'
defineProps({ isDark: Boolean })
const tips = ref([
  { course: '大学英语视听说', rating: 4.8, hot: 128, comment: '作业适中，适合提分' },
  { course: 'Python 程序设计', rating: 4.6, hot: 96, comment: '实践多，推荐非科班' },
  { course: '艺术鉴赏', rating: 4.9, hot: 210, comment: '公选热门，注意抢课' },
])
</script>

<template>
  <div class="app-card-surface p-5">
    <p class="text-sm text-amber-600 dark:text-amber-400 mb-4">匿名选课参考（演示数据）。上线需后端审核与用户协议。</p>
    <ul class="space-y-3">
      <li v-for="(t, i) in tips" :key="i" class="p-3 rounded-xl border text-sm" :class="isDark ? 'border-dark-border' : 'border-zinc-100'">
        <div class="flex justify-between font-medium">
          <span>{{ t.course }}</span>
          <span class="text-brand-600">★ {{ t.rating }}</span>
        </div>
        <p class="text-zinc-500 mt-1">{{ t.comment }} · {{ t.hot }} 人选过</p>
      </li>
    </ul>
  </div>
</template>
