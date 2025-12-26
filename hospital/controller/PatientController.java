package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.AddPatientRequest;
import com.example.hospital.dto.PatientResponse;
import com.example.hospital.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * 就诊人控制器
 */
@RestController
@RequestMapping("/api/patients")
@UserLoginToken // 整个Controller都需要登录访问
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 获取当前登录用户的所有就诊人
     * @param request HttpServletRequest，用于获取用户ID
     * @return 就诊人列表
     */
    @GetMapping
    public Result<List<PatientResponse>> getMyPatients(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<PatientResponse> patients = patientService.getPatientsByUserId(userId);
        return Result.success(patients);
    }

    /**
     * 为当前登录用户添加就诊人
     * @param request HttpServletRequest，用于获取用户ID
     * @param addPatientRequest 添加就诊人的请求数据
     * @return 新添加的就诊人信息
     */
    @PostMapping
    public Result<PatientResponse> addPatient(HttpServletRequest request, @RequestBody AddPatientRequest addPatientRequest) {
        String userId = (String) request.getAttribute("userId");
        PatientResponse newPatient = patientService.addPatientForUser(userId, addPatientRequest);
        return Result.success(newPatient);
    }
}

