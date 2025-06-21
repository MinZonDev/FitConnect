package com.fitconnect.controller;

import com.fitconnect.model.dto.AuthResponse;
import com.fitconnect.model.dto.LoginRequest;
import com.fitconnect.model.dto.LogoutRequest;
import com.fitconnect.model.dto.RegisterRequest;
import com.fitconnect.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import com.fitconnect.model.dto.ResetPasswordRequest;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> register(
            @RequestPart("data") @Valid RegisterRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) {
        try {
            authService.register(request, avatarFile);
            return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // Log lỗi ra để debug
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam String email, @RequestParam String otp) {
        try {
            authService.verifyAccount(email, otp);
            return ResponseEntity.ok("Xác thực tài khoản thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Đăng xuất thành công.");
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getMyAccount() {
        var user = authService.getMyAccount();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<AuthResponse.UserDto> getUserAccount(@PathVariable UUID id) {
        var user = authService.getUserAccount(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String userEmail) {
        authService.createPasswordResetTokenForUser(userEmail);
        // Luôn trả về thông báo thành công chung chung để bảo mật, tránh email enumeration attack
        return ResponseEntity.ok("Nếu tài khoản tồn tại, một email đặt lại mật khẩu đã được gửi đi.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}