package by.gsu.duelingobackend.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeagueTierTest {

    @Test
    void resolvesAllBoundaryRatings() {
        assertThat(LeagueTier.forPoints(0)).isEqualTo(LeagueTier.EMBER);
        assertThat(LeagueTier.forPoints(199)).isEqualTo(LeagueTier.EMBER);
        assertThat(LeagueTier.forPoints(200)).isEqualTo(LeagueTier.COPPER);
        assertThat(LeagueTier.forPoints(750)).isEqualTo(LeagueTier.GOLD);
        assertThat(LeagueTier.forPoints(3000)).isEqualTo(LeagueTier.DIAMOND);
        assertThat(LeagueTier.forPoints(3650)).isEqualTo(LeagueTier.LEGEND);
    }

    @Test
    void reportsProgressWithinCurrentLeague() {
        var league = LeagueTier.COPPER.response(325);

        assertThat(league.progressPercent()).isEqualTo(50);
        assertThat(league.pointsToNextLeague()).isEqualTo(125);
    }
}
