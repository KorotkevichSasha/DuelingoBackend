package by.gsu.duelingobackend.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record WordProgressResponse(
        UUID userId,
        UUID wordId,
        String term,
        String translation,
        Integer repetitions,
        Double easinessFactor,
        LocalDate nextReviewDate
) {}
