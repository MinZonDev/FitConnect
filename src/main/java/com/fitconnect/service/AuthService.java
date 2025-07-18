package com.fitconnect.service;

import com.fitconnect.model.dto.AuthResponse;
import com.fitconnect.model.dto.LoginRequest;
import com.fitconnect.model.dto.LogoutRequest;
import com.fitconnect.model.dto.RegisterRequest;
import com.fitconnect.model.entity.User;
import com.fitconnect.model.entity.UserStatus;
import com.fitconnect.model.entity.VerificationToken;
import com.fitconnect.repository.UserRepository;
import com.fitconnect.repository.VerificationTokenRepository;
import com.fitconnect.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fitconnect.model.dto.ResetPasswordRequest;
import com.fitconnect.model.entity.PasswordResetToken;
import com.fitconnect.repository.PasswordResetTokenRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GeometryFactory geometryFactory;
    private final FirebaseStorageService firebaseStorageService; // Thêm service
    private final EmailService emailService; // Thêm service
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public void register(RegisterRequest request, MultipartFile avatarFile) throws IOException {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Mật khẩu xác nhận không khớp.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email đã được sử dụng");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());

        // CẬP NHẬT TRẠNG THÁI
        user.setStatus(UserStatus.ACTIVE);
        user.setVerified(false); // Chưa xác thực

        // Upload avatar nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = firebaseStorageService.uploadFile(avatarFile);
            user.setAvatarUrl(avatarUrl);
        }

        // ... logic chuyển đổi vị trí ...

        User savedUser = userRepository.save(user);

        // Tạo và gửi OTP
        String otp = generateOtp();
        createVerificationTokenForUser(savedUser, otp);
        emailService.sendOtpEmail(savedUser.getEmail(), otp);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 1. Tải thông tin người dùng trước để kiểm tra thủ công
        User user = userRepository.findByEmail(request.getEmail())
                // Nếu không tìm thấy email, ném lỗi sai thông tin đăng nhập
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không chính xác."));

        // 2. Kiểm tra xem tài khoản đã được xác thực chưa
        if (!user.isVerified()) {
            throw new DisabledException("Tài khoản chưa được xác thực. Vui lòng kiểm tra email để lấy mã OTP.");
        }

        // 3. Kiểm tra các trạng thái khác, ví dụ: tài khoản bị khóa
        if (user.getStatus() == UserStatus.BANNED) {
            throw new LockedException("Tài khoản của bạn đã bị khóa.");
        }

        // 4. Tiến hành xác thực bằng Spring Security để kiểm tra mật khẩu
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // Nếu sai mật khẩu, ném lại ngoại lệ với thông điệp thống nhất
            throw new BadCredentialsException("Email hoặc mật khẩu không chính xác.");
        }

        // 5. Nếu xác thực thành công, tạo token và trả về
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }

    // Logic logout sẽ phức tạp hơn, cần 1 service để quản lý token bị thu hồi
    // Ví dụ đơn giản:
    public void logout(LogoutRequest request) {
        // Thêm refreshToken vào một blocklist (ví dụ: Redis, DB)
        // TokenBlocklistService.blockToken(request.getRefreshToken());
        System.out.println("Token " + request.getRefreshToken() + " đã được thu hồi.");
    }

    @Transactional(readOnly = true)
    public AuthResponse.UserDto getMyAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if ("anonymousUser".equals(principal)) {
            throw new IllegalStateException("Người dùng chưa được xác thực.");
        }

        User user;
        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof String) {
            String email = (String) principal;
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));
        } else {
            throw new IllegalStateException("Principal không hợp lệ.");
        }

        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public AuthResponse.UserDto getUserAccount(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Transactional
    public void verifyAccount(String email, String otp) {
        VerificationToken token = tokenRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Email không hợp lệ hoặc chưa đăng ký."));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn.");
        }
        if (!token.getToken().equals(otp)) {
            throw new RuntimeException("Mã OTP không chính xác.");
        }

        User user = token.getUser();
        user.setVerified(true);
        userRepository.save(user);

        // Xóa token sau khi đã xác thực thành công
        tokenRepository.delete(token);
    }

    private String generateOtp() {
        return new DecimalFormat("000000").format(new SecureRandom().nextInt(999999));
    }

    private void createVerificationTokenForUser(User user, String token) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // Hết hạn sau 10 phút
        tokenRepository.save(verificationToken);
    }

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        // Nếu không tìm thấy email, không làm gì cả để tránh lộ thông tin người dùng
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken();
        myToken.setToken(token);
        myToken.setUser(user);
        myToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Token có hiệu lực trong 1 giờ
        passwordResetTokenRepository.save(myToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Mật khẩu xác nhận không khớp.");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token đặt lại mật khẩu không hợp lệ."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đặt lại mật khẩu đã hết hạn.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa token sau khi đã sử dụng
        passwordResetTokenRepository.delete(resetToken);
    }
}