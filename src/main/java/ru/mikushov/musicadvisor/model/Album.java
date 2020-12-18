package ru.mikushov.musicadvisor.model;

import java.util.List;

public class Album {
    private String name;
    private List<Artist> artistList;
    private String url;

    public Album(String name, List<Artist> artists, String url) {
        this.name = name;
        this.artistList = artists;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public List<Artist> getArtistList() {
        return artistList;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        StringBuilder artists = new StringBuilder();
        for (Artist artist : artistList) {
            artists.append(artist.getName()).append(" ");
        }

        return name + "\n[" +
                artists.toString() + "]\n" +
                url;
    }
}
