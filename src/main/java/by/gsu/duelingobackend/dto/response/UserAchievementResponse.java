package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.model.enums.AchievementLevel;
import by.gsu.duelingobackend.model.enums.AchievementType;

import java.util.UUID;

public record UserAchievementResponse(
        UUID achievementId,
        String title,
        String description,
        AchievementType type,
        AchievementLevel level,
        int requiredValue,
        int currentValue,
        boolean isAchieved,
        String iconUrl
) {}

