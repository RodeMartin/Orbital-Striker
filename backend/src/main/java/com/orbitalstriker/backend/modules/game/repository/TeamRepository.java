package com.orbitalstriker.backend.modules.game.repository;

import com.orbitalstriker.backend.modules.game.model.db.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, String> {

    // pl. "Bundesliga", "Premier League", stb.
    List<Team> findByLeague(String league);
}
