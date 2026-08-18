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
import java.util.HashMap;
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

    public void removeUser(UUID userId) {
        redisTemplate.opsForZSet().remove(LEADERBOARD_KEY, userId.toString());
    }

    public Long getUserRank(UUID userId) {
        Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
        return score == null ? null : rankForScore(score);
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
                getUserRank(userId),
                getPointsToNextRank(userId)
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
        Map<Double, Long> ranksByScore = new HashMap<>();
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
                        ranksByScore.computeIfAbsent(tuple.getScore(), this::rankForScore),
                        pointsToNextScore(tuple.getScore())
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
        List<User> users = userRepository.findAll();
        Set<String> actualUserIds = users.stream()
                .map(user -> user.getId().toString())
                .collect(Collectors.toSet());

        Set<Object> cachedUserIds = redisTemplate.opsForZSet().range(LEADERBOARD_KEY, 0, -1);
        if (cachedUserIds != null) {
            cachedUserIds.stream()
                    .filter(cachedId -> !actualUserIds.contains(String.valueOf(cachedId)))
                    .forEach(cachedId -> redisTemplate.opsForZSet().remove(LEADERBOARD_KEY, cachedId));
        }

        users.forEach(user -> updateUserScore(user.getId(), user.getPoints()));
        log.info("Leaderboard synchronized with {} active users", users.size());
    }

    private long rankForScore(double score) {
        Long usersWithMorePoints = redisTemplate.opsForZSet().count(
                LEADERBOARD_KEY,
                Math.nextUp(score),
                Double.POSITIVE_INFINITY
        );
        return (usersWithMorePoints == null ? 0 : usersWithMorePoints) + 1;
    }

    private Integer getPointsToNextRank(UUID userId) {
        Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
        return score == null ? null : pointsToNextScore(score);
    }

    private Integer pointsToNextScore(double score) {
        Set<ZSetOperations.TypedTuple<Object>> next = redisTemplate.opsForZSet()
                .rangeByScoreWithScores(
                        LEADERBOARD_KEY,
                        Math.nextUp(score),
                        Double.POSITIVE_INFINITY,
                        0,
                        1
                );
        if (next == null || next.isEmpty()) {
            return null;
        }
        Double nextScore = next.iterator().next().getScore();
        return nextScore == null ? null : Math.max(1, nextScore.intValue() - (int) score);
    }


    public LeaderboardResponse getLeaderboardWithUser(int page, int size, UserDetailsImpl principal) {
        PaginationResponse<UserInLeaderboardResponse> leaderboard = getTopUsers(page, size);

        UserInLeaderboardResponse currentUser = (principal != null) ? getUserInLeaderboard(principal.getUser().getId()) : null;

        return new LeaderboardResponse(leaderboard, currentUser);
    }
}
