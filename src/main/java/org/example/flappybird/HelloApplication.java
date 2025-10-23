package org.example.flappybird;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class HelloApplication extends Application {
    private static final double WIDTH = 400, HEIGHT = 600;

    // Physics / rendering constants (real units)
    private final double GRAVITY = 1000.0;       // px / s^2
    private final double JUMP_VELOCITY = -350.0; // px / s
    private final double SCROLL_SPEED = 150.0;   // px / s
    private final double SPAWN_INTERVAL = 1.2;   // seconds

    // Fixed timestep config
    private static final double FIXED_DT = 1.0 / 60.0; // seconds per physics step
    private static final double MAX_ACCUM = 0.25;      // clamp large frame gaps

    private Pane root;
    private Circle player;
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

        player = new Circle(20, Color.ORANGERED);
        player.setTranslateX(100);
        player.setTranslateY(HEIGHT / 2);

        scoreText = new Text(10, 30, "Score: 0");
        scoreText.setFont(Font.font(20));

        root.getChildren().addAll(player, scoreText);

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
        primaryStage.setTitle("Flappy-like (JavaFX) - Fixed Timestep");
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

                // rendering can simply reflect current state (no complex interpolation here)
                // nodes are updated inside physicsUpdate
            }
        };
        loop.start();
    }

    private void physicsUpdate(double dt) {
        // Apply gravity and integrate position
        velocity += GRAVITY * dt;
        player.setTranslateY(player.getTranslateY() + velocity * dt);

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
                scoreText.setText("Score: " + score);
            }

            if (player.getBoundsInParent().intersects(top.getBoundsInParent()) ||
                    player.getBoundsInParent().intersects(bottom.getBoundsInParent())) {
                gameOver();
            }

            if (top.getTranslateX() + top.getWidth() < -50) {
                root.getChildren().removeAll(top, bottom);
                it.remove();
            }
        }

        // Ground / ceiling collision
        if (player.getTranslateY() - player.getRadius() < 0 || player.getTranslateY() + player.getRadius() > HEIGHT) {
            gameOver();
        }
    }

    private void jump() {
        velocity = JUMP_VELOCITY;
    }

    private void spawnPipes() {
        double gap = 150;
        double minY = 80;
        double maxY = HEIGHT - 80 - gap;
        double y = minY + rand.nextDouble() * (maxY - minY);

        Rectangle top = new Rectangle(60, y);
        top.setFill(Color.DARKGREEN);
        top.setTranslateX(WIDTH);
        top.setTranslateY(0);

        Rectangle bottom = new Rectangle(60, HEIGHT - (y + gap));
        bottom.setFill(Color.DARKGREEN);
        bottom.setTranslateX(WIDTH);
        bottom.setTranslateY(y + gap);

        pipes.add(new Rectangle[]{top, bottom});
        root.getChildren().addAll(top, bottom);
    }

    private void gameOver() {
        running = false;
        Text go = new Text(WIDTH / 2 - 80, HEIGHT / 2, "Game Over\nPress Space to restart");
        go.setFont(Font.font(20));
        go.setFill(Color.BLACK);
        root.getChildren().add(go);
    }

    private void restart() {
        root.getChildren().clear();
        pipes.clear();
        player.setTranslateX(100);
        player.setTranslateY(HEIGHT / 2);
        velocity = 0;
        score = 0;
        scoreText.setText("Score: 0");
        root.getChildren().addAll(player, scoreText);
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
