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
