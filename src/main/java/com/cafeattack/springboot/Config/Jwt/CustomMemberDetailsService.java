package com.cafeattack.springboot.Config.Jwt;

import com.cafeattack.springboot.Domain.Entity.Member;
import com.cafeattack.springboot.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String signId) throws UsernameNotFoundException {
        return memberRepository.findBySignid(signId).stream()
                .map(this::createUserDetails).findFirst()
                .orElseThrow(()->new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다"));
    }

    // 해당하는 User의 객체가 존재할 때
    private UserDetails createUserDetails(Member member) {
        return User.builder()
                .username(member.getSignId())
                .password(passwordEncoder.encode(member.getPassword()))
                .build();
    }
}
