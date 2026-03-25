import './assets/css/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import axios from "axios";
import Typewriter from "@/components/Typewriter.vue";
import { getStoredTheme, setTheme } from './utils/theme'

// 1. 初始化主题（应用启动时立即生效）
const initTheme = () => {
  const theme = getStoredTheme();
  setTheme(theme);
};
initTheme();


axios.defaults.baseURL = 'http://localhost:8500'//设置全局的基础路径

axios.defaults.withCredentials=true;
//后端基础url 之后在请求时只用填写路径 Axios会自动以该url为基础添加路径

const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(createPinia())
app.use(router).use(ElementPlus).use(Antd);
app.component('Typewriter', Typewriter)

app.mount('#app')

// 3. 监听系统主题变化（可选，增强体验）
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
  // 只有当本地没有手动设置过主题时，才跟随系统变化
  if (!localStorage.getItem('app_theme')) {
    setTheme(e.matches ? 'dark' : 'light');
  }
});
