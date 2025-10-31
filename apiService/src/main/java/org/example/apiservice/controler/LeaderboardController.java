package org.example.apiservice.controler;

import org.example.apiservice.dto.LeaderboardEntry;
import org.example.apiservice.dto.RegisterRequest;
import org.example.apiservice.dto.ScoreRequest;
import org.example.apiservice.model.User;
import org.example.apiservice.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LeaderboardController {
    private final UserRepository repo;

    public LeaderboardController(UserRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (repo.findByUsername(req.username).isPresent()) {
            return ResponseEntity.status(409).body("username already exists");
        }
        User u = new User(req.username, req.password);
        repo.save(u);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/score")
    public ResponseEntity<?> submitScore(@Valid @RequestBody ScoreRequest req) {
        return repo.findByUsername(req.username)
                .map(user -> {
                    if (!user.getPassword().equals(req.password)) {
                        return ResponseEntity.status(401).body("invalid credentials");
                    }
                    if (req.highScore > user.getHighScore()) {
                        user.setHighScore(req.highScore);
                        user.setLastUpdate(Instant.now());
                        repo.save(user);
                    }
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.status(404).body("user not found"));
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard() {
        return repo.findAll().stream()
                .sorted(Comparator.comparingInt(User::getHighScore).reversed())
                .limit(3)
                .map(u -> new LeaderboardEntry(u.getUsername(), u.getHighScore()))
                .collect(Collectors.toList());
    }
}
