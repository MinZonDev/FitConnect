package com.fitconnect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi khi người dùng nhập sai email hoặc mật khẩu.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    /**
     * Xử lý lỗi khi tài khoản chưa được kích hoạt (chưa xác thực OTP).
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleDisabledAccount(DisabledException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN); // 403 Forbidden
    }

    /**
     * Xử lý lỗi khi tài khoản bị khóa.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> handleLockedAccount(LockedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN); // 403 Forbidden
    }
}