package by.gsu.duelingobackend.dto.response.admin.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserReportResponse(
        UUID id,
        UUID reporterId,
        String reporterUsername,
        UUID reportedUserId,
        String reportedUsername,
        String reason,
        LocalDateTime createdAt
) {
}
