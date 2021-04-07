package ru.mikushov.musicadvisor.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.mikushov.musicadvisor.controller.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class SpotifyAuthenticationService  implements AuthenticationService {
    private String accessToken = "BQAlSyiynj3DVHDkAnz4O4I58Ir2o9seVFTosHBje92rCfBGAt56YUfMK-HdxYdWeXTbI7qHMWB8dK7UJyER4zGHNF8h8XtRzYWCBGavxwsrrcIbNi1xi6pFm18xoeeNv0Rv2yor4XEwUML3M8oKVfs_QN4y0GyX-lM47w";
    private String refreshToken = "";
    private String expires_in;

    @Override
    public void authenticate() {
        if (accessToken.isEmpty()) {
            HttpServer server = null;
            try {
                HttpClient client = HttpClient.newBuilder().build();
                server = HttpServer.create();
                server.bind(new InetSocketAddress(8080), 0);
                HttpHandler httpHandler = getAuthHandler(client);

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
    }

    @Override
    public boolean isAuthenticated() {
        return !accessToken.isEmpty();
    }

    @Override
    public String getAccessToken() {
        return accessToken;
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

                JsonObject jsonObject = getJsonObject(client, query);
                accessToken = getValue(jsonObject, "access_token");
                expires_in = getValue(jsonObject, "expires_in");
                refreshToken = getValue(jsonObject, "refresh_token");

                System.out.println("---SUCCESS---");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private String getValue(JsonObject jsonObject, String type) {
        return jsonObject.get(type).getAsString();
    }

    private JsonObject getJsonObject(HttpClient client, String query) throws IOException, InterruptedException {
        final String base64 = Base64.getEncoder().withoutPadding().encodeToString((Config.CLIENT_ID + ":" + Config.CLIENT_SECRET).getBytes());
        final String code = query.substring(5);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString("grant_type=authorization_code&code=" + code + "&redirect_uri=" + Config.REDIRECT_URI);

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + base64)
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .POST(bodyPublisher)
                .build();

        final HttpResponse<String> response =  client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("response:");

        String body = response.body();
        System.out.println(body);
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private void getSpotifyCode(HttpClient client) throws IOException, InterruptedException {
        String requestUri = "https://accounts.spotify.com/authorize?client_id=" + Config.CLIENT_ID + "&redirect_uri=" + Config.REDIRECT_URI + "&response_type=code";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUri))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("use this link to request the access code:\n" + requestUri + "\nwaiting for code...");

        System.out.println(response.body());
    }
}
