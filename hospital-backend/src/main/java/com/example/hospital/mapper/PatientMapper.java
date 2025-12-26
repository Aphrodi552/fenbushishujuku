package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * Patient Mapper 接口
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}

