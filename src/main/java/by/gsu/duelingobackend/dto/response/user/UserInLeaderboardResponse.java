package by.gsu.duelingobackend.dto.response.user;

import java.util.UUID;

public record UserInLeaderboardResponse(
        UUID id,
        String username,
        Integer points,
        String avatarUrl,
        Long rank
) {
}
