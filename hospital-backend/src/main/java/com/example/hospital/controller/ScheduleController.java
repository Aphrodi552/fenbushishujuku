package com.example.hospital.controller;

import com.example.hospital.common.PassToken;
import com.example.hospital.common.Result;
import com.example.hospital.dto.OutpatientScheduleResponse;
import com.example.hospital.dto.ScheduleResponse;
import com.example.hospital.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班控制器
 */
@RestController
@RequestMapping("/api/schedules")
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 根据医生ID获取排班信息
     * @param doctorId 医生ID（必填）
     * @param hospitalId 院区ID（可选，用于分片）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 排班列表
     */
    @PassToken
    @GetMapping
    public Result<List<ScheduleResponse>> getSchedules(
            @RequestParam(required = true) String doctorId,
            @RequestParam(required = false) String hospitalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        System.out.println("前端正在请求排班信息... doctorId: " + doctorId + ", hospitalId: " + hospitalId + 
                         ", startDate: " + startDate + ", endDate: " + endDate);
        
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDoctor(doctorId, hospitalId, startDate, endDate);
        return Result.success(schedules);
    }

    /**
     * 获取门诊排班信息（按科室分组）
     * @param hospitalId 院区ID（必填）
     * @param titleFilter 职称筛选（可选）："all"=全部, "normal"=普通门诊（医师、主任）, "expert"=专家门诊（专家）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 按科室分组的排班列表
     */
    @PassToken
    @GetMapping("/outpatient")
    public Result<List<OutpatientScheduleResponse>> getOutpatientSchedules(
            @RequestParam(required = true) String hospitalId,
            @RequestParam(required = false, defaultValue = "all") String titleFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        System.out.println("前端正在请求门诊排班信息... hospitalId: " + hospitalId + ", titleFilter: " + titleFilter + 
                         ", startDate: " + startDate + ", endDate: " + endDate);
        
        List<OutpatientScheduleResponse> schedules = scheduleService.getOutpatientSchedules(hospitalId, titleFilter, startDate, endDate);
        return Result.success(schedules);
    }
}

