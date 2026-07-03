/**
 * 文件说明：拾光记前台应用拾光记项目脚本模块，封装拾光记项目相关的配置、状态、路由或工具逻辑。
 */
const themeScale = (name) => ({
  50: `rgb(var(--color-${name}-50) / <alpha-value>)`,
  100: `rgb(var(--color-${name}-100) / <alpha-value>)`,
  200: `rgb(var(--color-${name}-200) / <alpha-value>)`,
  300: `rgb(var(--color-${name}-300) / <alpha-value>)`,
  400: `rgb(var(--color-${name}-400) / <alpha-value>)`,
  500: `rgb(var(--color-${name}-500) / <alpha-value>)`,
  600: `rgb(var(--color-${name}-600) / <alpha-value>)`,
  700: `rgb(var(--color-${name}-700) / <alpha-value>)`,
  800: `rgb(var(--color-${name}-800) / <alpha-value>)`,
  900: `rgb(var(--color-${name}-900) / <alpha-value>)`,
  950: `rgb(var(--color-${name}-950) / <alpha-value>)`,
})

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class', // 启用类名控制的暗黑模式
  theme: {
    extend: {
      colors: {
        brand: themeScale('brand'),
        accent: themeScale('accent'),
        indigo: themeScale('brand'),
        fuchsia: themeScale('brand'),
        pink: themeScale('brand'),
        purple: themeScale('accent'),
        violet: themeScale('accent'),
        dark: {
          bg: '#000000',
          surface: '#111111',
          border: '#333333',
          text: '#ffffff'
        }
      },
      boxShadow: {
        soft: '0 12px 34px -24px rgb(var(--color-brand-600) / 0.35)',
        glow: '0 18px 42px -24px rgb(var(--color-brand-500) / 0.55)',
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-in-out',
        'slide-in': 'slideIn 0.3s ease-out'
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' }
        },
        slideIn: {
          '0%': { transform: 'translateX(20px)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' }
        }
      }
    },
  },
  plugins: [],
}
