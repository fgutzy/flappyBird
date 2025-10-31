package org.example.gamedirectory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//todo: one httpclient class for each action( register, login, submit score, get leaderboard)?
public class HttpClientGame {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String baseUrl;

    public HttpClientGame(Properties cfg) {
        Objects.requireNonNull(cfg, "cfg");
        String url = cfg.getProperty("api.url");
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Missing required property: api.url");
        }
        this.baseUrl = url;
    }

    public HttpClientGame(String cfgPath) {
        this.baseUrl = cfgPath;
    }

    public boolean register(String username, String password) throws IOException {
        var map = new HashMap<String, Object>();
        map.put("username", username);
        map.put("password", password);
        String json = gson.toJson(map);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> r;

        try {
            r = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() < 200 || r.statusCode() >= 300) {
                throw new IOException("register failed: " + r.statusCode() + " " + r.body());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    //todo: is it ok to base logic purely on response code? and is boolean appropriate?
    public boolean login(String username, String password) throws IOException {
        var map = new HashMap<String, Object>();
        map.put("username", username);
        map.put("password", password);
        String json = gson.toJson(map);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> r;
        try {
            System.out.println("Sending login request for user: " + username + " and password: " + password);
            r = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("Login response: " + r.statusCode() + " " + r.body());
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (r.statusCode() < 200 || r.statusCode() >= 300) {
            throw new IOException("login failed: " + r.statusCode() + " " + r.body());
        }
        return true;
    }

    //todo: only submit for logged in users
    public void submitScore(String username, String password, int score) throws IOException {
        var map = new HashMap<String, Object>();
        map.put("username", username);
        map.put("password", password);
        map.put("highScore", score);
        String json = gson.toJson(map);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/score"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> r;
        try {
            r = client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (r.statusCode() < 200 || r.statusCode() >= 300) {
            throw new IOException("register failed: " + r.statusCode() + " " + r.body());
        }
    }

    public List<Map<String, Object>> getLeaderboard() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/leaderboard"))
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IOException("leaderboard failed: " + res.statusCode());
        }
        Type t = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(res.body(), t);
    }

    public int getUserHighScore(String username) throws IOException, InterruptedException {
        List<Map<String, Object>> list = getLeaderboard();
        for (var e : list) {
            if (username.equals(e.get("username"))) {
                Object hs = e.get("highScore");
                if (hs instanceof Number) return ((Number) hs).intValue();
                try { return Integer.parseInt(String.valueOf(hs)); } catch (Exception ex) { return 0; }
            }
        }
        return 0;
    }
}

