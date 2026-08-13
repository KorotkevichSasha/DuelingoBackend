package by.gsu.duelingobackend.dto.event;

import java.time.Instant;
import java.util.UUID;

public record DuelChallengeEvent(
        UUID challengeId,
        UUID challengerId,
        String challengerUsername,
        String difficulty,
        Instant expiresAt
) {
}
