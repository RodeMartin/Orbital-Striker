package com.orbitalstriker.backend.modules.game.controller;

import com.orbitalstriker.backend.modules.game.model.db.League;
import com.orbitalstriker.backend.modules.game.model.db.Team;
import com.orbitalstriker.backend.modules.game.repository.LeagueRepository;
import com.orbitalstriker.backend.modules.game.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GameDataController {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    @GetMapping("/leagues")
    public List<League> getLeagues() {
        return leagueRepository.findAll();
    }

    @GetMapping("/teams")
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}