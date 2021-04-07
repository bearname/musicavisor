package ru.mikushov.musicadvisor.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpHandler;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Artist;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.repository.AlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MusicRepository;
import ru.mikushov.musicadvisor.service.AuthenticationService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static ru.mikushov.musicadvisor.controller.Command.PLAYLISTS;

public class AppController extends Controller {
    private final AuthenticationService authenticationService;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final MusicRepository categoryMusicRepository;
    private final MusicRepository featuredMusicRepository;

    private final Map<String, String> apiRouter;

    public AppController(AlbumCategoryRepository albumCategoryRepository, MusicRepository categoryMusicRepository, MusicRepository featuredMusicRepository, Map<String, String> apiUrls, AuthenticationService authenticationService) {
        this.albumCategoryRepository = albumCategoryRepository;
        this.categoryMusicRepository = categoryMusicRepository;
        this.featuredMusicRepository = featuredMusicRepository;
        this.apiRouter = apiUrls;
        this.authenticationService = authenticationService;
    }

    public void handleExitCommand() {
        System.out.println("---GOODBYE!---");
    }

    public void handlePlaylistCommand(String command) throws IOException, InterruptedException {
        System.out.println("---MOOD PLAYLISTS---");
        String categoryName = command.substring((PLAYLISTS + " ").length());
        AlbumCategory albumCategory = albumCategoryRepository.findByName(categoryName);
        if (albumCategory == null) {
            System.out.println("Unknown category name.");
        } else {
            fillCategoryMusicRepository(albumCategory);
            displayMusicRepository(categoryMusicRepository);
        }
    }

    public void handleCategoriesCommand() throws IOException, InterruptedException {
        System.out.println("---CATEGORIES---");
        fillCategoryRepository();
        albumCategoryRepository.getAll().forEach(category -> System.out.println(category.getName()));
    }

    public void handleFeaturedCommand() throws IOException, InterruptedException {
        System.out.println("---FEATURED---");
        fillFeaturedMusicRepository();
        displayMusicRepository(featuredMusicRepository);
    }

    public void fillAlbumRepository() throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(Command.NEW);
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray albums = asJsonObject.get("albums").getAsJsonObject().get("items").getAsJsonArray();

        List<Album> albumList = fillAlbumList(albums);
        albumList.forEach(System.out::println);
    }

    public boolean isAuthenticated() {
        return this.authenticationService.isAuthenticated();
    }

    public void handleAuthCommand() {
        this.authenticationService.authenticate();
    }

    public void invalidCommand(String command) {
        System.out.println("Invalid command '" + command + "'");
        printHelpMessage();
    }

    public void printHelpMessage() {
        System.out.println(Config.USAGE_MESSAGE);
    }


    private void fillCategoryRepository() throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(Command.CATEGORIES);
        String body = response.body();
        System.out.println(body);
        JsonObject asJsonObject = JsonParser.parseString(body).getAsJsonObject();
        JsonArray categories = asJsonObject.get("categories").getAsJsonObject().get("items").getAsJsonArray();

        for (JsonElement category : categories) {

            JsonObject album = category.getAsJsonObject();

            String id = album.get("id").getAsString();
            String name = album.get("name").getAsString();
            albumCategoryRepository.add(new AlbumCategory(id, name));
        }
    }

    private void fillFeaturedMusicRepository() throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(Command.FEATURED);
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();

        parseJson(musics, featuredMusicRepository);
    }

    private void displayMusicRepository(MusicRepository musicRepository) {
        musicRepository.getAll().forEach(music -> System.out.println(music.getName() + "\n" + music.getUrl()));
    }

    private void fillCategoryMusicRepository(HttpResponse<String> response) {
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();
        System.out.println(response.body());

        parseJson(musics, categoryMusicRepository);

        displayMusicRepository(categoryMusicRepository);
    }

    private HttpResponse<String> getSpotifyInformation(String command) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + this.authenticationService.getAccessToken())
                .uri(URI.create(apiRouter.get(command)))
                .GET()
                .build();
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//        return sendRequest(client, httpRequest);
    }

    private void fillCategoryMusicRepository(AlbumCategory byName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + this.authenticationService.getAccessToken())
                .uri(URI.create("https://api.spotify.com/v1/browse/categories/" + byName.getId() + "/playlists"))
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(client, httpRequest);

        fillCategoryMusicRepository(response);
    }

    private List<Album> fillAlbumList(JsonArray albums) {
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

    private List<Artist> fillArtistList(JsonArray artists) {
        List<Artist> artistList = new ArrayList<>();

        for (JsonElement artist : artists) {
            String artistName = artist.getAsJsonObject().get("name").toString();
            String artistId = artist.getAsJsonObject().get("id").toString();
            String artistUrlOnSpotify = artist.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();

            artistList.add(new Artist(artistId, artistName, artistUrlOnSpotify));
        }

        return artistList;
    }
}
