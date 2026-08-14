package by.gsu.duelingobackend.dto.request;

import java.util.UUID;

public record DuelChallengeRequest(UUID friendId, String difficulty) {
}
