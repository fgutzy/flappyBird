package org.example.gamedirectory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientGame {
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final String baseUrl;

    public HttpClientGame(String cfgPath) {
        this.baseUrl = cfgPath;
        this.client = createHttpClient();
    }

    private HttpClient createHttpClient() {
        try {
            // Get SSL context that trusts all certificates
            SSLContext sslContext = SSLHelper.getTrustAllSSLContext();

            // Disable hostname verification
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm(null);

            // Build HttpClient with disabled SSL checks
            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .sslParameters(sslParameters)
                    .build();

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create HttpClient with disabled SSL", e);
        }
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

    public boolean login(String username, String password) throws IOException {
        System.out.println("http login called with " + username + " " + password);
        System.out.flush();
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
            System.out.flush();
            System.out.println("client details: " + client.toString());
            System.out.flush();
            r = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("Login response: " + r.statusCode() + " " + r.body());
            System.out.flush();
        } catch (Exception e) {
            System.out.println("Login request interrupted: " + e.getMessage());
            e.printStackTrace();
            System.out.flush();
            throw new RuntimeException(e.getMessage());
        }
        if (r.statusCode() < 200 || r.statusCode() >= 300) {
            System.out.println("Login failed with status: " + r.statusCode() + " and body: " + r.body());
            System.out.flush();
            throw new IOException("login failed: " + r.statusCode() + " " + r.body());
        }
        return true;
    }

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
            throw new IOException("submitting score failed: " + r.statusCode() + " " + r.body());
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
        System.out.println("response as json " + gson.fromJson(res.body(), t));
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

