<template>
  <div class="relative w-full h-screen overflow-hidden bg-gradient-to-br from-slate-900 via-indigo-950 to-purple-900">
    <!-- 3D画布容器 -->
    <div ref="canvasContainer" class="absolute inset-0 z-0"></div>

    <!-- 高级UI覆盖层 - 玻璃态控制面板 更精致 -->
    <div class="absolute top-6 left-6 z-20 backdrop-blur-2xl bg-white/5 rounded-2xl border border-white/20 shadow-2xl p-5 w-80 transition-all duration-300 hover:bg-white/10">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg animate-pulse">
          <i class="fas fa-brain text-white text-xl"></i>
        </div>
        <div>
          <h2 class="text-white font-bold text-lg tracking-tight">错题知识图谱</h2>
          <p class="text-white/60 text-xs">动态3D知识关联网络</p>
        </div>
      </div>

      <!-- 统计卡片 带光效 -->
      <div class="grid grid-cols-2 gap-3 mb-5">
        <div class="bg-white/5 rounded-xl p-2 backdrop-blur-sm border border-white/10 hover:border-indigo-400/50 transition-all">
          <div class="text-white/50 text-xs">总节点</div>
          <div class="text-white text-xl font-bold">{{ stats.totalNodes }}</div>
        </div>
        <div class="bg-white/5 rounded-xl p-2 backdrop-blur-sm border border-white/10 hover:border-indigo-400/50 transition-all">
          <div class="text-white/50 text-xs">关联关系</div>
          <div class="text-white text-xl font-bold">{{ stats.totalEdges }}</div>
        </div>
        <div class="bg-white/5 rounded-xl p-2 backdrop-blur-sm border border-white/10 hover:border-indigo-400/50 transition-all">
          <div class="text-white/50 text-xs">科目覆盖</div>
          <div class="text-white text-xl font-bold">{{ stats.subjects }}</div>
        </div>
        <div class="bg-white/5 rounded-xl p-2 backdrop-blur-sm border border-white/10 hover:border-indigo-400/50 transition-all">
          <div class="text-white/50 text-xs">掌握率</div>
          <div class="text-white text-xl font-bold">{{ stats.masteryRate }}%</div>
        </div>
      </div>

      <!-- 筛选器 -->
      <div class="space-y-3">
        <div>
          <label class="text-white/70 text-xs block mb-1">筛选学科</label>
          <select v-model="selectedSubject" class="w-full bg-white/10 border border-white/20 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400">
            <option value="all">全部学科</option>
            <option v-for="subj in subjects" :key="subj" :value="subj">{{ subj }}</option>
          </select>
        </div>
        <div>
          <label class="text-white/70 text-xs block mb-1">节点类型</label>
          <div class="flex gap-2">
            <button @click="toggleType('subject')" :class="['px-3 py-1 rounded-lg text-xs transition-all', showTypes.subject ? 'bg-indigo-500 text-white shadow-lg shadow-indigo-500/50' : 'bg-white/10 text-white/60 hover:bg-white/20']">科目</button>
            <button @click="toggleType('type')" :class="['px-3 py-1 rounded-lg text-xs transition-all', showTypes.type ? 'bg-indigo-500 text-white shadow-lg shadow-indigo-500/50' : 'bg-white/10 text-white/60 hover:bg-white/20']">题型</button>
            <button @click="toggleType('knowledge')" :class="['px-3 py-1 rounded-lg text-xs transition-all', showTypes.knowledge ? 'bg-indigo-500 text-white shadow-lg shadow-indigo-500/50' : 'bg-white/10 text-white/60 hover:bg-white/20']">知识点</button>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="mt-5 pt-3 border-t border-white/10 text-white/40 text-xs flex items-center justify-between">
        <span><i class="fas fa-mouse-pointer mr-1"></i> 拖拽旋转视角</span>
        <span><i class="fas fa-scroll mr-1"></i> 滚轮缩放</span>
      </div>
    </div>

    <!-- 右侧信息面板 - 点击节点展示详情 更精美 -->
    <div v-if="selectedNode" class="absolute top-6 right-6 z-20 backdrop-blur-2xl bg-black/50 rounded-2xl border border-white/30 shadow-2xl p-5 w-80 animate-slideInRight">
      <div class="flex justify-between items-start mb-3">
        <div class="flex items-center gap-2">
          <div :class="['w-8 h-8 rounded-lg flex items-center justify-center shadow-lg', getNodeColor(selectedNode.type)]">
            <i :class="getNodeIcon(selectedNode.type)" class="text-white text-sm"></i>
          </div>
          <h3 class="text-white font-semibold">{{ selectedNode.name }}</h3>
        </div>
        <button @click="selectedNode = null" class="text-white/50 hover:text-white transition-all">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="space-y-2 text-sm">
        <div class="flex justify-between">
          <span class="text-white/50">节点类型</span>
          <span class="text-white">{{ getTypeLabel(selectedNode.type) }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-white/50">关联数量</span>
          <span class="text-white">{{ selectedNode.connections }} 个关系</span>
        </div>
        <div v-if="selectedNode.type === 'knowledge'">
          <div class="flex justify-between mt-2 pt-2 border-t border-white/10">
            <span class="text-white/50">掌握状态</span>
            <span :class="selectedNode.mastered ? 'text-green-400' : 'text-yellow-400'">
              <i :class="selectedNode.mastered ? 'fas fa-check-circle' : 'fas fa-exclamation-triangle'"></i>
              {{ selectedNode.mastered ? '已掌握' : '待强化' }}
            </span>
          </div>
          <div class="mt-2">
            <div class="text-white/50 text-xs mb-1">复习次数</div>
            <div class="w-full bg-white/10 rounded-full h-1.5">
              <div class="bg-gradient-to-r from-indigo-400 to-purple-400 h-1.5 rounded-full" :style="{ width: `${Math.min(100, selectedNode.reviewCount * 10)}%` }"></div>
            </div>
            <div class="text-white/70 text-xs mt-1">{{ selectedNode.reviewCount }} 次复习</div>
          </div>
        </div>
        <div v-if="selectedNode.sampleMistake" class="mt-2 p-2 bg-white/5 rounded-lg">
          <div class="text-white/50 text-xs">示例错题</div>
          <div class="text-white/80 text-xs mt-1 line-clamp-2">{{ selectedNode.sampleMistake }}</div>
        </div>
      </div>
    </div>

    <!-- 底部水印 -->
    <div class="absolute bottom-4 left-1/2 transform -translate-x-1/2 z-20 text-white/30 text-xs flex gap-4">
      <span><i class="fas fa-chart-network mr-1"></i> 智能错题分析系统</span>
      <span>基于真实错题数据构建知识图谱</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js'

// ========== 模拟错题数据 (基于mistake_records表结构) ==========
const generateMockData = () => {
  const subjects = ['数学', '英语', '物理', '语文']
  const knowledgeMap = {
    '数学': ['三角函数', '立体几何', '函数与导数', '概率统计'],
    '英语': ['虚拟语气', '定语从句', '词汇辨析', '阅读理解'],
    '物理': ['牛顿力学', '电磁感应', '热学定律', '光学原理'],
    '语文': ['文言文翻译', '古诗鉴赏', '作文立意', '现代文阅读']
  }
  const records = []
  let id = 1
  for (const subject of subjects) {
    const kps = knowledgeMap[subject]
    for (const kp of kps) {
      const mastered = Math.random() > 0.5
      const reviewCount = Math.floor(Math.random() * 15)
      records.push({
        id: id++,
        subject_name: subject,
        knowledge_point: kp,
        mistake_type: ['单选', '多选', '填空', '解答'][Math.floor(Math.random() * 4)],
        mastered: mastered ? 1 : 0,
        review_count: reviewCount,
        mistake_name: `${subject}·${kp} 典型错题示例`
      })
    }
  }
  return records
}

const mockRecords = generateMockData()
const subjects = [...new Set(mockRecords.map(r => r.subject_name))]

const stats = computed(() => {
  const totalNodes = subjects.length + 4 + [...new Set(mockRecords.map(r => r.knowledge_point))].length
  const totalEdges = mockRecords.length + subjects.length * 2
  const masteryRate = Math.round((mockRecords.filter(r => r.mastered === 1).length / mockRecords.length) * 100)
  return { totalNodes, totalEdges, subjects: subjects.length, masteryRate }
})

const selectedSubject = ref('all')
const showTypes = ref({ subject: true, type: true, knowledge: true })
const selectedNode = ref(null)
const canvasContainer = ref(null)

let scene, camera, renderer, labelRenderer, controls, rafId
let nodesGroup = new THREE.Group()
let edgesGroup = new THREE.Group()
let glowGroup = new THREE.Group() // 用于光晕效果

const getNodeColor = (type) => {
  const colors = {
    subject: 'bg-gradient-to-br from-emerald-500 to-teal-500',
    type: 'bg-gradient-to-br from-amber-500 to-orange-500',
    knowledge: 'bg-gradient-to-br from-indigo-500 to-purple-500'
  }
  return colors[type] || 'bg-gray-500'
}

const getNodeIcon = (type) => {
  const icons = {
    subject: 'fas fa-book',
    type: 'fas fa-question-circle',
    knowledge: 'fas fa-lightbulb'
  }
  return icons[type] || 'fas fa-circle'
}

const getTypeLabel = (type) => {
  const labels = { subject: '学科节点', type: '题型节点', knowledge: '知识点节点' }
  return labels[type] || '节点'
}

const generateGraphData = () => {
  const nodes = []
  const edges = []

  subjects.forEach(subj => nodes.push({ id: `subj_${subj}`, name: subj, type: 'subject' }))
  const typesList = ['单选', '多选', '填空', '解答']
  typesList.forEach(type => nodes.push({ id: `type_${type}`, name: type, type: 'type' }))

  let knowledgeNodes = [...new Set(mockRecords.map(r => r.knowledge_point))]
  if (selectedSubject.value !== 'all') {
    knowledgeNodes = [...new Set(mockRecords.filter(r => r.subject_name === selectedSubject.value).map(r => r.knowledge_point))]
  }

  knowledgeNodes.forEach(kp => {
    const record = mockRecords.find(r => r.knowledge_point === kp)
    nodes.push({
      id: `kp_${kp}`, name: kp, type: 'knowledge',
      subject: record?.subject_name || '未知',
      mastered: record?.mastered === 1,
      reviewCount: record?.review_count || 0,
      sampleMistake: record?.mistake_name || `${kp} 相关错题`
    })
  })

  // 建立边关系
  nodes.forEach(node => {
    if (node.type === 'knowledge') {
      const subjectNodeId = `subj_${node.subject}`
      if (nodes.some(n => n.id === subjectNodeId)) edges.push({ source: subjectNodeId, target: node.id })
      const recordsForKp = mockRecords.filter(r => r.knowledge_point === node.name)
      const relatedTypes = [...new Set(recordsForKp.map(r => r.mistake_type))]
      relatedTypes.forEach(type => {
        if (nodes.some(n => n.id === `type_${type}`)) edges.push({ source: `type_${type}`, target: node.id })
      })
    }
  })

  subjects.forEach(subj => {
    typesList.forEach(type => {
      if (mockRecords.some(r => r.subject_name === subj && r.mistake_type === type)) {
        edges.push({ source: `subj_${subj}`, target: `type_${type}` })
      }
    })
  })

  return { nodes, edges }
}

const computeLayout = (nodes, edges) => {
  const positions = new Map()
  const subjectNodes = nodes.filter(n => n.type === 'subject')
  const typeNodes = nodes.filter(n => n.type === 'type')
  const knowledgeNodes = nodes.filter(n => n.type === 'knowledge')

  subjectNodes.forEach((node, idx) => {
    const angle = (idx / subjectNodes.length) * Math.PI * 1.5 - Math.PI / 3
    positions.set(node.id, new THREE.Vector3(-5 + Math.cos(angle) * 2.5, Math.sin(angle * 1.5) * 1.5, Math.sin(angle) * 3))
  })

  typeNodes.forEach((node, idx) => {
    const angle = (idx / typeNodes.length) * Math.PI * 1.2
    positions.set(node.id, new THREE.Vector3(Math.cos(angle) * 4, 4 + Math.sin(angle * 2) * 1, Math.sin(angle) * 3))
  })

  knowledgeNodes.forEach((node, idx) => {
    const total = knowledgeNodes.length
    const phi = Math.acos(2 * idx / total - 1)
    const theta = Math.PI * 2 * (idx / total)
    positions.set(node.id, new THREE.Vector3(3 + Math.sin(phi) * Math.cos(theta) * 4, Math.sin(phi) * Math.sin(theta) * 3, Math.cos(phi) * 4))
  })

  // 力调整
  for (let iter = 0; iter < 60; iter++) {
    edges.forEach(edge => {
      const p1 = positions.get(edge.source)
      const p2 = positions.get(edge.target)
      if (p1 && p2) {
        const dir = new THREE.Vector3().subVectors(p2, p1).normalize()
        const dist = p1.distanceTo(p2)
        const force = (dist - 2.2) * 0.03
        p1.addScaledVector(dir, force)
        p2.addScaledVector(dir, -force)
      }
    })
    positions.forEach(pos => {
      pos.x = Math.min(7, Math.max(-7, pos.x))
      pos.y = Math.min(5, Math.max(-3, pos.y))
      pos.z = Math.min(6, Math.max(-5, pos.z))
    })
  }
  return positions
}

const initThree = () => {
  if (!canvasContainer.value) return
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x050510)
  scene.fog = new THREE.FogExp2(0x050510, 0.008)

  camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(8, 5, 12)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.shadowMap.enabled = true
  renderer.setPixelRatio(window.devicePixelRatio)
  canvasContainer.value.appendChild(renderer.domElement)

  labelRenderer = new CSS2DRenderer()
  labelRenderer.setSize(window.innerWidth, window.innerHeight)
  labelRenderer.domElement.style.position = 'absolute'
  labelRenderer.domElement.style.top = '0px'
  labelRenderer.domElement.style.left = '0px'
  labelRenderer.domElement.style.pointerEvents = 'none'
  canvasContainer.value.appendChild(labelRenderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.autoRotate = true
  controls.autoRotateSpeed = 1.0
  controls.enableZoom = true
  controls.zoomSpeed = 1.2
  controls.target.set(0, 0.5, 0)

  // 灯光系统
  const ambientLight = new THREE.AmbientLight(0x2a2a4a)
  scene.add(ambientLight)
  const mainLight = new THREE.DirectionalLight(0xffffff, 1)
  mainLight.position.set(2, 5, 3)
  mainLight.castShadow = true
  scene.add(mainLight)
  const backLight = new THREE.PointLight(0x4466ff, 0.6)
  backLight.position.set(-2, 1, -4)
  scene.add(backLight)
  const fillLight = new THREE.PointLight(0xffaa66, 0.4)
  fillLight.position.set(3, 2, 2)
  scene.add(fillLight)
  const purpleLight = new THREE.PointLight(0xaa66ff, 0.5)
  purpleLight.position.set(0, 3, 0)
  scene.add(purpleLight)

  // 星空粒子
  const starGeometry = new THREE.BufferGeometry()
  const starCount = 2000
  const starPositions = new Float32Array(starCount * 3)
  for (let i = 0; i < starCount; i++) {
    starPositions[i*3] = (Math.random() - 0.5) * 200
    starPositions[i*3+1] = (Math.random() - 0.5) * 100
    starPositions[i*3+2] = (Math.random() - 0.5) * 80 - 40
  }
  starGeometry.setAttribute('position', new THREE.BufferAttribute(starPositions, 3))
  const starMaterial = new THREE.PointsMaterial({ color: 0xffffff, size: 0.12, transparent: true, opacity: 0.7, blending: THREE.AdditiveBlending })
  const stars = new THREE.Points(starGeometry, starMaterial)
  scene.add(stars)

  // 动态粒子光环
  const particleCount = 1200
  const particleGeo = new THREE.BufferGeometry()
  const particlePositions = new Float32Array(particleCount * 3)
  for (let i = 0; i < particleCount; i++) {
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(2 * Math.random() - 1)
    const r = 6.5
    particlePositions[i*3] = r * Math.sin(phi) * Math.cos(theta)
    particlePositions[i*3+1] = r * Math.sin(phi) * Math.sin(theta) * 0.7
    particlePositions[i*3+2] = r * Math.cos(phi)
  }
  particleGeo.setAttribute('position', new THREE.BufferAttribute(particlePositions, 3))
  const particleMat = new THREE.PointsMaterial({ color: 0x88aaff, size: 0.06, transparent: true, blending: THREE.AdditiveBlending })
  const particles = new THREE.Points(particleGeo, particleMat)
  scene.add(particles)

  const animate = () => {
    rafId = requestAnimationFrame(animate)
    controls.update()
    particles.rotation.y += 0.001
    particles.rotation.x += 0.0005
    stars.rotation.y += 0.0003
    if (glowGroup) glowGroup.rotation.y += 0.002
    renderer.render(scene, camera)
    labelRenderer.render(scene, camera)
  }
  animate()
}

const buildGraph = () => {
  if (!scene) return
  while(nodesGroup.children.length) nodesGroup.remove(nodesGroup.children[0])
  while(edgesGroup.children.length) edgesGroup.remove(edgesGroup.children[0])
  while(glowGroup.children.length) glowGroup.remove(glowGroup.children[0])

  const { nodes, edges } = generateGraphData()
  const positions = computeLayout(nodes, edges)

  // 创建发光连线 (带光晕效果)
  edges.forEach(edge => {
    const pos1 = positions.get(edge.source)
    const pos2 = positions.get(edge.target)
    if (pos1 && pos2) {
      const points = []
      const mid = new THREE.Vector3().lerpVectors(pos1, pos2, 0.5)
      mid.y += 0.3
      points.push(pos1, mid, pos2)
      const curve = new THREE.CatmullRomCurve3(points)
      const tubeGeometry = new THREE.TubeGeometry(curve, 30, 0.04, 8, false)
      const material = new THREE.MeshBasicMaterial({ color: 0x66ccff, transparent: true, opacity: 0.4, blending: THREE.AdditiveBlending })
      const tube = new THREE.Mesh(tubeGeometry, material)
      edgesGroup.add(tube)

      // 沿线流动粒子
      const flowCount = 40
      const flowPositions = []
      for (let i = 0; i <= flowCount; i++) {
        const t = i / flowCount
        flowPositions.push(...curve.getPoint(t).toArray())
      }
      const flowGeo = new THREE.BufferGeometry()
      flowGeo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(flowPositions), 3))
      const flowMat = new THREE.PointsMaterial({ color: 0x88ddff, size: 0.07, transparent: true, blending: THREE.AdditiveBlending })
      const flowPoints = new THREE.Points(flowGeo, flowMat)
      edgesGroup.add(flowPoints)
    }
  })

  // 创建华丽节点球体
  nodes.forEach(node => {
    const pos = positions.get(node.id)
    if (!pos) return

    const colorMap = { subject: 0x4ade80, type: 0xfb923c, knowledge: 0xa855f7 }
    const baseColor = colorMap[node.type] || 0x6c5ce7

    // 核心球体 (高光材质)
    const geometry = new THREE.SphereGeometry(0.55, 64, 64)
    const material = new THREE.MeshStandardMaterial({
      color: baseColor,
      emissive: baseColor,
      emissiveIntensity: 0.35,
      metalness: 0.9,
      roughness: 0.15,
      flatShading: false
    })
    const sphere = new THREE.Mesh(geometry, material)
    sphere.position.copy(pos)
    sphere.castShadow = true
    sphere.userData = { nodeData: node }
    sphere.userData.onClick = () => {
      selectedNode.value = {
        id: node.id, name: node.name, type: node.type,
        connections: edges.filter(e => e.source === node.id || e.target === node.id).length,
        mastered: node.mastered, reviewCount: node.reviewCount,
        sampleMistake: node.sampleMistake, subject: node.subject
      }
    }
    nodesGroup.add(sphere)

    // 外层光晕 (粒子环)
    const glowGeometry = new THREE.SphereGeometry(0.75, 32, 32)
    const glowMaterial = new THREE.MeshBasicMaterial({
      color: baseColor,
      transparent: true,
      opacity: 0.2,
      side: THREE.BackSide
    })
    const glowSphere = new THREE.Mesh(glowGeometry, glowMaterial)
    glowSphere.position.copy(pos)
    nodesGroup.add(glowSphere)

    // 环绕粒子环
    const ringCount = 30
    const ringPositions = []
    for (let i = 0; i < ringCount; i++) {
      const angle = (i / ringCount) * Math.PI * 2
      const radius = 0.85
      ringPositions.push(pos.x + Math.cos(angle) * radius, pos.y + Math.sin(angle) * radius, pos.z)
    }
    const ringGeo = new THREE.BufferGeometry()
    ringGeo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(ringPositions), 3))
    const ringMat = new THREE.PointsMaterial({ color: baseColor, size: 0.05, transparent: true, blending: THREE.AdditiveBlending })
    const ringPoints = new THREE.Points(ringGeo, ringMat)
    nodesGroup.add(ringPoints)

    // CSS2D文字标签 (更精致)
    const div = document.createElement('div')
    div.textContent = node.name
    div.style.color = 'white'
    div.style.fontSize = '13px'
    div.style.fontWeight = '600'
    div.style.textShadow = '0 0 15px rgba(0,0,0,0.8)'
    div.style.background = 'rgba(0,0,0,0.65)'
    div.style.padding = '5px 14px'
    div.style.borderRadius = '24px'
    div.style.backdropFilter = 'blur(12px)'
    div.style.border = '1px solid rgba(255,255,255,0.25)'
    div.style.whiteSpace = 'nowrap'
    div.style.fontFamily = 'Inter, sans-serif'
    div.style.letterSpacing = '0.3px'
    const label = new CSS2DObject(div)
    label.position.copy(pos)
    label.position.y += 0.85
    nodesGroup.add(label)
  })

  scene.add(nodesGroup)
  scene.add(edgesGroup)
  scene.add(glowGroup)
}

let raycaster, mouse
const setupRaycaster = () => {
  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()
  window.addEventListener('click', (event) => {
    if (!renderer || !camera || !nodesGroup) return
    const rect = renderer.domElement.getBoundingClientRect()
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
    raycaster.setFromCamera(mouse, camera)
    const intersects = raycaster.intersectObjects(nodesGroup.children)
    if (intersects.length > 0) {
      const hit = intersects[0].object
      if (hit.userData && hit.userData.onClick) hit.userData.onClick()
    }
  })
}

const toggleType = (type) => {
  showTypes.value[type] = !showTypes.value[type]
  buildGraph()
}

watch([selectedSubject, showTypes], () => buildGraph(), { deep: true })

onMounted(() => {
  initThree()
  setupRaycaster()
  buildGraph()
  setTimeout(() => { if (controls) controls.autoRotateSpeed = 0.5 }, 4000)
})

onUnmounted(() => {
  if (rafId) cancelAnimationFrame(rafId)
  if (renderer) renderer.dispose()
  if (labelRenderer) labelRenderer.domElement.remove()
})
</script>

<style scoped>
@keyframes slideInRight {
  from { opacity: 0; transform: translateX(40px); }
  to { opacity: 1; transform: translateX(0); }
}
.animate-slideInRight {
  animation: slideInRight 0.35s cubic-bezier(0.2, 0.9, 0.4, 1.1) forwards;
}
</style>
