<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import request from '@/utils/request';
import {message} from "ant-design-vue";

const [messageApi, contextHolder] = message.useMessage();

// 主题状态：false = 浅色, true = 深色(纯黑)
const isDark = ref(false)
// 面板位置标记：true=默认布局（左欢迎/右表单） false=互换布局（左表单/右欢迎）
const isDefaultLayout = ref(true)
// 动画状态
const isAnimating = ref(false)
// 卡片翻转角度
const cardRotation = ref(0)
// 表单内容淡入淡出状态
const isContentVisible = ref(true)

// 表单数据
const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  name: '',
  username: '',
  password: ''
})

// 忘记密码弹窗
const showForgotPassword = ref(false)
const forgotEmail = ref('')
const resetMessage = ref('')

// 计算当前激活的面板类型
const activePanel = computed(() => isDefaultLayout.value ? 'login' : 'register')

// 计算类名：控制主题过渡
const themeClasses = computed(() =>
  isDark.value
    ? 'bg-black text-white transition-colors duration-500'
    : 'bg-slate-100 text-slate-900 transition-colors duration-500'
)

// ==================== 打字机效果相关 ====================
// 打字机文本配置
const typewriterTexts = [
  '欢迎来到我们的平台 ✨',
  '安全可靠的登录体验 🔒',
  '随时随地访问您的账户 🌍',
  '享受极速流畅的服务 ⚡'
]

const displayText = ref('')
const isTypingComplete = ref(false)
let currentTextIndex = 0
let currentCharIndex = 0
let isDeleting = false
let typewriterTimer: ReturnType<typeof setTimeout> | null = null

// 打字机核心逻辑
const runTypewriter = () => {
  const currentText = typewriterTexts[currentTextIndex]

  if (!isDeleting) {
    // 打字阶段
    if (currentCharIndex < currentText.length) {
      displayText.value = currentText.substring(0, currentCharIndex + 1)
      currentCharIndex++
      typewriterTimer = setTimeout(runTypewriter, 80)
    } else {
      // 打字完成，暂停2秒后开始删除
      isTypingComplete.value = true
      typewriterTimer = setTimeout(() => {
        isDeleting = true
        runTypewriter()
      }, 2000)
    }
  } else {
    // 删除阶段
    if (currentCharIndex > 0) {
      displayText.value = currentText.substring(0, currentCharIndex - 1)
      currentCharIndex--
      typewriterTimer = setTimeout(runTypewriter, 40)
    } else {
      // 删除完成，切换到下一个文本
      isDeleting = false
      isTypingComplete.value = false
      currentTextIndex = (currentTextIndex + 1) % typewriterTexts.length
      typewriterTimer = setTimeout(runTypewriter, 300)
    }
  }
}

// 启动打字机效果
const startTypewriter = () => {
  if (typewriterTimer) clearTimeout(typewriterTimer)
  displayText.value = ''
  currentCharIndex = 0
  isDeleting = false
  runTypewriter()
}

// 组件挂载时启动打字机
onMounted(() => {
  startTypewriter()
})

// 监听面板切换，重置打字机效果（可选）
watch(activePanel, () => {
  if (!isAnimating.value) {
    // 切换面板时重置打字机，让欢迎语重新开始
    startTypewriter()
  }
})
// ==================== 打字机效果结束 ====================

// 面板切换（互换位置）- 带动画
const togglePanel = () => {
  if (isAnimating.value) return

  isAnimating.value = true

  // 第一阶段：表单内容淡出
  isContentVisible.value = false

  setTimeout(() => {
    // 第二阶段：卡片旋转并切换布局
    cardRotation.value = isDefaultLayout.value ? -15 : 15

    setTimeout(() => {
      // 第三阶段：切换布局
      isDefaultLayout.value = !isDefaultLayout.value
      cardRotation.value = isDefaultLayout.value ? 15 : -15

      setTimeout(() => {
        // 第四阶段：表单内容淡入
        isContentVisible.value = true

        setTimeout(() => {
          // 第五阶段：恢复原始角度
          cardRotation.value = 0
          isAnimating.value = false
        }, 350)
      }, 150)
    }, 350)
  }, 300)
}

// 主题切换
const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
}

// 监听主题变化，添加过渡效果
watch(isDark, () => {
  document.documentElement.classList.add('theme-transition')
  setTimeout(() => {
    document.documentElement.classList.remove('theme-transition')
  }, 500)
})

// 忘记密码处理
const handleForgotPassword = () => {
  if (!forgotEmail.value) {
    alert('请输入邮箱地址')
    return
  }
  if (!/^[^\s@]+@([^\s@]+\.)+[^\s@]+$/.test(forgotEmail.value)) {
    alert('请输入有效的邮箱地址')
    return
  }

  // 模拟发送重置邮件
  resetMessage.value = '重置链接已发送到您的邮箱'
  setTimeout(() => {
    resetMessage.value = ''
    showForgotPassword.value = false
    forgotEmail.value = ''
    alert('密码重置链接已发送，请查收邮件')
  }, 2000)
}

// 第三方登录
const handleSocialLogin = (provider: string) => {
  console.log(`使用 ${provider} 登录`)
  // 这里添加第三方登录逻辑
  alert(`正在跳转到 ${provider} 登录...`)
}

// 表单提交处理
const handleLogin = () => {
  console.log('登录:', loginForm.value)
  // 这里添加登录逻辑
  request.post("auth/login",loginForm.value,(message,data)=>{
    messageApi.success(message)
  })
 // router.push('/')
}

const handleRegister = () => {
  console.log('注册:', registerForm.value)
  // 这里添加注册逻辑
}
</script>

<template>
  <contextHolder/>
  <div :class="themeClasses" class="min-h-screen flex items-center justify-center p-4 overflow-hidden">
    <!-- 背景装饰（增强毛玻璃效果） -->
    <div class="absolute inset-0 z-0">
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-500/20 rounded-full blur-3xl animate-pulse"></div>
      <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl animate-pulse animation-delay-2000"></div>
    </div>

    <!-- Logo 和主题切换按钮区域 -->
    <div class="absolute top-6 left-6 right-6 flex justify-between items-center z-30">
      <!-- Logo -->
      <div class="flex items-center space-x-3 backdrop-blur-md bg-white/10 rounded-full px-4 py-2 border border-white/20 shadow-lg">
        <div class="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10 2a8 8 0 100 16 8 8 0 000-16zm0 14a6 6 0 110-12 6 6 0 010 12z"/>
            <path d="M10 6a4 4 0 100 8 4 4 0 000-8z"/>
          </svg>
        </div>
        <span class="font-bold text-lg" :class="isDark ? 'text-white' : 'text-gray-800'">
          YourBrand
        </span>
      </div>

      <!-- 主题切换按钮 -->
      <button
        @click="toggleTheme"
        class="p-3 rounded-full shadow-lg transition-all duration-300 hover:scale-110 backdrop-blur-md border border-white/20"
        :class="[
          isDark ? 'bg-white/10 text-white' : 'bg-black/10 text-black',
          isDark ? 'hover:bg-white/20' : 'hover:bg-black/20'
        ]"
      >
        {{ isDark ? '☀️' : '🌙' }}
      </button>
    </div>

    <!-- 外层容器 - 添加3D效果 -->
    <div class="w-full max-w-4xl perspective z-10 mt-12">
      <!-- 卡片 - 应用3D旋转动画 + 毛玻璃效果，固定最小高度 -->
      <div
        class="rounded-2xl overflow-hidden relative transform-gpu transition-all duration-700 ease-in-out border border-white/20 shadow-2xl min-h-[600px]"
        :class="[
          isAnimating ? 'shadow-2xl scale-105' : '',
          isDark ? 'bg-gray-900/60' : 'bg-white/60',
          isDark ? 'backdrop-blur-xl' : 'backdrop-blur-md'
        ]"
        :style="{
          transform: `perspective(1000px) rotateY(${cardRotation}deg) scale(${isAnimating ? 0.98 : 1})`,
          transition: 'transform 0.7s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.7s ease'
        }"
      >
        <!-- 动画容器，使用 flex 确保两个面板高度一致 -->
        <div
          class="flex w-full transition-all duration-700 ease-in-out min-h-[600px]"
          :class="[
            isDefaultLayout ? '' : 'flex-row-reverse'
          ]"
        >
          <!-- 欢迎面板（毛玻璃子面板） -->
          <div
            class="w-full md:w-1/2 p-10 flex flex-col items-center justify-center text-center relative overflow-hidden transition-all duration-700 ease-in-out border-r border-white/10"
            :class="[
              isDark ? 'bg-gray-800/40 backdrop-blur-md' : 'bg-slate-50/40 backdrop-blur-sm',
              isAnimating ? 'opacity-90' : 'opacity-100'
            ]"
          >
            <!-- 背景装饰 - 添加旋转动画 -->
            <div
              class="absolute top-0 left-0 w-32 h-32 bg-blue-100/30 rounded-full -translate-x-1/2 -translate-y-1/2 opacity-50 transition-all duration-700"
              :class="{ 'rotate-180 scale-110': isAnimating }"
            ></div>
            <div
              class="absolute bottom-0 right-0 w-32 h-32 bg-blue-100/30 rounded-full translate-x-1/2 translate-y-1/2 opacity-50 transition-all duration-700"
              :class="{ '-rotate-180 scale-110': isAnimating }"
            ></div>

            <!-- 内容区域 -->
            <div
              class="relative z-10 transform transition-all duration-700 ease-in-out w-full"
              :class="[
                isAnimating ? 'scale-95' : 'scale-100'
              ]"
            >
              <!-- 打字机标题 -->
              <div class="mb-6">
                <h1 class="text-4xl font-bold mb-4 min-h-[4rem]">
                  {{ displayText || '✨' }}
                  <span
                    class="inline-block w-0.5 h-8 ml-1 align-middle"
                    :class="[
                      isTypingComplete ? 'opacity-0' : 'opacity-100',
                      isDark ? 'bg-white' : 'bg-gray-900'
                    ]"
                    :style="{
                      animation: isTypingComplete ? 'none' : 'blink 0.7s infinite',
                      transition: 'opacity 0.2s'
                    }"
                  ></span>
                </h1>
              </div>

              <!-- 静态欢迎语 -->
              <p class="text-slate-500 mb-8" :class="isDark ? 'text-slate-300' : ''">
                {{ activePanel === 'login' ? '请使用您的个人信息登录' : '使用邮箱快速注册' }}
              </p>

              <button
                @click="togglePanel"
                :disabled="isAnimating"
                class="px-10 py-3 rounded-full font-semibold shadow-lg transition-all duration-300 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed backdrop-blur-md border border-white/20"
                :class="[
                  isDark ? 'bg-blue-600/80 text-white hover:bg-blue-700/80' : 'bg-blue-600/80 text-white hover:bg-blue-700/80'
                ]"
              >
                {{ activePanel === 'login' ? '去注册' : '去登录' }}
              </button>
            </div>
          </div>

          <!-- 表单面板（毛玻璃子面板） - 固定高度和内容布局 -->
          <div
            class="w-full md:w-1/2 p-10 flex flex-col justify-center transition-all duration-700 ease-in-out"
            :class="[
              isDark ? 'bg-gray-900/20 backdrop-blur-md' : 'bg-white/20 backdrop-blur-sm',
              isAnimating ? 'opacity-90' : 'opacity-100'
            ]"
          >
            <!-- 表单内容容器 - 固定高度，确保内容居中 -->
            <div class="w-full">
              <!-- 表单内容 - 带淡入淡出效果 -->
              <div
                class="transform transition-all duration-500 ease-in-out"
                :class="[
                  isContentVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4',
                  isAnimating ? 'scale-95' : 'scale-100'
                ]"
                :key="activePanel"
              >
                <h2 class="text-3xl font-bold mb-6 text-center">
                  {{ activePanel === 'login' ? '登录' : '注册' }}
                </h2>

                <p class="text-center text-sm text-slate-500 mb-6" :class="isDark ? 'text-slate-300' : ''">
                  或使用邮箱{{ activePanel === 'login' ? '登录' : '注册' }}
                </p>

                <!-- 登录表单 -->
                <form v-if="activePanel === 'login'" @submit.prevent="handleLogin" class="space-y-4">
                  <div class="transition-all duration-500" style="transition-delay: 50ms">
                    <label class="block text-sm font-medium mb-1" :class="isDark ? 'text-slate-200' : ''">用户名/邮箱</label>
                    <input
                      v-model="loginForm.username"
                      placeholder="请输入用户名或邮箱"
                      class="w-full px-4 py-3 rounded-lg border transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-blue-500 backdrop-blur-md"
                      :class="[
                        isDark
                          ? 'bg-gray-800/40 border-gray-700/50 text-white placeholder:text-gray-400'
                          : 'bg-white/40 border-gray-200/50 placeholder:text-gray-500'
                      ]"
                    />
                  </div>
                  <div class="transition-all duration-500" style="transition-delay: 100ms">
                    <label class="block text-sm font-medium mb-1" :class="isDark ? 'text-slate-200' : ''">密码</label>
                    <input
                      v-model="loginForm.password"
                      type="password"
                      placeholder="请输入您的密码"
                      class="w-full px-4 py-3 rounded-lg border transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-blue-500 backdrop-blur-md"
                      :class="[
                        isDark
                          ? 'bg-gray-800/40 border-gray-700/50 text-white placeholder:text-gray-400'
                          : 'bg-white/40 border-gray-200/50 placeholder:text-gray-500'
                      ]"
                    />
                  </div>

                  <!-- 忘记密码链接 -->
                  <div class="flex justify-end">
                    <button
                      type="button"
                      @click="showForgotPassword = true"
                      class="text-sm text-blue-500 hover:text-blue-600 transition-colors"
                    >
                      忘记密码？
                    </button>
                  </div>

                  <button
                    type="submit"
                    class="w-full py-3 rounded-full font-semibold shadow-lg transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] backdrop-blur-md border border-white/20"
                    :class="[
                      isDark ? 'bg-blue-600/80 text-white hover:bg-blue-700/80' : 'bg-blue-600/80 text-white hover:bg-blue-700/80'
                    ]"
                    :disabled="isAnimating"
                  >
                    登录
                  </button>
                </form>

                <!-- 注册表单 -->
                <form v-else @submit.prevent="handleRegister" class="space-y-4">
                  <div class="transition-all duration-500" style="transition-delay: 50ms">
                    <label class="block text-sm font-medium mb-1" :class="isDark ? 'text-slate-200' : ''">姓名</label>
                    <input
                      v-model="registerForm.name"
                      type="text"
                      placeholder="请输入您的姓名"
                      class="w-full px-4 py-3 rounded-lg border transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-blue-500 backdrop-blur-md"
                      :class="[
                        isDark
                          ? 'bg-gray-800/40 border-gray-700/50 text-white placeholder:text-gray-400'
                          : 'bg-white/40 border-gray-200/50 placeholder:text-gray-500'
                      ]"
                    />
                  </div>
                  <div class="transition-all duration-500" style="transition-delay: 100ms">
                    <label class="block text-sm font-medium mb-1" :class="isDark ? 'text-slate-200' : ''">用户名</label>
                    <input
                      v-model="registerForm.username"
                      placeholder="请输入您的用户名"
                      class="w-full px-4 py-3 rounded-lg border transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-blue-500 backdrop-blur-md"
                      :class="[
                        isDark
                          ? 'bg-gray-800/40 border-gray-700/50 text-white placeholder:text-gray-400'
                          : 'bg-white/40 border-gray-200/50 placeholder:text-gray-500'
                      ]"
                    />
                  </div>
                  <div class="transition-all duration-500" style="transition-delay: 150ms">
                    <label class="block text-sm font-medium mb-1" :class="isDark ? 'text-slate-200' : ''">密码</label>
                    <input
                      v-model="registerForm.password"
                      type="password"
                      placeholder="请输入您的密码"
                      class="w-full px-4 py-3 rounded-lg border transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-blue-500 backdrop-blur-md"
                      :class="[
                        isDark
                          ? 'bg-gray-800/40 border-gray-700/50 text-white placeholder:text-gray-400'
                          : 'bg-white/40 border-gray-200/50 placeholder:text-gray-500'
                      ]"
                    />
                  </div>
                  <button
                    type="submit"
                    class="w-full py-3 rounded-full font-semibold shadow-lg transition-all duration-300 hover:scale-[1.02] active:scale-[0.98] backdrop-blur-md border border-white/20"
                    :class="[
                      isDark ? 'bg-blue-600/80 text-white hover:bg-blue-700/80' : 'bg-blue-600/80 text-white hover:bg-blue-700/80'
                    ]"
                    :disabled="isAnimating"
                  >
                    注册
                  </button>
                </form>

                <!-- 其他登录方式 -->
                <div class="mt-6">
                  <div class="relative">
                    <div class="absolute inset-0 flex items-center">
                      <div class="w-full border-t border-white/20"></div>
                    </div>
                    <div class="relative flex justify-center text-sm">
                      <span class="px-4 bg-transparent text-slate-500" :class="isDark ? 'text-slate-400' : 'text-slate-500'">
                        其他登录方式
                      </span>
                    </div>
                  </div>

                  <div class="mt-4 flex justify-center space-x-4">
                    <!-- Google 登录 -->
                    <button
                      type="button"
                      @click="handleSocialLogin('Google')"
                      class="p-2 rounded-full hover:bg-white/10 transition-all duration-300"
                      :class="isDark ? 'hover:bg-white/10' : 'hover:bg-black/5'"
                    >
                      <svg class="w-6 h-6" viewBox="0 0 24 24">
                        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                      </svg>
                    </button>

                    <!-- GitHub 登录 -->
                    <button
                      type="button"
                      @click="handleSocialLogin('GitHub')"
                      class="p-2 rounded-full hover:bg-white/10 transition-all duration-300"
                      :class="isDark ? 'hover:bg-white/10' : 'hover:bg-black/5'"
                    >
                      <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.03-2.682-.103-.253-.447-1.27.098-2.646 0 0 .84-.269 2.75 1.025.8-.223 1.65-.334 2.5-.334.85 0 1.7.111 2.5.334 1.91-1.294 2.75-1.025 2.75-1.025.545 1.376.201 2.393.099 2.646.64.698 1.03 1.591 1.03 2.682 0 3.841-2.337 4.687-4.565 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z"/>
                      </svg>
                    </button>

                    <!-- 微信登录 -->
                    <button
                      type="button"
                      @click="handleSocialLogin('WeChat')"
                      class="p-2 rounded-full hover:bg-white/10 transition-all duration-300"
                      :class="isDark ? 'hover:bg-white/10' : 'hover:bg-black/5'"
                    >
                      <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M8.5 10.5c-.8 0-1.5-.7-1.5-1.5s.7-1.5 1.5-1.5 1.5.7 1.5 1.5-.7 1.5-1.5 1.5zm7 0c-.8 0-1.5-.7-1.5-1.5s.7-1.5 1.5-1.5 1.5.7 1.5 1.5-.7 1.5-1.5 1.5zm4.5-4.5c-3.6 0-6.5 2.6-6.5 5.9 0 3.3 2.9 5.9 6.5 5.9.7 0 1.4-.1 2.1-.3.3-.1.6 0 .8.2l1.6 1.1c.2.1.4.1.6 0 .1-.1.1-.3.1-.4l-.3-1.5c0-.2 0-.4-.1-.6-.2-.5-.3-1-.3-1.5.1-1.1.5-2.1 1.2-3 .5-.7.8-1.5.8-2.4 0-3.3-2.9-5.9-6.5-5.9z"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <div
      v-if="showForgotPassword"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
      @click.self="showForgotPassword = false"
    >
      <div
        class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 transform transition-all"
        :class="isDark ? 'bg-gray-800' : 'bg-white'"
      >
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">
            找回密码
          </h3>
          <button
            @click="showForgotPassword = false"
            class="text-gray-400 hover:text-gray-600 transition-colors"
          >
            ✕
          </button>
        </div>

        <p class="text-sm mb-4" :class="isDark ? 'text-gray-300' : 'text-gray-600'">
          请输入您注册时使用的邮箱地址，我们将发送密码重置链接给您。
        </p>

        <input
          v-model="forgotEmail"
          type="email"
          placeholder="请输入邮箱地址"
          class="w-full px-4 py-3 rounded-lg border focus:outline-none focus:ring-2 focus:ring-blue-500 mb-4"
          :class="[
            isDark
              ? 'bg-gray-700 border-gray-600 text-white placeholder:text-gray-400'
              : 'bg-gray-50 border-gray-200 text-gray-900'
          ]"
        />

        <div v-if="resetMessage" class="text-green-500 text-sm mb-4">
          {{ resetMessage }}
        </div>

        <div class="flex space-x-3">
          <button
            @click="handleForgotPassword"
            class="flex-1 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            发送重置链接
          </button>
          <button
            @click="showForgotPassword = false"
            class="flex-1 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
            :class="isDark ? 'border-gray-600 hover:bg-gray-700' : ''"
          >
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 3D透视效果 */
.perspective {
  perspective: 1000px;
}

/* 动画延迟 */
.animation-delay-2000 {
  animation-delay: 2s;
}

/* 全局主题过渡效果 */
.theme-transition * {
  transition-property: color, background-color, border-color, text-decoration-color, fill, stroke;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 500ms;
}

/* 输入框聚焦动画 */
input:focus {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* 按钮按压反馈 */
button:active:not(:disabled) {
  transform: scale(0.98);
}

/* 硬件加速 */
.transform-gpu {
  transform: translateZ(0);
  backface-visibility: hidden;
  perspective: 1000px;
}

/* 毛玻璃兼容优化 */
:deep(.backdrop-blur-md) {
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
}

:deep(.backdrop-blur-sm) {
  -webkit-backdrop-filter: blur(6px);
  backdrop-filter: blur(6px);
}

:deep(.backdrop-blur-xl) {
  -webkit-backdrop-filter: blur(24px);
  backdrop-filter: blur(24px);
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 0.2;
  }
  50% {
    opacity: 0.4;
  }
}

.animate-pulse {
  animation: pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* 光标闪烁动画 */
@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
