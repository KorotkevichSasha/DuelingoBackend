package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.EconomyResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import by.gsu.duelingobackend.model.Duel;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.LeagueTier;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EconomyService {

    public static final int MAX_RUSH_CHARGES = 10;
    public static final int MINUTES_PER_CHARGE = 60;

    private final UserRepository users;

    @Transactional
    public EconomyResponse getEconomy(UUID userId) {
        User user = lockedUser(userId);
        refreshRushCharges(user, LocalDateTime.now());
        return response(user);
    }

    @Transactional
    public EconomyResponse purchaseRushPack(UUID userId, String packId) {
        RushPack pack = RushPack.from(packId);
        User user = lockedUser(userId);
        refreshRushCharges(user, LocalDateTime.now());
        if (user.getRushCharges() >= MAX_RUSH_CHARGES) {
            throw new InvalidOperationException("Rush Sparks are already full");
        }
        if (user.getGold() < pack.goldCost) {
            throw new InvalidOperationException("Not enough gold for this Rush Spark pack");
        }
        user.setGold(user.getGold() - pack.goldCost);
        user.setRushCharges(Math.min(MAX_RUSH_CHARGES, user.getRushCharges() + pack.charges));
        if (user.getRushCharges() >= MAX_RUSH_CHARGES) {
            user.setRushChargesUpdatedAt(LocalDateTime.now());
        }
        users.save(user);
        return response(user);
    }

    @Transactional
    public by.gsu.duelingobackend.dto.response.LearningRewardResponse claimDailyTipReward(UUID userId) {
        User user = lockedUser(userId);
        LocalDate today = LocalDate.now();
        boolean awarded = !today.equals(user.getLastDailyTipRewardAt());
        int reward = awarded ? 2 : 0;
        if (awarded) {
            user.setGold(user.getGold() + reward);
            user.setLastDailyTipRewardAt(today);
            users.save(user);
        }
        return new by.gsu.duelingobackend.dto.response.LearningRewardResponse(
                reward, user.getGold(), awarded);
    }

    @Transactional
    public by.gsu.duelingobackend.dto.response.LearningRewardResponse awardListeningGold(
            UUID userId, int similarityPercent) {
        User user = lockedUser(userId);
        LocalDate today = LocalDate.now();
        if (!today.equals(user.getListeningRewardDate())) {
            user.setListeningRewardDate(today);
            user.setListeningGoldToday(0);
        }
        if (user.getListeningGoldToday() == null) {
            user.setListeningGoldToday(0);
        }
        int requested = similarityPercent >= 95 ? 3
                : similarityPercent >= 80 ? 2
                : similarityPercent >= 60 ? 1 : 0;
        int remaining = Math.max(0, 15 - user.getListeningGoldToday());
        int reward = Math.min(requested, remaining);
        if (reward > 0) {
            user.setGold(user.getGold() + reward);
            user.setListeningGoldToday(user.getListeningGoldToday() + reward);
            users.save(user);
        }
        return new by.gsu.duelingobackend.dto.response.LearningRewardResponse(
                reward, user.getGold(), reward > 0);
    }

    @Transactional
    public void consumeRankedDuelCharges(Collection<UUID> participantIds) {
        List<UUID> ids = participantIds.stream().distinct().sorted().toList();
        List<User> participants = users.findAllByIdForUpdate(ids);
        if (participants.size() != ids.size()) {
            throw new EntityNotFoundException("A duel participant no longer exists");
        }
        LocalDateTime now = LocalDateTime.now();
        participants.forEach(user -> refreshRushCharges(user, now));
        List<User> payingPlayers = participants.stream().filter(user -> !user.isVirtualPlayer()).toList();
        if (payingPlayers.stream().anyMatch(user -> user.getRushCharges() <= 0)) {
            throw new InvalidOperationException("Not enough Rush Sparks. Restore one or buy a pack with gold");
        }
        payingPlayers.forEach(user -> {
            if (user.getRushCharges() >= MAX_RUSH_CHARGES) {
                user.setRushChargesUpdatedAt(now);
            }
            user.setRushCharges(user.getRushCharges() - 1);
        });
        users.saveAll(participants);
    }

    @Transactional
    public int awardLearningGold(UUID userId, QuestionDifficulty difficulty) {
        int reward = switch (difficulty) {
            case EASY -> 5;
            case MEDIUM -> 9;
            case HARD -> 14;
        };
        User user = lockedUser(userId);
        user.setGold(user.getGold() + reward);
        users.save(user);
        return reward;
    }

    @Transactional
    public Map<UUID, DuelReward> awardDuelGold(Duel duel, Map<UUID, Integer> previousPoints) {
        Map<UUID, DuelReward> rewards = new LinkedHashMap<>();
        if (!duel.isRanked() || duel.isRewardsSettled()) return rewards;

        List<UUID> ids = List.of(duel.getPlayer1().getId(), duel.getPlayer2().getId())
                .stream().distinct().sorted().toList();
        Map<UUID, User> lockedPlayers = new LinkedHashMap<>();
        users.findAllByIdForUpdate(ids).forEach(user -> lockedPlayers.put(user.getId(), user));
        if (lockedPlayers.size() != ids.size()) {
            throw new EntityNotFoundException("A duel participant no longer exists");
        }
        User player1 = lockedPlayers.get(duel.getPlayer1().getId());
        User player2 = lockedPlayers.get(duel.getPlayer2().getId());
        int comparison = Integer.compare(duel.getPlayer1Score(), duel.getPlayer2Score());
        int win = switch (duel.getDifficulty()) {
            case EASY -> 12;
            case MEDIUM -> 18;
            case HARD -> 25;
        };
        int draw = switch (duel.getDifficulty()) {
            case EASY -> 7;
            case MEDIUM -> 10;
            case HARD -> 14;
        };
        int loss = switch (duel.getDifficulty()) {
            case EASY -> 4;
            case MEDIUM -> 6;
            case HARD -> 8;
        };
        int reward1 = comparison == 0 ? draw : comparison > 0 ? win : loss;
        int reward2 = comparison == 0 ? draw : comparison < 0 ? win : loss;
        rewardPlayer(player1, reward1, previousPoints.getOrDefault(player1.getId(), player1.getPoints()), rewards);
        rewardPlayer(player2, reward2, previousPoints.getOrDefault(player2.getId(), player2.getPoints()), rewards);
        users.saveAll(List.of(player1, player2));
        return rewards;
    }

    @Transactional
    public User refreshUserEconomy(UUID userId) {
        User user = lockedUser(userId);
        refreshRushCharges(user, LocalDateTime.now());
        return user;
    }

    public EconomyResponse response(User user) {
        LocalDateTime next = user.getRushCharges() >= MAX_RUSH_CHARGES
                ? null : user.getRushChargesUpdatedAt().plusMinutes(MINUTES_PER_CHARGE);
        return new EconomyResponse(
                user.getGold(),
                user.getRushCharges(),
                MAX_RUSH_CHARGES,
                next,
                MINUTES_PER_CHARGE,
                user.getPoints(),
                LeagueTier.forPoints(user.getPoints()).response(user.getPoints())
        );
    }

    private User lockedUser(UUID userId) {
        return users.findByIdForUpdate(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void refreshRushCharges(User user, LocalDateTime now) {
        if (user.getRushCharges() >= MAX_RUSH_CHARGES) {
            user.setRushCharges(MAX_RUSH_CHARGES);
            return;
        }
        LocalDateTime updatedAt = user.getRushChargesUpdatedAt();
        if (updatedAt == null || updatedAt.isAfter(now)) {
            user.setRushChargesUpdatedAt(now);
            return;
        }
        long recovered = Duration.between(updatedAt, now).toMinutes() / MINUTES_PER_CHARGE;
        if (recovered <= 0) return;
        int refreshed = Math.min(MAX_RUSH_CHARGES, user.getRushCharges() + (int) recovered);
        user.setRushCharges(refreshed);
        user.setRushChargesUpdatedAt(refreshed >= MAX_RUSH_CHARGES
                ? now : updatedAt.plusMinutes(recovered * MINUTES_PER_CHARGE));
        users.save(user);
    }

    private void rewardPlayer(
            User user,
            int duelGold,
            int previousPoints,
            Map<UUID, DuelReward> rewards
    ) {
        if (user.isVirtualPlayer()) {
            rewards.put(user.getId(), DuelReward.none());
            return;
        }
        LeagueTier previousLeague = LeagueTier.forPoints(previousPoints);
        LeagueTier currentLeague = LeagueTier.forPoints(user.getPoints());
        int alreadyRewarded = user.getHighestLeagueRewarded() == null
                ? previousLeague.ordinal() : user.getHighestLeagueRewarded();
        int rewardFrom = Math.max(alreadyRewarded, previousLeague.ordinal());
        int leagueBonus = 0;
        for (int ordinal = rewardFrom + 1; ordinal <= currentLeague.ordinal(); ordinal++) {
            leagueBonus += LeagueTier.values()[ordinal].promotionGold();
        }
        if (currentLeague.ordinal() > alreadyRewarded) {
            user.setHighestLeagueRewarded(currentLeague.ordinal());
        }
        int total = duelGold + leagueBonus;
        user.setGold(user.getGold() + total);
        rewards.put(user.getId(), new DuelReward(duelGold, leagueBonus, total));
    }

    public record DuelReward(int duelGold, int leagueBonusGold, int totalGold) {
        public static DuelReward none() {
            return new DuelReward(0, 0, 0);
        }
    }

    private enum RushPack {
        POCKET(3, 35),
        BOOST(6, 60),
        VAULT(10, 90);

        private final int charges;
        private final int goldCost;

        RushPack(int charges, int goldCost) {
            this.charges = charges;
            this.goldCost = goldCost;
        }

        private static RushPack from(String value) {
            try {
                return valueOf(value == null ? "POCKET" : value.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new InvalidOperationException("Unknown Rush Spark pack");
            }
        }
    }
}
