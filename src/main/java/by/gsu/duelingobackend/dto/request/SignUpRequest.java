package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters long")
        String username,

        @NotBlank(message = "Email address cannot be empty")
        @Email(message = "Email address must be in the format user@example.com")
        @Size(min = 5, max = 255, message = "Email address must be between 5 and 255 characters long")
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
        String password
) {
}
