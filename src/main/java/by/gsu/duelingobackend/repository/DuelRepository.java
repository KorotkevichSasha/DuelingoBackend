package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.Duel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DuelRepository extends JpaRepository<Duel, UUID> {
    Page<Duel> findAllByPlayer1IdOrPlayer2IdOrderByStartedAtDesc(UUID player1Id, UUID player2Id, Pageable pageable);

    @Query("""
            select d from Duel d
            where d.endedAt is not null
              and (d.player1.id = :userId or d.player2.id = :userId)
            order by d.startedAt desc
            """)
    Page<Duel> findFinishedByUserId(@Param("userId") UUID userId, Pageable pageable);
}
