// src/api/hospital.js
import request from '../utils/request'; // 引入刚才写的工具

// 定义一个获取科室列表的方法
export function getDepartmentList() {
  return request({
    url: '/api/hospital/departments', // 对应后端的 @GetMapping("/departments")
    method: 'get'
  });
}

// 定义一个提交预约的方法 (比如以后要用)
export function submitAppointment(data) {
  return request({
    url: '/api/hospital/appointment',
    method: 'post',
    data: data
  });
}