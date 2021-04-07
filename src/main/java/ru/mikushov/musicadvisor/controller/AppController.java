package ru.mikushov.musicadvisor.controller;

import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.service.ExportService;
import ru.mikushov.musicadvisor.service.MusicService;
import ru.mikushov.musicadvisor.service.MusicServiceImpl;

import java.util.List;

public class AppController extends Controller {
    private final MusicService musicService;
    private final ExportService exportService;

    public AppController(MusicService musicService, ExportService exportService) {
        this.musicService = musicService;
        this.exportService = exportService;
    }
//    public AppController(MusicService musicService) {
//        this.musicService = musicService;
//    }
    public void handleNewCommand() {
        List<Album> albumList = this.musicService.getNewReleasesMusic();
        albumList.forEach(System.out::println);
    }

    public void handleFeaturedCommand() {
        System.out.println("---FEATURED---");
        List<Music> allFeaturedMusic = this.musicService.getFeaturedMusicList();
        displayMusic(allFeaturedMusic);
    }

    public void handleCategoriesCommand() {
        System.out.println("---CATEGORIES---");
        List<AlbumCategory> all = musicService.getAlbumCategoryList();
        all.forEach(category -> System.out.println(category.getName()));
    }

    public void handlePlaylistCommand(String categoryName) {
        System.out.println("---MOOD PLAYLISTS---");
        List<Music> musics = this.musicService.getMusicByCategoryName(categoryName);
        if (musics.isEmpty()){
            System.out.println("Unknown category name.");
        }

        displayMusic(musics);
    }

    public void handleExitCommand() {
        System.out.println("---GOODBYE!---");
    }

//    public boolean isAuthenticated() {
//        return this.authenticationService.isAuthenticated();
//    }

    public void handleAuthCommand() {
        System.out.println("Authenticate");
//        this.authenticationService.authenticate();
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

    public void handleCacheCommand() {
        this.musicService.updateCache();
    }

    public void handleExportCommand() {
        System.out.println("Ex");
        this.exportService.export();
    }

    public void handleLoadCommand(String fileName) {
        System.out.println("Load");
        this.exportService.load(fileName);
    }
}
