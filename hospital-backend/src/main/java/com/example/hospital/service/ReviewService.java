package com.example.hospital.service;

import com.example.hospital.dto.CreateReviewRequest;
import com.example.hospital.dto.ReviewResponse;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 创建评价
     * @param userId 用户ID
     * @param request 创建评价请求
     * @return 创建的评价信息
     */
    ReviewResponse createReview(String userId, CreateReviewRequest request);

    /**
     * 根据预约ID获取评价
     * @param appointmentId 预约ID
     * @return 评价信息，如果不存在返回null
     */
    ReviewResponse getReviewByAppointmentId(String appointmentId);
}

