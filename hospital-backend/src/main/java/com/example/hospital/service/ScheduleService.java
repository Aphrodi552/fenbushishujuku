package com.example.hospital.service;

import com.example.hospital.dto.OutpatientScheduleResponse;
import com.example.hospital.dto.ScheduleResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班服务接口
 */
public interface ScheduleService {

    /**
     * 根据医生ID和日期范围获取排班信息
     * @param doctorId 医生ID
     * @param hospitalId 院区ID（可选，用于分片）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 排班列表
     */
    List<ScheduleResponse> getSchedulesByDoctor(String doctorId, String hospitalId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取门诊排班信息（按科室分组）
     * @param hospitalId 院区ID（必填）
     * @param titleFilter 职称筛选（可选）："all"=全部, "normal"=普通门诊（医师、主任）, "expert"=专家门诊（专家）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 按科室分组的排班列表
     */
    List<OutpatientScheduleResponse> getOutpatientSchedules(String hospitalId, String titleFilter, LocalDate startDate, LocalDate endDate);
}

