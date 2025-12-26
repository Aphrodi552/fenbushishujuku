// package com.example.hospital.config;

// import com.example.hospital.common.JwtAuthenticationInterceptor;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// /**
//  * Web配置类
//  */
// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     @Autowired
//     private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

//     /**
//      * 添加拦截器
//      */
//     @Override
//     public void addInterceptors(InterceptorRegistry registry) {
//         registry.addInterceptor(jwtAuthenticationInterceptor)
//                 .addPathPatterns("/api/**")  // 拦截所有/api/路径的请求
//                 .excludePathPatterns(
//                         "/api/auth/**",        // 放行认证相关接口
//                         "/api/hospital/departments", // 放行科室列表接口
//                         "/api/hospital/departments/**", // 放行科室详情接口（包含所有子路径）
//                         "/api/hospital/list",  // 放行医院列表接口
//                         "/api/hospitals/**",   // 放行医院相关接口
//                         "/error"               // 放行错误页面
//                 );
//     }

//     /**
//      * 跨域配置
//      */
//     @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         registry.addMapping("/**")
//                 .allowedOriginPatterns("*")
//                 .allowedOrigins("http://localhost:5173")
//                 .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                 .allowedHeaders("*")
//                 .allowCredentials(true)
//                 .maxAge(3600);
//     }
// }
package com.example.hospital.config;

import com.example.hospital.common.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有/api/路径的请求
                .excludePathPatterns(
                        "/api/auth/**",                 // 放行认证相关接口（登录/注册）
                        "/api/hospital/departments",    // 放行科室列表接口
                        "/api/hospital/departments/**", // 放行科室详情接口
                        "/api/hospital/list",           // 放行医院列表接口
                        "/api/hospitals/**",            // 放行医院相关接口
                        "/error"                        // 放行错误页面
                );
    }

    /**
     * 全局跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:5173") // 允许前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*")       // 允许所有请求头
                .allowCredentials(true)    // 允许携带 cookie
                .maxAge(3600);             // 预检请求缓存时间
    }
}
