// 假设项目已存在基础请求工具（如axios实例）
import request from '../utils/request.js'

/**
 * 获取当前医生排班信息
 * 后端接口：GET /api/schedules/my-schedules
 */
export const getMySchedules = () => {
  return request({
    url: '/api/schedules/my-schedules',
    method: 'GET'
  })
}

/**
 * 根据日期范围获取当前医生排班信息
 * 后端接口：GET /api/schedules/my-schedules/range
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期（yyyy-MM-dd）
 * @param {string} params.endDate - 结束日期（yyyy-MM-dd）
 * @param {string} params.timeSlot - 时间段（可选）
 */
export const getDoctorSchedules = (params) => {
  return request({
    url: '/api/schedules/my-schedules/range',
    method: 'GET',
    params
  })
}

/**
 * 创建排班
 * 后端接口：POST /api/schedules
 * @param {Object} data - 排班信息
 */
export const createSchedule = (data) => {
  return request({
    url: '/api/schedules',
    method: 'POST',
    data
  })
}

/**
 * 更新排班
 * 后端接口：PUT /api/schedules/{scheduleId}
 * @param {string} scheduleId - 排班ID
 * @param {Object} data - 排班信息
 */
export const updateSchedule = (scheduleId, data) => {
  return request({
    url: `/api/schedules/${scheduleId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除排班
 * 后端接口：DELETE /api/schedules/{scheduleId}
 * @param {string} scheduleId - 排班ID
 */
export const deleteSchedule = (scheduleId) => {
  return request({
    url: `/api/schedules/${scheduleId}`,
    method: 'DELETE'
  })
}