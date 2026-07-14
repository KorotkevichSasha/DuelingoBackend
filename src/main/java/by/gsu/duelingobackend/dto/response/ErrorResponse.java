package by.gsu.duelingobackend.dto.response;

public record ErrorResponse(
        int status,
        String message,
        long timestamp
) {
}
