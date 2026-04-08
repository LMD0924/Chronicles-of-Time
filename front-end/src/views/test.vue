


<template>
  <div class="campus-3d-container">
    <div ref="canvasContainer" class="canvas-container"></div>

    <!-- 控制面板 -->
    <div class="controls-panel" :class="isDark ? 'dark' : ''">
      <div class="control-group">
        <h4>🏫 校园导览</h4>
        <div class="building-list">
          <button
            v-for="building in buildings"
            :key="building.id"
            @click="focusBuilding(building)"
            class="building-btn"
            :class="{ active: activeBuilding === building.id }"
          >
            <span>{{ building.icon }}</span>
            <span>{{ building.name }}</span>
          </button>
        </div>
      </div>

      <div class="control-group">
        <h4>🎮 视角控制</h4>
        <div class="view-buttons">
          <button @click="setView('overview')" class="view-btn">🏙️ 全景</button>
          <button @click="setView('top')" class="view-btn">⬇️ 俯视</button>
          <button @click="setView('side')" class="view-btn">↗️ 侧视</button>
          <button @click="resetCamera" class="view-btn">🔄 重置</button>
        </div>
      </div>

      <div class="control-group">
        <h4>🌤️ 环境</h4>
        <div class="env-buttons">
          <button @click="setTime('day')" class="env-btn" :class="{ active: currentTime === 'day' }">☀️ 白天</button>
          <button @click="setTime('night')" class="env-btn" :class="{ active: currentTime === 'night' }">🌙 夜晚</button>
          <button @click="setWeather('clear')" class="env-btn" :class="{ active: currentWeather === 'clear' }">☀️ 晴朗</button>
          <button @click="setWeather('cloudy')" class="env-btn" :class="{ active: currentWeather === 'cloudy' }">☁️ 多云</button>
        </div>
      </div>
    </div>

    <!-- 信息提示 -->
    <div v-if="selectedBuilding" class="info-card" :class="isDark ? 'dark' : ''">
      <h3>{{ selectedBuilding.name }}</h3>
      <p>{{ selectedBuilding.description }}</p>
      <div class="info-detail">
        <span>📐 建筑面积: {{ selectedBuilding.area }}</span>
        <span>📅 建成时间: {{ selectedBuilding.year }}</span>
      </div>
      <button @click="closeInfo" class="close-btn">×</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js'

const props = defineProps({
  isDark: {
    type: Boolean,
    default: false
  }
})

const canvasContainer = ref(null)
let scene, camera, renderer, labelRenderer, controls
let animationId
const buildings = ref([])
const activeBuilding = ref(null)
const selectedBuilding = ref(null)
let raycaster, mouse

// 环境状态
const currentTime = ref('day')
const currentWeather = ref('clear')

// 建筑数据
const campusBuildings = [
  {
    id: 1,
    name: '图书馆',
    icon: '📚',
    position: { x: -8, y: 0, z: -5 },
    size: { width: 4, height: 5, depth: 4 },
    color: 0x8B4513,
    description: '藏书百万册，是学校的精神殿堂',
    area: '8000㎡',
    year: '2010年'
  },
  {
    id: 2,
    name: '教学楼A座',
    icon: '🏫',
    position: { x: 0, y: 0, z: -8 },
    size: { width: 5, height: 4, depth: 3 },
    color: 0xCD853F,
    description: '主要教学楼，配备现代化教室',
    area: '12000㎡',
    year: '2012年'
  },
  {
    id: 3,
    name: '实验楼',
    icon: '🔬',
    position: { x: 8, y: 0, z: -5 },
    size: { width: 3.5, height: 4.5, depth: 3.5 },
    color: 0xBC8F8F,
    description: '各类实验室齐全，科研设备先进',
    area: '6000㎡',
    year: '2015年'
  },
  {
    id: 4,
    name: '学生食堂',
    icon: '🍽️',
    position: { x: -5, y: 0, z: 8 },
    size: { width: 4, height: 3, depth: 4 },
    color: 0xDEB887,
    description: '美食天堂，汇聚各地风味',
    area: '5000㎡',
    year: '2008年'
  },
  {
    id: 5,
    name: '体育馆',
    icon: '🏀',
    position: { x: 6, y: 0, z: 7 },
    size: { width: 5, height: 3.5, depth: 5 },
    color: 0xD2B48C,
    description: '室内体育馆，篮球场、羽毛球场',
    area: '7000㎡',
    year: '2018年'
  },
  {
    id: 6,
    name: '学生公寓',
    icon: '🏢',
    position: { x: -7, y: 0, z: 3 },
    size: { width: 6, height: 6, depth: 3 },
    color: 0xF5DEB3,
    description: '舒适的学生宿舍，生活便利',
    area: '15000㎡',
    year: '2011年'
  },
  {
    id: 7,
    name: '行政楼',
    icon: '🏛️',
    position: { x: 0, y: 0, z: 5 },
    size: { width: 3, height: 4, depth: 3 },
    color: 0xDAA520,
    description: '学校行政办公中心',
    area: '4000㎡',
    year: '2005年'
  },
  {
    id: 8,
    name: '艺术中心',
    icon: '🎨',
    position: { x: 9, y: 0, z: 0 },
    size: { width: 3.5, height: 3.5, depth: 3.5 },
    color: 0xB8860B,
    description: '艺术展览、演出场地',
    area: '3500㎡',
    year: '2019年'
  }
]

// 地面纹理（草地效果）
const createGroundTexture = () => {
  const canvas = document.createElement('canvas')
  canvas.width = 512
  canvas.height = 512
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#4a7c59'
  ctx.fillRect(0, 0, 512, 512)
  for (let i = 0; i < 2000; i++) {
    ctx.fillStyle = `rgba(60, 100, 70, ${Math.random() * 0.5})`
    ctx.fillRect(Math.random() * 512, Math.random() * 512, 2, 2)
  }
  const texture = new THREE.CanvasTexture(canvas)
  texture.wrapS = THREE.RepeatWrapping
  texture.wrapT = THREE.RepeatWrapping
  texture.repeat.set(20, 20)
  return texture
}

// 创建立方体建筑
const createBuilding = (building) => {
  const geometry = new THREE.BoxGeometry(building.size.width, building.size.height, building.size.depth)
  const material = new THREE.MeshStandardMaterial({
    color: building.color,
    roughness: 0.3,
    metalness: 0.1,
    emissive: currentTime.value === 'night' ? 0x222222 : 0x000000
  })
  const mesh = new THREE.Mesh(geometry, material)
  mesh.position.set(building.position.x, building.size.height / 2, building.position.z)
  mesh.userData = { building }
  mesh.castShadow = true
  mesh.receiveShadow = true

  // 添加屋顶
  const roofGeo = new THREE.ConeGeometry(building.size.width * 0.8, 1, 4)
  const roofMat = new THREE.MeshStandardMaterial({ color: 0x8B0000 })
  const roof = new THREE.Mesh(roofGeo, roofMat)
  roof.position.y = building.size.height / 2
  roof.castShadow = true
  mesh.add(roof)

  return mesh
}

// 创建标签
const createLabel = (building) => {
  const div = document.createElement('div')
  div.textContent = `${building.icon} ${building.name}`
  div.style.color = '#fff'
  div.style.backgroundColor = 'rgba(0,0,0,0.7)'
  div.style.padding = '4px 12px'
  div.style.borderRadius = '20px'
  div.style.fontSize = '14px'
  div.style.fontWeight = 'bold'
  div.style.border = '1px solid rgba(255,255,255,0.3)'
  div.style.backdropFilter = 'blur(4px)'
  div.style.cursor = 'pointer'
  div.style.transition = 'all 0.3s'
  div.addEventListener('mouseenter', () => {
    div.style.transform = 'scale(1.05)'
    div.style.backgroundColor = 'rgba(99,102,241,0.9)'
  })
  div.addEventListener('mouseleave', () => {
    div.style.transform = 'scale(1)'
    div.style.backgroundColor = 'rgba(0,0,0,0.7)'
  })

  const label = new CSS2DObject(div)
  label.position.set(building.position.x, building.size.height + 0.8, building.position.z)
  return label
}

// 创建树木
const createTree = (x, z) => {
  const group = new THREE.Group()

  // 树干
  const trunkGeo = new THREE.CylinderGeometry(0.5, 0.7, 1.5)
  const trunkMat = new THREE.MeshStandardMaterial({ color: 0x8B5A2B })
  const trunk = new THREE.Mesh(trunkGeo, trunkMat)
  trunk.position.y = 0.75
  trunk.castShadow = true
  group.add(trunk)

  // 树叶（三层）
  const leafMat = new THREE.MeshStandardMaterial({ color: 0x3CB371 })
  const leaf1 = new THREE.Mesh(new THREE.ConeGeometry(0.8, 1.2, 8), leafMat)
  leaf1.position.y = 1.5
  leaf1.castShadow = true
  group.add(leaf1)

  const leaf2 = new THREE.Mesh(new THREE.ConeGeometry(0.6, 1, 8), leafMat)
  leaf2.position.y = 2.3
  leaf2.castShadow = true
  group.add(leaf2)

  const leaf3 = new THREE.Mesh(new THREE.ConeGeometry(0.4, 0.8, 8), leafMat)
  leaf3.position.y = 3
  leaf3.castShadow = true
  group.add(leaf3)

  group.position.set(x, 0, z)
  return group
}

// 创建路灯
const createStreetLight = (x, z) => {
  const group = new THREE.Group()

  // 灯柱
  const poleGeo = new THREE.CylinderGeometry(0.2, 0.3, 3)
  const poleMat = new THREE.MeshStandardMaterial({ color: 0x696969, metalness: 0.8 })
  const pole = new THREE.Mesh(poleGeo, poleMat)
  pole.position.y = 1.5
  pole.castShadow = true
  group.add(pole)

  // 灯头
  const lampGeo = new THREE.SphereGeometry(0.3)
  const lampMat = new THREE.MeshStandardMaterial({
    color: 0xFFD700,
    emissive: currentTime.value === 'night' ? 0xFF6600 : 0x442200,
    emissiveIntensity: currentTime.value === 'night' ? 0.5 : 0
  })
  const lamp = new THREE.Mesh(lampGeo, lampMat)
  lamp.position.y = 3
  lamp.castShadow = true
  group.add(lamp)

  group.position.set(x, 0, z)
  return group
}

// 创建道路
const createRoad = () => {
  const roadMat = new THREE.MeshStandardMaterial({ color: 0x555555, roughness: 0.8 })

  // 主路（十字形）
  const road1 = new THREE.Mesh(new THREE.PlaneGeometry(3, 25), roadMat)
  road1.rotation.x = -Math.PI / 2
  road1.position.set(0, 0.01, 0)
  road1.receiveShadow = true

  const road2 = new THREE.Mesh(new THREE.PlaneGeometry(25, 3), roadMat)
  road2.rotation.x = -Math.PI / 2
  road2.position.set(0, 0.01, 0)
  road2.receiveShadow = true

  return [road1, road2]
}

// 创建草地
const createLawn = () => {
  const lawnMat = new THREE.MeshStandardMaterial({
    map: createGroundTexture(),
    roughness: 0.9,
    metalness: 0.1
  })
  const lawn = new THREE.Mesh(new THREE.PlaneGeometry(40, 40), lawnMat)
  lawn.rotation.x = -Math.PI / 2
  lawn.position.y = -0.05
  lawn.receiveShadow = true
  return lawn
}

// 添加灯光效果
const setupLights = () => {
  // 环境光
  const ambientLight = new THREE.AmbientLight(0x404060, currentTime.value === 'night' ? 0.3 : 0.6)
  scene.add(ambientLight)

  // 主光源（太阳）
  const directionalLight = new THREE.DirectionalLight(0xffffff, 1)
  directionalLight.position.set(10, 20, 5)
  directionalLight.castShadow = true
  directionalLight.shadow.mapSize.width = 1024
  directionalLight.shadow.mapSize.height = 1024
  directionalLight.shadow.camera.near = 0.5
  directionalLight.shadow.camera.far = 50
  directionalLight.shadow.camera.left = -15
  directionalLight.shadow.camera.right = 15
  directionalLight.shadow.camera.top = 15
  directionalLight.shadow.camera.bottom = -15
  scene.add(directionalLight)

  // 补光
  const fillLight = new THREE.PointLight(0x4466cc, 0.3)
  fillLight.position.set(0, 10, 0)
  scene.add(fillLight)

  // 背光暖色补光
  const backLight = new THREE.PointLight(0xffaa66, 0.2)
  backLight.position.set(-5, 5, -10)
  scene.add(backLight)

  return { directionalLight, ambientLight, fillLight }
}

// 设置时间（白天/夜晚）
const setTime = (time) => {
  currentTime.value = time
  const { directionalLight, ambientLight } = lights
  if (time === 'day') {
    directionalLight.intensity = 1
    ambientLight.intensity = 0.6
    scene.background = new THREE.Color(0x87CEEB)
  } else {
    directionalLight.intensity = 0.2
    ambientLight.intensity = 0.3
    scene.background = new THREE.Color(0x0a0a2a)
  }
}

// 设置天气
const setWeather = (weather) => {
  currentWeather.value = weather
  if (weather === 'cloudy') {
    scene.background = new THREE.Color(0x888888)
    lights.directionalLight.intensity = 0.5
  } else {
    setTime(currentTime.value)
  }
}

// 聚焦建筑
const focusBuilding = (building) => {
  activeBuilding.value = building.id
  const targetPos = new THREE.Vector3(building.position.x, building.size.height + 2, building.position.z)
  controls.target.copy(targetPos)

  // 平滑移动相机
  const cameraOffset = new THREE.Vector3(8, 5, 8)
  const newCameraPos = targetPos.clone().add(cameraOffset)

  // 动画移动相机
  const animateCamera = () => {
    const startPos = camera.position.clone()
    const startTarget = controls.target.clone()
    const duration = 500
    const startTime = performance.now()

    const step = (now) => {
      const elapsed = now - startTime
      const t = Math.min(1, elapsed / duration)
      const ease = 1 - Math.pow(1 - t, 3)

      camera.position.lerpVectors(startPos, newCameraPos, ease)
      controls.target.lerpVectors(startTarget, targetPos, ease)
      controls.update()

      if (t < 1) {
        requestAnimationFrame(step)
      }
    }

    requestAnimationFrame(step)
  }

  animateCamera()
  selectedBuilding.value = building
}

// 设置视角
const setView = (view) => {
  switch (view) {
    case 'overview':
      camera.position.set(15, 12, 15)
      controls.target.set(0, 2, 0)
      break
    case 'top':
      camera.position.set(0, 20, 0.1)
      controls.target.set(0, 0, 0)
      break
    case 'side':
      camera.position.set(20, 5, 0)
      controls.target.set(0, 2, 0)
      break
  }
  controls.update()
}

// 重置相机
const resetCamera = () => {
  camera.position.set(15, 10, 15)
  controls.target.set(0, 2, 0)
  controls.update()
  activeBuilding.value = null
}

// 关闭信息卡片
const closeInfo = () => {
  selectedBuilding.value = null
}

// 初始化场景
const initScene = () => {
  const container = canvasContainer.value
  const width = container.clientWidth
  const height = container.clientHeight

  // 创建场景
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x87CEEB)
  scene.fog = new THREE.Fog(0x87CEEB, 30, 50)

  // 创建相机
  camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  camera.position.set(15, 10, 15)

  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(width, height)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.appendChild(renderer.domElement)

  // CSS2渲染器用于文字标签
  labelRenderer = new CSS2DRenderer()
  labelRenderer.setSize(width, height)
  labelRenderer.domElement.style.position = 'absolute'
  labelRenderer.domElement.style.top = '0px'
  labelRenderer.domElement.style.left = '0px'
  labelRenderer.domElement.style.pointerEvents = 'none'
  container.appendChild(labelRenderer.domElement)

  // 轨道控制
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.autoRotate = false
  controls.enableZoom = true
  controls.enablePan = true
  controls.zoomSpeed = 1
  controls.panSpeed = 0.8
  controls.rotateSpeed = 0.8

  // 添加辅助元素
  const gridHelper = new THREE.GridHelper(40, 20, 0x888888, 0x444444)
  gridHelper.position.y = -0.1
  scene.add(gridHelper)

  // 添加草地
  const lawn = createLawn()
  scene.add(lawn)

  // 添加道路
  const roads = createRoad()
  roads.forEach(road => scene.add(road))

  // 添加建筑
  campusBuildings.forEach(building => {
    const buildingMesh = createBuilding(building)
    scene.add(buildingMesh)
    buildings.value.push(building)

    // 添加标签
    const label = createLabel(building)
    scene.add(label)
  })

  // 添加树木（围绕建筑周边）
  const treePositions = [
    [-12, -8], [-10, -10], [-8, -12], [-6, -13], [8, -12], [10, -10], [12, -8],
    [-13, 5], [-12, 8], [-10, 10], [10, 10], [12, 8], [13, 5],
    [-13, -3], [13, -3], [-3, -13], [3, -13]
  ]
  treePositions.forEach(([x, z]) => {
    const tree = createTree(x, z)
    scene.add(tree)
  })

  // 添加路灯
  const lightPositions = [
    [-10, -5], [-5, -10], [5, -10], [10, -5],
    [-10, 5], [-5, 10], [5, 10], [10, 5],
    [0, -12], [0, 12], [-12, 0], [12, 0]
  ]
  lightPositions.forEach(([x, z]) => {
    const light = createStreetLight(x, z)
    scene.add(light)
  })

  // 添加灯光系统
  const lights = setupLights()

  // 射线检测用于建筑点击
  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()

  renderer.domElement.addEventListener('click', onCanvasClick)
}

// 处理画布点击
const onCanvasClick = (event) => {
  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects(scene.children, true)

  for (const intersect of intersects) {
    let obj = intersect.object
    while (obj.parent) {
      if (obj.userData && obj.userData.building) {
        selectedBuilding.value = obj.userData.building
        activeBuilding.value = obj.userData.building.id
        return
      }
      obj = obj.parent
    }
  }
}

// 动画循环
const animate = () => {
  animationId = requestAnimationFrame(animate)
  controls.update()
  renderer.render(scene, camera)
  labelRenderer.render(scene, camera)
}

// 处理窗口大小变化
const handleResize = () => {
  const container = canvasContainer.value
  const width = container.clientWidth
  const height = container.clientHeight
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
  labelRenderer.setSize(width, height)
}

onMounted(() => {
  initScene()
  animate()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
  if (renderer) {
    renderer.dispose()
  }
})
</script>

<style scoped>
.campus-3d-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.canvas-container {
  width: 100%;
  height: 100%;
}

/* 控制面板 */
.controls-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 16px;
  min-width: 200px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 10;
  transition: all 0.3s;
}

.controls-panel.dark {
  background: rgba(30, 30, 40, 0.95);
  color: #e5e5e5;
}

.control-group {
  margin-bottom: 16px;
}

.control-group h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 600;
}

.building-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.building-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 20px;
  background: #f0f0f0;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.dark .building-btn {
  background: #3a3a4a;
  color: #e5e5e5;
}

.building-btn:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
}

.dark .building-btn:hover {
  background: #4a4a5a;
}

.building-btn.active {
  background: #6366f1;
  color: white;
}

.view-buttons, .env-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.view-btn, .env-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 20px;
  background: #f0f0f0;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.dark .view-btn, .dark .env-btn {
  background: #3a3a4a;
  color: #e5e5e5;
}

.view-btn:hover, .env-btn:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
}

.dark .view-btn:hover, .dark .env-btn:hover {
  background: #4a4a5a;
}

.env-btn.active {
  background: #10b981;
  color: white;
}

/* 信息卡片 */
.info-card {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 16px 20px;
  max-width: 300px;
  z-index: 10;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease;
}

.info-card.dark {
  background: rgba(30, 30, 40, 0.95);
  color: #e5e5e5;
}

.info-card h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.info-card p {
  margin: 0 0 12px 0;
  font-size: 14px;
  line-height: 1.5;
}

.info-detail {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #666;
}

.dark .info-detail {
  color: #aaa;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: #f0f0f0;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.dark .close-btn {
  background: #4a4a5a;
  color: #e5e5e5;
}

.close-btn:hover {
  background: #e0e0e0;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .controls-panel {
    top: auto;
    bottom: 20px;
    right: 20px;
    max-width: 180px;
  }

  .info-card {
    max-width: 250px;
    font-size: 12px;
  }

  .building-btn, .view-btn, .env-btn {
    padding: 4px 8px;
    font-size: 10px;
  }
}
</style>
