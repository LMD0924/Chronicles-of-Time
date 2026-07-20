<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  modelValue: Boolean,
  record: {
    type: Object,
    default: null
  },
  isDark: Boolean,
  userId: [String, Number],
  userName: String
})

const emit = defineEmits(['update:modelValue', 'saved'])

const today = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const createDefaultForm = () => ({
  id: null,
  guidanceDate: today(),
  guidanceType: '1',
  guidanceContent: '',
  suggestedCombination: '',
  suggestedMajor: '',
  strengthAnalysis: '',
  weaknessAnalysis: '',
  opportunityAnalysis: '',
  threatAnalysis: '',
  actionPlan: '',
  advisorName: '',
  advisorPosition: '',
  userFeedback: '',
  additionalFeedback: '',
  followUpDate: '',
  status: 1
})

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const form = ref(createDefaultForm())
const saving = ref(false)

const cloneForm = (source) => ({
  ...createDefaultForm(),
  ...(source ? { ...source } : {})
})

const resetForm = () => {
  form.value = createDefaultForm()
}

const syncForm = () => {
  form.value = cloneForm(props.record)
}

const closeDialog = () => {
  dialogVisible.value = false
}

const submit = async () => {
  if (!props.userId) {
    ElMessage.warning('用户信息未加载')
    return
  }

  saving.value = true
  try {
    const payload = {
      ...form.value,
      userId: String(props.userId),
      userName: props.userName || '当前用户'
    }
    const res = await request.post('/guidance/save', payload)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      emit('saved', res.data)
      closeDialog()
    }
  } finally {
    saving.value = false
  }
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      syncForm()
    }
  }
)

watch(
  () => props.record,
  () => {
    if (props.modelValue) {
      syncForm()
    }
  },
  { deep: true }
)
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="编辑选科指导"
    width="980px"
    destroy-on-close
    :class="{ 'dark-dialog': isDark }"
    @closed="resetForm"
  >
    <div class="dialog-form">
      <div class="dialog-grid">
        <div class="dialog-field">
          <label class="dialog-label">指导日期</label>
          <el-date-picker v-model="form.guidanceDate" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" class="dialog-control" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">指导类型</label>
          <el-select v-model="form.guidanceType" class="dialog-control">
            <el-option label="个别咨询" value="1" />
            <el-option label="集体辅导" value="2" />
            <el-option label="家长会" value="3" />
            <el-option label="讲座" value="4" />
          </el-select>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">状态</label>
          <el-select v-model="form.status" class="dialog-control">
            <el-option :value="1" label="进行中" />
            <el-option :value="2" label="已完成" />
            <el-option :value="3" label="已采纳" />
            <el-option :value="4" label="已放弃" />
          </el-select>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">推荐组合</label>
          <el-input v-model="form.suggestedCombination" class="dialog-control" placeholder="例如：物化生" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">推荐专业</label>
          <el-input v-model="form.suggestedMajor" class="dialog-control" placeholder="例如：计算机科学与技术" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">下次跟进日期</label>
          <el-date-picker v-model="form.followUpDate" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" class="dialog-control" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">指导人</label>
          <el-input v-model="form.advisorName" class="dialog-control" placeholder="指导人姓名" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">指导人职务</label>
          <el-input v-model="form.advisorPosition" class="dialog-control" placeholder="例如：年级主任、班主任" />
        </div>

        <div class="dialog-field dialog-span-2">
          <label class="dialog-label">指导内容</label>
          <el-input v-model="form.guidanceContent" type="textarea" :rows="3" class="dialog-control" placeholder="记录本次指导的主要内容" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">优势分析</label>
          <el-input v-model="form.strengthAnalysis" type="textarea" :rows="3" class="dialog-control" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">劣势分析</label>
          <el-input v-model="form.weaknessAnalysis" type="textarea" :rows="3" class="dialog-control" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">机会分析</label>
          <el-input v-model="form.opportunityAnalysis" type="textarea" :rows="3" class="dialog-control" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">挑战分析</label>
          <el-input v-model="form.threatAnalysis" type="textarea" :rows="3" class="dialog-control" />
        </div>

        <div class="dialog-field dialog-span-2">
          <label class="dialog-label">行动计划</label>
          <el-input v-model="form.actionPlan" type="textarea" :rows="3" class="dialog-control" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">用户反馈</label>
          <el-input v-model="form.userFeedback" type="textarea" :rows="3" class="dialog-control" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">补充反馈</label>
          <el-input v-model="form.additionalFeedback" type="textarea" :rows="3" class="dialog-control" />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-form {
  max-height: 68vh;
  overflow-y: auto;
  padding: 4px 2px 0;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.dialog-field {
  min-width: 0;
}

.dialog-span-2 {
  grid-column: span 2;
}

.dialog-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
}

.dialog-control {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.dark-dialog .el-dialog) {
  background: #000 !important;
  border: 1px solid #1a1a2e;
}

:deep(.dark-dialog .el-dialog__title) {
  color: #e5e7eb;
}

:deep(.dark-dialog .el-dialog__body) {
  background: #000 !important;
}

@media (max-width: 768px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }

  .dialog-span-2 {
    grid-column: span 1;
  }
}
</style>
