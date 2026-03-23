// src/utils/request.js
import axios from 'axios';
import { ElMessage } from "element-plus"; // 导入 Element Plus 的提示组件

// 1. 创建Axios实例
const service = axios.create({
  baseURL: 'http://localhost:8500/api/', // 适配Vite环境变量
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 2. 请求拦截器：添加token、处理请求参数等
service.interceptors.request.use(
  (config) => {
    // 示例：添加token到请求头（登录后存储的token）
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    ElMessage.error('请求参数异常，请检查！');
    return Promise.reject(error);
  }
);

// 3. 响应拦截器：统一处理响应结果
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败');
      return Promise.reject(res); // 抛出错误，让业务层可以catch
    }
    return res; // 成功时直接返回后端数据
  },
  (error) => {
    // 处理网络错误/状态码错误
    const errMsg = error.response
      ? `请求失败 [${error.response.status}]：${error.response.data?.msg || '服务器错误'}`
      : '网络异常，请检查网络连接！';
    ElMessage.error(errMsg);
    return Promise.reject(error);
  }
);

// 4. 通用请求方法封装（核心）
const request = {
  // GET请求：params传查询参数
  get: (url, params = {}, successCallback) => {
    return service.get(url, { params })
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res; // 同时返回Promise，支持async/await
      });
  },

  // POST请求：data传请求体
  post: (url, data = {}, successCallback) => {
    return service.post(url, data)
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      });
  },

  // PUT请求（更新数据）
  put: (url, data = {}, successCallback) => {
    return service.put(url, data)
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      });
  },

  // DELETE请求
  delete: (url, params = {}, successCallback) => {
    return service.delete(url, { params })
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      });
  }
};

// 导出通用请求对象
export default request;
