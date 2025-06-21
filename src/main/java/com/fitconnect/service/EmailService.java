package com.fitconnect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã xác thực tài khoản FitConnect của bạn");
        message.setText("Chào bạn,\n\nMã OTP để xác thực tài khoản FitConnect của bạn là: " + otp + "\n\nMã này sẽ hết hạn sau 10 phút.\n\nTrân trọng,\nĐội ngũ FitConnect");
        mailSender.send(message);
    }
}