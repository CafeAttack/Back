package com.cafeattack.springboot.Service;

import com.cafeattack.springboot.Exception.BadRequestException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@PropertySource("classpath:application.properties")
@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String name;

    private MimeMessage createEmailRequestMessage(String to, String code)
            throws MessagingException, UnsupportedEncodingException {
        log.info("보내는 대상 : " + to);
        log.info("인증 번호 : " + code);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        mimeMessage.addRecipients(MimeMessage.RecipientType.TO, to);
        mimeMessage.setSubject("CafeAttack 회원가입 인증 코드 메일");

        // 메일 내용
        String message = "";
        message += "<h3>아래 확인 코드를 회원가입 화면에서 입력해주세요.</h3>";
        message += "<h1>" + code + "</h1>";
        message += "<h3>감사합니다.</h3>";
        mimeMessage.setText(message, "utf-8", "html");
        mimeMessage.setFrom(new InternetAddress(name, "CafeAttack"));
        // name : 보내는 사람의 메일 주소, "CafeAttack" : 보내는 사람 이름

        return mimeMessage;
    }

    public void sendValidateEmailRequestMessage(String to, String code) {
        try {
            javaMailSender.send(createEmailRequestMessage(to, code));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("이메일 발송 실패");
        }
    }
}
