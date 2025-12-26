package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * Department Mapper 接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}

