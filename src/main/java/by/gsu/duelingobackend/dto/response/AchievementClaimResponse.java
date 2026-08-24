package by.gsu.duelingobackend.dto.response;

public record AchievementClaimResponse(
        int claimedGold,
        int totalGold,
        UserAchievementResponse achievement
) {}
