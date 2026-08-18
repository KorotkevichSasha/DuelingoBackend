package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.Duel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DuelRepository extends JpaRepository<Duel, UUID> {
    interface DuelStatsProjection {
        long getTotal();
        long getWins();
        long getLosses();
        long getDraws();
    }
    Page<Duel> findAllByPlayer1IdOrPlayer2IdOrderByStartedAtDesc(UUID player1Id, UUID player2Id, Pageable pageable);

    @Query("""
            select d from Duel d
            where d.endedAt is not null
              and (d.player1.id = :userId or d.player2.id = :userId)
            order by d.startedAt desc
            """)
    Page<Duel> findFinishedByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
            SELECT COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE
                       (player1_id = :userId AND player1_score > player2_score) OR
                       (player2_id = :userId AND player2_score > player1_score)) AS wins,
                   COUNT(*) FILTER (WHERE
                       (player1_id = :userId AND player1_score < player2_score) OR
                       (player2_id = :userId AND player2_score < player1_score)) AS losses,
                   COUNT(*) FILTER (WHERE player1_score = player2_score) AS draws
            FROM duel
            WHERE ended_at IS NOT NULL
              AND (player1_id = :userId OR player2_id = :userId)
            """, nativeQuery = true)
    DuelStatsProjection getStatsByUserId(@Param("userId") UUID userId);
}
