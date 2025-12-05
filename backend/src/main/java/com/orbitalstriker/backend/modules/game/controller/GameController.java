package com.orbitalstriker.backend.modules.game.controller;

import com.orbitalstriker.backend.modules.game.dto.PlayerMove;
import com.orbitalstriker.backend.modules.game.service.GameService; 
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor 
public class GameController {

    private final GameService gameService;

    @MessageMapping("/start")
    public void startGame(com.orbitalstriker.backend.modules.game.dto.StartGameRequest request) {
        System.out.println("JÁTÉK INDÍTÁSA: " + request.getPlayerTeamId() + " vs " + request.getAiTeamId());
        gameService.startGame(request);
    }
    
    @MessageMapping("/move")
    public void receiveMove(PlayerMove move) {
        // Átadjuk a parancsot a szimulációnak
        gameService.processPlayerShot(move.getPlayerId(), move.getVectorX(), move.getVectorY());
    }
    
}