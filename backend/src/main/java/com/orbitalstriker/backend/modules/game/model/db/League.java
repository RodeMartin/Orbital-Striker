package com.orbitalstriker.backend.modules.game.model.db;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "leagues")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String logoUrl;
    private String slug;
}