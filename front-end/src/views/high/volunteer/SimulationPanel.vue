<template>
  <div class="volunteer-panel">
    <section class="panel-shell panel-shell-light" :class="isDark ? 'panel-shell-dark' : 'panel-shell-light'">
      <div class="panel-head">
        <div class="panel-brand panel-brand-primary">
          <span>🎲</span>
        </div>
        <div>
          <h2 class="panel-title" :class="isDark ? 'text-white' : 'text-slate-900'">模拟录取</h2>
          <p class="panel-subtitle" :class="isDark ? 'text-slate-400' : 'text-slate-500'">选择志愿方案后，查看批量模拟结果和整体录取趋势。</p>
        </div>
      </div>

      <div class="control-grid control-grid-2">
        <label class="field field-wide">
          <span class="field-label">选择志愿方案</span>
          <div class="select-wrap">
            <select v-model="simulatePlanId" class="select-input">
              <option value="" disabled>请选择志愿方案</option>
              <option v-for="plan in props.volunteerPlans" :key="plan.id" :value="plan.id">
                {{ plan.name || plan.year }} 年方案 - {{ plan.score }} 分
              </option>
            </select>
            <svg class="select-arrow" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
        </label>
      </div>

      <div class="button-row">
        <button type="button" class="action-btn action-btn-primary" :disabled="!simulatePlanId || simulating" @click="runBatchSimulation">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M6 4v12l9-6-9-6Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
          </svg>
          <span>{{ simulating ? '模拟中...' : '批量模拟录取' }}</span>
        </button>

        <button type="button" class="action-btn action-btn-secondary" :disabled="!simulatePlanId" @click="getAdmissionAnalysis">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M4 4h12v12H4z" stroke="currentColor" stroke-width="1.6" />
            <path d="M7 8h6M7 11h4M7 14h5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
          </svg>
          <span>分析报告</span>
        </button>
      </div>

      <div v-if="simulationResults.length > 0" class="content-block mt-6">
        <div class="block-head">
          <h3 class="block-title" :class="isDark ? 'text-white' : 'text-slate-900'">模拟结果</h3>
          <span class="block-badge" :class="isDark ? 'text-slate-400' : 'text-slate-500'">{{ simulationResults.length }} 条记录</span>
        </div>

        <div class="stack-list">
          <article v-for="(result, index) in simulationResults" :key="index" class="result-card" :class="isDark ? 'result-card-dark' : 'result-card-light'">
            <div class="result-card-main">
              <div class="result-leading">
                <div class="rank-badge" :class="isDark ? 'rank-badge-dark' : 'rank-badge-light'">{{ result.priority }}</div>
                <div>
                  <h4 class="result-title" :class="isDark ? 'text-white' : 'text-slate-900'">{{ result.universityName }}</h4>
                  <p class="result-subtitle" :class="isDark ? 'text-slate-400' : 'text-slate-500'">{{ result.majorName }}</p>
                </div>
              </div>

              <div class="result-aside">
                <div class="stat-grid">
                  <div class="stat-item">
                    <span class="stat-label">目标分数</span>
                    <strong class="stat-value" :class="isDark ? 'text-white' : 'text-slate-900'">{{ result.targetScore }} 分</strong>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">分差</span>
                    <strong class="stat-value" :class="result.scoreDiff >= 0 ? 'text-emerald-500' : 'text-rose-500'">{{ result.scoreDiff }} 分</strong>
                  </div>
                </div>
                <span class="status-pill" :class="statusClass(result.status)">{{ result.status }}</span>
                <span class="probability-text" :class="isDark ? 'text-slate-400' : 'text-slate-500'">概率 {{ result.probability }}</span>
              </div>
            </div>
          </article>
        </div>
      </div>

      <div v-if="admissionAnalysis" class="analysis-panel mt-6" :class="isDark ? 'analysis-panel-dark' : 'analysis-panel-light'">
        <div class="block-head">
          <h3 class="block-title" :class="isDark ? 'text-white' : 'text-slate-900'">录取分析报告</h3>
        </div>

        <div class="analysis-grid">
          <div class="analysis-item">
            <div class="analysis-number" :class="isDark ? 'text-white' : 'text-slate-900'">{{ admissionAnalysis.totalVolunteers }}</div>
            <div class="analysis-label">总志愿数</div>
          </div>
          <div class="analysis-item">
            <div class="analysis-number text-emerald-500">{{ admissionAnalysis.admittedCount }}</div>
            <div class="analysis-label">预计录取</div>
          </div>
          <div class="analysis-item">
            <div class="analysis-number text-amber-500">{{ admissionAnalysis.waitingCount }}</div>
            <div class="analysis-label">待定</div>
          </div>
          <div class="analysis-item">
            <div class="analysis-number text-rose-500">{{ admissionAnalysis.rejectedCount }}</div>
            <div class="analysis-label">未录取</div>
          </div>
        </div>

        <div class="analysis-suggestion">
          {{ admissionAnalysis.suggestion }}
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import request from '@/utils/request'

const isDark = inject('isDark', ref(false))

const props = defineProps({
  userId: {
    type: Number,
    default: 1
  },
  volunteerPlans: {
    type: Array,
    default: () => []
  }
})

const simulating = ref(false)
const simulatePlanId = ref('')
const simulationResults = ref([])
const admissionAnalysis = ref(null)

const statusClass = (status) => {
  if (status === '录取') return 'status-pill-emerald'
  if (status === '待定') return 'status-pill-amber'
  return 'status-pill-rose'
}

const runBatchSimulation = async () => {
  if (!simulatePlanId.value) return
  simulating.value = true
  try {
    const res = await request.get(`/volunteer/simulate/batch/${simulatePlanId.value}`)
    if (res.code === 200) {
      simulationResults.value = res.data || []
      await getAdmissionAnalysis()
    }
  } catch (error) {
    console.error('批量模拟失败', error)
  } finally {
    simulating.value = false
  }
}

const getAdmissionAnalysis = async () => {
  if (!simulatePlanId.value) return
  try {
    const res = await request.get(`/volunteer/simulate/analysis/${simulatePlanId.value}`)
    if (res.code === 200) {
      admissionAnalysis.value = res.data
    }
  } catch (error) {
    console.error('获取分析报告失败', error)
  }
}
</script>

<style scoped>
.volunteer-panel {
  color: inherit;
}

.panel-shell {
  border-radius: 20px;
  padding: 24px;
  backdrop-filter: blur(22px);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
}

.panel-shell-light {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.panel-shell-dark {
  background: rgba(15, 23, 42, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 22px;
}

.panel-brand {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex: 0 0 auto;
}

.panel-brand-primary {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.18), rgba(37, 99, 235, 0.18));
}

.panel-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0;
}

.panel-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  line-height: 1.6;
}

.control-grid {
  display: grid;
  gap: 16px;
}

.control-grid-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.field {
  display: grid;
  gap: 8px;
}

.field-wide {
  grid-column: 1 / -1;
}

.field-label {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.select-wrap {
  position: relative;
}

.select-input {
  width: 100%;
  height: 44px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.82);
  color: #0f172a;
  padding: 0 14px;
  padding-right: 42px;
  outline: none;
  appearance: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.select-input:focus {
  border-color: rgba(15, 118, 110, 0.7);
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.12);
}

.select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  width: 18px;
  height: 18px;
  transform: translateY(-50%);
  color: #64748b;
  pointer-events: none;
}

.button-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.action-btn {
  height: 48px;
  border: none;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.action-btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #0f766e, #2563eb);
  box-shadow: 0 16px 30px rgba(37, 99, 235, 0.22);
}

.action-btn-secondary {
  color: #fff;
  background: linear-gradient(135deg, #14b8a6, #0ea5e9);
  box-shadow: 0 16px 30px rgba(14, 165, 233, 0.18);
}

.content-block,
.analysis-panel {
  border-radius: 20px;
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.block-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
}

.block-badge,
.probability-text,
.stat-label,
.analysis-label {
  font-size: 12px;
}

.stack-list {
  display: grid;
  gap: 12px;
}

.result-card {
  border-radius: 18px;
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.result-card-light {
  background: rgba(248, 250, 252, 0.9);
}

.result-card-dark {
  background: rgba(30, 41, 59, 0.72);
}

.result-card-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.result-leading {
  display: flex;
  align-items: center;
  gap: 14px;
}

.rank-badge {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
}

.rank-badge-light {
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
}

.rank-badge-dark {
  background: rgba(37, 99, 235, 0.18);
  color: #f8fafc;
}

.result-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
}

.result-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
}

.result-aside {
  display: grid;
  justify-items: end;
  gap: 10px;
}

.stat-grid {
  display: flex;
  align-items: flex-end;
  gap: 18px;
}

.stat-item {
  text-align: right;
}

.stat-label {
  color: #64748b;
}

.stat-value {
  display: block;
  margin-top: 4px;
  font-size: 14px;
  font-weight: 800;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.status-pill-emerald { background: rgba(16, 185, 129, 0.12); color: #059669; }
.status-pill-amber { background: rgba(245, 158, 11, 0.12); color: #d97706; }
.status-pill-rose { background: rgba(244, 63, 94, 0.12); color: #e11d48; }

.analysis-panel {
  padding: 18px;
}

.analysis-panel-light {
  background: rgba(239, 246, 255, 0.9);
  border: 1px solid rgba(147, 197, 253, 0.3);
}

.analysis-panel-dark {
  background: rgba(15, 23, 42, 0.72);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.analysis-item {
  border-radius: 16px;
  padding: 14px;
  background: rgba(148, 163, 184, 0.08);
  text-align: center;
}

.analysis-number {
  font-size: 24px;
  font-weight: 900;
}

.analysis-label {
  margin-top: 4px;
  color: #64748b;
}

.analysis-suggestion {
  margin-top: 14px;
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(20, 184, 166, 0.08);
  color: #0f766e;
  font-weight: 700;
}

@media (max-width: 960px) {
  .control-grid-2,
  .analysis-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .panel-shell {
    padding: 18px;
  }

  .control-grid-2,
  .button-row,
  .analysis-grid {
    grid-template-columns: 1fr;
  }

  .result-card-main,
  .result-leading,
  .stat-grid {
    flex-direction: column;
    align-items: flex-start;
  }

  .result-aside {
    justify-items: start;
  }
}
</style>
