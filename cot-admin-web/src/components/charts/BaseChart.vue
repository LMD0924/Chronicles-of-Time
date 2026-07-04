<!--
  文件说明：拾光记后台管理系统通用组件页面组件，承载通用组件场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' },
})

const chartRef = ref(null)
let chart
let observer

const render = () => {
  if (!chart && chartRef.value) chart = echarts.init(chartRef.value)
  chart?.setOption(props.option, true)
}

onMounted(() => {
  render()
  observer = new ResizeObserver(() => chart?.resize())
  observer.observe(chartRef.value)
})

watch(() => props.option, render, { deep: true })

onBeforeUnmount(() => {
  observer?.disconnect()
  chart?.dispose()
})
</script>

<template>
  <div ref="chartRef" class="chart-box" :style="{ height }"></div>
</template>

<style scoped>
.chart-box {
  width: 100%;
}
</style>
