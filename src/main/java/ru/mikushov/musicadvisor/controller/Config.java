package ru.mikushov.musicadvisor.controller;

public class Config {
    public static final String USAGE_MESSAGE = "featured — a list of Spotify-featured playlists with their links fetched from API;\n" +
            "new — a list of new albums with artists and links on Spotify;\n" +
            "categories — a list of all available categories on Spotify (just their names);\n" +
            "playlists C_NAME, where C_NAME is the name of category. The list contains playlists of this category and their links on Spotify;\n" +
            "exit shuts down the application.";
    public static final String CLIENT_ID = "56c9c163d86e4522879985ad33d65805";
    public static final String CLIENT_SECRET = "0746a39525aa4ade9ce380a840b929c0";
    public static final String REDIRECT_URI = "http://localhost:8080/";
}
