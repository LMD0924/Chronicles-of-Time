/**
 * 文件说明：拾光记后台管理系统拾光记项目脚本模块，封装拾光记项目相关的配置、状态、路由或工具逻辑。
 */
export const dashboardApi = {
  overview() {
    return Promise.resolve({
      cards: [
        { label: '用户总数', value: 12860, unit: '人', trend: '+12.8%', icon: 'User' },
        { label: '今日注册', value: 186, unit: '人', trend: '+8.2%', icon: 'UserFilled' },
        { label: '笔记总数', value: 48210, unit: '篇', trend: '+5.6%', icon: 'Notebook' },
        { label: '动态总数', value: 15642, unit: '条', trend: '+3.1%', icon: 'Bell' },
        { label: '论文数量', value: 942, unit: '份', trend: '+2.4%', icon: 'Document' },
        { label: '访问量统计', value: 386420, unit: '次', trend: '+18.6%', icon: 'Histogram' },
      ],
      logs: [
        { type: '内容审核', text: '时光笔记《六月复盘》已通过审核', time: '11:30' },
        { type: '系统告警', text: 'high-service 平均响应时间超过阈值', time: '10:48' },
        { type: '用户登录', text: '运营管理员从 127.0.0.1 登录后台', time: '09:52' },
        { type: '资源清理', text: '清理无引用图片资源 28 个', time: '08:40' },
      ],
      todos: ['审核待发布动态 12 条', '处理违规图片 3 张', '复核论文延期风险 5 个', '导出本周访问报表'],
    })
  },
}

export const makeRows = (module) => {
  const owners = ['陈思远', '林可', '周老师', '李顾问', '运营一号', '王明']
  const statuses = ['正常', '待审核', '已下架', '禁用']
  return Array.from({ length: 38 }, (_, index) => ({
    id: `${module.prefix}${String(index + 1).padStart(4, '0')}`,
    title: `${module.title}记录 ${index + 1}`,
    name: owners[index % owners.length],
    category: module.categories[index % module.categories.length],
    status: statuses[index % statuses.length],
    createdAt: `2026-06-${String((index % 28) + 1).padStart(2, '0')}`,
    updatedAt: `2026-06-${String((index % 28) + 1).padStart(2, '0')} 10:${String(index % 60).padStart(2, '0')}`,
    views: 1200 + index * 37,
    remark: module.remark,
  }))
}

export const crudApi = {
  list(module, params) {
    const all = makeRows(module).filter((item) => {
      const keyword = params.keyword?.trim()
      const matchKeyword = !keyword || [item.id, item.title, item.name, item.category].some((field) => String(field).includes(keyword))
      const matchStatus = !params.status || params.status === '全部' || item.status === params.status
      return matchKeyword && matchStatus
    })
    const start = (params.page - 1) * params.pageSize
    return Promise.resolve({ total: all.length, list: all.slice(start, start + params.pageSize) })
  },
}
