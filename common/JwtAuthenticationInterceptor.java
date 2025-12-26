package com.example.hospital.common;

import com.example.hospital.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * JWT认证拦截器
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        // 检查是否有@PassToken注解，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        
        // 从http请求头中取出token
        String token = request.getHeader("Authorization");
        
        // 检查是否有@UserLoginToken注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // 执行认证
                if (token == null || !token.startsWith("Bearer ")) {
                    throw new UnauthorizedException("无token，请重新登录");
                }
                
                // 获取token，去除"Bearer "前缀
                token = token.substring(7);
                
                // 验证token
                if (!jwtUtils.validateToken(token)) {
                    throw new UnauthorizedException("token无效或已过期");
                }
                
                // 获取token中的用户信息，存入request
                String userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);
                
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);
                
                // 检查角色权限
                if (userLoginToken.roles().length > 0) {
                    boolean hasRole = false;
                    for (String r : userLoginToken.roles()) {
                        if (r.equals(role)) {
                            hasRole = true;
                            break;
                        }
                    }
                    if (!hasRole) {
                        throw new ForbiddenException("权限不足");
                    }
                }
            }
        }
        
        return true;
    }
}
