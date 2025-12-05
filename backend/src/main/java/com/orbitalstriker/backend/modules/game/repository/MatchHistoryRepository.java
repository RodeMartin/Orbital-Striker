package com.orbitalstriker.backend.modules.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.orbitalstriker.backend.modules.game.model.db.MatchHistory;

@Repository
public interface MatchHistoryRepository extends JpaRepository<MatchHistory, Long> {
}
