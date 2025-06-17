package com.fitconnect.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "workout_groups")
public class WorkoutGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "scheduled_time", nullable = false)
    private ZonedDateTime scheduledTime;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "location_coordinates", columnDefinition = "geography(Point, 4326)")
    private Point locationCoordinates;

    @Column(name = "max_members")
    private Integer maxMembers;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fitness_goal_id")
    private FitnessGoal fitnessGoal;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupMember> members;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Message> messages;
}
