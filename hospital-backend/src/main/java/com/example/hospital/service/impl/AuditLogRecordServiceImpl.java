package com.example.hospital.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.hospital.entity.AuditLog;
import com.example.hospital.mapper.AuditLogMapper;
import com.example.hospital.service.AuditLogRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogRecordServiceImpl implements AuditLogRecordService {

    private final AuditLogMapper auditLogMapper;

    public AuditLogRecordServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public void record(String operatorUserId, String roleType, String action) {
        AuditLog log = new AuditLog();
        log.setLogId(IdWorker.getIdStr()); // 32左右长度的字符串ID
        log.setOperatorUserId(operatorUserId);
        log.setRoleType(roleType);
        log.setAction(action);
        log.setActionTime(LocalDateTime.now());

        auditLogMapper.insert(log);
    }
}
