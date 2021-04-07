package ru.mikushov.musicadvisor.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.repository.AlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MusicRepository;
import ru.mikushov.musicadvisor.service.AuthenticationService;
import ru.mikushov.musicadvisor.service.MusicServiceImpl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static ru.mikushov.musicadvisor.controller.Command.PLAYLISTS;

public class AppController extends Controller {
    private final AuthenticationService authenticationService;
    private final MusicServiceImpl musicService;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final MusicRepository categoryMusicRepository;
    private final MusicRepository featuredMusicRepository;

    private final Map<String, String> apiRouter;

    public AppController(AlbumCategoryRepository albumCategoryRepository, MusicRepository categoryMusicRepository, MusicRepository featuredMusicRepository, Map<String, String> apiUrls, AuthenticationService authenticationService, MusicServiceImpl musicService) {
        this.albumCategoryRepository = albumCategoryRepository;
        this.categoryMusicRepository = categoryMusicRepository;
        this.featuredMusicRepository = featuredMusicRepository;
        this.apiRouter = apiUrls;
        this.authenticationService = authenticationService;
        this.musicService = musicService;
    }

    public void handleNewCommand() {
        List<Album> albumList = this.musicService.getMusicAlbum(authenticationService.getAccessToken());
        albumList.forEach(System.out::println);
    }

    public void handleFeaturedCommand() {
        System.out.println("---FEATURED---");
        List<Music> allFeaturedMusic = this.musicService.getFeaturedMusic(authenticationService.getAccessToken());
        allFeaturedMusic.forEach(music -> System.out.println(music.getName() + "\n" + music.getUrl()));
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
}
