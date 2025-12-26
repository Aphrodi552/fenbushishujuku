package com.example.hospital.service.impl;

import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.ChangePasswordRequest;
import com.example.hospital.dto.DoctorProfileResponse;
import com.example.hospital.dto.DoctorProfileUpdateRequest;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.Hospital;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.DepartmentService;
import com.example.hospital.service.DoctorService;
import com.example.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 医生服务实现类
 */
@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public Doctor getDoctorByUserId(String userId) {
        Doctor doctor = doctorMapper.selectByUserId(userId);
        // if (doctor == null) {
        //     // 如果医生记录不存在，创建一条默认记录
        //     doctor = new Doctor();
        //     doctor.setDoctorId(userId); // 使用userId作为doctorId
        //     doctor.setUserId(userId);
        //     doctor.setDoctorName("医生" + userId); // 默认姓名
        //     doctor.setDoctorGender("未知");
        //     doctor.setDoctorIdcard(""); // 身份证号，暂时为空
        //     doctor.setDoctorPhone(""); // 手机号，暂时为空（但用户必须设置）
        //     doctor.setDoctorEmail("");
        //     doctor.setTitle("住院医师"); // 默认职称
        //     doctor.setDoctorIntro("暂无简介");
        //     doctor.setHospitalId(""); // 空字符串表示未设置
        //     doctor.setDepartmentId(null); // null表示未设置

        //     // 插入数据库
        //     doctorMapper.insert(doctor);
        // }
        return doctor;
    }

    @Override
    public DoctorProfileResponse getDoctorProfile(String userId) {
        Doctor doctor = getDoctorByUserId(userId);

        DoctorProfileResponse response = new DoctorProfileResponse();
        response.setDoctorId(doctor.getDoctorId());
        response.setDoctorName(doctor.getDoctorName());
        response.setDoctorGender(doctor.getDoctorGender());
        response.setDoctorPhone(doctor.getDoctorPhone());
        response.setDoctorEmail(doctor.getDoctorEmail());
        response.setTitle(doctor.getTitle());
        response.setDoctorIntro(doctor.getDoctorIntro());
        response.setHospitalId(doctor.getHospitalId());
        response.setDepartmentId(doctor.getDepartmentId());

        // 获取医院名称
        if (doctor.getHospitalId() != null && !doctor.getHospitalId().isEmpty()) {
            try {
                Hospital hospital = hospitalService.getHospitalById(doctor.getHospitalId());
                response.setHospitalName(hospital != null ? hospital.getHospitalName() : "");
            } catch (Exception e) {
                response.setHospitalName("");
            }
        } else {
            response.setHospitalName("");
        }

        // 获取科室名称
        if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().isEmpty()) {
            try {
                Department department = departmentService.getDepartmentById(doctor.getDepartmentId());
                response.setDepartmentName(department != null ? department.getDepartmentName() : "");
            } catch (Exception e) {
                response.setDepartmentName("");
            }
        } else {
            response.setDepartmentName("");
        }

        // 擅长领域暂时为空，后续可根据业务需求扩展
        response.setSpecialty("");

        return response;
    }

    @Override
    public Doctor updateDoctorProfile(String userId, DoctorProfileUpdateRequest request) {
        // 使用getDoctorByUserId方法，确保医生记录存在
        Doctor doctor = getDoctorByUserId(userId);

        // 准备更新值
        String doctorPhone = doctor.getDoctorPhone(); // 默认保持原值
        String doctorEmail = doctor.getDoctorEmail(); // 默认保持原值
        String doctorIntro = doctor.getDoctorIntro(); // 默认保持原值

        // 更新手机号（如果提供）
        if (request.getDoctorPhone() != null && !request.getDoctorPhone().trim().isEmpty()) {
            // 检查新手机号是否已被其他医生使用
            Doctor existingDoctor = doctorMapper.selectByDoctorPhone(request.getDoctorPhone().trim());
            if (existingDoctor != null && !existingDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被其他医生使用");
            }
            doctorPhone = request.getDoctorPhone().trim();
        }

        // 更新邮箱（如果提供）
        if (request.getDoctorEmail() != null && !request.getDoctorEmail().trim().isEmpty()) {
            // 检查新邮箱是否已被其他医生使用
            Doctor existingDoctor = doctorMapper.selectByDoctorEmail(request.getDoctorEmail().trim());
            if (existingDoctor != null && !existingDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该邮箱已被其他医生使用");
            }
            doctorEmail = request.getDoctorEmail().trim();
        }

        // 更新简介（如果提供且不为空）
        if (request.getDoctorIntro() != null && !request.getDoctorIntro().trim().isEmpty()) {
            doctorIntro = request.getDoctorIntro().trim();
        }

        // 使用自定义更新方法，避免更新分片键
        doctorMapper.updateDoctorProfile(doctorPhone, doctorEmail, doctorIntro, doctor.getDoctorId());

        // 重新查询以返回最新数据
        return doctorMapper.selectByUserId(userId);
    }

    @Override
    public boolean changePassword(String userId, ChangePasswordRequest request) {
        // 获取用户信息进行密码验证
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 验证旧密码
        if (!user.getUserPassword().equals(request.getOldPassword())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "当前密码错误");
        }

        // 验证新密码长度
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "新密码至少6位");
        }

        // 更新密码
        user.setUserPassword(request.getNewPassword());
        int result = userMapper.updateById(user);

        return result > 0;
    }
}
