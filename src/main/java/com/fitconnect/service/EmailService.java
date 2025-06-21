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

    public void sendPasswordResetEmail(String to, String token) {
        // QUAN TRỌNG: Thay thế "http://your-frontend-app.com" bằng domain thực tế của ứng dụng frontend của bạn.
        String resetUrl = "http://your-frontend-app.com/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Yêu cầu đặt lại mật khẩu FitConnect");
        message.setText("Chào bạn,\n\nĐể đặt lại mật khẩu, vui lòng nhấp vào liên kết dưới đây:\n" + resetUrl +
                "\n\nLiên kết này sẽ hết hạn sau 1 giờ." +
                "\nNếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này."+
                "\n\nTrân trọng,\nĐội ngũ FitConnect");
        mailSender.send(message);
    }
}