package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.dto.MedicalRecordDTO;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Doctor;
import com.example.hospital.service.AppointmentService;
import com.example.hospital.service.DoctorService;
import com.example.hospital.utils.JwtUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取当前登录医生今日的预约列表
     * @param request HttpServletRequest
     * @return 今日预约列表
     */
    @UserLoginToken
    @GetMapping("/today")
    public Result<List<Appointment>> getTodayAppointments(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        if (doctor == null) {
            return Result.error("医生信息不存在");
        }

        LocalDate today = LocalDate.now();
        List<Appointment> appointments = appointmentService.getTodayAppointmentsByDoctorId(doctor.getDoctorId(), today);
        return Result.success(appointments);
    }

    /**
     * 获取预约详细信息
     * @param appointmentId 预约ID
     * @return 预约详细信息列表
     */
    @UserLoginToken
    @GetMapping("/{appointmentId}/details")
    public Result<List<AppointmentDetailDTO>> getAppointmentDetails(@PathVariable String appointmentId) {
        List<AppointmentDetailDTO> appointments = appointmentService.getAppointmentWithDetails(appointmentId);
        if (appointments == null || appointments.isEmpty()) {
            return Result.error("预约不存在");
        }
        return Result.success(appointments);
    }

    /**
     * 开始接诊
     * @param request HttpServletRequest
     * @param appointmentId 预约ID
     * @param requestBody 请求体，包含visitTime
     * @return 操作结果
     */
    @UserLoginToken
    @PostMapping("/{appointmentId}/start")
    public Result<String> startConsultation(HttpServletRequest request,
                                          @PathVariable String appointmentId,
                                          @RequestBody Map<String, String> requestBody) {
        String visitTimeStr = requestBody.get("visitTime");
        boolean success = appointmentService.startConsultation(appointmentId, visitTimeStr);
        if (success) {
            return Result.success("开始接诊");
        } else {
            return Result.error("无法开始接诊，预约状态不正确");
        }
    }

    /**
     * 完成接诊
     * @param request HttpServletRequest
     * @param appointmentId 预约ID
     * @param requestBody 请求体，包含patientId和diagnosis
     * @return 操作结果
     */
    @UserLoginToken
    @PostMapping("/{appointmentId}/complete")
    public Result<String> completeConsultation(HttpServletRequest request,
                                             @PathVariable String appointmentId,
                                             @RequestBody Map<String, String> requestBody) {
        String patientId = requestBody.get("patientId");
        String diagnosis = requestBody.get("diagnosis");

        boolean success = appointmentService.completeConsultation(appointmentId, patientId, diagnosis);
        if (success) {
            return Result.success("诊断完成");
        } else {
            return Result.error("无法完成诊断，预约状态不正确");
        }
    }

    /**
     * 获取诊断结果
     * @param request HttpServletRequest
     * @param appointmentId 预约ID
     * @return 诊断结果
     */
    @UserLoginToken
    @GetMapping("/{appointmentId}/diagnosis")
    public Result<String> getDiagnosis(HttpServletRequest request, @PathVariable String appointmentId) {
        String diagnosis = appointmentService.getDiagnosis(appointmentId);
        if (diagnosis != null) {
            return Result.success(diagnosis);
        } else {
            return Result.error("未找到诊断记录");
        }
    }

    /**
     * 获取医生病历档案（基于visit表）
     * @param request HttpServletRequest
     * @param month 查询月份 (YYYY-MM格式)
     * @param page 页码
     * @param pageSize 每页大小
     * @return 病历记录列表
     */
    @UserLoginToken(required = false)  // 临时允许无认证访问用于调试
    @GetMapping("/visit-records")
    public Result<Page<MedicalRecordDTO>> getMedicalRecords(HttpServletRequest request,
                                                           @RequestParam(required = false) String month,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        // 从token中获取用户ID
        String userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = jwtUtils.getUserIdFromToken(token);
            } catch (Exception e) {
                // token无效，继续处理
            }
        }

        // 如果没有有效的token，返回错误
        if (userId == null) {
            return Result.error("用户未登录或token无效");
        }

        try {
            Page<MedicalRecordDTO> result = appointmentService.getMedicalRecords(userId, month, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取病历档案失败", e);
            return Result.error("获取病历档案失败：" + e.getMessage());
        }
    }
}
