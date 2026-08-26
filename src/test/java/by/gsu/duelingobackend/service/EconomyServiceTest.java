package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.EconomyResponse;
import by.gsu.duelingobackend.model.Duel;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EconomyServiceTest {

    @Mock private UserRepository users;

    @Test
    void restoresRushSparksWithoutExceedingTheMaximum() {
        User user = player(false, 100, 8);
        user.setRushChargesUpdatedAt(LocalDateTime.now().minusMinutes(125));
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        EconomyResponse result = new EconomyService(users).getEconomy(user.getId());

        assertThat(result.rushCharges()).isEqualTo(10);
        assertThat(result.nextRushChargeAt()).isNull();
    }

    @Test
    void rankedDuelConsumesOnlyRealPlayersRushSparks() {
        User human = player(false, 100, 10);
        User virtualOpponent = player(true, 0, 10);
        List<UUID> ids = List.of(human.getId(), virtualOpponent.getId()).stream().sorted().toList();
        when(users.findAllByIdForUpdate(ids)).thenReturn(List.of(human, virtualOpponent));

        new EconomyService(users).consumeRankedDuelCharges(ids);

        assertThat(human.getRushCharges()).isEqualTo(9);
        assertThat(virtualOpponent.getRushCharges()).isEqualTo(10);
    }

    @Test
    void hardRankedWinnerReceivesMoreGoldThanLoser() {
        User winner = player(false, 10, 10);
        User loser = player(false, 10, 10);
        List<UUID> ids = List.of(winner.getId(), loser.getId()).stream().sorted().toList();
        when(users.findAllByIdForUpdate(ids)).thenReturn(List.of(winner, loser));
        Duel duel = Duel.builder()
                .player1(winner).player2(loser)
                .player1Score(9).player2Score(4)
                .difficulty(QuestionDifficulty.HARD)
                .ranked(true)
                .build();

        Map<UUID, EconomyService.DuelReward> rewards = new EconomyService(users).awardDuelGold(
                duel, Map.of(winner.getId(), 0, loser.getId(), 0));

        assertThat(rewards.get(winner.getId()).totalGold()).isEqualTo(25);
        assertThat(rewards.get(loser.getId()).totalGold()).isEqualTo(8);
        assertThat(winner.getGold()).isEqualTo(35);
        assertThat(loser.getGold()).isEqualTo(18);
    }

    @Test
    void rewardsEachNewLeagueOnlyOnce() {
        User promoted = player(false, 100, 10);
        promoted.setPoints(205);
        User opponent = player(false, 100, 10);
        opponent.setHighestLeagueRewarded(0);
        List<UUID> ids = List.of(promoted.getId(), opponent.getId()).stream().sorted().toList();
        when(users.findAllByIdForUpdate(ids)).thenReturn(List.of(promoted, opponent));
        Duel duel = Duel.builder()
                .player1(promoted).player2(opponent)
                .player1Score(8).player2Score(4)
                .difficulty(QuestionDifficulty.MEDIUM)
                .ranked(true)
                .build();
        EconomyService service = new EconomyService(users);

        var first = service.awardDuelGold(
                duel, Map.of(promoted.getId(), 190, opponent.getId(), 0));

        assertThat(first.get(promoted.getId()).duelGold()).isEqualTo(18);
        assertThat(first.get(promoted.getId()).leagueBonusGold()).isEqualTo(30);
        assertThat(first.get(promoted.getId()).totalGold()).isEqualTo(48);
        assertThat(promoted.getHighestLeagueRewarded()).isEqualTo(1);

        duel.setRewardsSettled(false);
        var repeated = service.awardDuelGold(
                duel, Map.of(promoted.getId(), 190, opponent.getId(), 0));

        assertThat(repeated.get(promoted.getId()).leagueBonusGold()).isZero();
        assertThat(repeated.get(promoted.getId()).totalGold()).isEqualTo(18);
    }

    @Test
    void dailyTipCanOnlyBeClaimedOncePerDay() {
        User user = player(false, 10, 10);
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        EconomyService service = new EconomyService(users);

        var first = service.claimDailyTipReward(user.getId());
        var second = service.claimDailyTipReward(user.getId());

        assertThat(first.goldAwarded()).isEqualTo(2);
        assertThat(first.firstCompletion()).isTrue();
        assertThat(second.goldAwarded()).isZero();
        assertThat(second.firstCompletion()).isFalse();
        assertThat(user.getGold()).isEqualTo(12);
        assertThat(user.getLastDailyTipRewardAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void listeningRewardDependsOnAccuracyAndHasADailyCap() {
        User user = player(false, 10, 10);
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        EconomyService service = new EconomyService(users);

        assertThat(service.awardListeningGold(user.getId(), 59).goldAwarded()).isZero();
        assertThat(service.awardListeningGold(user.getId(), 60).goldAwarded()).isEqualTo(1);
        assertThat(service.awardListeningGold(user.getId(), 80).goldAwarded()).isEqualTo(2);
        assertThat(service.awardListeningGold(user.getId(), 95).goldAwarded()).isEqualTo(3);
        service.awardListeningGold(user.getId(), 95);
        service.awardListeningGold(user.getId(), 95);
        service.awardListeningGold(user.getId(), 95);
        assertThat(service.awardListeningGold(user.getId(), 95).goldAwarded()).isZero();
        assertThat(user.getListeningGoldToday()).isEqualTo(15);
        assertThat(user.getGold()).isEqualTo(25);
    }

    private User player(boolean virtual, int gold, int charges) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(UUID.randomUUID().toString())
                .points(0)
                .gold(gold)
                .rushCharges(charges)
                .rushChargesUpdatedAt(LocalDateTime.now())
                .virtualPlayer(virtual)
                .build();
    }
}
