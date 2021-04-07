package ru.mikushov.musicadvisor.controller;

import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.repository.AlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MemoryMusicRepository;
import ru.mikushov.musicadvisor.service.AuthenticationService;
import ru.mikushov.musicadvisor.service.MusicServiceImpl;

import java.io.IOException;
import java.util.*;

public class AppController extends Controller {
    private final AuthenticationService authenticationService;
    private final MusicServiceImpl musicService;
    private final MemoryMusicRepository categoryMusicRepository;

    public AppController(MemoryMusicRepository categoryMusicRepository, AuthenticationService authenticationService, MusicServiceImpl musicService) {
        this.categoryMusicRepository = categoryMusicRepository;
        this.authenticationService = authenticationService;
        this.musicService = musicService;
    }

    public void handleNewCommand() {
        List<Album> albumList = this.musicService.getMusicAlbum(authenticationService.getAccessToken());
        albumList.forEach(System.out::println);
    }

    public void handleFeaturedCommand() {
        System.out.println("---FEATURED---");
        List<Music> allFeaturedMusic = this.musicService.getFeaturedMusicList(authenticationService.getAccessToken());
        allFeaturedMusic.forEach(music -> System.out.println(music.getName() + "\n" + music.getUrl()));
    }

    public void handleCategoriesCommand() {
        System.out.println("---CATEGORIES---");
        List<AlbumCategory> all = musicService.getAlbumCategoryList(authenticationService.getAccessToken());
        all.forEach(category -> System.out.println(category.getName()));
    }

    public void handlePlaylistCommand(String categoryName) {
        System.out.println("---MOOD PLAYLISTS---");
        List<Music> musics = this.musicService.getMusicByCategoryName(categoryName, authenticationService.getAccessToken());
        if (musics.isEmpty()){
            System.out.println("Unknown category name.");
        }

        displayMusic(musics);
//        final var albumCategory = albumCategoryRepository.findByName(categoryName);
//        if (albumCategory == null) {
//            System.out.println("Unknown category name.");
//        } else {
//            fillCategoryMusicRepository(albumCategory);
//            displayMusicRepository(categoryMusicRepository);
//        }
    }


    public void handleExitCommand() {
        System.out.println("---GOODBYE!---");
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

    private void displayMusic(List<Music> musicList) {
        musicList.forEach(music -> System.out.println(music.getName() + "\n" + music.getUrl()));
    }

//    private HttpResponse<String> getSpotifyInformation(String command) throws IOException, InterruptedException {
//        HttpClient client = HttpClient.newBuilder().build();
//        HttpRequest httpRequest = HttpRequest.newBuilder()
//                .header("Authorization", "Bearer " + this.authenticationService.getAccessToken())
//                .uri(URI.create(apiRouter.get(command)))
//                .GET()
//                .build();
//        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
////        return sendRequest(client, httpRequest);
//    }

//    private void fillCategoryMusicRepository(AlbumCategory byName) throws IOException, InterruptedException {
//        HttpClient client = HttpClient.newBuilder().build();
//        HttpRequest httpRequest = HttpRequest.newBuilder()
//                .header("Authorization", "Bearer " + this.authenticationService.getAccessToken())
//                .uri(URI.create("https://api.spotify.com/v1/browse/categories/" + byName.getId() + "/playlists"))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = sendRequest(client, httpRequest);
//
//        fillCategoryMusicRepository(response);
//    }
//
//    private void fillCategoryMusicRepository(HttpResponse<String> response) {
//        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
//        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();
//        System.out.println(response.body());
//
//        parseJson(musics, categoryMusicRepository);
//
//        displayMusicRepository(categoryMusicRepository);
//    }
}
