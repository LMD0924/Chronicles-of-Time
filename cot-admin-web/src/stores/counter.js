/**
 * 文件说明：拾光记后台管理系统全局状态脚本模块，封装全局状态相关的配置、状态、路由或工具逻辑。
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  const doubleCount = computed(() => count.value * 2)
  function increment() {
    count.value++
  }

  return { count, doubleCount, increment }
})
