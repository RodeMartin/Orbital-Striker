package com.orbitalstriker.backend.modules.game.controller;

import com.orbitalstriker.backend.modules.game.model.db.Player;
import com.orbitalstriker.backend.modules.game.model.db.Team;
import com.orbitalstriker.backend.modules.game.repository.PlayerRepository;
import com.orbitalstriker.backend.modules.game.repository.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TeamApiController {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    // ÖSSZES CSAPAT – frontend itt kapja a listát
    @GetMapping
    public List<TeamDto> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(TeamDto::fromEntity)
                .collect(Collectors.toList());
    }

    // LIGÁK LISTÁJA (pl. ["Bundesliga","Premier League",...])
    @GetMapping("/leagues")
    public List<String> getLeagues() {
        return teamRepository.findAll()
                .stream()
                .map(Team::getLeague)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Csapatok adott ligában
    @GetMapping("/league/{league}")
    public List<TeamDto> getTeamsByLeague(@PathVariable String league) {
        return teamRepository.findByLeague(league)
                .stream()
                .map(TeamDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Egy csapat játékosai
    @GetMapping("/api/teams/{teamId}/players")
    public List<PlayerDto> getPlayersByTeam(@PathVariable String teamId) {
        return playerRepository.findByTeam_Id(teamId)
                .stream()
                .map(PlayerDto::fromEntity)
                .collect(Collectors.toList());
    }

    //DTO-k, hogy ne legyen végtelen JSON (Team <-> Player ciklus)

    @Data
    @AllArgsConstructor
    public static class TeamDto {
        private String id;
        private String name;
        private String league;
        private String color;
        private String logoUrl;

        public static TeamDto fromEntity(Team t) {
            return new TeamDto(
                    t.getId(),
                    t.getName(),
                    t.getLeague(),
                    t.getColor(),
                    t.getLogoUrl()
            );
        }
    }

    @Data
    @AllArgsConstructor
    public static class PlayerDto {
        private Long id;
        private String name;
        private String position;

        public static PlayerDto fromEntity(Player p) {
            return new PlayerDto(
                    p.getId(),
                    p.getName(),
                    p.getPosition()
            );
        }
    }
}
