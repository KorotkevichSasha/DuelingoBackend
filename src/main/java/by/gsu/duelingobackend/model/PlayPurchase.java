package by.gsu.duelingobackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "play_purchases")
public class PlayPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "purchase_token_hash", nullable = false, unique = true, length = 64)
    private String purchaseTokenHash;

    @Column(name = "product_id", nullable = false, length = 80)
    private String productId;

    @Column(name = "gold_awarded", nullable = false)
    private int goldAwarded;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;
}
