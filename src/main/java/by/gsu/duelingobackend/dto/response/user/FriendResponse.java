package by.gsu.duelingobackend.dto.response.user;

import java.util.UUID;

public record FriendResponse(

        UUID id,
        String username,
        Integer points,
        String avatarUrl
) {
}
