package com.orbitalstriker.backend.modules.game.model.db; // 🔥 ajánlott ide tenni, a model/db alá!

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String player1Name;
    private String player2Name;
    private String player1Team;
    private String player2Team;
    private Integer scorePlayer1;
    private Integer scorePlayer2;
    private String winner;
    private Integer durationSeconds;

    @CreationTimestamp
    private LocalDateTime playedAt;
}
