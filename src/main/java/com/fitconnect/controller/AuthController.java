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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            // Có thể trả về response chi tiết hơn
            return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dữ liệu không hợp lệ.");
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
}