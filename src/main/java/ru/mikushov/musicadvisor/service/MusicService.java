package ru.mikushov.musicadvisor.service;

import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.model.SearchType;

import java.util.List;

public interface MusicService {
    List<Album> getNewReleasesMusic();
    void addFeaturedMusic(List<Music> musicList);

    List<Music> getFeaturedMusicList();

    List<AlbumCategory> getAlbumCategoryList();

    List<Music> getMusicByCategoryName(String categoryName);

    void updateCache();

    void search(SearchType type, String searchQuery);
}
