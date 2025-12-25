import request from '../utils/request';

// 创建评价
export function createReview(data) {
  return request({
    url: '/api/reviews',
    method: 'post',
    data: data
  });
}

// 根据预约ID获取评价
export function getReviewByAppointmentId(appointmentId) {
  return request({
    url: `/api/reviews/appointment/${appointmentId}`,
    method: 'get'
  });
}

