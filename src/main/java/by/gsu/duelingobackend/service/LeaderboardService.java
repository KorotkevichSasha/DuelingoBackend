package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.LeaderboardResponse;
import by.gsu.duelingobackend.dto.response.PaginationResponse;
import by.gsu.duelingobackend.dto.response.user.UserInLeaderboardResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static by.gsu.duelingobackend.util.Constants.USER_NOT_FOUND_BY_ID_ERR_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private static final String LEADERBOARD_KEY = "leaderboard";

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    public void updateUserScore(UUID userId, Integer points) {
        redisTemplate.opsForZSet().add(
                LEADERBOARD_KEY,
                userId.toString(),
                points.doubleValue()
        );
    }

    public Long getUserRank(UUID userId) {
        Long rank = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
        return rank != null ? rank + 1 : null;
    }

    public Integer getUserPoints(UUID userId) {
        Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
        return score != null ? score.intValue() : 0;
    }

    public UserInLeaderboardResponse getUserInLeaderboard(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID_ERR_MSG, userId)));
        return new UserInLeaderboardResponse(
                user.getId(),
                user.getUsername(),
                user.getPoints(),
                user.getAvatarUrl(),
                getUserRank(userId)
        );
    }

    public PaginationResponse<UserInLeaderboardResponse> getTopUsers(int page, int size) {
        long totalElements = redisTemplate.opsForZSet().size(LEADERBOARD_KEY);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = page * size;
        int end = start + size - 1;

        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(LEADERBOARD_KEY, start, end);

        List<UUID> userIds = tuples.stream()
                .map(tuple -> {
                    try {
                        return UUID.fromString((String) tuple.getValue());
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid UUID format in leaderboard: {}", tuple.getValue());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        Map<UUID, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<UserInLeaderboardResponse> responses = new ArrayList<>();
        long position = start + 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            try {
                UUID userId = UUID.fromString((String) tuple.getValue());
                User user = usersMap.get(userId);

                if (user == null) {
                    log.warn("User not found for ID: {}", userId);
                    continue;
                }

                responses.add(new UserInLeaderboardResponse(
                        userId,
                        user.getUsername(),
                        tuple.getScore().intValue(),
                        user.getAvatarUrl(),
                        position++
                ));
            } catch (IllegalArgumentException e) {
                log.error("Skipping invalid UUID: {}", tuple.getValue());
            }
        }

        return new PaginationResponse<>(
                responses,
                page,
                totalPages,
                totalElements
        );
    }

    @PostConstruct
    public void initializeLeaderboard() {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(LEADERBOARD_KEY))) {
            List<User> users = userRepository.findAll();
            users.forEach(user ->
                    redisTemplate.opsForZSet().add(
                            LEADERBOARD_KEY,
                            user.getId().toString(),
                            user.getPoints()
                    )
            );
        }
    }


    public LeaderboardResponse getLeaderboardWithUser(int page, int size, UserDetailsImpl principal) {
        PaginationResponse<UserInLeaderboardResponse> leaderboard = getTopUsers(page, size);

        UserInLeaderboardResponse currentUser = (principal != null) ? getUserInLeaderboard(principal.getUser().getId()) : null;

        return new LeaderboardResponse(leaderboard, currentUser);
    }
}
