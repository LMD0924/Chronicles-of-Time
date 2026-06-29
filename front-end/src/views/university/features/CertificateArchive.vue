<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

defineProps({ majorId: Number, isDark: Boolean })

const STORAGE_KEY = 'uni_certificate_archive'
const certs = ref([])
const form = ref({ name: '', type: '竞赛', date: '', level: '' })

const load = async () => {
  const local = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  certs.value = local
  try {
    const res = await request.get('/resume/getCompleteResume')
    if (res.code === 200 && res.data?.certificates?.length) {
      const fromResume = res.data.certificates.map((c) => ({
        id: 'r-' + c.id,
        name: c.certificateName || c.name,
        type: '证书',
        date: c.issueDate,
        level: c.level,
        source: '简历',
      }))
      certs.value = [...fromResume, ...local]
    }
  } catch (_) { /* resume optional */ }
}

const add = () => {
  if (!form.value.name) return
  const item = { id: Date.now(), ...form.value, source: '本地' }
  const list = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  list.push(item)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
  form.value = { name: '', type: '竞赛', date: '', level: '' }
  load()
}

const remove = (id) => {
  const list = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]').filter((c) => c.id !== id)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
  load()
}

onMounted(load)
</script>

<template>
  <div class="grid lg:grid-cols-2 gap-6">
    <div class="app-card-surface p-5 space-y-3">
      <h3 class="font-semibold">添加证书 / 竞赛</h3>
      <input v-model="form.name" placeholder="名称" class="w-full px-3 py-2 rounded-lg border text-sm" />
      <select v-model="form.type" class="w-full px-3 py-2 rounded-lg border text-sm">
        <option>竞赛</option>
        <option>四六级</option>
        <option>实习</option>
        <option>其他</option>
      </select>
      <input v-model="form.date" type="date" class="w-full px-3 py-2 rounded-lg border text-sm" />
      <input v-model="form.level" placeholder="等级/奖项" class="w-full px-3 py-2 rounded-lg border text-sm" />
      <button type="button" class="app-btn-primary w-full" @click="add">保存</button>
    </div>
    <div class="app-card-surface p-5">
      <h3 class="font-semibold mb-4">档案列表</h3>
      <div v-for="c in certs" :key="c.id" class="flex justify-between items-start py-3 border-b last:border-0 text-sm">
        <div>
          <p class="font-medium">{{ c.name }}</p>
          <p class="text-zinc-500 text-xs">{{ c.type }} · {{ c.level || '-' }} · {{ c.date || '未填日期' }}</p>
          <span class="text-xs text-brand-500">{{ c.source }}</span>
        </div>
        <button v-if="c.source === '本地'" type="button" class="text-red-500 text-xs" @click="remove(c.id)">删除</button>
      </div>
      <p v-if="!certs.length" class="text-zinc-500 text-sm">暂无记录</p>
    </div>
  </div>
</template>
