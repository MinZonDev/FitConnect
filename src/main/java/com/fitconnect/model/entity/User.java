package com.fitconnect.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point; // Từ hibernate-spatial
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

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

    @Lob // Dùng cho kiểu TEXT
    private String bio;

    // Ánh xạ đến Point của PostGIS
    @Column(name = "current_location", columnDefinition = "geography(Point, 4326)")
    private Point currentLocation;

    @Column(name = "last_online")
    private ZonedDateTime lastOnline;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fitness_goal_id")
    private FitnessGoal fitnessGoal;

    @OneToMany(mappedBy = "creator")
    private Set<WorkoutGroup> createdGroups;

    @OneToMany(mappedBy = "user")
    private Set<GroupMember> groupMemberships;

    @OneToMany(mappedBy = "user")
    private Set<WorkoutPlan> workoutPlans;
}
