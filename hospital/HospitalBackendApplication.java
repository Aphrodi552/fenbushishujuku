package com.example.hospital;

import org.mybatis.spring.annotation.MapperScan; // 必须手动加上这一行
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.hospital.mapper")
public class HospitalBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospitalBackendApplication.class, args);
    }
}