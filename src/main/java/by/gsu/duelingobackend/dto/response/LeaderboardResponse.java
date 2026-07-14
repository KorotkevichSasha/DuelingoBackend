package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.dto.response.user.UserInLeaderboardResponse;

public record LeaderboardResponse(
        PaginationResponse<UserInLeaderboardResponse> top,
        UserInLeaderboardResponse currentUser
) {
}
