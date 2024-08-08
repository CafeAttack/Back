package com.cafeattack.springboot.Config.Jwt;

import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Exception.BaseException;
import com.cafeattack.springboot.Repository.MemberRepository;
import com.cafeattack.springboot.Util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    private final Long expiration = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 60;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, AuthenticationManagerBuilder authenticationManagerBuilder,
                            MemberRepository memberRepository, RedisUtil redisUtil) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.memberRepository = memberRepository;
        this.redisUtil = redisUtil;
        byte[] secretByKey = DatatypeConverter.parseBase64Binary(secretKey);
        this.key = Keys.hmacShaKeyFor(secretByKey);
    }

    public JwtToken generateToken(Member member) {
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(member.getAuthenticationToken());

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setExpiration(new Date(expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .memberid(member.getMemberid())
                .build();
    }

    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") != null) {
            throw new RuntimeException("권한 정보 없는 토큰");
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            if (redisUtil.hasKeyBlackList(token)) {
                throw new BaseException(HttpStatus.FORBIDDEN.value(), "로그아웃한 토큰");
            }
            return true;
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty", e);
        }
        return false;
    }

    public void checkMemberId(Authentication authentication, HttpServletRequest request) {
        String tokenEmail = authentication.getName();

        String requestURI = ((HttpServletRequest) request).getRequestURI();
        String[] parts = requestURI.split("/");

        if (checkApiURL(parts))
            return;

        Integer memberid = getMemberid(parts);

        Member member = memberRepository.findById(memberid)
                .orElseThrow(() -> new BaseException(HttpStatus.FORBIDDEN.value(), "유효하지 않은 회원 ID"));

        if (!tokenEmail.equals(member.getEmail())) {
            throw new BaseException(HttpStatus.FORBIDDEN.value(), "유효하지 않은 AccessToken");
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")
            && bearerToken.equals("Bearer null")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public void logoutToken(String accessToken) {
        if (validateToken(accessToken)) {
            Long expirationDate = getExpiration(accessToken);
            // redis에 로그아웃 토큰 저장
            // redis에 accessToken을 사용하지 못하도록 등록
            redisUtil.setBlackList(accessToken, "accessToken", expirationDate);
        }
    }

    private Long getExpiration(String accessToken) {
        // accessToken 남은 유효 시간
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getExpiration();

        // 현재 시간
        Long now = new Date().getTime();

        return (expiration.getTime() - now);
    }

    // LogoutToken 구현하기

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key)
                    .build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private boolean checkApiURL(String[] parts) {
        return parts[1].equals("auth");
    }

    private Integer getMemberid(String[] parts) {
        return Integer.parseInt(parts[2]);
    }
}
