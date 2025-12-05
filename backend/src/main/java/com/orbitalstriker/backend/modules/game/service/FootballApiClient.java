package com.orbitalstriker.backend.modules.game.service;

import com.orbitalstriker.backend.modules.game.dto.football.ApiPlayer;
import com.orbitalstriker.backend.modules.game.dto.football.ApiTeam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
@Slf4j
public class FootballApiClient {

    // Ezt hívja a TeamImportService
    public List<ApiTeam> getTeamsByLeague(int leagueId, int season) {
        log.info("[FootballApiClient] LOCAL MODE - getTeamsByLeague({}, {}) hívva. " +
                 "Külső API letiltva, üres lista tér vissza.", leagueId, season);
        return Collections.emptyList();
    }

    public List<ApiPlayer> getPlayersByTeam(Integer teamId, int season) {
        log.info("[FootballApiClient] LOCAL MODE - getPlayersByTeam({}, {}) hívva. " +
                 "Külső API letiltva, üres lista tér vissza.", teamId, season);
        return Collections.emptyList();
    }

    public List<ApiTeam> getTeams(int leagueId, int season) {
        return getTeamsByLeague(leagueId, season);
    }

    public List<ApiPlayer> getPlayers(int teamId, int season) {
        return getPlayersByTeam(teamId, season);
    }
}
