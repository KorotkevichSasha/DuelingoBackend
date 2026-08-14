package by.gsu.duelingobackend.dto.response;

public record RegistrationResponse(
        String email,
        boolean verificationRequired,
        boolean emailSent
) {
}
