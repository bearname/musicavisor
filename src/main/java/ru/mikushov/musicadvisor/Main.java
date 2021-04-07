package ru.mikushov.musicadvisor;


import ru.mikushov.musicadvisor.controller.AppController;
import ru.mikushov.musicadvisor.controller.Command;
import ru.mikushov.musicadvisor.repository.MemoryAlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MemoryMusicRepository;
import ru.mikushov.musicadvisor.service.SpotifyAuthenticationService;

import java.io.IOException;
import java.util.*;

public class Main {

    private static final Map<String, String> API_ROUTERS = new HashMap<>() {{
        put(Command.NEW, "https://api.spotify.com/v1/browse/new-releases");
        put(Command.FEATURED, "https://api.spotify.com/v1/browse/featured-playlists");
        put(Command.CATEGORIES, "https://api.spotify.com/v1/browse/categories");
        put(Command.PLAYLISTS, "https://api.spotify.com/v1/browse/categories/{category_id}/playlists");
    }};

    private static final AppController appController = new AppController(new MemoryAlbumCategoryRepository(), new MemoryMusicRepository(), new MemoryMusicRepository(), API_ROUTERS, new SpotifyAuthenticationService());

    public static void main(String[] args) {
        appController.printHelpMessage();
        String command = "";
        Scanner scanner = new Scanner(System.in);

        appController.handleAuthCommand();
        System.out.print("> ");
        while (scanner.hasNext()) {
            try {
                command = scanner.nextLine();
                if (handleCommand(command)) break;
                System.out.println(command);
            } catch (InputMismatchException exception) {
                appController.invalidCommand(command);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            System.out.print("> ");
        }
    }

    private static boolean handleCommand(String command) throws IOException, InterruptedException {
        if (command.equals(Command.NEW)) {
            System.out.println("---NEW RELEASES---");
            appController.fillAlbumRepository();
        } else if (command.equals("auth")) {
            if (appController.isAuthenticated()) {
                appController.handleAuthCommand();
            } else {
                System.out.println("Already authenticated.");
            }
        } else if (command.equals(Command.FEATURED)) {
            appController.handleFeaturedCommand();
        } else if (command.equals(Command.CATEGORIES)) {
            appController.handleCategoriesCommand();
        } else if (command.startsWith(Command.PLAYLISTS + " ") && command.substring((Command.PLAYLISTS + " ").length()).length() > 0) {
            appController.handlePlaylistCommand(command);
        } else if (command.equals(Command.EXIT)) {
            appController.handleExitCommand();
            return true;
        } else {
            appController.invalidCommand(command);
        }
        return false;
    }
}