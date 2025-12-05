package com.orbitalstriker.backend.modules.game.dto;

import java.math.BigDecimal; 

public class StartGameRequest {
    private String playerTeamId;
    private String aiTeamId;
    private String difficulty;
    private String playerName;
    private BigDecimal stake; 

    // Getterek és Setterek
    public String getPlayerTeamId() { return playerTeamId; }
    public void setPlayerTeamId(String playerTeamId) { this.playerTeamId = playerTeamId; }

    public String getAiTeamId() { return aiTeamId; }
    public void setAiTeamId(String aiTeamId) { this.aiTeamId = aiTeamId; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public BigDecimal getStake() { return stake; }
    public void setStake(BigDecimal stake) { this.stake = stake; }
}