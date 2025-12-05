package com.orbitalstriker.backend.modules.game.service;

import com.orbitalstriker.backend.modules.game.dto.football.ApiPlayer;
import com.orbitalstriker.backend.modules.game.dto.football.ApiTeam;
import com.orbitalstriker.backend.modules.game.model.db.Player;
import com.orbitalstriker.backend.modules.game.model.db.Team;
import com.orbitalstriker.backend.modules.game.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamImportService {

    private final FootballApiClient footballApiClient;
    private final TeamRepository teamRepository;
    private int season;

    @Transactional
    public void importLeague(int leagueId, String leagueName) {
        List<ApiTeam> apiTeams = footballApiClient.getTeamsByLeague(leagueId, season);

        for (ApiTeam apiTeam : apiTeams) {
            if (teamRepository.existsById(apiTeam.getId().toString())) continue;

            Team team = Team.builder()
                    .id(apiTeam.getId().toString())
                    .name(apiTeam.getName())
                    .league(leagueName)
                    .color("#FFFFFF")
                    .logoUrl(apiTeam.getLogo())
                    .build();

            List<ApiPlayer> apiPlayers =
                    footballApiClient.getPlayersByTeam(apiTeam.getId(), season);

            List<Player> players = apiPlayers.stream()
                    .map(p -> Player.builder()
                            .name(p.getName())
                            .position(mapPosition(p.getPosition()))
                            .team(team)
                            .build())
                    .toList();

            team.setPlayers(players);
            teamRepository.save(team);
        }
    }
    

    private String mapPosition(String pos) {
        if (pos == null) return "FWD";
        String lower = pos.toLowerCase();
        if (lower.contains("goalkeeper")) return "GK";
        if (lower.contains("defender")) return "DEF";
        if (lower.contains("midfield")) return "MID";
        return "FWD";
    }
}
