import request from '../utils/request';

/**
 * 获取医生的排班信息
 * @param {string} doctorId 医生ID（必填）
 * @param {string} hospitalId 院区ID（可选）
 * @param {string} startDate 开始日期（可选，格式：yyyy-MM-dd）
 * @param {string} endDate 结束日期（可选，格式：yyyy-MM-dd）
 * @returns {Promise} 排班列表
 */
export function getSchedules(doctorId, hospitalId, startDate, endDate) {
  const params = { doctorId };
  if (hospitalId) params.hospitalId = hospitalId;
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;

  return request({
    url: '/api/schedules',
    method: 'get',
    params: params
  });
}

/**
 * 获取门诊排班信息（按科室分组）
 * @param {string} hospitalId 院区ID（必填）
 * @param {string} titleFilter 职称筛选（可选）："all"=全部, "normal"=普通门诊, "expert"=专家门诊
 * @param {string} startDate 开始日期（可选，格式：yyyy-MM-dd）
 * @param {string} endDate 结束日期（可选，格式：yyyy-MM-dd）
 * @returns {Promise} 按科室分组的排班列表
 */
export function getOutpatientSchedules(hospitalId, titleFilter, startDate, endDate) {
  const params = { hospitalId };
  if (titleFilter) params.titleFilter = titleFilter;
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;

  return request({
    url: '/api/schedules/outpatient',
    method: 'get',
    params: params
  });
}

