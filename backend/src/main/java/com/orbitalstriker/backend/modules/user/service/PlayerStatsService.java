package com.orbitalstriker.backend.modules.user.service;

import com.orbitalstriker.backend.modules.game.model.db.MatchHistory;
import com.orbitalstriker.backend.modules.game.repository.MatchHistoryRepository;
import com.orbitalstriker.backend.modules.user.model.User;
import com.orbitalstriker.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerStatsService {

    private final UserRepository userRepository;
    private final MatchHistoryRepository matchHistoryRepository;

    @Transactional
    public void recordMatchEnd(String playerName, String aiName, String pTeam, String aiTeam, int scoreP, int scoreAi) {
        
        String winner = "DRAW";
        if (scoreP > scoreAi) winner = "PLAYER";
        else if (scoreAi > scoreP) winner = "AI";

        MatchHistory history = MatchHistory.builder()
                .player1Name(playerName)
                .player2Name(aiName)
                .player1Team(pTeam)
                .player2Team(aiTeam)
                .scorePlayer1(scoreP)
                .scorePlayer2(scoreAi)
                .winner(winner)
                .durationSeconds(0)
                .build();

        matchHistoryRepository.save(history);

        User user = userRepository.findByUsername(playerName)
                .orElseGet(() -> userRepository.save(User.builder().username(playerName).build()));

        user.setXp(user.getXp() + 100);
        user.setGold(user.getGold() + 50);
        
        user.setBpXp(user.getBpXp() + 50);
        if (user.getBpXp() >= 1000) {
            user.setBpXp(user.getBpXp() - 1000);
            user.setBpLevel(user.getBpLevel() + 1);
        }

        if (scoreP > scoreAi) {
            user.setWins(user.getWins() + 1);
            user.setEloRating(user.getEloRating() + 25);
            user.setGold(user.getGold() + 100);
        } else if (scoreAi > scoreP) {
            user.setEloRating(Math.max(0, user.getEloRating() - 15));
        }

        userRepository.save(user);
        System.out.println("MECCS MENTVE: " + playerName + " vs " + aiName);
    }
}