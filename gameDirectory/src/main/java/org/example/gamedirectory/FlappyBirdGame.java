package org.example.gamedirectory;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
//import javafx.scene.media.Media;
//import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class FlappyBirdGame extends Application {

    /* =========================================================
     *  STATIC CONSTANTS
     * ========================================================= */
    private static final double WIDTH = 400;
    private static final double HEIGHT = 600;

    private static final double PLAYER_RADIUS = 18.0;
    private static final double SPAWN_INTERVAL = 1.2;
    private static final double HITBOX_SCALE = 0.95;

    private static final double FIXED_DT = 1.0 / 60.0;
    private static final double MAX_ACCUM = 0.25;

    /* =========================================================
     *  INSTANCE VARIABLES
     * ========================================================= */

    // ---- Networking / Authentication ----
    private HttpClientGame httpClientGame;
    private AuthenticationScreen authScreen;
    private String loggedInUsername = "guest";
    private String loggedInPassword;

    // ---- JavaFX Root + UI ----
    private Pane root;
    private Group player;
    private Text scoreText;

    private final Logger logger = Logger.getLogger(getClass().getName());

    // ---- Game State ----
    private double velocity = 0;
    private int score = 0;
    private int highscore = 0;

    private boolean running = true;
    private boolean hasJumpedOnce = false;

    // ---- Pipes + Random ----
    private final ArrayList<Rectangle[]> pipes = new ArrayList<>();
    private final Random rand = new Random();

    // ---- Game Loop Timing ----
    private long prevTime = 0;
    private double accumulator = 0.0;
    private double spawnTimer = SPAWN_INTERVAL;

    // ---- Audio ----
//    private MediaPlayer deathSound;
//    private MediaPlayer swingSound;


    /* =========================================================
     *  JAVAFX ENTRY POINT
     * ========================================================= */
    @Override
    public void start(Stage primaryStage) {
        httpClientGame = new HttpClientGame("https://api.myveryownhomenetwork.site/api");
        authScreen = new AuthenticationScreen(httpClientGame);

        authScreen.show(primaryStage, () -> startGame(primaryStage));
    }


    /* =========================================================
     *  GAME INITIALIZATION
     * ========================================================= */
    private void startGame(Stage primaryStage) {

        loggedInUsername = authScreen.getCurrentUsername();
        loggedInPassword = authScreen.getCurrentPassword();
        logger.info("logging works with user: " + loggedInUsername);

        if (!loggedInUsername.equals("guest")) {
            try {
                logger.info("updating highscore variable");
                highscore = httpClientGame.getUserHighScore(loggedInUsername);
                logger.info("highscore: " + highscore);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        root = new Pane();
        root.setPrefSize(WIDTH, HEIGHT);

        // Sky gradient
        Rectangle sky = new Rectangle(WIDTH, HEIGHT);
        sky.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#87CEEB")), new Stop(1, Color.web("#BFE9FF"))));

        // Ground
        Rectangle ground = new Rectangle(0, HEIGHT - 60, WIDTH, 60);
        ground.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#5DB85D")), new Stop(1, Color.web("#3A8A3A"))));
        ground.setStroke(Color.web("#2E7D2E"));

        // Bird
        player = createBird();
        player.setTranslateX(100);
        player.setTranslateY(HEIGHT / 2);

        // Score Text
        scoreText = new Text("0");
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        scoreText.setFill(Color.WHITE);
        scoreText.setStroke(Color.web("#00000055"));
        scoreText.setTranslateX(WIDTH / 2 - 10);
        scoreText.setTranslateY(40);
        scoreText.setTextOrigin(VPos.TOP);

        // Start Hint
        Text startHint = new Text(WIDTH / 2 - 100, HEIGHT / 2 - 100, "Press SPACE to Start");
        startHint.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        startHint.setX(WIDTH / 2 - startHint.getBoundsInLocal().getWidth() / 2);
        startHint.setFill(Color.WHITE);
        startHint.setStroke(Color.BLACK);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ferdi's Bird");
        primaryStage.setResizable(false);
        primaryStage.show();

        //loadSounds();

        root.getChildren().addAll(sky, ground, player, scoreText, startHint);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                if (running) {
                    if (!hasJumpedOnce) {
                        root.getChildren().remove(startHint);
                        hasJumpedOnce = true;
                    }
                    jump();
                } else {
                    restart();
                    root.getChildren().add(startHint);
                    hasJumpedOnce = false;
                }
            }
        });

        // Main loop
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (prevTime == 0) {
                    prevTime = now;
                    return;
                }

                if (!running) {
                    prevTime = now;
                    return;
                }

                double frameDt = (now - prevTime) / 1_000_000_000.0;
                prevTime = now;

                frameDt = Math.min(frameDt, MAX_ACCUM);
                accumulator += frameDt;

                while (accumulator >= FIXED_DT) {
                    physicsUpdate();
                    accumulator -= FIXED_DT;
                }

                scoreText.setTranslateX(WIDTH / 2 - scoreText.getLayoutBounds().getWidth() / 2);
            }
        };
        loop.start();
    }


    /* =========================================================
     *  RENDERING HELPERS
     * ========================================================= */
    private Group createBird() {
        RadialGradient bodyGrad = new RadialGradient(0, 0.1, 0.2, 0.2, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFD36E")), new Stop(1, Color.web("#FF9A2E")));

        Circle body = new Circle(PLAYER_RADIUS);
        body.setFill(bodyGrad);
        body.setEffect(new DropShadow(6, Color.gray(0, 0.4)));

        Circle eyeWhite = new Circle(PLAYER_RADIUS * 0.28, Color.WHITE);
        eyeWhite.setTranslateX(PLAYER_RADIUS * 0.45);
        eyeWhite.setTranslateY(-PLAYER_RADIUS * 0.3);

        Circle pupil = new Circle(PLAYER_RADIUS * 0.12, Color.BLACK);
        pupil.setTranslateX(PLAYER_RADIUS * 0.55);
        pupil.setTranslateY(-PLAYER_RADIUS * 0.3);

        Polygon beak = new Polygon(
                PLAYER_RADIUS * 0.8, 0.0,
                PLAYER_RADIUS * 1.4, -PLAYER_RADIUS * 0.18,
                PLAYER_RADIUS * 1.4, PLAYER_RADIUS * 0.18
        );
        beak.setFill(Color.web("#FFB347"));
        beak.setEffect(new DropShadow(4, Color.gray(0, 0.35)));

        return new Group(body, eyeWhite, pupil, beak);
    }


    /* =========================================================
     *  GAME LOOP / PHYSICS
     * ========================================================= */
    private void physicsUpdate() {
        if (!hasJumpedOnce) return;

        double gravity = 1000.0;

        velocity += gravity * FIXED_DT;
        player.setTranslateY(player.getTranslateY() + velocity * FIXED_DT);

        double angle = Math.max(-25, Math.min(90, velocity / 6));
        player.setRotate(angle);

        spawnTimer -= FIXED_DT;
        if (spawnTimer <= 0) {
            spawnPipes();
            spawnTimer += SPAWN_INTERVAL;
        }

        Iterator<Rectangle[]> it = pipes.iterator();
        while (it.hasNext()) {
            Rectangle[] pair = it.next();
            Rectangle top = pair[0];
            Rectangle bottom = pair[1];

            double scrollSpeed = 200.0;
            double dx = scrollSpeed * FIXED_DT;
            top.setTranslateX(top.getTranslateX() - dx);
            bottom.setTranslateX(bottom.getTranslateX() - dx);

            if (!top.getProperties().containsKey("scored")
                    && top.getTranslateX() + top.getWidth() < player.getTranslateX()) {

                top.getProperties().put("scored", true);
                score++;
                scoreText.setText(String.valueOf(score));
            }

            double cx = player.getTranslateX();
            double cy = player.getTranslateY();
            double r = PLAYER_RADIUS * HITBOX_SCALE;

            if (circleIntersectsRect(cx, cy, r, top) ||
                    circleIntersectsRect(cx, cy, r, bottom)) {
                gameOver();
            }

            if (top.getTranslateX() + top.getWidth() < -50) {
                root.getChildren().removeAll(top, bottom);
                it.remove();
            }
        }

        if (player.getTranslateY() - PLAYER_RADIUS < 0 ||
                player.getTranslateY() + PLAYER_RADIUS > HEIGHT - 60) {
            gameOver();
        }
    }

    private void spawnPipes() {
        double gap = 120;
        double minY = 80;
        double maxY = HEIGHT - 80 - gap - 60;
        double y = minY + rand.nextDouble() * Math.max(0, (maxY - minY));

        Rectangle top = new Rectangle(60, y);
        top.setArcWidth(14);
        top.setArcHeight(14);
        top.setTranslateX(WIDTH);
        top.setTranslateY(0);
        top.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6BBF6B")), new Stop(1, Color.web("#2E8B57"))));
        top.setEffect(new DropShadow(8, Color.gray(0, 0.35)));

        Rectangle bottom = new Rectangle(60, HEIGHT - (y + gap) - 60);
        bottom.setArcWidth(14);
        bottom.setArcHeight(14);
        bottom.setTranslateX(WIDTH);
        bottom.setTranslateY(y + gap);
        bottom.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6BBF6B")), new Stop(1, Color.web("#2E8B57"))));
        bottom.setEffect(new DropShadow(8, Color.gray(0, 0.35)));

        pipes.add(new Rectangle[]{top, bottom});

        int insertIndex = Math.max(2, root.getChildren().size() - 2);
        root.getChildren().add(insertIndex, top);
        root.getChildren().add(insertIndex + 1, bottom);
    }

    private void jump() {
//        if (swingSound != null) {
//            swingSound.stop();
//            swingSound.play();
//        }
        velocity = -350.0;
    }

    private boolean circleIntersectsRect(double cx, double cy, double r, Rectangle rect) {
        double rx = rect.getTranslateX();
        double ry = rect.getTranslateY();
        double rw = rect.getWidth();
        double rh = rect.getHeight();

        double closestX = clamp(cx, rx, rx + rw);
        double closestY = clamp(cy, ry, ry + rh);

        double dx = cx - closestX;
        double dy = cy - closestY;

        return dx * dx + dy * dy <= r * r;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }


    /* =========================================================
     *  GAME STATE (GAME OVER / RESTART)
     * ========================================================= */
    private void gameOver() {
//        if (deathSound != null) {
//            deathSound.stop();
//            deathSound.play();
//        }

        running = false;

        Text go = new Text(WIDTH / 2 - 120, HEIGHT / 2 - 170, "Game Over\nPress Space to restart");
        go.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        go.setFill(Color.WHITE);
        go.setStroke(Color.BLACK);
        go.setEffect(new DropShadow(6, Color.gray(0, 0.6)));
        root.getChildren().add(go);

        if (score > highscore) {
            highscore = score;

            if (!loggedInUsername.equals("guest")) {
                try {
                    logger.info("submitting new highscore");
                    httpClientGame.submitScore(loggedInUsername, loggedInPassword, highscore);
                    logger.info("submitted highscore with value " + highscore);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Leaderboard Display
        if (!loggedInUsername.equals("guest")) {
            try {
                Object raw = httpClientGame.getLeaderboard();
                logger.info("leaderboard raw: " + raw);

                if (raw instanceof java.util.List<?> list) {
                    double startX = WIDTH / 2 - 120;
                    double startY = HEIGHT / 2 + 30;

                    Text header = new Text(startX, startY + 20, "RANKING");
                    header.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                    header.setFill(Color.GOLD);
                    header.setStroke(Color.BLACK);
                    header.setStrokeWidth(1);
                    header.setEffect(new DropShadow(4, Color.gray(0, 0.6)));
                    root.getChildren().add(header);

                    for (int i = 0; i < list.size(); i++) {
                        Object item = list.get(i);
                        String name = null;
                        Integer userScore = null;

                        if (item instanceof Map<?, ?> map) {
                            Object n = map.get("username");
                            if (n == null) n = map.get("name");
                            if (n == null) n = map.get("user");
                            if (n != null) name = String.valueOf(n).trim();

                            Object s = map.get("highScore");
                            if (s == null) s = map.get("score");
                            if (s == null) s = map.get("points");

                            if (s instanceof Number) {
                                userScore = ((Number) s).intValue();
                            } else if (s != null) {
                                try {
                                    userScore = Integer.parseInt(String.valueOf(s).trim().replaceAll("\\D", ""));
                                } catch (Exception ignored) { }
                            }
                        }

                        logger.info("Leaderboard entry: " + (i + 1) + ". " + name + " - " + userScore);

                        double rowY = startY + 50 + (i * 25);
                        addLeaderboardRow(startX, rowY, (i + 1) + ".", name, userScore);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double ownRowY = (HEIGHT / 2 + 30) + 135;
        addLeaderboardRow(WIDTH / 2 - 120, ownRowY, "-", "Youre Best", highscore);
    }


    private void restart() {
        root.getChildren().clear();
        pipes.clear();

        player = createBird();
        player.setTranslateX(100);
        player.setTranslateY(HEIGHT / 2);

        velocity = 0;
        score = 0;
        scoreText.setText(String.valueOf(score));
        scoreText.setTranslateX(WIDTH / 2 - scoreText.getLayoutBounds().getWidth() / 2);

        root.getChildren().addAll(
                new Rectangle(WIDTH, HEIGHT, new LinearGradient(0, 0, 0, 1, true,
                        CycleMethod.NO_CYCLE, new Stop(0, Color.web("#87CEEB")),
                        new Stop(1, Color.web("#BFE9FF")))),

                new Rectangle(0, HEIGHT - 60, WIDTH, 60) {{
                    setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#5DB85D")), new Stop(1, Color.web("#3A8A3A"))));
                    setStroke(Color.web("#2E7D2E"));
                }},

                player,
                scoreText
        );

        running = true;

        prevTime = 0;
        accumulator = 0.0;
        spawnTimer = SPAWN_INTERVAL;
    }


    /* =========================================================
     *  LEADERBOARD UI
     * ========================================================= */
    private void addLeaderboardRow(double x, double y, String rank, String name, Integer score) {
        Text rankText = new Text(x + 5, y, rank);
        rankText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        rankText.setFill(Color.WHITE);
        rankText.setStroke(Color.BLACK);
        rankText.setEffect(new DropShadow(4, Color.gray(0, 0.6)));
        root.getChildren().add(rankText);

        String displayName = (name != null) ? name : "Unknown";
        Text nameText = new Text(x + 50, y, displayName);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameText.setFill(Color.WHITE);
        nameText.setStroke(Color.BLACK);
        nameText.setEffect(new DropShadow(4, Color.gray(0, 0.6)));
        root.getChildren().add(nameText);

        String displayScore = (score != null) ? String.valueOf(score) : "?";
        Text scoreTextNode = new Text(x + 180, y, displayScore);
        scoreTextNode.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scoreTextNode.setFill(Color.WHITE);
        scoreTextNode.setStroke(Color.BLACK);
        scoreTextNode.setEffect(new DropShadow(4, Color.gray(0, 0.6)));
        root.getChildren().add(scoreTextNode);
    }


    /* =========================================================
     *  UTILITY
     * ========================================================= */
//    private void loadSounds() {
//        try {
//            Media hit = new Media(Objects.requireNonNull(
//                    getClass().getResource("/sounds/death.wav")).toExternalForm());
//            deathSound = new MediaPlayer(hit);
//            deathSound.setOnEndOfMedia(() -> deathSound.stop());
//        } catch (Exception e) {
//            System.err.println("Could not load death sound!");
//            e.printStackTrace();
//        }
//
//        try {
//            Media swing = new Media(Objects.requireNonNull(
//                    getClass().getResource("/sounds/swing.wav")).toExternalForm());
//            swingSound = new MediaPlayer(swing);
//            swingSound.setOnEndOfMedia(() -> swingSound.stop());
//        } catch (Exception e) {
//            System.err.println("Could not load swing sound!");
//            e.printStackTrace();
//        }
//    }

    private static void setupLogging() {
        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            java.io.File logFile = new java.io.File(System.getProperty("user.home"),
                    "flappy_log_" + timestamp + ".txt");
            java.io.PrintStream logStream =
                    new java.io.PrintStream(new java.io.FileOutputStream(logFile, true), true, "UTF-8");
            System.setOut(logStream);
            System.setErr(logStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* =========================================================
     *  MAIN
     * ========================================================= */
    public static void main(String[] args) {
        setupLogging();
        launch(args);
    }
}

