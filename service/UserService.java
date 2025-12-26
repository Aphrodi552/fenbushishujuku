package com.example.hospital.service;

import com.example.hospital.dto.UserProfileUpdateRequest;
import com.example.hospital.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户实体
     */
    User getUserById(String userId);

    /**
     * 更新用户个人资料
     * @param userId 用户ID
     * @param request 更新请求体
     * @return 更新后的用户实体
     */
    User updateUserProfile(String userId, UserProfileUpdateRequest request);

}

