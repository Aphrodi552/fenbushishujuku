package com.example.hospital.service;

public interface AuditLogRecordService {

    /**
     * 记录审计日志（方案B：应用层落库）
     *
     * @param operatorUserId 操作者用户ID（演示可写固定值，或从JWT解析）
     * @param roleType       角色类型：admin/user/doctor
     * @param action         操作描述
     */
    void record(String operatorUserId, String roleType, String action);
}
