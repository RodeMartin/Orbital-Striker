package com.orbitalstriker.backend.modules.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMove {
    private String playerId;
    private double vectorX;
    private double vectorY;
}