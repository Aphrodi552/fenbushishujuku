package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.hospital.dto.AddPatientRequest;
import com.example.hospital.dto.PatientResponse;
import com.example.hospital.entity.Patient;
import com.example.hospital.entity.UserPatient;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.PatientMapper;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.mapper.UserPatientMapper;
import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
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

    @Autowired
    private UserMapper userMapper;

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
        // 1. 验证身份证号唯一性（不能与其他病患相同）
        if (request.getIdCard() != null && !request.getIdCard().trim().isEmpty()) {
            Patient existingByIdCard = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>()
                            .eq(Patient::getPatientIdcard, request.getIdCard().trim())
            );
            if (existingByIdCard != null) {
                // 检查是否已与当前用户关联
                UserPatient existingLink = userPatientMapper.selectOne(
                        new LambdaQueryWrapper<UserPatient>()
                                .eq(UserPatient::getUserId, userId)
                                .eq(UserPatient::getPatientId, existingByIdCard.getPatientId())
                );
                if (existingLink != null) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR, "该就诊人已添加，请勿重复操作");
                }
                // 如果已存在但未关联，使用现有患者（不验证手机号，因为身份证号已确定身份）
                Patient targetPatient = existingByIdCard;
                
                // 创建用户与就诊人的关联关系
                UserPatient newUserPatientLink = new UserPatient();
                newUserPatientLink.setUserId(userId);
                newUserPatientLink.setPatientId(targetPatient.getPatientId());
                newUserPatientLink.setRelationType(request.getRelation());
                userPatientMapper.insert(newUserPatientLink);
                
                // 组装并返回结果
                PatientResponse response = new PatientResponse();
                response.setPatientId(targetPatient.getPatientId());
                response.setName(targetPatient.getPatientName());
                response.setDob(targetPatient.getPatientBirthday());
                response.setGender(targetPatient.getPatientGender());
                response.setIdCard(targetPatient.getPatientIdcard());
                response.setPhone(targetPatient.getPatientPhone());
                response.setRelation(request.getRelation());
                return response;
            }
        }
        
        // 2. 验证手机号唯一性（不能与其他病患相同，仅在创建新患者时验证）
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            Patient existingByPhone = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>()
                            .eq(Patient::getPatientPhone, request.getPhone().trim())
            );
            if (existingByPhone != null) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被其他病患使用");
            }
        }
        
        // 3. 创建新患者
        Patient targetPatient = new Patient();
        targetPatient.setPatientId(UUID.randomUUID().toString());
        targetPatient.setPatientName(request.getName());
        targetPatient.setPatientIdcard(request.getIdCard());
        targetPatient.setPatientPhone(request.getPhone());
        targetPatient.setPatientBirthday(request.getDob());
        targetPatient.setPatientGender(request.getGender());
        patientMapper.insert(targetPatient);

        // 4. 创建用户与就诊人的关联关系
        UserPatient newUserPatientLink = new UserPatient();
        newUserPatientLink.setUserId(userId);
        newUserPatientLink.setPatientId(targetPatient.getPatientId());
        newUserPatientLink.setRelationType(request.getRelation());
        userPatientMapper.insert(newUserPatientLink);

        // 5. 如果关系是"本人"，同步更新用户表的手机号
        if ("本人".equals(request.getRelation()) && request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            // 验证新手机号是否已被其他用户使用（排除当前用户）
            User existingUser = userMapper.selectByUserPhone(newPhone);
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                // 如果手机号已被其他用户使用，回滚操作（事务会自动回滚）
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被其他用户使用，无法同步更新");
            }
            
            // 更新用户表的手机号
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setUserPhone(newPhone);
                userMapper.updateById(user);
                System.out.println("添加本人就诊人，同步更新用户表手机号: userId=" + userId + ", newPhone=" + newPhone);
            }
        }

        // 6. 组装并返回结果
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
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该就诊人不属于当前用户，无法修改");
        }

        // 2. 获取就诊人信息
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "就诊人不存在");
        }

        // 3. 验证身份证号唯一性（不能与其他病患相同，排除当前就诊人）
        if (request.getIdCard() != null && !request.getIdCard().trim().isEmpty()) {
            String newIdCard = request.getIdCard().trim();
            // 如果身份证号有变化，检查是否与其他病患重复
            if (!newIdCard.equals(patient.getPatientIdcard())) {
                Patient existingByIdCard = patientMapper.selectOne(
                        new LambdaQueryWrapper<Patient>()
                                .eq(Patient::getPatientIdcard, newIdCard)
                );
                if (existingByIdCard != null && !existingByIdCard.getPatientId().equals(patientId)) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR, "该身份证号已被其他病患使用");
                }
            }
        }
        
        // 4. 验证手机号唯一性（不能与其他病患相同，排除当前就诊人）
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            // 如果手机号有变化，检查是否与其他病患重复
            if (!newPhone.equals(patient.getPatientPhone())) {
                Patient existingByPhone = patientMapper.selectOne(
                        new LambdaQueryWrapper<Patient>()
                                .eq(Patient::getPatientPhone, newPhone)
                );
                if (existingByPhone != null && !existingByPhone.getPatientId().equals(patientId)) {
                    throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被其他病患使用");
                }
            }
        }
        
        // 5. 更新就诊人信息
        String oldPhone = patient.getPatientPhone();
        patient.setPatientName(request.getName());
        patient.setPatientIdcard(request.getIdCard());
        patient.setPatientPhone(request.getPhone());
        patient.setPatientBirthday(request.getDob());
        patient.setPatientGender(request.getGender());
        patientMapper.updateById(patient);

        // 6. 更新关联关系（如果需要修改关系）
        String finalRelation = link.getRelationType(); // 最终的关系类型
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
                finalRelation = request.getRelation();
                link.setRelationType(request.getRelation());
            }
        }

        // 7. 如果关系是"本人"，同步更新用户表的手机号
        if ("本人".equals(finalRelation) && request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            // 如果手机号有变化，更新用户表
            if (!newPhone.equals(oldPhone)) {
                // 验证新手机号是否已被其他用户使用（排除当前用户）
                User existingUser = userMapper.selectByUserPhone(newPhone);
                if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                    // 如果手机号已被其他用户使用，回滚就诊人更新（事务会自动回滚）
                    throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被其他用户使用，无法同步更新");
                }
                
                // 更新用户表的手机号
                User user = userMapper.selectById(userId);
                if (user != null) {
                    user.setUserPhone(newPhone);
                    userMapper.updateById(user);
                    System.out.println("同步更新用户表手机号: userId=" + userId + ", oldPhone=" + oldPhone + ", newPhone=" + newPhone);
                }
            }
        }

        // 7. 组装并返回结果
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
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该就诊人不属于当前用户，无法删除");
        }

        // 2. 删除用户与就诊人的关联关系
        // 注意：UserPatient 使用复合主键，必须使用 QueryWrapper 删除
        int deleteResult = userPatientMapper.delete(
                new LambdaQueryWrapper<UserPatient>()
                        .eq(UserPatient::getUserId, userId)
                        .eq(UserPatient::getPatientId, patientId)
        );

        if (deleteResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "删除关联关系失败");
        }

        // 注意：这里只删除关联关系，不删除 patient 表中的数据
        // 因为该患者可能被其他用户关联
    }
}

