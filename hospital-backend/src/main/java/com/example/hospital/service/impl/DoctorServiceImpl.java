package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hospital.dto.DoctorResponse;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Schedule;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.ScheduleMapper;
import com.example.hospital.service.DoctorService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生服务实现类
 */
@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<DoctorResponse> getDoctors(String hospitalId, String departmentId, String keyword) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按院区筛选
        if (hospitalId != null && !hospitalId.trim().isEmpty()) {
            queryWrapper.eq(Doctor::getHospitalId, hospitalId);
        }
        
        // 按关键词搜索（医生姓名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Doctor::getDoctorName, keyword.trim());
        }
        
        // 2. 查询医生列表
        List<Doctor> doctors = doctorMapper.selectList(queryWrapper);
        
        if (doctors.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 3. 获取所有医生的ID
        List<String> doctorIds = doctors.stream()
                .map(Doctor::getDoctorId)
                .collect(Collectors.toList());
        
        // 4. 如果指定了科室ID，需要通过 schedule 表查询该科室的医生
        // 注意：Schedule 表中没有 departmentId，所以这里先简化处理
        // 如果后续数据库中有 doctor_department 关联表，可以在这里添加查询逻辑
        
        // 5. 组装返回数据（先不关联科室，后续可以根据实际数据库结构调整）
        List<DoctorResponse> doctorResponses = doctors.stream()
                .map(doctor -> {
                    DoctorResponse response = new DoctorResponse();
                    BeanUtils.copyProperties(doctor, response);
                    response.setDoctorId(doctor.getDoctorId());
                    response.setDoctorName(doctor.getDoctorName());
                    response.setTitle(doctor.getTitle());
                    response.setDoctorGender(doctor.getDoctorGender());
                    response.setDoctorPhone(doctor.getDoctorPhone());
                    response.setDoctorEmail(doctor.getDoctorEmail());
                    response.setDoctorIntro(doctor.getDoctorIntro());
                    response.setAvatarUrl(doctor.getAvatarUrl());
                    response.setHospitalId(doctor.getHospitalId());
                    return response;
                })
                .collect(Collectors.toList());
        
        // 6. 如果指定了科室ID，需要通过其他方式过滤（暂时返回所有医生）
        // TODO: 根据实际数据库结构调整科室关联逻辑
        return doctorResponses;
    }

    @Override
    public DoctorResponse getDoctorById(String doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return null;
        }
        
        DoctorResponse response = new DoctorResponse();
        BeanUtils.copyProperties(doctor, response);
        response.setDoctorId(doctor.getDoctorId());
        response.setDoctorName(doctor.getDoctorName());
        response.setTitle(doctor.getTitle());
        response.setDoctorGender(doctor.getDoctorGender());
        response.setDoctorPhone(doctor.getDoctorPhone());
        response.setDoctorEmail(doctor.getDoctorEmail());
        response.setDoctorIntro(doctor.getDoctorIntro());
        response.setAvatarUrl(doctor.getAvatarUrl());
        response.setHospitalId(doctor.getHospitalId());
        
        // 查询医生所属科室（暂时不实现，因为 Schedule 表中没有 departmentId）
        // TODO: 根据实际数据库结构调整科室关联逻辑
        
        return response;
    }
}

