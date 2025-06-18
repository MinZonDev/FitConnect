package com.fitconnect.service;

import com.fitconnect.model.dto.AuthResponse;
import com.fitconnect.model.dto.LoginRequest;
import com.fitconnect.model.dto.LogoutRequest;
import com.fitconnect.model.dto.RegisterRequest;
import com.fitconnect.model.entity.User;
import com.fitconnect.repository.UserRepository;
import com.fitconnect.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GeometryFactory geometryFactory; // Inject bean này

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email đã được sử dụng"); // Nên dùng custom exception
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());

        // Chuyển đổi DTO vị trí sang Point của JTS
        if (request.getCurrentLocation() != null) {
            Point point = geometryFactory.createPoint(new Coordinate(request.getCurrentLocation().getLongitude(), request.getCurrentLocation().getLatitude()));
            point.setSRID(4326); // Set SRID cho WGS 84
            user.setCurrentLocation(point);
        }

        // Tạm thời bỏ qua fitnessGoalId, cần thêm logic để lấy FitnessGoal entity từ DB

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

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
}