package org.example.apiservice.dto;

import java.time.Instant;
/*
DTO for leaderboard entries.
 */
public class LeaderboardEntry {
    public String username;
    public int highScore;

    public LeaderboardEntry(String username, int highScore) {
        this.username = username;
        this.highScore = highScore;
    }
}
