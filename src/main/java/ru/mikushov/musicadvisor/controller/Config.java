package ru.mikushov.musicadvisor.controller;

public class Config {
    public static final String USAGE_MESSAGE = "featured — a list of Spotify-featured playlists with their links fetched from API;\n" +
            "new — a list of new albums with artists and links on Spotify;\n" +
            "categories — a list of all available categories on Spotify (just their names);\n" +
            "playlists C_NAME, where C_NAME is the name of category. The list contains playlists of this category and their links on Spotify;\n" +
            "exit shuts down the application.";

    public static final String CLIENT_ID = "";
    public static final String CLIENT_SECRET = "";
    public static final String REDIRECT_URI = "http://localhost:8080/";
}
