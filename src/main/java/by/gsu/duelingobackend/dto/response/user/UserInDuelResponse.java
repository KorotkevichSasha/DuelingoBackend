package by.gsu.duelingobackend.dto.response.user;

import java.util.UUID;

public record UserInDuelResponse(
        UUID userId,
        String username,
        int points,
        String avatarUrl
) {
}
