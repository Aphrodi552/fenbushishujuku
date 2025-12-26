package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId
    private String logId;           // varchar(32), 主键
    private String operatorUserId;  // 外键
    private String hospitalId;      // 分片键
    private String roleType;        // 'user'/'doctor'/'admin'
    private String action;          // varchar(255)
    private LocalDateTime actionTime; // 默认当前时间
}