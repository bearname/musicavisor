package ru.mikushov.musicadvisor.repository;

import ru.mikushov.musicadvisor.model.AlbumCategory;

import java.util.List;

public interface AlbumCategoryRepository {

    List<AlbumCategory> getAll();
    void add(AlbumCategory category) ;

    boolean hasCategory(String id);

    AlbumCategory findById(String id) ;

    AlbumCategory findByName(String name) ;
}
