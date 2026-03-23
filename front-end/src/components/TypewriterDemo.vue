<!-- views/TypewriterDemo.vue -->
<template>
  <div class="typewriter-demo">
    <h1>🎨 打字机效果组件库 - 使用示例</h1>

    <!-- 示例1: 基础使用 -->
    <section class="demo-section">
      <h2>1. 基础打字机效果</h2>
      <div class="demo-card">
        <Typewriter
          :texts="basicTexts"
          :type-speed="100"
          :delete-speed="50"
          :pause-time="1500"
          text-class="basic-text"
        />
      </div>
    </section>

    <!-- 示例2: 自定义样式 -->
    <section class="demo-section">
      <h2>2. 自定义样式</h2>
      <div class="demo-card">
        <Typewriter
          :texts="styleTexts"
          :type-speed="80"
          :delete-speed="40"
          cursor-char="✨"
          cursor-speed="0.5s"
          container-class="custom-container"
          text-class="gradient-text"
        />
      </div>
    </section>

    <!-- 示例3: 单行文本不循环 -->
    <section class="demo-section">
      <h2>3. 单行文本（不循环）</h2>
      <div class="demo-card">
        <Typewriter
          text="这是一段不会循环的文本，打字完成后光标消失"
          :type-speed="60"
          :loop="false"
          :pause-time="0"
        />
      </div>
    </section>

    <!-- 示例4: 带控制按钮的增强版 -->
    <section class="demo-section">
      <h2>4. 增强版（带控制按钮）</h2>
      <div class="demo-card">
        <AdvancedTypewriter
          ref="advancedRef"
          :texts="advancedTexts"
          :type-speed="70"
          :delete-speed="35"
          :show-controls="true"
          :gradient="true"
          font-size="24px"
          font-weight="bold"
          color="#667eea"
        />
      </div>
    </section>

    <!-- 示例5: 随机顺序 -->
    <section class="demo-section">
      <h2>5. 随机顺序显示</h2>
      <div class="demo-card">
        <AdvancedTypewriter
          :texts="randomTexts"
          :type-speed="90"
          :shuffle="true"
          :loop="true"
          text-class="random-text"
        />
      </div>
    </section>

    <!-- 示例6: 完整的事件监听 -->
    <section class="demo-section">
      <h2>6. 事件监听示例</h2>
      <div class="demo-card">
        <Typewriter
          :texts="eventTexts"
          :type-speed="80"
          @start="handleStart"
          @complete="handleComplete"
          @cycle-complete="handleCycleComplete"
          @typing="handleTyping"
        />
        <div class="event-log">
          <h3>事件日志：</h3>
          <div v-for="(log, index) in eventLogs" :key="index" class="log-item">
            {{ log }}
          </div>
        </div>
      </div>
    </section>

    <!-- 示例7: 在欢迎页面中使用 -->
    <section class="demo-section">
      <h2>7. 欢迎页面应用</h2>
      <div class="demo-card welcome-card">
        <div class="welcome-content">
          <AdvancedTypewriter
            :texts="welcomeTexts"
            :type-speed="100"
            :delete-speed="60"
            :show-cursor="true"
            cursor-char="▋"
            font-size="32px"
            font-weight="bold"
            :gradient="true"
          />
          <p class="welcome-description">打造极致用户体验，让文字动起来</p>
          <button class="get-started-btn" @click="handleGetStarted">
            开始体验 →
          </button>
        </div>
      </div>
    </section>

    <!-- 示例8: 在表单中使用 -->
    <section class="demo-section">
      <h2>8. 表单中的应用</h2>
      <div class="demo-card form-card">
        <div class="form-header">
          <AdvancedTypewriter
            :texts="formTexts"
            :type-speed="70"
            :loop="false"
            :show-cursor="false"
            text-class="form-title"
          />
        </div>
        <form @submit.prevent="handleSubmit" class="demo-form">
          <input
            v-model="formData.username"
            placeholder="用户名"
            class="form-input"
          />
          <input
            v-model="formData.email"
            type="email"
            placeholder="邮箱"
            class="form-input"
          />
          <button type="submit" class="submit-btn">注册</button>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Typewriter from '@/components/Typewriter.vue'
import AdvancedTypewriter from '@/components/Typewriter.vue'

// 示例数据
const basicTexts = ['Hello Vue3!', '欢迎使用打字机组件', '开箱即用', '简单高效']
const styleTexts = ['✨ 渐变文字效果', '🎨 自定义光标', '⚡ 流畅动画', '💎 美观大方']
const advancedTexts = ['点击按钮控制', '暂停/开始', '重置效果', '随心所欲']
const randomTexts = ['第一句话', '第二句话', '第三句话', '第四句话']
const eventTexts = ['事件监听演示', '开始打字', '打字中...', '完成啦！']
const welcomeTexts = ['打造极致体验', '让文字活起来', '惊艳用户视觉']
const formTexts = ['加入我们，开启精彩旅程']

// 表单数据
const formData = ref({
  username: '',
  email: ''
})

// 事件日志
const eventLogs = ref<string[]>([])

const advancedRef = ref()

// 事件处理方法
const handleStart = () => {
  addLog('开始打字效果')
}

const handleComplete = (text: string, index: number) => {
  addLog(`完成第 ${index + 1} 段文字: "${text}"`)
}

const handleCycleComplete = (totalCycles: number) => {
  addLog(`完成一个循环，共 ${totalCycles + 1} 段文字`)
}

const handleTyping = (text: string, progress: number) => {
  if (progress === 0.5) {
    addLog(`打字进度: ${Math.round(progress * 100)}%`)
  }
}

const addLog = (message: string) => {
  const time = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${time}] ${message}`)
  if (eventLogs.value.length > 10) {
    eventLogs.value.pop()
  }
}

const handleGetStarted = () => {
  alert('开始体验打字机效果！')
}

const handleSubmit = () => {
  alert(`注册成功！用户名：${formData.value.username}`)
  formData.value = { username: '', email: '' }
}
</script>

<style scoped>
.typewriter-demo {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

h1 {
  text-align: center;
  color: white;
  margin-bottom: 40px;
  font-size: 36px;
}

h2 {
  color: white;
  margin-bottom: 20px;
  font-size: 24px;
}

.demo-section {
  margin-bottom: 40px;
}

.demo-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.demo-card:hover {
  transform: translateY(-4px);
}

/* 自定义样式 */
.basic-text {
  font-size: 20px;
  color: #333;
}

.custom-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 30px;
  border-radius: 50px;
}

.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  font-size: 24px;
  font-weight: bold;
}

.random-text {
  font-size: 20px;
  color: #ff6b6b;
  font-weight: 500;
}

/* 事件日志 */
.event-log {
  margin-top: 20px;
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.event-log h3 {
  margin-bottom: 10px;
  font-size: 14px;
  color: #4a5568;
}

.log-item {
  font-size: 12px;
  padding: 4px 0;
  color: #718096;
  font-family: monospace;
  border-bottom: 1px solid #e2e8f0;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  text-align: center;
}

.welcome-content {
  padding: 20px;
}

.welcome-description {
  color: white;
  margin: 20px 0;
  font-size: 18px;
  opacity: 0.9;
}

.get-started-btn {
  background: white;
  border: none;
  padding: 12px 30px;
  border-radius: 50px;
  font-size: 16px;
  font-weight: bold;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s;
}

.get-started-btn:hover {
  transform: translateX(5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* 表单卡片 */
.form-card {
  background: white;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-title {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.demo-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-input {
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
  .demo-card {
    padding: 20px;
  }

  h1 {
    font-size: 28px;
  }

  h2 {
    font-size: 20px;
  }
}
</style>
