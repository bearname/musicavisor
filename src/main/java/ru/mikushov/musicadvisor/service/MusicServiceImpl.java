package ru.mikushov.musicadvisor.service;

import ru.mikushov.musicadvisor.infrostructure.SpotifyClient;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.model.SearchType;
import ru.mikushov.musicadvisor.repository.AlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MusicRepository;

import java.util.ArrayList;
import java.util.List;

public class MusicServiceImpl implements MusicService {

    private final MusicRepository featuredMusicRepository;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final MusicRepository categoryMusicRepository;
    private final SpotifyClient spotifyClient;

    public MusicServiceImpl(MusicRepository featuredMusicRepository, AlbumCategoryRepository albumCategoryRepository, MusicRepository categoryMusicRepository, SpotifyClient spotifyClient) {
        this.featuredMusicRepository = featuredMusicRepository;
        this.albumCategoryRepository = albumCategoryRepository;
        this.categoryMusicRepository = categoryMusicRepository;
        this.spotifyClient = spotifyClient;
    }

    @Override
    public List<Album> getNewReleasesMusic() {
        try {
            return spotifyClient.getNewReleaseMusic();
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void addFeaturedMusic(List<Music> musicList) {
        musicList.forEach(featuredMusicRepository::add);
    }

    public List<Music> getFeaturedMusicList() {
        List<Music> featuredPlaylists = featuredMusicRepository.getAll();

        if (featuredPlaylists.isEmpty()) {
            featuredPlaylists = updateFeaturedCache();
        }

        return featuredPlaylists;
    }

    @Override
    public List<AlbumCategory> getAlbumCategoryList() {
        try {
            List<AlbumCategory> all = albumCategoryRepository.getAll();
            if (all.isEmpty()) {
                all = updateCacheOfAlbumCategories();
            }

            return all;
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Music> getMusicByCategoryName(String categoryName) {
        if (albumCategoryRepository.isEmpty()) {
            updateCacheOfAlbumCategories();
        }

        final AlbumCategory albumCategory = albumCategoryRepository.findByName(categoryName);
        List<Music> musicList = new ArrayList<>();
        if (albumCategory != null) {
            if (categoryMusicRepository.isEmpty()) {
                updateCacheOfCategoryMusicRepository(albumCategory);
            }

            musicList = categoryMusicRepository.getAll();
        }

        return musicList;
    }

    @Override
    public void updateCache() {
        updateFeaturedCache();
        List<AlbumCategory> albumCategories = updateCacheOfAlbumCategories();
        albumCategories.forEach(this::updateCacheOfCategoryMusicRepository);
    }

    @Override
    public void search(SearchType type, String searchQuery) {
        List<?> list = spotifyClient.search(type, searchQuery);

    }

    private void updateCacheOfCategoryMusicRepository(AlbumCategory albumCategory) {
        System.out.println("update Cache Of Category Music ");
        List<Music> musics = spotifyClient.getMusicByCategory(albumCategory);
        saveMusicToRepository(musics, categoryMusicRepository);
    }

    private List<Music> updateFeaturedCache() {
        System.out.println("Update Featured playlist cache");
        List<Music> featuredPlaylists = spotifyClient.getFeaturedPlaylists();

        for (Music music : featuredPlaylists) {
            featuredMusicRepository.add(music);
        }

        featuredPlaylists = featuredMusicRepository.getAll();
        return featuredPlaylists;
    }

    private List<AlbumCategory> updateCacheOfAlbumCategories() {
        System.out.println("update Cache Of Album Categories");
        List<AlbumCategory> categories = spotifyClient.getAlbumCategories();

        for (AlbumCategory category : categories) {
            albumCategoryRepository.add(category);
        }

        return categories;
    }

    private void saveMusicToRepository(List<Music> musics, MusicRepository musicRepository) {
        for (Music music : musics) {
            musicRepository.add(music);
        }
    }
}
