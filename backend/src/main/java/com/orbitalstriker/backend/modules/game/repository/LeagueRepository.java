package com.orbitalstriker.backend.modules.game.repository;
import com.orbitalstriker.backend.modules.game.model.db.League;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LeagueRepository extends JpaRepository<League, Long> {}