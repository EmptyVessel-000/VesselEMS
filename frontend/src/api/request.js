import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router/index.js'

const request = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动携带 JWT token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 401 和业务错误
request.interceptors.response.use(
  (response) => {
    // 后端返回 ApiResponse<T> 格式 {code, message, data}
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data !== undefined ? body.data : body
      }
      // 业务错误
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    // 非标准 ApiResponse，直接返回原始数据
    return response.data
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        if (router.currentRoute.value.path !== '/login') {
          ElMessage.error('登录已过期，请重新登录')
          // 动态导入避免循环依赖
          import('../router/dynamicRoutes.js').then(m => m.clearDynamicRoutes(router))
          import('../stores/permissions.js').then(m => m.resetPermissions())
          router.push('/login')
        } else {
          ElMessage.error(error.response.data?.message || '邮箱或密码错误')
        }
      } else if (status === 403) {
        ElMessage.error(error.response.data?.message || '权限不足')
      } else {
        const message = error.response.data?.message || error.message || '请求失败'
        ElMessage.error(message)
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request