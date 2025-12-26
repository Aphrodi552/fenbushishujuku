package com.example.hospital.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("doctor")
public class Doctor {
    @TableId
    private String doctorId;     // 主键

    private String userId;       // 外键（User.user_id）
    private String hospitalId;   // 院区/分片键
    private String doctorName;   // 姓名
    private String doctorGender; // '男'/'女'
    private String doctorIdcard; // 非空且唯一
    private String title;        // 职称
    private String doctorPhone;  // 非空且唯一
    private String doctorEmail;  // 可空且唯一
    private String doctorIntro;  // 简介
    private String avatarUrl;    // 头像URL

    /** ✅ 新增：科室ID（使 getDepartmentId() 存在，订单管理可查科室） */
    @TableField("department_id")
    private String departmentId;


}
