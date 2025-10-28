package org.example.gamedirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class CredentialsStore {
    private final Path path = Path.of(System.getProperty("user.home"), ".leaderboard_creds");

    public void save(String username, String password) throws IOException {
        Files.writeString(path, username + ":" + password);
    }

    public Optional<String[]> load() {
        try {
            if (!Files.exists(path)) return Optional.empty();
            String s = Files.readString(path).trim();
            String[] parts = s.split(":", 2);
            if (parts.length < 2) return Optional.empty();
            return Optional.of(new String[]{parts[0], parts[1]});
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
