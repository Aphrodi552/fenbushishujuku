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
        // 添加调试日志
        System.out.println("=== JWT拦截器被触发 ===");
        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("Handler类型: " + handler.getClass().getName());
        
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            System.out.println("Handler不是HandlerMethod，跳过拦截");
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();
        
        System.out.println("方法: " + method.getName());
        System.out.println("类: " + clazz.getName());
        
        // 检查方法或类上是否有@PassToken注解，有则跳过认证
        PassToken passToken = method.getAnnotation(PassToken.class);
        if (passToken == null) {
            passToken = clazz.getAnnotation(PassToken.class);
        }
        if (passToken != null && passToken.required()) {
            System.out.println("检测到@PassToken注解，跳过认证");
            return true;
        }
        
        // 从http请求头中取出token
        String token = request.getHeader("Authorization");
        System.out.println("Authorization头: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        
        // 检查方法或类上是否有@UserLoginToken注解
        UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
        if (userLoginToken == null) {
            userLoginToken = clazz.getAnnotation(UserLoginToken.class);
            System.out.println("方法上没有@UserLoginToken，检查类级别注解");
        } else {
            System.out.println("方法上有@UserLoginToken注解");
        }
        
        if (userLoginToken != null) {
            System.out.println("检测到@UserLoginToken注解，required=" + userLoginToken.required());
        } else {
            System.out.println("未检测到@UserLoginToken注解，跳过认证");
        }
        
        if (userLoginToken != null && userLoginToken.required()) {
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
            
            // 添加调试日志
            System.out.println("拦截器提取的用户信息 - userId: " + userId + ", username: " + username + ", role: " + role);
            
            if (userId == null || userId.isEmpty()) {
                System.err.println("警告：从token中提取的userId为空！");
                throw new UnauthorizedException("token中缺少用户信息，请重新登录");
            }
            
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
            
            System.out.println("=== 拦截器执行完成，userId已设置到request ===");
        } else {
            System.out.println("=== 拦截器执行完成，但未设置userId（没有@UserLoginToken注解或required=false） ===");
        }
        
        // 最终检查：如果请求路径是 /api/patients 但 userId 为空，记录警告
        if (request.getRequestURI().startsWith("/api/patients")) {
            String finalUserId = (String) request.getAttribute("userId");
            if (finalUserId == null) {
                System.err.println("警告：/api/patients 请求的 userId 为空！");
                System.err.println("Token: " + (token != null ? "存在" : "不存在"));
                System.err.println("UserLoginToken注解: " + (userLoginToken != null ? "存在" : "不存在"));
            }
        }
        
        return true;
    }
}
