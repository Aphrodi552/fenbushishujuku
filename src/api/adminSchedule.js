// hospital-frontend/src/api/adminSchedule.js
import request from '../utils/request'

// 注意：request.js 已经把非 200 code 拦截并 reject；成功时返回 Result 对象:contentReference[oaicite:3]{index=3}

export function listHospitals() {
    return request.get('/api/admin/schedules/hospitals')
}

export function listDepartments(params) {
    // params: { hospitalId }
    return request.get('/api/admin/schedules/departments', { params })
}

export function listDoctors(params) {
    // params: { hospitalId, departmentId? }
    return request.get('/api/admin/schedules/doctors', { params })
}

export function pageSchedules(params) {
    // params: { hospitalId, departmentId?, doctorId?, workDateFrom?, workDateTo?, timeSlot?, page, size }
    return request.get('/api/admin/schedules', { params })
}

export function createSchedule(data) {
    // data: { hospitalId, doctorId, workDate, timeSlot, totalQuota }
    return request.post('/api/admin/schedules', data)
}

export function updateSchedule(scheduleId, data) {
    // data: { doctorId, workDate, timeSlot, totalQuota }
    return request.put(`/api/admin/schedules/${scheduleId}`, data)
}

export function deleteSchedule(scheduleId) {
    return request.delete(`/api/admin/schedules/${scheduleId}`)
}
