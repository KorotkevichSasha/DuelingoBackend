package by.gsu.duelingobackend.dto.response;

import java.time.LocalDateTime;

public record EconomyResponse(
        int gold,
        int rushCharges,
        int maxRushCharges,
        LocalDateTime nextRushChargeAt,
        int minutesPerCharge,
        int ratingPoints,
        LeagueResponse league
) {
}
