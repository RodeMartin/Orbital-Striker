package com.orbitalstriker.backend.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private String username;
    private Integer eloRating;
    private Integer wins;
    private Integer totalMatches;
    private String winRate;
}