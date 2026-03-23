<!-- components/AdvancedTypewriter.vue -->
<template>
  <div class="advanced-typewriter" :class="containerClass">
    <div class="typewriter-wrapper" :style="wrapperStyle">
      <slot name="prefix"></slot>
      <span
        ref="textRef"
        class="typewriter-text"
        :class="textClass"
        :style="textStyle"
      >
        {{ displayText }}
      </span>
      <span
        v-if="showCursor"
        class="cursor"
        :class="{ 'cursor-blink': cursorBlink }"
        :style="cursorStyle"
      >
        {{ cursorChar }}
      </span>
      <slot name="suffix"></slot>
    </div>

    <!-- 控制按钮（可选） -->
    <div v-if="showControls" class="typewriter-controls">
      <button @click="start" :disabled="isRunning" class="control-btn">
        ▶ 开始
      </button>
      <button @click="stop" :disabled="!isRunning" class="control-btn">
        ⏸ 暂停
      </button>
      <button @click="reset" class="control-btn">
        🔄 重置
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface Props {
  texts?: string[]
  text?: string
  typeSpeed?: number
  deleteSpeed?: number
  pauseTime?: number
  loop?: boolean
  showCursor?: boolean
  cursorChar?: string
  cursorBlink?: boolean
  cursorSpeed?: string
  autoStart?: boolean
  showControls?: boolean
  containerClass?: string
  textClass?: string
  // 高级选项
  shuffle?: boolean // 随机顺序
  keepLastText?: boolean // 完成后保留最后一个文本
  onComplete?: () => void
  fontSize?: string
  fontWeight?: string
  color?: string
  gradient?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  texts: () => [],
  text: '',
  typeSpeed: 100,
  deleteSpeed: 50,
  pauseTime: 1500,
  loop: true,
  showCursor: true,
  cursorChar: '|',
  cursorBlink: true,
  cursorSpeed: '0.7s',
  autoStart: true,
  showControls: false,
  containerClass: '',
  textClass: '',
  shuffle: false,
  keepLastText: false,
  fontSize: 'inherit',
  fontWeight: 'inherit',
  color: 'inherit',
  gradient: false
})

const emit = defineEmits(['complete', 'cycleComplete', 'start', 'charTyped'])

const displayText = ref('')
const textRef = ref<HTMLElement>()
let textArray: string[] = []
let originalIndices: number[] = []
let currentIndex = 0
let currentCharIndex = 0
let isDeleting = false
let timer: ReturnType<typeof setTimeout> | null = null
let isActive = true
const isRunning = ref(false)

// 文本样式
const textStyle = computed(() => ({
  fontSize: props.fontSize,
  fontWeight: props.fontWeight,
  color: props.color
}))

// 光标样式
const cursorStyle = computed(() => ({
  animationDuration: props.cursorSpeed
}))

// 包装器样式
const wrapperStyle = computed(() => ({
  background: props.gradient
    ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    : 'transparent',
  backgroundClip: props.gradient ? 'text' : 'unset',
  WebkitBackgroundClip: props.gradient ? 'text' : 'unset',
  color: props.gradient ? 'transparent' : props.color
}))

// 初始化文本
const initTexts = () => {
  let texts = props.texts.length > 0 ? [...props.texts] : (props.text ? [props.text] : ['Welcome!'])

  if (props.shuffle) {
    // 随机打乱顺序
    for (let i = texts.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [texts[i], texts[j]] = [texts[j], texts[i]]
    }
  }

  textArray = texts
  originalIndices = textArray.map((_, i) => i)
}

// 打字机核心
const type = () => {
  if (!isActive || textArray.length === 0) return

  const currentText = textArray[currentIndex]
  if (!currentText) return

  if (!isDeleting) {
    if (currentCharIndex < currentText.length) {
      const newChar = currentText[currentCharIndex]
      displayText.value = currentText.substring(0, currentCharIndex + 1)
      currentCharIndex++
      emit('charTyped', newChar, currentCharIndex, currentText.length)
      timer = setTimeout(type, props.typeSpeed)
    } else {
      emit('complete', currentText, currentIndex)

      if (textArray.length === 1 && !props.loop) {
        isActive = false
        isRunning.value = false
        if (props.keepLastText) {
          // 保留最后文本
        } else {
          // 可选操作
        }
        return
      }

      isDeleting = true
      timer = setTimeout(type, props.pauseTime)
    }
  } else {
    if (currentCharIndex > 0) {
      displayText.value = currentText.substring(0, currentCharIndex - 1)
      currentCharIndex--
      timer = setTimeout(type, props.deleteSpeed)
    } else {
      isDeleting = false
      currentIndex++

      if (currentIndex >= textArray.length) {
        if (props.loop) {
          currentIndex = 0
          emit('cycleComplete', currentIndex)
        } else {
          isActive = false
          isRunning.value = false
          return
        }
      }

      timer = setTimeout(type, props.typeSpeed)
    }
  }
}

const start = () => {
  if (!isActive || isRunning.value) return
  isActive = true
  isRunning.value = true
  emit('start')
  type()
}

const stop = () => {
  isActive = false
  isRunning.value = false
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

const reset = () => {
  stop()
  displayText.value = ''
  currentIndex = 0
  currentCharIndex = 0
  isDeleting = false
  isActive = true
  isRunning.value = false
  initTexts()
  if (props.autoStart) {
    start()
  }
}

onMounted(() => {
  initTexts()
  if (props.autoStart) {
    start()
  }
})

onUnmounted(() => {
  stop()
})

defineExpose({
  start,
  stop,
  reset,
  setTexts: (newTexts: string[]) => {
    stop()
    textArray = [...newTexts]
    currentIndex = 0
    currentCharIndex = 0
    isDeleting = false
    start()
  }
})
</script>

<style scoped>
.advanced-typewriter {
  display: inline-block;
}

.typewriter-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.typewriter-text {
  display: inline-block;
  word-break: break-word;
}

.cursor {
  display: inline-block;
  margin-left: 2px;
  font-weight: normal;
}

.cursor-blink {
  animation: blink infinite;
}

.typewriter-controls {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  justify-content: center;
}

.control-btn {
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.control-btn:hover:not(:disabled) {
  background: #f7fafc;
  transform: translateY(-1px);
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
