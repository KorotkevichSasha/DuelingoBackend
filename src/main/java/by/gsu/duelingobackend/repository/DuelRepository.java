package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.Duel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DuelRepository extends JpaRepository<Duel, UUID> {
    Page<Duel> findAllByPlayer1IdOrPlayer2IdOrderByStartedAtDesc(UUID player1Id, UUID player2Id, Pageable pageable);
}
