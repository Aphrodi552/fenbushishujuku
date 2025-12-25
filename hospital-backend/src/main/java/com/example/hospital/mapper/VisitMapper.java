package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.Visit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitMapper extends BaseMapper<Visit> {
}
