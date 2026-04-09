<!--



<template>
  <div class="campus-3d-container">
    <div ref="canvasContainer" class="canvas-container"></div>

    &lt;!&ndash; 控制面板 &ndash;&gt;
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

    &lt;!&ndash; 信息提示 &ndash;&gt;
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
-->
<template>
  <div class="knowledge-graph-container">
    <!-- 控制栏 -->
    <div class="graph-controls mb-4 flex flex-wrap gap-3 items-center justify-between">
      <div class="flex gap-2 flex-wrap">
        <button
          @click="setViewType('category')"
          :class="[
            'px-3 py-1.5 rounded-lg text-sm font-medium transition-all',
            viewType === 'category'
              ? 'bg-indigo-500 text-white shadow-md'
              : isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          📂 按分类聚合
        </button>
        <button
          @click="setViewType('tag')"
          :class="[
            'px-3 py-1.5 rounded-lg text-sm font-medium transition-all',
            viewType === 'tag'
              ? 'bg-indigo-500 text-white shadow-md'
              : isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          🏷️ 按标签聚合
        </button>
        <button
          @click="setViewType('network')"
          :class="[
            'px-3 py-1.5 rounded-lg text-sm font-medium transition-all',
            viewType === 'network'
              ? 'bg-indigo-500 text-white shadow-md'
              : isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          🔗 关联网络
        </button>
      </div>

      <div class="flex gap-2">
        <button
          @click="zoomIn"
          class="w-8 h-8 rounded-lg flex items-center justify-center transition-all"
          :class="isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          title="放大"
        >
          ➕
        </button>
        <button
          @click="zoomOut"
          class="w-8 h-8 rounded-lg flex items-center justify-center transition-all"
          :class="isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          title="缩小"
        >
          ➖
        </button>
        <button
          @click="resetView"
          class="w-8 h-8 rounded-lg flex items-center justify-center transition-all"
          :class="isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          title="重置视图"
        >
          🔄
        </button>
        <button
          @click="toggleFullscreen"
          class="w-8 h-8 rounded-lg flex items-center justify-center transition-all"
          :class="isDark ? 'bg-gray-800 text-gray-400 hover:bg-gray-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          title="全屏"
        >
          ⛶
        </button>
      </div>
    </div>

    <!-- 图表面板 -->
    <div
      ref="graphContainer"
      class="graph-container relative rounded-xl overflow-hidden border transition-all"
      :class="isDark ? 'bg-gray-900 border-gray-700' : 'bg-white border-gray-200'"
      :style="{ height: height + 'px' }"
    >
      <svg
        ref="svgElement"
        :width="containerWidth"
        :height="height"
        class="graph-svg"
        @click="handleSvgClick"
      >
        <!-- 连线层 -->
        <g class="edges-layer">
          <g
            v-for="edge in edges"
            :key="edge.id"
            class="edge-group"
          >
            <!-- 主连线 -->
            <line
              :x1="edge.source.x"
              :y1="edge.source.y"
              :x2="edge.target.x"
              :y2="edge.target.y"
              :stroke="edge.color"
              :stroke-width="edge.width || 1.5"
              :stroke-dasharray="edge.dasharray || ''"
              :opacity="edge.opacity || 0.6"
              class="edge-line"
            />
            <!-- 动画粒子（仅在关联网络模式下） -->
            <circle
              v-if="viewType === 'network' && edge.animated"
              r="3"
              :fill="edge.color"
              class="edge-particle"
              :style="{
                animation: `flowParticle ${edge.duration || 2}s linear infinite`
              }"
            />
          </g>
        </g>

        <!-- 节点层 -->
        <g class="nodes-layer">
          <g
            v-for="node in nodes"
            :key="node.id"
            class="node-group"
            :transform="`translate(${node.x}, ${node.y})`"
            @mouseenter="hoverNode(node)"
            @mouseleave="unhoverNode"
            @click="selectNode(node)"
          >
            <!-- 节点光晕 -->
            <circle
              v-if="selectedNodeId === node.id"
              r="32"
              fill="none"
              stroke="#8b5cf6"
              stroke-width="2"
              :opacity="0.5"
              class="node-glow"
            />
            <!-- 节点背景 -->
            <circle
              :r="node.radius || 24"
              :fill="node.color || getNodeColor(node.type)"
              :stroke="selectedNodeId === node.id ? '#fff' : node.stroke || '#fff'"
              :stroke-width="selectedNodeId === node.id ? 3 : 1.5"
              :opacity="node.opacity || 0.9"
              class="node-circle cursor-pointer transition-all"
            />
            <!-- 节点图标 -->
            <text
              :y="4"
              text-anchor="middle"
              fill="#fff"
              font-size="16"
              class="node-icon"
            >
              {{ node.icon || getNodeIcon(node.type) }}
            </text>
            <!-- 节点标签 -->
            <text
              :y="(node.radius || 24) + 16"
              text-anchor="middle"
              :fill="isDark ? '#e5e7eb' : '#374151'"
              font-size="11"
              class="node-label font-medium"
            >
              {{ node.label }}
            </text>
            <!-- 节点数值（数量） -->
            <text
              v-if="node.count && node.count > 1"
              :y="-(node.radius || 24) - 4"
              text-anchor="middle"
              fill="#f59e0b"
              font-size="10"
              font-weight="bold"
              class="node-count"
            >
              {{ node.count }}
            </text>
          </g>
        </g>
      </svg>

      <!-- 加载状态 -->
      <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-black/50 backdrop-blur-sm">
        <div class="text-center">
          <div class="animate-spin text-3xl mb-2">⏳</div>
          <div class="text-white">加载图谱中...</div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="nodes.length === 0" class="absolute inset-0 flex items-center justify-center">
        <div class="text-center">
          <div class="text-6xl mb-4">📊</div>
          <div class="text-gray-400">暂无数据，请先创建内容</div>
        </div>
      </div>

      <!-- 节点详情浮窗 -->
      <div
        v-if="hoveredNode"
        class="node-tooltip absolute z-20 p-3 rounded-xl shadow-xl max-w-[250px] transition-all"
        :class="isDark ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'"
        :style="{ left: hoveredNode.tooltipX + 'px', top: hoveredNode.tooltipY + 'px' }"
      >
        <div class="flex items-center gap-2 mb-2">
          <span class="text-xl">{{ hoveredNode.icon || getNodeIcon(hoveredNode.type) }}</span>
          <h4 class="font-semibold" :class="isDark ? 'text-white' : 'text-gray-900'">{{ hoveredNode.label }}</h4>
        </div>
        <p class="text-sm mb-2" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
          {{ getNodeDescription(hoveredNode) }}
        </p>
        <div class="flex flex-wrap gap-1">
          <span class="text-xs px-2 py-0.5 rounded-full" :class="isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-100 text-gray-600'">
            📄 {{ hoveredNode.contentCount || hoveredNode.count || 0 }} 篇内容
          </span>
        </div>
      </div>
    </div>

    <!-- 图例说明 -->
    <div class="graph-legend mt-4 flex flex-wrap gap-4 justify-center text-xs">
      <div class="flex items-center gap-1">
        <span class="w-3 h-3 rounded-full bg-indigo-500"></span>
        <span class="text-gray-500">文章</span>
      </div>
      <div class="flex items-center gap-1">
        <span class="w-3 h-3 rounded-full bg-purple-500"></span>
        <span class="text-gray-500">日记</span>
      </div>
      <div class="flex items-center gap-1">
        <span class="w-3 h-3 rounded-full bg-pink-500"></span>
        <span class="text-gray-500">随笔</span>
      </div>
      <div class="flex items-center gap-1">
        <span class="w-3 h-3 rounded-full bg-emerald-500"></span>
        <span class="text-gray-500">技术</span>
      </div>
      <div class="flex items-center gap-1">
        <span class="w-3 h-3 rounded-full bg-orange-500"></span>
        <span class="text-gray-500">标签节点</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import request from '@/utils/request.js'

const props = defineProps({
  isDark: {
    type: Boolean,
    default: false
  },
  height: {
    type: Number,
    default: 500
  },
  userId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['nodeClick', 'refresh'])

// 状态
const loading = ref(false)
const viewType = ref('category') // category, tag, network
const nodes = ref([])
const edges = ref([])
const selectedNodeId = ref(null)
const hoveredNode = ref(null)
const svgElement = ref(null)
const graphContainer = ref(null)
const containerWidth = ref(800)

// 缩放相关
let zoomLevel = 1
let panX = 0
let panY = 0
let isDragging = false
let dragStart = { x: 0, y: 0 }

// 颜色映射
const categoryColors = {
  '随笔': { color: '#8b5cf6', lightColor: '#c4b5fd' },
  '技术': { color: '#10b981', lightColor: '#6ee7b7' },
  '生活': { color: '#f59e0b', lightColor: '#fcd34d' },
  '旅行': { color: '#06b6d4', lightColor: '#67e8f9' },
  '读书': { color: '#ef4444', lightColor: '#fca5a5' },
  '日记': { color: '#ec4899', lightColor: '#f9a8d4' }
}

const contentTypeColors = {
  'article': '#8b5cf6',
  'journal': '#ec4899',
  'essay': '#f59e0b',
  'note': '#10b981'
}

const getNodeColor = (type) => {
  if (viewType.value === 'category') {
    return categoryColors[type]?.color || '#6b7280'
  } else if (viewType.value === 'tag') {
    return '#f59e0b'
  }
  return contentTypeColors[type] || '#8b5cf6'
}

const getNodeIcon = (type) => {
  const iconMap = {
    '随笔': '✍️',
    '技术': '💻',
    '生活': '🌿',
    '旅行': '✈️',
    '读书': '📚',
    '日记': '📖',
    'article': '📄',
    'journal': '📔',
    'essay': '✏️',
    'note': '📝'
  }
  return iconMap[type] || '📄'
}

const getNodeDescription = (node) => {
  if (viewType.value === 'category') {
    return `${node.label}分类下共有 ${node.count || 0} 篇内容`
  } else if (viewType.value === 'tag') {
    return `标签"${node.label}"被用于 ${node.count || 0} 篇内容`
  }
  return node.contentTitle || node.label
}

// 加载分类图谱数据
const loadCategoryGraph = async () => {
  loading.value = true
  try {
    const res = await request.get('/content/statistics/by-category', { params: { userId: props.userId } })
    const data = res.data || []

    const centerX = containerWidth.value / 2
    const centerY = props.height / 2
    const radius = Math.min(containerWidth.value, props.height) * 0.35

    const newNodes = data.map((item, idx) => {
      const angle = (idx / data.length) * Math.PI * 2 - Math.PI / 2
      return {
        id: `cat_${item.category}`,
        label: item.category,
        type: item.category,
        count: item.count,
        x: centerX + radius * Math.cos(angle),
        y: centerY + radius * Math.sin(angle),
        radius: 28 + Math.min(item.count / 10, 12),
        color: categoryColors[item.category]?.color || '#6b7280'
      }
    })

    nodes.value = newNodes
    edges.value = [] // 分类视图无边
  } catch (error) {
    console.error('加载分类图谱失败', error)
  } finally {
    loading.value = false
  }
}

// 加载标签图谱数据
const loadTagGraph = async () => {
  loading.value = true
  try {
    const res = await request.get('/content/statistics/by-tag', { params: { userId: props.userId, limit: 20 } })
    const data = res.data || []

    const centerX = containerWidth.value / 2
    const centerY = props.height / 2
    const radius = Math.min(containerWidth.value, props.height) * 0.4

    // 按使用频率排序，频率高的放在中心附近
    const sortedData = [...data].sort((a, b) => b.count - a.count)
    const maxCount = sortedData[0]?.count || 1

    const newNodes = sortedData.map((item, idx) => {
      // 动态半径：使用频率越高半径越大
      const nodeRadius = 20 + (item.count / maxCount) * 16
      // 动态距离：使用频率越高离中心越近
      const distance = radius * (1 - (item.count / maxCount) * 0.5)
      const angle = (idx / sortedData.length) * Math.PI * 2 - Math.PI / 2

      return {
        id: `tag_${item.tag}`,
        label: item.tag,
        type: 'tag',
        count: item.count,
        x: centerX + distance * Math.cos(angle),
        y: centerY + distance * Math.sin(angle),
        radius: nodeRadius,
        color: '#f59e0b'
      }
    })

    nodes.value = newNodes
    edges.value = []
  } catch (error) {
    console.error('加载标签图谱失败', error)
  } finally {
    loading.value = false
  }
}

// 加载关联网络数据
const loadNetworkGraph = async () => {
  loading.value = true
  try {
    const res = await request.get('/content/network/relations', { params: { userId: props.userId, depth: 2 } })
    const data = res.data || { nodes: [], edges: [] }

    // 布局计算 - 力导向布局的简化版本
    const centerX = containerWidth.value / 2
    const centerY = props.height / 2
    const radius = Math.min(containerWidth.value, props.height) * 0.35

    const contentNodes = data.nodes.map((node, idx) => {
      const angle = (idx / data.nodes.length) * Math.PI * 2 - Math.PI / 2
      return {
        id: node.id,
        label: node.title?.length > 15 ? node.title.slice(0, 12) + '...' : node.title,
        type: node.contentType || 'article',
        contentTitle: node.title,
        x: centerX + radius * Math.cos(angle),
        y: centerY + radius * Math.sin(angle),
        radius: 28,
        color: contentTypeColors[node.contentType] || '#8b5cf6'
      }
    })

    const relationEdges = data.edges.map((edge, idx) => ({
      id: `edge_${idx}`,
      sourceId: edge.sourceId,
      targetId: edge.targetId,
      source: contentNodes.find(n => n.id === edge.sourceId),
      target: contentNodes.find(n => n.id === edge.targetId),
      color: '#9ca3af',
      width: 1.5,
      animated: true,
      duration: 3 + Math.random() * 2
    })).filter(e => e.source && e.target)

    nodes.value = contentNodes
    edges.value = relationEdges

    // 重新计算连线坐标
    updateEdgeCoordinates()
  } catch (error) {
    console.error('加载关联网络失败', error)
  } finally {
    loading.value = false
  }
}

// 更新连线的端点坐标
const updateEdgeCoordinates = () => {
  edges.value.forEach(edge => {
    const sourceNode = nodes.value.find(n => n.id === edge.sourceId)
    const targetNode = nodes.value.find(n => n.id === edge.targetId)
    if (sourceNode && targetNode) {
      edge.source = sourceNode
      edge.target = targetNode
    }
  })
}

// 设置视图类型
const setViewType = (type) => {
  viewType.value = type
  selectedNodeId.value = null
  loadGraphData()
}

// 根据视图类型加载数据
const loadGraphData = () => {
  if (viewType.value === 'category') {
    loadCategoryGraph()
  } else if (viewType.value === 'tag') {
    loadTagGraph()
  } else {
    loadNetworkGraph()
  }
}

// 节点悬停
const hoverNode = (node) => {
  const rect = graphContainer.value?.getBoundingClientRect()
  if (rect) {
    hoveredNode.value = {
      ...node,
      tooltipX: node.x + 20,
      tooltipY: node.y - 40
    }
  }
}

const unhoverNode = () => {
  hoveredNode.value = null
}

// 选择节点
const selectNode = (node) => {
  selectedNodeId.value = node.id
  emit('nodeClick', node)
}

// 缩放控制
const zoomIn = () => {
  zoomLevel = Math.min(zoomLevel + 0.1, 2)
  applyTransform()
}

const zoomOut = () => {
  zoomLevel = Math.max(zoomLevel - 0.1, 0.5)
  applyTransform()
}

const resetView = () => {
  zoomLevel = 1
  panX = 0
  panY = 0
  applyTransform()
}

const applyTransform = () => {
  if (svgElement.value) {
    svgElement.value.style.transform = `translate(${panX}px, ${panY}px) scale(${zoomLevel})`
    svgElement.value.style.transformOrigin = 'center'
  }
}

// SVG 交互事件
const handleSvgClick = (event) => {
  if (isDragging) {
    isDragging = false
    return
  }
}

// 全屏切换
const toggleFullscreen = () => {
  const container = graphContainer.value
  if (!container) return

  if (!document.fullscreenElement) {
    container.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 窗口大小变化时重新布局
const handleResize = () => {
  if (graphContainer.value) {
    containerWidth.value = graphContainer.value.clientWidth
    loadGraphData()
  }
}

// 监听容器宽度变化
watch(() => containerWidth.value, () => {
  loadGraphData()
})

onMounted(() => {
  if (graphContainer.value) {
    containerWidth.value = graphContainer.value.clientWidth
  }
  loadGraphData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

defineExpose({
  refresh: loadGraphData
})
</script>

<style scoped>
.graph-container {
  position: relative;
  overflow: hidden;
}

.graph-svg {
  transition: transform 0.3s ease;
  cursor: grab;
}

.graph-svg:active {
  cursor: grabbing;
}

.node-circle {
  transition: r 0.2s ease, filter 0.2s ease;
  cursor: pointer;
}

.node-circle:hover {
  filter: brightness(1.1);
  r: 28;
}

.node-glow {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.3;
    r: 32;
  }
  50% {
    opacity: 0.6;
    r: 36;
  }
}

@keyframes flowParticle {
  0% {
    stroke-dashoffset: 0;
  }
  100% {
    stroke-dashoffset: 100;
  }
}

.edge-particle {
  animation: flowParticle 2s linear infinite;
}

.node-tooltip {
  animation: fadeIn 0.15s ease-out;
  pointer-events: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.graph-legend {
  border-top: 1px solid;
  border-color: #e5e7eb;
  padding-top: 12px;
}

.dark .graph-legend {
  border-color: #374151;
}
</style>
