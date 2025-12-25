package com.example.hospital.service;

import com.example.hospital.dto.AppointmentResponse;
import com.example.hospital.dto.CreateAppointmentRequest;

import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {

    /**
     * 获取当前用户的所有预约记录
     * 通过 user_patient 表查找与该用户关联的所有就诊人
     * 然后查询这些就诊人的所有预约记录
     * 
     * @param userId 用户ID
     * @return 预约记录列表
     */
    List<AppointmentResponse> getAppointmentsByUserId(String userId);

    /**
     * 创建预约
     * 
     * @param userId 用户ID
     * @param request 创建预约请求
     * @return 创建的预约记录
     */
    AppointmentResponse createAppointment(String userId, CreateAppointmentRequest request);

    /**
     * 取消预约
     * 将预约状态修改为 CANCELLED（已取消）
     * 
     * @param userId 用户ID
     * @param appointmentId 预约ID
     * @return 是否取消成功
     */
    boolean cancelAppointment(String userId, String appointmentId);
}

