package by.gsu.duelingobackend.dto.response.user;

import by.gsu.duelingobackend.dto.response.LeagueResponse;

import java.util.UUID;

public record UserInLeaderboardResponse(
        UUID id,
        String username,
        Integer points,
        String avatarUrl,
        Long rank,
        Integer pointsToNextRank,
        LeagueResponse league
) {
}
