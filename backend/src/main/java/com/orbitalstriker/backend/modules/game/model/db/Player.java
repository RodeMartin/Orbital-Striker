package com.orbitalstriker.backend.modules.game.model.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;      // "Messi"
    private String position;  // "GK", "DEF", "FWD" (hogy tudjuk hova rakni)

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}