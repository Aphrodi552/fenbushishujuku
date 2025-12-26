package com.example.hospital.controller;

import com.example.hospital.common.Result;
import com.example.hospital.common.UserLoginToken;
import com.example.hospital.dto.UserProfileUpdateRequest;
import com.example.hospital.entity.User;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户的个人资料
     * @param request HttpServletRequest，用于获取拦截器中存入的用户ID
     * @return 用户信息
     */
    @UserLoginToken // 需要Token验证
    @GetMapping("/profile")
    public Result<User> getUserProfile(HttpServletRequest request) {
        // 从request中获取由拦截器注入的用户ID
        String userId = (String) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 更新当前登录用户的个人资料
     * @param request HttpServletRequest
     * @param updateRequest 包含更新信息的数据体
     * @return 更新后的用户信息
     */
    @UserLoginToken // 需要Token验证
    @PutMapping("/profile")
    public Result<User> updateUserProfile(HttpServletRequest request, @RequestBody UserProfileUpdateRequest updateRequest) {
        String userId = (String) request.getAttribute("userId");
        User updatedUser = userService.updateUserProfile(userId, updateRequest);
        return Result.success(updatedUser);
    }

}

