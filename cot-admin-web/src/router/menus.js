/**
 * 文件说明：拾光记后台管理系统路由与菜单脚本模块，封装路由与菜单相关的配置、状态、路由或工具逻辑。
 */
import {
  Aim,
  Bell,
  Briefcase,
  Camera,
  Collection,
  Connection,
  DataAnalysis,
  Document,
  Files,
  FolderOpened,
  Grid,
  Guide,
  Histogram,
  House,
  Key,
  List,
  Lock,
  Memo,
  Menu,
  Monitor,
  Notebook,
  Operation,
  Picture,
  Platform,
  Postcard,
  Reading,
  School,
  Setting,
  ChatDotRound,
  Star,
  Tickets,
  TrendCharts,
  Trophy,
  User,
  UserFilled,
  Warning,
} from '@element-plus/icons-vue'

// Element Plus 图标统一映射，菜单只保存字符串标识，避免把组件实例直接写进业务配置。
export const iconMap = {
  Aim,
  Bell,
  Briefcase,
  Camera,
  Collection,
  Connection,
  DataAnalysis,
  Document,
  Files,
  FolderOpened,
  Grid,
  Guide,
  Histogram,
  House,
  Key,
  List,
  Lock,
  Memo,
  Menu,
  Monitor,
  Notebook,
  Operation,
  Picture,
  Platform,
  Postcard,
  Reading,
  School,
  Setting,
  ChatDotRound,
  Star,
  Tickets,
  TrendCharts,
  Trophy,
  User,
  UserFilled,
  Warning,
}



// 后台侧边栏的唯一菜单源：页面标题、图标、组件路径和缓存信息都从这里生成。
// 新增后台模块时优先维护这个数组，路由表会通过 toRouteRecords 自动展开为真实路由。
export const adminMenus = [
  { path: '/dashboard', name: 'Dashboard', title: '首页控制台', icon: 'House', component: 'dashboard/DashboardView', affix: true },
  {
    path: '/system', title: '系统管理', icon: 'Setting', children: [
      { path: '/system/users', name: 'SystemUsers', title: '个人用户', icon: 'User', component: 'system/UserManage' },
      { path: '/system/roles', name: 'SystemRoles', title: '角色管理', icon: 'UserFilled', component: 'system/RoleManage' },
      { path: '/system/menus', name: 'SystemMenus', title: '菜单管理', icon: 'Menu', component: 'system/MenuManage' },
      { path: '/system/permissions', name: 'SystemPermissions', title: '权限管理', icon: 'Lock', component: 'system/PermissionManage' },
      { path: '/system/logs', name: 'SystemLogs', title: '日志管理', icon: 'Tickets', component: 'system/LogManage' },
    ],
  },
  {
    path: '/learning', title: '学习中心管理', icon: 'Reading', children: [
      { path: '/learning/questions', name: 'QuestionManage', title: '题目管理', icon: 'Notebook', component: 'learning/QuestionManage' },
      { path: '/learning/mistakes', name: 'MistakeManage', title: '错题管理', icon: 'Warning', component: 'learning/MistakeManage' },
      { path: '/learning/answers', name: 'AnswerRecordManage', title: '答题记录管理', icon: 'Tickets', component: 'learning/AnswerRecordManage' },
      { path: '/learning/scores', name: 'ScoreRecordManage', title: '成绩记录管理', icon: 'TrendCharts', component: 'learning/ScoreRecordManage' },
      { path: '/learning/knowledge-graph', name: 'KnowledgeGraphManage', title: '知识图谱管理', icon: 'Connection', component: 'learning/KnowledgeGraphManage' },
      { path: '/learning/knowledge-heatmap', name: 'KnowledgeHeatmapManage', title: '知识热力图管理', icon: 'Histogram', component: 'learning/KnowledgeHeatmapManage' },
    ],
  },
  {
    path: '/stage', title: '人生阶段管理', icon: 'Guide', children: [
      { path: '/stage/high-school', name: 'HighSchoolStage', title: '高中阶段管理', icon: 'Reading', component: 'stage/HighSchoolStage' },
      { path: '/stage/university', name: 'UniversityStage', title: '大学阶段管理', icon: 'School', component: 'stage/UniversityStage' },
      { path: '/stage/workplace', name: 'WorkplaceStage', title: '职场阶段管理', icon: 'Briefcase', component: 'stage/WorkplaceStage' },
      { path: '/stage/advanced', name: 'AdvancedStage', title: '进阶成长管理', icon: 'TrendCharts', component: 'stage/AdvancedStage' },
    ],
  },
  {
    path: '/planning', title: '升学规划管理', icon: 'Aim', children: [
      { path: '/planning/course-selection', name: 'CourseSelectionManage', title: '选科管理', icon: 'Grid', component: 'planning/CourseSelectionManage' },
      { path: '/planning/selection-approval', name: 'SelectionApprovalManage', title: '选科审批管理', icon: 'Operation', component: 'planning/SelectionApprovalManage' },
      { path: '/planning/grading-scale', name: 'GradingScaleManage', title: '赋分规则管理', icon: 'DataAnalysis', component: 'planning/GradingScaleManage' },
      { path: '/planning/course-guidance', name: 'CourseGuidanceManage', title: '选科指导管理', icon: 'Guide', component: 'planning/CourseGuidanceManage' },
      { path: '/planning/volunteer', name: 'VolunteerPlanManage', title: '志愿方案管理', icon: 'Trophy', component: 'planning/VolunteerPlanManage' },
      { path: '/planning/university-major', name: 'UniversityMajorManage', title: '院校专业库管理', icon: 'School', component: 'planning/UniversityMajorManage' },
    ],
  },
  {
    path: '/content', title: '用户内容管理', icon: 'Collection', children: [
      { path: '/content/notes', name: 'TimeNotes', title: '时光笔记管理', icon: 'Notebook', component: 'content/NoteManage' },
      { path: '/content/moments', name: 'CampusMoments', title: '校园动态管理', icon: 'Bell', component: 'content/MomentManage' },
      { path: '/content/albums', name: 'AlbumImages', title: '相册图片管理', icon: 'Picture', component: 'content/AlbumManage' },
      { path: '/content/letters', name: 'TimeLetters', title: '时光笺管理', icon: 'Postcard', component: 'content/LetterManage' },
      { path: '/content/articles', name: 'ArticleManage', title: '文章内容管理', icon: 'Document', component: 'content/ArticleManage' },
      { path: '/content/comments', name: 'CommentManage', title: '评论管理', icon: 'Memo', component: 'content/CommentManage' },
      { path: '/content/content-graph', name: 'ContentGraphManage', title: '内容知识图谱管理', icon: 'Connection', component: 'content/ContentGraphManage' },
    ],
  },
  {
    path: '/community', title: '社区互动管理', icon: 'ChatDotRound', children: [
      { path: '/community/activity-medals', name: 'ActivityMedalManage', title: '打卡勋章管理', icon: 'Trophy', component: 'community/ActivityMedalManage' },
      { path: '/community/chat', name: 'ChatManage', title: '在线聊天管理', icon: 'ChatDotRound', component: 'community/ChatManage' },
    ],
  },
  {
    path: '/academic', title: '大学学业管理', icon: 'School', children: [
      { path: '/academic/majors', name: 'MajorTree', title: '专业树管理', icon: 'Connection', component: 'academic/MajorTreeManage' },
      { path: '/academic/course-tree', name: 'CourseTreeManage', title: '课程树管理', icon: 'Grid', component: 'academic/CourseTreeManage' },
      { path: '/academic/student-courses', name: 'StudentCourseManage', title: '学生课程管理', icon: 'Reading', component: 'academic/StudentCourseManage' },
      { path: '/academic/progress', name: 'AcademicProgress', title: '学业进度管理', icon: 'DataAnalysis', component: 'academic/ProgressManage' },
      { path: '/academic/graduation-gap', name: 'GraduationGapManage', title: '毕业差距管理', icon: 'Warning', component: 'academic/GraduationGapManage' },
      { path: '/academic/gpa', name: 'GpaSimulationManage', title: 'GPA 模拟管理', icon: 'TrendCharts', component: 'academic/GpaSimulationManage' },
      { path: '/academic/certificates', name: 'CertificateManage', title: '证书档案管理', icon: 'Trophy', component: 'academic/CertificateManage' },
      { path: '/academic/papers', name: 'PaperManage', title: '论文管理', icon: 'Document', component: 'academic/PaperManage' },
    ],
  },
  { path: '/resources', name: 'ResourceManage', title: '文件资源管理', icon: 'FolderOpened', component: 'resource/ResourceManage' },
  {
    path: '/monitor', title: '系统监控', icon: 'Monitor', children: [
      { path: '/monitor/services', name: 'ServiceMonitor', title: '服务状态监控', icon: 'Platform', component: 'monitor/ServiceMonitor' },
      { path: '/monitor/online', name: 'OnlineUsers', title: '在线用户监控', icon: 'UserFilled', component: 'monitor/OnlineUsers' },
      { path: '/monitor/api', name: 'ApiStats', title: '接口访问统计', icon: 'Histogram', component: 'monitor/ApiStats' },
    ],
  },
]



// Vite 会在构建期收集 views 下的 Vue 页面，运行时按菜单中的 component 字段懒加载。
const viewModules = import.meta.glob('@/views/**/*.vue')



// 将多级菜单扁平化为 vue-router 子路由，父级只负责分组，叶子节点才对应实际页面。
export const toRouteRecords = (menus) => menus.flatMap((item) => {
  if (item.children?.length) return toRouteRecords(item.children)
  return [{
    path: item.path,
    name: item.name,
    component: viewModules[`/src/views/${item.component}.vue`],
    meta: { title: item.title, icon: item.icon, keepAlive: true, affix: item.affix },
  }]
})
