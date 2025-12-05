package com.orbitalstriker.backend.modules.game.service;

import com.orbitalstriker.backend.modules.game.dto.StartGameRequest;
import com.orbitalstriker.backend.modules.game.engine.PhysicsEngine;
import com.orbitalstriker.backend.modules.game.engine.Vector2D;
import com.orbitalstriker.backend.modules.game.model.Disc;
import com.orbitalstriker.backend.modules.game.model.GameState;
import com.orbitalstriker.backend.modules.user.model.User;
import com.orbitalstriker.backend.modules.user.repository.UserRepository;
import com.orbitalstriker.backend.modules.user.service.PlayerStatsService;
import com.orbitalstriker.backend.modules.game.model.db.Player;
import com.orbitalstriker.backend.modules.game.model.db.Team;
import com.orbitalstriker.backend.modules.game.repository.TeamRepository;
import com.orbitalstriker.backend.modules.game.repository.PlayerRepository;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@EnableScheduling
public class GameService {

    private final PhysicsEngine physicsEngine;
    private final SimpMessagingTemplate messagingTemplate;
    private GameState gameState;
    private final PlayerStatsService playerStatsService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    private List<Player> cachedPlayerTeam = null;
    private List<Player> cachedAiTeam = null;

    private String lastPlayerTeam = "barcelona";
    private String lastAiTeam = "real";
    private String aiDifficulty = "MEDIUM";
    private String lastPlayerName = "Guest";
    private BigDecimal matchStake = BigDecimal.ZERO;

    // --- belső AI időzítés --- 
    private long lastAiActionTime = 0;

    public GameService(
            PhysicsEngine physicsEngine,
            SimpMessagingTemplate messagingTemplate,
            PlayerStatsService playerStatsService,
            UserRepository userRepository,
            TeamRepository teamRepository,
            PlayerRepository playerRepository
    ) {
        this.physicsEngine = physicsEngine;
        this.messagingTemplate = messagingTemplate;
        this.playerStatsService = playerStatsService;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.gameState = new GameState();
    }

    //JÁTÉK INDÍTÁS
    @Transactional
    public void startGame(StartGameRequest request) {
        System.out.println("=== JÁTÉK INDÍTÁSA ===");
        this.gameState = new GameState();

        try {
            this.aiDifficulty = request.getDifficulty();
            this.lastPlayerTeam = request.getPlayerTeamId();
            this.lastAiTeam = request.getAiTeamId();
            this.lastPlayerName = request.getPlayerName() != null ? request.getPlayerName() : "Guest";

            if (!"Guest".equals(lastPlayerName)) {
                Optional<User> userOpt = userRepository.findByUsername(lastPlayerName);
                userOpt.ifPresent(user -> {
                    int stakeAmount = 100;
                    this.matchStake = BigDecimal.valueOf(stakeAmount);
                    if (user.getGold() >= stakeAmount) {
                        user.setGold(user.getGold() - stakeAmount);
                        userRepository.save(user);
                        System.out.println("💰 Tét levonva: " + stakeAmount + " gold");
                    } else {
                        System.out.println("⚠️ Nincs elég gold, játék teszt módban indul.");
                    }
                });
            }

            gameState.setHomeScore(0);
            gameState.setAwayScore(0);
            gameState.setGameOver(false);
            gameState.setAiActionInProgress(false);

            resetPositions();
            messagingTemplate.convertAndSend("/topic/game-state", gameState);

        } catch (Exception e) {
            System.err.println("❌ startGame hiba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //KEZDŐPOZÍCIÓK
    private void resetPositions() {
        gameState.getDiscs().clear();

        gameState.getDiscs().add(Disc.builder()
                .id("ball")
                .isBall(true)
                .color("#FFFFFF")
                .position(new Vector2D(400, 300))
                .velocity(new Vector2D(0, 0))
                .radius(10)
                .mass(0.8)
                .damping(0.98)
                .build());

        cachedPlayerTeam = loadTeamById(lastPlayerTeam);
        cachedAiTeam = loadTeamById(lastAiTeam);

        createTeamFromCache(cachedPlayerTeam, "player", -1);
        createTeamFromCache(cachedAiTeam, "enemy_ai", 1);

        gameState.setCurrentTurn("player");
        System.out.println("✅ Csapatok inicializálva, összes bábu: " + gameState.getDiscs().size());
    }

    private List<Player> loadTeamById(String teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            System.out.println("⚠️ Nincs ilyen csapat: " + teamId);
            return List.of();
        }
        Team team = teamOpt.get();
        List<Player> players = playerRepository.findByTeam_Id(team.getId());
        System.out.println("🔹 Betöltve: " + team.getName() + " (" + players.size() + " játékos)");
        return players;
    }

    private void createTeamFromCache(List<Player> players, String ownerId, int side) {
        if (players == null || players.isEmpty()) return;
        String color = "#AAAAAA";
        if (players.get(0).getTeam() != null && players.get(0).getTeam().getColor() != null) {
            color = players.get(0).getTeam().getColor();
        }

        double centerX = (side == -1) ? 200 : 600;
        double goalX = (side == -1) ? 80 : 720;
        double forwardX = (side == -1) ? 380 : 420;

        for (int i = 0; i < Math.min(5, players.size()); i++) {
            Player p = players.get(i);
            double x = switch (p.getPosition()) {
                case "GK" -> goalX;
                case "DEF" -> centerX;
                case "FWD" -> forwardX;
                default -> centerX;
            };
            double y = switch (i) {
                case 0 -> 300;
                case 1 -> 150;
                case 2 -> 450;
                case 3 -> 250;
                default -> 350;
            };
            addDisc(ownerId + "_" + p.getPosition() + "_" + i, p.getName(), color, x, y);
        }
    }

    //FŐ CIKLUS
    @Scheduled(fixedRate = 16)
    public void gameLoop() {
        try {
            if (gameState.isGameOver()) {
                messagingTemplate.convertAndSend("/topic/game-state", gameState);
                return;
            }

            physicsEngine.update(gameState);
            for (Disc d : gameState.getDiscs()) handleFieldBoundaries(d);
            checkGoals();

            boolean isMoving = gameState.getDiscs().stream()
                    .anyMatch(d -> d.getVelocity().length() > 0.25);

            long now = System.currentTimeMillis();

            if (!isMoving && "enemy_ai".equals(gameState.getCurrentTurn())
                    && !gameState.isAiActionInProgress()
                    && (now - lastAiActionTime > 800)) {

                gameState.setAiActionInProgress(true);
                lastAiActionTime = now;

                new Thread(() -> {
                    try {
                        Thread.sleep(600);
                        playAiTurn();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        gameState.setAiActionInProgress(false);
                    }
                }).start();
            }

            messagingTemplate.convertAndSend("/topic/game-state", gameState);

        } catch (Exception e) {
            System.err.println("⚠️ GameLoop error: " + e.getMessage());
        }
    }

    //falak
    private void handleFieldBoundaries(Disc disc) {
        double x = disc.getPosition().getX();
        double y = disc.getPosition().getY();
        double r = disc.getRadius();

        double minX = 25, maxX = 775, minY = 25, maxY = 575;
        double goalTop = 220, goalBottom = 380;

        if (x - r < minX && !(y > goalTop && y < goalBottom)) {
            disc.getPosition().setX(minX + r);
            disc.getVelocity().setX(-disc.getVelocity().getX() * 0.6);
        }

        if (x + r > maxX && !(y > goalTop && y < goalBottom)) {
            disc.getPosition().setX(maxX - r);
            disc.getVelocity().setX(-disc.getVelocity().getX() * 0.6);
        }

        if (y - r < minY) {
            disc.getPosition().setY(minY + r);
            disc.getVelocity().setY(-disc.getVelocity().getY() * 0.6);
        }

        if (y + r > maxY) {
            disc.getPosition().setY(maxY - r);
            disc.getVelocity().setY(-disc.getVelocity().getY() * 0.6);
        }
    }

    //gol-detektalas
    private void checkGoals() {
        Disc ball = gameState.getDiscs().stream()
                .filter(Disc::isBall)
                .findFirst().orElse(null);
        if (ball == null) return;

        double x = ball.getPosition().getX();
        double y = ball.getPosition().getY();
        double goalTop = 220, goalBottom = 380;

        boolean goal = false;
        if (x < 25 && y > goalTop && y < goalBottom) {
            gameState.setAwayScore(gameState.getAwayScore() + 1);
            goal = true;
        } else if (x > 775 && y > goalTop && y < goalBottom) {
            gameState.setHomeScore(gameState.getHomeScore() + 1);
            goal = true;
        }

        if (goal) {
            System.out.println("⚽ GÓÓÓL!");

            if (gameState.getHomeScore() >= 3 || gameState.getAwayScore() >= 3) {
                gameState.setGameOver(true);
                String winner = (gameState.getHomeScore() > gameState.getAwayScore()) ? lastPlayerTeam : lastAiTeam;
                System.out.println("🏁 MECCS VÉGE! Győztes: " + winner);
                messagingTemplate.convertAndSend("/topic/game-state", gameState);
                return;
            }

            resetAfterGoal();
        }
    }

    //gol utan
    private void resetAfterGoal() {
        Disc ball = gameState.getDiscs().stream().filter(Disc::isBall).findFirst().orElse(null);
        if (ball != null) {
            ball.getPosition().setX(400);
            ball.getPosition().setY(300);
            ball.getVelocity().setX(0);
            ball.getVelocity().setY(0);
        }

        gameState.getDiscs().removeIf(d -> !d.isBall());
        createTeamFromCache(cachedPlayerTeam, "player", -1);
        createTeamFromCache(cachedAiTeam, "enemy_ai", 1);

        gameState.setCurrentTurn("player");
        messagingTemplate.convertAndSend("/topic/game-state", gameState);

        new Thread(() -> {
            try {
                Thread.sleep(1300);
                gameState.setCurrentTurn("enemy_ai");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    //AI TURN
    private void playAiTurn() {
        Disc ball = gameState.getDiscs().stream().filter(Disc::isBall).findFirst().orElse(null);
        if (ball == null) return;

        double aggression = switch (aiDifficulty.toUpperCase()) {
            case "EASY" -> 0.045;
            case "MEDIUM" -> 0.065;
            default -> 0.085;
        };

        Disc closestAi = null;
        double minDist = Double.MAX_VALUE;

        for (Disc ai : gameState.getDiscs()) {
            if (!ai.getId().startsWith("enemy_ai")) continue;
            double dx = ball.getPosition().getX() - ai.getPosition().getX();
            double dy = ball.getPosition().getY() - ai.getPosition().getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < minDist) {
                minDist = dist;
                closestAi = ai;
            }
        }

        if (closestAi != null) {
            double dx = ball.getPosition().getX() - closestAi.getPosition().getX();
            double dy = ball.getPosition().getY() - closestAi.getPosition().getY();

            dx += (Math.random() - 0.5) * 25;
            dy += (Math.random() - 0.5) * 25;

            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 260) {
                closestAi.setVelocity(new Vector2D(dx * aggression, dy * aggression));
                new Thread(() -> {
                    try {
                        Thread.sleep(1100);
                        gameState.setCurrentTurn("player");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        }
    }

    //YOU - lövés
    public void processPlayerShot(String playerId, double vecX, double vecY) {
        if (gameState.isGameOver()) return;
        if (!"player".equals(gameState.getCurrentTurn())) return;

        boolean moving = gameState.getDiscs().stream()
                .anyMatch(d -> d.getVelocity().length() > 0.25);
        if (moving) return;

        gameState.getDiscs().stream()
                .filter(d -> d.getId().equals(playerId))
                .findFirst()
                .ifPresent(p -> {
                    p.setVelocity(new Vector2D(vecX, vecY));
                    gameState.setCurrentTurn("enemy_ai");
                });
    }

    private void addDisc(String id, String name, String color, double x, double y) {
        gameState.getDiscs().add(Disc.builder()
                .id(id)
                .name(name)
                .color(color)
                .position(new Vector2D(x, y))
                .velocity(new Vector2D(0, 0))
                .radius(18)
                .mass(2.0)
                .damping(0.95)
                .isBall(false)
                .build());
    }
}



