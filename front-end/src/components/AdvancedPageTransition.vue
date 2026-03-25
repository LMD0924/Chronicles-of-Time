<!-- components/AdvancedPageTransition.vue -->
<template>
  <div v-if="isVisible" class="advanced-transition-overlay" :class="overlayClass">
    <!-- 顶部进度条 -->
    <div class="progress-bar-container">
      <div class="progress-bar" :style="{ width: `${progress}%` }"></div>
    </div>

    <!-- 主内容：3D魔方 -->
    <div class="transition-content">
      <div class="cube-wrapper">
        <!-- 3D魔方 - 放大版 -->
        <div class="cube">
          <div class="cube-face front"></div>
          <div class="cube-face back"></div>
          <div class="cube-face left"></div>
          <div class="cube-face right"></div>
          <div class="cube-face top"></div>
          <div class="cube-face bottom"></div>
        </div>
      </div>

      <p class="loading-text">{{ currentText }}</p>

      <div class="dots">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  duration: {
    type: Number,
    default: 10000
  },
  texts: {
    type: Array,
    default: () => ['加载中', '即将完成', '马上就好']
  },
  overlayClass: {
    type: String,
    default: ''
  }
})

const isVisible = ref(false)
const progress = ref(0)
const currentText = ref(props.texts[0])
let timer = null
let progressTimer = null

const show = () => {
  isVisible.value = true
  startProgress()
}

const hide = () => {
  isVisible.value = false
  reset()
}

const startProgress = () => {
  progress.value = 0
  let textIndex = 0

  progressTimer = setInterval(() => {
    if (progress.value < 100) {
      progress.value += 1.5  // 从2改为1.5，进度更慢
      const newIndex = Math.floor(progress.value / 33)
      if (newIndex < props.texts.length && newIndex !== textIndex) {
        textIndex = newIndex
        currentText.value = props.texts[newIndex]
      }
    } else {
      clearInterval(progressTimer)
      timer = setTimeout(() => hide(), 500)  // 从300改为500，停留时间更长
    }
  }, props.duration / 66)  // 调整间隔，让整体时间更长
}

const reset = () => {
  if (progressTimer) clearInterval(progressTimer)
  if (timer) clearTimeout(timer)
  progress.value = 0
  currentText.value = props.texts[0]
}

defineExpose({ show, hide })

onUnmounted(() => reset())
</script>

<style scoped>
/* 笼罩层背景 */
.advanced-transition-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  animation: fadeIn 0.6s ease;  /* 从0.4s改为0.6s，进入更慢 */
}

/* 进度条 */
.progress-bar-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;  /* 从3px改为4px，稍粗一点 */
  background: rgba(255, 255, 255, 0.15);
}
.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.2s ease;  /* 从0.15s改为0.2s，过渡更平滑 */
  box-shadow: 0 0 12px rgba(102, 126, 234, 0.6);
}

/* 内容布局 */
.transition-content {
  text-align: center;
  animation: bounceIn 0.7s ease;  /* 从0.5s改为0.7s，进入更慢 */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;  /* 从28px改为32px，间距稍大 */
}

/* ====================== 3D魔方核心样式 - 放大版 ====================== */
.cube-wrapper {
  width: 120px;  /* 从80px改为120px */
  height: 120px;
  perspective: 1200px;  /* 从1000px改为1200px，透视更远 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.cube {
  position: relative;
  width: 90px;  /* 从60px改为90px */
  height: 90px;
  transform-style: preserve-3d;
  /* 旋转速度更慢，动画时间更长 */
  animation: cubeRotate 6s cubic-bezier(0.4, 0, 0.2, 1) infinite;  /* 从3.5s改为6s */
}

.cube-face {
  position: absolute;
  width: 90px;  /* 从60px改为90px */
  height: 90px;
  background: rgba(255, 255, 255, 0.12);
  border: 2px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(4px);
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.3);
}

/* 6个面定位 - 放大版 */
.front  { transform: rotateY(0deg) translateZ(45px); }  /* 从30px改为45px */
.back   { transform: rotateY(180deg) translateZ(45px); }
.left   { transform: rotateY(-90deg) translateZ(45px); }
.right  { transform: rotateY(90deg) translateZ(45px); }
.top    { transform: rotateX(90deg) translateZ(45px); }
.bottom { transform: rotateX(-90deg) translateZ(45px); }

/* 魔方3D旋转动画 - 速度更慢，更优雅 */
@keyframes cubeRotate {
  0% {
    transform: rotateX(0deg) rotateY(0deg) rotateZ(0deg);
  }
  20% {
    transform: rotateX(90deg) rotateY(45deg) rotateZ(22.5deg);
  }
  40% {
    transform: rotateX(180deg) rotateY(90deg) rotateZ(45deg);
  }
  60% {
    transform: rotateX(270deg) rotateY(135deg) rotateZ(67.5deg);
  }
  80% {
    transform: rotateX(360deg) rotateY(180deg) rotateZ(90deg);
  }
  100% {
    transform: rotateX(540deg) rotateY(360deg) rotateZ(180deg);
  }
}

/* 文字 - 稍大一点 */
.loading-text {
  font-size: 20px;  /* 从18px改为20px */
  font-weight: 500;
  color: white;
  letter-spacing: 1px;
  margin: 0;
}

/* 加载点 - 稍大一点 */
.dots {
  display: flex;
  gap: 10px;  /* 从8px改为10px */
  justify-content: center;
}
.dots span {
  width: 10px;  /* 从8px改为10px */
  height: 10px;
  background: white;
  border-radius: 50%;
  animation: bounce 1.6s ease-in-out infinite both;  /* 从1.4s改为1.6s，更慢 */
}
.dots span:nth-child(1) { animation-delay: -0.36s; }  /* 调整延迟 */
.dots span:nth-child(2) { animation-delay: -0.18s; }

/* 基础动画 - 更慢 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes bounceIn {
  0% { transform: scale(0.6); opacity: 0; }
  80% { transform: scale(1.02); }  /* 添加轻微回弹 */
  100% { transform: scale(1); opacity: 1; }
}
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
