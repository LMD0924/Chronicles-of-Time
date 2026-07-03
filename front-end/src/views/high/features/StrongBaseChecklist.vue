<!--
  文件说明：拾光记前台应用高中阶段页面组件，承载高中阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { ref, onMounted } from 'vue'

defineProps({ isDark: Boolean })
const STORAGE = 'strong_base_checklist'
const defaultItems = [
  { id: 1, label: '个人陈述', done: false, due: '' },
  { id: 2, label: '高中成绩单盖章', done: false, due: '' },
  { id: 3, label: '竞赛获奖证明', done: false, due: '' },
  { id: 4, label: '推荐信', done: false, due: '' },
  { id: 5, label: '综合素质评价表', done: false, due: '' },
  { id: 6, label: '报名表签字', done: false, due: '' },
]
const items = ref([])

const load = () => {
  items.value = JSON.parse(localStorage.getItem(STORAGE) || 'null') || [...defaultItems]
}
const save = () => localStorage.setItem(STORAGE, JSON.stringify(items.value))
const add = () => {
  items.value.push({ id: Date.now(), label: '新材料', done: false, due: '' })
  save()
}

onMounted(load)
</script>

<template>
  <div class="app-card-surface p-5 max-w-xl">
    <h3 class="font-semibold mb-4">强基 / 综评材料清单</h3>
    <ul class="space-y-3">
      <li v-for="item in items" :key="item.id" class="flex items-center gap-3 text-sm">
        <input v-model="item.done" type="checkbox" class="rounded border-brand-400" @change="save" />
        <input v-model="item.label" class="flex-1 px-2 py-1 rounded border text-sm" @blur="save" />
        <input v-model="item.due" type="date" class="w-32 px-2 py-1 rounded border text-xs" @change="save" />
      </li>
    </ul>
    <button type="button" class="app-btn-secondary text-sm mt-4" @click="add">+ 添加材料项</button>
    <p class="text-xs text-zinc-500 mt-4">截止日期到期前可在系统通知中扩展提醒（当前为本地清单）</p>
  </div>
</template>
