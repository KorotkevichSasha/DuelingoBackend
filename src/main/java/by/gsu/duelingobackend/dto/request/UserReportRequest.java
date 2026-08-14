package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UserReportRequest(
        UUID reportedUserId,
        @NotBlank @Size(max = 80) String reason
) {
}
