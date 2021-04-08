package ru.mikushov.musicadvisor.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.mikushov.musicadvisor.controller.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class SpotifyAuthenticationService implements AuthenticationService {
    private String accessToken = "";
    private String refreshToken = "";
    private long expiresIn = 3600;

    @Override
    public void authenticate() {
        if (!isAuthenticated()) {
            initAccessToken();
        }
    }

    private void initAccessToken() {
        HttpServer server = null;
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpHandler httpHandler = getAuthHandler(client);

            server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);

            server.createContext("/", httpHandler);
            server.start();

            getSpotifyCode(client);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if (server != null) {
                server.stop(1);
            }
        }
    }

    @Override
    public boolean isAuthenticated() {
        return !accessTokenExpired() && !accessToken.isEmpty();
    }

    @Override
    public String getAccessToken() {

        if (accessTokenExpired()) {
            refreshToken();
        } else {
            System.out.println("token valid");
        }

        return accessToken;
    }

    private void refreshToken() {
        try {
            System.out.println("refresh token");
            HttpClient client = HttpClient.newHttpClient();
            String parameters = parsePostBodyParameter(Map.of("grant_type", "refresh_token", "refresh_token", refreshToken));
            setNewCredential(getJsonObject(client, parameters));
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private String parsePostBodyParameter(Map<String, String> bodyParameter) {
        return bodyParameter
                .entrySet()
                .stream()
                .map(entry -> String.join("=",
                        URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                ).collect(Collectors.joining("&"));
    }

    private boolean accessTokenExpired() {
        long current = System.currentTimeMillis();
        System.out.println(current + " token expire at: " + expiresIn);
        System.out.println(formatDate(current) + " token expire at: " + formatDate(expiresIn));

        return expiresIn < current;
    }

    private HttpHandler getAuthHandler(HttpClient client) {
        return exchange -> {
            try {
                System.out.println("code received\n" +
                        "making http request for access_token...");

                final String query = exchange.getRequestURI().getQuery();
                final String responseText = "Got the code. Return back to your program.";
                exchange.sendResponseHeaders(200, responseText.length());
                exchange.getResponseBody().write(responseText.getBytes());
                exchange.getResponseBody().close();

                final String code = query.substring(5);
                JsonObject jsonObject = getJsonObject(client, "grant_type=authorization_code&code=" + code + "&redirect_uri=" + Config.REDIRECT_URI);
                setNewCredential(jsonObject);
                refreshToken = getValue(jsonObject, "refresh_token");

                System.out.println("---SUCCESS---");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private void setNewCredential(JsonObject jsonObject) {
        accessToken = getValue(jsonObject, "access_token");
        expiresIn = System.currentTimeMillis() + (Long.parseLong(getValue(jsonObject, "expires_in"))) * 1000;
        String dateFormatted = formatDate(expiresIn);
        System.out.println("token expire at: " + dateFormatted);
    }

    private String formatDate(long date1) {
        Date date = new Date(date1);
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }

    private String getValue(JsonObject jsonObject, String type) {
        return jsonObject.get(type).getAsString();
    }

    private JsonObject getJsonObject(HttpClient client, String bodyParameter) throws IOException, InterruptedException {
        final String base64 = encodeBase64(Config.CLIENT_ID + ":" + Config.CLIENT_SECRET);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyParameter);

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + base64)
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .POST(bodyPublisher)
                .build();

        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("response:");

        String body = response.body();
        System.out.println(body);
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private String encodeBase64(String string) {
        return Base64.getEncoder().withoutPadding().encodeToString(string.getBytes());
    }

    private void getSpotifyCode(HttpClient client) throws IOException, InterruptedException {
        String requestUri = "https://accounts.spotify.com/authorize?client_id=" + Config.CLIENT_ID + "&redirect_uri=" + Config.REDIRECT_URI + "&response_type=code";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUri))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("use this link to request the access code:\n" + requestUri + "\nwaiting for code...");
//        System.out.println(response.body());
    }

}
