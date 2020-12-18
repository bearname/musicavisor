package ru.mikushov.musicadvisor.model;

public class Music {

    private String id;
    private String name;
    private String url;

    public Music(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
