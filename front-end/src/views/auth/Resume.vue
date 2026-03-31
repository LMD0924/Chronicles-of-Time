<script setup>
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getStoredTheme, onThemeChange, ThemeType } from '@/utils/theme'
import {message} from "ant-design-vue";

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const activeTab = ref('full')
const isNavScrolled = ref(false)
const isDark = ref(getStoredTheme() === ThemeType.DARK)
const [messageApi,contextHolder] = message.useMessage();

// 简历基本信息
const resumeInfo = ref({
  id: null,
  name: '',
  title: '',
  email: '',
  phone: '',
  address: '',
  birthDate: '',
  summary: '',
  avatar: ''
})

// 工作经历
const workExperiences = ref([])
const workDialogVisible = ref(false)
const workForm = ref({
  id: null,
  companyName: '',
  position: '',
  startDate: '',
  endDate: '',
  isCurrent: false,
  description: '',
  achievements: '',
  sortOrder: 0
})
const isEditWork = ref(false)

// 教育经历
const educations = ref([])
const educationDialogVisible = ref(false)
const educationForm = ref({
  id: null,
  schoolName: '',
  degree: '',
  major: '',
  startDate: '',
  endDate: '',
  isCurrent: false,
  description: '',
  sortOrder: 0
})
const isEditEducation = ref(false)

// 项目经验
const projects = ref([])
const projectDialogVisible = ref(false)
const projectForm = ref({
  id: null,
  projectName: '',
  projectRole: '',
  startDate: '',
  endDate: '',
  description: '',
  responsibilities: '',
  techStack: '',
  projectUrl: '',
  sortOrder: 0
})
const isEditProject = ref(false)

// 技能特长
const skills = ref([])
const skillDialogVisible = ref(false)
const skillForm = ref({
  id: null,
  skillName: '',
  skillLevel: '',
  yearsExperience: null,
  sortOrder: 0
})
const isEditSkill = ref(false)

// 证书
const certificates = ref([])
const certDialogVisible = ref(false)
const certForm = ref({
  id: null,
  certificateName: '',
  issueAuthority: '',
  issueDate: '',
  score: '',
  description: '',
  sortOrder: 0
})
const isEditCert = ref(false)

// 社会经历
const socialExperiences = ref([])
const socialDialogVisible = ref(false)
const socialForm = ref({
  id: null,
  experienceType: '',
  title: '',
  startDate: '',
  endDate: '',
  description: '',
  achievements: '',
  sortOrder: 0
})
const isEditSocial = ref(false)

// 右侧提示字段
const fieldTips = ref({
  basic: [
    { label: '姓名', placeholder: '真实姓名', example: '张三' },
    { label: '求职意向', placeholder: '期望职位', example: 'Java开发工程师' },
    { label: '邮箱', placeholder: '常用邮箱', example: 'zhangsan@example.com' },
    { label: '电话', placeholder: '手机号码', example: '138****0000' },
    { label: '出生日期', placeholder: 'YYYY-MM-DD', example: '1995-01-01' },
    { label: '所在城市', placeholder: '工作城市', example: '北京' },
    { label: '个人简介', placeholder: '自我介绍', example: 'X年工作经验，擅长...' }
  ],
  work: [
    { label: '公司名称', placeholder: '公司全称', example: '阿里巴巴集团' },
    { label: '职位名称', placeholder: '具体职位', example: 'Java开发工程师' },
    { label: '在职时间', placeholder: 'YYYY-MM-DD', example: '2020-01 - 至今' },
    { label: '工作职责', placeholder: '主要工作内容', example: '负责核心模块开发...' },
    { label: '工作成就', placeholder: '量化成果', example: '系统性能提升30%' }
  ],
  education: [
    { label: '学校名称', placeholder: '学校全称', example: '清华大学' },
    { label: '学历', placeholder: '最高学历', example: '本科 / 硕士 / 博士' },
    { label: '专业', placeholder: '专业名称', example: '计算机科学与技术' },
    { label: '就读时间', placeholder: 'YYYY-MM-DD', example: '2015-09 - 2019-06' },
    { label: '荣誉/描述', placeholder: '获奖情况', example: '校级优秀毕业生' }
  ],
  project: [
    { label: '项目名称', placeholder: '项目名称', example: '电商平台系统' },
    { label: '项目角色', placeholder: '担任角色', example: '核心开发 / 项目经理' },
    { label: '项目时间', placeholder: 'YYYY-MM-DD', example: '2023-01 - 2023-06' },
    { label: '技术栈', placeholder: '使用技术', example: 'Spring Boot, Vue.js, MySQL' },
    { label: '项目描述', placeholder: '项目背景', example: '这是一个...' },
    { label: '职责内容', placeholder: '具体职责', example: '负责订单模块设计...' }
  ],
  skill: [
    { label: '技能名称', placeholder: '技术/工具', example: 'Java, Python, Docker' },
    { label: '技能等级', placeholder: '熟练程度', example: '精通 / 熟练 / 入门' },
    { label: '经验年限', placeholder: '使用年限', example: '3年' }
  ],
  certificate: [
    { label: '证书名称', placeholder: '证书全称', example: 'CET-6 / PMP' },
    { label: '颁发机构', placeholder: '颁发单位', example: '教育部考试中心' },
    { label: '获得日期', placeholder: 'YYYY-MM-DD', example: '2022-06' },
    { label: '分数/等级', placeholder: '成绩', example: '优秀 / 90分' }
  ],
  social: [
    { label: '经历类型', placeholder: '类型', example: '社会实践 / 志愿服务' },
    { label: '标题', placeholder: '活动名称', example: 'XX大学学生会主席' },
    { label: '时间', placeholder: 'YYYY-MM-DD', example: '2021-09 - 2022-06' },
    { label: '描述', placeholder: '活动内容', example: '组织策划校园活动...' },
    { label: '成就', placeholder: '获得成果', example: '获得优秀组织奖' }
  ]
})

// 学历选项
const degreeOptions = [
  { label: '博士', value: '博士' },
  { label: '硕士', value: '硕士' },
  { label: '本科', value: '本科' },
  { label: '大专', value: '大专' },
  { label: '高中', value: '高中' }
]

// 技能等级选项
const skillLevelOptions = [
  { label: '入门', value: '入门' },
  { label: '熟练', value: '熟练' },
  { label: '精通', value: '精通' },
  { label: '专家', value: '专家' }
]

// 经历类型选项
const experienceTypeOptions = [
  { label: '社会实践', value: '社会实践' },
  { label: '社团活动', value: '社团活动' },
  { label: '志愿服务', value: '志愿服务' },
  { label: '竞赛获奖', value: '竞赛获奖' }
]

// 计算简历完整度
const completeness = computed(() => {
  let total = 0
  let filled = 0

  // 基本信息
  if (resumeInfo.value.name) filled++
  if (resumeInfo.value.title) filled++
  if (resumeInfo.value.email) filled++
  if (resumeInfo.value.phone) filled++
  total += 4

  // 工作经历
  if (workExperiences.value.length > 0) filled++
  total++

  // 教育经历
  if (educations.value.length > 0) filled++
  total++

  // 项目经验
  if (projects.value.length > 0) filled++
  total++

  // 技能
  if (skills.value.length > 0) filled++
  total++

  // 证书
  if (certificates.value.length > 0) filled++
  total++

  return Math.round((filled / total) * 100)
})

// 获取简历信息
const fetchResumeInfo = async () => {
  loading.value = true
  try {
    await request.get('/resume/getCompleteResume', {}, (msg, data) => {
      if (data && data.resume) {
        resumeInfo.value = {
          id: data.resume.id != null ? String(data.resume.id) : null,
          name: data.resume.name || '',
          title: data.resume.jobTitle || '',
          email: data.resume.email || '',
          phone: data.resume.phone || '',
          address: data.resume.address || '',
          birthDate: data.resume.birthDate || '',
          summary: data.resume.selfEvaluation || '',
          avatar: data.resume.avatar || ''
        }

        workExperiences.value = data.workExperienceList || []
        educations.value = data.educationList || []
        projects.value = data.projectList || []
        skills.value = data.skillList || []
        certificates.value = data.certificateList || []
        socialExperiences.value = data.socialExperienceList || []
      } else {
        resumeInfo.value = {
          id: null,
          name: '',
          title: '',
          email: '',
          phone: '',
          address: '',
          birthDate: '',
          summary: '',
          avatar: ''
        }
        workExperiences.value = []
        educations.value = []
        projects.value = []
        skills.value = []
        certificates.value = []
        socialExperiences.value = []
      }
    })
  } catch (error) {
    messageApi.error('获取简历信息失败')
  } finally {
    loading.value = false
    setTimeout(() => {
      document.querySelectorAll('.fade-in-up').forEach(el => {
        el.classList.add('show')
      })
    }, 100)
  }
}

// 保存基本信息
const saveBasicInfo = async () => {
  try {
    const resumeData = {
      id: resumeInfo.value.id,
      name: resumeInfo.value.name,
      jobTitle: resumeInfo.value.title,
      email: resumeInfo.value.email,
      phone: resumeInfo.value.phone,
      address: resumeInfo.value.address,
      birthDate: resumeInfo.value.birthDate,
      selfEvaluation: resumeInfo.value.summary,
      avatar: resumeInfo.value.avatar
    }

    const response = await request.post('/resume/addResume', resumeData, (msg, data) => {
      messageApi.success('保存成功')
    })
    if (response.code === 200) {
      // 后端返回的是真实 resumeId（雪花 Long），避免前端把 insert 行数当作 id
      resumeInfo.value.id = response.data != null ? String(response.data) : null
      // 重新拉取完整简历，确保各列表用正确 resumeId 展示
      await fetchResumeInfo()
    }
  } catch (error) {
    messageApi.error('保存失败')
  }
}

// 工作经历 CRUD
const addWork = () => {
  isEditWork.value = false
  workForm.value = {
    id: null,
    companyName: '',
    position: '',
    startDate: '',
    endDate: '',
    isCurrent: false,
    description: '',
    achievements: '',
    sortOrder: workExperiences.value.length
  }
  workDialogVisible.value = true
}

const editWork = (item) => {
  isEditWork.value = true
  workForm.value = { ...item }
  workDialogVisible.value = true
}

const saveWork = async () => {
  try {
    if (!isEditWork.value && !resumeInfo.value.id) {
      messageApi.warning('请先保存基本信息')
      return
    }

    if (isEditWork.value) {
      await request.post('/resume/updateWorkExperience', workForm.value, (msg, data) => {
        const index = workExperiences.value.findIndex(w => w.id === workForm.value.id)
        if (index !== -1) {
          workExperiences.value[index] = data
        }
        messageApi.success('更新成功')
      })
    } else {
      workForm.value.resumeId = resumeInfo.value.id
      await request.post('/resume/addWorkExperience', workForm.value, (msg, data) => {
        workExperiences.value.push(data)
        messageApi.success('添加成功')
      })
    }
    workDialogVisible.value = false
    await fetchResumeInfo()
  } catch (error) {
    messageApi.error('操作失败')
  }
}

const deleteWork = async (id) => {
  ElMessageBox.confirm('确定删除这条工作经历吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post('/resume/deleteWorkExperience', { id }, (msg, data) => {
      workExperiences.value = workExperiences.value.filter(w => w.id !== id)
      messageApi.success('删除成功')
    })
    await fetchResumeInfo()
  }).catch(() => {})
}

// 教育经历 CRUD
const addEducation = () => {
  isEditEducation.value = false
  educationForm.value = {
    id: null,
    schoolName: '',
    degree: '',
    major: '',
    startDate: '',
    endDate: '',
    isCurrent: false,
    description: '',
    sortOrder: educations.value.length
  }
  educationDialogVisible.value = true
}

const editEducation = (item) => {
  isEditEducation.value = true
  educationForm.value = { ...item }
  educationDialogVisible.value = true
}

const saveEducation = async () => {
  try {
    if (!isEditEducation.value && !resumeInfo.value.id) {
      messageApi.warning('请先保存基本信息')
      return
    }

    if (isEditEducation.value) {
      await request.post('/resume/updateEducation', educationForm.value, (msg, data) => {
        const index = educations.value.findIndex(e => e.id === educationForm.value.id)
        if (index !== -1) {
          educations.value[index] = data
        }
        messageApi.success('更新成功')
      })
    } else {
      educationForm.value.resumeId = resumeInfo.value.id
      await request.post('/resume/addEducation', educationForm.value, (msg, data) => {
        educations.value.push(data)
        messageApi.success('添加成功')
      })
    }
    educationDialogVisible.value = false
    await fetchResumeInfo()
  } catch (error) {
    messageApi.error('操作失败')
  }
}

const deleteEducation = async (id) => {
  ElMessageBox.confirm('确定删除这条教育经历吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post('/resume/deleteEducation', { id }, (msg, data) => {
      educations.value = educations.value.filter(e => e.id !== id)
      messageApi.success('删除成功')
    })
    await fetchResumeInfo()
  }).catch(() => {})
}

// 项目经验 CRUD
const addProject = () => {
  isEditProject.value = false
  projectForm.value = {
    id: null,
    projectName: '',
    projectRole: '',
    startDate: '',
    endDate: '',
    description: '',
    responsibilities: '',
    techStack: '',
    projectUrl: '',
    sortOrder: projects.value.length
  }
  projectDialogVisible.value = true
}

const editProject = (item) => {
  isEditProject.value = true
  projectForm.value = { ...item }
  projectDialogVisible.value = true
}

const saveProject = async () => {
  try {
    if (!isEditProject.value && !resumeInfo.value.id) {
      messageApi.warning('请先保存基本信息')
      return
    }

    if (isEditProject.value) {
      await request.post('/resume/updateProject', projectForm.value, (msg, data) => {
        const index = projects.value.findIndex(p => p.id === projectForm.value.id)
        if (index !== -1) {
          projects.value[index] = data
        }
        messageApi.success('更新成功')
      })
    } else {
      projectForm.value.resumeId = resumeInfo.value.id
      await request.post('/resume/addProject', projectForm.value, (msg, data) => {
        projects.value.push(data)
        messageApi.success('添加成功')
      })
    }
    projectDialogVisible.value = false
    await fetchResumeInfo()
  } catch (error) {
    messageApi.error('操作失败')
  }
}

const deleteProject = async (id) => {
  ElMessageBox.confirm('确定删除这个项目经验吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post('/resume/deleteProject', { id }, (msg, data) => {
      projects.value = projects.value.filter(p => p.id !== id)
      messageApi.success('删除成功')
    })
    await fetchResumeInfo()
  }).catch(() => {})
}

// 技能 CRUD
const addSkill = () => {
  isEditSkill.value = false
  skillForm.value = {
    id: null,
    skillName: '',
    skillLevel: '',
    yearsExperience: null,
    sortOrder: skills.value.length
  }
  skillDialogVisible.value = true
}

const editSkill = (item) => {
  isEditSkill.value = true
  skillForm.value = { ...item }
  skillDialogVisible.value = true
}

const saveSkill = async () => {
  try {
    if (!isEditSkill.value && !resumeInfo.value.id) {
      messageApi.warning('请先保存基本信息')
      return
    }

    if (isEditSkill.value) {
      await request.post('/resume/updateSkill', skillForm.value, (msg, data) => {
        const index = skills.value.findIndex(s => s.id === skillForm.value.id)
        if (index !== -1) {
          skills.value[index] = data
        }
        messageApi.success('更新成功')
      })
    } else {
      skillForm.value.resumeId = resumeInfo.value.id
      await request.post('/resume/addSkill', skillForm.value, (msg, data) => {
        skills.value.push(data)
        messageApi.success('添加成功')
      })
    }
    skillDialogVisible.value = false
    await fetchResumeInfo()
  } catch (error) {
    messageApi.error('操作失败')
  }
}

const deleteSkill = async (id) => {
  ElMessageBox.confirm('确定删除这个技能吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post('/resume/deleteSkill', { id }, (msg, data) => {
      skills.value = skills.value.filter(s => s.id !== id)
      messageApi.success('删除成功')
    })
    await fetchResumeInfo()
  }).catch(() => {})
}

// 证书 CRUD
const addCertificate = () => {
  isEditCert.value = false
  certForm.value = {
    id: null,
    certificateName: '',
    issueAuthority: '',
    issueDate: '',
    score: '',
    description: '',
    sortOrder: certificates.value.length
  }
  certDialogVisible.value = true
}

const editCertificate = (item) => {
  isEditCert.value = true
  certForm.value = { ...item }
  certDialogVisible.value = true
}

const saveCertificate = async () => {
  try {
    if (!isEditCert.value && !resumeInfo.value.id) {
      messageApi.warning('请先保存基本信息')
      return
    }

    if (isEditCert.value) {
      await request.post('/resume/updateCertificate', certForm.value, (msg, data) => {
        const index = certificates.value.findIndex(c => c.id === certForm.value.id)
        if (index !== -1) {
          certificates.value[index] = data
        }
        messageApi.success('更新成功')
      })
    } else {
      certForm.value.resumeId = resumeInfo.value.id
      await request.post('/resume/addCertificate', certForm.value, (msg, data) => {
        certificates.value.push(data)
        messageApi.success('添加成功')
      })
    }
    certDialogVisible.value = false
    await fetchResumeInfo()
  } catch (error) {
    messageApi.error('操作失败')
  }
}

const deleteCertificate = async (id) => {
  ElMessageBox.confirm('确定删除这个证书吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post('/resume/deleteCertificate', { id }, (msg, data) => {
      certificates.value = certificates.value.filter(c => c.id !== id)
      messageApi.success('删除成功')
    })
    await fetchResumeInfo()
  }).catch(() => {})
}

// 社会经历 CRUD
const addSocial = () => {
  isEditSocial.value = false
  socialForm.value = {
    id: null,
    experienceType: '',
    title: '',
    startDate: '',
    endDate: '',
    description: '',
    achievements: '',
    sortOrder: socialExperiences.value.length
  }
  socialDialogVisible.value = true
}

const editSocial = (item) => {
  isEditSocial.value = true
  socialForm.value = { ...item }
  socialDialogVisible.value = true
}

const saveSocial = async () => {
  try {
    if (!isEditSocial.value && !resumeInfo.value.id) {
      messageApi.warning('请先保存基本信息')
      return
    }

    if (isEditSocial.value) {
      await request.post('/resume/updateSocialExperience', socialForm.value, (msg, data) => {
        const index = socialExperiences.value.findIndex(s => s.id === socialForm.value.id)
        if (index !== -1) {
          socialExperiences.value[index] = data
        }
        messageApi.success('更新成功')
      })
    } else {
      socialForm.value.resumeId = resumeInfo.value.id
      await request.post('/resume/addSocialExperience', socialForm.value, (msg, data) => {
        socialExperiences.value.push(data)
        messageApi.success('添加成功')
      })
    }
    socialDialogVisible.value = false
    await fetchResumeInfo()
  } catch (error) {
    messageApi.error('操作失败')
  }
}

const deleteSocial = async (id) => {
  ElMessageBox.confirm('确定删除这条经历吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.post('/resume/deleteSocialExperience', { id }, (msg, data) => {
      socialExperiences.value = socialExperiences.value.filter(s => s.id !== id)
      messageApi.success('删除成功')
    })
    await fetchResumeInfo()
  }).catch(() => {})
}

const handleScroll = () => {
  isNavScrolled.value = window.scrollY > 10
}

const previewResume = () => {
  router.push('/resume/preview')
}

const exportResume = () => {
  messageApi.success('正在生成简历PDF...')
}

onMounted(() => {
  fetchResumeInfo()
  window.addEventListener('scroll', handleScroll)

  const stopListen = onThemeChange((theme) => {
    isDark.value = theme === ThemeType.DARK
  })

  onUnmounted(() => {
    stopListen()
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <contextHolder />
  <div :class="[isDark ? 'bg-black text-white' : 'bg-gradient-to-br from-gray-50 via-white to-indigo-50/30 text-gray-900', 'min-h-screen']">
    <div class="flex justify-between items-center">
      <!-- 左侧占位，保持平衡 -->
      <div class="w-32"></div>

      <!-- 中间：个人简历按钮 -->
      <div class="mt-4 shadow-lg" :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'flex items-center gap-1 rounded-full p-1 shadow-sm']">
        <button
          @click="$router.push('/Resume')"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        route.path === '/Resume'
          ? (isDark ? 'bg-indigo-900/50 text-indigo-400' : 'bg-indigo-100 text-indigo-600 shadow-lg')
          : (isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600')
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">👤</span>
        <span>个人简历</span>
      </span>
        </button>
        <button
          @click="$router.push('/PersonalProfile')"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        route.path === '/PersonalProfile'
          ? (isDark ? 'bg-indigo-900/50 text-indigo-400' : 'bg-indigo-100 text-indigo-600')
          : (isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600')
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">👤</span>
        <span>个人档案</span>
      </span>
        </button>
      </div>

      <!-- 右侧按钮组 -->
      <div class="mt-4 mr-60" :class="[isDark ? 'bg-gray-900/80 backdrop-blur-sm' : 'bg-gray-50/80 backdrop-blur-sm', 'flex items-center gap-1 rounded-full p-1 shadow-sm']">
        <button
          @click="exportResume"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        isDark ? 'text-gray-300 hover:text-green-400' : 'text-gray-600 hover:text-green-600'
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">📄</span>
        <span>导出PDF</span>
      </span>
        </button>
        <button
          @click="$router.push('/home')"
          class="relative px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 overflow-hidden group hover:shadow-lg"
          :class="[
        isDark ? 'text-gray-300 hover:text-indigo-400' : 'text-gray-600 hover:text-indigo-600'
      ]"
        >
      <span class="relative flex items-center gap-2 z-10">
        <span class="text-base">🏠</span>
        <span>返回首页</span>
      </span>
        </button>
      </div>
    </div>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex gap-6">
        <!-- 左侧内容区域 -->
        <div class="flex-1">
          <!-- 标签页切换 -->
          <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white border border-gray-100', 'rounded-xl shadow-sm overflow-hidden mb-8 fade-in-up']">
            <div :class="[isDark ? 'border-white/20' : 'border-gray-100', 'flex flex-wrap border-b']">
              <button
                v-for="tab in [
                  { id: 'full', name: '预览简历', icon: '📋' },
                  { id: 'basic', name: '基本信息', icon: '👤' },
                  { id: 'work', name: '工作经历', icon: '💼' },
                  { id: 'education', name: '教育背景', icon: '🎓' },
                  { id: 'project', name: '项目经验', icon: '🚀' },
                  { id: 'skill', name: '技能特长', icon: '⚡' },
                  { id: 'certificate', name: '证书荣誉', icon: '🏅' },
                  { id: 'social', name: '社会经历', icon: '🌱' }
                ]"
                :key="tab.id"
                @click="activeTab = tab.id"
                class="px-4 py-3 text-center transition-all duration-300 relative group"
                :class="activeTab === tab.id ? 'text-indigo-600 font-medium' : (isDark ? 'text-gray-400 hover:text-gray-300' : 'text-gray-500 hover:text-gray-700')"
              >
                <span class="text-lg mr-1 inline-block group-hover:scale-110 transition-transform">{{ tab.icon }}</span>
                <span class="text-sm">{{ tab.name }}</span>
                <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-indigo-500 to-purple-600"></div>
              </button>
            </div>

            <div class="p-6">
              <!-- 完整简历视图 -->
              <div v-if="activeTab === 'full'" class="tab-content">
                <!-- 简历头部 -->
                <div class="mb-8 pb-6 border-b" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
                  <div class="flex items-start justify-between">
                    <div>
                      <h1 class="text-3xl font-bold mb-2" :class="isDark ? 'text-white' : 'text-gray-800'">{{ resumeInfo.name || '待完善' }}</h1>
                      <p class="text-lg mb-2" :class="isDark ? 'text-gray-300' : 'text-gray-600'">{{ resumeInfo.title || '求职意向：待填写' }}</p>
                      <div class="flex flex-wrap gap-4 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                        <span v-if="resumeInfo.email">📧 {{ resumeInfo.email }}</span>
                        <span v-if="resumeInfo.phone">📱 {{ resumeInfo.phone }}</span>
                        <span v-if="resumeInfo.address">📍 {{ resumeInfo.address }}</span>
                        <span v-if="resumeInfo.birthDate">🎂 {{ resumeInfo.birthDate }}</span>
                      </div>
                    </div>
                    <div class="text-right">
                      <div class="text-sm mb-2">简历完整度</div>
                      <div class="relative inline-flex items-center justify-center">
                        <svg class="w-20 h-20 transform -rotate-90">
                          <circle cx="40" cy="40" r="32" stroke="currentColor" stroke-width="4" fill="none" class="text-gray-200"/>
                          <circle cx="40" cy="40" r="32" stroke="currentColor" stroke-width="4" fill="none" :stroke-dasharray="`${completeness * 2.01}, 201`" class="text-indigo-500 transition-all duration-1000"/>
                        </svg>
                        <span class="absolute text-xl font-bold">{{ completeness }}%</span>
                      </div>
                    </div>
                  </div>
                  <p v-if="resumeInfo.summary" class="mt-4" :class="isDark ? 'text-gray-300' : 'text-gray-600'">{{ resumeInfo.summary }}</p>
                </div>

                <!-- 工作经历 -->
                <div v-if="workExperiences.length > 0" class="mb-6">
                  <h3 class="text-lg font-semibold mb-3 flex items-center gap-2">
                    <span class="w-1 h-5 bg-indigo-500 rounded-full"></span>
                    工作经历
                  </h3>
                  <div class="space-y-4">
                    <div v-for="(item, idx) in workExperiences" :key="idx" class="pl-4 border-l-2" :class="isDark ? 'border-indigo-500/50' : 'border-indigo-300'">
                      <div class="flex justify-between items-start flex-wrap">
                        <div>
                          <h4 class="font-semibold" :class="isDark ? 'text-white' : 'text-gray-800'">{{ item.companyName }}</h4>
                          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.position }}</p>
                        </div>
                        <span :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-sm">{{ item.startDate }} - {{ item.isCurrent ? '至今' : item.endDate }}</span>
                      </div>
                      <p v-if="item.description" class="mt-2 text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">{{ item.description }}</p>
                      <p v-if="item.achievements" class="mt-1 text-sm text-indigo-500">✨ {{ item.achievements }}</p>
                    </div>
                  </div>
                </div>

                <!-- 教育经历 -->
                <div v-if="educations.length > 0" class="mb-6">
                  <h3 class="text-lg font-semibold mb-3 flex items-center gap-2">
                    <span class="w-1 h-5 bg-indigo-500 rounded-full"></span>
                    教育背景
                  </h3>
                  <div class="space-y-4">
                    <div v-for="(item, idx) in educations" :key="idx" class="pl-4 border-l-2" :class="isDark ? 'border-purple-500/50' : 'border-purple-300'">
                      <div class="flex justify-between items-start flex-wrap">
                        <div>
                          <h4 class="font-semibold" :class="isDark ? 'text-white' : 'text-gray-800'">{{ item.schoolName }}</h4>
                          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.degree }} · {{ item.major }}</p>
                        </div>
                        <span :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-sm">{{ item.startDate }} - {{ item.isCurrent ? '至今' : item.endDate }}</span>
                      </div>
                      <p v-if="item.description" class="mt-2 text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">{{ item.description }}</p>
                    </div>
                  </div>
                </div>

                <!-- 项目经验 -->
                <div v-if="projects.length > 0" class="mb-6">
                  <h3 class="text-lg font-semibold mb-3 flex items-center gap-2">
                    <span class="w-1 h-5 bg-indigo-500 rounded-full"></span>
                    项目经验
                  </h3>
                  <div class="space-y-4">
                    <div v-for="(item, idx) in projects" :key="idx" class="pl-4 border-l-2" :class="isDark ? 'border-green-500/50' : 'border-green-300'">
                      <div class="flex justify-between items-start flex-wrap">
                        <div>
                          <h4 class="font-semibold" :class="isDark ? 'text-white' : 'text-gray-800'">{{ item.projectName }}</h4>
                          <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm">{{ item.projectRole }}</p>
                        </div>
                        <span :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-sm">{{ item.startDate }} - {{ item.endDate || '至今' }}</span>
                      </div>
                      <p v-if="item.description" class="mt-2 text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">{{ item.description }}</p>
                      <p v-if="item.techStack" class="mt-1 text-xs" :class="isDark ? 'text-indigo-400' : 'text-indigo-600'">技术栈：{{ item.techStack }}</p>
                    </div>
                  </div>
                </div>

                <!-- 技能特长 -->
                <div v-if="skills.length > 0" class="mb-6">
                  <h3 class="text-lg font-semibold mb-3 flex items-center gap-2">
                    <span class="w-1 h-5 bg-indigo-500 rounded-full"></span>
                    技能特长
                  </h3>
                  <div class="flex flex-wrap gap-2">
                    <span v-for="(item, idx) in skills" :key="idx" :class="[isDark ? 'bg-indigo-500/20 text-indigo-300 border-indigo-500/30' : 'bg-indigo-50 text-indigo-700 border-indigo-200', 'px-3 py-1 rounded-full text-sm border']">
                      {{ item.skillName }} <span class="text-xs opacity-75">{{ item.skillLevel }}</span>
                    </span>
                  </div>
                </div>

                <!-- 证书荣誉 -->
                <div v-if="certificates.length > 0" class="mb-6">
                  <h3 class="text-lg font-semibold mb-3 flex items-center gap-2">
                    <span class="w-1 h-5 bg-indigo-500 rounded-full"></span>
                    证书荣誉
                  </h3>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div v-for="(item, idx) in certificates" :key="idx" :class="[isDark ? 'bg-white/5 border-white/10' : 'bg-gray-50 border-gray-100', 'rounded-lg p-3 border']">
                      <div class="font-medium">{{ item.certificateName }}</div>
                      <div class="text-xs mt-1" :class="isDark ? 'text-gray-400' : 'text-gray-500'">{{ item.issueAuthority }} · {{ item.issueDate }}</div>
                    </div>
                  </div>
                </div>

                <!-- 社会经历 -->
                <div v-if="socialExperiences.length > 0" class="mb-6">
                  <h3 class="text-lg font-semibold mb-3 flex items-center gap-2">
                    <span class="w-1 h-5 bg-indigo-500 rounded-full"></span>
                    社会/校园经历
                  </h3>
                  <div class="space-y-3">
                    <div v-for="(item, idx) in socialExperiences" :key="idx" class="pl-4 border-l-2" :class="isDark ? 'border-cyan-500/50' : 'border-cyan-300'">
                      <div class="flex justify-between items-start flex-wrap">
                        <div>
                          <h4 class="font-semibold" :class="isDark ? 'text-white' : 'text-gray-800'">{{ item.title }}</h4>
                          <span :class="[isDark ? 'bg-cyan-500/20 text-cyan-400' : 'bg-cyan-100 text-cyan-600', 'text-xs px-2 py-0.5 rounded-full inline-block mt-1']">{{ item.experienceType }}</span>
                        </div>
                        <span :class="isDark ? 'text-gray-500' : 'text-gray-400'" class="text-sm">{{ item.startDate }} - {{ item.endDate || '至今' }}</span>
                      </div>
                      <p v-if="item.description" class="mt-2 text-sm" :class="isDark ? 'text-gray-300' : 'text-gray-600'">{{ item.description }}</p>
                    </div>
                  </div>
                </div>

                <!-- 空状态提示 -->
                <div v-if="workExperiences.length === 0 && educations.length === 0 && projects.length === 0 && skills.length === 0" class="text-center py-12" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                  <div class="text-6xl mb-4">📝</div>
                  <p>暂无简历内容，请点击上方标签页添加信息</p>
                </div>
              </div>

              <!-- 基本信息 -->
              <div v-if="activeTab === 'basic'" class="tab-content">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">姓名 *</label>
                    <input v-model="resumeInfo.name" type="text" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200', 'w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400']" placeholder="请输入真实姓名">
                  </div>
                  <div>
                    <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">求职意向</label>
                    <input v-model="resumeInfo.title" type="text" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200', 'w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400']" placeholder="例如：Java开发工程师">
                  </div>
                  <div>
                    <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">邮箱</label>
                    <input v-model="resumeInfo.email" type="email" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200', 'w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400']" placeholder="example@email.com">
                  </div>
                  <div>
                    <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">电话</label>
                    <input v-model="resumeInfo.phone" type="tel" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200', 'w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400']" placeholder="手机号码">
                  </div>
                  <div>
                    <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">出生日期</label>
                    <input v-model="resumeInfo.birthDate" type="date" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200', 'w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400']">
                  </div>
                  <div>
                    <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">所在城市</label>
                    <input v-model="resumeInfo.address" type="text" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200', 'w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400']" placeholder="北京 / 上海 / 深圳">
                  </div>
                  <div class="md:col-span-2">
                    <label :class="isDark ? 'text-gray-400' : 'text-gray-600'" class="block text-sm mb-2">个人简介</label>
                    <textarea v-model="resumeInfo.summary" rows="4" :class="[isDark ? 'bg-white/5 border-white/20 text-white' : 'bg-white border-gray-200', 'w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400']" placeholder="简要介绍自己，突出优势和特点..."></textarea>
                  </div>
                </div>
                <div class="mt-6 flex justify-end">
                  <button @click="saveBasicInfo" class="px-6 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-lg hover:shadow-lg transition-all hover:scale-105">
                    保存基本信息
                  </button>
                </div>
              </div>

              <!-- 工作经历 -->
              <div v-if="activeTab === 'work'" class="tab-content">
                <div class="flex justify-between items-center mb-4">
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="text-lg font-semibold">工作经历</h3>
                  <button @click="addWork" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition-all hover:scale-105 text-sm">+ 添加经历</button>
                </div>
                <div v-if="workExperiences.length === 0" class="text-center py-12" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                  <div class="text-6xl mb-4">💼</div>
                  <p>暂无工作经历，点击上方按钮添加</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="(item, idx) in workExperiences" :key="idx" :class="[isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50 border border-gray-100', 'rounded-lg p-4 hover:shadow-md transition-all']">
                    <div class="flex justify-between items-start">
                      <div class="flex-1">
                        <div class="flex items-center gap-3 flex-wrap mb-2">
                          <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold text-lg">{{ item.companyName }}</h4>
                          <span :class="[isDark ? 'bg-indigo-500/20 text-indigo-400' : 'bg-indigo-100 text-indigo-600', 'px-2 py-1 rounded-full text-xs']">{{ item.position }}</span>
                        </div>
                        <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm mb-2">{{ item.startDate }} - {{ item.isCurrent ? '至今' : item.endDate }}</p>
                        <p v-if="item.description" :class="isDark ? 'text-gray-300' : 'text-gray-600'" class="text-sm mb-1">{{ item.description }}</p>
                      </div>
                      <div class="flex gap-2">
                        <button @click="editWork(item)" :class="[isDark ? 'hover:text-indigo-400' : 'hover:text-indigo-600', 'text-gray-500 transition-colors']">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                        <button @click="deleteWork(item.id)" :class="[isDark ? 'hover:text-red-400' : 'hover:text-red-600', 'text-gray-500 transition-colors']">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 其他模块内容保持不变 -->
              <div v-if="activeTab === 'education'" class="tab-content">
                <div class="flex justify-between items-center mb-4">
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="text-lg font-semibold">教育背景</h3>
                  <button @click="addEducation" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition-all hover:scale-105 text-sm">+ 添加经历</button>
                </div>
                <div v-if="educations.length === 0" class="text-center py-12" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                  <div class="text-6xl mb-4">🎓</div>
                  <p>暂无教育经历，点击上方按钮添加</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="(item, idx) in educations" :key="idx" :class="[isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50 border border-gray-100', 'rounded-lg p-4 hover:shadow-md transition-all']">
                    <div class="flex justify-between items-start">
                      <div class="flex-1">
                        <div class="flex items-center gap-3 flex-wrap mb-2">
                          <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold text-lg">{{ item.schoolName }}</h4>
                          <span :class="[isDark ? 'bg-purple-500/20 text-purple-400' : 'bg-purple-100 text-purple-600', 'px-2 py-1 rounded-full text-xs']">{{ item.degree }}</span>
                        </div>
                        <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm mb-2">{{ item.startDate }} - {{ item.isCurrent ? '至今' : item.endDate }}</p>
                      </div>
                      <div class="flex gap-2">
                        <button @click="editEducation(item)" class="text-gray-500 hover:text-indigo-600 transition-colors">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                        <button @click="deleteEducation(item.id)" class="text-gray-500 hover:text-red-600 transition-colors">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 其他模块快速展示（项目、技能、证书、社会经历） -->
              <div v-if="activeTab === 'project'" class="tab-content">
                <div class="flex justify-between items-center mb-4">
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="text-lg font-semibold">项目经验</h3>
                  <button @click="addProject" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition-all hover:scale-105 text-sm">+ 添加项目</button>
                </div>
                <div v-if="projects.length === 0" class="text-center py-12" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                  <div class="text-6xl mb-4">🚀</div>
                  <p>暂无项目经验，点击上方按钮添加</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="(item, idx) in projects" :key="idx" :class="[isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50 border border-gray-100', 'rounded-lg p-4 hover:shadow-md transition-all']">
                    <div class="flex justify-between items-start">
                      <div class="flex-1">
                        <div class="flex items-center gap-3 flex-wrap mb-2">
                          <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold text-lg">{{ item.projectName }}</h4>
                          <span :class="[isDark ? 'bg-green-500/20 text-green-400' : 'bg-green-100 text-green-600', 'px-2 py-1 rounded-full text-xs']">{{ item.projectRole }}</span>
                        </div>
                        <p v-if="item.techStack" :class="isDark ? 'text-indigo-400' : 'text-indigo-600'" class="text-xs">技术栈：{{ item.techStack }}</p>
                      </div>
                      <div class="flex gap-2">
                        <button @click="editProject(item)" class="text-gray-500 hover:text-indigo-600 transition-colors">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                        <button @click="deleteProject(item.id)" class="text-gray-500 hover:text-red-600 transition-colors">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="activeTab === 'skill'" class="tab-content">
                <div class="flex justify-between items-center mb-4">
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="text-lg font-semibold">技能特长</h3>
                  <button @click="addSkill" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition-all hover:scale-105 text-sm">+ 添加技能</button>
                </div>
                <div class="flex flex-wrap gap-3">
                  <div v-for="(item, idx) in skills" :key="idx" :class="[isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50 border border-gray-100', 'rounded-lg p-3 hover:shadow-md transition-all group']">
                    <div class="flex items-center gap-2">
                      <span :class="isDark ? 'text-white' : 'text-gray-800'" class="font-medium">{{ item.skillName }}</span>
                      <span :class="[item.skillLevel === '精通' || item.skillLevel === '专家' ? (isDark ? 'bg-yellow-500/20 text-yellow-400' : 'bg-yellow-100 text-yellow-600') : (isDark ? 'bg-blue-500/20 text-blue-400' : 'bg-blue-100 text-blue-600'), 'px-2 py-0.5 rounded-full text-xs']">{{ item.skillLevel }}</span>
                      <button @click="editSkill(item)" class="opacity-0 group-hover:opacity-100 transition-opacity ml-2 text-gray-500 hover:text-indigo-600">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                      </button>
                      <button @click="deleteSkill(item.id)" class="opacity-0 group-hover:opacity-100 transition-opacity text-gray-500 hover:text-red-600">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="activeTab === 'certificate'" class="tab-content">
                <div class="flex justify-between items-center mb-4">
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="text-lg font-semibold">证书荣誉</h3>
                  <button @click="addCertificate" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition-all hover:scale-105 text-sm">+ 添加证书</button>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div v-for="(item, idx) in certificates" :key="idx" :class="[isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50 border border-gray-100', 'rounded-lg p-4 hover:shadow-md transition-all group']">
                    <div class="flex justify-between items-start">
                      <div>
                        <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold">{{ item.certificateName }}</h4>
                        <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-xs mt-1">{{ item.issueAuthority }}</p>
                      </div>
                      <div class="flex gap-2">
                        <button @click="editCertificate(item)" class="opacity-0 group-hover:opacity-100 transition-opacity text-gray-500 hover:text-indigo-600">
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                        <button @click="deleteCertificate(item.id)" class="opacity-0 group-hover:opacity-100 transition-opacity text-gray-500 hover:text-red-600">
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="activeTab === 'social'" class="tab-content">
                <div class="flex justify-between items-center mb-4">
                  <h3 :class="isDark ? 'text-white' : 'text-gray-800'" class="text-lg font-semibold">社会/校园经历</h3>
                  <button @click="addSocial" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition-all hover:scale-105 text-sm">+ 添加经历</button>
                </div>
                <div v-if="socialExperiences.length === 0" class="text-center py-12" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                  <div class="text-6xl mb-4">🌱</div>
                  <p>暂无社会经历，点击上方按钮添加</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="(item, idx) in socialExperiences" :key="idx" :class="[isDark ? 'bg-white/5 border border-white/10' : 'bg-gray-50 border border-gray-100', 'rounded-lg p-4 hover:shadow-md transition-all']">
                    <div class="flex justify-between items-start">
                      <div class="flex-1">
                        <div class="flex items-center gap-3 flex-wrap mb-2">
                          <h4 :class="isDark ? 'text-white' : 'text-gray-800'" class="font-semibold text-lg">{{ item.title }}</h4>
                          <span :class="[isDark ? 'bg-cyan-500/20 text-cyan-400' : 'bg-cyan-100 text-cyan-600', 'px-2 py-1 rounded-full text-xs']">{{ item.experienceType }}</span>
                        </div>
                        <p :class="isDark ? 'text-gray-400' : 'text-gray-500'" class="text-sm mb-2">{{ item.startDate }} - {{ item.endDate || '至今' }}</p>
                      </div>
                      <div class="flex gap-2">
                        <button @click="editSocial(item)" class="text-gray-500 hover:text-indigo-600 transition-colors">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                        <button @click="deleteSocial(item.id)" class="text-gray-500 hover:text-red-600 transition-colors">
                          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧提示面板 -->
        <div class="w-80 hidden lg:block">
          <div class="sticky top-20">
            <div :class="[isDark ? 'bg-white/10 backdrop-blur-sm border border-white/20' : 'bg-white border border-gray-100', 'rounded-xl p-5 shadow-sm fade-in-up']">
              <div class="flex items-center gap-2 mb-4 pb-3 border-b" :class="isDark ? 'border-gray-700' : 'border-gray-100'">
                <span class="text-2xl">💡</span>
                <h3 class="font-semibold" :class="isDark ? 'text-white' : 'text-gray-800'">填写提示</h3>
              </div>

              <div class="space-y-4">
                <!-- 根据当前标签页显示不同提示 -->
                <template v-if="activeTab === 'basic'">
                  <div v-for="(tip, idx) in fieldTips.basic" :key="idx" class="group cursor-pointer hover:translate-x-1 transition-all">
                    <div class="flex items-start gap-2">
                      <span class="text-indigo-500 text-sm mt-0.5">•</span>
                      <div>
                        <div class="text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ tip.label }}</div>
                        <div class="text-xs mt-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span class="opacity-60">示例：</span>{{ tip.example }}
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

                <template v-if="activeTab === 'work'">
                  <div v-for="(tip, idx) in fieldTips.work" :key="idx" class="group cursor-pointer hover:translate-x-1 transition-all">
                    <div class="flex items-start gap-2">
                      <span class="text-indigo-500 text-sm mt-0.5">•</span>
                      <div>
                        <div class="text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ tip.label }}</div>
                        <div class="text-xs mt-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span class="opacity-60">示例：</span>{{ tip.example }}
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="mt-3 p-3 rounded-lg" :class="isDark ? 'bg-indigo-500/10' : 'bg-indigo-50'">
                    <div class="text-xs" :class="isDark ? 'text-indigo-300' : 'text-indigo-600'">
                      <span class="font-medium">✨ 小技巧：</span>用STAR法则描述工作经历（情境、任务、行动、结果），突出量化成果
                    </div>
                  </div>
                </template>

                <template v-if="activeTab === 'education'">
                  <div v-for="(tip, idx) in fieldTips.education" :key="idx" class="group cursor-pointer hover:translate-x-1 transition-all">
                    <div class="flex items-start gap-2">
                      <span class="text-indigo-500 text-sm mt-0.5">•</span>
                      <div>
                        <div class="text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ tip.label }}</div>
                        <div class="text-xs mt-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span class="opacity-60">示例：</span>{{ tip.example }}
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

                <template v-if="activeTab === 'project'">
                  <div v-for="(tip, idx) in fieldTips.project" :key="idx" class="group cursor-pointer hover:translate-x-1 transition-all">
                    <div class="flex items-start gap-2">
                      <span class="text-indigo-500 text-sm mt-0.5">•</span>
                      <div>
                        <div class="text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ tip.label }}</div>
                        <div class="text-xs mt-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span class="opacity-60">示例：</span>{{ tip.example }}
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="mt-3 p-3 rounded-lg" :class="isDark ? 'bg-indigo-500/10' : 'bg-indigo-50'">
                    <div class="text-xs" :class="isDark ? 'text-indigo-300' : 'text-indigo-600'">
                      <span class="font-medium">✨ 小技巧：</span>清晰描述项目的技术难点和你解决的问题，突出个人贡献
                    </div>
                  </div>
                </template>

                <template v-if="activeTab === 'skill'">
                  <div v-for="(tip, idx) in fieldTips.skill" :key="idx" class="group cursor-pointer hover:translate-x-1 transition-all">
                    <div class="flex items-start gap-2">
                      <span class="text-indigo-500 text-sm mt-0.5">•</span>
                      <div>
                        <div class="text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ tip.label }}</div>
                        <div class="text-xs mt-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span class="opacity-60">示例：</span>{{ tip.example }}
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

                <template v-if="activeTab === 'certificate'">
                  <div v-for="(tip, idx) in fieldTips.certificate" :key="idx" class="group cursor-pointer hover:translate-x-1 transition-all">
                    <div class="flex items-start gap-2">
                      <span class="text-indigo-500 text-sm mt-0.5">•</span>
                      <div>
                        <div class="text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ tip.label }}</div>
                        <div class="text-xs mt-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span class="opacity-60">示例：</span>{{ tip.example }}
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

                <template v-if="activeTab === 'social'">
                  <div v-for="(tip, idx) in fieldTips.social" :key="idx" class="group cursor-pointer hover:translate-x-1 transition-all">
                    <div class="flex items-start gap-2">
                      <span class="text-indigo-500 text-sm mt-0.5">•</span>
                      <div>
                        <div class="text-sm font-medium" :class="isDark ? 'text-gray-300' : 'text-gray-700'">{{ tip.label }}</div>
                        <div class="text-xs mt-1" :class="isDark ? 'text-gray-500' : 'text-gray-400'">
                          <span class="opacity-60">示例：</span>{{ tip.example }}
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

                <template v-if="activeTab === 'full'">
                  <div class="space-y-3">
                    <div class="p-3 rounded-lg" :class="isDark ? 'bg-green-500/10' : 'bg-green-50'">
                      <div class="text-sm font-medium mb-1" :class="isDark ? 'text-green-400' : 'text-green-600'">📊 简历完整度</div>
                      <div class="text-2xl font-bold" :class="isDark ? 'text-green-400' : 'text-green-600'">{{ completeness }}%</div>
                      <div class="text-xs mt-1" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                        {{ completeness < 50 ? '完善更多信息，让简历更吸引人' : completeness < 80 ? '很不错，继续加油' : '完美！简历已准备就绪' }}
                      </div>
                    </div>
                    <div class="p-3 rounded-lg" :class="isDark ? 'bg-blue-500/10' : 'bg-blue-50'">
                      <div class="text-sm font-medium mb-2" :class="isDark ? 'text-blue-400' : 'text-blue-600'">💡 简历建议</div>
                      <ul class="text-xs space-y-1" :class="isDark ? 'text-gray-300' : 'text-gray-600'">
                        <li>• 工作经历建议使用STAR法则描述</li>
                        <li>• 项目经验突出你的个人贡献</li>
                        <li>• 技能等级建议如实填写</li>
                        <li>• 证书选择与求职方向相关的</li>
                      </ul>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗部分保持不变 -->
    <el-dialog v-model="workDialogVisible" :title="isEditWork ? '编辑工作经历' : '添加工作经历'" width="600px" :class="isDark ? 'dark-dialog' : ''">
      <div class="space-y-4">
        <div>
          <label class="block text-sm mb-2">公司名称 *</label>
          <input v-model="workForm.companyName" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
        </div>
        <div>
          <label class="block text-sm mb-2">职位 *</label>
          <input v-model="workForm.position" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm mb-2">开始时间</label>
            <input v-model="workForm.startDate" type="date" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
          <div>
            <label class="block text-sm mb-2">结束时间</label>
            <input v-model="workForm.endDate" type="date" :disabled="workForm.isCurrent" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
        </div>
        <div class="flex items-center gap-2">
          <input v-model="workForm.isCurrent" type="checkbox" id="isCurrentWork">
          <label for="isCurrentWork" class="text-sm">至今在职</label>
        </div>
        <div>
          <label class="block text-sm mb-2">工作职责描述</label>
          <textarea v-model="workForm.description" rows="3" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
        <div>
          <label class="block text-sm mb-2">工作成就</label>
          <textarea v-model="workForm.achievements" rows="2" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
      </div>
      <template #footer>
        <button @click="workDialogVisible = false" class="px-4 py-2 border rounded-lg hover:bg-gray-50">取消</button>
        <button @click="saveWork" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 ml-2">保存</button>
      </template>
    </el-dialog>

    <el-dialog v-model="educationDialogVisible" :title="isEditEducation ? '编辑教育经历' : '添加教育经历'" width="600px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm mb-2">学校名称 *</label>
          <input v-model="educationForm.schoolName" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm mb-2">学历</label>
            <select v-model="educationForm.degree" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
              <option v-for="opt in degreeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm mb-2">专业</label>
            <input v-model="educationForm.major" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm mb-2">入学时间</label>
            <input v-model="educationForm.startDate" type="date" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
          <div>
            <label class="block text-sm mb-2">毕业时间</label>
            <input v-model="educationForm.endDate" type="date" :disabled="educationForm.isCurrent" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
        </div>
        <div class="flex items-center gap-2">
          <input v-model="educationForm.isCurrent" type="checkbox" id="isCurrentEducation">
          <label for="isCurrentEducation" class="text-sm">在读</label>
        </div>
        <div>
          <label class="block text-sm mb-2">描述/荣誉</label>
          <textarea v-model="educationForm.description" rows="2" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
      </div>
      <template #footer>
        <button @click="educationDialogVisible = false" class="px-4 py-2 border rounded-lg hover:bg-gray-50">取消</button>
        <button @click="saveEducation" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 ml-2">保存</button>
      </template>
    </el-dialog>

    <el-dialog v-model="projectDialogVisible" :title="isEditProject ? '编辑项目经验' : '添加项目经验'" width="700px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm mb-2">项目名称 *</label>
          <input v-model="projectForm.projectName" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
        </div>
        <div>
          <label class="block text-sm mb-2">项目角色</label>
          <input v-model="projectForm.projectRole" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm mb-2">开始时间</label>
            <input v-model="projectForm.startDate" type="date" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
          <div>
            <label class="block text-sm mb-2">结束时间</label>
            <input v-model="projectForm.endDate" type="date" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
        </div>
        <div>
          <label class="block text-sm mb-2">项目描述</label>
          <textarea v-model="projectForm.description" rows="3" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
        <div>
          <label class="block text-sm mb-2">职责内容</label>
          <textarea v-model="projectForm.responsibilities" rows="3" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
        <div>
          <label class="block text-sm mb-2">技术栈</label>
          <input v-model="projectForm.techStack" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400" placeholder="如：Vue.js, Spring Boot, MySQL">
        </div>
        <div>
          <label class="block text-sm mb-2">项目链接</label>
          <input v-model="projectForm.projectUrl" type="url" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400" placeholder="https://">
        </div>
      </div>
      <template #footer>
        <button @click="projectDialogVisible = false" class="px-4 py-2 border rounded-lg hover:bg-gray-50">取消</button>
        <button @click="saveProject" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 ml-2">保存</button>
      </template>
    </el-dialog>

    <el-dialog v-model="skillDialogVisible" :title="isEditSkill ? '编辑技能' : '添加技能'" width="500px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm mb-2">技能名称 *</label>
          <input v-model="skillForm.skillName" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400" placeholder="如：Java, Python, Vue.js">
        </div>
        <div>
          <label class="block text-sm mb-2">技能等级</label>
          <select v-model="skillForm.skillLevel" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
            <option v-for="opt in skillLevelOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm mb-2">经验年限</label>
          <input v-model="skillForm.yearsExperience" type="number" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400" placeholder="年">
        </div>
      </div>
      <template #footer>
        <button @click="skillDialogVisible = false" class="px-4 py-2 border rounded-lg hover:bg-gray-50">取消</button>
        <button @click="saveSkill" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 ml-2">保存</button>
      </template>
    </el-dialog>

    <el-dialog v-model="certDialogVisible" :title="isEditCert ? '编辑证书' : '添加证书'" width="600px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm mb-2">证书名称 *</label>
          <input v-model="certForm.certificateName" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
        </div>
        <div>
          <label class="block text-sm mb-2">颁发机构</label>
          <input v-model="certForm.issueAuthority" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm mb-2">获得日期</label>
            <input v-model="certForm.issueDate" type="date" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
          <div>
            <label class="block text-sm mb-2">分数/等级</label>
            <input v-model="certForm.score" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400" placeholder="如：90分 / 优秀">
          </div>
        </div>
        <div>
          <label class="block text-sm mb-2">描述</label>
          <textarea v-model="certForm.description" rows="2" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
      </div>
      <template #footer>
        <button @click="certDialogVisible = false" class="px-4 py-2 border rounded-lg hover:bg-gray-50">取消</button>
        <button @click="saveCertificate" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 ml-2">保存</button>
      </template>
    </el-dialog>

    <el-dialog v-model="socialDialogVisible" :title="isEditSocial ? '编辑社会经历' : '添加社会经历'" width="600px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm mb-2">经历类型 *</label>
          <select v-model="socialForm.experienceType" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
            <option v-for="opt in experienceTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm mb-2">标题 *</label>
          <input v-model="socialForm.title" type="text" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400" placeholder="如：XX大学学生会主席">
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm mb-2">开始时间</label>
            <input v-model="socialForm.startDate" type="date" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
          <div>
            <label class="block text-sm mb-2">结束时间</label>
            <input v-model="socialForm.endDate" type="date" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400">
          </div>
        </div>
        <div>
          <label class="block text-sm mb-2">描述</label>
          <textarea v-model="socialForm.description" rows="3" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
        <div>
          <label class="block text-sm mb-2">成就/奖项</label>
          <textarea v-model="socialForm.achievements" rows="2" class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:border-indigo-400"></textarea>
        </div>
      </div>
      <template #footer>
        <button @click="socialDialogVisible = false" class="px-4 py-2 border rounded-lg hover:bg-gray-50">取消</button>
        <button @click="saveSocial" class="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 ml-2">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.nav-animation {
  animation: slideDown 0.5s ease-out forwards;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}

.fade-in-up.show {
  opacity: 1;
  transform: translateY(0);
}

.tab-content {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(to bottom, #6366f1, #a855f7);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(to bottom, #4f46e5, #9333ea);
}
</style>
