<script setup>
import messageApi from '@/utils/messageApi'
import { computed, onMounted, ref } from 'vue'
import { Delete, EditPen, Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const loading = ref(true)
const organizations = ref([])
const activities = ref([])
const summary = ref({ organizationCount: 0, activeOrganizationCount: 0, activityCount: 0, completedActivityCount: 0, serviceHours: 0 })
const organizationDialog = ref(false)
const activityDialog = ref(false)
const editingOrganization = ref(null)
const editingActivity = ref(null)
const blankOrganization = () => ({ organizationType: 'CLUB', organizationName: '', department: '', roleName: '', startDate: '', endDate: '', status: 'ACTIVE', description: '', achievements: '' })
const blankActivity = () => ({ organizationId: organizations.value[0]?.id || null, activityType: 'ACTIVITY', title: '', startAt: '', endAt: '', location: '', status: 'PLANNED', serviceHours: 0, responsibility: '', resultSummary: '' })
const organizationForm = ref(blankOrganization())
const activityForm = ref(blankActivity())

const upcoming = computed(() => activities.value.filter(item => ['PLANNED', 'IN_PROGRESS'].includes(item.status)).slice(0, 8))
const completed = computed(() => activities.value.filter(item => item.status === 'DONE').slice(0, 8))
const organizationName = id => organizations.value.find(item => String(item.id) === String(id))?.organizationName || '未关联组织'
const typeLabel = type => ({ CLUB: '社团', STUDENT_UNION: '学生会', ASSOCIATION: '协会', VOLUNTEER: '志愿组织' }[type] || type)
const activityTypeLabel = type => ({ ACTIVITY: '活动', MEETING: '会议', RECRUITMENT: '招新', VOLUNTEER: '志愿服务', COMPETITION: '赛事' }[type] || type)
const statusLabel = status => ({ ACTIVE: '任职中', ENDED: '已结束', PLANNED: '待开始', IN_PROGRESS: '进行中', DONE: '已完成', CANCELLED: '已取消' }[status] || status)
const dateText = value => value ? String(value).replace('T', ' ').slice(0, 16) : '时间待定'

const loadData = async () => {
  loading.value = true
  try {
    const [orgRes, activityRes, summaryRes] = await Promise.all([
      request.get('/university/campus/organizations'),
      request.get('/university/campus/activities'),
      request.get('/university/campus/summary'),
    ])
    organizations.value = orgRes.data || []
    activities.value = activityRes.data || []
    summary.value = { ...summary.value, ...summaryRes.data }
  } finally { loading.value = false }
}

const openOrganization = item => {
  editingOrganization.value = item || null
  organizationForm.value = item ? { ...item } : blankOrganization()
  organizationDialog.value = true
}
const saveOrganization = async () => {
  if (!organizationForm.value.organizationName.trim()) return messageApi.warning('请填写组织名称')
  if (editingOrganization.value) await request.put(`/university/campus/organizations/${editingOrganization.value.id}`, organizationForm.value)
  else await request.post('/university/campus/organizations', organizationForm.value)
  organizationDialog.value = false
  messageApi.success(editingOrganization.value ? '组织经历已更新' : '组织经历已添加')
  await loadData()
}
const deleteOrganization = async item => {
  await request.delete(`/university/campus/organizations/${item.id}`)
  messageApi.success('组织经历及关联事务已删除')
  await loadData()
}
const openActivity = item => {
  editingActivity.value = item || null
  activityForm.value = item ? { ...item } : blankActivity()
  activityDialog.value = true
}
const saveActivity = async () => {
  if (!activityForm.value.title.trim()) return messageApi.warning('请填写事务名称')
  if (editingActivity.value) await request.put(`/university/campus/activities/${editingActivity.value.id}`, activityForm.value)
  else await request.post('/university/campus/activities', activityForm.value)
  activityDialog.value = false
  messageApi.success(editingActivity.value ? '校园事务已更新' : '校园事务已添加')
  await loadData()
}
const deleteActivity = async item => {
  await request.delete(`/university/campus/activities/${item.id}`)
  messageApi.success('校园事务已删除')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <section class="campus-workspace">
    <header class="campus-header"><div><p class="app-section-label">Campus life</p><h2>社团与学生会</h2><p>管理任职经历、活动执行、会议、招新和志愿服务。</p></div><div class="campus-actions"><button class="app-btn-secondary" type="button" :disabled="loading" @click="loadData"><Refresh />同步</button><button class="app-btn-secondary" type="button" @click="openOrganization()"><Plus />组织经历</button><button class="app-btn-primary" type="button" @click="openActivity()"><Plus />校园事务</button></div></header>
    <div class="campus-stats"><article><span>组织经历</span><strong>{{ summary.organizationCount }}</strong><small>{{ summary.activeOrganizationCount }} 个任职中</small></article><article><span>校园事务</span><strong>{{ summary.activityCount }}</strong><small>{{ summary.completedActivityCount }} 项已完成</small></article><article><span>志愿时长</span><strong>{{ summary.serviceHours || 0 }}<em>h</em></strong><small>累计服务时间</small></article><article><span>待推进</span><strong>{{ upcoming.length }}</strong><small>活动、会议与招新</small></article></div>
    <div class="campus-grid">
      <section class="app-card-surface campus-panel"><div class="panel-heading"><div><h3>我的组织</h3><p>社团、学生会、协会与志愿组织</p></div></div><div v-if="organizations.length" class="item-list"><article v-for="item in organizations" :key="item.id" class="item-row"><span class="item-kind">{{ typeLabel(item.organizationType) }}</span><div><strong>{{ item.organizationName }}</strong><small>{{ item.department || '未填写部门' }} · {{ item.roleName || '成员' }} · {{ statusLabel(item.status) }}</small><p>{{ item.achievements || item.description || '暂未记录成果' }}</p></div><button class="icon-button" title="编辑" @click="openOrganization(item)"><EditPen /></button><button class="icon-button danger" title="删除" @click="deleteOrganization(item)"><Delete /></button></article></div><div v-else class="campus-empty">还没有组织经历，先记录加入的社团或学生会。</div></section>
      <section class="app-card-surface campus-panel"><div class="panel-heading"><div><h3>近期事务</h3><p>跟进待开始和进行中的事项</p></div></div><div v-if="upcoming.length" class="item-list"><article v-for="item in upcoming" :key="item.id" class="item-row"><span class="activity-date">{{ dateText(item.startAt) }}</span><div><strong>{{ item.title }}</strong><small>{{ activityTypeLabel(item.activityType) }} · {{ organizationName(item.organizationId) }} · {{ statusLabel(item.status) }}</small><p>{{ item.responsibility || item.location || '待补充执行信息' }}</p></div><button class="icon-button" title="编辑" @click="openActivity(item)"><EditPen /></button><button class="icon-button danger" title="删除" @click="deleteActivity(item)"><Delete /></button></article></div><div v-else class="campus-empty">当前没有待推进的校园事务。</div></section>
    </div>
    <section v-if="completed.length" class="app-card-surface campus-panel"><div class="panel-heading"><div><h3>成果归档</h3><p>已完成事项和结果沉淀</p></div></div><div class="completed-grid"><article v-for="item in completed" :key="item.id"><span>{{ activityTypeLabel(item.activityType) }}</span><strong>{{ item.title }}</strong><p>{{ item.resultSummary || '已完成，暂未填写成果总结' }}</p></article></div></section>

    <el-dialog v-model="organizationDialog" :title="editingOrganization ? '编辑组织经历' : '新增组织经历'" width="min(620px, 92vw)"><div class="campus-form"><label>组织类型<select v-model="organizationForm.organizationType"><option value="CLUB">社团</option><option value="STUDENT_UNION">学生会</option><option value="ASSOCIATION">协会</option><option value="VOLUNTEER">志愿组织</option></select></label><label>组织名称<input v-model="organizationForm.organizationName"></label><label>部门<input v-model="organizationForm.department"></label><label>职务<input v-model="organizationForm.roleName"></label><label>开始日期<input v-model="organizationForm.startDate" type="date"></label><label>结束日期<input v-model="organizationForm.endDate" type="date"></label><label>状态<select v-model="organizationForm.status"><option value="ACTIVE">任职中</option><option value="ENDED">已结束</option></select></label><label class="full">职责说明<textarea v-model="organizationForm.description" rows="3"></textarea></label><label class="full">成果与荣誉<textarea v-model="organizationForm.achievements" rows="3"></textarea></label></div><template #footer><button class="app-btn-secondary" @click="organizationDialog=false">取消</button><button class="app-btn-primary" @click="saveOrganization">保存</button></template></el-dialog>
    <el-dialog v-model="activityDialog" :title="editingActivity ? '编辑校园事务' : '新增校园事务'" width="min(680px, 92vw)"><div class="campus-form"><label>关联组织<select v-model="activityForm.organizationId"><option :value="null">不关联</option><option v-for="item in organizations" :key="item.id" :value="item.id">{{ item.organizationName }}</option></select></label><label>事务类型<select v-model="activityForm.activityType"><option value="ACTIVITY">活动</option><option value="MEETING">会议</option><option value="RECRUITMENT">招新</option><option value="VOLUNTEER">志愿服务</option><option value="COMPETITION">赛事</option></select></label><label class="full">事务名称<input v-model="activityForm.title"></label><label>开始时间<input v-model="activityForm.startAt" type="datetime-local"></label><label>结束时间<input v-model="activityForm.endAt" type="datetime-local"></label><label>地点<input v-model="activityForm.location"></label><label>状态<select v-model="activityForm.status"><option value="PLANNED">待开始</option><option value="IN_PROGRESS">进行中</option><option value="DONE">已完成</option><option value="CANCELLED">已取消</option></select></label><label>志愿时长<input v-model.number="activityForm.serviceHours" type="number" min="0" step="0.5"></label><label class="full">我的职责<textarea v-model="activityForm.responsibility" rows="3"></textarea></label><label class="full">结果总结<textarea v-model="activityForm.resultSummary" rows="3"></textarea></label></div><template #footer><button class="app-btn-secondary" @click="activityDialog=false">取消</button><button class="app-btn-primary" @click="saveActivity">保存</button></template></el-dialog>
  </section>
</template>

<style scoped>
.campus-workspace{display:grid;gap:18px}.campus-header{display:flex;align-items:flex-end;justify-content:space-between;gap:18px}.campus-header h2{margin-top:4px;color:var(--app-text);font-size:24px;font-weight:850}.campus-header p:not(.app-section-label),.panel-heading p{margin-top:4px;color:var(--app-text-muted);font-size:12px}.campus-actions{display:flex;flex-wrap:wrap;gap:8px}.campus-actions button{display:flex;align-items:center;gap:5px}.campus-actions svg{width:16px}.campus-stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:10px}.campus-stats article,.campus-panel{border:1px solid var(--app-border);border-radius:8px;background:var(--app-card-solid);padding:16px}.campus-stats span,.campus-stats small{display:block;color:var(--app-text-muted);font-size:11px}.campus-stats strong{display:block;margin:6px 0;color:var(--app-text);font-size:27px}.campus-stats em{margin-left:3px;font-size:12px;font-style:normal}.campus-grid{display:grid;grid-template-columns:minmax(0,.9fr) minmax(0,1.1fr);gap:14px}.panel-heading{margin-bottom:13px}.panel-heading h3{color:var(--app-text);font-size:16px;font-weight:800}.item-list{display:grid;gap:7px}.item-row{display:flex;align-items:flex-start;gap:9px;border:1px solid var(--app-border);border-radius:7px;padding:10px}.item-row>div{min-width:0;flex:1}.item-row strong,.item-row small,.item-row p{display:block}.item-row strong{color:var(--app-text);font-size:13px}.item-row small{margin-top:3px;color:var(--app-text-muted);font-size:10px}.item-row p{overflow:hidden;margin-top:5px;color:var(--app-text-secondary);font-size:11px;text-overflow:ellipsis;white-space:nowrap}.item-kind,.activity-date{flex:0 0 auto;border-radius:5px;background:rgb(var(--theme-primary-rgb)/.1);padding:4px 6px;color:var(--theme-primary);font-size:10px}.activity-date{width:83px;text-align:center}.icon-button{display:grid;width:27px;height:27px;flex:0 0 27px;place-items:center;border-radius:5px;color:var(--app-text-muted)}.icon-button:hover{background:rgb(var(--theme-primary-rgb)/.08);color:var(--theme-primary)}.icon-button.danger:hover{color:#dc2626}.icon-button svg{width:14px}.campus-empty{padding:50px 16px;color:var(--app-text-muted);text-align:center;font-size:12px}.completed-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:9px}.completed-grid article{border:1px solid var(--app-border);border-radius:7px;padding:12px}.completed-grid span{color:var(--theme-primary);font-size:10px}.completed-grid strong,.completed-grid p{display:block}.completed-grid strong{margin-top:4px;color:var(--app-text);font-size:13px}.completed-grid p{margin-top:5px;color:var(--app-text-muted);font-size:11px}.campus-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.campus-form label{display:grid;gap:5px;color:var(--app-text-secondary);font-size:12px}.campus-form .full{grid-column:1/-1}.campus-form input,.campus-form select,.campus-form textarea{width:100%;border:1px solid var(--app-border);border-radius:7px;background:var(--app-card-solid);padding:9px;color:var(--app-text);outline:none}.campus-form input:focus,.campus-form select:focus,.campus-form textarea:focus{border-color:var(--theme-primary)}@media(max-width:900px){.campus-header{align-items:flex-start;flex-direction:column}.campus-grid{grid-template-columns:1fr}.campus-stats{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.campus-actions,.campus-actions button{width:100%}.campus-actions button{justify-content:center}.campus-stats,.completed-grid,.campus-form{grid-template-columns:1fr}.campus-form .full{grid-column:auto}.activity-date{display:none}}
</style>
