package com.orbitalstriker.backend.modules.game.repository;

import com.orbitalstriker.backend.modules.game.model.db.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Team.id alapján (pl. "bundesliga-borussia-dortmund")
    List<Player> findByTeam_Id(String teamId);
}
