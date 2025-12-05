package com.orbitalstriker.backend.modules.game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamDto {
    private String id;
    private String name;
    private String league;
    private String color;
    private String logoUrl;
}
