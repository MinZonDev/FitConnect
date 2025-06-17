package com.fitconnect.model.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
public class GroupMemberId implements Serializable {
    private UUID userId;
    private UUID groupId;
}
