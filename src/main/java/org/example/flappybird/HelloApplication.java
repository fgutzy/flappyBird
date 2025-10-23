package org.example.flappybird;

// FlappyGame.java
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
    private Circle player;
    private double velocity = 0;
    private final double GRAVITY = 0.35;
    private final double JUMP_VELOCITY = -7.5;
    private final ArrayList<Rectangle[]> pipes = new ArrayList<>();
    private Pane root;
    private long lastSpawn = 0;
    private final long SPAWN_INTERVAL = 1_200_000_000L; // nanoseconds (1.2s)
    private double scrollSpeed = 2.5;
    private Text scoreText;
    private int score = 0;
    private final Random rand = new Random();
    private boolean running = true;

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
        primaryStage.setTitle("Flappy-like (JavaFX)");
        primaryStage.show();

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!running) return;
                update(now);
            }
        };
        loop.start();
    }

    private void jump() {
        velocity = JUMP_VELOCITY;
    }

    private void update(long now) {
        // Physics
        velocity += GRAVITY;
        player.setTranslateY(player.getTranslateY() + velocity);

        // Spawn pipes
        if (now - lastSpawn > SPAWN_INTERVAL) {
            spawnPipes();
            lastSpawn = now;
        }

        // Move pipes and check collision & scoring
        Iterator<Rectangle[]> it = pipes.iterator();
        while (it.hasNext()) {
            Rectangle[] pair = it.next();
            Rectangle top = pair[0], bottom = pair[1];
            top.setTranslateX(top.getTranslateX() - scrollSpeed);
            bottom.setTranslateX(bottom.getTranslateX() - scrollSpeed);

            // Scoring: when pipe passes player
            if (!top.getProperties().containsKey("scored") && top.getTranslateX() + top.getWidth() < player.getTranslateX()) {
                top.getProperties().put("scored", true);
                score++;
                scoreText.setText("Score: " + score);
                // optional: add visual feedback
            }

            // Collision
            if (player.getBoundsInParent().intersects(top.getBoundsInParent()) ||
                    player.getBoundsInParent().intersects(bottom.getBoundsInParent())) {
                gameOver();
            }

            // Remove off-screen
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
        // reset
        root.getChildren().clear();
        pipes.clear();
        player.setTranslateX(100);
        player.setTranslateY(HEIGHT / 2);
        velocity = 0;
        score = 0;
        scoreText.setText("Score: 0");
        root.getChildren().addAll(player, scoreText);
        running = true;
        lastSpawn = 0;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
