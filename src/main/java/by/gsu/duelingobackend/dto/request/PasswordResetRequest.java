package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank(message = "Email address cannot be empty")
        @Email(message = "Email address must be in the format user@example.com")
        @Size(max = 255, message = "Email address must not exceed 255 characters")
        String email
) {
}
