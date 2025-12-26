package com.example.hospital.service.impl;

import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.LoginRequest;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.AuthService;
import com.example.hospital.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public String login(LoginRequest loginRequest) {
        // 1. 根据手机号查询用户
        User user = userMapper.selectByUserPhone(loginRequest.getUserPhone());

        // 2. 校验用户是否存在
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 3. 校验密码 (注意：这里是明文比较，生产环境应使用加密密码)
        if (!user.getUserPassword().equals(loginRequest.getUserPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR, "密码错误");
        }

        // 4. 校验角色
        if (!user.getRole().equalsIgnoreCase(loginRequest.getRole())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "角色选择错误");
        }

        // 5. 登录成功，生成JWT Token
        return jwtUtils.generateToken(user.getUserId(), user.getUserPhone(), user.getRole());
    }
}

