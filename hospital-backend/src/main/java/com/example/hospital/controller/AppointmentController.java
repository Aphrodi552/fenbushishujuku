package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.entity.Appointment;
import com.example.hospital.entity.Doctor;
import com.example.hospital.service.AppointmentService;
import com.example.hospital.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

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
     * @return 操作结果
     */
    @UserLoginToken
    @PostMapping("/{appointmentId}/start")
    public Result<String> startConsultation(HttpServletRequest request, @PathVariable String appointmentId) {
        boolean success = appointmentService.startConsultation(appointmentId);
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
     * @param diagnosis 诊断结果
     * @return 操作结果
     */
    @UserLoginToken
    @PostMapping("/{appointmentId}/complete")
    public Result<String> completeConsultation(HttpServletRequest request,
                                             @PathVariable String appointmentId,
                                             @RequestBody String diagnosis) {
        boolean success = appointmentService.completeConsultation(appointmentId, diagnosis);
        if (success) {
            return Result.success("诊断完成");
        } else {
            return Result.error("无法完成诊断，预约状态不正确");
        }
    }
}
