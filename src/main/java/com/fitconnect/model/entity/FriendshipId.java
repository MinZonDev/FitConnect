package com.fitconnect.model.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
public class FriendshipId implements Serializable {
    private UUID requesterId;
    private UUID addresseeId;
}
