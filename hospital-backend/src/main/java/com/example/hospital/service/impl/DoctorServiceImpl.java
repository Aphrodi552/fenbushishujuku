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
        
        // 按科室筛选（通过医生的 departmentId 字段）
        if (departmentId != null && !departmentId.trim().isEmpty()) {
            queryWrapper.eq(Doctor::getDepartmentId, departmentId);
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
        
        // 3. 收集所有医生的科室ID，批量查询科室信息
        List<String> departmentIds = doctors.stream()
                .map(Doctor::getDepartmentId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询科室信息
        // 使用 final 确保在 Lambda 中可以安全访问
        final Map<String, Department> departmentMap;
        if (!departmentIds.isEmpty()) {
            List<Department> departments = departmentMapper.selectBatchIds(departmentIds);
            departmentMap = departments.stream()
                    .collect(Collectors.toMap(Department::getDepartmentId, d -> d));
        } else {
            departmentMap = Collections.emptyMap();
        }
        
        // 4. 组装返回数据
        // 使用 final 确保在 Lambda 中可以安全访问 doctors
        final List<Doctor> finalDoctors = doctors;
        List<DoctorResponse> doctorResponses = finalDoctors.stream()
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
                    response.setDepartmentId(doctor.getDepartmentId());
                    
                    // 设置科室名称
                    if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().trim().isEmpty()) {
                        Department department = departmentMap.get(doctor.getDepartmentId());
                        if (department != null) {
                            response.setDepartmentName(department.getDepartmentName());
                        }
                    }
                    
                    return response;
                })
                .collect(Collectors.toList());
        
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
        response.setDepartmentId(doctor.getDepartmentId());
        
        // 查询医生所属科室
        if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().trim().isEmpty()) {
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            if (department != null) {
                response.setDepartmentName(department.getDepartmentName());
            }
        }
        
        return response;
    }
}

