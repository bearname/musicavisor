package ru.mikushov.musicadvisor.repository;

import ru.mikushov.musicadvisor.model.AlbumCategory;

import java.util.ArrayList;
import java.util.List;

public class MemoryAlbumCategoryRepository implements AlbumCategoryRepository {
    private final List<AlbumCategory> albumCategoryList = new ArrayList<>();

    public List<AlbumCategory> getAll() {
        return albumCategoryList;
    }

    public void add(AlbumCategory category) {
        if (!albumCategoryList.contains(category)) {
            albumCategoryList.add(category);
        }
    }

    public boolean hasCategory(String id) {
        for (AlbumCategory albumCategory : albumCategoryList) {
            if (albumCategory.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public AlbumCategory findById(String id) {
        for (AlbumCategory albumCategory : albumCategoryList) {
            if (albumCategory.getId().equals(id)) {
                return albumCategory;
            }
        }

        return null;
    }

    public AlbumCategory findByName(String name) {
        for (AlbumCategory albumCategory : albumCategoryList) {
            if (albumCategory.getName().equals(name)) {
                return albumCategory;
            }
        }

        return null;
    }
}
