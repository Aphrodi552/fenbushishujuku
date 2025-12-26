package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Doctor;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.DoctorMapper;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.AdminDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDoctorServiceImpl implements AdminDoctorService {

    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;

    @Override
    public Page<AdminDoctorVO> pageDoctors(AdminDoctorQueryRequest req) {
        long pageNo = Math.max(1, req.getPage());
        long size = Math.max(1, req.getSize());
        Page<Doctor> page = new Page<>(pageNo, size);

        QueryWrapper<Doctor> qw = new QueryWrapper<>();

        // 推荐：尽量传 hospitalId，减少分片广播
        if (StringUtils.isNotBlank(req.getHospitalId())) {
            qw.eq("hospital_id", req.getHospitalId().trim());
        }
        if (StringUtils.isNotBlank(req.getDepartmentId())) {
            qw.eq("department_id", req.getDepartmentId().trim());
        }

        // keyword：医生姓名/doctor_id/user_id/doctor_phone 模糊
        if (StringUtils.isNotBlank(req.getKeyword())) {
            String kw = req.getKeyword().trim();
            qw.and(w -> w.like("doctor_name", kw)
                    .or().like("doctor_id", kw)
                    .or().like("user_id", kw)
                    .or().like("doctor_phone", kw)
            );
        }

        qw.orderByDesc("doctor_id"); // 你也可以改为 created_at（如果 doctor 表有）

        Page<Doctor> doctorPage = doctorMapper.selectPage(page, qw);
        List<Doctor> records = doctorPage.getRecords();
        if (records == null || records.isEmpty()) {
            Page<AdminDoctorVO> empty = new Page<>(pageNo, size);
            empty.setTotal(doctorPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 批量取 userPhone/createdAt
        Set<String> userIds = records.stream().map(Doctor::getUserId)
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());

        Map<String, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity(), (a, b) -> a));

        List<AdminDoctorVO> voList = new ArrayList<>(records.size());
        for (Doctor d : records) {
            AdminDoctorVO vo = new AdminDoctorVO();
            vo.setDoctorId(d.getDoctorId());
            vo.setUserId(d.getUserId());

            vo.setHospitalId(d.getHospitalId());
            vo.setDepartmentId(d.getDepartmentId());

            vo.setDoctorName(d.getDoctorName());
            vo.setDoctorGender(d.getDoctorGender());
            vo.setDoctorIdcard(d.getDoctorIdcard());
            vo.setTitle(d.getTitle());

            vo.setDoctorPhone(d.getDoctorPhone());
            vo.setDoctorEmail(d.getDoctorEmail());
            vo.setDoctorIntro(d.getDoctorIntro());
            vo.setAvatarUrl(d.getAvatarUrl());

            User u = userMap.get(d.getUserId());
            if (u != null) {
                vo.setUserPhone(u.getUserPhone());
                vo.setCreatedAt(u.getCreatedAt());
            }
            voList.add(vo);
        }

        Page<AdminDoctorVO> result = new Page<>(pageNo, size);
        result.setTotal(doctorPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional
    public void createDoctor(AdminDoctorCreateRequest req) {
        // 1) 创建 user（role=doctor）
        User u = new User();
        u.setUserId(genId("U"));
        u.setUserPhone(req.getUserPhone());
        u.setUserPassword(StringUtils.isBlank(req.getUserPassword()) ? "123456" : req.getUserPassword());
        u.setRole("doctor"); // User.role 支持 'user'/'doctor'/'admin' :contentReference[oaicite:7]{index=7}
        userMapper.insert(u);

        // 2) 创建 doctor（注意：doctor.hospitalId 是分片键 :contentReference[oaicite:8]{index=8}）
        Doctor d = new Doctor();
        d.setDoctorId(genId("D"));
        d.setUserId(u.getUserId());
        d.setHospitalId(req.getHospitalId());
        d.setDepartmentId(req.getDepartmentId());

        d.setDoctorName(req.getDoctorName());
        d.setDoctorGender(req.getDoctorGender());
        d.setDoctorIdcard(req.getDoctorIdcard());
        d.setTitle(req.getTitle());

        d.setDoctorPhone(StringUtils.isBlank(req.getDoctorPhone()) ? req.getUserPhone() : req.getDoctorPhone());
        d.setDoctorEmail(req.getDoctorEmail());
        d.setDoctorIntro(req.getDoctorIntro());
        d.setAvatarUrl(req.getAvatarUrl());

        doctorMapper.insert(d);
    }

    @Override
    @Transactional
    public void updateDoctor(String doctorId, String hospitalId, AdminDoctorUpdateRequest req) {
        if (StringUtils.isBlank(hospitalId)) {
            // 强制精准路由，避免 update 广播
            throw new RuntimeException("hospitalId 必填（用于分片路由），且更新不会修改 hospitalId 本身");
        }

        // 1) 先查 doctor，拿 userId（并校验 hospitalId 匹配）
        Doctor d = doctorMapper.selectOne(new QueryWrapper<Doctor>()
                .eq("doctor_id", doctorId)
                .eq("hospital_id", hospitalId)
        );
        if (d == null) throw new RuntimeException("医生不存在或院区不匹配");

        // 2) 更新 userPhone（不改 userId）
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getUserId, d.getUserId())
                .set(User::getUserPhone, req.getUserPhone())
        );

        // 3) 更新 doctor：严禁 set hospital_id / department_id / user_id / doctor_id
        doctorMapper.update(null, new LambdaUpdateWrapper<Doctor>()
                .eq(Doctor::getDoctorId, doctorId)
                .eq(Doctor::getHospitalId, hospitalId) // ✅ where 带分片键
                .set(Doctor::getDoctorName, req.getDoctorName())
                .set(Doctor::getDoctorGender, req.getDoctorGender())
                .set(Doctor::getDoctorIdcard, req.getDoctorIdcard())
                .set(Doctor::getTitle, req.getTitle())
                .set(Doctor::getDoctorPhone, req.getDoctorPhone())
                .set(Doctor::getDoctorEmail, req.getDoctorEmail())
                .set(Doctor::getDoctorIntro, req.getDoctorIntro())
                .set(Doctor::getAvatarUrl, req.getAvatarUrl())
        );
    }

    @Override
    @Transactional
    public void deleteDoctor(String doctorId, String hospitalId) {
        if (StringUtils.isBlank(hospitalId)) {
            throw new RuntimeException("hospitalId 必填（用于分片路由）");
        }

        // 先定位 doctor，拿 userId
        Doctor d = doctorMapper.selectOne(new QueryWrapper<Doctor>()
                .eq("doctor_id", doctorId)
                .eq("hospital_id", hospitalId)
        );
        if (d == null) return;

        // 先删 doctor（分片表）
        doctorMapper.delete(new QueryWrapper<Doctor>()
                .eq("doctor_id", doctorId)
                .eq("hospital_id", hospitalId)
        );

        // 再删 user（全局/单表：视你路由配置而定）
        userMapper.deleteById(d.getUserId());
    }

    private static String genId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
