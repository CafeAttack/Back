package com.cafeattack.springboot.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    String[] accessURL = {"http://localhost:8080","https://localhost:8080","http://3.39.172.187","https://3.39.172.187"};
    // 앱 주소로 바꿔야 함

    // CORS 설정 : 애플리케이션이 다른 도메인에서 오는 요청을 허용할 수 있게 함
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")  // 애플리케이션의 모든 경로에 대해 적용
                .allowedOrigins(accessURL)
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 쿠키 인증 요청 허용
                .maxAge(3000);
            // pre-flight 요청 (서버가 실제 요청 허용하는지 확인)의 결과 캐싱하는 시간 지정
    }
}
