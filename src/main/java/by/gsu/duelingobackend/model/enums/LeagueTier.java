package by.gsu.duelingobackend.model.enums;

import by.gsu.duelingobackend.dto.response.LeagueResponse;

public enum LeagueTier {
    EMBER("Ember", 0, 0),
    COPPER("Copper", 200, 30),
    SILVER("Silver", 450, 50),
    GOLD("Gold", 750, 75),
    SAPPHIRE("Sapphire", 1100, 110),
    EMERALD("Emerald", 1500, 150),
    RUBY("Ruby", 1950, 200),
    OBSIDIAN("Obsidian", 2450, 260),
    DIAMOND("Diamond", 3000, 340),
    LEGEND("Legend", 3650, 500);

    private final String displayName;
    private final int minimumPoints;
    private final int promotionGold;

    LeagueTier(String displayName, int minimumPoints, int promotionGold) {
        this.displayName = displayName;
        this.minimumPoints = minimumPoints;
        this.promotionGold = promotionGold;
    }

    public int promotionGold() {
        return promotionGold;
    }

    public static LeagueTier forPoints(int points) {
        LeagueTier result = EMBER;
        for (LeagueTier tier : values()) {
            if (points < tier.minimumPoints) break;
            result = tier;
        }
        return result;
    }

    public LeagueResponse response(int points) {
        LeagueTier[] tiers = values();
        int index = ordinal();
        Integer next = index == tiers.length - 1 ? null : tiers[index + 1].minimumPoints;
        int range = next == null ? 1 : next - minimumPoints;
        int progress = next == null ? 100 : Math.max(0, Math.min(100,
                (int) Math.round((points - minimumPoints) * 100.0 / range)));
        return new LeagueResponse(name(), displayName, minimumPoints, next, progress,
                next == null ? null : Math.max(0, next - points));
    }
}
