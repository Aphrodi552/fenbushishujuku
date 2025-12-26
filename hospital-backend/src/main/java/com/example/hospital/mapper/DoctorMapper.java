package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    // 使用 MyBatis-Plus 的 updateById
    // 不允许自定义 UPDATE 语句去修改 user_id / hospital_id / department_id
}
