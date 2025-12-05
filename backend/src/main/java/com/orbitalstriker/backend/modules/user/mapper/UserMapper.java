package com.orbitalstriker.backend.modules.user.mapper;

import com.orbitalstriker.backend.modules.user.dto.LeaderboardEntryDto;
import com.orbitalstriker.backend.modules.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public LeaderboardEntryDto toLeaderboardDto(User user) {
        double wr = 0.0;
        if (user.getTotalMatches() > 0) {
            wr = (double) user.getWins() / user.getTotalMatches() * 100.0;
        }

        return LeaderboardEntryDto.builder()
                .username(user.getUsername())
                .eloRating(user.getEloRating())
                .wins(user.getWins())
                .totalMatches(user.getTotalMatches())
                .winRate(String.format("%.1f%%", wr)) // pl: "55.4%"
                .build();
    }
}