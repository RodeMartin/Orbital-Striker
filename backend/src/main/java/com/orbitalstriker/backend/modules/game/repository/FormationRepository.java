package com.orbitalstriker.backend.modules.game.repository;

import com.orbitalstriker.backend.modules.game.model.Formation; // ITT A HELYES IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {
    Optional<Formation> findByName(String name);
}