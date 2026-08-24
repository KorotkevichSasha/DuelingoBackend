package by.gsu.duelingobackend.dto.response;

public record LearningRewardResponse(
        int goldAwarded,
        int totalGold,
        boolean firstCompletion
) {
}
