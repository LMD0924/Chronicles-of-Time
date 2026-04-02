<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  selectionData: {
    type: Object,
    required: true
  },
  isDark: Boolean
})

const emit = defineEmits(['success', 'cancel'])

const form = ref({
  ...props.selectionData
})

const saving = ref(false)
const firstSubjects = ref([])
const secondSubjects = ref([])

// 获取科目列表
const fetchSubjects = async () => {
  try {
    const [firstRes, secondRes] = await Promise.all([
      request.get('/subject/first'),
      request.get('/subject/second')
    ])
    if (firstRes.code === 200) firstSubjects.value = firstRes.data
    if (secondRes.code === 200) secondSubjects.value = secondRes.data
  } catch (error) {
    console.error('获取科目失败', error)
  }
}

const submit = async () => {
  saving.value = true
  try {
    const res = await request.put('/selection/modify', form.value)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      emit('success')
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchSubjects()
})
</script>

<template>
  <div class="space-y-4">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label class="block text-sm mb-1">首选科目</label>
        <select v-model="form.firstSubjectName" class="w-full px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600">
          <option value="">请选择</option>
          <option v-for="subject in firstSubjects" :key="subject.id" :value="subject.name">
            {{ subject.name }}
          </option>
        </select>
      </div>
      <div>
        <label class="block text-sm mb-1">再选科目1</label>
        <select v-model="form.secondSubject1Name" class="w-full px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600">
          <option value="">请选择</option>
          <option v-for="subject in secondSubjects" :key="subject.id" :value="subject.name">
            {{ subject.name }}
          </option>
        </select>
      </div>
      <div>
        <label class="block text-sm mb-1">再选科目2</label>
        <select v-model="form.secondSubject2Name" class="w-full px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600">
          <option value="">请选择</option>
          <option v-for="subject in secondSubjects" :key="subject.id" :value="subject.name">
            {{ subject.name }}
          </option>
        </select>
      </div>
      <div>
        <label class="block text-sm mb-1">选课理由</label>
        <input v-model="form.selectionReason" class="w-full px-3 py-2 rounded-lg border dark:bg-slate-800 dark:border-slate-600" />
      </div>
    </div>
    <div class="flex justify-end gap-2">
      <button @click="emit('cancel')" class="px-4 py-2 rounded-lg border">取消</button>
      <button @click="submit" :disabled="saving" class="px-4 py-2 rounded-lg bg-indigo-600 text-white">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </div>
  </div>
</template>
