package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.user.UserInLeaderboardResponse;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Optional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @Mock
    private UserRepository userRepository;

    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        service = new LeaderboardService(redisTemplate, userRepository);
    }

    @Test
    void usersWithEqualPointsReceiveTheSameRank() {
        UUID userId = UUID.randomUUID();
        when(zSetOperations.score("leaderboard", userId.toString())).thenReturn(420.0);
        when(zSetOperations.reverseRangeByScoreWithScores(
                "leaderboard", Math.nextUp(420.0), Double.POSITIVE_INFINITY
        )).thenReturn(Set.of(
                new DefaultTypedTuple<>("first", 700.0),
                new DefaultTypedTuple<>("second", 500.0),
                new DefaultTypedTuple<>("tied-second", 500.0)
        ));

        assertThat(service.getUserRank(userId)).isEqualTo(3L);
    }

    @Test
    void reportsTheRealPointGapToTheNextHigherScore() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("learner").points(420).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(zSetOperations.score("leaderboard", userId.toString())).thenReturn(420.0);
        when(zSetOperations.reverseRangeByScoreWithScores(
                "leaderboard", Math.nextUp(420.0), Double.POSITIVE_INFINITY
        )).thenReturn(Set.of(
                new DefaultTypedTuple<>("first", 700.0),
                new DefaultTypedTuple<>("second", 437.0),
                new DefaultTypedTuple<>("tied-second", 437.0)
        ));
        when(zSetOperations.rangeByScoreWithScores(
                "leaderboard", Math.nextUp(420.0), Double.POSITIVE_INFINITY, 0, 1
        )).thenReturn(Set.of(new DefaultTypedTuple<>("next-user", 437.0)));

        UserInLeaderboardResponse response = service.getUserInLeaderboard(userId);

        assertThat(response.rank()).isEqualTo(3L);
        assertThat(response.pointsToNextRank()).isEqualTo(17);
    }

    @Test
    void topUsersUseDenseRanksWithoutSkippingAfterTies() {
        UUID first = UUID.randomUUID();
        UUID tiedA = UUID.randomUUID();
        UUID tiedB = UUID.randomUUID();
        UUID next = UUID.randomUUID();
        List<User> users = List.of(
                User.builder().id(first).username("first").points(700).build(),
                User.builder().id(tiedA).username("tied-a").points(500).build(),
                User.builder().id(tiedB).username("tied-b").points(500).build(),
                User.builder().id(next).username("next").points(420).build()
        );
        Set<ZSetOperations.TypedTuple<Object>> ranking = new LinkedHashSet<>(List.of(
                new DefaultTypedTuple<>(first.toString(), 700.0),
                new DefaultTypedTuple<>(tiedA.toString(), 500.0),
                new DefaultTypedTuple<>(tiedB.toString(), 500.0),
                new DefaultTypedTuple<>(next.toString(), 420.0)
        ));
        when(zSetOperations.size("leaderboard")).thenReturn(4L);
        when(zSetOperations.reverseRangeWithScores("leaderboard", 0, 9)).thenReturn(ranking);
        when(userRepository.findAllById(List.of(first, tiedA, tiedB, next))).thenReturn(users);

        assertThat(service.getTopUsers(0, 10).content())
                .extracting(UserInLeaderboardResponse::rank)
                .containsExactly(1L, 2L, 2L, 3L);
    }
}
