// src/utils/request.js
import axios from 'axios';

// 1. 创建 axios 实例
const service = axios.create({
  // 后端接口的基础地址
  baseURL: 'http://localhost:8080', 
  // 请求超时时间 (5秒)
  timeout: 5000 
});

// 2. 请求拦截器 (发送请求前做的事，比如加 Token)
service.interceptors.request.use(
  config => {
    // 从localStorage获取token并添加到请求头
    const token = localStorage.getItem('hospital_token');
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 3. 响应拦截器 (收到数据后做的事，比如统一报错)
service.interceptors.response.use(
  response => {
    const res = response.data;
    // 如果后端返回的 code 不是 200，说明出错了
    if (res.code !== 200) {
      alert(res.message || '系统错误');
      return Promise.reject(new Error(res.message || 'Error'));
    } else {
      return res; // 直接把后端返回的 Result 对象里的内容给出去
    }
  },
  error => {
    console.error('请求出错:', error);
    alert('连接后端失败，请检查后端是否启动');
    return Promise.reject(error);
  }
);

export default service;