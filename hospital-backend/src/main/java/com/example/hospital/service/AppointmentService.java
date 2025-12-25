package com.example.hospital.service;

import com.example.hospital.dto.AppointmentDetailDTO;
import com.example.hospital.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 获取医生今日的预约列表
     * @param doctorId 医生ID
     * @param today 今日日期
     * @return 预约列表
     */
    List<Appointment> getTodayAppointmentsByDoctorId(String doctorId, LocalDate today);

    /**
     * 根据预约ID获取完整的预约信息
     * @param appointmentId 预约ID
     * @return 预约详细信息列表
     */
    List<AppointmentDetailDTO> getAppointmentWithDetails(String appointmentId);

    /**
     * 更新预约状态
     * @param appointmentId 预约ID
     * @param status 新状态
     * @return 更新后的预约
     */
    Appointment updateAppointmentStatus(String appointmentId, String status);

    /**
     * 开始接诊
     * @param appointmentId 预约ID
     * @return 是否成功
     */
    boolean startConsultation(String appointmentId);

    /**
     * 完成接诊
     * @param appointmentId 预约ID
     * @param diagnosis 诊断结果
     * @return 是否成功
     */
    boolean completeConsultation(String appointmentId, String diagnosis);
}
