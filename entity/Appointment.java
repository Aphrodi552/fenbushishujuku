package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId
    private String appointmentId; // 主键
    private String userId;        // 外键（User.user_id）
    private String patientId;     // 外键（Patient.patient_id）
    private String scheduleId;    // 外键（Schedule.schedule_id）
    private String hospitalId;    // 医院/院区ID [cite: 36]
    private String status;        // 'BOOKED'/'CANCELLED'/'COMPLETED'/'NO_SHOW'
    private LocalDateTime createdAt; // 默认当前时间
}