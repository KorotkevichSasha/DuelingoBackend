package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.PlayPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayPurchaseRepository extends JpaRepository<PlayPurchase, UUID> {
    Optional<PlayPurchase> findByPurchaseTokenHash(String purchaseTokenHash);
}
