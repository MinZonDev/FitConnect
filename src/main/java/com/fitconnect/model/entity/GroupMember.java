package com.fitconnect.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "group_members")
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Ánh xạ tới thuộc tính userId trong GroupMemberId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId") // Ánh xạ tới thuộc tính groupId trong GroupMemberId
    @JoinColumn(name = "group_id")
    private WorkoutGroup group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupRole role = GroupRole.MEMBER;

    @Column(name = "joined_at")
    private ZonedDateTime joinedAt = ZonedDateTime.now();
}
