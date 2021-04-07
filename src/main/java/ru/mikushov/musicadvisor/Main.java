package ru.mikushov.musicadvisor;


import ru.mikushov.musicadvisor.controller.AppController;
import ru.mikushov.musicadvisor.controller.Command;
import ru.mikushov.musicadvisor.infrostructure.SpotifyClient;
import ru.mikushov.musicadvisor.repository.AlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MemoryAlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MemoryMusicRepository;
import ru.mikushov.musicadvisor.repository.MusicRepository;
import ru.mikushov.musicadvisor.service.ExportService;
import ru.mikushov.musicadvisor.service.JsonExportService;
import ru.mikushov.musicadvisor.service.MusicServiceImpl;
import ru.mikushov.musicadvisor.service.SpotifyAuthenticationService;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Map<String, String> API_ROUTERS = new HashMap<>() {{
        put(Command.NEW, "https://api.spotify.com/v1/browse/new-releases");
        put(Command.FEATURED, "https://api.spotify.com/v1/browse/featured-playlists");
        put(Command.CATEGORIES, "https://api.spotify.com/v1/browse/categories");
        put(Command.PLAYLISTS, "https://api.spotify.com/v1/browse/categories/{category_id}/playlists");
    }};

    private static final AppController appController;

    static {
        SpotifyAuthenticationService authenticationService = new SpotifyAuthenticationService();
        AlbumCategoryRepository albumCategoryRepository = new MemoryAlbumCategoryRepository();
        MusicRepository categoryMusicRepository = new MemoryMusicRepository();
        MusicRepository featuredMusicRepository = new MemoryMusicRepository();

        SpotifyClient spotifyClient = new SpotifyClient(API_ROUTERS, authenticationService);

        MusicServiceImpl musicService = new MusicServiceImpl(featuredMusicRepository, albumCategoryRepository, categoryMusicRepository, spotifyClient);

        ExportService exportService = new JsonExportService(musicService);
        appController = new AppController(musicService, exportService);
    }

    public static void main(String[] args) {
        appController.printHelpMessage();
        String command = "";
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        while (scanner.hasNext()) {
            try {
                command = scanner.nextLine();
                if (handleCommand(command)) break;
                System.out.println(command);
            } catch (InputMismatchException exception) {
                appController.invalidCommand(command);
            }
            System.out.print("> ");
        }
    }

    private static boolean handleCommand(String command) {
        if (command.equals(Command.NEW)) {
            System.out.println("---NEW RELEASES---");
            appController.handleNewCommand();
        } else if (command.equals("auth")) {
            appController.handleAuthCommand();
//            if (appController.isAuthenticated()) {
//            } else {
//                System.out.println("Already authenticated.");
//            }
        } else if (command.equals(Command.FEATURED)) {
            appController.handleFeaturedCommand();
        } else if (command.equals(Command.EXPORT)) {
            appController.handleExportCommand();
        } else if (command.startsWith(Command.LOAD + " ") && getFileName(command).length() > 0) {
            appController.handleLoadCommand(getFileName(command));
        } else if (command.equals(Command.CATEGORIES)) {
            appController.handleCategoriesCommand();
        } else if (command.equals(Command.UPDATE_CACHE)) {
            appController.handleCacheCommand();
        } else if (command.startsWith(Command.PLAYLISTS + " ") && getCategoryName(command).length() > 0) {
            appController.handlePlaylistCommand(getCategoryName(command));
        } else if (command.equals(Command.EXIT)) {
            appController.handleExitCommand();
            return true;
        } else {
            appController.invalidCommand(command);
        }
        return false;
    }


    private static String getFileName(String command) {
        return getSecondaryParameter(command, Command.LOAD);
    }
    private static String getCategoryName(String command) {
        return getSecondaryParameter(command, Command.PLAYLISTS);
    }

    private static String getSecondaryParameter(String command, String playlists) {
        return command.substring((playlists + " ").length());
    }
}