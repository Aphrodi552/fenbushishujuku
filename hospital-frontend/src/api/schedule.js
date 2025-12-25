// 假设项目已存在基础请求工具（如axios实例）
import request from '../utils/request.js'

/**
 * 获取当前医生今日预约列表
 * 后端接口：GET /api/appointments/today
 */
export const getTodayAppointments = () => {
  return request({
    url: '/api/appointments/today',
    method: 'GET'
  })
}

/**
 * 获取预约详细信息
 * 后端接口：GET /api/appointments/{appointmentId}/details
 * @param {string} appointmentId - 预约ID
 */
export const getAppointmentDetails = (appointmentId) => {
  return request({
    url: `/api/appointments/${appointmentId}/details`,
    method: 'GET'
  })
}

/**
 * 开始接诊
 * 后端接口：POST /api/appointments/{appointmentId}/start
 * @param {string} appointmentId - 预约ID
 * @param {string} visitTime - 就诊时间（前端本地时间）
 */
export const startConsultation = (appointmentId, visitTime) => {
  return request({
    url: `/api/appointments/${appointmentId}/start`,
    method: 'POST',
    data: { visitTime }
  })
}

/**
 * 完成接诊
 * 后端接口：POST /api/appointments/{appointmentId}/complete
 * @param {string} appointmentId - 预约ID
 * @param {string} patientId - 患者ID
 * @param {string} diagnosis - 诊断结果
 */
export const completeConsultation = (appointmentId, patientId, diagnosis) => {
  return request({
    url: `/api/appointments/${appointmentId}/complete`,
    method: 'POST',
    data: { patientId, diagnosis },
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 获取已有的诊断结果
 * 后端接口：GET /api/appointments/{appointmentId}/diagnosis
 * @param {string} appointmentId - 预约ID
 */
export const getDiagnosis = (appointmentId) => {
  return request({
    url: `/api/appointments/${appointmentId}/diagnosis`,
    method: 'GET'
  })
}

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