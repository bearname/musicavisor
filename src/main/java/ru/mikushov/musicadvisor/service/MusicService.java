package ru.mikushov.musicadvisor.service;

import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Music;

import java.util.List;

public interface MusicService {
    List<Album> getNewReleasesMusic();

    List<Music> getFeaturedMusicList();

    List<AlbumCategory> getAlbumCategoryList();

    List<Music> getMusicByCategoryName(String categoryName);

    void updateCache();
}
