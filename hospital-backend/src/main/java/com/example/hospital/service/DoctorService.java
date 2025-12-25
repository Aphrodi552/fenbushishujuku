package com.example.hospital.service;

import com.example.hospital.dto.DoctorResponse;

import java.util.List;

/**
 * 医生服务接口
 */
public interface DoctorService {

    /**
     * 根据院区和科室获取医生列表
     * @param hospitalId 院区ID（可选）
     * @param departmentId 科室ID（可选）
     * @param keyword 搜索关键词（医生姓名，可选）
     * @return 医生列表
     */
    List<DoctorResponse> getDoctors(String hospitalId, String departmentId, String keyword);

    /**
     * 根据医生ID获取医生详情
     * @param doctorId 医生ID
     * @return 医生信息
     */
    DoctorResponse getDoctorById(String doctorId);
}

