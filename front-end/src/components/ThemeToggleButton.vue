<template>
  <button
    @click="handleToggle"
    class="z-50 w-10 h-10 rounded-full flex items-center justify-center shadow-lg transition-all duration-300 hover:scale-110"
    :class="[
      isDark ? 'bg-gray-700 text-white' : 'bg-white text-slate-800 border border-slate-200'
    ]"
    aria-label="切换主题"
  >
    <!-- 太阳/月亮图标 -->
    <svg v-if="!isDark" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
    </svg>
    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
    </svg>
  </button>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { getStoredTheme, toggleTheme, onThemeChange, ThemeType } from '@/utils/theme';

// 响应式主题状态
const isDark = ref(getStoredTheme() === ThemeType.DARK);

// 切换主题方法
const handleToggle = () => {
  toggleTheme(); // 调用全局切换方法
};

// 监听全局主题变化，同步组件状态
onMounted(() => {
  const stopListen = onThemeChange((theme) => {
    isDark.value = theme === ThemeType.DARK;
  });

  // 组件卸载时取消监听
  onUnmounted(() => stopListen());
});
</script>
