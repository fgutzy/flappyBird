package org.example.apiservice.dto;

import java.time.Instant;
/*
DTO for leaderboard entries.
 */
public class LeaderboardEntry {
    public String username;
    public int highScore;
    public Instant lastUpdate;

    public LeaderboardEntry(String username, int highScore, Instant lastUpdate) {
        this.username = username;
        this.highScore = highScore;
        this.lastUpdate = lastUpdate;
    }
}
