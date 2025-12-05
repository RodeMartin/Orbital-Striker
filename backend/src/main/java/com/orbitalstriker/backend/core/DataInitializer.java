package com.orbitalstriker.backend.core;

import com.orbitalstriker.backend.modules.game.repository.TeamRepository;
import com.orbitalstriker.backend.modules.game.service.XmlLeagueImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final XmlLeagueImportService xmlLeagueImportService;
    private final TeamRepository teamRepository;

    @Override
    public void run(String... args) {
        if (teamRepository.count() > 0) {
            System.out.println("➡️ Már vannak csapatok az adatbázisban, XML import kihagyva.");
            return;
        }

        System.out.println("===> XML ligák/csapatok/játékosok import indul...");
        xmlLeagueImportService.importLeaguesFromXml();
        System.out.println("===> XML import kész ✅");
    }
}
