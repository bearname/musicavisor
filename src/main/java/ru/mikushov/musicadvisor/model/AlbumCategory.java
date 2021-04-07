package ru.mikushov.musicadvisor.model;

public class AlbumCategory {
    private final String id;
    private final String name;

    public AlbumCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AlbumCategory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
