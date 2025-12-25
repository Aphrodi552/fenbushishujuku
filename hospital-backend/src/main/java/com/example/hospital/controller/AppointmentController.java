package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.AppointmentResponse;
import com.example.hospital.dto.CreateAppointmentRequest;
import com.example.hospital.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/appointments")
@UserLoginToken // 需要登录访问
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 获取当前登录用户的所有预约记录
     * @param request HttpServletRequest，用于获取用户ID
     * @return 预约记录列表
     */
    @GetMapping
    public Result<List<AppointmentResponse>> getMyAppointments(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("AppointmentController.getMyAppointments - 从request获取的userId: " + userId);
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法获取预约记录");
            return Result.error("用户身份验证失败，请重新登录");
        }
        
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    /**
     * 创建预约
     * @param request HttpServletRequest，用于获取用户ID
     * @param createRequest 创建预约请求
     * @return 创建的预约记录
     */
    @PostMapping
    public Result<AppointmentResponse> createAppointment(HttpServletRequest request, @RequestBody CreateAppointmentRequest createRequest) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("AppointmentController.createAppointment - 从request获取的userId: " + userId);
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法创建预约");
            return Result.error("用户身份验证失败，请重新登录");
        }
        
        try {
            AppointmentResponse appointment = appointmentService.createAppointment(userId, createRequest);
            return Result.success(appointment);
        } catch (Exception e) {
            System.err.println("创建预约异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约
     * @param request HttpServletRequest，用于获取用户ID
     * @param appointmentId 预约ID
     * @return 操作结果
     */
    @PutMapping("/{appointmentId}/cancel")
    public Result<String> cancelAppointment(HttpServletRequest request, @PathVariable String appointmentId) {
        String userId = (String) request.getAttribute("userId");
        System.out.println("AppointmentController.cancelAppointment - 从request获取的userId: " + userId + ", appointmentId: " + appointmentId);
        
        if (userId == null || userId.isEmpty()) {
            System.err.println("错误：userId为空，无法取消预约");
            return Result.error("用户身份验证失败，请重新登录");
        }
        
        try {
            boolean success = appointmentService.cancelAppointment(userId, appointmentId);
            if (success) {
                return Result.success("预约取消成功");
            } else {
                return Result.error("取消预约失败，请重试");
            }
        } catch (Exception e) {
            System.err.println("取消预约异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

