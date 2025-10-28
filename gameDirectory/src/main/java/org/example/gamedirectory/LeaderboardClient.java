package org.example.gamedirectory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class LeaderboardClient {
    private final CredentialsStore store = new CredentialsStore();
    private final HttpClientGame httpClientGame;

    public LeaderboardClient() throws IOException {
        Properties cfg = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            cfg.load(in);
        }
        httpClientGame = new HttpClientGame(cfg);
    }

    // register and persist creds locally
    public void registerAndSave(String username, String password) throws IOException {
        httpClientGame.register(username, password);
        store.save(username, password);
    }

    // attempts submit only if new high
    public void submitIfHigh(String username, String password, int newScore) throws IOException, InterruptedException {
        int current = httpClientGame.getUserHighScore(username);
        if (newScore > current) {
            httpClientGame.submitScore(username, password, newScore);
        }
    }

    public Optional<String[]> loadSavedCredentials() {
        return store.load();
    }
}
