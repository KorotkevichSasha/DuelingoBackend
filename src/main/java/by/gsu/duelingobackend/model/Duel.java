package by.gsu.duelingobackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "duel")
public class Duel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "player1_id")
    private User player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    private User player2;

    @Column(name = "player1_score")
    private Integer player1Score;

    @Column(name = "player1_time")
    private Long player1Time;


    @Column(name = "player2_score")
    private Integer player2Score;

    @Column(name = "player2_time")
    private Long player2Time;

    @Column(name = "question_ids")
    private String[] questionIds;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}
