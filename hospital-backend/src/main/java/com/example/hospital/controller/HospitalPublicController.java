package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.entity.Hospital;
import com.example.hospital.mapper.HospitalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/hospital")
public class HospitalPublicController {

    @Autowired
    private HospitalMapper hospitalMapper;

    /**
     * 给前端下拉框用：
     * 对应前端：adminDepartment.js -> GET /api/hospital/list
     */
    @GetMapping("/list")
    public Result<List<Hospital>> list() {
        List<Hospital> list = hospitalMapper.selectList(null);
        if (list == null) list = Collections.emptyList();
        return Result.success(list);
    }
}
