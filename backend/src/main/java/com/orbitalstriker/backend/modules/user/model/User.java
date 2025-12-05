package com.orbitalstriker.backend.modules.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String email;

    //GAME STATS
    @Builder.Default
    private Integer xp = 0;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Integer eloRating = 1000;

    @Builder.Default
    private Integer wins = 0;

    @Builder.Default
    private Integer losses = 0;

    @Builder.Default
    private Integer draws = 0;

    @Builder.Default
    private Integer goalsScored = 0;

    @Builder.Default
    private Integer goalsConceded = 0;

    @Builder.Default
    private Integer totalMatches = 0;

    @Builder.Default
    private Boolean isPremium = false;

    @Builder.Default
    private Integer bpLevel = 1;

    @Builder.Default
    private Integer bpXp = 0;

    @Builder.Default
    private Integer gold = 500;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
