package com.cafeattack.springboot.Config;

import com.cafeattack.springboot.Config.Jwt.JwtAuthenticationFilter;
import com.cafeattack.springboot.Config.Jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    private String[] possibleAccess = {"/api/auth/signup", "/api/auth/email-verification"
            , "/api/auth/login", "/api/error", "/api", "/error", "/auth/**" };

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, RedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers((header) -> header.frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()));
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)  // 나중에 주석처리하기
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorizeRequests)->
                        authorizeRequests
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, possibleAccess).permitAll()
                                .requestMatchers(HttpMethod.GET, possibleAccess).permitAll()
                                .requestMatchers(HttpMethod.PUT, possibleAccess).permitAll()
                                .requestMatchers(HttpMethod.DELETE, possibleAccess).permitAll()
                                .requestMatchers(HttpMethod.PATCH, possibleAccess).permitAll()
                                .requestMatchers("/member/{memberid}/logout").permitAll()
                                .requestMatchers("/member/{memberid}/signout").permitAll()
                                .requestMatchers("/member/**").authenticated()
                                .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}