<template>
  <div class="space-y-6">
    <!-- 大学搜索 -->
    <div class="search-card" :class="[isDark ? 'search-card-dark' : 'search-card-light']">
      <div class="search-card-header">
        <div class="header-icon bg-gradient-to-r from-blue-500 to-indigo-500">
          <span class="text-xl">🏫</span>
        </div>
        <div>
          <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-lg font-semibold">大学搜索</h2>
          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">按名称、省份、层次查询大学信息</p>
        </div>
      </div>

      <div class="search-filters">
        <div class="filter-group">
          <label class="filter-label">大学名称</label>
          <input
            v-model="universitySearch.keyword"
            type="text"
            placeholder="输入大学名称..."
            class="filter-input"
            :class="isDark ? 'filter-input-dark' : 'filter-input-light'"
            @keyup.enter="searchUniversities"
          />
        </div>
        <div class="filter-group">
          <label class="filter-label">省份</label>
          <select v-model="universitySearch.province" class="filter-select" :class="isDark ? 'filter-select-dark' : 'filter-select-light'">
            <option value="">全部省份</option>
            <option v-for="province in provinces" :key="province" :value="province">{{ province }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label class="filter-label">大学层次</label>
          <select v-model="universitySearch.level" class="filter-select" :class="isDark ? 'filter-select-dark' : 'filter-select-light'">
            <option value="">全部层次</option>
            <option v-for="level in levels" :key="level" :value="level">{{ level }}</option>
          </select>
        </div>
        <button @click="searchUniversities" class="search-btn search-btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          搜索大学
        </button>
      </div>

      <div class="table-container">
        <div v-if="universities.length === 0 && !universityLoading" class="empty-state-small">
          <span class="text-3xl opacity-50">🏫</span>
          <p>暂无数据，请点击搜索</p>
        </div>
        <div v-else class="university-grid">
          <div
            v-for="uni in universities"
            :key="uni.id"
            class="university-card"
            :class="isDark ? 'university-card-dark' : 'university-card-light'"
          >
            <div class="university-card-header">
              <h3 class="university-name">{{ uni.name }}</h3>
              <span class="university-level" :class="getLevelClass(uni.level)">
                {{ uni.level }}
              </span>
            </div>
            <div class="university-info">
              <span class="info-tag">
                <span class="info-icon">📍</span>
                {{ uni.province }}
              </span>
              <span class="info-tag">
                <span class="info-icon">📚</span>
                {{ uni.type || '综合类' }}
              </span>
            </div>
            <button
              @click="getAdmissionHistory(null, uni.id, null)"
              class="history-btn"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              查看招生历史
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 专业搜索 -->
    <div class="search-card" :class="[isDark ? 'search-card-dark' : 'search-card-light']">
      <div class="search-card-header">
        <div class="header-icon bg-gradient-to-r from-green-500 to-teal-500">
          <span class="text-xl">📚</span>
        </div>
        <div>
          <h2 :class="isDark ? 'text-white' : 'text-gray-900'" class="text-lg font-semibold">专业搜索</h2>
          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">按名称、学科门类查询专业信息</p>
        </div>
      </div>

      <div class="search-filters">
        <div class="filter-group">
          <label class="filter-label">专业名称</label>
          <input
            v-model="majorSearch.keyword"
            type="text"
            placeholder="输入专业名称..."
            class="filter-input"
            :class="isDark ? 'filter-input-dark' : 'filter-input-light'"
            @keyup.enter="searchMajors"
          />
        </div>
        <div class="filter-group">
          <label class="filter-label">学科门类</label>
          <select v-model="majorSearch.category" class="filter-select" :class="isDark ? 'filter-select-dark' : 'filter-select-light'">
            <option value="">全部学科</option>
            <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
          </select>
        </div>
        <button @click="searchMajors" class="search-btn search-btn-success">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          搜索专业
        </button>
      </div>

      <div class="table-container">
        <div v-if="majors.length === 0 && !majorLoading" class="empty-state-small">
          <span class="text-3xl opacity-50">📚</span>
          <p>暂无数据，请点击搜索</p>
        </div>
        <div v-else class="major-grid">
          <div
            v-for="major in majors"
            :key="major.id"
            class="major-card"
            :class="isDark ? 'major-card-dark' : 'major-card-light'"
          >
            <div class="major-card-header">
              <h3 class="major-name">{{ major.name }}</h3>
              <span class="major-code">{{ major.code }}</span>
            </div>
            <div class="major-info">
              <span class="info-tag">
                <span class="info-icon">📖</span>
                {{ major.category || '未分类' }}
              </span>
              <span v-if="major.subCategory" class="info-tag">
                <span class="info-icon">🔖</span>
                {{ major.subCategory }}
              </span>
            </div>
            <button
              @click="getAdmissionHistory(major.id, null, null)"
              class="history-btn"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              查看招生历史
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 招生历史弹窗 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="📜 招生历史数据"
      width="900px"
      :class="{ 'dark-dialog': isDark }"
      class="history-dialog"
    >
      <div v-if="admissionHistory.length > 0" class="history-content">
        <div class="history-stats">
          <div class="history-stat">
            <span class="stat-icon">📊</span>
            <span class="stat-text">共 {{ admissionHistory.length }} 条记录</span>
          </div>
          <div class="history-stat">
            <span class="stat-icon">📅</span>
            <span class="stat-text">{{ getYearRange() }}</span>
          </div>
        </div>
        <div class="history-table-wrapper">
          <table class="history-table">
            <thead>
            <tr>
              <th>年份</th>
              <th>大学名称</th>
              <th>专业名称</th>
              <th>省份</th>
              <th>最低分</th>
              <th>最低位次</th>
              <th>平均分</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in admissionHistory" :key="item.id">
              <td class="year-cell">{{ item.year }}</td>
              <td class="university-cell">{{ item.universityName }}</td>
              <td class="major-cell">{{ item.majorName }}</td>
              <td>{{ item.province }}</td>
              <td class="score-cell">{{ item.minScore || '-' }}</td>
              <td class="rank-cell">{{ item.minRank?.toLocaleString() || '-' }}</td>
              <td class="score-cell">{{ item.avgScore || '-' }}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-else class="empty-history">
        <div class="empty-icon">📭</div>
        <p>暂无招生历史数据</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button @click="getAllHistory" class="dialog-btn success">查看全部</button>
          <button @click="showHistoryDialog = false" class="dialog-btn cancel">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const isDark = inject('isDark', ref(false))

const universitySearch = ref({ keyword: '', province: '', level: '' })
const majorSearch = ref({ keyword: '', category: '', universityId: null })
const universities = ref([])
const majors = ref([])
const admissionHistory = ref([])
const showHistoryDialog = ref(false)
const universityLoading = ref(false)
const majorLoading = ref(false)

// 筛选条件数据
const provinces = ref([])
const levels = ref([])
const categories = ref([])

// 当前查看历史的参数
const currentHistoryParams = ref({ majorId: null, universityId: null, year: null })

const getLevelClass = (level) => {
  const map = {
    '985': 'level-985',
    '211': 'level-211',
    '一本': 'level-first',
    '二本': 'level-second'
  }
  return map[level] || ''
}

const getYearRange = () => {
  if (admissionHistory.value.length === 0) return ''
  const years = admissionHistory.value.map(h => h.year).filter(y => y)
  if (years.length === 0) return ''
  const minYear = Math.min(...years)
  const maxYear = Math.max(...years)
  if (minYear === maxYear) return `${minYear}年`
  return `${minYear}年 - ${maxYear}年`
}

const searchUniversities = async () => {
  universityLoading.value = true
  try {
    const res = await request.get('/volunteer/search/universities', universitySearch.value)
    if (res.code === 200) {
      universities.value = res.data || []
      if (universities.value.length === 0) {
        ElMessage.info('未找到相关大学')
      }
    }
  } catch (error) {
    console.error('搜索大学失败', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    universityLoading.value = false
  }
}

const searchMajors = async () => {
  majorLoading.value = true
  try {
    const res = await request.get('/volunteer/search/majors', majorSearch.value)
    if (res.code === 200) {
      majors.value = res.data || []
      if (majors.value.length === 0) {
        ElMessage.info('未找到相关专业')
      }
    }
  } catch (error) {
    console.error('搜索专业失败', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    majorLoading.value = false
  }
}

const getAdmissionHistory = async (majorId, universityId, year) => {
  currentHistoryParams.value = { majorId, universityId, year }
  try {
    const res = await request.get('/volunteer/admission/history', { majorId, universityId, year })
    if (res.code === 200) {
      admissionHistory.value = res.data || []
      showHistoryDialog.value = true
      if (admissionHistory.value.length === 0) {
        ElMessage.info('暂无招生历史数据')
      }
    }
  } catch (error) {
    console.error('获取招生历史失败', error)
    ElMessage.error('获取招生历史失败')
  }
}

const getAllHistory = async () => {
  try {
    const res = await request.get('/volunteer/admission/history')
    if (res.code === 200) {
      admissionHistory.value = res.data || []
      if (admissionHistory.value.length === 0) {
        ElMessage.info('暂无招生历史数据')
      }
    }
  } catch (error) {
    console.error('获取全部招生历史失败', error)
    ElMessage.error('获取全部招生历史失败')
  }
}

const loadFilters = async () => {
  try {
    const [provincesRes, levelsRes, categoriesRes] = await Promise.all([
      request.get('/volunteer/filter/provinces'),
      request.get('/volunteer/filter/levels'),
      request.get('/volunteer/filter/categories')
    ])
    if (provincesRes.code === 200) provinces.value = provincesRes.data || []
    if (levelsRes.code === 200) levels.value = levelsRes.data || []
    if (categoriesRes.code === 200) categories.value = categoriesRes.data || []
  } catch (error) {
    console.error('加载筛选条件失败', error)
  }
}

onMounted(() => {
  loadFilters()
})
</script>

<style scoped>
/* 搜索卡片样式 */
.search-card {
  border-radius: 24px;
  padding: 24px;
  transition: all 0.3s ease;
}

.search-card-light {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.search-card-dark {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.search-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 筛选区域 */
.search-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  align-items: flex-end;
}

.filter-group {
  flex: 1;
  min-width: 150px;
}

.filter-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #6b7280;
}

.dark .filter-label {
  color: #9ca3af;
}

.filter-input, .filter-select {
  width: 100%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.2s;
  outline: none;
}

.filter-input-light, .filter-select-light {
  background: white;
  border: 1px solid #e5e7eb;
  color: #1f2937;
}

.filter-input-dark, .filter-select-dark {
  background: #1e293b;
  border: 1px solid #334155;
  color: #f3f4f6;
}

.filter-input:focus, .filter-select:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* 搜索按钮 */
.search-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  color: white;
}

.search-btn-primary {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.search-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.search-btn-success {
  background: linear-gradient(135deg, #10b981, #14b8a6);
}

.search-btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.3);
}

/* 大学网格 */
.university-grid, .major-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;
}

.university-card, .major-card {
  border-radius: 16px;
  padding: 16px;
  transition: all 0.2s;
  cursor: pointer;
}

.university-card-light, .major-card-light {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.university-card-dark, .major-card-dark {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.university-card:hover, .major-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.university-card-header, .major-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.university-name, .major-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .university-name,
.dark .major-name {
  color: #f3f4f6;
}

.university-level {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.level-985 {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}

.level-211 {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.level-first {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.level-second {
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
}

.major-code {
  font-size: 11px;
  font-family: monospace;
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.05);
  color: #6b7280;
}

.dark .major-code {
  background: rgba(255, 255, 255, 0.1);
  color: #9ca3af;
}

.university-info, .major-info {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.info-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.dark .info-tag {
  color: #9ca3af;
}

.info-icon {
  font-size: 12px;
}

.history-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px;
  border-radius: 40px;
  font-size: 13px;
  font-weight: 500;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: #6366f1;
  cursor: pointer;
  transition: all 0.2s;
}

.history-btn:hover {
  background: rgba(99, 102, 241, 0.2);
  transform: translateY(-1px);
}

/* 表格容器 */
.table-container {
  margin-top: 8px;
}

.empty-state-small {
  text-align: center;
  padding: 60px;
  color: #6b7280;
}

.dark .empty-state-small {
  color: #9ca3af;
}

/* 滚动条样式 */
.university-grid::-webkit-scrollbar,
.major-grid::-webkit-scrollbar {
  width: 6px;
}

.university-grid::-webkit-scrollbar-track,
.major-grid::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
}

.university-grid::-webkit-scrollbar-thumb,
.major-grid::-webkit-scrollbar-thumb {
  background: #c7d2fe;
  border-radius: 10px;
}

.university-grid::-webkit-scrollbar-thumb:hover,
.major-grid::-webkit-scrollbar-thumb:hover {
  background: #818cf8;
}

/* 弹窗样式 */
.history-dialog :deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
}

.dark-dialog :deep(.el-dialog) {
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.history-content {
  max-height: 500px;
  overflow: auto;
}

.history-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.dark .history-stats {
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.history-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 40px;
  font-size: 13px;
  color: #6366f1;
}

.stat-icon {
  font-size: 16px;
}

.stat-text {
  font-weight: 500;
}

.history-table-wrapper {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.history-table th,
.history-table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark .history-table th,
.dark .history-table td {
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.history-table th {
  font-weight: 600;
  color: #4b5563;
  background: rgba(0, 0, 0, 0.02);
}

.dark .history-table th {
  color: #9ca3af;
  background: rgba(255, 255, 255, 0.03);
}

.history-table td {
  color: #374151;
}

.dark .history-table td {
  color: #e5e7eb;
}

.year-cell {
  font-weight: 600;
  color: #6366f1;
}

.university-cell {
  font-weight: 500;
}

.major-cell {
  color: #6b7280;
}

.dark .major-cell {
  color: #9ca3af;
}

.score-cell {
  font-weight: 600;
  color: #10b981;
}

.rank-cell {
  font-family: monospace;
}

.empty-history {
  text-align: center;
  padding: 60px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.dialog-btn {
  padding: 10px 28px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.dialog-btn.cancel {
  background: #f3f4f6;
  color: #4b5563;
}

.dark .dialog-btn.cancel {
  background: #374151;
  color: #9ca3af;
}

.dialog-btn.cancel:hover {
  background: #e5e7eb;
}

.dialog-btn.success {
  background: linear-gradient(135deg, #10b981, #14b8a6);
  color: white;
  margin-right: 12px;
}

.dialog-btn.success:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.3);
}

/* 响应式 */
@media (max-width: 768px) {
  .search-card {
    padding: 16px;
  }

  .search-filters {
    flex-direction: column;
  }

  .filter-group {
    width: 100%;
  }

  .search-btn {
    width: 100%;
    justify-content: center;
  }

  .university-grid, .major-grid {
    grid-template-columns: 1fr;
  }

  .history-table th,
  .history-table td {
    padding: 8px 4px;
    font-size: 12px;
  }
}
</style>
