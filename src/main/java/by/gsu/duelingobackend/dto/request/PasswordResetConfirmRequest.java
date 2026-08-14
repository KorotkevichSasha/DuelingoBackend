package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "Email address cannot be empty")
        @Email(message = "Email address must be in the format user@example.com")
        @Size(max = 255, message = "Email address must not exceed 255 characters")
        String email,

        @NotBlank(message = "Reset code cannot be empty")
        @Pattern(regexp = "\\d{6}", message = "Reset code must contain exactly six digits")
        String code,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain at least one letter and one digit")
        String newPassword
) {
}
