package org.example.apiservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

//todo: understand how to use mongodb with spring boot (i.e. annotations)
@Document(collation = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private int highScore;
    private Instant lastUpdate;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.highScore = 0;
        this.lastUpdate = Instant.now();
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }
    public Instant getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Instant lastUpdate) { this.lastUpdate = lastUpdate; }
}
