package com.orbitalstriker.backend.modules.game.service;

import com.orbitalstriker.backend.modules.game.model.db.Player;
import com.orbitalstriker.backend.modules.game.model.db.Team;
import com.orbitalstriker.backend.modules.game.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XmlLeagueImportService {

    private final TeamRepository teamRepository;

    public void importLeaguesFromXml() {
        try {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("data/leagues.xml");

            if (is == null) {
                System.out.println("❌ leagues.xml nem található a resources/data alatt");
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList leagueNodes = doc.getElementsByTagName("league");

            int teamCount = 0;
            int playerCount = 0;

            for (int i = 0; i < leagueNodes.getLength(); i++) {
                Element leagueEl = (Element) leagueNodes.item(i);
                String leagueName = leagueEl.getAttribute("name");

                NodeList teamNodes = leagueEl.getElementsByTagName("team");
                for (int j = 0; j < teamNodes.getLength(); j++) {
                    Element teamEl = (Element) teamNodes.item(j);
                    String teamName = teamEl.getAttribute("name");

                    if (teamName == null || teamName.isBlank()) {
                        continue;
                    }

                    String teamId = slug(leagueName + "-" + teamName);

                    // ha már létezik, ne duplikáljunk
                    if (teamRepository.existsById(teamId)) {
                        continue;
                    }

                    List<Player> players = new ArrayList<>();

                    NodeList playerNodes = teamEl.getElementsByTagName("player");
                    for (int k = 0; k < playerNodes.getLength(); k++) {
                        Element playerEl = (Element) playerNodes.item(k);
                        String shortName = playerEl.getAttribute("name");
                        String fullName = playerEl.getAttribute("fullName");
                        String positionRaw = playerEl.getAttribute("position");

                        if (shortName == null || shortName.isBlank()) {
                            continue;
                        }

                        String position = mapPosition(positionRaw);

                        Player p = Player.builder()
                                .name(shortName)      // rövid név: "Yamal", "Ronaldo", stb.
                                .position(position)   // "GK", "DEF", "MID", "FWD"
                                .build();

                        players.add(p);
                    }

                    Team team = Team.builder()
                            .id(teamId)
                            .name(teamName)
                            .league(leagueName)
                            .color("#FFFFFF")
                            .logoUrl(null) // - lógok a clearbitről, ami nem mukodott az lett letöltve transparent .png-ként
                            .players(players)
                            .build();

                    for (Player p : players) {
                        p.setTeam(team);
                    }

                    teamRepository.save(team);
                    teamCount++;
                    playerCount += players.size();
                }
            }

            System.out.println("✅ XML import kész: " + teamCount + " csapat, " + playerCount + " játékos mentve.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // egyszerű slug képzés csapat ID-hez
    private String slug(String input) {
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return normalized.toLowerCase()
                .replaceAll("[^a-z0-9\\-]", "");
    }
    private String mapPosition(String pos) {
        if (pos == null || pos.isBlank()) return "FWD";
        String p = pos.toUpperCase();

        if (p.equals("GK")) return "GK";

        // védők
        if (p.contains("CB") || p.contains("LB") || p.contains("RB") || p.contains("RWB") || p.contains("LWB")) {
            return "DEF";
        }

        // középpályások
        if (p.contains("CM") || p.contains("CDM") || p.contains("CAM")
                || p.contains("LM") || p.contains("RM")) {
            return "MID";
        }

        // támadók
        if (p.contains("ST") || p.contains("CF") || p.contains("LW") || p.contains("RW")) {
            return "FWD";
        }

        return "FWD";
    }
}
