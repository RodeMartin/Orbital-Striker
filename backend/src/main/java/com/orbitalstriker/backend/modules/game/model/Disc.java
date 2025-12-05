package com.orbitalstriker.backend.modules.game.model;

import com.orbitalstriker.backend.modules.game.engine.Vector2D;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Disc {
    private String id;
    private String name;
    private String color;
    private Vector2D position;
    private Vector2D velocity;
    private double radius;
    private double mass;
    private double damping;
    private boolean isBall;
}