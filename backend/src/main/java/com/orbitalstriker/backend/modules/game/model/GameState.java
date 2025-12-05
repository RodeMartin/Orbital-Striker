package com.orbitalstriker.backend.modules.game.model;

import com.orbitalstriker.backend.modules.game.engine.Vector2D;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class GameState {
    private String gameId;
    private List<Disc> discs = new ArrayList<>();
    private double width = 800;
    private double height = 600;
    
    private String currentTurn = "player"; 
    private int homeScore = 0;
    private int awayScore = 0;
    
    
    private boolean gameOver = false; 
    private boolean isAiActionInProgress = false;

    public boolean isAiActionInProgress() {
        return isAiActionInProgress;
    }
    public void setAiActionInProgress(boolean aiActionInProgress) {
        this.isAiActionInProgress = aiActionInProgress;
    }

    public GameState() {
        resetBoard();
    }

    public void resetBoard() {
        discs.clear();
        gameOver = false;
        
        // --- LABDA ---
        discs.add(Disc.builder().id("ball").isBall(true).name("").color("#FFFFFF")
                .position(new Vector2D(400, 300)).velocity(new Vector2D(0,0))
                .radius(10).mass(0.8).damping(0.98).build());

        // Bal Kapu (Felső, Alsó)
        addPost(50, 250); 
        addPost(50, 350);
        // Jobb Kapu (Felső, Alsó)
        addPost(750, 250);
        addPost(750, 350);

        // Csapatok (Alapértelmezett formációval)
        createTeam("player", "#004d98", -1); 
        createTeam("enemy_ai", "#EFEFEF", 1); 
    }

    private void addPost(double x, double y) {
        discs.add(Disc.builder()
            .id("POST")
            .name("")
            .color("#AAAAAA")
            .position(new Vector2D(x, y))
            .velocity(new Vector2D(0,0))
            .radius(8)
            .mass(1000.0)
            .damping(0.0)
            .isBall(false)
            .build());
    }

    private void createTeam(String teamId, String color, int side) {
        double centerX = (side == -1) ? 200 : 600;
        double goalX = (side == -1) ? 50 : 750;
        double forwardX = (side == -1) ? 380 : 420;

        addDisc(teamId + "_GK", "", color, goalX, 300);
        addDisc(teamId + "_D1", "", color, centerX, 150);
        addDisc(teamId + "_D2", "", color, centerX, 450);
        addDisc(teamId + "_F1", "", color, forwardX, 250);
        addDisc(teamId + "_F2", "", color, forwardX, 350);
    }

    private void addDisc(String id, String name, String color, double x, double y) {
        discs.add(Disc.builder()
                .id(id).name(name).color(color)
                .position(new Vector2D(x, y)).velocity(new Vector2D(0, 0))
                .radius(18).mass(2.0).damping(0.95).isBall(false).build());
    }
}