package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.hospital.dto.AddPatientRequest;
import com.example.hospital.dto.PatientResponse;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.UserPatient;
import com.example.hospital.mapper.PatientMapper;
import com.example.hospital.mapper.UserPatientMapper;
import com.example.hospital.service.PatientService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private UserPatientMapper userPatientMapper;

    @Override
    public List<PatientResponse> getPatientsByUserId(String userId) {
        // 1. 查找用户和就诊人的所有关联关系
        // 注意：UserPatient 使用复合主键，必须使用 QueryWrapper，不能使用 getById
        List<UserPatient> userPatientLinks = userPatientMapper.selectList(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
        );

        if (userPatientLinks.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 提取所有 patientId
        List<String> patientIds = userPatientLinks.stream()
                .map(UserPatient::getPatientId)
                .collect(Collectors.toList());

        // 3. 批量查询所有就诊人的详细信息
        List<Patient> patients = patientMapper.selectBatchIds(patientIds);

        // 4. 将就诊人信息和关系进行组装
        Map<String, String> patientIdToRelationMap = userPatientLinks.stream()
                .collect(Collectors.toMap(UserPatient::getPatientId, UserPatient::getRelationType));

        return patients.stream().map(patient -> {
            PatientResponse response = new PatientResponse();
            BeanUtils.copyProperties(patient, response);
            response.setPatientId(patient.getPatientId()); // copyProperties might miss this if names differ
            response.setName(patient.getPatientName());
            response.setDob(patient.getPatientBirthday());
            response.setGender(patient.getPatientGender());
            response.setIdCard(patient.getPatientIdcard());
            response.setPhone(patient.getPatientPhone());
            response.setRelation(patientIdToRelationMap.get(patient.getPatientId()));
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional // 开启事务管理
    public PatientResponse addPatientForUser(String userId, AddPatientRequest request) {
        // 1. 检查该身份证号是否已存在
        Patient existingPatient = patientMapper.selectOne(
                new QueryWrapper<Patient>().eq("patient_idcard", request.getIdCard())
        );

        Patient targetPatient;

        if (existingPatient != null) {
            // 2a. 如果患者已存在，检查是否已与当前用户关联
            // 注意：UserPatient 使用复合主键，必须使用 QueryWrapper，不能使用 getById
            UserPatient link = userPatientMapper.selectOne(
                    new LambdaQueryWrapper<UserPatient>()
                            .eq(UserPatient::getUserId, userId)
                            .eq(UserPatient::getPatientId, existingPatient.getPatientId())
            );
            if (link != null) {
                throw new RuntimeException("该就诊人已添加，请勿重复操作");
            }
            targetPatient = existingPatient;
        } else {
            // 2b. 如果患者不存在，则创建新患者
            targetPatient = new Patient();
            targetPatient.setPatientId(UUID.randomUUID().toString());
            targetPatient.setPatientName(request.getName());
            targetPatient.setPatientIdcard(request.getIdCard());
            targetPatient.setPatientPhone(request.getPhone());
            targetPatient.setPatientBirthday(request.getDob());
            targetPatient.setPatientGender(request.getGender());
            patientMapper.insert(targetPatient);
        }

        // 3. 创建用户与就诊人的关联关系
        UserPatient newUserPatientLink = new UserPatient();
        newUserPatientLink.setUserId(userId);
        newUserPatientLink.setPatientId(targetPatient.getPatientId());
        newUserPatientLink.setRelationType(request.getRelation());
        userPatientMapper.insert(newUserPatientLink);

        // 4. 组装并返回结果
        PatientResponse response = new PatientResponse();
        BeanUtils.copyProperties(targetPatient, response);
        response.setPatientId(targetPatient.getPatientId());
        response.setName(targetPatient.getPatientName());
        response.setDob(targetPatient.getPatientBirthday());
        response.setGender(targetPatient.getPatientGender());
        response.setIdCard(targetPatient.getPatientIdcard());
        response.setPhone(targetPatient.getPatientPhone());
        response.setRelation(request.getRelation());

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientResponse updatePatient(String userId, String patientId, AddPatientRequest request) {
        // 1. 验证该就诊人是否属于当前用户
        UserPatient link = userPatientMapper.selectOne(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
                        .eq(UserPatient::getPatientId, patientId)
        );
        
        if (link == null) {
            throw new RuntimeException("该就诊人不属于当前用户，无法修改");
        }

        // 2. 获取就诊人信息
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new RuntimeException("就诊人不存在");
        }

        // 3. 更新就诊人信息
        patient.setPatientName(request.getName());
        patient.setPatientIdcard(request.getIdCard());
        patient.setPatientPhone(request.getPhone());
        patient.setPatientBirthday(request.getDob());
        patient.setPatientGender(request.getGender());
        patientMapper.updateById(patient);

        // 4. 更新关联关系（如果需要修改关系）
        if (request.getRelation() != null && !request.getRelation().equals(link.getRelationType())) {
            // 注意：UserPatient 是复合主键，不能使用 updateById
            // 使用 UpdateWrapper 更新非主键字段
            UserPatient updateEntity = new UserPatient();
            updateEntity.setRelationType(request.getRelation());
            
            int updateResult = userPatientMapper.update(
                    updateEntity,
                    new LambdaUpdateWrapper<UserPatient>()
                            .eq(UserPatient::getUserId, userId)
                            .eq(UserPatient::getPatientId, patientId)
            );
            
            if (updateResult > 0) {
                link.setRelationType(request.getRelation());
            }
        }

        // 5. 组装并返回结果
        PatientResponse response = new PatientResponse();
        response.setPatientId(patient.getPatientId());
        response.setName(patient.getPatientName());
        response.setDob(patient.getPatientBirthday());
        response.setGender(patient.getPatientGender());
        response.setIdCard(patient.getPatientIdcard());
        response.setPhone(patient.getPatientPhone());
        response.setRelation(link.getRelationType());

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePatient(String userId, String patientId) {
        // 1. 验证该就诊人是否属于当前用户
        UserPatient link = userPatientMapper.selectOne(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
                        .eq(UserPatient::getPatientId, patientId)
        );
        
        if (link == null) {
            throw new RuntimeException("该就诊人不属于当前用户，无法删除");
        }

        // 2. 删除用户与就诊人的关联关系
        // 注意：UserPatient 使用复合主键，必须使用 QueryWrapper 删除
        int deleteResult = userPatientMapper.delete(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
                        .eq(UserPatient::getPatientId, patientId)
        );

        if (deleteResult <= 0) {
            throw new RuntimeException("删除关联关系失败");
        }

        // 注意：这里只删除关联关系，不删除 patient 表中的数据
        // 因为该患者可能被其他用户关联
    }
}

