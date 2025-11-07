package org.example.flappybird;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AuthenticationScreen {
    private final HttpClientGame httpClientGame;
    private String currentUsername;
    private String currentPassword;

    public AuthenticationScreen(HttpClientGame httpClientGame) {
        this.httpClientGame = httpClientGame;
    }

    public void show(Stage primaryStage, Runnable onAuthSuccess) {
        Stage authStage = new Stage();
        authStage.setTitle("Login Page");

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #87CEEB; -fx-padding: 30;");
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Ferdi's Bird");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(200);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(100);
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText(), onAuthSuccess, authStage));

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(100);
        registerButton.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText(), onAuthSuccess, authStage));

        Button guestButton = new Button("Play as Guest");
        guestButton.setPrefWidth(100);
        guestButton.setOnAction(e -> {
            //todo: change to empty instead of guest - or boolean or so
            currentUsername = "guest";
            currentPassword = "guest";
            authStage.close();
            onAuthSuccess.run();
        });

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton, guestButton);

        root.getChildren().addAll(titleLabel, usernameField, passwordField, buttonBox);

        Scene scene = new Scene(root, 300, 400);
        authStage.setScene(scene);
        authStage.showAndWait();
    }

    private void handleLogin(String username, String password, Runnable onAuthSuccess, Stage authStage) {
        System.out.println("handle login called with values " + username + " "  + password);
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }
        try {
            System.out.println("calling http login");
            System.out.flush();
            boolean successfullyLogin = httpClientGame.login(username, password);
            System.out.println("did call http login with result " + successfullyLogin);
            System.out.flush();
            if (successfullyLogin) {
                currentUsername = username;
                currentPassword = password;
                authStage.close();
                onAuthSuccess.run();
            }
            else showError("Login failed");
        } catch (Exception e) {
            showError("Exception during Login: " + e.getMessage());
        }
    }

    private void handleRegister(String username, String password, Runnable onAuthSuccess, Stage authStage) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }
        try {
            boolean successfullyRegistered = httpClientGame.register(username, password);
            if (successfullyRegistered) {
                currentUsername = username;
                currentPassword = password;
                authStage.close();
                onAuthSuccess.run();
            }
            else showError("Registration Failed");
        } catch (Exception e) {
            showError("Exception during Registration " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    //todo: better without password getter?
    public String getCurrentPassword() {
        return currentPassword;
    }
}
