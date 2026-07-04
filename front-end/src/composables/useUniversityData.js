/**
 * 文件说明：拾光记前台应用拾光记项目脚本模块，封装拾光记项目相关的配置、状态、路由或工具逻辑。
 */
import { ref } from 'vue'
import request from '@/utils/request'

export function useUniversityData() {
  const majors = ref([])
  const currentMajorId = ref(null)
  const loading = ref(false)

  const loadMajors = async () => {
    loading.value = true
    try {
      const res = await request.get('/university/major/list')
      if (res.code === 200 && res.data?.length) {
        majors.value = res.data
        if (!currentMajorId.value) {
          currentMajorId.value = res.data[0].id
        }
      }
    } finally {
      loading.value = false
    }
  }

  return { majors, currentMajorId, loading, loadMajors }
}
