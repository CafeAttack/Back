package com.cafeattack.springboot.Config.Jwt;

import com.cafeattack.springboot.Exception.BaseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@AllArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException, BaseException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);

        try {
            // 토큰 유효성 검사
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                jwtTokenProvider.checkMemberId(authentication, (HttpServletRequest) request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BaseException baseException) {
            // 예외 발생 시 응답 생성, 전송
            HttpServletResponse result = (HttpServletResponse) response;
            result.setStatus(baseException.getCode());
            result.setContentType("application/json;charset=UTF-8");
            result.getWriter().write("{\"status\":\"" + baseException.getCode() +
                    "\",\"message\":\"" + baseException.getMessage() + "\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
