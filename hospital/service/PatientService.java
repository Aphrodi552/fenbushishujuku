package com.example.hospital.service;

import com.example.hospital.dto.AddPatientRequest;
import com.example.hospital.dto.PatientResponse;

import java.util.List;

/**
 * 就诊人服务接口
 */
public interface PatientService {

    /**
     * 根据用户ID获取其所有就诊人列表
     * @param userId 用户ID
     * @return 就诊人信息列表
     */
    List<PatientResponse> getPatientsByUserId(String userId);

    /**
     * 为指定用户添加新的就诊人
     * @param userId 用户ID
     * @param request 添加就诊人的请求数据
     * @return 新添加的就诊人信息
     */
    PatientResponse addPatientForUser(String userId, AddPatientRequest request);

    // 更新和删除的功能可以后续再添加
}

