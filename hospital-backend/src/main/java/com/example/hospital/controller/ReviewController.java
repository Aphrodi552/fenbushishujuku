package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.CreateReviewRequest;
import com.example.hospital.dto.ReviewResponse;
import com.example.hospital.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/reviews")
@UserLoginToken // 需要登录访问
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 创建评价
     * @param request HttpServletRequest，用于获取用户ID
     * @param createRequest 创建评价请求
     * @return 创建的评价信息
     */
    @PostMapping
    public Result<ReviewResponse> createReview(HttpServletRequest request, @RequestBody CreateReviewRequest createRequest) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("ReviewController.createReview - 从request获取的userId: " + userId);

        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法创建评价");
            return Result.error("用户身份验证失败，请重新登录");
        }

        try {
            ReviewResponse review = reviewService.createReview(userId, createRequest);
            return Result.success(review);
        } catch (Exception e) {
            System.err.println("创建评价异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据预约ID获取评价
     * @param appointmentId 预约ID
     * @return 评价信息
     */
    @GetMapping("/appointment/{appointmentId}")
    public Result<ReviewResponse> getReviewByAppointmentId(@PathVariable String appointmentId) {
        ReviewResponse review = reviewService.getReviewByAppointmentId(appointmentId);
        if (review == null) {
            return Result.error("未找到该预约的评价信息");
        }
        return Result.success(review);
    }
}

