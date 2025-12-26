package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hospital.dto.admin.*;
import com.example.hospital.entity.Department;
import com.example.hospital.entity.Hospital;
import com.example.hospital.mapper.DepartmentMapper;
import com.example.hospital.mapper.HospitalMapper;
import com.example.hospital.service.AdminDepartmentService;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDepartmentServiceImpl implements AdminDepartmentService {



    private final DepartmentMapper departmentMapper;
    private final HospitalMapper hospitalMapper;

    public AdminDepartmentServiceImpl(
            DepartmentMapper departmentMapper,
            HospitalMapper hospitalMapper
    ) {
        this.departmentMapper = departmentMapper;
        this.hospitalMapper = hospitalMapper;
    }

    // ================= 查询 =================

    @Override
    public IPage<AdminDepartmentVO> pageDepartments(AdminDepartmentQueryRequest req) {
        Page<Department> page = new Page<>(req.getPage(), req.getSize());

        LambdaQueryWrapper<Department> qw = new LambdaQueryWrapper<>();
        if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
            qw.like(Department::getDepartmentName, req.getKeyword());
        }

        IPage<Department> entityPage = departmentMapper.selectPage(page, qw);

        return entityPage.convert(dep -> {
            AdminDepartmentVO vo = new AdminDepartmentVO();
            vo.setDepartmentId(dep.getDepartmentId());
            vo.setHospitalId(dep.getHospitalId());
            vo.setDepartmentName(dep.getDepartmentName());
            vo.setDepartmentIntro(dep.getDepartmentIntro());
            return vo;
        });
    }

    // ================= 新增 =================

    @Override
    public boolean createDepartment(AdminDepartmentCreateRequest req) {
        Department dep = new Department();
        dep.setDepartmentId(req.getDepartmentId());
        dep.setHospitalId(req.getHospitalId()); // ✅ 新增允许写分片键
        dep.setDepartmentName(req.getDepartmentName());
        dep.setDepartmentIntro(req.getDepartmentIntro());

        departmentMapper.insert(dep);
        return false;
    }

    // ================= 修改（重点：不更新 hospital_id） =================

    @Override
    public boolean updateDepartment(AdminDepartmentUpdateRequest req) {
        Department exist = departmentMapper.selectById(req.getDepartmentId());
        if (exist == null) {
            throw new RuntimeException("科室不存在");
        }

        // 只更新非分片键字段：department_name、department_intro
        // 同时把 hospital_id 放到 WHERE 用于分片路由（不在 SET 中出现）
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Department> uw =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();

        uw.eq(Department::getDepartmentId, req.getDepartmentId());
        uw.eq(Department::getHospitalId, exist.getHospitalId()); // 用于路由，避免广播

        uw.set(Department::getDepartmentName, req.getDepartmentName());
        uw.set(Department::getDepartmentIntro, req.getDepartmentIntro());

        int rows = departmentMapper.update(null, uw);
        if (rows == 0) {
            throw new RuntimeException("更新失败（可能是分片路由或数据不存在）");
        }
        return false;
    }

    // ================= 删除 =================

    @Override
    public boolean deleteDepartment(String departmentId) {
        departmentMapper.deleteById(departmentId);
        return false;
    }

    // ================= 院区列表（必须实现，否则编译失败） =================

    @Override
    public List<Hospital> listHospitals() {
        return hospitalMapper.selectList(null);
    }




}
