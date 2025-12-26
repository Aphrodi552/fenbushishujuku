package com.example.hospital.service.impl;

import com.example.hospital.common.BusinessException;
import com.example.hospital.common.ResultCode;
import com.example.hospital.dto.UserProfileUpdateRequest;
import com.example.hospital.entity.User;
import com.example.hospital.mapper.UserMapper;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }
        // 密码脱敏
        user.setUserPassword(null);
        return user;
    }

    @Override
    public User updateUserProfile(String userId, UserProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 更新手机号 (暂不处理realName)
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            // 检查新手机号是否已被其他用户注册
            User existingUser = userMapper.selectByUserPhone(request.getPhone());
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号已被注册");
            }
            user.setUserPhone(request.getPhone());
        }

        userMapper.updateById(user);
        
        // 返回更新后的用户信息，密码脱敏
        user.setUserPassword(null);
        return user;
    }
}

