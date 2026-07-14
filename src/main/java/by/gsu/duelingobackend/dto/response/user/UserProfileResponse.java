package by.gsu.duelingobackend.dto.response.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        Integer points,
        String avatarUrl,
        LocalDateTime lastLogin
) implements Serializable {
}
