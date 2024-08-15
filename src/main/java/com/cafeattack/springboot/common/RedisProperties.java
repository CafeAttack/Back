package com.cafeattack.springboot.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter   // Getter 없애도 되면 없애기
@Setter
public class RedisProperties {

    @Value("${spring.data.redis.port}")   // 기본 port 번호
    private int port;
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.password}")
    private String password;

    public int getPort() {
        return this.port;
    }

    public String getHost() {
        return this.host;
    }

    public String getPassword() {
        return this.password;
    }
}
