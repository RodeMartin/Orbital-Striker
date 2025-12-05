package com.orbitalstriker.backend.modules.game.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "formations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name; 
    
    private String coordinates; 
}