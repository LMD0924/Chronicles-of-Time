<!--
  文件说明：拾光记后台管理系统通用组件页面组件，承载通用组件场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { onMounted, ref, watch } from 'vue'

const props = defineProps({ value: { type: Number, required: true }, duration: { type: Number, default: 700 } })
const display = ref(0)
let rafId = 0

const animate = () => {
  cancelAnimationFrame(rafId)
  const start = display.value
  const diff = props.value - start
  const startTime = performance.now()
  const tick = (time) => {
    const progress = Math.min((time - startTime) / props.duration, 1)
    display.value = Math.round(start + diff * (1 - Math.pow(1 - progress, 3)))
    if (progress < 1) rafId = requestAnimationFrame(tick)
  }
  rafId = requestAnimationFrame(tick)
}

onMounted(animate)
watch(() => props.value, animate)
</script>

<template>
  <span>{{ display.toLocaleString() }}</span>
</template>
