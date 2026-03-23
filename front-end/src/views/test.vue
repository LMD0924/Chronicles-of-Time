<!-- MilkLogin.vue -->
<template>
  <div class="min-h-screen bg-[#fef9f0] flex items-center justify-center p-4">
    <!-- 主容器 -->
    <div class="w-full max-w-4xl">
      <!-- 卡片容器 -->
      <div class="bg-white rounded-3xl shadow-xl overflow-hidden">
        <div class="flex flex-col md:flex-row">

          <!-- 左侧品牌区 - 牛奶色系 -->
          <div class="w-full md:w-2/5 bg-[#fef5e8] p-10 flex flex-col justify-between">
            <div>
              <div class="flex items-center space-x-2 mb-12">
                <div class="w-8 h-8 bg-amber-400 rounded-full"></div>
                <span class="text-amber-800 text-xl font-light tracking-wide">Milk</span>
              </div>

              <h2 class="text-3xl font-light text-amber-900 mb-4">
                {{ isLoginMode ? '欢迎回来' : '加入我们' }}
              </h2>

              <!-- 简约打字机效果 -->
              <div class="mb-6">
                <Typewriter
                  :texts="typewriterTexts"
                  :type-speed="80"
                  :delete-speed="50"
                  :pause-time="2000"
                  :show-cursor="true"
                  cursor-char="|"
                  text-class="typewriter-text"
                />
              </div>

              <p class="text-amber-700/60 text-sm leading-relaxed">
                {{ isLoginMode
                ? '登录后享受更多精彩内容'
                : '注册成为会员，开启你的专属体验'
                }}
              </p>
            </div>

            <!-- 装饰 -->
            <div class="mt-8">
              <div class="flex space-x-1">
                <div class="w-1 h-1 bg-amber-300 rounded-full"></div>
                <div class="w-1 h-1 bg-amber-400 rounded-full"></div>
                <div class="w-1 h-1 bg-amber-500 rounded-full"></div>
              </div>
            </div>
          </div>

          <!-- 右侧表单区 - 添加动画容器 -->
          <div class="w-full md:w-3/5 p-10 bg-white overflow-hidden">
            <!-- 模式切换按钮 -->
            <div class="flex justify-end mb-8">
              <div class="bg-[#fef5e8] rounded-full p-1 flex space-x-1">
                <button
                  @click="switchToLogin"
                  class="px-6 py-2 rounded-full text-sm transition-all duration-300 relative"
                  :class="[
                    isLoginMode
                      ? 'bg-white text-amber-800 shadow-sm'
                      : 'text-amber-600 hover:text-amber-800'
                  ]"
                >
                  登录
                </button>
                <button
                  @click="switchToRegister"
                  class="px-6 py-2 rounded-full text-sm transition-all duration-300 relative"
                  :class="[
                    !isLoginMode
                      ? 'bg-white text-amber-800 shadow-sm'
                      : 'text-amber-600 hover:text-amber-800'
                  ]"
                >
                  注册
                </button>
              </div>
            </div>

            <!-- 动画过渡容器 -->
            <div class="form-switch-container">
              <transition
                name="fade-slide"
                mode="out-in"
                @before-leave="beforeLeave"
                @after-enter="afterEnter"
              >
                <!-- 登录表单 -->
                <div v-if="isLoginMode" key="login" class="form-wrapper">
                  <form @submit.prevent="handleLogin" class="space-y-6">
                    <div class="input-group" :class="{ 'input-animate': animateField }">
                      <label class="block text-amber-800 text-sm mb-2">邮箱</label>
                      <input
                        v-model="loginForm.email"
                        type="email"
                        placeholder="your@email.com"
                        class="w-full px-4 py-3 bg-[#fefaf5] border border-amber-200 rounded-xl focus:outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-200 transition-all duration-300"
                      />
                    </div>

                    <div class="input-group" :class="{ 'input-animate': animateField }">
                      <label class="block text-amber-800 text-sm mb-2">密码</label>
                      <input
                        v-model="loginForm.password"
                        :type="showPassword ? 'text' : 'password'"
                        placeholder="请输入密码"
                        class="w-full px-4 py-3 bg-[#fefaf5] border border-amber-200 rounded-xl focus:outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-200 transition-all duration-300"
                      />
                      <div class="flex justify-end mt-2">
                        <button type="button" @click="showPassword = !showPassword" class="text-amber-500 text-xs">
                          {{ showPassword ? '隐藏' : '显示' }}
                        </button>
                      </div>
                    </div>

                    <div class="flex items-center justify-between">
                      <label class="flex items-center space-x-2 cursor-pointer">
                        <input type="checkbox" v-model="rememberMe" class="w-4 h-4 rounded border-amber-300 text-amber-500 focus:ring-amber-200">
                        <span class="text-amber-600 text-sm">记住我</span>
                      </label>
                      <a href="#" class="text-amber-500 text-sm hover:text-amber-600">忘记密码？</a>
                    </div>

                    <button
                      type="submit"
                      class="w-full py-3 bg-amber-400 text-white rounded-xl font-medium hover:bg-amber-500 transform hover:scale-[1.02] transition-all duration-300 shadow-sm"
                      :disabled="loading"
                    >
                      <span v-if="!loading">登录</span>
                      <span v-else class="flex items-center justify-center">
                        <svg class="animate-spin h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24">
                          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        登录中...
                      </span>
                    </button>
                  </form>
                </div>

                <!-- 注册表单 -->
                <div v-else key="register" class="form-wrapper">
                  <form @submit.prevent="handleRegister" class="space-y-5">
                    <div class="input-group" :class="{ 'input-animate': animateField }">
                      <label class="block text-amber-800 text-sm mb-2">用户名</label>
                      <input
                        v-model="registerForm.username"
                        type="text"
                        placeholder="请输入用户名"
                        class="w-full px-4 py-3 bg-[#fefaf5] border border-amber-200 rounded-xl focus:outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-200 transition-all duration-300"
                      />
                    </div>

                    <div class="input-group" :class="{ 'input-animate': animateField }">
                      <label class="block text-amber-800 text-sm mb-2">邮箱</label>
                      <input
                        v-model="registerForm.email"
                        type="email"
                        placeholder="your@email.com"
                        class="w-full px-4 py-3 bg-[#fefaf5] border border-amber-200 rounded-xl focus:outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-200 transition-all duration-300"
                      />
                    </div>

                    <div class="input-group" :class="{ 'input-animate': animateField }">
                      <label class="block text-amber-800 text-sm mb-2">密码</label>
                      <input
                        v-model="registerForm.password"
                        :type="showPassword ? 'text' : 'password'"
                        placeholder="请输入密码"
                        class="w-full px-4 py-3 bg-[#fefaf5] border border-amber-200 rounded-xl focus:outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-200 transition-all duration-300"
                      />
                    </div>

                    <div class="input-group" :class="{ 'input-animate': animateField }">
                      <label class="block text-amber-800 text-sm mb-2">确认密码</label>
                      <input
                        v-model="registerForm.confirmPassword"
                        :type="showConfirmPassword ? 'text' : 'password'"
                        placeholder="请再次输入密码"
                        class="w-full px-4 py-3 bg-[#fefaf5] border border-amber-200 rounded-xl focus:outline-none focus:border-amber-400 focus:ring-2 focus:ring-amber-200 transition-all duration-300"
                      />
                      <div class="flex justify-end mt-2">
                        <button type="button" @click="showConfirmPassword = !showConfirmPassword" class="text-amber-500 text-xs">
                          {{ showConfirmPassword ? '隐藏' : '显示' }}
                        </button>
                      </div>
                    </div>

                    <div class="flex items-center space-x-2">
                      <input type="checkbox" v-model="agreeTerms" class="w-4 h-4 rounded border-amber-300 text-amber-500 focus:ring-amber-200">
                      <span class="text-amber-600 text-sm">
                        同意
                        <a href="#" class="text-amber-500 hover:underline">服务条款</a>
                      </span>
                    </div>

                    <button
                      type="submit"
                      class="w-full py-3 bg-amber-400 text-white rounded-xl font-medium hover:bg-amber-500 transform hover:scale-[1.02] transition-all duration-300 shadow-sm"
                      :disabled="loading"
                    >
                      <span v-if="!loading">注册</span>
                      <span v-else class="flex items-center justify-center">
                        <svg class="animate-spin h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24">
                          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        注册中...
                      </span>
                    </button>
                  </form>
                </div>
              </transition>
            </div>

            <!-- 社交登录 -->
            <div class="mt-8">
              <div class="relative">
                <div class="absolute inset-0 flex items-center">
                  <div class="w-full border-t border-amber-100"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                  <span class="px-4 bg-white text-amber-400">或</span>
                </div>
              </div>

              <div class="mt-6 flex justify-center space-x-4">
                <button class="p-2 text-amber-400 hover:text-amber-500 transition-colors">
                  <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.603-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.03-2.682-.103-.253-.447-1.27.098-2.646 0 0 .84-.269 2.75 1.025.8-.223 1.65-.334 2.5-.334.85 0 1.7.111 2.5.334 1.91-1.294 2.75-1.025 2.75-1.025.545 1.376.201 2.393.099 2.646.64.698 1.03 1.591 1.03 2.682 0 3.841-2.337 4.687-4.565 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.416 22 12c0-5.523-4.477-10-10-10z"/>
                  </svg>
                </button>
                <button class="p-2 text-amber-400 hover:text-amber-500 transition-colors">
                  <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部 -->
      <p class="text-center text-amber-400/60 text-sm mt-6">
        &copy; 2024 Milk. All rights reserved.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import Typewriter from '@/components/Typewriter.vue'

// 模式切换
const isLoginMode = ref(true)

// 动画状态
const animateField = ref(false)

// 表单数据
const loginForm = reactive({
  email: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// UI状态
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const rememberMe = ref(false)
const agreeTerms = ref(true)

// 打字机文本
const typewriterTexts = [
  '简单 · 纯粹 · 美好',
  '让每一天都充满温暖',
  '欢迎来到你的世界',
  '遇见更好的自己'
]

// 切换到登录
const switchToLogin = () => {
  if (!isLoginMode.value) {
    isLoginMode.value = true
    triggerFieldAnimation()
  }
}

// 切换到注册
const switchToRegister = () => {
  if (isLoginMode.value) {
    isLoginMode.value = false
    triggerFieldAnimation()
  }
}

// 触发字段动画
const triggerFieldAnimation = () => {
  animateField.value = true
  setTimeout(() => {
    animateField.value = false
  }, 300)
}

// 动画钩子
const beforeLeave = (el: Element) => {
  // 可以在这里添加离开前的逻辑
  console.log('动画开始')
}

const afterEnter = (el: Element) => {
  // 动画完成后的逻辑
  console.log('动画完成')
}

// 登录处理
const handleLogin = async () => {
  if (!loginForm.email || !loginForm.password) {
    alert('请填写完整信息')
    return
  }

  loading.value = true
  setTimeout(() => {
    console.log('登录:', loginForm)
    alert('登录成功！')
    loading.value = false
  }, 1000)
}

// 注册处理
const handleRegister = async () => {
  if (!registerForm.username || !registerForm.email || !registerForm.password) {
    alert('请填写完整信息')
    return
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }

  if (!agreeTerms.value) {
    alert('请同意服务条款')
    return
  }

  loading.value = true
  setTimeout(() => {
    console.log('注册:', registerForm)
    alert('注册成功！')
    loading.value = false
    isLoginMode.value = true
    // 清空表单
    registerForm.username = ''
    registerForm.email = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
  }, 1000)
}
</script>

<style scoped>
.typewriter-text {
  color: #b45309;
  font-size: 18px;
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* 表单切换动画 */
.form-switch-container {
  position: relative;
  min-height: 400px;
}

.form-wrapper {
  width: 100%;
}

/* 淡入淡出 + 滑动动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateX(0);
}

/* 输入框入场动画 */
.input-group {
  transition: all 0.3s ease;
}

.input-animate {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 按钮悬停效果 */
button {
  position: relative;
  overflow: hidden;
}

button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

button:hover::before {
  width: 300px;
  height: 300px;
}

/* 输入框焦点效果 */
input:focus {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.1);
}

/* 平滑过渡 */
* {
  transition: all 0.2s ease;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #fef5e8;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #fcd34d;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #f59e0b;
}
</style>
