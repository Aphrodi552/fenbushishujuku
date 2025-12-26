package com.example.hospital.config;

import com.example.hospital.common.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 关键修复点：
     * 使用“前置 CorsFilter（最高优先级）”处理浏览器 CORS 预检（OPTIONS）请求，
     * 避免 OPTIONS 被 JwtAuthenticationInterceptor 拦截导致 403。
     *
     * 说明：
     * - allowedOrigins 这里收敛为前端开发地址（推荐）；如你有多个前端地址，可继续加。
     * - allowCredentials=true 时，allowedOrigins 不能用 "*"，必须显式列出域名。
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration config = new CorsConfiguration();

        // 你的前端开发地址（Vite 默认 5173）
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // 如果你未来还有别的端口或域名，可以继续加：
        // config.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));

        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // 必须最高优先级：确保在拦截器前生效
        return bean;
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有/api/路径的请求
                .excludePathPatterns(
                        "/api/auth/**",              // 放行认证相关接口
                        "/api/hospital/departments", // 放行科室列表接口
                        "/api/hospital/departments/**", // 放行科室详情接口（包含所有子路径）
                        "/api/hospital/list",        // 放行医院列表接口
                        "/api/hospitals/**",         // 放行医院相关接口
                        "/error"                     // 放行错误页面
                );
    }

    /**
     * 跨域配置（保留：MVC 层 CORS 配置）
     * 说明：实际生效优先由上面的 CorsFilter 保障；这里保留不冲突。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
