/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#102033',
        brand: {
          50: '#f0fdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',
        },
        signal: {
          amber: '#f59e0b',
          rose: '#e11d48',
          blue: '#2563eb',
          violet: '#7c3aed',
        },
      },
      boxShadow: {
        panel: '0 18px 50px -36px rgba(15, 23, 42, 0.45)',
      },
    },
  },
  plugins: [],
}

