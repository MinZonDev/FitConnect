package com.fitconnect.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Lob // Dùng cho kiểu TEXT trong DB, cho phép nội dung dài
    private String bio;

    // Lưu trữ vị trí địa lý, yêu cầu extension PostGIS
    @Column(name = "current_location", columnDefinition = "geography(Point, 4326)")
    private Point currentLocation;

    @Column(name = "last_online")
    private ZonedDateTime lastOnline;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // --- CÁC MỐI QUAN HỆ (RELATIONSHIPS) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fitness_goal_id")
    private FitnessGoal fitnessGoal;

    @OneToMany(mappedBy = "creator")
    private Set<WorkoutGroup> createdGroups;

    @OneToMany(mappedBy = "user")
    private Set<GroupMember> groupMemberships;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkoutPlan> workoutPlans;

    // Bạn cũng có thể thêm các mối quan hệ với Friendship, Message nếu cần truy vấn từ User
    // Ví dụ:
    // @OneToMany(mappedBy = "requester")
    // private Set<Friendship> sentFriendRequests;
    //
    // @OneToMany(mappedBy = "addressee")
    // private Set<Friendship> receivedFriendRequests;

    // --- CÁC PHƯƠNG THỨC CỦA INTERFACE 'USERDETAILS' ---

    /**
     * Trả về danh sách các quyền của người dùng.
     * Trong dự án phức tạp hơn, bạn sẽ lấy các quyền (roles) từ database.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Tạm thời, tất cả người dùng đều có quyền là "USER"
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Trả về mật khẩu đã được mã hóa của người dùng.
     */
    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    /**
     * Trả về "username" được sử dụng để xác thực.
     * Trong ứng dụng này, chúng ta dùng email.
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    /**
     * Cho biết tài khoản của người dùng có hết hạn hay không.
     */
    @Override
    public boolean isAccountNonExpired() {
        // Trả về true trừ khi bạn có logic về việc tài khoản hết hạn
        return true;
    }

    /**
     * Cho biết người dùng có bị khóa hay không.
     */
    @Override
    public boolean isAccountNonLocked() {
        // Trả về true trừ khi bạn có chức năng khóa tài khoản
        return true;
    }

    /**
     * Cho biết thông tin xác thực (mật khẩu) của người dùng có hết hạn hay không.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // Trả về true trừ khi bạn bắt người dùng đổi mật khẩu định kỳ
        return true;
    }

    /**
     * Cho biết người dùng có được kích hoạt hay không.
     */
    @Override
    public boolean isEnabled() {
        // Có thể trả về false nếu bạn có chức năng yêu cầu xác thực email
        return true;
    }
}