package org.example.flappybird;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class HelloApplication extends Application {
    private static final double WIDTH = 400, HEIGHT = 600;
    private static final double PLAYER_RADIUS = 18.0;

    // Physics / rendering constants (real units)
    private final double GRAVITY = 1000.0;       // px / s^2
    private final double JUMP_VELOCITY = -350.0; // px / s
    private final double SCROLL_SPEED = 200.0;   // px / s (increased from 150)
    private final double SPAWN_INTERVAL = 1.2;   // seconds
    private final double HITBOX_SCALE = 0.95;    // collision radius scale (increased from 0.7)

    // Fixed timestep config
    private static final double FIXED_DT = 1.0 / 60.0; // seconds per physics step
    private static final double MAX_ACCUM = 0.25;      // clamp large frame gaps

    private Pane root;
    private Group player; // bird composed of shapes
    private double velocity = 0;
    private final ArrayList<Rectangle[]> pipes = new ArrayList<>();
    private Text scoreText;
    private int score = 0;
    private final Random rand = new Random();
    private boolean running = true;

    // loop state
    private long prevTime = 0;
    private double accumulator = 0.0;
    private double spawnTimer = SPAWN_INTERVAL;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setPrefSize(WIDTH, HEIGHT);

        // Background sky gradient
        Stop[] stops = new Stop[]{
                new Stop(0, Color.web("#87CEEB")), // light sky blue
                new Stop(1, Color.web("#BFE9FF"))  // pale
        };
        Rectangle sky = new Rectangle(WIDTH, HEIGHT);
        sky.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops));

        // Ground strip
        Rectangle ground = new Rectangle(0, HEIGHT - 60, WIDTH, 60);
        ground.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#5DB85D")), new Stop(1, Color.web("#3A8A3A"))));
        ground.setStroke(Color.web("#2E7D2E"));

        // Create a composed bird (Group) with body, eye, beak
        player = createBird();
        player.setTranslateX(100);
        player.setTranslateY(HEIGHT / 2);

        scoreText = new Text("0");
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        scoreText.setFill(Color.WHITE);
        scoreText.setStroke(Color.web("#00000055"));
        scoreText.setTranslateX(WIDTH / 2 - 10);
        scoreText.setTranslateY(40);
        scoreText.setTextOrigin(VPos.TOP);

        // Add children in back-to-front order
        root.getChildren().addAll(sky, ground, player, scoreText);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                if (running) jump();
                else restart();
            }
        });
        scene.setOnMouseClicked(e -> {
            if (running) jump();
            else restart();
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Flappy-like (JavaFX) - Modernized");
        primaryStage.show();

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (prevTime == 0) { // initialize to avoid huge initial dt
                    prevTime = now;
                    return;
                }

                if (!running) {
                    prevTime = now; // keep timers in sync while paused
                    return;
                }

                double frameDt = (now - prevTime) / 1_000_000_000.0;
                prevTime = now;

                // clamp large frame times (e.g., after pause or window drag)
                frameDt = Math.min(frameDt, MAX_ACCUM);
                accumulator += frameDt;

                // run fixed-size physics steps
                while (accumulator >= FIXED_DT) {
                    physicsUpdate(FIXED_DT);
                    accumulator -= FIXED_DT;
                }

                // update score position (keep centered)
                scoreText.setTranslateX(WIDTH / 2 - scoreText.getLayoutBounds().getWidth() / 2);
            }
        };
        loop.start();
    }

    // create a small bird using shapes and gradients
    private Group createBird() {
        // body with radial gradient
        RadialGradient bodyGrad = new RadialGradient(0, 0.1, 0.2, 0.2, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFD36E")), new Stop(1, Color.web("#FF9A2E")));
        Circle body = new Circle(PLAYER_RADIUS);
        body.setFill(bodyGrad);
        body.setEffect(new DropShadow(6, Color.gray(0, 0.4)));

        // eye
        Circle eyeWhite = new Circle(PLAYER_RADIUS * 0.28, Color.WHITE);
        eyeWhite.setTranslateX(PLAYER_RADIUS * 0.45);
        eyeWhite.setTranslateY(-PLAYER_RADIUS * 0.3);
        Circle pupil = new Circle(PLAYER_RADIUS * 0.12, Color.BLACK);
        pupil.setTranslateX(PLAYER_RADIUS * 0.55);
        pupil.setTranslateY(-PLAYER_RADIUS * 0.3);

        // beak (triangle)
        Polygon beak = new Polygon();
        beak.getPoints().addAll(
                PLAYER_RADIUS * 0.8, 0.0,
                PLAYER_RADIUS * 1.4, -PLAYER_RADIUS * 0.18,
                PLAYER_RADIUS * 1.4, PLAYER_RADIUS * 0.18
        );
        beak.setFill(Color.web("#FFB347"));
        beak.setEffect(new DropShadow(4, Color.gray(0, 0.35)));

        Group bird = new Group(body, eyeWhite, pupil, beak);
        bird.setRotate(0);
        return bird;
    }

    private void physicsUpdate(double dt) {
        // Apply gravity and integrate position
        velocity += GRAVITY * dt;
        player.setTranslateY(player.getTranslateY() + velocity * dt);

        // tilt bird based on velocity (smooth clamp)
        double angle = Math.max(-25, Math.min(90, velocity / 6)); // simple mapping
        player.setRotate(angle);

        // Spawn logic using a timer in seconds (deterministic with fixed steps)
        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnPipes();
            spawnTimer += SPAWN_INTERVAL;
        }

        // Move pipes and handle scoring/collision/removal
        Iterator<Rectangle[]> it = pipes.iterator();
        while (it.hasNext()) {
            Rectangle[] pair = it.next();
            Rectangle top = pair[0], bottom = pair[1];

            double dx = SCROLL_SPEED * dt;
            top.setTranslateX(top.getTranslateX() - dx);
            bottom.setTranslateX(bottom.getTranslateX() - dx);

            if (!top.getProperties().containsKey("scored") && top.getTranslateX() + top.getWidth() < player.getTranslateX()) {
                top.getProperties().put("scored", true);
                score++;
                scoreText.setText(String.valueOf(score));
            }

            // replace bounding-box intersects with circle-vs-rect collision to avoid false positives
            double cx = player.getTranslateX();
            double cy = player.getTranslateY();
            double collisionRadius = PLAYER_RADIUS * HITBOX_SCALE; // use constant for easier tuning
            if (circleIntersectsRect(cx, cy, collisionRadius, top) || circleIntersectsRect(cx, cy, collisionRadius, bottom)) {
                gameOver();
            }

            if (top.getTranslateX() + top.getWidth() < -50) {
                root.getChildren().removeAll(top, bottom);
                it.remove();
            }
        }

        // Ground / ceiling collision using PLAYER_RADIUS
        if (player.getTranslateY() - PLAYER_RADIUS < 0 || player.getTranslateY() + PLAYER_RADIUS > HEIGHT - 60) {
            gameOver();
        }
    }

    // circle (cx,cy,r) vs axis-aligned rectangle collision
    private boolean circleIntersectsRect(double cx, double cy, double r, Rectangle rect) {
        double rx = rect.getTranslateX();
        double ry = rect.getTranslateY();
        double rw = rect.getWidth();
        double rh = rect.getHeight();

        // find closest point on rect to circle center
        double closestX = clamp(cx, rx, rx + rw);
        double closestY = clamp(cy, ry, ry + rh);

        double dx = cx - closestX;
        double dy = cy - closestY;
        return dx * dx + dy * dy <= r * r;
    }

    private double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    private void jump() {
        velocity = JUMP_VELOCITY;
    }

    private void spawnPipes() {
        double gap = 120; // reduced from 150 for tighter challenge
        double minY = 80;
        double maxY = HEIGHT - 80 - gap - 60; // avoid ground area
        double y = minY + rand.nextDouble() * Math.max(0, (maxY - minY));

        // rounded pipe rectangles with gradient and shadow
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
        // insert pipes above ground but below player and UI
        int insertIndex = Math.max(2, root.getChildren().size() - 2);
        root.getChildren().add(insertIndex, top);
        root.getChildren().add(insertIndex + 1, bottom);
    }

    private void gameOver() {
        running = false;
        Text go = new Text(WIDTH / 2 - 120, HEIGHT / 2 - 20, "Game Over\nPress Space to restart");
        go.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        go.setFill(Color.WHITE);
        go.setStroke(Color.BLACK);
        go.setEffect(new DropShadow(6, Color.gray(0, 0.6)));
        root.getChildren().add(go);
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
                // recreate background and ground and player and score (simple rebuild)
                new Rectangle(WIDTH, HEIGHT, new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#87CEEB")), new Stop(1, Color.web("#BFE9FF")))),
                new Rectangle(0, HEIGHT - 60, WIDTH, 60) {{
                    setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#5DB85D")), new Stop(1, Color.web("#3A8A3A"))));
                    setStroke(Color.web("#2E7D2E"));
                }},
                player,
                scoreText
        );
        running = true;

        // reset loop state
        prevTime = 0;
        accumulator = 0.0;
        spawnTimer = SPAWN_INTERVAL;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
