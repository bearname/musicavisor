package ru.mikushov.musicadvisor;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Artist;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.repository.AlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MusicRepository;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Main {

    public static final String USAGE_MESSAGE = "featured — a list of Spotify-featured playlists with their links fetched from API;\n" +
            "new — a list of new albums with artists and links on Spotify;\n" +
            "categories — a list of all available categories on Spotify (just their names);\n" +
            "playlists C_NAME, where C_NAME is the name of category. The list contains playlists of this category and their links on Spotify;\n" +
            "exit shuts down the application.";
    public static final String C_9_C_163_D_86_E_4522879985_AD_33_D_65805 = "56c9c163d86e4522879985ad33d65805";
    public static final String CLIENT_ID = C_9_C_163_D_86_E_4522879985_AD_33_D_65805;
    public static final String CLIENT_SECRET = "0746a39525aa4ade9ce380a840b929c0";
    public static final String REDIRECT_URI = "http://localhost:8080/";
    public static final String EXIT = "exit";
    private static String accessToken = "BQDBXn4W1JLgZm1ZWtQcD3tTYXvdH0hHeZQAdYrslLasSsffti4bDa3SDvsULB106i43W-NWbva6t9QFNE592sxkjMK32BLm2lS3AtxCFQRm3mUhME7AUthX434KhJ0fVny-3vNBl97HKf7gWAciV9iO5k3YjuV6CLg5Tw";

    public static final String NEW_COMMAND = "new";
    public static final String FEATURED_COMMAND = "featured";
    public static final String CATEGORIES_COMMAND = "categories";
    public static final String PLAYLISTS_COMMAND = "playlists";

    private static final Map<String, String> API_URLS = new HashMap<>() {{
        put(NEW_COMMAND, "https://api.spotify.com/v1/browse/new-releases");
        put(FEATURED_COMMAND, "https://api.spotify.com/v1/browse/featured-playlists");
        put(CATEGORIES_COMMAND, "https://api.spotify.com/v1/browse/categories");
        put(PLAYLISTS_COMMAND, "https://api.spotify.com/v1/browse/categories/{category_id}/playlists");
    }};

    private static final AlbumCategoryRepository albumCategoryRepository = new AlbumCategoryRepository();
    private static final MusicRepository categoryMusicRepository = new MusicRepository();
    private static final MusicRepository featuredMusicRepository = new MusicRepository();

    public static void main(String[] args) {
        printHelpMessage();
        String command = "";
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            try {
                System.out.println("> ");
                command = scanner.nextLine();
                if (command.equals(NEW_COMMAND)) {
                    System.out.println("---NEW RELEASES---");
                    fillAlbumRepository();

                } else if (command.equals("auth")) {
                    handleAuthCommand();
                } else if (command.equals(FEATURED_COMMAND)) {
                    handleFeaturedCommand();
                } else if (command.equals(CATEGORIES_COMMAND)) {
                    handleCategoriesCommand();
                } else if (command.startsWith(PLAYLISTS_COMMAND + " ") && command.substring((PLAYLISTS_COMMAND + " ").length()).length() > 0) {
                    handlePlaylistCommand(command);
                } else if (command.equals(EXIT)) {
                    handleExitCommand();
                    break;
                } else {
                    invalidCommand(command);
                }
                System.out.println(command);
            } catch (InputMismatchException exception) {
                invalidCommand(command);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleExitCommand() {
        System.out.println("---GOODBYE!---");
    }

    private static void handlePlaylistCommand(String command) throws IOException, InterruptedException {
        System.out.println("---MOOD PLAYLISTS---");
        String categoryName = command.substring((PLAYLISTS_COMMAND + " ").length());
        AlbumCategory albumCategory = albumCategoryRepository.findByName(categoryName);
        if (albumCategory == null) {
            System.out.println("Unknown category name.");
        } else {
            fillCategoryMusicRepository(albumCategory);
            displayMusicRepository(categoryMusicRepository, System.out);
        }
    }

    private static void handleCategoriesCommand() throws IOException, InterruptedException {
        System.out.println("---CATEGORIES---");
        fillCategoryRepository();
        albumCategoryRepository.getAll().forEach(category -> System.out.println(category.getName()));
    }

    private static void handleFeaturedCommand() throws IOException, InterruptedException {
        System.out.println("---FEATURED---");
        fillFeaturedMusicRepository();
        displayMusicRepository(featuredMusicRepository, System.out);
    }

    private static void fillFeaturedMusicRepository() throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(FEATURED_COMMAND);
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();

        for (JsonElement music : musics) {
            JsonObject album = music.getAsJsonObject();

            String id = album.get("id").getAsString();
            String name = album.get("name").getAsString();
            String url = album.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            featuredMusicRepository.add(new Music(id, name, url));
        }
    }

    private static void displayMusicRepository(MusicRepository musicRepository, PrintStream printStream) {
        musicRepository.getAll().forEach(music -> printStream.println(music.getName() + "\n" + music.getUrl()));
    }

    private static void fillCategoryMusicRepository(HttpResponse<String> response) {
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();
        System.out.println(response.body());

        for (JsonElement musicJsonElement : musics) {
            JsonObject musicJsonObject = musicJsonElement.getAsJsonObject();

            String id = musicJsonObject.get("id").getAsString();
            String name = musicJsonObject.get("name").getAsString();
            String url = musicJsonObject.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            categoryMusicRepository.add(new Music(id, name, url));
        }

        displayMusicRepository(categoryMusicRepository, System.out);
    }

    private static void fillAlbumRepository() throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(NEW_COMMAND);
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray albums = asJsonObject.get("albums").getAsJsonObject().get("items").getAsJsonArray();

        List<Album> albumList = fillAlbumList(albums);
        albumList.forEach(System.out::println);
    }

    private static List<Album> fillAlbumList(JsonArray albums) {
        List<Album> albumList = new ArrayList<>();
        for (JsonElement albumElement : albums) {

            JsonObject albumObject = albumElement.getAsJsonObject();

            String name = albumObject.get("name").getAsString();
            JsonArray artists = albumObject.get("artists").getAsJsonArray();
            String albumUrl = albumObject.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            Album album = new Album(name, fillArtistList(artists), albumUrl);
            System.out.println(album);
            albumList.add(album);
        }

        return albumList;
    }

    private static List<Artist> fillArtistList(JsonArray artists) {
        List<Artist> artistList = new ArrayList<>();

        for (JsonElement artist : artists) {
            String artistName = artist.getAsJsonObject().get("name").toString();
            String artistId = artist.getAsJsonObject().get("id").toString();
            String artistUrlOnSpotify = artist.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();

            artistList.add(new Artist(artistId, artistName, artistUrlOnSpotify));
        }

        return artistList;
    }

    private static void fillCategoryRepository() throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(CATEGORIES_COMMAND);
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray categories = asJsonObject.get("categories").getAsJsonObject().get("items").getAsJsonArray();

        for (JsonElement category : categories) {

            JsonObject album = category.getAsJsonObject();

            String id = album.get("id").getAsString();
            String name = album.get("name").getAsString();
            albumCategoryRepository.add(new AlbumCategory(id, name));
        }
    }

    private static void fillCategoryMusicRepository(AlbumCategory byName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create("https://api.spotify.com/v1/browse/categories/" +  byName.getId() +"/playlists"))
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(client, httpRequest);

        fillCategoryMusicRepository(response);
    }

    private static HttpResponse<String> getSpotifyInformation(String command) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(API_URLS.get(command)))
                .GET()
                .build();
        return sendRequest(client, httpRequest);
    }

    private static void handleAuthCommand() {
        HttpServer server = null;
        try {
            HttpClient client = HttpClient.newBuilder().build();
            server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            HttpHandler httpHandler = getSpotifyAuthHandler(client);

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

    private static HttpHandler getSpotifyAuthHandler(HttpClient client) {
        return exchange -> {
            try {
                System.out.println("code received\n" +
                        "making http request for access_token...");

                final String query = exchange.getRequestURI().getQuery();
                final String responseText = "Got the code. Return back to your program.";
                exchange.sendResponseHeaders(200, responseText.length());
                exchange.getResponseBody().write(responseText.getBytes());
                exchange.getResponseBody().close();

                accessToken = getAccessToken(client, query);

                System.out.println("---SUCCESS---");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private static String getAccessToken(HttpClient client, String query) throws IOException, InterruptedException {
        final String base64 = Base64.getEncoder().withoutPadding().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
        final String code = query.substring(5);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString("grant_type=authorization_code&code=" + code + "&redirect_uri=" + REDIRECT_URI);

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + base64)
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .POST(bodyPublisher)
                .build();

        final HttpResponse<String> response = sendRequest(client, request);
        System.out.println("response:");

        String body = response.body();
        System.out.println(body);
        JsonObject jo = JsonParser.parseString(body).getAsJsonObject();
        return jo.get("access_token").getAsString();
    }

    private static HttpResponse<String> sendRequest(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void getSpotifyCode(HttpClient client) throws IOException, InterruptedException {
        String requestUri = "https://accounts.spotify.com/authorize?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUri))
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(client, request);
        System.out.println("use this link to request the access code:\n" + requestUri + "\nwaiting for code...");

        System.out.println(response.body());
    }

    private static void invalidCommand(String command) {
        System.out.println("Invalid command '" + command + "'");
        printHelpMessage();
    }

    private static void printHelpMessage() {
        System.out.println(USAGE_MESSAGE);
    }
}