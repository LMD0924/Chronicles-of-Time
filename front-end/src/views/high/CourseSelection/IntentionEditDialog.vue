<script setup>
import messageApi from '@/utils/messageApi'
import { computed, ref, watch } from 'vue'
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

const createDefaultForm = () => ({
  id: null,
  grade: '高一',
  firstSubjectIntention: '物理',
  secondSubjectIntention1: '',
  secondSubjectIntention2: '',
  secondSubjectBackup1: '',
  secondSubjectBackup2: '',
  intentionReason: '',
  targetMajor: '',
  targetUniversity: '',
  strengthSubjects: '',
  weakSubjects: '',
  careerInterest: '',
  adminFeedback: '',
  additionalFeedback: ''
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
    messageApi.warning('用户信息未加载')
    return
  }
  if (!form.value.secondSubjectIntention1 || !form.value.secondSubjectIntention2) {
    messageApi.warning('请至少填写两门再选科目')
    return
  }

  saving.value = true
  try {
    const payload = {
      ...form.value,
      userId: String(props.userId),
      userName: props.userName || '当前用户'
    }
    const res = await request.post('/intention/save', payload)
    if (res.code === 200) {
      messageApi.success('保存成功')
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
    title="编辑选课意向"
    width="920px"
    destroy-on-close
    :class="{ 'dark-dialog': isDark }"
    @closed="resetForm"
  >
    <div class="dialog-form">
      <div class="dialog-grid">
        <div class="dialog-field">
          <label class="dialog-label">年级</label>
          <el-select v-model="form.grade" class="dialog-control" placeholder="请选择年级">
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
          </el-select>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">首选科目</label>
          <el-radio-group v-model="form.firstSubjectIntention" class="dialog-radio-group">
            <el-radio-button label="物理" />
            <el-radio-button label="历史" />
          </el-radio-group>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">再选科目 1</label>
          <el-select v-model="form.secondSubjectIntention1" class="dialog-control" placeholder="请选择">
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="政治" value="政治" />
            <el-option label="地理" value="地理" />
          </el-select>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">再选科目 2</label>
          <el-select v-model="form.secondSubjectIntention2" class="dialog-control" placeholder="请选择">
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="政治" value="政治" />
            <el-option label="地理" value="地理" />
          </el-select>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">备选科目 1</label>
          <el-select v-model="form.secondSubjectBackup1" class="dialog-control" placeholder="可选">
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="政治" value="政治" />
            <el-option label="地理" value="地理" />
          </el-select>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">备选科目 2</label>
          <el-select v-model="form.secondSubjectBackup2" class="dialog-control" placeholder="可选">
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="政治" value="政治" />
            <el-option label="地理" value="地理" />
          </el-select>
        </div>

        <div class="dialog-field">
          <label class="dialog-label">目标专业</label>
          <el-input v-model="form.targetMajor" class="dialog-control" placeholder="例如：计算机科学与技术" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">目标大学</label>
          <el-input v-model="form.targetUniversity" class="dialog-control" placeholder="例如：清华大学" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">优势科目</label>
          <el-input v-model="form.strengthSubjects" class="dialog-control" placeholder="例如：数学、物理" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">弱势科目</label>
          <el-input v-model="form.weakSubjects" class="dialog-control" placeholder="例如：英语、化学" />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">职业兴趣</label>
          <el-input v-model="form.careerInterest" class="dialog-control" placeholder="例如：程序员、医生" />
        </div>

        <div class="dialog-field dialog-span-2">
          <label class="dialog-label">选课理由</label>
          <el-input
            v-model="form.intentionReason"
            type="textarea"
            :rows="3"
            class="dialog-control"
            placeholder="请说明选择该组合的原因"
          />
        </div>

        <div class="dialog-field">
          <label class="dialog-label">管理员反馈</label>
          <el-input v-model="form.adminFeedback" type="textarea" :rows="3" class="dialog-control" />
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

.dialog-radio-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
