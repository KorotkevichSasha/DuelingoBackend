package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.model.enums.RelationshipStatus;

import java.util.UUID;

public record RelationshipResponse(
        UUID id,
        UUID fromUserId,
        String fromUsername,
        String fromAvatarUrl,
        UUID toUserId,
        String toUsername,
        String toAvatarUrl,
        RelationshipStatus status
) {
}
