<template>
  <div class="major-tree-page app-page-bg">
    <div class="header-card">
      <h2>🎓 我的专业培养方案树</h2>
      <div class="progress-box">
        <span>毕业进度：{{ progress }}%</span>
        <el-progress :percentage="progress" stroke-height="12" />
      </div>
    </div>

    <div class="tree-container">
      <el-tree
        v-loading="loading"
        :data="treeData"
        :expand-on-click-node="false"
        default-expand-all
        node-key="id"
        show-checkbox
        :check-strictly="true"
        @check="handleCheckChange"
      >
        <template #default="{ node }">
          <span class="tree-label">
            <!-- 分类节点 -->
            <span v-if="node.data.nodeType === 'CATEGORY'" class="category">
              {{ node.label }}
              <span v-if="node.data.credit" class="credit">
                学分：{{ node.data.credit }}
              </span>
            </span>

            <!-- 课程节点 -->
            <span v-else class="course" :class="{ passed: node.data.passed }">
              {{ node.label }}
              <span class="course-credit">{{ node.data.credit }}学分</span>
            </span>
          </span>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const loading = ref(false)
const treeData = ref([])
const progress = ref(35)

// 假数据 —— 专业树
treeData.value = [
  {
    id: 1,
    label: "通识教育课程",
    nodeType: "CATEGORY",
    credit: 48,
    children: [
      { id: 101, label: "高等数学", nodeType: "COURSE", credit: 5, passed: true },
      { id: 102, label: "大学英语", nodeType: "COURSE", credit: 4, passed: true },
      { id: 103, label: "大学物理", nodeType: "COURSE", credit: 4, passed: false },
      { id: 104, label: "思想道德与法治", nodeType: "COURSE", credit: 3, passed: true },
    ]
  },
  {
    id: 2,
    label: "专业基础课程",
    nodeType: "CATEGORY",
    credit: 52,
    children: [
      { id: 201, label: "C语言程序设计", nodeType: "COURSE", credit: 4, passed: true },
      { id: 202, label: "数据结构", nodeType: "COURSE", credit: 4.5, passed: false },
      { id: 203, label: "计算机组成原理", nodeType: "COURSE", credit: 4, passed: false },
    ]
  },
  {
    id: 3,
    label: "专业核心课程",
    nodeType: "CATEGORY",
    credit: 60,
    children: [
      { id: 301, label: "操作系统", nodeType: "COURSE", credit: 4.5, passed: false },
      { id: 302, label: "数据库系统概论", nodeType: "COURSE", credit: 4, passed: false },
      { id: 303, label: "计算机网络", nodeType: "COURSE", credit: 4, passed: false },
    ]
  }
]

// 勾选事件（假的，只做演示）
const handleCheckChange = () => {
  console.log("勾选成功")
}
</script>

<style scoped>
.major-tree-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1.25rem 3rem;
  min-height: 100vh;
}

.header-card {
  background: var(--app-card);
  border: 1px solid var(--app-card-border);
  border-radius: var(--app-radius-lg);
  padding: 1.5rem 1.75rem;
  margin-bottom: 1.5rem;
  box-shadow: var(--app-card-shadow);
}

.header-card h2 {
  margin: 0 0 16px;
  font-size: 22px;
  color: var(--app-text);
}

.progress-box {
  max-width: 400px;
}

.tree-container {
  background: var(--app-card);
  border: 1px solid var(--app-card-border);
  border-radius: var(--app-radius-lg);
  padding: 1.75rem;
  box-shadow: var(--app-card-shadow);
}

.tree-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.category {
  font-weight: 600;
  color: #1f2937;
  font-size: 15px;
}

.course {
  font-size: 14px;
  color: #6b7280;
}

.course.passed {
  color: #0fa867;
  font-weight: 500;
}

.credit,
.course-credit {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 10px;
}
</style>
