package com.example.hospital.service;

import com.example.hospital.dto.LoginRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求体
     * @return JWT Token
     */
    String login(LoginRequest loginRequest);

}

